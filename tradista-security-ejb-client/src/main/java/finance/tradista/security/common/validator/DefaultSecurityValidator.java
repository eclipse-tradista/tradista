package org.eclipse.tradista.security.common.validator;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.validator.DefaultProductValidator;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.eclipse.tradista.security.common.model.Security;

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

public class DefaultSecurityValidator extends DefaultProductValidator {

	private static final long serialVersionUID = 6255982705139838320L;

	@Override
	public void validateProduct(Product product) throws TradistaBusinessException {
		StringBuilder errMsg = validateProductBasics(product);
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	/*
	 * Making issue date and issue price optional. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tradista.core.validator.DefaultProductValidator#validateProductBasics
	 * (org.eclipse.tradista.core.product.Product)
	 */
	protected StringBuilder validateProductBasics(Product product) throws TradistaBusinessException {
		Security security = (Security) product;

		StringBuilder errMsg = super.validateProductBasics(security);

		if (security.getCurrency() == null) {
			errMsg.append(String.format("The currency is mandatory.%n"));
		}

		if (StringUtils.isBlank(security.getIsin())) {
			errMsg.append(String.format("The ISIN is mandatory.%n"));
		}

		if (security.getIssuer() == null) {
			errMsg.append(String.format("The issuer is mandatory.%n"));
		} else {
			LegalEntity issuer = new LegalEntityBusinessDelegate()
					.getLegalEntityByShortName(security.getIssuer().getShortName());
			if (issuer == null) {
				errMsg.append(String.format("The issuer (%s) must exist in the system.%n",
						security.getIssuer().getShortName()));
			}
		}

		if (security.getIssueDate() == null) {
			errMsg.append(String.format("The issue date is mandatory.%n"));
		}

		if (security.getIssuePrice() == null) {
			errMsg.append(String.format("The issue price is mandatory.%n"));
		} else {
			if (security.getIssuePrice().doubleValue() <= 0) {
				errMsg.append(
						String.format("The issue price (%s) is mandatory.%n", security.getIssuePrice().doubleValue()));
			}
		}

		return errMsg;
	}

}
