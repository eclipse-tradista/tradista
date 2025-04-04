package org.eclipse.tradista.ai.agent.model;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public class Agent extends TradistaObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -242917493894323568L;

	@Id
	private String name;

	private boolean onlyInformative;

	private boolean started;

	public Agent(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isOnlyInformative() {
		return onlyInformative;
	}

	public void setOnlyInformative(boolean onlyInformative) {
		this.onlyInformative = onlyInformative;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

}