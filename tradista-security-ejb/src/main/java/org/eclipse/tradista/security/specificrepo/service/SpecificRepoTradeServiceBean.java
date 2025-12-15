package org.eclipse.tradista.security.specificrepo.service;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.trade.service.TradeAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.workflow.model.mapping.StatusMapper;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.repo.trade.RepoTradeUtil;
import org.eclipse.tradista.security.specificrepo.messaging.SpecificRepoTradeEvent;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;
import org.eclipse.tradista.security.specificrepo.persistence.SpecificRepoTradeSQL;
import org.eclipse.tradista.security.specificrepo.workflow.mapping.SpecificRepoTradeMapper;
import org.jboss.ejb3.annotation.SecurityDomain;

import finance.tradista.flow.exception.TradistaFlowBusinessException;
import finance.tradista.flow.exception.TradistaFlowTechnicalException;
import finance.tradista.flow.model.Workflow;
import finance.tradista.flow.service.WorkflowManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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
public class SpecificRepoTradeServiceBean implements SpecificRepoTradeService {

	private ConnectionFactory factory;

	private JMSContext context;

	private Destination destination;

	@PostConstruct
	private void initialize() {
		context = factory.createContext();
	}

	@Interceptors({ SpecificRepoProductScopeFilteringInterceptor.class, TradeAuthorizationFilteringInterceptor.class })
	@Override
	public long saveSpecificRepoTrade(SpecificRepoTrade trade, String action) throws TradistaBusinessException {
		SpecificRepoTradeEvent event = new SpecificRepoTradeEvent();
		long result;
		if (trade.getId() != 0) {
			SpecificRepoTrade oldTrade = SpecificRepoTradeSQL.getTradeById(trade.getId());
			event.setOldTrade(oldTrade);
		}

		RepoTradeUtil.getAllocatedCollateral(trade);

		if (!StringUtils.isEmpty(action)) {
			try {
				Workflow<org.eclipse.tradista.security.specificrepo.workflow.mapping.SpecificRepoTrade> workflow = WorkflowManager
						.getWorkflowByName(trade.getWorkflow());
				org.eclipse.tradista.security.specificrepo.workflow.mapping.SpecificRepoTrade mappedTrade = SpecificRepoTradeMapper
						.map(trade, workflow);
				mappedTrade = WorkflowManager.applyAction(mappedTrade, action);
				trade.setStatus(StatusMapper.map(mappedTrade.getStatus()));
			} catch (TradistaFlowBusinessException tfbe) {
				throw new TradistaBusinessException(tfbe);
			} catch (TradistaFlowTechnicalException tfte) {
				throw new TradistaTechnicalException(tfte);
			}
		}

		event.setTrade(trade);
		event.setAppliedAction(action);
		result = SpecificRepoTradeSQL.saveSpecificRepoTrade(trade);
		context.createProducer().send(destination, event);

		return result;
	}

	@PreDestroy
	private void clean() {
		context.close();
	}

	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	@Override
	public SpecificRepoTrade getSpecificRepoTradeById(long id) {
		return SpecificRepoTradeSQL.getTradeById(id);
	}

	@Override
	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	public Map<Security, Map<Book, BigDecimal>> getAllocatedCollateral(SpecificRepoTrade trade)
			throws TradistaBusinessException {
		return RepoTradeUtil.getAllocatedCollateral(trade);
	}

}