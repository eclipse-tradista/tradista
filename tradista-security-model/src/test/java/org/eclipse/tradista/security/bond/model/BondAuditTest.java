package org.eclipse.tradista.security.bond.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
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

public class BondAuditTest {

	private static Exchange exchange;
	private static Currency currency;

	@BeforeAll
	public static void setUp() {
		exchange = new Exchange("EPA");
		currency = new Currency("EUR");
	}

	@Test
	public void testDefaultCreationTimeOnNewBond() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		Bond bond = new Bond.Builder(exchange, "FR0000120271")
				.currency(currency)
				.principal(BigDecimal.valueOf(1000))
				.build();
		Instant after = Instant.now().plus(1, ChronoUnit.SECONDS);

		assertNotNull(bond.getCreationTime());
		assertTrue(bond.getCreationTime().isAfter(before));
		assertTrue(bond.getCreationTime().isBefore(after));
		assertNotNull(bond.getLastUpdateTime());
		assertTrue(bond.getLastUpdateTime().isAfter(before));
		assertTrue(bond.getLastUpdateTime().isBefore(after));
	}

	@Test
	public void testExplicitCreationTimeViaBuilder() {
		Instant historicalCreation = Instant.parse("2024-01-15T10:30:00Z");
		Instant historicalUpdate = Instant.parse("2024-01-16T14:20:00Z");

		Bond bond = new Bond.Builder(exchange, "FR0000120271")
				.creationTime(historicalCreation)
				.lastUpdateTime(historicalUpdate)
				.build();

		assertEquals(historicalCreation, bond.getCreationTime());
		assertEquals(historicalUpdate, bond.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		Bond bond = new Bond.Builder(exchange, "FR0000120271")
				.creationTime(originalCreation)
				.build();

		assertEquals(originalCreation, bond.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		bond.setCreationDate(LocalDate.of(2026, 12, 31));

		assertEquals(originalCreation, bond.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		Bond bond = new Bond.Builder(exchange, "FR0000120271").build();

		assertNotNull(bond.getLastUpdateTime());

		Instant updatedTime = Instant.now().plus(10, ChronoUnit.MINUTES);
		bond.setLastUpdateTime(updatedTime);

		assertEquals(updatedTime, bond.getLastUpdateTime());
	}

	@Test
	public void testToBuilderPreservesAuditFields() {
		Instant creationTime = Instant.parse("2024-02-01T12:00:00Z");
		Instant lastUpdateTime = Instant.parse("2024-02-02T15:00:00Z");

		Bond original = new Bond.Builder(exchange, "FR0000120271")
				.currency(currency)
				.principal(BigDecimal.valueOf(5000))
				.creationTime(creationTime)
				.lastUpdateTime(lastUpdateTime)
				.build();

		Bond copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getIsin(), copy.getIsin());
		assertEquals(original.getPrincipal(), copy.getPrincipal());
	}

}
