package org.eclipse.tradista.core.common.persistence.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
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

public final class Join {

	private final Field[] fields;

	private final JoinType type;

	public enum JoinType {
		INNER_JOIN, LEFT_OUTER_JOIN;

		@Override
		public String toString() {
			return switch (this) {
			case INNER_JOIN -> "INNER JOIN";
			case LEFT_OUTER_JOIN -> "LEFT OUTER JOIN";
			default -> super.toString();
			};
		}
	}

	public Join(JoinType type, Field... fields) {
		StringBuilder errMsg = new StringBuilder();
		if (ArrayUtils.isEmpty(fields)) {
			errMsg.append("fields are mandatory.");
		}
		if (type == null) {
			type = JoinType.INNER_JOIN;
		}
		if (!errMsg.isEmpty()) {
			throw new IllegalArgumentException(errMsg.toString());
		}
		this.type = type;
		this.fields = fields;
	}

	/**
	 * @deprecated Use {@link #Join(JoinType, Field...)},
	 *             {@link #leftOuter(Field...)} or {@link #inner(Field...)} instead
	 * @param fields the joined fields
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public Join(Field[] fields) {
		this(null, fields);
	}

	public String getJoinField(Table table) {
		Optional<Field> field = Arrays.stream(fields).filter(f -> f.getTable().equals(table)).findFirst();
		String fieldName = null;
		if (field.isPresent()) {
			fieldName = field.get().getFullName();
		}
		return fieldName;
	}

	public static Join inner(Field... fields) {
		return new Join(JoinType.INNER_JOIN, fields);
	}

	public static Join leftOuter(Field... fields) {
		return new Join(JoinType.LEFT_OUTER_JOIN, fields);
	}

	public Field[] getFields() {
		return fields.clone();
	}

	/**
	 * @deprecated use {@link #getFields()} instead
	 * @return fields the joined fields
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public Field[] fields() {
		return fields.clone();
	}

	public Field[] getAllFields() {
		Set<Field> uniqueFields = new LinkedHashSet<>();
		for (Field f : fields) {
			uniqueFields.addAll(new HashSet<>(Arrays.asList(f.getTable().getFields())));
		}
		return uniqueFields.toArray(new Field[uniqueFields.size()]);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(fields);
		result = prime * result + Objects.hash(type);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Join other = (Join) obj;
		return Arrays.equals(fields, other.fields) && type == other.type;
	}

	@Override
	public String toString() {
		StringBuilder joinClause = new StringBuilder();
		for (int i = 1; i < fields.length; i++) {
			Field currentField = fields[i];
			Field previousField = fields[i - 1];
			joinClause.append(StringUtils.SPACE).append(type).append(StringUtils.SPACE).append(previousField.getTable())
					.append(" ON ").append(previousField.getFullName()).append(" = ")
					.append(currentField.getFullName());
		}
		return joinClause.toString();
	}
}