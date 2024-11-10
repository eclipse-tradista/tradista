package org.eclipse.tradista.core.workflow.model;

import java.util.Objects;
import java.util.Set;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;

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

public class Workflow extends TradistaObject {

	private static final long serialVersionUID = 1L;

	private String name;

	private String description;

	private Set<Action> actions;

	private Set<Status> status;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@SuppressWarnings("unchecked")
	public Set<Action> getActions() {
		return (Set<Action>) TradistaModelUtil.deepCopy(actions);
	}

	public void setActions(Set<Action> actions) {
		this.actions = actions;
	}

	@SuppressWarnings("unchecked")
	public Set<Status> getStatus() {
		return (Set<Status>) TradistaModelUtil.deepCopy(status);
	}

	public void setStatus(Set<Status> status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Workflow other = (Workflow) obj;
		return Objects.equals(name, other.name);
	}

}