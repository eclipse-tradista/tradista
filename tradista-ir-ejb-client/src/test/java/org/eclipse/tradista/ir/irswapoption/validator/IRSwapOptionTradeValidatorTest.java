package org.eclipse.tradista.ir.irswapoption.validator;

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
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;
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

public class IRSwapOptionTradeValidatorTest {

	private static IRSwapOptionTradeValidator validator;
	private static Book book;
	private static LegalEntity counterparty;
	private static Currency currency;
	private static SingleCurrencyIRSwapTrade underlying;

	@BeforeAll
	public static void setUp() {
		validator = new IRSwapOptionTradeValidator();

		currency = new Currency("EUR");
		LegalEntity po = new LegalEntity("PO");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		counterparty = new LegalEntity("CP");
		counterparty.setRole(LegalEntity.Role.COUNTERPARTY);
		book = new Book("TradingBook", po);
		Index index = new Index("EURIBOR");

		underlying = new SingleCurrencyIRSwapTrade.Builder().currency(currency).amount(BigDecimal.valueOf(1_000_000))
				.book(book).counterparty(counterparty).tradeDate(LocalDate.of(2025, 12, 1))
				.settlementDate(LocalDate.of(2025, 12, 3)).maturityDate(LocalDate.of(2030, 12, 3))
				.maturityTenor(Tenor.NO_TENOR).paymentFrequency(Tenor.SIX_MONTHS).receptionFrequency(Tenor.THREE_MONTHS)
				.paymentInterestPayment(InterestPayment.END_OF_PERIOD)
				.receptionInterestPayment(InterestPayment.END_OF_PERIOD)
				.paymentInterestFixing(InterestPayment.BEGINNING_OF_PERIOD)
				.receptionInterestFixing(InterestPayment.BEGINNING_OF_PERIOD).receptionReferenceRateIndex(index)
				.receptionReferenceRateIndexTenor(Tenor.THREE_MONTHS).interestsToPayFixed(true)
				.paymentFixedInterestRate(BigDecimal.valueOf(2.5))
				.paymentDayCountConvention(new DayCountConvention(DayCountConvention.ACT_360))
				.receptionDayCountConvention(new DayCountConvention(DayCountConvention.ACT_360)).build();
	}

	private IRSwapOptionTrade.Builder createValidTradeBuilder() {
		return new IRSwapOptionTrade.Builder().underlying(underlying).book(book).counterparty(counterparty)
				.currency(currency).amount(BigDecimal.valueOf(5000)).strike(BigDecimal.valueOf(2.5))
				.style(VanillaOptionTrade.Style.EUROPEAN).type(OptionTrade.Type.CALL)
				.settlementType(OptionTrade.SettlementType.CASH).settlementDateOffset(2)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2025, 12, 1)).exerciseDate(LocalDate.of(2025, 12, 1));
	}

	@Test
	public void testValidIRSwapOptionTrade() {
		IRSwapOptionTrade trade = createValidTradeBuilder().build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTrade() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(null));
	}

	@Test
	public void testNullUnderlying() {
		IRSwapOptionTrade trade = createValidTradeBuilder().underlying(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullBook() {
		IRSwapOptionTrade trade = createValidTradeBuilder().book(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCounterparty() {
		IRSwapOptionTrade trade = createValidTradeBuilder().counterparty(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCurrency() {
		IRSwapOptionTrade trade = createValidTradeBuilder().currency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTradeDate() {
		IRSwapOptionTrade trade = createValidTradeBuilder().tradeDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullSettlementDate() {
		IRSwapOptionTrade trade = createValidTradeBuilder().settlementDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSettlementDateBeforeTradeDate() {
		IRSwapOptionTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 5))
				.settlementDate(LocalDate.of(2025, 6, 2)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullStyle() {
		IRSwapOptionTrade trade = createValidTradeBuilder().style(null).exerciseDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullSettlementType() {
		IRSwapOptionTrade trade = createValidTradeBuilder().settlementType(null).exerciseDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNegativeSettlementDateOffset() {
		IRSwapOptionTrade trade = createValidTradeBuilder().settlementDateOffset(-1).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullMaturityDate() {
		IRSwapOptionTrade trade = createValidTradeBuilder().maturityDate(null).exerciseDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMaturityDateBeforeTradeDate() {
		IRSwapOptionTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 1))
				.maturityDate(LocalDate.of(2025, 5, 1)).exerciseDate(LocalDate.of(2025, 5, 1)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSettlementDateAfterMaturityDate() {
		IRSwapOptionTrade trade = createValidTradeBuilder().maturityDate(LocalDate.of(2025, 12, 1))
				.exerciseDate(LocalDate.of(2025, 12, 1)).settlementDate(LocalDate.of(2025, 12, 2)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testExerciseDateBeforeTradeDate() {
		IRSwapOptionTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 1))
				.exerciseDate(LocalDate.of(2025, 5, 1)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testEuropeanExerciseDateNotEqualToMaturityDate() {
		IRSwapOptionTrade trade = createValidTradeBuilder().style(VanillaOptionTrade.Style.EUROPEAN)
				.maturityDate(LocalDate.of(2025, 12, 1)).exerciseDate(LocalDate.of(2025, 11, 28)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testAmericanExerciseDateAfterMaturityDate() {
		IRSwapOptionTrade trade = createValidTradeBuilder().style(VanillaOptionTrade.Style.AMERICAN)
				.maturityDate(LocalDate.of(2025, 12, 1)).exerciseDate(LocalDate.of(2025, 12, 2)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullStrike() {
		IRSwapOptionTrade trade = createValidTradeBuilder().strike(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroOrNegativePremium() {
		IRSwapOptionTrade tradeZero = createValidTradeBuilder().amount(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		IRSwapOptionTrade tradeNeg = createValidTradeBuilder().amount(BigDecimal.valueOf(-500)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

}
