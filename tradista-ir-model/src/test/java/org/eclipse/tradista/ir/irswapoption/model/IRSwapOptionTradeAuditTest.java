package org.eclipse.tradista.ir.irswapoption.model;

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
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;
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

public class IRSwapOptionTradeAuditTest {

	private static Book book;
	private static LegalEntity counterparty;
	private static Currency currency;
	private static SingleCurrencyIRSwapTrade underlying;

	@BeforeAll
	public static void setUp() {
		currency = new Currency("EUR");
		LegalEntity po = new LegalEntity("PO");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		counterparty = new LegalEntity("CP");
		counterparty.setRole(LegalEntity.Role.COUNTERPARTY);
		book = new Book("TradingBook", po);
		Index index = new Index("EURIBOR");

		underlying = new SingleCurrencyIRSwapTrade.Builder()
				.currency(currency)
				.amount(BigDecimal.valueOf(1_000_000))
				.book(book)
				.counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2030, 6, 3))
				.paymentFrequency(Tenor.SIX_MONTHS)
				.receptionFrequency(Tenor.THREE_MONTHS)
				.paymentInterestPayment(InterestPayment.END_OF_PERIOD)
				.receptionInterestPayment(InterestPayment.END_OF_PERIOD)
				.paymentInterestFixing(InterestPayment.BEGINNING_OF_PERIOD)
				.receptionInterestFixing(InterestPayment.BEGINNING_OF_PERIOD)
				.receptionReferenceRateIndex(index)
				.receptionReferenceRateIndexTenor(Tenor.THREE_MONTHS)
				.interestsToPayFixed(true)
				.paymentFixedInterestRate(BigDecimal.valueOf(2.5))
				.paymentDayCountConvention(new DayCountConvention(DayCountConvention.ACT_360))
				.receptionDayCountConvention(new DayCountConvention(DayCountConvention.ACT_360))
				.build();
	}

	@Test
	public void testDefaultCreationTimeOnNewIRSwapOptionTrade() {
		Instant before = Instant.now().minus(1, ChronoUnit.SECONDS);
		IRSwapOptionTrade trade = new IRSwapOptionTrade.Builder()
				.underlying(underlying)
				.book(book)
				.counterparty(counterparty)
				.currency(currency)
				.amount(BigDecimal.valueOf(5000))
				.strike(BigDecimal.valueOf(2.5))
				.style(VanillaOptionTrade.Style.EUROPEAN)
				.type(OptionTrade.Type.CALL)
				.settlementType(OptionTrade.SettlementType.CASH)
				.settlementDateOffset(2)
				.tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2025, 12, 1))
				.exerciseDate(LocalDate.of(2025, 12, 1))
				.build();
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

		IRSwapOptionTrade trade = new IRSwapOptionTrade.Builder()
				.currency(currency)
				.book(book)
				.counterparty(counterparty)
				.creationTime(historicalCreation)
				.lastUpdateTime(historicalUpdate)
				.build();

		assertEquals(historicalCreation, trade.getCreationTime());
		assertEquals(historicalUpdate, trade.getLastUpdateTime());
	}

	@Test
	public void testDeprecatedSetCreationDateIsNoOp() {
		Instant originalCreation = Instant.parse("2024-05-10T08:00:00Z");
		IRSwapOptionTrade trade = new IRSwapOptionTrade.Builder()
				.currency(currency)
				.book(book)
				.counterparty(counterparty)
				.creationTime(originalCreation)
				.build();

		assertEquals(originalCreation, trade.getCreationTime());

		// Deprecated method call must be no-op to guarantee immutability
		trade.setCreationDate(LocalDate.of(2026, 12, 31));

		assertEquals(originalCreation, trade.getCreationTime());
	}

	@Test
	public void testLastUpdateTimeModification() {
		IRSwapOptionTrade trade = new IRSwapOptionTrade.Builder()
				.currency(currency)
				.book(book)
				.counterparty(counterparty)
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

		IRSwapOptionTrade original = new IRSwapOptionTrade.Builder()
				.underlying(underlying)
				.book(book)
				.counterparty(counterparty)
				.currency(currency)
				.amount(BigDecimal.valueOf(5000))
				.strike(BigDecimal.valueOf(2.5))
				.style(VanillaOptionTrade.Style.EUROPEAN)
				.type(OptionTrade.Type.CALL)
				.settlementType(OptionTrade.SettlementType.CASH)
				.settlementDateOffset(2)
				.tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2025, 12, 1))
				.exerciseDate(LocalDate.of(2025, 12, 1))
				.creationTime(creationTime)
				.lastUpdateTime(lastUpdateTime)
				.build();

		IRSwapOptionTrade copy = original.toBuilder().build();

		assertEquals(original.getCreationTime(), copy.getCreationTime());
		assertEquals(original.getLastUpdateTime(), copy.getLastUpdateTime());
		assertEquals(original.getStrike(), copy.getStrike());
		assertEquals(original.getAmount(), copy.getAmount());
		assertEquals(original.getStyle(), copy.getStyle());
	}

}
