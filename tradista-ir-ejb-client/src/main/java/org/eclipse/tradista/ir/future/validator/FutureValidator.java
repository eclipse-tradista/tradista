package org.eclipse.tradista.ir.future.validator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.validator.DefaultProductValidator;
import org.eclipse.tradista.ir.future.model.Future;
import org.eclipse.tradista.ir.future.service.FutureBusinessDelegate;

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

public class FutureValidator extends DefaultProductValidator {

	private static final long serialVersionUID = 1121603261939982187L;

	@Override
	public void validateProduct(Product product) throws TradistaBusinessException {
		Future future = (Future) product;
		StringBuilder errMsg = validateProductBasics(product);

		try {
			validateSymbol(future.getSymbol());
		} catch (TradistaBusinessException tbe) {
			errMsg.append(String.format("%s%n", tbe.getMessage()));
		}

		if (future.getContractSpecification() == null) {
			errMsg.append(String.format("The contract specification is mandatory.%n"));
		}

		if (future.getMaturityDate() == null) {
			errMsg.append("The maturity date is mandatory.");
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

	public void validateSymbol(String symbol) throws TradistaBusinessException {
		if (StringUtils.isEmpty(symbol)) {
			throw new TradistaBusinessException("The symbol is mandatory.");
		}
		// Checking symbol format.
		if (symbol.length() != 5) {
			throw new TradistaBusinessException(
					String.format("The symbol (%s)'s length must be 5 characters.", symbol));
		}

		// Checking the year.
		try {
			Integer.parseInt(symbol.substring(3));
		} catch (NumberFormatException _) {
			throw new TradistaBusinessException(
					String.format("The symbol's year ('%s') is not correct.", symbol.substring(3)));
		}
		// checking the month.
		new FutureBusinessDelegate().getMonth(symbol.substring(0, 3));
	}

}
