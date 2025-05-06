package org.eclipse.tradista.core.message.workflow.mapping;

import finance.tradista.flow.model.Workflow;

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

public final class MessageMapper {

	private MessageMapper() {
	}

	@SuppressWarnings("unchecked")
	public static Message map(org.eclipse.tradista.core.message.model.Message message,
			Workflow<? extends Message> wkf) {
		Message messageResult = null;
		if (message instanceof org.eclipse.tradista.core.message.model.IncomingMessage incomingMessage) {
			messageResult = new IncomingMessage((Workflow<? extends IncomingMessage>) wkf);
			((IncomingMessage) messageResult).setIncomingMessage(incomingMessage);
		}
		return messageResult;
	}

}