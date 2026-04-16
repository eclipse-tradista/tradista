package org.eclipse.tradista.core.common.persistence.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;

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

	private final Expression[] expressions;

	private final JoinType type;

	private Table targetTable;

	private BinaryCondition condition;

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

	public Join(JoinType type, Table targetTable, BinaryCondition condition) {
		StringBuilder errMsg = new StringBuilder();
		if (targetTable == null) {
			errMsg.append(String.format("The target table is mandatory.%n"));
		}
		if (condition == null) {
			errMsg.append("The join condition is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}

		this.type = (type == null) ? JoinType.INNER_JOIN : type;
		this.targetTable = targetTable;
		this.condition = condition;

		// We maintain the compatibility with the old API
		this.expressions = new Expression[] { condition.getLeft(), condition.getRight() };
	}

	/**
	 * @deprecated Use {@link #Join(JoinType,Table,BinaryCondition},
	 *             {@link #leftOuter(Expression...)} or
	 *             {@link #inner(Expression...)} instead
	 * @param fields the joined fields
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public Join(JoinType type, Expression... expressions) {
		StringBuilder errMsg = new StringBuilder();
		if (ArrayUtils.isEmpty(expressions)) {
			errMsg.append("expressions are mandatory.");
		}
		if (type == null) {
			type = JoinType.INNER_JOIN;
		}
		if (!errMsg.isEmpty()) {
			throw new IllegalArgumentException(errMsg.toString());
		}
		this.type = type;
		this.expressions = expressions;
	}

	/**
	 * @deprecated Use {@link #Join(JoinType,Table,BinaryCondition},
	 *             {@link #leftOuter(Expression...)} or
	 *             {@link #inner(Expression...)} instead
	 * @param fields the joined fields
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public Join(Expression... expressions) {
		StringBuilder errMsg = new StringBuilder();
		if (ArrayUtils.isEmpty(expressions)) {
			errMsg.append("expressions are mandatory.");
		}
		type = JoinType.INNER_JOIN;
		if (!errMsg.isEmpty()) {
			throw new IllegalArgumentException(errMsg.toString());
		}
		this.expressions = expressions;
	}

	/**
	 * @deprecated Use {@link #Join(JoinType, Expression...)},
	 *             {@link #leftOuter(Expression...)} or
	 *             {@link #inner(Expression...)} instead
	 * @param fields the joined fields
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public Join(Field[] fields) {
		this(null, fields);
	}

	public String getJoinField(Table table) {
		Optional<Expression> expression = Arrays.stream(expressions)
				.filter(e -> e instanceof Field f && f.getTable().equals(table)).findFirst();
		String fieldName = null;
		if (expression.isPresent()) {
			fieldName = ((Field) expression.get()).getFullName();
		}
		return fieldName;
	}

	public static Join inner(Table table, BinaryCondition condition) {
		return new Join(JoinType.INNER_JOIN, table, condition);
	}

	public static Join innerEq(Table table, Expression left, Expression right) {
		return new Join(JoinType.INNER_JOIN, table, left.eq(right));
	}

	public static Join leftOuter(Table table, BinaryCondition condition) {
		return new Join(JoinType.LEFT_OUTER_JOIN, table, condition);
	}

	public static Join leftOuterEq(Table table, Expression left, Expression right) {
		return new Join(JoinType.LEFT_OUTER_JOIN, table, left.eq(right));
	}

	public Expression[] getExpressions() {
		return (expressions != null) ? expressions.clone() : null;
	}

	/**
	 * @deprecated use {@link #getFields()} instead
	 * @return fields the joined fields
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public Field[] fields() {
		return getFields();
	}

	public Field[] getFields() {
		if (expressions == null) {
			return null;
		}
		return Arrays.stream(expressions).filter(Field.class::isInstance).map(Field.class::cast).toArray(Field[]::new);
	}

	public Field[] getAllFields() {
		Set<Field> uniqueFields = new LinkedHashSet<>();
		for (Field f : getFields()) {
			uniqueFields.addAll(new HashSet<>(Arrays.asList(f.getTable().getFields())));
		}
		return uniqueFields.toArray(new Field[uniqueFields.size()]);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(expressions);
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
		return Arrays.equals(expressions, other.expressions) && type == other.type;
	}

	@Override
	public String toString() {

		if (targetTable != null && condition != null) {
			return String.format(" %s %s ON %s", type, targetTable, condition.getRepresentation());
		}
		if (expressions != null && expressions.length > 1) {
			StringBuilder joinClause = new StringBuilder();
			for (int i = 1; i < expressions.length; i++) {
				Expression currentExpression = expressions[i];
				Expression previousExpression = expressions[i - 1];
				// We assume the table can be determined from the previous expression, using
				// #getTable()
				joinClause.append(StringUtils.SPACE).append(type).append(StringUtils.SPACE)
						.append(previousExpression.getTable()).append(" ON ")
						.append(previousExpression.getRepresentation()).append(" = ")
						.append(currentExpression.getRepresentation());
			}
			return joinClause.toString();
		}
		return StringUtils.EMPTY;
	}
}