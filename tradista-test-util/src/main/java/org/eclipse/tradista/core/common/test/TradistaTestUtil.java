package org.eclipse.tradista.core.common.test;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;

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

/**
 * Shared test fixtures.
 * <p>
 * Convention:
 * <ul>
 * <li>An object that can be created with a simple constructor call (no setter
 * needed) is exposed as a constant (e.g. {@link #EUR}, {@link #EPA}). These
 * constants are shared by all tests: they must never be modified.</li>
 * <li>An object that needs additional setup (e.g. a role to set) or that
 * depends on such an object is created by a {@code createXxx} method, which
 * returns a new instance on each call.</li>
 * <li>Only the variants actually used by tests are provided (YAGNI).</li>
 * </ul>
 */
public final class TradistaTestUtil {

	public static final Currency EUR = new Currency("EUR");
	public static final Currency USD = new Currency("USD");
	public static final Currency BRL = new Currency("BRL");
	public static final Index EURIBOR = new Index("EURIBOR");
	public static final Exchange EPA = new Exchange("EPA");

	private TradistaTestUtil() {
	}

	public static LegalEntity createProcessingOrg() {
		LegalEntity po = new LegalEntity("PO");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		return po;
	}

	public static LegalEntity createCounterparty() {
		LegalEntity cp = new LegalEntity("CP");
		cp.setRole(LegalEntity.Role.COUNTERPARTY);
		return cp;
	}

	public static Book createBook() {
		return new Book("Book", createProcessingOrg());
	}

	public static Book createBook(LegalEntity po) {
		return new Book("Book", po);
	}

	public static PricingParameter createPricingParameter() {
		return new PricingParameter("PricingParameter", createProcessingOrg());
	}

	public static PricingParameter createPricingParameter(LegalEntity po) {
		return new PricingParameter("PricingParameter", po);
	}

	public static PositionDefinition createPositionDefinition() {
		return new PositionDefinition("PositionDefinition", createProcessingOrg());
	}

	public static PositionDefinition createPositionDefinition(LegalEntity po) {
		return new PositionDefinition("PositionDefinition", po);
	}

	public static PositionDefinition createPositionDefinition(String name, LegalEntity po) {
		return new PositionDefinition(name, po);
	}

}