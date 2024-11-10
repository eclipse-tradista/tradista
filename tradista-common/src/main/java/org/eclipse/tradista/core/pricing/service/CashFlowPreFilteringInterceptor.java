package org.eclipse.tradista.core.pricing.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.service.PositionDefinitionBusinessDelegate;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.service.TradeBusinessDelegate;

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

public class CashFlowPreFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private TradeBusinessDelegate tradeBusinessDelegate;

	private PositionDefinitionBusinessDelegate positionDefinitionBusinessDelegate;

	public CashFlowPreFilteringInterceptor() {
		super();
		tradeBusinessDelegate = new TradeBusinessDelegate();
		positionDefinitionBusinessDelegate = new PositionDefinitionBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters[0] instanceof Long tradeId) {
			if (tradeId != 0) {
				Trade<?> trade = tradeBusinessDelegate.getTradeById(tradeId, false);
				if (trade == null) {
					throw new TradistaBusinessException(String.format("The trade %d was not found.", tradeId));
				}
			}
		}
		if (parameters[0] instanceof PricingParameter pp) {
			if (!pp.getProcessingOrg().equals(getCurrentUser().getProcessingOrg())) {
				throw new TradistaBusinessException(String.format("The Pricing Parameters Set %s was not found.", pp));
			}
		}
		if (parameters[1] instanceof PricingParameter pp) {
			if (!pp.getProcessingOrg().equals(getCurrentUser().getProcessingOrg())) {
				throw new TradistaBusinessException(String.format("The Pricing Parameters Set %s was not found.", pp));
			}
		}
		if (parameters.length >= 3 && parameters[2] instanceof Long posDefId) {
			if (posDefId != 0) {
				PositionDefinition posDef = positionDefinitionBusinessDelegate.getPositionDefinitionById(posDefId);
				if (posDef == null) {
					throw new TradistaBusinessException(
							String.format("The Position Definition %s was not found.", posDef));
				}
			}
		}
	}

}