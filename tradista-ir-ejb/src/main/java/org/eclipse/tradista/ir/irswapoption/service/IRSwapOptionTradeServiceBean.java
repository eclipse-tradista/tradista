package org.eclipse.tradista.ir.irswapoption.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.service.CheckTradeAccess;
import org.eclipse.tradista.core.trade.service.ProductScope;
import org.eclipse.tradista.core.trade.service.ProductScopeMode;
import org.eclipse.tradista.core.trade.service.TradeService;
import org.eclipse.tradista.ir.irswap.service.IRSwapTradeService;
import org.eclipse.tradista.ir.irswapoption.messaging.IRSwapOptionTradeEvent;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;
import org.eclipse.tradista.ir.irswapoption.persistence.IRSwapOptionTradeSQL;
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
 ********************************************************************************/

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class IRSwapOptionTradeServiceBean implements IRSwapOptionTradeService {


	@EJB
	private IRSwapTradeService irSwapTradeService;

	@EJB
	private LocalCoreMessagingService messagingConfigurationService;

	@EJB
	private TradeService tradeService;

	@PostConstruct
	private void initialize() {
	}

	@ProductScope(value = IRSwapOptionTrade.IR_SWAP_OPTION, mode = ProductScopeMode.ON_CREATION)
	@Override
	public long saveIRSwapOptionTrade(@CheckTradeAccess IRSwapOptionTrade trade) throws TradistaBusinessException {
		tradeService.checkTradeBasics(trade, true);
		IRSwapOptionTradeEvent event = new IRSwapOptionTradeEvent();

		IRSwapOptionTrade oldTrade = null;
		// If the option is exercised and the settlement is physical, make sure to
		// update the underlying
		// inventory calling the IRSwap Service
		if (trade.getExerciseDate() != null && trade.getSettlementType().equals(OptionTrade.SettlementType.PHYSICAL)) {
			if (trade.getId() != 0) {
				oldTrade = IRSwapOptionTradeSQL.getTradeById(trade.getId());
			}
			trade.getUnderlying().setId(irSwapTradeService.saveIRSwapTrade(trade.getUnderlying()));
		}

		if (trade.getId() != 0) {
			if (oldTrade == null) {
				oldTrade = IRSwapOptionTradeSQL.getTradeById(trade.getId());
			}
			// If the option was expired but is not anymore and if the settlement was
			// physical, use the ir swap trade service for cancellation of the underlying's
			// transfers.
			if (trade.getExerciseDate() == null && oldTrade.getExerciseDate() != null
					&& oldTrade.getSettlementType().equals(OptionTrade.SettlementType.PHYSICAL)) {
				trade.getUnderlying().setId(irSwapTradeService.saveIRSwapTrade(trade.getUnderlying()));
			}
			event.setOldTrade(oldTrade);
		}

		event.setTrade(trade);
		long result = IRSwapOptionTradeSQL.saveIRSwapOptionTrade(trade);


		messagingConfigurationService.publishEvent(event);
		return result;

	}

	@Override
	public IRSwapOptionTrade getIRSwapOptionTradeById(long id) {
		return IRSwapOptionTradeSQL.getTradeById(id);
	}


}