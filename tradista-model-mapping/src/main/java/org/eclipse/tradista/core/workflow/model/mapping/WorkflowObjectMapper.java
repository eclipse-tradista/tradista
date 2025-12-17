package org.eclipse.tradista.core.workflow.model.mapping;

import org.eclipse.tradista.core.workflow.model.Status;
import org.eclipse.tradista.core.workflow.model.WorkflowObject;

import finance.tradista.flow.model.Workflow;

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

public final class WorkflowObjectMapper {

	private WorkflowObjectMapper() {
	}

	public static finance.tradista.flow.model.WorkflowObject map(WorkflowObject wo, Workflow<?> wkf) {
		return new finance.tradista.flow.model.WorkflowObject() {

			private finance.tradista.flow.model.Status<?> status = StatusMapper.map(wo.getStatus(), wkf);

			@SuppressWarnings("unchecked")
			@Override
			public finance.tradista.flow.model.Status<?> getStatus() {
				return status;
			}

			@Override
			public String getWorkflow() {
				return wo.getWorkflow();
			}

			@Override
			public void setStatus(finance.tradista.flow.model.Status<?> status) {
				this.status = status;
			}

			@Override
			public finance.tradista.flow.model.WorkflowObject clone() throws java.lang.CloneNotSupportedException {
				return (finance.tradista.flow.model.WorkflowObject) super.clone();
			}

		};
	}

	public static WorkflowObject map(finance.tradista.flow.model.WorkflowObject wo) {
		return new WorkflowObject() {

			private Status status = StatusMapper.map(wo.getStatus());

			@Override
			public Status getStatus() {
				return status;
			}

			@Override
			public String getWorkflow() {
				return wo.getWorkflow();
			}

			@Override
			public void setStatus(Status status) {
				this.status = status;
			}

		};
	}

}