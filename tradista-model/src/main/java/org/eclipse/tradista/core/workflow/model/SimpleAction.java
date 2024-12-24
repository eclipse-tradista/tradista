package org.eclipse.tradista.core.workflow.model;

import java.util.Objects;
import java.util.Set;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;

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

public class SimpleAction extends Action {

	private static final long serialVersionUID = 1L;

	private Set<Process> processes;

	private Status arrivalStatus;

	@SuppressWarnings("unchecked")
	public Set<Process> getProcesses() {
		return (Set<Process>) TradistaModelUtil.deepCopy(processes);
	}

	public void setProcesses(Set<Process> processes) {
		this.processes = processes;
	}

	public Status getArrivalStatus() {
		return TradistaModelUtil.clone(arrivalStatus);
	}

	public void setArrivalStatus(Status arrivalStatus) {
		this.arrivalStatus = arrivalStatus;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SimpleAction clone() {
		SimpleAction action = (SimpleAction) super.clone();
		action.arrivalStatus = TradistaModelUtil.clone(arrivalStatus);
		action.processes = (Set<Process>) TradistaModelUtil.deepCopy(processes);
		return action;
	}

	@Override
	public int hashCode() {
		return Objects.hash(arrivalStatus, getDepartureStatus(), getName(), getWorkflowName());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleAction other = (SimpleAction) obj;
		return Objects.equals(arrivalStatus, other.arrivalStatus)
				&& Objects.equals(getDepartureStatus(), other.getDepartureStatus())
				&& Objects.equals(getName(), other.getName())
				&& Objects.equals(getWorkflowName(), other.getWorkflowName());
	}

}