package org.eclipse.tradista.core.product.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;

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

public class DefaultProductValidator implements ProductValidator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7230180363740521856L;

	@Override
	public void validateProduct(Product product) throws TradistaBusinessException {
		StringBuilder errMsg = validateProductBasics(product);
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	protected StringBuilder validateProductBasics(Product product) throws TradistaBusinessException {
		// Existence controls
		if (product == null) {
			throw new TradistaBusinessException("The product cannot be null.");
		}
		StringBuilder errMsg = new StringBuilder();
		if (product.getExchange() == null) {
			errMsg.append(String.format("The exchange is mandatory.%n"));
		}

		return errMsg;
	}

}
