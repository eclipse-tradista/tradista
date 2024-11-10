package org.eclipse.tradista.ir.ccyswap.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.service.TradeAuthorizationFilteringInterceptor;
import org.eclipse.tradista.ir.ccyswap.messaging.CcySwapTradeEvent;
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;
import org.eclipse.tradista.ir.ccyswap.persistence.CcySwapTradeSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

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
public class CcySwapTradeServiceBean implements CcySwapTradeService {

	private ConnectionFactory factory;

	private JMSContext context;

	private Destination destination;

	@PostConstruct
	private void initialize() {
		context = factory.createContext();
	}

	@Interceptors({ CcySwapTradeProductScopeFilteringInterceptor.class, TradeAuthorizationFilteringInterceptor.class })
	@Override
	public long saveCcySwapTrade(CcySwapTrade trade) throws TradistaBusinessException {

		CcySwapTradeEvent event = new CcySwapTradeEvent();
		if (trade.getId() != 0) {
			CcySwapTrade oldTrade = CcySwapTradeSQL.getTradeById(trade.getId());
			event.setOldTrade(oldTrade);
		}

		event.setTrade(trade);
		long result = CcySwapTradeSQL.saveCcySwapTrade(trade);

		context.createProducer().send(destination, event);

		return result;

	}

	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	@Override
	public CcySwapTrade getCcySwapTradeById(long id) {
		return CcySwapTradeSQL.getTradeById(id);
	}

	@PreDestroy
	private void clean() {
		context.close();
	}

}