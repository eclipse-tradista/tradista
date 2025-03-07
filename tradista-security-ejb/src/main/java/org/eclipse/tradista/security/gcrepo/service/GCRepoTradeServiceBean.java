package org.eclipse.tradista.security.gcrepo.service;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.service.TradeAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.workflow.model.mapping.StatusMapper;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.gcrepo.messaging.GCRepoTradeEvent;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.gcrepo.persistence.GCRepoTradeSQL;
import org.eclipse.tradista.security.gcrepo.workflow.mapping.GCRepoTradeMapper;
import org.eclipse.tradista.security.repo.trade.RepoTradeUtil;
import org.jboss.ejb3.annotation.SecurityDomain;

import finance.tradista.flow.exception.TradistaFlowBusinessException;
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
 * Copyright (c) 2023 Olivier Asuncion
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
public class GCRepoTradeServiceBean implements GCRepoTradeService {

	private ConnectionFactory factory;

	private JMSContext context;

	private Destination destination;

	@PostConstruct
	private void initialize() {
		context = factory.createContext();
	}

	@Interceptors({ GCRepoProductScopeFilteringInterceptor.class, TradeAuthorizationFilteringInterceptor.class })
	@Override
	public long saveGCRepoTrade(GCRepoTrade trade, String action) throws TradistaBusinessException {
		GCRepoTradeEvent event = new GCRepoTradeEvent();
		long result;
		if (trade.getId() != 0) {
			GCRepoTrade oldTrade = GCRepoTradeSQL.getTradeById(trade.getId());
			event.setOldTrade(oldTrade);
		}

		RepoTradeUtil.getAllocatedCollateral(trade);

		if (!StringUtils.isEmpty(action)) {
			try {
				Workflow workflow = WorkflowManager.getWorkflowByName(trade.getWorkflow());
				org.eclipse.tradista.security.gcrepo.workflow.mapping.GCRepoTrade mappedTrade = GCRepoTradeMapper.map(trade,
						workflow);
				mappedTrade = WorkflowManager.applyAction(mappedTrade, action);
				trade.setStatus(StatusMapper.map(mappedTrade.getStatus()));
			} catch (TradistaFlowBusinessException tfbe) {
				throw new TradistaBusinessException(tfbe);
			}
		}

		event.setTrade(trade);
		event.setAppliedAction(action);
		result = GCRepoTradeSQL.saveGCRepoTrade(trade);
		context.createProducer().send(destination, event);

		return result;
	}

	@PreDestroy
	private void clean() {
		context.close();
	}

	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	@Override
	public GCRepoTrade getGCRepoTradeById(long id) {
		return GCRepoTradeSQL.getTradeById(id);
	}

	@Override
	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	public Map<Security, Map<Book, BigDecimal>> getAllocatedCollateral(GCRepoTrade trade)
			throws TradistaBusinessException {
		return RepoTradeUtil.getAllocatedCollateral(trade);
	}

}