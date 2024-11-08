package org.eclipse.tradista.ir.future.service;

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
import org.eclipse.tradista.ir.future.messaging.FutureTradeEvent;
import org.eclipse.tradista.ir.future.model.FutureTrade;
import org.eclipse.tradista.ir.future.persistence.FutureSQL;
import org.eclipse.tradista.ir.future.persistence.FutureTradeSQL;
import org.eclipse.tradista.ir.future.service.FutureTradeService;

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
public class FutureTradeServiceBean implements FutureTradeService {

	private ConnectionFactory factory;

	private JMSContext context;

	private Destination destination;

	@PostConstruct
	private void initialize() {
		context = factory.createContext();
	}

	@Interceptors({ FutureProductScopeFilteringInterceptor.class, TradeAuthorizationFilteringInterceptor.class })
	@Override
	public long saveFutureTrade(FutureTrade trade) throws TradistaBusinessException {

		FutureTradeEvent event = new FutureTradeEvent();

		if (trade.getProduct().getId() == 0) {
			FutureSQL.saveFuture(trade.getProduct());
		}

		if (trade.getId() != 0) {
			FutureTrade oldTrade = FutureTradeSQL.getTradeById(trade.getId());
			event.setOldTrade(oldTrade);
		}

		event.setTrade(trade);
		long result = FutureTradeSQL.saveFutureTrade(trade);

		context.createProducer().send(destination, event);

		return result;
	}

	@PreDestroy
	private void clean() {
		context.close();
	}

	@Override
	public List<FutureTrade> getFutureTradesBeforeTradeDateByFutureAndBookIds(LocalDate date, long futureId,
			long bookId) {
		return FutureTradeSQL.getFutureTradesBeforeTradeDateByFutureAndBookIds(date, futureId, bookId);
	}

	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	@Override
	public FutureTrade getFutureTradeById(long id) {
		return FutureTradeSQL.getTradeById(id);
	}

}