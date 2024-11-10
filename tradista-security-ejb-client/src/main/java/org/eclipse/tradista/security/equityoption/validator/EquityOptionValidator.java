package org.eclipse.tradista.security.equityoption.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.security.common.validator.DefaultSecurityValidator;
import org.eclipse.tradista.security.equityoption.model.EquityOption;

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

public class EquityOptionValidator extends DefaultSecurityValidator {

	private static final long serialVersionUID = -3801876371578875255L;

	@Override
	public void validateProduct(Product product) throws TradistaBusinessException {
		EquityOption equityOption = (EquityOption) product;
		StringBuilder errMsg = new StringBuilder();
		if (equityOption.getUnderlying() == null) {
			errMsg.append(String.format("The underlying is mandatory.%n"));
		}

		if (equityOption.getStyle() == null) {
			errMsg.append(String.format("The style is mandatory.%n"));
		}

		if (equityOption.getCode() == null) {
			errMsg.append(String.format("The code is mandatory.%n"));
		}
		if (equityOption.getMaturityDate() == null) {
			errMsg.append(String.format("The maturity date is mandatory.%n"));
		}
		if (equityOption.getEquityOptionContractSpecification() == null) {
			errMsg.append(String.format("The equity option contract specification is mandatory.%n"));
		}
		if (equityOption.getStrike() == null) {
			errMsg.append(String.format("The strike is mandatory.%n"));
		} else {
			if (equityOption.getStrike().doubleValue() <= 0) {
				errMsg.append(
						String.format("The strike (%s) must be positive.%n", equityOption.getStrike().doubleValue()));
			}
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

}