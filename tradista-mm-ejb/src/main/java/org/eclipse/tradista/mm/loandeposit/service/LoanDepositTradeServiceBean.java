package org.eclipse.tradista.mm.loandeposit.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.service.TradeAuthorizationFilteringInterceptor;
import org.eclipse.tradista.mm.loandeposit.messaging.LoanDepositTradeEvent;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade;
import org.eclipse.tradista.mm.loandeposit.persistence.LoanDepositTradeSQL;
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
public class LoanDepositTradeServiceBean implements LoanDepositTradeService {

	private ConnectionFactory factory;

	private JMSContext context;

	private Destination destination;

	@PostConstruct
	private void initialize() {
		context = factory.createContext();
	}

	@Interceptors({ LoanDepositTradeProductScopeFilteringInterceptor.class,
			TradeAuthorizationFilteringInterceptor.class })
	@Override
	public long saveLoanDepositTrade(LoanDepositTrade trade) throws TradistaBusinessException {

		LoanDepositTradeEvent event = new LoanDepositTradeEvent();
		if (trade.getId() != 0) {
			LoanDepositTrade oldTrade = LoanDepositTradeSQL.getTradeById(trade.getId());
			event.setOldTrade(oldTrade);
		}

		event.setTrade(trade);
		long result = LoanDepositTradeSQL.saveLoanDepositTrade(trade);

		context.createProducer().send(destination, event);

		return result;
	}

	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	@Override
	public LoanDepositTrade getLoanDepositTradeById(long id) {
		return LoanDepositTradeSQL.getTradeById(id);
	}

	@PreDestroy
	private void clean() {
		context.close();
	}

}