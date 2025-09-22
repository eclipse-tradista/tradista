package org.eclipse.tradista.core.common.persistence.util;

import java.util.Objects;

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

public record Field(String name, Table table, String... alias) {
	public Field {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(name)) {
			errMsg.append(String.format("name cannot be blank.%n"));
		}
		if (table == null) {
			errMsg.append("table is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new IllegalArgumentException(errMsg.toString());
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, table);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Field other = (Field) obj;
		return Objects.equals(name, other.name) && Objects.equals(table, other.table);
	}

	@Override
	public String toString() {
		return table().name() + "." + name() + StringUtils.SPACE
				+ ((alias().length > 1) ? alias()[0] : StringUtils.EMPTY);
	}
}