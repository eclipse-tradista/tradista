package org.eclipse.tradista.core.message.workflow.mapping;

import org.eclipse.tradista.core.workflow.model.mapping.StatusMapper;

import finance.tradista.flow.model.Status;
import finance.tradista.flow.model.Workflow;
import finance.tradista.flow.model.WorkflowObject;

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

public abstract class Message implements WorkflowObject {

	protected org.eclipse.tradista.core.message.model.Message message;

	private Workflow<? extends Message> wkf;

	protected Message(Workflow<? extends Message> wkf) {
		this.wkf = wkf;
	}

	@Override
	public Status getStatus() {
		Status status = null;
		if (message != null) {
			status = StatusMapper.map(message.getStatus(), wkf);
		}
		return status;
	}

	@Override
	public String getWorkflow() {
		String wkf = null;
		if (message != null) {
			wkf = message.getWorkflow();
		}
		return wkf;
	}

	@Override
	public void setStatus(Status status) {
		if (message != null) {
			message.setStatus(StatusMapper.map(status));
		}
	}

	@Override
	public finance.tradista.flow.model.WorkflowObject clone() throws java.lang.CloneNotSupportedException {
		Message message = (Message) super.clone();
		if (this.message != null) {
			message.message = (org.eclipse.tradista.core.message.model.Message) this.message.clone();
		}
		if (this.wkf != null) {
			message.wkf = this.wkf.clone();
		}
		return message;
	}
}