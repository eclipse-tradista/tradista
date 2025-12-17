package org.eclipse.tradista.core.common.persistence.util;

import org.apache.commons.lang3.StringUtils;

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

public record Table(String name, String id) {
	public Table {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(name)) {
			errMsg.append(String.format("name cannot be blank.%n"));
		}
		if (StringUtils.isBlank(id)) {
			errMsg.append(String.format("id cannot be blank.%n"));
		}
		if (!errMsg.isEmpty()) {
			throw new IllegalArgumentException(errMsg.toString());
		}
	}

	@Override
	public String toString() {
		return name();
	}
}