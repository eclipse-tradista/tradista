package org.eclipse.tradista.ir.fra.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.service.CheckTradeAccess;
import org.eclipse.tradista.core.trade.service.ProductScope;
import org.eclipse.tradista.core.trade.service.ProductScopeMode;
import org.eclipse.tradista.core.trade.service.TradeService;
import org.eclipse.tradista.ir.fra.messaging.FRATradeEvent;
import org.eclipse.tradista.ir.fra.model.FRATrade;
import org.eclipse.tradista.ir.fra.persistence.FRATradeSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.eclipse.tradista.core.common.messaging.service.LocalCoreMessagingService;




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
public class FRATradeServiceBean implements FRATradeService {

	@EJB
	private LocalCoreMessagingService messagingConfigurationService;

	@EJB
	private TradeService tradeService;

	@PostConstruct
	private void initialize() {
	}

	@ProductScope(value = FRATrade.FRA, mode = ProductScopeMode.ON_CREATION)
	@Override
	public long saveFRATrade(@CheckTradeAccess FRATrade trade) throws TradistaBusinessException {
		tradeService.checkTradeBasics(trade, true);
		FRATradeEvent event = new FRATradeEvent();
		if (trade.getId() != 0) {
			FRATrade oldTrade = FRATradeSQL.getTradeById(trade.getId());
			event.setOldTrade(oldTrade);
		}

		event.setTrade(trade);
		long result = FRATradeSQL.saveFRATrade(trade);


		messagingConfigurationService.publishEvent(event);
		return result;
	}

	@Override
	public FRATrade getFRATradeById(long id) {
		return FRATradeSQL.getTradeById(id);
	}


}