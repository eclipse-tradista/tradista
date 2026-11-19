package org.eclipse.tradista.commercial.commercial.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.commercial.commercial.model.CommercialTrade.CommercialCashFlow;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.junit.jupiter.api.Test;

class CommercialTradeValidatorTest {

	private final CommercialTradeValidator validator = new CommercialTradeValidator();

	private CommercialTrade.Builder baseValidTradeBuilder() {
		return CommercialTrade.builder().book(new Book("BOOK", new LegalEntity("PO")))
				.counterparty(new LegalEntity("CP")).currency(new Currency("EUR")).tradeDate(LocalDate.now())
				.amount(new BigDecimal("1000.00")).documentReference("PO-2026-0001");
	}

	@Test
	void testValidTrade() {
		CommercialCashFlow flow = new CommercialCashFlow();
		flow.setSettlementDate(LocalDate.now().plusDays(30));
		flow.setAmount(new BigDecimal("1000.00"));

		CommercialTrade trade = baseValidTradeBuilder().items(List.of(flow)).build();

		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	void testMissingDocumentReferenceThrowsException() {
		CommercialCashFlow flow = new CommercialCashFlow();
		flow.setSettlementDate(LocalDate.now().plusDays(30));
		flow.setAmount(new BigDecimal("1000.00"));

		CommercialTrade trade = baseValidTradeBuilder().documentReference(" ").items(List.of(flow)).build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testEmptyItemsThrowsException() {
		CommercialTrade trade = baseValidTradeBuilder().items(Collections.emptyList()).build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNonPositiveAmountThrowsException() {
		CommercialCashFlow flow = new CommercialCashFlow();
		flow.setSettlementDate(LocalDate.now().plusDays(30));
		flow.setAmount(BigDecimal.ZERO);

		CommercialTrade trade = baseValidTradeBuilder().items(List.of(flow)).build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testTradeDateAfterCashFlowSettlementDateThrowsException() {
		CommercialCashFlow flow = new CommercialCashFlow();
		flow.setSettlementDate(LocalDate.now().plusDays(5));
		flow.setAmount(new BigDecimal("1000.00"));

		CommercialTrade trade = baseValidTradeBuilder().tradeDate(LocalDate.now().plusDays(10)).items(List.of(flow))
				.build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testFutureSettledCashFlowThrowsException() {
		CommercialCashFlow flow = new CommercialCashFlow();
		flow.setSettlementDate(LocalDate.now().plusDays(5));
		flow.setAmount(new BigDecimal("1000.00"));
		flow.setSettled(true);

		CommercialTrade trade = baseValidTradeBuilder().tradeDate(LocalDate.now()).items(List.of(flow)).build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

}
