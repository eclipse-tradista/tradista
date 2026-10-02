package org.eclipse.tradista.core.transfer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
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

public class TransferAuditTest {

	private static Book book;
	private static Currency currency;

	@BeforeAll
	public static void setUp() {
		currency = new Currency("EUR");
		LegalEntity po = new LegalEntity("PO");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		book = new Book("TradingBook", po);
	}

	@Test
	public void testDefaultCreationTimeOnNewTransfer() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		CashTransfer transfer = new CashTransfer.Builder(book, TransferPurpose.CASH_SETTLEMENT,
				LocalDate.of(2025, 6, 1), currency).quantityOrAmount(BigDecimal.valueOf(1000)).build();
		Instant after = Instant.now().plus(1, ChronoUnit.SECONDS);

		assertNotNull(transfer.getCreationTime());
		assertTrue(transfer.getCreationTime().isAfter(before));
		assertTrue(transfer.getCreationTime().isBefore(after));
		assertNotNull(transfer.getLastUpdateTime());
		assertTrue(transfer.getLastUpdateTime().isAfter(before));
		assertTrue(transfer.getLastUpdateTime().isBefore(after));
	}

	@Test
	public void testExplicitCreationTimeViaBuilder() {
		Instant historicalCreation = Instant.parse("2024-01-15T10:30:00Z");
		Instant historicalUpdate = Instant.parse("2024-01-16T14:20:00Z");

		CashTransfer transfer = new CashTransfer.Builder(book, TransferPurpose.CASH_SETTLEMENT,
				LocalDate.of(2025, 6, 1), currency).creationTime(historicalCreation).lastUpdateTime(historicalUpdate)
				.build();

		assertEquals(historicalCreation, transfer.getCreationTime());
		assertEquals(historicalUpdate, transfer.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateTimeIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		CashTransfer transfer = new CashTransfer.Builder(book, TransferPurpose.CASH_SETTLEMENT,
				LocalDate.of(2025, 6, 1), currency).creationTime(originalCreation).build();

		assertEquals(originalCreation, transfer.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		transfer.setCreationDateTime(LocalDateTime.of(2026, 12, 31, 23, 59));

		assertEquals(originalCreation, transfer.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		CashTransfer transfer = new CashTransfer.Builder(book, TransferPurpose.CASH_SETTLEMENT,
				LocalDate.of(2025, 6, 1), currency).build();

		assertNotNull(transfer.getLastUpdateTime());

		Instant updatedTime = Instant.now().plus(10, ChronoUnit.MINUTES);
		transfer.setLastUpdateTime(updatedTime);

		assertEquals(updatedTime, transfer.getLastUpdateTime());
	}

	@Test
	public void testToBuilderPreservesAuditFields() {
		Instant creationTime = Instant.parse("2024-02-01T12:00:00Z");
		Instant lastUpdateTime = Instant.parse("2024-02-02T15:00:00Z");

		CashTransfer original = new CashTransfer.Builder(book, TransferPurpose.CASH_SETTLEMENT,
				LocalDate.of(2025, 6, 1), currency).quantityOrAmount(BigDecimal.valueOf(5000))
				.creationTime(creationTime).lastUpdateTime(lastUpdateTime).build();

		CashTransfer copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getAmount(), copy.getAmount());
		assertEquals(original.getCurrency(), copy.getCurrency());
	}

}