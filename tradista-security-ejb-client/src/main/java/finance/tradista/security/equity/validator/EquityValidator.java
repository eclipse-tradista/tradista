package org.eclipse.tradista.security.equity.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.security.common.validator.DefaultSecurityValidator;
import org.eclipse.tradista.security.equity.model.Equity;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class EquityValidator extends DefaultSecurityValidator {

	private static final long serialVersionUID = 7682359998420318639L;

	@Override
	public void validateProduct(Product product) throws TradistaBusinessException {
		Equity equity = (Equity) product;
		StringBuilder errMsg = validateProductBasics(product);
		if (equity.getActiveFrom() == null) {
			errMsg.append(String.format("Active From is mandatory.%n"));
		} else {
			if (equity.getActiveTo() == null) {
				errMsg.append(String.format("Active To is mandatory.%n"));
			} else {
				if (equity.getActiveFrom().isAfter(equity.getActiveTo())) {
					errMsg.append(String.format("Active From cannot be after Active To.%n"));
				}
			}
		}

		if (equity.getTotalIssued() <= 0) {
			errMsg.append(String.format("Total issued (%s) must be positive.%n", equity.getTotalIssued()));
		}

		if (equity.getTradingSize() < 0) {
			errMsg.append(String.format("Trding size (%s) must be positive.%n", equity.getTradingSize()));
		}

		if (equity.isPayDividend()) {
			if (equity.getDividendCurrency() == null) {
				errMsg.append(String.format("The dividend currency is mandatory when Pay Dividend is selected.%n"));
			}
			if (equity.getDividendFrequency() == null) {
				errMsg.append(String.format("The dividend frequency is mandatory when Pay Dividend is selected.%n"));
			}
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

}
