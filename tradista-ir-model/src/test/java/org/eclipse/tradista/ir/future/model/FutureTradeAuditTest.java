package org.eclipse.tradista.ir.future.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.tenor.model.Tenor;
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

public class FutureTradeAuditTest {

	private static Book book;
	private static LegalEntity counterparty;
	private static Future future;

	@BeforeAll
	public static void setUp() {
		Currency currency = new Currency("EUR");
		LegalEntity po = new LegalEntity("PO");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		counterparty = new LegalEntity("CP");
		counterparty.setRole(LegalEntity.Role.COUNTERPARTY);
		book = new Book("TradingBook", po);

		FutureContractSpecification spec = new FutureContractSpecification("EURIBOR_3M");
		spec.setExchange(new Exchange("EPA"));
		spec.setCurrency(currency);
		spec.setDayCountConvention(new DayCountConvention(DayCountConvention.ACT_360));
		spec.setReferenceRateIndex(new Index("EURIBOR"));
		spec.setReferenceRateIndexTenor(Tenor.THREE_MONTHS);

		future = new Future.Builder("MAR26", spec).maturityDate(LocalDate.of(2026, 3, 20)).build();
	}

	@Test
	public void testDefaultCreationTimeOnNewFutureTrade() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		FutureTrade trade = new FutureTrade.Builder().future(future).quantity(BigDecimal.valueOf(10))
				.amount(BigDecimal.valueOf(98.5)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3)).build();
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

		FutureTrade trade = new FutureTrade.Builder().future(future).book(book).counterparty(counterparty)
				.creationTime(historicalCreation).lastUpdateTime(historicalUpdate).build();

		assertEquals(historicalCreation, trade.getCreationTime());
		assertEquals(historicalUpdate, trade.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		FutureTrade trade = new FutureTrade.Builder().future(future).book(book).counterparty(counterparty)
				.creationTime(originalCreation).build();

		assertEquals(originalCreation, trade.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		trade.setCreationDate(LocalDate.of(2026, 12, 31));

		assertEquals(originalCreation, trade.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		FutureTrade trade = new FutureTrade.Builder().future(future).book(book).counterparty(counterparty).build();

		assertNotNull(trade.getLastUpdateTime());

		Instant updatedTime = Instant.now().plus(10, ChronoUnit.MINUTES);
		trade.setLastUpdateTime(updatedTime);

		assertEquals(updatedTime, trade.getLastUpdateTime());
	}

	@Test
	public void testToBuilderPreservesAuditFields() {
		Instant creationTime = Instant.parse("2024-02-01T12:00:00Z");
		Instant lastUpdateTime = Instant.parse("2024-02-02T15:00:00Z");

		FutureTrade original = new FutureTrade.Builder().future(future).quantity(BigDecimal.valueOf(10))
				.amount(BigDecimal.valueOf(98.5)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3)).creationTime(creationTime)
				.lastUpdateTime(lastUpdateTime).build();

		FutureTrade copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getProduct(), copy.getProduct());
		assertEquals(original.getQuantity(), copy.getQuantity());
		assertEquals(original.getAmount(), copy.getAmount());
	}

}
