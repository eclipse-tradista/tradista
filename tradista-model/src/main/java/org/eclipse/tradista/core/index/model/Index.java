package org.eclipse.tradista.core.index.model;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

public class Index extends TradistaObject {

	private static final long serialVersionUID = -3890592152599924134L;

	public static final String INDEX = "Index";

	@Id
	private String name;

	private String description;

	private boolean isPrefixed;

	public static enum Fixing {
		PREFIXED, POSTFIXED;

		public String toString() {
			switch (this) {
			case PREFIXED:
				return "Prefixed";
			case POSTFIXED:
				return "PostFixed";
			}
			return super.toString();
		}
	}

	public Index(String name) {
		super();
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public boolean isPrefixed() {
		return isPrefixed;
	}

	public void setPrefixed(boolean isPrefixed) {
		this.isPrefixed = isPrefixed;
	}

	public String toString() {
		return name;
	}

}