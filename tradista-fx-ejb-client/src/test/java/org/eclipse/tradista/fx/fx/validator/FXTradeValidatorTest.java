package org.eclipse.tradista.fx.fx.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.fx.fx.model.FXTrade;
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

public class FXTradeValidatorTest {

	private static FXTradeValidator validator;
	private static Book book;
	private static LegalEntity counterparty;
	private static Currency eur;
	private static Currency usd;

	@BeforeAll
	public static void setUp() {
		FXTestUtil.setupFXExchange();
		validator = new FXTradeValidator();

		LegalEntity po = new LegalEntity("PO");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		counterparty = new LegalEntity("CP");
		counterparty.setRole(LegalEntity.Role.COUNTERPARTY);
		book = new Book("TradingBook", po);

		eur = new Currency("EUR");
		usd = new Currency("USD");
	}

	private FXTrade.Builder createValidTradeBuilder() {
		return new FXTrade.Builder().currencyOne(eur).currency(usd).amountOne(BigDecimal.valueOf(100_000))
				.amount(BigDecimal.valueOf(110_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.type(FXTrade.Type.FX_SPOT);
	}

	@Test
	public void testValidFXTrade() {
		FXTrade trade = createValidTradeBuilder().build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTrade() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(null));
	}

	@Test
	public void testNullBook() {
		FXTrade trade = createValidTradeBuilder().book(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCounterparty() {
		FXTrade trade = createValidTradeBuilder().counterparty(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTradeDate() {
		FXTrade trade = createValidTradeBuilder().tradeDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullSettlementDate() {
		FXTrade trade = createValidTradeBuilder().settlementDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSettlementDateBeforeTradeDate() {
		FXTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 5))
				.settlementDate(LocalDate.of(2025, 6, 2)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullAmountOne() {
		FXTrade trade = createValidTradeBuilder().amountOne(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroOrNegativeAmountOne() {
		FXTrade tradeZero = createValidTradeBuilder().amountOne(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		FXTrade tradeNeg = createValidTradeBuilder().amountOne(BigDecimal.valueOf(-1000)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

	@Test
	public void testNullCurrencyOne() {
		FXTrade trade = createValidTradeBuilder().currencyOne(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNullCurrencyTwo() {
		FXTrade trade = createValidTradeBuilder().currency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testSameCurrencies() {
		FXTrade trade = createValidTradeBuilder().currency(eur).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroOrNegativeAmountTwo() {
		FXTrade tradeZero = createValidTradeBuilder().amount(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeZero));

		FXTrade tradeNeg = createValidTradeBuilder().amount(BigDecimal.valueOf(-500)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(tradeNeg));
	}

}
