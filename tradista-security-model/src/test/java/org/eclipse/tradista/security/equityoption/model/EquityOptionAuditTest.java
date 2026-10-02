package org.eclipse.tradista.security.equityoption.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.security.equity.model.Equity;
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

public class EquityOptionAuditTest {

	private static EquityOptionContractSpecification contractSpecification;
	private static Equity underlying;

	@BeforeAll
	public static void setUp() {
		Exchange exchange = new Exchange("EPA");
		contractSpecification = new EquityOptionContractSpecification("SAN_OPTION");
		underlying = new Equity.Builder(exchange, "FR0000120271").build();
	}

	@Test
	public void testDefaultCreationTimeOnNewEquityOption() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		EquityOption option = new EquityOption.Builder("SAN_C_100", OptionTrade.Type.CALL, BigDecimal.valueOf(100),
				LocalDate.of(2026, 6, 19), contractSpecification).underlying(underlying).build();
		Instant after = Instant.now().plus(1, ChronoUnit.SECONDS);

		assertNotNull(option.getCreationTime());
		assertTrue(option.getCreationTime().isAfter(before));
		assertTrue(option.getCreationTime().isBefore(after));
		assertNotNull(option.getLastUpdateTime());
		assertTrue(option.getLastUpdateTime().isAfter(before));
		assertTrue(option.getLastUpdateTime().isBefore(after));
	}

	@Test
	public void testExplicitCreationTimeViaBuilder() {
		Instant historicalCreation = Instant.parse("2024-01-15T10:30:00Z");
		Instant historicalUpdate = Instant.parse("2024-01-16T14:20:00Z");

		EquityOption option = new EquityOption.Builder("SAN_C_100", OptionTrade.Type.CALL, BigDecimal.valueOf(100),
				LocalDate.of(2026, 6, 19), contractSpecification).creationTime(historicalCreation)
				.lastUpdateTime(historicalUpdate).build();

		assertEquals(historicalCreation, option.getCreationTime());
		assertEquals(historicalUpdate, option.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		EquityOption option = new EquityOption.Builder("SAN_C_100", OptionTrade.Type.CALL, BigDecimal.valueOf(100),
				LocalDate.of(2026, 6, 19), contractSpecification).creationTime(originalCreation).build();

		assertEquals(originalCreation, option.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		option.setCreationDate(LocalDate.of(2026, 12, 31));

		assertEquals(originalCreation, option.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		EquityOption option = new EquityOption.Builder("SAN_C_100", OptionTrade.Type.CALL, BigDecimal.valueOf(100),
				LocalDate.of(2026, 6, 19), contractSpecification).build();

		assertNotNull(option.getLastUpdateTime());

		Instant updatedTime = Instant.now().plus(10, ChronoUnit.MINUTES);
		option.setLastUpdateTime(updatedTime);

		assertEquals(updatedTime, option.getLastUpdateTime());
	}

	@Test
	public void testToBuilderPreservesAuditFields() {
		Instant creationTime = Instant.parse("2024-02-01T12:00:00Z");
		Instant lastUpdateTime = Instant.parse("2024-02-02T15:00:00Z");

		EquityOption original = new EquityOption.Builder("SAN_C_100", OptionTrade.Type.CALL, BigDecimal.valueOf(100),
				LocalDate.of(2026, 6, 19), contractSpecification).underlying(underlying).creationTime(creationTime)
				.lastUpdateTime(lastUpdateTime).build();

		EquityOption copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getCode(), copy.getCode());
		assertEquals(original.getStrike(), copy.getStrike());
		assertEquals(original.getType(), copy.getType());
	}

}
