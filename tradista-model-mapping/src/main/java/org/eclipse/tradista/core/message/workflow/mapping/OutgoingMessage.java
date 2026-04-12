package org.eclipse.tradista.core.message.workflow.mapping;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;

import finance.tradista.flow.model.Workflow;

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

public class OutgoingMessage extends Message {

	public OutgoingMessage(Workflow<? extends OutgoingMessage> wkf) {
		super(wkf);
	}

	public void setOutgoingMessage(org.eclipse.tradista.core.message.model.OutgoingMessage outgoingMessage) {
		this.message = outgoingMessage;
	}

	@Override
	public org.eclipse.tradista.core.message.model.OutgoingMessage getOriginalMessage() {
		return (org.eclipse.tradista.core.message.model.OutgoingMessage) TradistaModelUtil.clone(message);
	}

}