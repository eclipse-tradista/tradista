package org.eclipse.tradista.ir.irswap.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.tenor.model.Tenor;
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

public class SingleCurrencyIRSwapTradeAuditTest {

	private static final Currency currency = TradistaTestUtil.EUR;
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final Index index = TradistaTestUtil.EURIBOR;

	@Test
	public void testDefaultCreationTimeOnNewSingleCurrencyIRSwapTrade() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		SingleCurrencyIRSwapTrade trade = SingleCurrencyIRSwapTrade.builder().currency(currency)
				.amount(BigDecimal.valueOf(1_000_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2030, 6, 3)).paymentFrequency(Tenor.SIX_MONTHS)
				.receptionFrequency(Tenor.THREE_MONTHS).paymentInterestPayment(InterestPayment.END_OF_PERIOD)
				.receptionInterestPayment(InterestPayment.END_OF_PERIOD)
				.paymentInterestFixing(InterestPayment.BEGINNING_OF_PERIOD)
				.receptionInterestFixing(InterestPayment.BEGINNING_OF_PERIOD).receptionReferenceRateIndex(index)
				.receptionReferenceRateIndexTenor(Tenor.THREE_MONTHS).interestsToPayFixed(true)
				.paymentFixedInterestRate(BigDecimal.valueOf(2.5))
				.paymentDayCountConvention(new DayCountConvention(DayCountConvention.ACT_360))
				.receptionDayCountConvention(new DayCountConvention(DayCountConvention.ACT_360)).build();
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

		SingleCurrencyIRSwapTrade trade = SingleCurrencyIRSwapTrade.builder().currency(currency).book(book)
				.counterparty(counterparty).creationTime(historicalCreation).lastUpdateTime(historicalUpdate).build();

		assertEquals(historicalCreation, trade.getCreationTime());
		assertEquals(historicalUpdate, trade.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		SingleCurrencyIRSwapTrade trade = SingleCurrencyIRSwapTrade.builder().currency(currency).book(book)
				.counterparty(counterparty).creationTime(originalCreation).build();

		assertEquals(originalCreation, trade.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		trade.setCreationDate(LocalDate.of(2026, 12, 31));

		assertEquals(originalCreation, trade.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		SingleCurrencyIRSwapTrade trade = SingleCurrencyIRSwapTrade.builder().currency(currency).book(book)
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

		SingleCurrencyIRSwapTrade original = SingleCurrencyIRSwapTrade.builder().currency(currency)
				.amount(BigDecimal.valueOf(1_000_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2030, 6, 3)).creationTime(creationTime).lastUpdateTime(lastUpdateTime)
				.build();

		SingleCurrencyIRSwapTrade copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getAmount(), copy.getAmount());
		assertEquals(original.getMaturityDate(), copy.getMaturityDate());
	}

}