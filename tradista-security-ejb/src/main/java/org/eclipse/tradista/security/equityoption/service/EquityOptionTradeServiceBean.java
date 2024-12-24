package org.eclipse.tradista.security.equityoption.service;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.service.TradeAuthorizationFilteringInterceptor;
import org.eclipse.tradista.security.equity.service.EquityTradeService;
import org.eclipse.tradista.security.equityoption.messaging.EquityOptionTradeEvent;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;
import org.eclipse.tradista.security.equityoption.persistence.EquityOptionTradeSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
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
public class EquityOptionTradeServiceBean implements EquityOptionTradeService {

	private ConnectionFactory factory;

	private JMSContext context;

	private Destination destination;

	@EJB
	private EquityTradeService equityTradeService;

	@PostConstruct
	private void initialize() {
		context = factory.createContext();
	}

	@Interceptors({ EquityOptionProductScopeFilteringInterceptor.class, TradeAuthorizationFilteringInterceptor.class })
	@Override
	public long saveEquityOptionTrade(EquityOptionTrade trade) throws TradistaBusinessException {

		EquityOptionTradeEvent event = new EquityOptionTradeEvent();

		// If the option is expired and the settlement is physical, make sure to
		// update the underlying
		// inventory calling the Equity Service
		if (trade.getUnderlying().getTradeDate() != null
				&& trade.getSettlementType().equals(OptionTrade.SettlementType.PHYSICAL)) {
			trade.getUnderlying().setId(equityTradeService.saveEquityTrade(trade.getUnderlying()));
		}

		if (trade.getId() != 0) {
			EquityOptionTrade oldTrade = EquityOptionTradeSQL.getTradeById(trade.getId());
			// If the option was expired but is not anymore and if the settlement was
			// physical, use the equity trade service for cancellation of the underlying's
			// transfers.
			if (trade.getExerciseDate() == null && oldTrade.getExerciseDate() != null
					&& oldTrade.getSettlementType().equals(OptionTrade.SettlementType.PHYSICAL)) {
				trade.getUnderlying().setId(equityTradeService.saveEquityTrade(trade.getUnderlying()));
			}
			event.setOldTrade(oldTrade);
		}

		event.setTrade(trade);
		long result = EquityOptionTradeSQL.saveEquityOptionTrade(trade);

		context.createProducer().send(destination, event);

		return result;
	}

	@Override
	public List<EquityOptionTrade> getEquityOptionTradesBeforeTradeDateByEquityOptionAndBookIds(LocalDate tradeDate,
			long equityOptionId, long bookId) {
		return EquityOptionTradeSQL.getEquityOptionTradesBeforeTradeDateByEquityOptionAndBookIds(tradeDate,
				equityOptionId, bookId);
	}

	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	@Override
	public EquityOptionTrade getEquityOptionTradeById(long id) {
		return EquityOptionTradeSQL.getTradeById(id);
	}

	@PreDestroy
	private void clean() {
		context.close();
	}

}