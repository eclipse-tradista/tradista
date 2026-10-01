package org.eclipse.tradista.mm.loandeposit.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.mm.loandeposit.model.DepositTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade.InterestType;
import org.eclipse.tradista.mm.loandeposit.model.LoanTrade;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

public class LoanDepositTradeValidatorTest {

	private static LoanDepositTradeValidator validator;
	private static Book book;
	private static LegalEntity counterparty;
	private static Currency currency;
	private static DayCountConvention dcc;
	private static Index index;

	@BeforeAll
	public static void setUp() {
		validator = new LoanDepositTradeValidator();
		currency = new Currency("EUR");
		LegalEntity po = new LegalEntity("PO");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		counterparty = new LegalEntity("CP");
		counterparty.setRole(LegalEntity.Role.COUNTERPARTY);
		book = new Book("TradingBook", po);
		dcc = new DayCountConvention(DayCountConvention.ACT_360);
		index = new Index("EURIBOR");
	}

	private LoanTrade.Builder createValidLoanTradeBuilder() {
		return new LoanTrade.Builder().fixedRate(BigDecimal.valueOf(3.5)).currency(currency)
				.amount(BigDecimal.valueOf(1_000_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.endDate(LocalDate.of(2026, 6, 3)).paymentFrequency(Tenor.ONE_YEAR).dayCountConvention(dcc)
				.interestPayment(InterestPayment.END_OF_PERIOD).interestType(InterestType.SIMPLE);
	}

	private DepositTrade.Builder createValidDepositTradeBuilder() {
		return new DepositTrade.Builder().fixedRate(BigDecimal.valueOf(2.5)).currency(currency)
				.amount(BigDecimal.valueOf(500_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.endDate(LocalDate.of(2026, 6, 3)).paymentFrequency(Tenor.ONE_YEAR).dayCountConvention(dcc)
				.interestPayment(InterestPayment.END_OF_PERIOD).interestType(InterestType.SIMPLE);
	}

	@Test
	public void testValidLoanTrade() {
		LoanTrade trade = createValidLoanTradeBuilder().build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	public void testValidDepositTrade() {
		DepositTrade trade = createValidDepositTradeBuilder().build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	public void testValidFloatingLoanTrade() {
		LoanTrade trade = new LoanTrade.Builder().floatingRateIndex(index).floatingRateIndexTenor(Tenor.THREE_MONTHS)
				.fixingPeriod(Tenor.THREE_MONTHS).interestFixing(InterestPayment.BEGINNING_OF_PERIOD)
				.spread(BigDecimal.valueOf(0.25)).currency(currency).amount(BigDecimal.valueOf(1_000_000)).book(book)
				.counterparty(counterparty).tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.endDate(LocalDate.of(2026, 6, 3)).paymentFrequency(Tenor.THREE_MONTHS).dayCountConvention(dcc)
				.interestPayment(InterestPayment.END_OF_PERIOD).interestType(InterestType.SIMPLE).build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTrade() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(null));
	}

	@Test
	public void testNullBook() {
		LoanTrade trade = createValidLoanTradeBuilder().book(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCounterparty() {
		LoanTrade trade = createValidLoanTradeBuilder().counterparty(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCurrency() {
		LoanTrade trade = createValidLoanTradeBuilder().currency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullAmount() {
		LoanTrade trade = createValidLoanTradeBuilder().amount(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroAmount() {
		LoanTrade trade = createValidLoanTradeBuilder().amount(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNegativeAmount() {
		LoanTrade trade = createValidLoanTradeBuilder().amount(BigDecimal.valueOf(-1000)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTradeDate() {
		LoanTrade trade = createValidLoanTradeBuilder().tradeDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullSettlementDate() {
		LoanTrade trade = createValidLoanTradeBuilder().settlementDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSettlementDateBeforeTradeDate() {
		LoanTrade trade = createValidLoanTradeBuilder().tradeDate(LocalDate.of(2025, 6, 5))
				.settlementDate(LocalDate.of(2025, 6, 3)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSettlementDateEqualsTradeDate() {
		LoanTrade trade = createValidLoanTradeBuilder().tradeDate(LocalDate.of(2025, 6, 3))
				.settlementDate(LocalDate.of(2025, 6, 3)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullEndDate() {
		LoanTrade trade = createValidLoanTradeBuilder().endDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testEndDateBeforeTradeDate() {
		LoanTrade trade = createValidLoanTradeBuilder().endDate(LocalDate.of(2025, 5, 30)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testEndDateBeforeSettlementDate() {
		LoanTrade trade = createValidLoanTradeBuilder().settlementDate(LocalDate.of(2025, 6, 10))
				.endDate(LocalDate.of(2025, 6, 5)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testEndDateEqualsSettlementDate() {
		LoanTrade trade = createValidLoanTradeBuilder().settlementDate(LocalDate.of(2025, 6, 3))
				.endDate(LocalDate.of(2025, 6, 3)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullPaymentFrequency() {
		LoanTrade trade = createValidLoanTradeBuilder().paymentFrequency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullDayCountConvention() {
		LoanTrade trade = createValidLoanTradeBuilder().dayCountConvention(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullInterestPayment() {
		LoanTrade trade = createValidLoanTradeBuilder().interestPayment(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testBothFixedAndFloatingPresent() {
		LoanTrade trade = createValidLoanTradeBuilder().floatingRateIndex(index).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testBothFixedAndFloatingNull() {
		LoanTrade trade = createValidLoanTradeBuilder().fixedRate(null).floatingRateIndex(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroFixedRate() {
		LoanTrade trade = createValidLoanTradeBuilder().fixedRate(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNegativeFixedRate() {
		LoanTrade trade = createValidLoanTradeBuilder().fixedRate(BigDecimal.valueOf(-1.5)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testFloatingRateMissingFixingPeriod() {
		LoanTrade trade = new LoanTrade.Builder().floatingRateIndex(index).floatingRateIndexTenor(Tenor.THREE_MONTHS)
				.interestFixing(InterestPayment.BEGINNING_OF_PERIOD).currency(currency)
				.amount(BigDecimal.valueOf(1_000_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.endDate(LocalDate.of(2026, 6, 3)).paymentFrequency(Tenor.THREE_MONTHS).dayCountConvention(dcc)
				.interestPayment(InterestPayment.END_OF_PERIOD).interestType(InterestType.SIMPLE).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testFloatingRateMissingInterestFixing() {
		LoanTrade trade = new LoanTrade.Builder().floatingRateIndex(index).floatingRateIndexTenor(Tenor.THREE_MONTHS)
				.fixingPeriod(Tenor.THREE_MONTHS).currency(currency).amount(BigDecimal.valueOf(1_000_000)).book(book)
				.counterparty(counterparty).tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.endDate(LocalDate.of(2026, 6, 3)).paymentFrequency(Tenor.THREE_MONTHS).dayCountConvention(dcc)
				.interestPayment(InterestPayment.END_OF_PERIOD).interestType(InterestType.SIMPLE).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testFloatingRateMissingTenor() {
		LoanTrade trade = new LoanTrade.Builder().floatingRateIndex(index).fixingPeriod(Tenor.THREE_MONTHS)
				.interestFixing(InterestPayment.BEGINNING_OF_PERIOD).currency(currency)
				.amount(BigDecimal.valueOf(1_000_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.endDate(LocalDate.of(2026, 6, 3)).paymentFrequency(Tenor.THREE_MONTHS).dayCountConvention(dcc)
				.interestPayment(InterestPayment.END_OF_PERIOD).interestType(InterestType.SIMPLE).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testInterestPaymentBeforeInterestFixing() {
		LoanTrade trade = new LoanTrade.Builder().floatingRateIndex(index).floatingRateIndexTenor(Tenor.THREE_MONTHS)
				.fixingPeriod(Tenor.THREE_MONTHS).interestFixing(InterestPayment.END_OF_PERIOD).currency(currency)
				.amount(BigDecimal.valueOf(1_000_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.endDate(LocalDate.of(2026, 6, 3)).paymentFrequency(Tenor.THREE_MONTHS).dayCountConvention(dcc)
				.interestPayment(InterestPayment.BEGINNING_OF_PERIOD).interestType(InterestType.SIMPLE).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testCompoundInterestMissingCompoundPeriod() {
		LoanTrade trade = createValidLoanTradeBuilder().interestType(InterestType.COMPOUND).compoundPeriod(null)
				.build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMaturityAndEndDateMismatch() {
		LoanTrade trade = createValidLoanTradeBuilder().maturity(Tenor.ONE_YEAR)
				.settlementDate(LocalDate.of(2025, 6, 3)).endDate(LocalDate.of(2025, 12, 31)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}
}
