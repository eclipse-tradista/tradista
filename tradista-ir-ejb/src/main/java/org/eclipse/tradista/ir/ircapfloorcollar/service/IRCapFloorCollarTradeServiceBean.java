package org.eclipse.tradista.ir.ircapfloorcollar.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.messaging.service.LocalCoreMessagingService;
import org.eclipse.tradista.core.trade.service.CheckTradeAccess;
import org.eclipse.tradista.core.trade.service.ProductScope;
import org.eclipse.tradista.core.trade.service.ProductScopeMode;
import org.eclipse.tradista.core.trade.service.TradeService;
import org.eclipse.tradista.ir.ircapfloorcollar.messaging.IRCapFloorCollarTradeEvent;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;
import org.eclipse.tradista.ir.ircapfloorcollar.persistence.IRCapFloorCollarTradeSQL;
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
 ********************************************************************************/

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class IRCapFloorCollarTradeServiceBean implements IRCapFloorCollarTradeService {

	@EJB
	private LocalCoreMessagingService messagingConfigurationService;

	@EJB
	private TradeService tradeService;

	@ProductScope(value = IRCapFloorCollarTrade.IR_CAP_FLOOR_COLLAR, mode = ProductScopeMode.ON_CREATION)
	@Override
	public long saveIRCapFloorCollarTrade(@CheckTradeAccess IRCapFloorCollarTrade trade)
			throws TradistaBusinessException {
		tradeService.checkTradeBasics(trade, true);
		IRCapFloorCollarTradeEvent event = new IRCapFloorCollarTradeEvent();
		if (trade.getId() != 0) {
			IRCapFloorCollarTrade oldTrade = IRCapFloorCollarTradeSQL.getTradeById(trade.getId());
			event.setOldTrade(oldTrade);
		}

		event.setTrade(trade);
		long result = IRCapFloorCollarTradeSQL.saveIRCapFloorCollarTrade(trade);

		messagingConfigurationService.publishEvent(event);
		return result;
	}

	@Override
	public IRCapFloorCollarTrade getIRCapFloorCollarTradeById(long id) {
		return IRCapFloorCollarTradeSQL.getTradeById(id);
	}

}