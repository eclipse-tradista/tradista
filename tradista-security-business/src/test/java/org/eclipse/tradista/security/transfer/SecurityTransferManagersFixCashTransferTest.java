package org.eclipse.tradista.security.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.transfer.BondTransferManager;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.transfer.EquityTransferManager;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;
import org.eclipse.tradista.security.equityoption.transfer.EquityOptionTransferManager;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.repo.model.RepoTrade;
import org.eclipse.tradista.security.repo.trade.RepoTradeUtil;
import org.eclipse.tradista.security.repo.transfer.RepoTransferUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

@ExtendWith(MockitoExtension.class)
class SecurityTransferManagersFixCashTransferTest {

	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Currency EUR = TradistaTestUtil.EUR;
	private static final Exchange EPA = TradistaTestUtil.createExchange("EPA");

	private static MockedConstruction<ConfigurationBusinessDelegate> mockedConfigConstruction;
	private static MockedStatic<PricerUtil> mockedPricerUtil;
	private static MockedStatic<RepoTradeUtil> mockedRepoTradeUtil;

	@Mock
	private TransferBusinessDelegate transferBusinessDelegate;

	@Mock
	private FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	@Mock
	private ProductInventoryBusinessDelegate productInventoryBusinessDelegate;

	@InjectMocks
	private BondTransferManager bondTransferManager;

	@InjectMocks
	private EquityTransferManager equityTransferManager;

	@InjectMocks
	private EquityOptionTransferManager equityOptionTransferManager;

	@BeforeAll
	static void setUpClass() {
		mockedConfigConstruction = Mockito.mockConstruction(ConfigurationBusinessDelegate.class, (mock, _) -> {
			lenient().when(mock.getScale()).thenReturn((short) 2);
			lenient().when(mock.getRoundingMode()).thenReturn(RoundingMode.HALF_EVEN);
		});
		mockedPricerUtil = Mockito.mockStatic(PricerUtil.class);
		mockedRepoTradeUtil = Mockito.mockStatic(RepoTradeUtil.class);
	}

	@AfterAll
	static void tearDownClass() {
		if (mockedConfigConstruction != null) {
			mockedConfigConstruction.close();
		}
		if (mockedPricerUtil != null) {
			mockedPricerUtil.close();
		}
		if (mockedRepoTradeUtil != null) {
			mockedRepoTradeUtil.close();
		}
	}

	@Test
	void testBondTransferManagerFixCashTransferSetsStatusToKnown() throws TradistaBusinessException {
		Bond bond = Bond.builder(EPA, "FR0000120271").currency(EUR).principal(BigDecimal.valueOf(100_000))
				.couponFrequency(Tenor.ONE_YEAR).referenceRateIndex(TradistaTestUtil.EURIBOR).build();

		CashTransfer transfer = CashTransfer
				.builder(book, TransferPurpose.FLOATING_LEG_INTEREST_PAYMENT, LocalDate.of(2026, 4, 1), EUR)
				.product(bond).fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN)
				.build();
		transfer.setId(10L);

		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.INTEREST_RATE),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(3.5));
		mockedPricerUtil.when(() -> PricerUtil.daysToYear(Tenor.ONE_YEAR)).thenReturn(BigDecimal.valueOf(1.0));

		bondTransferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}

	@Test
	void testEquityTransferManagerFixCashTransferSetsStatusToKnown() throws TradistaBusinessException {
		Equity equity = Equity.builder(EPA, "FR0000120271").currency(EUR).build();

		CashTransfer transfer = CashTransfer.builder(book, TransferPurpose.DIVIDEND, LocalDate.of(2026, 4, 1), EUR)
				.product(equity).fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN)
				.build();
		transfer.setId(20L);

		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.DIVIDEND_YIELD),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(2.5));
		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.EQUITY_PRICE),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(50.0));
		lenient().when(productInventoryBusinessDelegate.getQuantityByDateProductAndBookIds(anyLong(), anyLong(), any()))
				.thenReturn(BigDecimal.valueOf(1000));

		equityTransferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}

	@Test
	void testEquityOptionTransferManagerFixCashTransferSetsStatusToKnown() throws TradistaBusinessException {
		Equity equity = Equity.builder(EPA, "FR0000120271").currency(EUR).build();
		EquityTrade underlying = EquityTrade.builder().product(equity).book(book).counterparty(counterparty)
				.currency(EUR).tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.quantity(BigDecimal.valueOf(100)).amount(BigDecimal.valueOf(50.0)).build();

		EquityOptionTrade trade = EquityOptionTrade.builder().underlying(underlying).book(book)
				.counterparty(counterparty).currency(EUR).tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3)).maturityDate(LocalDate.of(2025, 9, 1))
				.strike(BigDecimal.valueOf(100)).style(VanillaOptionTrade.Style.EUROPEAN).type(OptionTrade.Type.CALL)
				.settlementType(OptionTrade.SettlementType.CASH).amount(BigDecimal.valueOf(5.0)).build();
		trade.setId(30L);
		trade.setBuySell(true);

		CashTransfer transfer = CashTransfer
				.builder(book, TransferPurpose.CASH_SETTLEMENT, LocalDate.of(2026, 4, 1), EUR).trade(trade)
				.fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN).build();
		transfer.setId(30L);

		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.EQUITY_PRICE),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(60.0));

		equityOptionTransferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}

	@Test
	void testRepoTransferUtilFixCashTransferSetsStatusToKnown() throws Exception {
		Field transferDelegateField = RepoTransferUtil.class.getDeclaredField("transferBusinessDelegate");
		transferDelegateField.setAccessible(true);
		Object originalDelegate = transferDelegateField.get(null);
		transferDelegateField.set(null, transferBusinessDelegate);

		try {
			GCRepoTrade trade = GCRepoTrade.builder().book(book).counterparty(counterparty).currency(EUR)
					.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
					.endDate(LocalDate.of(2025, 6, 10)).amount(BigDecimal.valueOf(1_000_000))
					.repoRate(BigDecimal.valueOf(2.5)).build();
			trade.setId(40L);
			trade.setBuySell(true);

			CashTransfer transfer = CashTransfer
					.builder(book, TransferPurpose.CASH_SETTLEMENT, LocalDate.of(2026, 4, 1), EUR).trade(trade)
					.fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN).build();
			transfer.setId(40L);

			mockedRepoTradeUtil.when(() -> RepoTradeUtil.getClosingLegPayment(any(RepoTrade.class), anyLong(),
					any(DayCountConvention.class))).thenReturn(BigDecimal.valueOf(10050));

			RepoTransferUtil.fixCashTransfer(transfer, 1L);

			assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
			verify(transferBusinessDelegate).saveTransfer(transfer);
		} finally {
			transferDelegateField.set(null, originalDelegate);
		}
	}
}
