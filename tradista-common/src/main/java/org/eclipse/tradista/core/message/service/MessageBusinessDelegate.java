package org.eclipse.tradista.core.message.service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.message.workflow.mapping.MessageMapper;
import org.eclipse.tradista.core.workflow.model.mapping.StatusMapper;
import org.springframework.util.CollectionUtils;

import finance.tradista.flow.exception.TradistaFlowBusinessException;
import finance.tradista.flow.model.Workflow;
import finance.tradista.flow.service.WorkflowManager;

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

public class MessageBusinessDelegate implements Serializable {

	private static final long serialVersionUID = -3861846524598598760L;

	private MessageService messageService;

	public MessageBusinessDelegate() {
		messageService = TradistaServiceLocator.getInstance().getMessageService();
	}

	public long saveMessage(Message message) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (message == null) {
			throw new TradistaBusinessException("the message cannot be null.");
		}
		if (StringUtils.isBlank(message.getType())) {
			errMsg.append(String.format("the message type is mandatory.%n"));
		}
		if (StringUtils.isBlank(message.getObjectType()) && message.getObjectId() > 0) {
			errMsg.append(String.format("the message object type cannot be blank when the object id is positive.%n"));
		}
		if (!StringUtils.isBlank(message.getObjectType()) && message.getObjectId() <= 0) {
			errMsg.append(String.format("the message object id should be positive when the object type is present.%n"));
		}
		if (message.getStatus() == null) {
			errMsg.append("The status is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return messageService.saveMessage(message);
	}

	public void applyAction(Message message, String action) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(action)) {
			errMsg.append(String.format("The action is mandatory.%n"));
		}
		if (message == null) {
			errMsg.append("The message is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		try {
			Workflow<org.eclipse.tradista.core.message.workflow.mapping.Message> workflow = WorkflowManager
					.getWorkflowByName(message.getWorkflow());
			org.eclipse.tradista.core.message.workflow.mapping.Message mappedMessage = MessageMapper.map(message,
					workflow);
			mappedMessage = WorkflowManager.applyAction(mappedMessage, action);
			message.setStatus(StatusMapper.map(mappedMessage.getStatus()));
		} catch (TradistaFlowBusinessException tfbe) {
			throw new TradistaBusinessException(tfbe);
		}
	}

	public List<Message> getMessages(long id, Boolean isIncoming, Set<String> types, Set<String> interfaceNames,
			long objectId, Set<String> objectTypes, Set<String> statuses, LocalDateTime creationDateTimeFrom,
			LocalDateTime creationDateTimeTo, LocalDateTime lastUpdateDateTimeFrom, LocalDateTime lastUpdateDateTimeTo)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		List<Set<?>> sets = List.of(types, interfaceNames, objectTypes);
		long nonEmptyCount = sets.stream().filter(set -> !CollectionUtils.isEmpty(set)).count();

		if (nonEmptyCount > 1) {
			errMsg.append("You must select only one among Types, Interface Names or Object Types.");
		}
		DateUtil.checkNotAfter(creationDateTimeFrom, creationDateTimeTo, "Creation Date From", "Creation Date To",
				errMsg);
		DateUtil.checkNotAfter(lastUpdateDateTimeFrom, lastUpdateDateTimeTo, "Last Update From", "Last Update To",
				errMsg);
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(
				() -> messageService.getMessages(id, isIncoming, types, interfaceNames, objectId, objectTypes, statuses,
						creationDateTimeFrom, creationDateTimeTo, lastUpdateDateTimeFrom, lastUpdateDateTimeTo));
	}

	public Set<String> getAllMessageTypes() {
		return SecurityUtil.run(() -> messageService.getAllMessageTypes());
	}
	
	public Set<String> getAllObjectTypes() {
		return SecurityUtil.run(() -> messageService.getAllObjectTypes());
	}

}