package org.eclipse.tradista.commercial.commercial.service;

import org.eclipse.tradista.commercial.commercial.messaging.CommercialTradeEvent;
import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.commercial.commercial.persistence.CommercialTradeSQL;
import org.eclipse.tradista.commercial.commercial.workflow.mapping.CommercialTradeMapper;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.messaging.service.LocalCoreMessagingService;
import org.eclipse.tradista.core.workflow.model.mapping.StatusMapper;
import org.jboss.ejb3.annotation.SecurityDomain;

import finance.tradista.flow.exception.TradistaFlowBusinessException;
import finance.tradista.flow.exception.TradistaFlowTechnicalException;
import finance.tradista.flow.model.Workflow;
import finance.tradista.flow.service.WorkflowManager;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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
public class CommercialTradeServiceBean implements CommercialTradeService {

	@EJB
	private LocalCoreMessagingService messagingConfigurationService;

	@Override
	public long saveCommercialTrade(CommercialTrade trade, String action) throws TradistaBusinessException {
		CommercialTradeEvent event = new CommercialTradeEvent();
		if (trade.getId() != 0) {
			CommercialTrade oldTrade = CommercialTradeSQL.getTradeById(trade.getId());
			event.setOldTrade(oldTrade);
		}

		if (action != null && !action.isEmpty()) {
			try {
				Workflow<org.eclipse.tradista.commercial.commercial.workflow.mapping.CommercialTrade> workflow = WorkflowManager
						.getWorkflowByName(trade.getWorkflow());
				org.eclipse.tradista.commercial.commercial.workflow.mapping.CommercialTrade mappedTrade = CommercialTradeMapper
						.map(trade, workflow);
				mappedTrade = WorkflowManager.applyAction(mappedTrade, action);
				trade.setStatus(StatusMapper.map(mappedTrade.getStatus()));
			} catch (TradistaFlowBusinessException tfbe) {
				throw new TradistaBusinessException(tfbe.getMessage());
			} catch (TradistaFlowTechnicalException tfte) {
				throw new TradistaFlowTechnicalException(tfte.getMessage());
			}
		}

		event.setTrade(trade);
		long result = CommercialTradeSQL.saveTrade(trade);
		messagingConfigurationService.publishEvent(event);
		return result;
	}

	@Override
	public CommercialTrade getCommercialTradeById(long id) {
		return CommercialTradeSQL.getTradeById(id);
	}

}