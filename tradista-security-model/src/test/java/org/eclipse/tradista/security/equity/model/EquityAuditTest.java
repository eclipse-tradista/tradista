package org.eclipse.tradista.security.equity.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.exchange.model.Exchange;
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

public class EquityAuditTest {

	private static final Exchange exchange = TradistaTestUtil.createExchange("EPA");

	@Test
	public void testDefaultCreationTimeOnNewEquity() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		Equity equity = Equity.builder(exchange, "FR0000120271").currency(TradistaTestUtil.EUR).totalIssued(1_000_000L)
				.build();
		Instant after = Instant.now().plus(1, ChronoUnit.SECONDS);

		assertNotNull(equity.getCreationTime());
		assertTrue(equity.getCreationTime().isAfter(before));
		assertTrue(equity.getCreationTime().isBefore(after));
		assertNotNull(equity.getLastUpdateTime());
		assertTrue(equity.getLastUpdateTime().isAfter(before));
		assertTrue(equity.getLastUpdateTime().isBefore(after));
	}

	@Test
	public void testExplicitCreationTimeViaBuilder() {
		Instant historicalCreation = Instant.parse("2024-01-15T10:30:00Z");
		Instant historicalUpdate = Instant.parse("2024-01-16T14:20:00Z");

		Equity equity = Equity.builder(exchange, "FR0000120271").creationTime(historicalCreation)
				.lastUpdateTime(historicalUpdate).build();

		assertEquals(historicalCreation, equity.getCreationTime());
		assertEquals(historicalUpdate, equity.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		Equity equity = Equity.builder(exchange, "FR0000120271").creationTime(originalCreation).build();

		assertEquals(originalCreation, equity.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		equity.setCreationDate(LocalDate.of(2026, 12, 31));

		assertEquals(originalCreation, equity.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		Equity equity = Equity.of(exchange, "FR0000120271");

		assertNotNull(equity.getLastUpdateTime());

		Instant updatedTime = Instant.now().plus(10, ChronoUnit.MINUTES);
		equity.setLastUpdateTime(updatedTime);

		assertEquals(updatedTime, equity.getLastUpdateTime());
	}

	@Test
	public void testToBuilderPreservesAuditFields() {
		Instant creationTime = Instant.parse("2024-02-01T12:00:00Z");
		Instant lastUpdateTime = Instant.parse("2024-02-02T15:00:00Z");

		Equity original = Equity.builder(exchange, "FR0000120271").currency(TradistaTestUtil.EUR).totalIssued(500_000L)
				.tradingSize(100L).creationTime(creationTime).lastUpdateTime(lastUpdateTime).build();

		Equity copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getIsin(), copy.getIsin());
		assertEquals(original.getTotalIssued(), copy.getTotalIssued());
		assertEquals(original.getTradingSize(), copy.getTradingSize());
	}

}