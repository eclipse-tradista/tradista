package org.eclipse.tradista.core.message.service;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.message.workflow.mapping.MessageMapper;
import org.eclipse.tradista.core.workflow.model.mapping.StatusMapper;

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
			errMsg.append("the message type is mandatory.");
		}
		if (StringUtils.isBlank(message.getObjectType()) && message.getObjectId() > 0) {
			errMsg.append("the message object type cannot be blank when the object id is positive.");
		}
		if (!StringUtils.isBlank(message.getObjectType()) && message.getObjectId() <= 0) {
			errMsg.append("the message object id should be positive when the object type is present.");
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

}