package org.eclipse.tradista.fix.exporter.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class TestTradeCaptureReportExporter {

	private static final Logger logger = LoggerFactory.getLogger(TestTradeCaptureReportExporter.class);

	public static void main(String[] args) throws Exception {

		SpecificRepoTrade trade = new SpecificRepoTrade();
		LegalEntity po = new LegalEntity("TestPo");
		po.setId(1);
		trade.setBook(new Book("TestBook", po));
		Exchange exchange = new Exchange("TestExchange");
		Bond bond = new Bond(exchange, "TestBond");
		trade.setProduct(bond);
		trade.setAmount(new BigDecimal("1000"));
		trade.setBuySell(true);
		trade.setCurrency(new org.eclipse.tradista.core.currency.model.Currency("EUR"));
		trade.setCounterparty(new LegalEntity("TestCpty"));
		trade.setCreationDate(LocalDate.now());
		trade.setTradeDate(LocalDate.now());
		trade.setSettlementDate(LocalDate.now().plusDays(1));
		trade.setRepoRate(new BigDecimal("0.5"));
		trade.setEndDate(LocalDate.now().plusDays(100));
		trade.setMarginRate(new BigDecimal("1.1"));

		// Here, generate an event to export the trade.

		logger.info("Event published");
	}
}