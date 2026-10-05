package org.eclipse.tradista.core.common.test;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

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

public final class TradistaTestUtil {

	public static final Currency EUR = new Currency("EUR");
	public static final Currency USD = new Currency("USD");
	public static final Currency BRL = new Currency("BRL");
	public static final Index EURIBOR = new Index("EURIBOR");

	private TradistaTestUtil() {
	}

	public static LegalEntity createProcessingOrg() {
		LegalEntity po = new LegalEntity("PO");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		return po;
	}

	public static LegalEntity createProcessingOrg(String name) {
		LegalEntity po = new LegalEntity(name);
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		return po;
	}

	public static LegalEntity createCounterparty() {
		LegalEntity cp = new LegalEntity("CP");
		cp.setRole(LegalEntity.Role.COUNTERPARTY);
		return cp;
	}

	public static LegalEntity createCounterparty(String name) {
		LegalEntity cp = new LegalEntity(name);
		cp.setRole(LegalEntity.Role.COUNTERPARTY);
		return cp;
	}

	public static Book createTradingBook() {
		return new Book("TradingBook", createProcessingOrg());
	}

	public static Book createBook(String name) {
		return new Book(name, createProcessingOrg());
	}

	public static Book createBook(String name, LegalEntity po) {
		return new Book(name, po);
	}

	public static Exchange createExchange(String code) {
		return new Exchange(code);
	}

	public static Index createIndex(String name) {
		return new Index(name);
	}

}
