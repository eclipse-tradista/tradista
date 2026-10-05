package org.eclipse.tradista.security.specificrepo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.workflow.model.Status;
import org.eclipse.tradista.security.bond.model.Bond;
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

public class SpecificRepoTradeAuditTest {

	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Bond bond = Bond.builder(TradistaTestUtil.createExchange("EPA"), "FR0000120271")
			.currency(TradistaTestUtil.EUR).build();
	private static final Status status;

	static {
		status = new Status();
		status.setName("NEW");
	}

	@Test
	public void testDefaultCreationTimeOnNewSpecificRepoTrade() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		SpecificRepoTrade trade = SpecificRepoTrade.builder().security(bond).book(book).counterparty(counterparty)
				.currency(TradistaTestUtil.EUR).tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3)).endDate(LocalDate.of(2025, 6, 10))
				.repoRate(BigDecimal.valueOf(2.5)).marginRate(BigDecimal.valueOf(102.0))
				.amount(BigDecimal.valueOf(1_000_000)).status(status).build();
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

		SpecificRepoTrade trade = SpecificRepoTrade.builder().security(bond).book(book).counterparty(counterparty)
				.creationTime(historicalCreation).lastUpdateTime(historicalUpdate).build();

		assertEquals(historicalCreation, trade.getCreationTime());
		assertEquals(historicalUpdate, trade.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		SpecificRepoTrade trade = SpecificRepoTrade.builder().security(bond).book(book).counterparty(counterparty)
				.creationTime(originalCreation).build();

		assertEquals(originalCreation, trade.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		trade.setCreationDate(LocalDate.of(2026, 12, 31));

		assertEquals(originalCreation, trade.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		SpecificRepoTrade trade = SpecificRepoTrade.builder().security(bond).book(book).counterparty(counterparty)
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

		SpecificRepoTrade original = SpecificRepoTrade.builder().security(bond).book(book).counterparty(counterparty)
				.currency(TradistaTestUtil.EUR).tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3)).endDate(LocalDate.of(2025, 6, 10))
				.repoRate(BigDecimal.valueOf(2.5)).marginRate(BigDecimal.valueOf(102.0))
				.amount(BigDecimal.valueOf(1_000_000)).status(status).creationTime(creationTime)
				.lastUpdateTime(lastUpdateTime).build();

		SpecificRepoTrade copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getSecurity(), copy.getSecurity());
		assertEquals(original.getRepoRate(), copy.getRepoRate());
		assertEquals(original.getMarginRate(), copy.getMarginRate());
	}

}