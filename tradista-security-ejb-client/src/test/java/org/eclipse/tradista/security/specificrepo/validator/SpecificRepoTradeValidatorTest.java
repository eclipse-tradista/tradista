package org.eclipse.tradista.security.specificrepo.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.workflow.model.Status;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;
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

public class SpecificRepoTradeValidatorTest {

	private static final SpecificRepoTradeValidator validator = new SpecificRepoTradeValidator();
	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Currency currency = TradistaTestUtil.EUR;
	private static final Bond bond = Bond.builder(TradistaTestUtil.createExchange("EPA"), "FR0000120271")
			.currency(currency).build();
	private static final Status status;

	static {
		status = new Status();
		status.setName("NEW");
	}

	private SpecificRepoTrade.Builder createValidTradeBuilder() {
		return SpecificRepoTrade.builder().security(bond).book(book).counterparty(counterparty).currency(currency)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.endDate(LocalDate.of(2025, 6, 10)).repoRate(BigDecimal.valueOf(2.5))
				.marginRate(BigDecimal.valueOf(102.0)).amount(BigDecimal.valueOf(1_000_000)).status(status);
	}

	@Test
	public void testValidTrade() {
		SpecificRepoTrade trade = createValidTradeBuilder().build();
		assertDoesNotThrow(() -> validator.validateTrade(trade));
	}

	@Test
	public void testNullTrade() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(null));
	}

	@Test
	public void testMissingSecurity() {
		SpecificRepoTrade trade = createValidTradeBuilder().security(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingBook() {
		SpecificRepoTrade trade = createValidTradeBuilder().book(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingCounterparty() {
		SpecificRepoTrade trade = createValidTradeBuilder().counterparty(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingStartDate() {
		SpecificRepoTrade trade = createValidTradeBuilder().settlementDate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testEndDateBeforeStartDate() {
		SpecificRepoTrade trade = createValidTradeBuilder().settlementDate(LocalDate.of(2025, 6, 10))
				.endDate(LocalDate.of(2025, 6, 3)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingMarginRate() {
		SpecificRepoTrade trade = createValidTradeBuilder().marginRate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroMarginRate() {
		SpecificRepoTrade trade = createValidTradeBuilder().marginRate(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingRepoRateWhenFixedRate() {
		SpecificRepoTrade trade = createValidTradeBuilder().repoRate(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testZeroRepoRateWhenFixedRate() {
		SpecificRepoTrade trade = createValidTradeBuilder().repoRate(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingStatus() {
		SpecificRepoTrade trade = createValidTradeBuilder().status(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testMissingEndDateWhenNotTerminableOnDemand() {
		SpecificRepoTrade trade = createValidTradeBuilder().endDate(null).terminableOnDemand(false).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

	@Test
	public void testNegativeNoticePeriodWhenTerminableOnDemand() {
		SpecificRepoTrade trade = createValidTradeBuilder().terminableOnDemand(true).noticePeriod((short) -1).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateTrade(trade));
	}

}