package org.eclipse.tradista.ir.ccyswap.validator;

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
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;
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

public class CcySwapTradeValidatorTest {

	private static CcySwapTradeValidator validator;
	private static Book book;
	private static LegalEntity counterparty;
	private static Currency eur;
	private static Currency usd;
	private static Index index;

	@BeforeAll
	public static void setUp() {
		validator = new CcySwapTradeValidator();

		eur = new Currency("EUR");
		usd = new Currency("USD");
		LegalEntity po = new LegalEntity("PO");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		counterparty = new LegalEntity("CP");
		counterparty.setRole(LegalEntity.Role.COUNTERPARTY);
		book = new Book("TradingBook", po);
		index = new Index("EURIBOR");
	}

	private CcySwapTrade.Builder createValidTradeBuilder() {
		return new CcySwapTrade.Builder().currency(eur).currencyTwo(usd).amount(BigDecimal.valueOf(1_000_000))
				.notionalAmountTwo(BigDecimal.valueOf(1_100_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2030, 6, 3)).paymentFrequency(Tenor.SIX_MONTHS)
				.receptionFrequency(Tenor.THREE_MONTHS).paymentInterestPayment(InterestPayment.END_OF_PERIOD)
				.receptionInterestPayment(InterestPayment.END_OF_PERIOD)
				.paymentInterestFixing(InterestPayment.BEGINNING_OF_PERIOD)
				.receptionInterestFixing(InterestPayment.BEGINNING_OF_PERIOD).receptionReferenceRateIndex(index)
				.receptionReferenceRateIndexTenor(Tenor.THREE_MONTHS).interestsToPayFixed(true)
				.paymentFixedInterestRate(BigDecimal.valueOf(2.5))
				.paymentDayCountConvention(new DayCountConvention(DayCountConvention.ACT_360))
				.receptionDayCountConvention(new DayCountConvention(DayCountConvention.ACT_360));
	}

	@Test
	public void testValidCcySwapTrade() {
		CcySwapTrade trade = createValidTradeBuilder().build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTrade() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(null));
	}

	@Test
	public void testNullBook() {
		CcySwapTrade trade = createValidTradeBuilder().book(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCounterparty() {
		CcySwapTrade trade = createValidTradeBuilder().counterparty(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCurrencyOne() {
		CcySwapTrade trade = createValidTradeBuilder().currency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCurrencyTwo() {
		CcySwapTrade trade = createValidTradeBuilder().currencyTwo(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSameCurrencies() {
		CcySwapTrade trade = createValidTradeBuilder().currency(eur).currencyTwo(eur).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTradeDate() {
		CcySwapTrade trade = createValidTradeBuilder().tradeDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullSettlementDate() {
		CcySwapTrade trade = createValidTradeBuilder().settlementDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSettlementDateBeforeTradeDate() {
		CcySwapTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 5))
				.settlementDate(LocalDate.of(2025, 6, 2)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullMaturityDate() {
		CcySwapTrade trade = createValidTradeBuilder().maturityDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMaturityDateBeforeTradeDate() {
		CcySwapTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3)).maturityDate(LocalDate.of(2025, 5, 1)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMaturityDateBeforeSettlementDate() {
		CcySwapTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 10)).maturityDate(LocalDate.of(2025, 6, 5)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullPaymentFrequency() {
		CcySwapTrade trade = createValidTradeBuilder().paymentFrequency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullReceptionFrequency() {
		CcySwapTrade trade = createValidTradeBuilder().receptionFrequency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullReceptionReferenceRateIndex() {
		CcySwapTrade trade = createValidTradeBuilder().receptionReferenceRateIndex(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullReceptionReferenceRateIndexTenor() {
		CcySwapTrade trade = createValidTradeBuilder().receptionReferenceRateIndexTenor(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testReceptionReferenceRateIndexTenorNoTenor() {
		CcySwapTrade trade = createValidTradeBuilder().receptionReferenceRateIndexTenor(Tenor.NO_TENOR).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullReceptionDayCountConvention() {
		CcySwapTrade trade = createValidTradeBuilder().receptionDayCountConvention(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullPaymentDayCountConvention() {
		CcySwapTrade trade = createValidTradeBuilder().paymentDayCountConvention(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testFixedInterestsMissingFixedRate() {
		CcySwapTrade trade = createValidTradeBuilder().interestsToPayFixed(true).paymentFixedInterestRate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroOrNegativeNotionalAmountOne() {
		CcySwapTrade tradeZero = createValidTradeBuilder().amount(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		CcySwapTrade tradeNeg = createValidTradeBuilder().amount(BigDecimal.valueOf(-1000)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

	@Test
	public void testZeroOrNegativeNotionalAmountTwo() {
		CcySwapTrade tradeZero = createValidTradeBuilder().notionalAmountTwo(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		CcySwapTrade tradeNeg = createValidTradeBuilder().notionalAmountTwo(BigDecimal.valueOf(-1000)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

	@Test
	public void testPaymentInterestPaymentBeforePaymentInterestFixing() {
		CcySwapTrade trade = createValidTradeBuilder().paymentInterestPayment(InterestPayment.BEGINNING_OF_PERIOD)
				.paymentInterestFixing(InterestPayment.END_OF_PERIOD).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

}
