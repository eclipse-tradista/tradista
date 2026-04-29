package org.eclipse.tradista.fx.common.validator;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.service.PricingParameterModuleValidator;
import org.eclipse.tradista.fx.common.model.PricingParameterUnrealizedPnlCalculationModule;
import org.eclipse.tradista.fx.common.model.PricingParameterUnrealizedPnlCalculationModule.BookProductTypePair;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

public class PricingParameterUnrealizedPnlCalculationModuleValidator implements PricingParameterModuleValidator {

	private BookBusinessDelegate bookBusinessDelegate;

	public PricingParameterUnrealizedPnlCalculationModuleValidator() {
		bookBusinessDelegate = new BookBusinessDelegate();
	}

	@Override
	public void validateModule(PricingParameterModule module, LegalEntity po) throws TradistaBusinessException {
		PricingParameterUnrealizedPnlCalculationModule mod = (PricingParameterUnrealizedPnlCalculationModule) module;
		StringBuilder errMsg = new StringBuilder();
		if (mod.getUnrealizedPnlCalculations() != null && !mod.getUnrealizedPnlCalculations().isEmpty()) {
			for (BookProductTypePair bookProd : mod.getUnrealizedPnlCalculations().keySet()) {
				if (po == null) {
					errMsg.append(String.format(
							"Global Pricing Parameters Set cannot contain unrealized P&L calculation results by book (%s).%n",
							bookProd.getBook()));
				} else if (bookProd.getBook() != null && !bookProd.getBook().getProcessingOrg().equals(po)) {
					errMsg.append(
							String.format("The Pricing Parameters Set's PO (%s) and the book %s's PO (%s) should be the same.%n",
									po, bookProd.getBook(), bookProd.getBook().getProcessingOrg()));
				}
			}
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	@Override
	public void checkAccess(PricingParameterModule module, StringBuilder errMsg) {
		PricingParameterUnrealizedPnlCalculationModule mod = (PricingParameterUnrealizedPnlCalculationModule) module;
		if (mod.getUnrealizedPnlCalculations() != null && !mod.getUnrealizedPnlCalculations().isEmpty()) {
			for (BookProductTypePair bookProd : mod.getUnrealizedPnlCalculations().keySet()) {
				Book b = null;
				try {
					b = bookBusinessDelegate.getBookById(bookProd.getBook().getId());
				} catch (TradistaBusinessException tbe) {
					// Not expected here.
				}
				if (b == null) {
					errMsg.append(String.format("the book %s was not found.%n", bookProd.getBook()));
				}
			}
		}
	}

}