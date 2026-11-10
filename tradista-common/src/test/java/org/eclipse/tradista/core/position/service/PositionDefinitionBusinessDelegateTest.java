package org.eclipse.tradista.core.position.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
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

class PositionDefinitionBusinessDelegateTest {

	private PositionDefinitionBusinessDelegate businessDelegate = new PositionDefinitionBusinessDelegate();

	@Test
	void testSavePositionDefinitionNameExceeds50Characters() {
		LegalEntity po = new LegalEntity("PO");
		Book book = new Book("Book", po);
		Currency currency = new Currency("EUR");
		PricingParameter pricingParameter = new PricingParameter("PP", po);

		String name51Chars = "A".repeat(51);
		PositionDefinition posDef = new PositionDefinition(name51Chars, po);
		posDef.setBook(book);
		posDef.setCurrency(currency);
		posDef.setPricingParameter(pricingParameter);

		TradistaBusinessException exception = assertThrows(TradistaBusinessException.class,
				() -> businessDelegate.savePositionDefinition(posDef));

		assertEquals(String.format("The name cannot exceed 50 characters.%n"), exception.getMessage());
	}

	@Test
	void testSavePositionDefinitionBlankOrEmptyName() {
		LegalEntity po = new LegalEntity("PO");
		Book book = new Book("Book", po);
		Currency currency = new Currency("EUR");
		PricingParameter pricingParameter = new PricingParameter("PP", po);

		PositionDefinition posDef = new PositionDefinition("", po);
		posDef.setBook(book);
		posDef.setCurrency(currency);
		posDef.setPricingParameter(pricingParameter);

		TradistaBusinessException exception = assertThrows(TradistaBusinessException.class,
				() -> businessDelegate.savePositionDefinition(posDef));

		assertEquals(String.format("The name is mandatory.%n"), exception.getMessage());
	}
}
