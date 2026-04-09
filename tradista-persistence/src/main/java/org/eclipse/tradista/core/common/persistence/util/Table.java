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

public final class Table {

	private final String name;

	private final String id;

	private final Field[] fields;

	public Table(String name, String id, Field[] fields) {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(name)) {
			errMsg.append(String.format("the table name is mandatory.%n"));
		}
		if (StringUtils.isBlank(id)) {
			errMsg.append("the table id is mandatory.");
		}
		// When #Table(String, String) will be removed, a check on fields should be
		// added here.
		if (!errMsg.isEmpty()) {
			throw new IllegalArgumentException(errMsg.toString());
		}
		if (fields != null) {
			for (Field f : fields) {
				f.setTable(this);
			}
		}
		this.name = name;
		this.id = id;
		this.fields = fields;
	}

	/**
	 * @deprecated Use {@link #Table(String, String, Field[])} instead
	 * @param name  the table name
	 * @param table the table id
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public Table(String name, String id) {
		this(name, id, null);
	}

	public Field[] getFields() {
		return fields.clone();
	}

	public String getId() {
		return id;
	}

	/**
	 * @deprecated use {@link #getId()} instead
	 * @return the table id field
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public String id() {
		return id;
	}

	/**
	 * @deprecated use {@link #getName()} instead
	 * @return the table name
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public String name() {
		return name;
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
		Table other = (Table) obj;
		return Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return name;
	}
}