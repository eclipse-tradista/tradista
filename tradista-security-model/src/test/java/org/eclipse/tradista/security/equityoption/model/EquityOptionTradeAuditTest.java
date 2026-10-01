package org.eclipse.tradista.security.equityoption.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;
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

public class EquityOptionTradeAuditTest {

	private static Book book;
	private static LegalEntity counterparty;
	private static Currency currency;
	private static EquityTrade underlyingTrade;

	@BeforeAll
	public static void setUp() {
		Exchange exchange = new Exchange("EPA");
		currency = new Currency("EUR");
		LegalEntity po = new LegalEntity("PO");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		counterparty = new LegalEntity("CP");
		counterparty.setRole(LegalEntity.Role.COUNTERPARTY);
		book = new Book("TradingBook", po);

		Equity equity = new Equity.Builder(exchange, "FR0000120271").currency(currency).build();
		underlyingTrade = new EquityTrade.Builder()
				.product(equity)
				.book(book)
				.counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3))
				.quantity(BigDecimal.valueOf(100))
				.amount(BigDecimal.valueOf(50.0))
				.build();
	}

	@Test
	public void testDefaultCreationTimeOnNewEquityOptionTrade() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		EquityOptionTrade trade = new EquityOptionTrade.Builder()
				.underlying(underlyingTrade)
				.book(book)
				.counterparty(counterparty)
				.currency(currency)
				.tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2025, 9, 1))
				.strike(BigDecimal.valueOf(100))
				.style(VanillaOptionTrade.Style.EUROPEAN)
				.type(OptionTrade.Type.CALL)
				.settlementType(OptionTrade.SettlementType.PHYSICAL)
				.quantity(BigDecimal.valueOf(10))
				.amount(BigDecimal.valueOf(5.0))
				.build();
		Instant after = Instant.now().plus(1, ChronoUnit.SECONDS);

		assertNotNull(trade.getCreationTime());
		assertTrue(trade.getCreationTime().isAfter(before));
		assertTrue(trade.getCreationTime().isBefore(after));
		assertNotNull(trade.getLastUpdateTime());
		assertTrue(trade.getLastUpdateTime().isAfter(before));
		assertTrue(trade.getLastUpdateTime().isBefore(after));
	}

	@Test
	public void testExplicitCreationTimeViaBuilder() {
		Instant historicalCreation = Instant.parse("2024-01-15T10:30:00Z");
		Instant historicalUpdate = Instant.parse("2024-01-16T14:20:00Z");

		EquityOptionTrade trade = new EquityOptionTrade.Builder()
				.underlying(underlyingTrade)
				.book(book)
				.counterparty(counterparty)
				.creationTime(historicalCreation)
				.lastUpdateTime(historicalUpdate)
				.build();

		assertEquals(historicalCreation, trade.getCreationTime());
		assertEquals(historicalUpdate, trade.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		EquityOptionTrade trade = new EquityOptionTrade.Builder()
				.underlying(underlyingTrade)
				.book(book)
				.counterparty(counterparty)
				.creationTime(originalCreation)
				.build();

		assertEquals(originalCreation, trade.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		trade.setCreationDate(LocalDate.of(2026, 12, 31));

		assertEquals(originalCreation, trade.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		EquityOptionTrade trade = new EquityOptionTrade.Builder()
				.underlying(underlyingTrade)
				.book(book)
				.counterparty(counterparty)
				.build();

		assertNotNull(trade.getLastUpdateTime());

		Instant updatedTime = Instant.now().plus(10, ChronoUnit.MINUTES);
		trade.setLastUpdateTime(updatedTime);

		assertEquals(updatedTime, trade.getLastUpdateTime());
	}

	@Test
	public void testToBuilderPreservesAuditFields() {
		Instant creationTime = Instant.parse("2024-02-01T12:00:00Z");
		Instant lastUpdateTime = Instant.parse("2024-02-02T15:00:00Z");

		EquityOptionTrade original = new EquityOptionTrade.Builder()
				.underlying(underlyingTrade)
				.book(book)
				.counterparty(counterparty)
				.currency(currency)
				.tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2025, 9, 1))
				.strike(BigDecimal.valueOf(100))
				.quantity(BigDecimal.valueOf(10))
				.amount(BigDecimal.valueOf(5.0))
				.creationTime(creationTime)
				.lastUpdateTime(lastUpdateTime)
				.build();

		EquityOptionTrade copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getUnderlying().getProduct(), copy.getUnderlying().getProduct());
		assertEquals(original.getStrike(), copy.getStrike());
		assertEquals(original.getQuantity(), copy.getQuantity());
	}

}
