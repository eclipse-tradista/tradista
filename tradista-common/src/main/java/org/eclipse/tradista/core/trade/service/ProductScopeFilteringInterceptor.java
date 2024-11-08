package org.eclipse.tradista.core.trade.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

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

public abstract class ProductScopeFilteringInterceptor {

	private ProductBusinessDelegate productBusinessDelegate;

	private String productType;

	private boolean found;

	public ProductScopeFilteringInterceptor(String productType) {
		this.productType = productType;
		productBusinessDelegate = new ProductBusinessDelegate();
		// The available product types list never changes without restart of the
		// application.
		found = productBusinessDelegate.getAvailableProductTypes().contains(productType);
	}

	@AroundInvoke
	protected Object proceed(InvocationContext ic) throws TradistaBusinessException, Exception {
		preFilter(ic);
		return ic.proceed();
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		// Interceptor applied on Trade, Product, Contract Specifications services.
		if (ic.getParameters()[0] instanceof TradistaObject tradistaObject) {
			// Only object creations are controlled.
			if (tradistaObject.getId() == 0) {
				if (!found) {
					throw new TradistaBusinessException(String.format(
							"%s is not found among the allowed product types. Please contact your administrator.",
							productType));
				}
			}
		} else {
			// Interceptor applied on Pricer services.
			if (!found) {
				throw new TradistaBusinessException(String.format(
						"%s is not found among the allowed product types. Please contact your administrator.",
						productType));
			}

		}
	}

}