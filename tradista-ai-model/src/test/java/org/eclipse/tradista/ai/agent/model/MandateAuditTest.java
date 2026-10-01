package org.eclipse.tradista.ai.agent.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

public class MandateAuditTest {

	@Test
	public void testDefaultCreationTimeOnNewMandate() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		Mandate mandate = new Mandate.Builder("TestMandate").build();
		Instant after = Instant.now().plus(1, ChronoUnit.SECONDS);

		assertNotNull(mandate.getCreationTime());
		assertTrue(mandate.getCreationTime().isAfter(before));
		assertTrue(mandate.getCreationTime().isBefore(after));
		assertNotNull(mandate.getLastUpdateTime());
		assertTrue(mandate.getLastUpdateTime().isAfter(before));
		assertTrue(mandate.getLastUpdateTime().isBefore(after));
	}

	@Test
	public void testExplicitCreationTimeViaBuilder() {
		Instant historicalCreation = Instant.parse("2024-01-15T10:30:00Z");
		Instant historicalUpdate = Instant.parse("2024-01-16T14:20:00Z");

		Mandate mandate = new Mandate.Builder("TestMandate")
				.creationTime(historicalCreation)
				.lastUpdateTime(historicalUpdate)
				.build();

		assertEquals(historicalCreation, mandate.getCreationTime());
		assertEquals(historicalUpdate, mandate.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateTimeIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		Mandate mandate = new Mandate.Builder("TestMandate")
				.creationTime(originalCreation)
				.build();

		assertEquals(originalCreation, mandate.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		mandate.setCreationDateTime(LocalDateTime.of(2026, 12, 31, 23, 59));

		assertEquals(originalCreation, mandate.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		Mandate mandate = new Mandate.Builder("TestMandate").build();

		assertNotNull(mandate.getLastUpdateTime());

		Instant updatedTime = Instant.now().plus(10, ChronoUnit.MINUTES);
		mandate.setLastUpdateTime(updatedTime);

		assertEquals(updatedTime, mandate.getLastUpdateTime());
	}

	@Test
	public void testToBuilderPreservesAuditFields() {
		Instant creationTime = Instant.parse("2024-02-01T12:00:00Z");
		Instant lastUpdateTime = Instant.parse("2024-02-02T15:00:00Z");

		Mandate original = new Mandate.Builder("TestMandate")
				.acceptedRiskLevel(Mandate.RiskLevel.AVERAGE)
				.creationTime(creationTime)
				.lastUpdateTime(lastUpdateTime)
				.build();

		Mandate copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getName(), copy.getName());
		assertEquals(original.getAcceptedRiskLevel(), copy.getAcceptedRiskLevel());
	}

}
