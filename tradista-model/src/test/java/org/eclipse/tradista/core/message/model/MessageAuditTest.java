package org.eclipse.tradista.core.message.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
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

public class MessageAuditTest {

	@Test
	public void testDefaultCreationTimeOnNewMessage() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		IncomingMessage message = new IncomingMessage.Builder().type("CONFIRMATION").build();
		Instant after = Instant.now().plus(1, ChronoUnit.SECONDS);

		assertNotNull(message.getCreationTime());
		assertTrue(message.getCreationTime().isAfter(before));
		assertTrue(message.getCreationTime().isBefore(after));
		assertNotNull(message.getLastUpdateTime());
		assertTrue(message.getLastUpdateTime().isAfter(before));
		assertTrue(message.getLastUpdateTime().isBefore(after));
	}

	@Test
	public void testExplicitCreationTimeViaBuilder() {
		Instant historicalCreation = Instant.parse("2024-01-15T10:30:00Z");
		Instant historicalUpdate = Instant.parse("2024-01-16T14:20:00Z");

		IncomingMessage message = new IncomingMessage.Builder().type("CONFIRMATION").creationTime(historicalCreation)
				.lastUpdateTime(historicalUpdate).build();

		assertEquals(historicalCreation, message.getCreationTime());
		assertEquals(historicalUpdate, message.getLastUpdateTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		IncomingMessage message = new IncomingMessage.Builder().type("CONFIRMATION").build();

		assertNotNull(message.getLastUpdateTime());

		Instant updatedTime = Instant.now().plus(10, ChronoUnit.MINUTES);
		message.setLastUpdateTime(updatedTime);

		assertEquals(updatedTime, message.getLastUpdateTime());
	}

	@Test
	public void testToBuilderPreservesAuditFields() {
		Instant creationTime = Instant.parse("2024-02-01T12:00:00Z");
		Instant lastUpdateTime = Instant.parse("2024-02-02T15:00:00Z");

		IncomingMessage original = new IncomingMessage.Builder().type("CONFIRMATION").content("Sample content")
				.creationTime(creationTime).lastUpdateTime(lastUpdateTime).build();

		IncomingMessage copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getType(), copy.getType());
		assertEquals(original.getContent(), copy.getContent());
	}

}
