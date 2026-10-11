package org.eclipse.tradista.ir.ccyswap.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;
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

class CcySwapTradeValidatorTest {

	private static final CcySwapTradeValidator validator = new CcySwapTradeValidator();
	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Currency eur = TradistaTestUtil.EUR;
	private static final Currency usd = TradistaTestUtil.USD;
	private static final Index index = TradistaTestUtil.EURIBOR;

	private CcySwapTrade.Builder createValidTradeBuilder() {
		return CcySwapTrade.builder().currency(eur).currencyTwo(usd).amount(BigDecimal.valueOf(1_000_000))
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
	void testValidCcySwapTrade() {
		CcySwapTrade trade = createValidTradeBuilder().build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	void testNullTrade() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(null));
	}

	@Test
	void testNullBook() {
		CcySwapTrade trade = createValidTradeBuilder().book(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNullCounterparty() {
		CcySwapTrade trade = createValidTradeBuilder().counterparty(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNullCurrencyOne() {
		CcySwapTrade trade = createValidTradeBuilder().currency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNullCurrencyTwo() {
		CcySwapTrade trade = createValidTradeBuilder().currencyTwo(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testSameCurrencies() {
		CcySwapTrade trade = createValidTradeBuilder().currency(eur).currencyTwo(eur).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNullTradeDate() {
		CcySwapTrade trade = createValidTradeBuilder().tradeDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNullSettlementDate() {
		CcySwapTrade trade = createValidTradeBuilder().settlementDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testSettlementDateBeforeTradeDate() {
		CcySwapTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 5))
				.settlementDate(LocalDate.of(2025, 6, 2)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNullMaturityDate() {
		CcySwapTrade trade = createValidTradeBuilder().maturityDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testMaturityDateBeforeTradeDate() {
		CcySwapTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3)).maturityDate(LocalDate.of(2025, 5, 1)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testMaturityDateBeforeSettlementDate() {
		CcySwapTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 10)).maturityDate(LocalDate.of(2025, 6, 5)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNullPaymentFrequency() {
		CcySwapTrade trade = createValidTradeBuilder().paymentFrequency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNullReceptionFrequency() {
		CcySwapTrade trade = createValidTradeBuilder().receptionFrequency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNullReceptionReferenceRateIndex() {
		CcySwapTrade trade = createValidTradeBuilder().receptionReferenceRateIndex(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNullReceptionReferenceRateIndexTenor() {
		CcySwapTrade trade = createValidTradeBuilder().receptionReferenceRateIndexTenor(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testReceptionReferenceRateIndexTenorNoTenor() {
		CcySwapTrade trade = createValidTradeBuilder().receptionReferenceRateIndexTenor(Tenor.NO_TENOR).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNullReceptionDayCountConvention() {
		CcySwapTrade trade = createValidTradeBuilder().receptionDayCountConvention(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNullPaymentDayCountConvention() {
		CcySwapTrade trade = createValidTradeBuilder().paymentDayCountConvention(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testFixedInterestsMissingFixedRate() {
		CcySwapTrade trade = createValidTradeBuilder().interestsToPayFixed(true).paymentFixedInterestRate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testZeroOrNegativeNotionalAmountOne() {
		CcySwapTrade tradeZero = createValidTradeBuilder().amount(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		CcySwapTrade tradeNeg = createValidTradeBuilder().amount(BigDecimal.valueOf(-1000)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

	@Test
	void testZeroOrNegativeNotionalAmountTwo() {
		CcySwapTrade tradeZero = createValidTradeBuilder().notionalAmountTwo(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		CcySwapTrade tradeNeg = createValidTradeBuilder().notionalAmountTwo(BigDecimal.valueOf(-1000)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

	@Test
	void testPaymentInterestPaymentBeforePaymentInterestFixing() {
		CcySwapTrade trade = createValidTradeBuilder().paymentInterestPayment(InterestPayment.BEGINNING_OF_PERIOD)
				.paymentInterestFixing(InterestPayment.END_OF_PERIOD).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

}