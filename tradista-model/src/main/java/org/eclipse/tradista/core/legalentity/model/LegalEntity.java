package org.eclipse.tradista.core.legalentity.model;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

/********************************************************************************
 * Copyright (c) 2014 Olivier Asuncion
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

public class LegalEntity extends TradistaObject implements Comparable<LegalEntity> {

	private static final long serialVersionUID = -2952827349654099841L;

	public enum Role {
		PROCESSING_ORG, COUNTERPARTY;

		@Override
		public String toString() {
			switch (this) {
			case PROCESSING_ORG:
				return "ProcessingOrg";
			case COUNTERPARTY:
				return "Counterparty";
			}
			return super.toString();
		}

		/**
		 * Gets a Role from a display name. Display names are used in GUIs. A display
		 * name of a Role is the result of its toString() method.
		 * 
		 * @param type
		 * @return
		 */
		public static Role getRole(String displayName) {
			switch (displayName) {
			case "ProcessingOrg":
				return PROCESSING_ORG;
			case "Counterparty":
				return COUNTERPARTY;
			}
			return null;
		}
	}

	@Id
	private String shortName;

	private String longName;

	private String description;

	private Role role;

	public LegalEntity(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return shortName;
	}

	@Override
	public int compareTo(LegalEntity le) {
		return shortName.compareTo(le.getShortName());
	}

}