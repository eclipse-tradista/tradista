package org.eclipse.tradista.fx.fxoption.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.fx.fx.model.FXTrade;
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

public class FXOptionTradeAuditTest {

	private static final Currency eur = TradistaTestUtil.EUR;
	private static final Currency usd = TradistaTestUtil.USD;
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final FXTrade underlyingTrade = FXTrade.builder().currencyOne(eur).currency(usd)
			.amountOne(BigDecimal.valueOf(100_000)).amount(BigDecimal.valueOf(110_000)).book(book)
			.counterparty(counterparty).tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
			.build();

	@Test
	public void testDefaultCreationTimeOnNewFXOptionTrade() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		FXOptionTrade trade = FXOptionTrade.builder().underlying(underlyingTrade).currency(usd)
				.amount(BigDecimal.valueOf(2000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2025, 9, 1)).style(VanillaOptionTrade.Style.EUROPEAN)
				.type(OptionTrade.Type.CALL).settlementType(OptionTrade.SettlementType.PHYSICAL).build();
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

		FXOptionTrade trade = FXOptionTrade.builder().underlying(underlyingTrade).book(book).counterparty(counterparty)
				.creationTime(historicalCreation).lastUpdateTime(historicalUpdate).build();

		assertEquals(historicalCreation, trade.getCreationTime());
		assertEquals(historicalUpdate, trade.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		FXOptionTrade trade = FXOptionTrade.builder().underlying(underlyingTrade).book(book).counterparty(counterparty)
				.creationTime(originalCreation).build();

		assertEquals(originalCreation, trade.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		trade.setCreationDate(LocalDate.of(2026, 12, 31));

		assertEquals(originalCreation, trade.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		FXOptionTrade trade = FXOptionTrade.builder().underlying(underlyingTrade).book(book).counterparty(counterparty)
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

		FXOptionTrade original = FXOptionTrade.builder().underlying(underlyingTrade).currency(usd)
				.amount(BigDecimal.valueOf(2000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2025, 9, 1)).style(VanillaOptionTrade.Style.EUROPEAN)
				.type(OptionTrade.Type.CALL).creationTime(creationTime).lastUpdateTime(lastUpdateTime).build();

		FXOptionTrade copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getUnderlying().getCurrencyOne(), copy.getUnderlying().getCurrencyOne());
		assertEquals(original.getAmount(), copy.getAmount());
		assertEquals(original.getType(), copy.getType());
	}

}