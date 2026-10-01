package org.eclipse.tradista.fx.fxoption.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;
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

public class FXOptionTradeValidatorTest {

	private static FXOptionTradeValidator validator;
	private static Book book;
	private static LegalEntity counterparty;
	private static Currency eur;
	private static Currency usd;

	@BeforeAll
	public static void setUp() {
		FXTestUtil.setupFXExchange();
		validator = new FXOptionTradeValidator();

		LegalEntity po = new LegalEntity("PO");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		counterparty = new LegalEntity("CP");
		counterparty.setRole(LegalEntity.Role.COUNTERPARTY);
		book = new Book("TradingBook", po);

		eur = new Currency("EUR");
		usd = new Currency("USD");
	}

	private FXTrade createValidUnderlying() {
		return new FXTrade.Builder().currencyOne(eur).currency(usd).amountOne(BigDecimal.valueOf(100_000))
				.amount(BigDecimal.valueOf(110_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 12, 1)).settlementDate(LocalDate.of(2025, 12, 3))
				.type(FXTrade.Type.FX_SPOT).build();
	}

	private FXOptionTrade.Builder createValidTradeBuilder() {
		return new FXOptionTrade.Builder().underlying(createValidUnderlying()).book(book).counterparty(counterparty)
				.currency(usd).amount(BigDecimal.valueOf(5000)).style(VanillaOptionTrade.Style.EUROPEAN)
				.type(OptionTrade.Type.CALL).settlementType(OptionTrade.SettlementType.CASH).settlementDateOffset(2)
				.tradeDate(LocalDate.of(2025, 6, 1)).maturityDate(LocalDate.of(2025, 12, 1))
				.settlementDate(LocalDate.of(2025, 6, 3)).exerciseDate(LocalDate.of(2025, 12, 1));
	}

	@Test
	public void testValidFXOptionTrade() {
		FXOptionTrade trade = createValidTradeBuilder().build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTrade() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(null));
	}

	@Test
	public void testNullUnderlying() {
		FXOptionTrade trade = createValidTradeBuilder().underlying(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testUnderlyingMissingAmountOne() {
		FXTrade underlying = new FXTrade.Builder().currencyOne(eur).currency(usd).amount(BigDecimal.valueOf(110_000))
				.book(book).counterparty(counterparty).tradeDate(LocalDate.of(2025, 12, 1))
				.settlementDate(LocalDate.of(2025, 12, 3)).type(FXTrade.Type.FX_SPOT).build();
		FXOptionTrade trade = createValidTradeBuilder().underlying(underlying).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testUnderlyingZeroOrNegativeAmountOne() {
		FXTrade underlyingZero = new FXTrade.Builder().currencyOne(eur).currency(usd).amountOne(BigDecimal.ZERO)
				.amount(BigDecimal.valueOf(110_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 12, 1)).settlementDate(LocalDate.of(2025, 12, 3))
				.type(FXTrade.Type.FX_SPOT).build();
		FXOptionTrade tradeZero = createValidTradeBuilder().underlying(underlyingZero).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		FXTrade underlyingNeg = new FXTrade.Builder().currencyOne(eur).currency(usd).amountOne(BigDecimal.valueOf(-100))
				.amount(BigDecimal.valueOf(110_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 12, 1)).settlementDate(LocalDate.of(2025, 12, 3))
				.type(FXTrade.Type.FX_SPOT).build();
		FXOptionTrade tradeNeg = createValidTradeBuilder().underlying(underlyingNeg).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

	@Test
	public void testUnderlyingMissingAmountTwo() {
		FXTrade underlying = new FXTrade.Builder().currencyOne(eur).currency(usd).amountOne(BigDecimal.valueOf(100_000))
				.book(book).counterparty(counterparty).tradeDate(LocalDate.of(2025, 12, 1))
				.settlementDate(LocalDate.of(2025, 12, 3)).type(FXTrade.Type.FX_SPOT).build();
		FXOptionTrade trade = createValidTradeBuilder().underlying(underlying).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testUnderlyingZeroOrNegativeAmountTwo() {
		FXTrade underlyingZero = new FXTrade.Builder().currencyOne(eur).currency(usd)
				.amountOne(BigDecimal.valueOf(100_000)).amount(BigDecimal.ZERO).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 12, 1)).settlementDate(LocalDate.of(2025, 12, 3))
				.type(FXTrade.Type.FX_SPOT).build();
		FXOptionTrade tradeZero = createValidTradeBuilder().underlying(underlyingZero).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		FXTrade underlyingNeg = new FXTrade.Builder().currencyOne(eur).currency(usd)
				.amountOne(BigDecimal.valueOf(100_000)).amount(BigDecimal.valueOf(-100)).book(book)
				.counterparty(counterparty).tradeDate(LocalDate.of(2025, 12, 1))
				.settlementDate(LocalDate.of(2025, 12, 3)).type(FXTrade.Type.FX_SPOT).build();
		FXOptionTrade tradeNeg = createValidTradeBuilder().underlying(underlyingNeg).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

	@Test
	public void testNullStyle() {
		FXOptionTrade trade = createValidTradeBuilder().style(null).exerciseDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullSettlementType() {
		FXOptionTrade trade = createValidTradeBuilder().settlementType(null).exerciseDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNegativeSettlementDateOffset() {
		FXOptionTrade trade = createValidTradeBuilder().settlementDateOffset(-1).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullMaturityDate() {
		FXOptionTrade trade = createValidTradeBuilder().maturityDate(null).exerciseDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMaturityDateBeforeTradeDate() {
		FXOptionTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 1))
				.maturityDate(LocalDate.of(2025, 5, 1)).exerciseDate(LocalDate.of(2025, 5, 1)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullSettlementDate() {
		FXOptionTrade trade = createValidTradeBuilder().settlementDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSettlementDateAfterMaturityDate() {
		FXOptionTrade trade = createValidTradeBuilder().maturityDate(LocalDate.of(2025, 12, 1))
				.exerciseDate(LocalDate.of(2025, 12, 1)).settlementDate(LocalDate.of(2025, 12, 2)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testExerciseDateBeforeTradeDate() {
		FXOptionTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 1))
				.exerciseDate(LocalDate.of(2025, 5, 1)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testEuropeanExerciseDateNotEqualToMaturityDate() {
		FXOptionTrade trade = createValidTradeBuilder().style(VanillaOptionTrade.Style.EUROPEAN)
				.maturityDate(LocalDate.of(2025, 12, 1)).exerciseDate(LocalDate.of(2025, 11, 28)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testAmericanExerciseDateAfterMaturityDate() {
		FXOptionTrade trade = createValidTradeBuilder().style(VanillaOptionTrade.Style.AMERICAN)
				.maturityDate(LocalDate.of(2025, 12, 1)).exerciseDate(LocalDate.of(2025, 12, 2)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroOrNegativePremium() {
		FXOptionTrade tradeZero = createValidTradeBuilder().amount(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		FXOptionTrade tradeNeg = createValidTradeBuilder().amount(BigDecimal.valueOf(-500)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

}
