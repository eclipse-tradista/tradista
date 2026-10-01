package org.eclipse.tradista.security.gcrepo.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.workflow.model.Status;
import org.eclipse.tradista.security.gcrepo.model.GCBasket;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
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

public class GCRepoTradeValidatorTest {

	private static GCRepoTradeValidator validator;
	private static Book book;
	private static LegalEntity counterparty;
	private static Currency currency;
	private static GCBasket basket;
	private static Status status;

	@BeforeAll
	public static void setUp() {
		validator = new GCRepoTradeValidator();

		currency = new Currency("EUR");
		LegalEntity po = new LegalEntity("PO");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		counterparty = new LegalEntity("CP");
		counterparty.setRole(LegalEntity.Role.COUNTERPARTY);
		book = new Book("TradingBook", po);
		basket = new GCBasket();
		basket.setName("EUR_GOVT");
		status = new Status();
		status.setName("NEW");
	}

	private GCRepoTrade.Builder createValidTradeBuilder() {
		return new GCRepoTrade.Builder()
				.gcBasket(basket)
				.book(book)
				.counterparty(counterparty)
				.currency(currency)
				.tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3))
				.endDate(LocalDate.of(2025, 6, 10))
				.repoRate(BigDecimal.valueOf(2.5))
				.marginRate(BigDecimal.valueOf(102.0))
				.amount(BigDecimal.valueOf(1_000_000))
				.status(status);
	}

	@Test
	public void testValidTrade() {
		GCRepoTrade trade = createValidTradeBuilder().build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTrade() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(null));
	}

	@Test
	public void testMissingGCBasket() {
		GCRepoTrade trade = createValidTradeBuilder().gcBasket(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingBook() {
		GCRepoTrade trade = createValidTradeBuilder().book(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingCounterparty() {
		GCRepoTrade trade = createValidTradeBuilder().counterparty(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingStartDate() {
		GCRepoTrade trade = createValidTradeBuilder().settlementDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testEndDateBeforeStartDate() {
		GCRepoTrade trade = createValidTradeBuilder()
				.settlementDate(LocalDate.of(2025, 6, 10))
				.endDate(LocalDate.of(2025, 6, 3))
				.build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingMarginRate() {
		GCRepoTrade trade = createValidTradeBuilder().marginRate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroMarginRate() {
		GCRepoTrade trade = createValidTradeBuilder().marginRate(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingRepoRateWhenFixedRate() {
		GCRepoTrade trade = createValidTradeBuilder().repoRate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroRepoRateWhenFixedRate() {
		GCRepoTrade trade = createValidTradeBuilder().repoRate(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingStatus() {
		GCRepoTrade trade = createValidTradeBuilder().status(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingEndDateWhenNotTerminableOnDemand() {
		GCRepoTrade trade = createValidTradeBuilder().endDate(null).terminableOnDemand(false).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNegativeNoticePeriodWhenTerminableOnDemand() {
		GCRepoTrade trade = createValidTradeBuilder().terminableOnDemand(true).noticePeriod((short) -1).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

}
