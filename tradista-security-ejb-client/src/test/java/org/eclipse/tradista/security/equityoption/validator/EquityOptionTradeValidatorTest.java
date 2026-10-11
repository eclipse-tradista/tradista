package org.eclipse.tradista.security.equityoption.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;
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

class EquityOptionTradeValidatorTest {

	private static final EquityOptionTradeValidator validator = new EquityOptionTradeValidator();
	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Currency currency = TradistaTestUtil.EUR;
	private static final EquityTrade underlyingTrade;

	static {
		Exchange exchange = TradistaTestUtil.createExchange("EPA");
		Calendar calendar = new Calendar("EPA_CAL");
		exchange.setCalendar(calendar);

		Equity equity = Equity.builder(exchange, "FR0000120271").currency(currency).build();
		underlyingTrade = EquityTrade.builder().product(equity).book(book).counterparty(counterparty).currency(currency)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.quantity(BigDecimal.valueOf(100)).amount(BigDecimal.valueOf(50.0)).build();
	}

	private EquityOptionTrade.Builder createValidTradeBuilder() {
		return EquityOptionTrade.builder().underlying(underlyingTrade).book(book).counterparty(counterparty)
				.currency(currency).tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2025, 9, 1)).strike(BigDecimal.valueOf(100))
				.style(VanillaOptionTrade.Style.EUROPEAN).type(OptionTrade.Type.CALL)
				.settlementType(OptionTrade.SettlementType.PHYSICAL).amount(BigDecimal.valueOf(5.0));
	}

	@Test
	void testValidTrade() {
		EquityOptionTrade trade = createValidTradeBuilder().build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	void testNullTrade() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(null));
	}

	@Test
	void testMissingUnderlying() {
		EquityOptionTrade trade = createValidTradeBuilder().underlying(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testMissingBook() {
		EquityOptionTrade trade = createValidTradeBuilder().book(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testMissingCounterparty() {
		EquityOptionTrade trade = createValidTradeBuilder().counterparty(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testMissingStyle() {
		EquityOptionTrade trade = createValidTradeBuilder().style(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testMissingSettlementType() {
		EquityOptionTrade trade = createValidTradeBuilder().settlementType(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testMissingStrike() {
		EquityOptionTrade trade = createValidTradeBuilder().strike(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testZeroStrike() {
		EquityOptionTrade trade = createValidTradeBuilder().strike(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNegativeStrike() {
		EquityOptionTrade trade = createValidTradeBuilder().strike(BigDecimal.valueOf(-10)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testMissingMaturityDate() {
		EquityOptionTrade trade = createValidTradeBuilder().maturityDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testMaturityDateBeforeTradeDate() {
		EquityOptionTrade trade = createValidTradeBuilder().tradeDate(LocalDate.of(2025, 6, 1))
				.maturityDate(LocalDate.of(2025, 5, 1)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testSettlementDateAfterMaturityDate() {
		EquityOptionTrade trade = createValidTradeBuilder().maturityDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 5)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testMissingSettlementDate() {
		EquityOptionTrade trade = createValidTradeBuilder().settlementDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testZeroAmount() {
		EquityOptionTrade trade = createValidTradeBuilder().amount(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	void testNegativeAmount() {
		EquityOptionTrade trade = createValidTradeBuilder().amount(BigDecimal.valueOf(-5.0)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

}