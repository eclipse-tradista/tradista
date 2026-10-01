package org.eclipse.tradista.ir.future.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

public class FutureAuditTest {

	private static FutureContractSpecification contractSpecification;

	@BeforeAll
	public static void setUp() {
		contractSpecification = new FutureContractSpecification("EURIBOR_3M");
		contractSpecification.setExchange(new Exchange("EPA"));
	}

	@Test
	public void testDefaultCreationTimeOnNewFuture() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		Future future = new Future.Builder("MAR26", contractSpecification)
				.maturityDate(LocalDate.of(2026, 3, 20))
				.build();
		Instant after = Instant.now().plus(1, ChronoUnit.SECONDS);

		assertNotNull(future.getCreationTime());
		assertTrue(future.getCreationTime().isAfter(before));
		assertTrue(future.getCreationTime().isBefore(after));
		assertNotNull(future.getLastUpdateTime());
		assertTrue(future.getLastUpdateTime().isAfter(before));
		assertTrue(future.getLastUpdateTime().isBefore(after));
	}

	@Test
	public void testExplicitCreationTimeViaBuilder() {
		Instant historicalCreation = Instant.parse("2024-01-15T10:30:00Z");
		Instant historicalUpdate = Instant.parse("2024-01-16T14:20:00Z");

		Future future = new Future.Builder("MAR26", contractSpecification)
				.creationTime(historicalCreation)
				.lastUpdateTime(historicalUpdate)
				.build();

		assertEquals(historicalCreation, future.getCreationTime());
		assertEquals(historicalUpdate, future.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		Future future = new Future.Builder("MAR26", contractSpecification)
				.creationTime(originalCreation)
				.build();

		assertEquals(originalCreation, future.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		future.setCreationDate(LocalDate.of(2026, 12, 31));

		assertEquals(originalCreation, future.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		Future future = new Future.Builder("MAR26", contractSpecification).build();

		assertNotNull(future.getLastUpdateTime());

		Instant updatedTime = Instant.now().plus(10, ChronoUnit.MINUTES);
		future.setLastUpdateTime(updatedTime);

		assertEquals(updatedTime, future.getLastUpdateTime());
	}

	@Test
	public void testToBuilderPreservesAuditFields() {
		Instant creationTime = Instant.parse("2024-02-01T12:00:00Z");
		Instant lastUpdateTime = Instant.parse("2024-02-02T15:00:00Z");

		Future original = new Future.Builder("MAR26", contractSpecification)
				.maturityDate(LocalDate.of(2026, 3, 20))
				.creationTime(creationTime)
				.lastUpdateTime(lastUpdateTime)
				.build();

		Future copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getSymbol(), copy.getSymbol());
		assertEquals(original.getMaturityDate(), copy.getMaturityDate());
	}

}
