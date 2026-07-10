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
package org.eclipse.tradista.core.rating.model;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

public class RatingAgency extends TradistaObject implements Comparable<RatingAgency> {

	private static final long serialVersionUID = 1L;

	@Id
	private String name;

	private boolean active = true;

	public RatingAgency(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(RatingAgency agency) {
		return name.compareTo(agency.getName());
	}
}