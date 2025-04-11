package org.eclipse.tradista.core.message.service;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.messsage.service.MessageService;

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

public class MessageBusinessDelegate {

	private MessageService messageService;

	public MessageBusinessDelegate() {
		messageService = TradistaServiceLocator.getInstance().getMessageService();
	}

	public long saveMessage(Message message) throws TradistaBusinessException {
		if (message == null) {
			throw new TradistaBusinessException("the message cannot be null.");
		}
		if (StringUtils.isEmpty(message.getType())) {
			throw new TradistaBusinessException("the message type is mandatory.");
		}
		return messageService.saveMessage(message);
	}

}