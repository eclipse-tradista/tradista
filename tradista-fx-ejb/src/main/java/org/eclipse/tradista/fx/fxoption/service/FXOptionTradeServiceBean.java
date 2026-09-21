package org.eclipse.tradista.fx.fxoption.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.messaging.service.LocalCoreMessagingService;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.service.CheckTradeAccess;
import org.eclipse.tradista.core.trade.service.ProductScope;
import org.eclipse.tradista.core.trade.service.ProductScopeMode;
import org.eclipse.tradista.core.trade.service.TradeService;
import org.eclipse.tradista.fx.fx.service.FXTradeService;
import org.eclipse.tradista.fx.fxoption.messaging.FXOptionTradeEvent;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;
import org.eclipse.tradista.fx.fxoption.persistence.FXOptionTradeSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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
 *******************************************************************************/

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class FXOptionTradeServiceBean implements FXOptionTradeService {

	@EJB
	private FXTradeService fxTradeService;

	@EJB
	private LocalCoreMessagingService messagingConfigurationService;

	@EJB
	private TradeService tradeService;

	@ProductScope(value = FXOptionTrade.FX_OPTION, mode = ProductScopeMode.ON_CREATION)
	@Override
	public long saveFXOptionTrade(@CheckTradeAccess FXOptionTrade trade) throws TradistaBusinessException {
		tradeService.checkTradeBasics(trade, true);

		FXOptionTradeEvent event = new FXOptionTradeEvent();
		FXOptionTrade oldTrade = null;

		// If the option is exercised and the settlement is physical, make sure to
		// update the underlying
		// inventory calling the FX Service
		if (trade.getExerciseDate() != null && trade.getSettlementType().equals(OptionTrade.SettlementType.PHYSICAL)) {
			if (trade.getId() != 0) {
				oldTrade = FXOptionTradeSQL.getTradeById(trade.getId());
			}
			try {
				fxTradeService.saveFXTrade(trade.getUnderlying());
			} catch (TradistaBusinessException _) {
				// Should not happen here.
			}
		}

		if (trade.getId() != 0) {
			if (oldTrade == null) {
				oldTrade = FXOptionTradeSQL.getTradeById(trade.getId());
			}
			event.setOldTrade(oldTrade);
		}

		event.setTrade(trade);
		long result = FXOptionTradeSQL.saveFXOptionTrade(trade);

		messagingConfigurationService.publishEvent(event);
		return result;
	}

	@Override
	public FXOptionTrade getFXOptionTradeById(long id) {
		return FXOptionTradeSQL.getTradeById(id);
	}

}