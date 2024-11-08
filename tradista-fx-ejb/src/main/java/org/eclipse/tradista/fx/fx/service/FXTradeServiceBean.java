package org.eclipse.tradista.fx.fx.service;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.service.TradeAuthorizationFilteringInterceptor;
import org.eclipse.tradista.fx.fx.messaging.FXTradeEvent;
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fx.persistence.FXTradeSQL;
import org.eclipse.tradista.fx.fx.service.FXTradeBusinessDelegate;
import org.eclipse.tradista.fx.fx.service.FXTradeService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;

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
public class FXTradeServiceBean implements FXTradeService {

	private ConnectionFactory factory;

	private JMSContext context;

	private Destination destination;

	private FXTradeBusinessDelegate fxTradeBusinessDelegate;

	@PostConstruct
	private void initialize() {
		context = factory.createContext();
		fxTradeBusinessDelegate = new FXTradeBusinessDelegate();
	}

	@Interceptors({ FXProductScopeFilteringInterceptor.class, TradeAuthorizationFilteringInterceptor.class })
	@Override
	public long saveFXTrade(FXTrade trade) throws TradistaBusinessException {

		FXTradeEvent event = new FXTradeEvent();
		if (trade.getId() != 0) {
			FXTrade oldTrade = FXTradeSQL.getTradeById(trade.getId(), false);
			event.setOldTrade(oldTrade);
		}

		event.setTrade(trade);
		long result = FXTradeSQL.saveFXTrade(trade);
		try {
			fxTradeBusinessDelegate.determinateType(trade);
		} catch (TradistaBusinessException tbe) {
			// Should not happen here.
		}

		context.createProducer().send(destination, event);

		return result;
	}

	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	@Override
	public FXTrade getFXTradeById(long id) {
		return FXTradeSQL.getTradeById(id, false);
	}

	@PreDestroy
	private void clean() {
		context.close();
	}

}