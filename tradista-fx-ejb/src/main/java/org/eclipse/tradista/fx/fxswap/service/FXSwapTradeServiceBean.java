package org.eclipse.tradista.fx.fxswap.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.messaging.service.LocalCoreMessagingService;
import org.eclipse.tradista.core.trade.service.CheckTradeAccess;
import org.eclipse.tradista.core.trade.service.ProductScope;
import org.eclipse.tradista.core.trade.service.ProductScopeMode;
import org.eclipse.tradista.core.trade.service.TradeService;
import org.eclipse.tradista.fx.fxswap.messaging.FXSwapTradeEvent;
import org.eclipse.tradista.fx.fxswap.model.FXSwapTrade;
import org.eclipse.tradista.fx.fxswap.persistence.FXSwapTradeSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

/*
 * Copyright 2015 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class FXSwapTradeServiceBean implements FXSwapTradeService {

	@EJB
	private LocalCoreMessagingService messagingConfigurationService;

	@EJB
	private TradeService tradeService;

	@ProductScope(value = FXSwapTrade.FX_SWAP, mode = ProductScopeMode.ON_CREATION)
	@Override
	public long saveFXSwapTrade(@CheckTradeAccess FXSwapTrade trade) throws TradistaBusinessException {
		tradeService.checkTradeBasics(trade);

		FXSwapTradeEvent event = new FXSwapTradeEvent();
		if (trade.getId() != 0) {
			FXSwapTrade oldTrade = FXSwapTradeSQL.getTradeById(trade.getId());
			event.setOldTrade(oldTrade);
		}

		event.setTrade(trade);
		long result = FXSwapTradeSQL.saveFXSwapTrade(trade);

		messagingConfigurationService.publishEvent(event);
		return result;
	}

	@Override
	public FXSwapTrade getFXSwapTradeById(long id) {
		return FXSwapTradeSQL.getTradeById(id);
	}

}