package org.eclipse.tradista.ir.ircapfloorcollar.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.fra.model.FRATrade;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;
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

public class IRCapFloorCollarTradeValidatorTest {

	private static final IRCapFloorCollarTradeValidator validator = new IRCapFloorCollarTradeValidator();
	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Currency currency = TradistaTestUtil.EUR;
	private static final FRATrade forwardTrade;

	static {
		forwardTrade = FRATrade.builder().fixedRate(BigDecimal.valueOf(2.5)).startDate(LocalDate.of(2025, 5, 30))
				.maturityDate(LocalDate.of(2025, 8, 30)).referenceRateIndex(TradistaTestUtil.EURIBOR)
				.referenceRateIndexTenor(Tenor.THREE_MONTHS)
				.dayCountConvention(new DayCountConvention(DayCountConvention.ACT_360))
				.interestFixing(InterestPayment.BEGINNING_OF_PERIOD).currency(currency)
				.amount(BigDecimal.valueOf(1_000_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 5, 28)).settlementDate(LocalDate.of(2025, 6, 1)).build();
	}

	private IRCapFloorCollarTrade.Builder createValidTradeBuilder() {
		return IRCapFloorCollarTrade.builder().capStrike(BigDecimal.valueOf(3.0)).irForwardTrade(forwardTrade)
				.currency(currency).amount(BigDecimal.valueOf(10_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 5, 28)).settlementDate(LocalDate.of(2025, 6, 1));
	}

	@Test
	public void testValidIRCapFloorCollarTrade() {
		IRCapFloorCollarTrade trade = createValidTradeBuilder().build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTrade() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(null));
	}

	@Test
	public void testNullBook() {
		IRCapFloorCollarTrade trade = createValidTradeBuilder().book(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCounterparty() {
		IRCapFloorCollarTrade trade = createValidTradeBuilder().counterparty(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCurrency() {
		IRCapFloorCollarTrade trade = createValidTradeBuilder().currency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTradeDate() {
		IRCapFloorCollarTrade trade = createValidTradeBuilder().tradeDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullSettlementDate() {
		IRCapFloorCollarTrade trade = createValidTradeBuilder().settlementDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSettlementDateBeforeTradeDate() {
		IRCapFloorCollarTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 5))
				.settlementDate(LocalDate.of(2025, 6, 2)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullUnderlyingIRForwardTrade() {
		IRCapFloorCollarTrade trade = createValidTradeBuilder().irForwardTrade(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testUnderlyingMaturityDateBeforeSettlementDate() {
		FRATrade badForward = forwardTrade.toBuilder().settlementDate(LocalDate.of(2025, 9, 1))
				.maturityDate(LocalDate.of(2025, 8, 1)).build();
		IRCapFloorCollarTrade trade = createValidTradeBuilder().irForwardTrade(badForward).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testUnderlyingNullAmount() {
		FRATrade badForward = forwardTrade.toBuilder().amount(null).build();
		IRCapFloorCollarTrade trade = createValidTradeBuilder().irForwardTrade(badForward).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testUnderlyingZeroOrNegativeAmount() {
		FRATrade badForwardZero = forwardTrade.toBuilder().amount(BigDecimal.ZERO).build();
		IRCapFloorCollarTrade tradeZero = createValidTradeBuilder().irForwardTrade(badForwardZero).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		FRATrade badForwardNeg = forwardTrade.toBuilder().amount(BigDecimal.valueOf(-100)).build();
		IRCapFloorCollarTrade tradeNeg = createValidTradeBuilder().irForwardTrade(badForwardNeg).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

	@Test
	public void testZeroOrNegativePremium() {
		IRCapFloorCollarTrade tradeZero = createValidTradeBuilder().amount(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		IRCapFloorCollarTrade tradeNeg = createValidTradeBuilder().amount(BigDecimal.valueOf(-500)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

}