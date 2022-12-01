package finance.tradista.ir.irswapoption.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;

import org.jboss.ejb3.annotation.SecurityDomain;

import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.trade.model.OptionTrade;
import finance.tradista.core.trade.service.TradeAuthorizationFilteringInterceptor;
import finance.tradista.ir.irswap.service.IRSwapTradeService;
import finance.tradista.ir.irswapoption.messaging.IRSwapOptionTradeEvent;
import finance.tradista.ir.irswapoption.model.IRSwapOptionTrade;
import finance.tradista.ir.irswapoption.persistence.IRSwapOptionTradeSQL;

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

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class IRSwapOptionTradeServiceBean implements IRSwapOptionTradeService {

	private ConnectionFactory factory;

	private JMSContext context;

	private Destination destination;

	@EJB
	private IRSwapTradeService irSwapTradeService;

	@PostConstruct
	private void initialize() {
		context = factory.createContext();
	}

	@Interceptors({ IRSwapOptionTradeProductScopeFilteringInterceptor.class,
			TradeAuthorizationFilteringInterceptor.class })
	@Override
	public long saveIRSwapOptionTrade(IRSwapOptionTrade trade) throws TradistaBusinessException {
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

		context.createProducer().send(destination, event);

		return result;

	}

	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	@Override
	public IRSwapOptionTrade getIRSwapOptionTradeById(long id) {
		return IRSwapOptionTradeSQL.getTradeById(id);
	}

	@PreDestroy
	private void clean() {
		context.close();
	}

}