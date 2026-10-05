package org.eclipse.tradista.ir.ircapfloorcollar.model;

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
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.fra.model.FRATrade;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
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

public class IRCapFloorCollarTradeAuditTest {

	private static final Currency currency = TradistaTestUtil.EUR;
	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final FRATrade forwardTrade = FRATrade.builder().fixedRate(BigDecimal.valueOf(2.5))
			.startDate(LocalDate.of(2025, 6, 1)).maturityDate(LocalDate.of(2025, 9, 1))
			.referenceRateIndex(TradistaTestUtil.EURIBOR).referenceRateIndexTenor(Tenor.THREE_MONTHS)
			.dayCountConvention(new DayCountConvention(DayCountConvention.ACT_360)).currency(currency)
			.amount(BigDecimal.valueOf(1_000_000)).book(book).counterparty(counterparty)
			.tradeDate(LocalDate.of(2025, 5, 28)).settlementDate(LocalDate.of(2025, 6, 1)).build();

	@Test
	public void testDefaultCreationTimeOnNewIRCapFloorCollarTrade() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		IRCapFloorCollarTrade trade = IRCapFloorCollarTrade.builder().capStrike(BigDecimal.valueOf(3.0))
				.irForwardTrade(forwardTrade).currency(currency).amount(BigDecimal.valueOf(10_000)).book(book)
				.counterparty(counterparty).tradeDate(LocalDate.of(2025, 5, 28))
				.settlementDate(LocalDate.of(2025, 6, 1)).build();
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

		IRCapFloorCollarTrade trade = IRCapFloorCollarTrade.builder().currency(currency).book(book)
				.counterparty(counterparty).creationTime(historicalCreation).lastUpdateTime(historicalUpdate).build();

		assertEquals(historicalCreation, trade.getCreationTime());
		assertEquals(historicalUpdate, trade.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		IRCapFloorCollarTrade trade = IRCapFloorCollarTrade.builder().currency(currency).book(book)
				.counterparty(counterparty).creationTime(originalCreation).build();

		assertEquals(originalCreation, trade.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		trade.setCreationDate(LocalDate.of(2026, 12, 31));

		assertEquals(originalCreation, trade.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		IRCapFloorCollarTrade trade = IRCapFloorCollarTrade.builder().currency(currency).book(book)
				.counterparty(counterparty).build();

		assertNotNull(trade.getLastUpdateTime());

		Instant updatedTime = Instant.now().plus(10, ChronoUnit.MINUTES);
		trade.setLastUpdateTime(updatedTime);

		assertEquals(updatedTime, trade.getLastUpdateTime());
	}

	@Test
	public void testToBuilderPreservesAuditFields() {
		Instant creationTime = Instant.parse("2024-02-01T12:00:00Z");
		Instant lastUpdateTime = Instant.parse("2024-02-02T15:00:00Z");

		IRCapFloorCollarTrade original = IRCapFloorCollarTrade.builder().capStrike(BigDecimal.valueOf(3.0))
				.irForwardTrade(forwardTrade).currency(currency).amount(BigDecimal.valueOf(10_000)).book(book)
				.counterparty(counterparty).tradeDate(LocalDate.of(2025, 5, 28))
				.settlementDate(LocalDate.of(2025, 6, 1)).creationTime(creationTime).lastUpdateTime(lastUpdateTime)
				.build();

		IRCapFloorCollarTrade copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getCapStrike(), copy.getCapStrike());
		assertEquals(original.getAmount(), copy.getAmount());
	}

}