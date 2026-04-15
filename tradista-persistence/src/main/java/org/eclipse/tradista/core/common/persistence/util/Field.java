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

public final class Field implements Expression {

	private final String name;

	private Table table;

	private String alias;

	/**
	 * @param name  the field name
	 * @param alias optional, the field alias
	 */
	public Field(String name, String... alias) {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(name)) {
			errMsg.append(String.format("name cannot be blank.%n"));
		}
		if (!errMsg.isEmpty()) {
			throw new IllegalArgumentException(errMsg.toString());
		}
		this.name = name;
		if (alias.length > 0) {
			this.alias = alias[0];
		}
	}

	/**
	 * @deprecated use {@link Field(String, String...)} and {@link #setTable(Table)}
	 *             instead
	 * @param name  the field name
	 * @param table the field table
	 * @param alias optional, the field alias
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public Field(String name, Table table, String... alias) {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(name)) {
			errMsg.append(String.format("name cannot be blank.%n"));
		}
		if (!errMsg.isEmpty()) {
			throw new IllegalArgumentException(errMsg.toString());
		}
		this.name = name;
		if (alias.length > 0) {
			this.alias = alias[0];
		}
	}

	/**
	 * Gets the alias if set, otherwise returns the name
	 * 
	 * @return the alias if set, otherwise returns the name
	 */
	public String getNameOrAlias() {
		return alias != null ? alias : name;
	}

	/**
	 * Gets the full name, ie table name.field name
	 * 
	 * @return the full name, ie table name.field name
	 */
	public String getFullName() {
		return table + "." + name;
	}

	public String getAlias() {
		return alias;
	}

	public String getName() {
		return name;
	}

	@Override
	public Table getTable() {
		return table;
	}

	/**
	 * @deprecated use {@link #getAlias()} instead
	 * @return the field alias (may not exist)
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public String[] alias() {
		return new String[] { alias };
	}

	/**
	 * @deprecated use {@link #getName()} instead
	 * @return the field name
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public String name() {
		return name;
	}

	/**
	 * @deprecated use {@link #getTable()} instead
	 * @return the field table
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public Table table() {
		return table;
	}

	public void setTable(Table table) {
		if (this.table != null) {
			throw new IllegalStateException(String.format("The %s field has already a table (%s).", name, table));
		}
		this.table = table;
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
	public String getRepresentation() {
		return getFullName();
	}

	@Override
	public String toString() {
		return table + "." + name + StringUtils.SPACE + ((alias != null) ? alias : StringUtils.EMPTY);
	}

}