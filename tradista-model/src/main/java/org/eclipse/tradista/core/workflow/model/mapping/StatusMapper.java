package org.eclipse.tradista.core.workflow.model.mapping;

import org.eclipse.tradista.core.workflow.model.PseudoStatus;
import org.eclipse.tradista.core.workflow.model.Status;

import finance.tradista.flow.model.Workflow;
import finance.tradista.flow.model.WorkflowObject;

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

public final class StatusMapper {

	private StatusMapper() {
	}

	public static Status map(finance.tradista.flow.model.Status<?> status) {
		Status statusResult = null;
		if (status != null) {
			if (status instanceof finance.tradista.flow.model.PseudoStatus) {
				statusResult = new PseudoStatus();
			} else {
				statusResult = new Status();
			}
			statusResult.setId(status.getId());
			statusResult.setName(status.getName());
			statusResult.setWorkflowName(status.getWorkflow().getName());
		}
		return statusResult;
	}

	public static <X extends WorkflowObject> finance.tradista.flow.model.Status<X> map(Status status,
			Workflow<X> workflow) {
		finance.tradista.flow.model.Status<X> statusResult = null;
		if (status != null) {
			if (status instanceof PseudoStatus) {
				statusResult = new finance.tradista.flow.model.PseudoStatus<>();
			} else {
				statusResult = new finance.tradista.flow.model.Status<>();
			}
			statusResult.setId(status.getId());
			statusResult.setName(status.getName());
			statusResult.setWorkflow(workflow);
		}
		return statusResult;
	}

}