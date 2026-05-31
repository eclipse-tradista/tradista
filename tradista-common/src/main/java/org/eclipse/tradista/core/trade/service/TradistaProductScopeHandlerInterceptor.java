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

package org.eclipse.tradista.core.trade.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.position.service.PositionDefinitionBusinessDelegate;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.model.ProductScoped;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Global interceptor responsible for enforcing product scope validation. It
 * relies on the @ProductScope annotation.
 */
@Interceptor
public class TradistaProductScopeHandlerInterceptor {

	private ProductBusinessDelegate productBusinessDelegate;

	public TradistaProductScopeHandlerInterceptor() {
		productBusinessDelegate = new ProductBusinessDelegate();
	}

	@AroundInvoke
	public Object proceed(InvocationContext ic) throws TradistaBusinessException, Exception {
		preFilter(ic);
		return ic.proceed();
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		ProductScope annotation = ic.getMethod().getAnnotation(ProductScope.class);
		if (annotation == null) {
			annotation = ic.getTarget().getClass().getAnnotation(ProductScope.class);
		}

		if (annotation == null) {
			return;
		}

		String type = annotation.value();
		ProductScopeMode mode = annotation.mode();
		Product product = null;

		if (type.isEmpty()) {
			// Dynamic detection
			Object[] parameters = ic.getParameters();
			if (parameters != null && parameters.length > 0) {
				for (Object param : parameters) {
					if (param instanceof ProductScoped productScoped) {
						type = productScoped.getProductType();
						product = productScoped.getProduct();
					} else if (param instanceof String posDefName) {
						try {
							PositionDefinitionBusinessDelegate posDefBusinessDelegate = new PositionDefinitionBusinessDelegate();
							ProductScoped posDef = posDefBusinessDelegate.getPositionDefinitionByName(posDefName);
							if (posDef != null) {
								type = posDef.getProductType();
								product = posDef.getProduct();
							}
						} catch (TradistaBusinessException _) {
							// If it's not a valid position definition name, we just skip it
						}
					}

					if (type != null && !type.isEmpty() || product != null) {
						break;
					}
				}
			}
		}

		if (type == null || type.isEmpty()) {
			return;
		}

		if (mode == ProductScopeMode.ON_CREATION) {
			boolean isCreationDetected = false;
			if (ic.getParameters() != null) {
				for (Object parameter : ic.getParameters()) {
					if (parameter instanceof TradistaObject tradistaObject) {
						if (tradistaObject.getId() == 0) {
							isCreationDetected = true;
							break;
						}
					}
				}
			}

			if (!isCreationDetected) {
				return;
			}
		}

		StringBuilder errMsg = new StringBuilder();
		if (type != null && !type.isEmpty()) {
			if (!productBusinessDelegate.getAvailableProductTypes().contains(type)) {
				errMsg.append(String.format(
						"%s is not found among the allowed product types. Please contact your administrator.%n", type));
			}
		}
		if (product != null) {
			if (!productBusinessDelegate.getAllProducts().contains(product)) {
				errMsg.append(String.format("%s cannot be found. Please contact your administrator.", product));
			}
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}
