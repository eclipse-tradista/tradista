package org.eclipse.tradista.fx.fxswap.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.fx.fxswap.model.FXSwapTrade;
import org.eclipse.tradista.fx.test.util.FXTestUtil;
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

public class FXSwapTradeValidatorTest {

	private static final FXSwapTradeValidator validator = new FXSwapTradeValidator();
	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Currency eur = TradistaTestUtil.EUR;
	private static final Currency usd = TradistaTestUtil.USD;

	@BeforeAll
	public static void setUp() {
		FXTestUtil.setupFXExchange();
	}

	private FXSwapTrade.Builder createValidTradeBuilder() {
		return FXSwapTrade.builder().currencyOne(eur).currency(usd).amountOneSpot(BigDecimal.valueOf(100_000))
				.amount(BigDecimal.valueOf(110_000)).amountOneForward(BigDecimal.valueOf(100_000))
				.amountTwoForward(BigDecimal.valueOf(112_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.settlementDateForward(LocalDate.of(2025, 9, 3));
	}

	@Test
	public void testValidFXSwapTrade() {
		FXSwapTrade trade = createValidTradeBuilder().build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTrade() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(null));
	}

	@Test
	public void testNullBook() {
		FXSwapTrade trade = createValidTradeBuilder().book(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCounterparty() {
		FXSwapTrade trade = createValidTradeBuilder().counterparty(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTradeDate() {
		FXSwapTrade trade = createValidTradeBuilder().tradeDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullSettlementDate() {
		FXSwapTrade trade = createValidTradeBuilder().settlementDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSettlementDateBeforeTradeDate() {
		FXSwapTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 5))
				.settlementDate(LocalDate.of(2025, 6, 2)).settlementDateForward(LocalDate.of(2025, 9, 5)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCurrencyOne() {
		FXSwapTrade trade = createValidTradeBuilder().currencyOne(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSameCurrencies() {
		FXSwapTrade trade = createValidTradeBuilder().currencyOne(usd).currency(usd).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullSettlementDateForward() {
		FXSwapTrade trade = createValidTradeBuilder().settlementDateForward(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSettlementDateForwardNotAfterSpotSettlementDate() {
		FXSwapTrade tradeEqual = createValidTradeBuilder().settlementDate(LocalDate.of(2025, 6, 3))
				.settlementDateForward(LocalDate.of(2025, 6, 3)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeEqual));

		FXSwapTrade tradeBefore = createValidTradeBuilder().settlementDate(LocalDate.of(2025, 6, 3))
				.settlementDateForward(LocalDate.of(2025, 6, 2)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeBefore));
	}

	@Test
	public void testNullAmountOneSpot() {
		FXSwapTrade trade = createValidTradeBuilder().amountOneSpot(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroOrNegativeAmountOneSpot() {
		FXSwapTrade tradeZero = createValidTradeBuilder().amountOneSpot(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		FXSwapTrade tradeNeg = createValidTradeBuilder().amountOneSpot(BigDecimal.valueOf(-100)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

	@Test
	public void testNullAmountOneForward() {
		FXSwapTrade trade = createValidTradeBuilder().amountOneForward(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroOrNegativeAmountOneForward() {
		FXSwapTrade tradeZero = createValidTradeBuilder().amountOneForward(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		FXSwapTrade tradeNeg = createValidTradeBuilder().amountOneForward(BigDecimal.valueOf(-100)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

	@Test
	public void testNullAmountTwoForward() {
		FXSwapTrade trade = createValidTradeBuilder().amountTwoForward(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroOrNegativeAmountTwoForward() {
		FXSwapTrade tradeZero = createValidTradeBuilder().amountTwoForward(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		FXSwapTrade tradeNeg = createValidTradeBuilder().amountTwoForward(BigDecimal.valueOf(-100)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

	@Test
	public void testZeroOrNegativeAmountTwoSpot() {
		FXSwapTrade tradeZero = createValidTradeBuilder().amount(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		FXSwapTrade tradeNeg = createValidTradeBuilder().amount(BigDecimal.valueOf(-100)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

}