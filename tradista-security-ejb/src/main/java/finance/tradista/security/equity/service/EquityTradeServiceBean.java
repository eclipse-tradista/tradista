package org.eclipse.tradista.security.equity.service;

import java.time.LocalDate;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;

import org.jboss.ejb3.annotation.SecurityDomain;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.service.TradeAuthorizationFilteringInterceptor;
import org.eclipse.tradista.security.equity.messaging.EquityTradeEvent;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.persistence.EquityTradeSQL;

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
public class EquityTradeServiceBean implements EquityTradeService {

	private ConnectionFactory factory;

	private JMSContext context;

	private Destination destination;

	@PostConstruct
	private void initialize() {
		context = factory.createContext();
	}

	@Interceptors({ EquityProductScopeFilteringInterceptor.class, TradeAuthorizationFilteringInterceptor.class })
	@Override
	public long saveEquityTrade(EquityTrade trade) throws TradistaBusinessException {
		EquityTradeEvent event = new EquityTradeEvent();
		if (trade.getId() != 0) {
			EquityTrade oldTrade = EquityTradeSQL.getTradeById(trade.getId(), false);
			// oldTrade can be null when the trade is an option underlying that
			// has just been exercised.
			if (oldTrade != null) {
				event.setOldTrade(oldTrade);
			}
		}

		event.setTrade(trade);
		long result = EquityTradeSQL.saveEquityTrade(trade);

		context.createProducer().send(destination, event);

		return result;

	}

	@Override
	public List<EquityTrade> getEquityTradesBeforeTradeDateByEquityAndBookIds(LocalDate date, long equityId,
			long bookId) {
		return EquityTradeSQL.getEquityTradesBeforeTradeDateByEquityAndBookIds(date, equityId, bookId);
	}

	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	@Override
	public EquityTrade getEquityTradeById(long id) {
		return EquityTradeSQL.getTradeById(id, false);
	}

	@PreDestroy
	private void clean() {
		context.close();
	}

}