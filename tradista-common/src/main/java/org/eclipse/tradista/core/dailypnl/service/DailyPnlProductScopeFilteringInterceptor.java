package org.eclipse.tradista.core.dailypnl.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.dailypnl.model.DailyPnl;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

/********************************************************************************
 * Copyright (c) 2021 Olivier Asuncion
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

public class DailyPnlProductScopeFilteringInterceptor {

	protected ProductBusinessDelegate productBusinessDelegate;

	public DailyPnlProductScopeFilteringInterceptor() {
		productBusinessDelegate = new ProductBusinessDelegate();
	}

	@AroundInvoke
	protected Object proceed(InvocationContext ic) throws TradistaBusinessException, Exception {
		preFilter(ic);
		return ic.proceed();
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		if (ic.getParameters()[0] instanceof DailyPnl) {
			DailyPnl dailyPnl = (DailyPnl) ic.getParameters()[0];
			// Only object creations are controlled.
			if (dailyPnl.getId() == 0) {
				PositionDefinition posDef = dailyPnl.getPositionDefinition();
				if (posDef.getProductType() != null)
					if (!productBusinessDelegate.getAllProducts().contains(posDef.getProduct())) {
						throw new TradistaBusinessException(String.format(
								"%s is not found among the allowed product types. Please contact your administrator.",
								posDef.getProductType()));
					}
			}
		}
	}
}
