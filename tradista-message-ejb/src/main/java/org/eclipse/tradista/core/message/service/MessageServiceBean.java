package org.eclipse.tradista.core.message.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.util.TradistaConstants;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.message.persistence.MessageSQL;
import org.eclipse.tradista.core.message.workflow.mapping.MessageMapper;
import org.eclipse.tradista.core.workflow.model.mapping.StatusMapper;
import org.jboss.ejb3.annotation.SecurityDomain;

import finance.tradista.flow.exception.TradistaFlowBusinessException;
import finance.tradista.flow.exception.TradistaFlowTechnicalException;
import finance.tradista.flow.model.Workflow;
import finance.tradista.flow.service.WorkflowManager;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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
public class MessageServiceBean implements MessageService {

	@Override
	public long saveMessage(Message message) {
		return MessageSQL.saveMessage(message);
	}

	@Override
	public void applyAction(Message message, String action) throws TradistaBusinessException {
		try {
			Workflow<org.eclipse.tradista.core.message.workflow.mapping.Message> workflow = WorkflowManager
					.getWorkflowByName(message.getWorkflow());
			org.eclipse.tradista.core.message.workflow.mapping.Message mappedMessage = MessageMapper.map(message,
					workflow);
			mappedMessage = WorkflowManager.applyAction(mappedMessage, action);
			message.setStatus(StatusMapper.map(mappedMessage.getStatus()));
		} catch (TradistaFlowBusinessException tfbe) {
			throw new TradistaBusinessException(tfbe);
		} catch (TradistaFlowTechnicalException tfte) {
			throw new TradistaTechnicalException(tfte);
		}
	}

	@Override
	public List<Message> getMessages(long id, Boolean isIncoming, Set<String> types, Set<String> interfaceNames,
			long objectId, Set<String> objectTypes, Set<String> statuses, LocalDateTime creationDateTimeFrom,
			LocalDateTime creationDateTimeTo, LocalDateTime lastUpdateDateTimeFrom,
			LocalDateTime lastUpdateDateTimeTo) {
		return MessageSQL.getMessages(id, isIncoming, types, interfaceNames, objectId, objectTypes, statuses,
				creationDateTimeFrom, creationDateTimeTo, lastUpdateDateTimeFrom, lastUpdateDateTimeTo);
	}

	@Override
	public Set<String> getAllMessageTypes() {
		return TradistaUtil.getDistinctValuesFromProperties(TradistaConstants.META_INF, "messageTypes");
	}

	@Override
	public Set<String> getAllObjectTypes() {
		return TradistaUtil.getDistinctValuesFromProperties(TradistaConstants.META_INF, "objectTypes");
	}

}