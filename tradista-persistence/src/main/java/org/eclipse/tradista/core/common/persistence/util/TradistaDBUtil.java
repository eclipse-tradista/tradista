package org.eclipse.tradista.core.common.persistence.util;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.AND;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.FROM;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.IN;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MM_DD_YYYY;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.SELECT;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.WHERE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.YYYY_MM_DD_HH_MM_SS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

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

public final class TradistaDBUtil {

	private static final String THE_SQL_QUERY_IS_MANDATORY = "The SQL query is mandatory.%n";
	private static final String THE_EXPRESSION_IS_MANDATORY = "The expression is mandatory.";
	private static final String THE_FIELD_DOESNT_HAVE_THE_EXPECTED_TABLE = "The field %s doesn't have the expected table (%s instead of %s).%n";
	private static final Logger logger = LoggerFactory.getLogger(TradistaDBUtil.class);

	private TradistaDBUtil() {
	}

	/**
	 * Build a select SQL query, given fields, tables and optional join conditions.
	 * 
	 * @deprecated This method is replaced by a method using the Ansi SQL-92 join
	 *             logic. So, only one base table (used in the 'From' clause) is
	 *             needed. Use
	 *             {@link #buildSelectQuery(Expression[], Table, Join...)} for
	 *             queries with joins, or
	 *             {@link #buildSelectQuery(Expression[], Table)} for simple
	 *             single-table queries instead.
	 * 
	 * @param fields The fields to be retrieved
	 * @param tables The tables to be queried
	 * @param join   optional in case specific join conditions are needed (by
	 *               default a natural join is used)
	 * @throws TradistaTechnicalException if no field or no table is specified.
	 * @return A SQL query.
	 */
	@Deprecated(forRemoval = true, since = "3.2.0")
	public static String buildSelectQuery(Field[] fields, Table[] tables, Join... join) {
		StringBuilder errMsg = new StringBuilder();
		StringBuilder select = new StringBuilder(SELECT);
		if (ArrayUtils.isEmpty(fields)) {
			errMsg.append(String.format("The fields are mandatory.%n"));
		}
		if (tables == null) {
			errMsg.append("The tables cannot be null.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		select.append(String.join(",", Arrays.stream(fields).map(Object::toString).toList()));
		select.append(FROM + String.join(",", Arrays.stream(tables).map(Object::toString).toList()));
		if (tables.length > 1) {
			Table previousTable = null;
			String sqlKeyword = null;
			for (Table t : tables) {
				if (previousTable != null) {
					if (select.indexOf(WHERE) != -1) {
						sqlKeyword = AND;
					} else {
						sqlKeyword = WHERE;
					}
					String joinOne;
					String joinTwo;
					if (join.length == 0) {
						joinOne = previousTable.id();
						joinTwo = t.id();
					} else {
						joinOne = join[0].getJoinField(previousTable);
						joinTwo = join[0].getJoinField(t);
					}
					select.append(new StringBuilder(sqlKeyword).append(joinOne).append(" = ").append(joinTwo));
				}
				previousTable = t;
			}
		}
		return select.toString();
	}

	/**
	 * Builds a select SQL query, given expressions, a table and join conditions
	 * with other tables. If no fields are specified, all fields from the joins's
	 * table are queried. If neither field or joins are specified, all fields from
	 * the table are queried.
	 * 
	 * @param expressions optional, the expressions to be retrieved.
	 * @param table       The table to be queried
	 * @param join        optional, in case joins are needed
	 * @throws TradistaTechnicalException if no table is specified.
	 * @return A SQL query.
	 */
	public static String buildSelectQuery(Expression[] expressions, Table table, Join... joins) {
		StringBuilder errMsg = new StringBuilder();
		StringBuilder select = new StringBuilder(SELECT);
		if (table == null) {
			errMsg.append("The table cannot be null.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		Expression[] queriedExpressions = null;
		if (!ArrayUtils.isEmpty(expressions)) {
			queriedExpressions = expressions;
		}
		if (ArrayUtils.isEmpty(queriedExpressions)) {
			if (!ArrayUtils.isEmpty(joins)) {
				Set<Field> uniqueFields = new LinkedHashSet<>();
				for (Join j : joins) {
					uniqueFields.addAll(new HashSet<>(Arrays.asList(j.getAllFields())));
				}
				queriedExpressions = uniqueFields.toArray(new Field[uniqueFields.size()]);
			}
			if (ArrayUtils.isEmpty(queriedExpressions)) {
				queriedExpressions = table.getFields();
			}
		}
		select.append(String.join(",", Arrays.stream(queriedExpressions).map(Object::toString).toList()));
		select.append(FROM).append(table).append(StringUtils.SPACE);
		if (joins != null) {
			for (Join j : joins) {
				select.append(j);
			}
		}
		return select.toString();
	}

	/**
	 * Builds a select SQL query, given an expression, a table and join conditions
	 * with other tables. If no fields are specified, all fields from the joins's
	 * table are queried. If neither field or joins are specified, all fields from
	 * the table are queried.
	 * 
	 * @param expression optional, the expressions to be retrieved.
	 * @param table      The table to be queried
	 * @param join       optional, in case joins are needed
	 * @throws TradistaTechnicalException if no table is specified.
	 * @return A SQL query.
	 */
	public static String buildSelectQuery(Expression expression, Table table, Join... joins) {
		return buildSelectQuery(expression == null ? null : new Expression[] { expression }, table, joins);
	}

	/**
	 * Build a select SQL query, given a table and join conditions with other
	 * tables.
	 * 
	 * @param table The table to be queried
	 * @param join  optional in case inner join are needed
	 * @throws TradistaTechnicalException if no field or no table is specified.
	 * @return A SQL query.
	 */
	public static String buildSelectQuery(Table table, Join... joins) {
		return TradistaDBUtil.buildSelectQuery((Expression[]) null, table, joins);
	}

	/**
	 * Build a select SQL query, given fields and a table.
	 * 
	 * @param expressions optional, the expressions to be retrieved. If not set, all
	 *                    fields from the table are retrieved
	 * @param table       The table to be queried
	 * @throws TradistaTechnicalException if no table is specified.
	 * @return A SQL query.
	 */
	public static String buildSelectQuery(Expression[] expressions, Table table) {
		return TradistaDBUtil.buildSelectQuery(expressions, table, (Join[]) null);
	}

	/**
	 * Build a select SQL query, given fields and a table.
	 * 
	 * @param expressions optional, the expressions to be retrieved. If not set, all
	 *                    fields from the table are retrieved
	 * @param table       The table to be queried
	 * @throws TradistaTechnicalException if no table is specified.
	 * @return A SQL query.
	 */
	public static String buildSelectQuery(Expression expression, Table table) {
		return TradistaDBUtil.buildSelectQuery(expression == null ? null : new Expression[] { expression }, table);
	}

	/**
	 * Build a select SQL query, given a table. All fields will be queried
	 * 
	 * @param table The table to be queried
	 * @throws TradistaTechnicalException if no table is specified.
	 * @return A SQL query.
	 */
	public static String buildSelectQuery(Table table) {
		return TradistaDBUtil.buildSelectQuery((Expression[]) null, table, (Join[]) null);
	}

	/**
	 * Build a delete prepared statement, given a table and optional filters.
	 * 
	 * @param table   The table concerned by the deletions
	 * @param filters optional filters
	 * @throws TradistaTechnicalException if no table or an incorrect filter is
	 *                                    specified.
	 * @return A delete prepared statement
	 */
	public static PreparedStatement buildDeletePreparedStatement(Connection con, Table table, Expression... filters) {
		StringBuilder errMsg = new StringBuilder();
		if (table == null) {
			errMsg.append("The table cannot be null.");
		}
		if (!ArrayUtils.isEmpty(filters)) {
			for (Expression filter : filters) {
				if (filter != null && (filter instanceof Field || filter instanceof UnaryFunctionExpression)) {
					if (!filter.getTable().equals(table)) {
						errMsg.append(String.format(
								"The expression %s doesn't have the expected table (%s instead of %s).%n", filter,
								filter.getTable(), table));
					}
				}
			}
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}

		StringBuilder delete = new StringBuilder("DELETE " + FROM);
		delete.append(table);
		if (!ArrayUtils.isEmpty(filters)) {
			delete.append(WHERE);
			for (int i = 0; i < filters.length; i++) {
				Expression filter = filters[i];
				delete.append(filter.getRepresentation());
				delete.append("=?");
				if (i < filters.length - 1) {
					delete.append(AND);
				}
			}
		}

		logger.debug("Generated delete statement: {}", delete);

		try {
			return con.prepareStatement(delete.toString(), Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle.getMessage());
		}
	}

	/**
	 * Add a query filter to a SQL query (...IN (SELECT xxx FROM yyy))
	 * 
	 * @param sqlQuery    the sql query to enrich
	 * @param expression  the expression concerned by the filter
	 * @param queryFilter the query filter
	 * @param isExclusion true if the value should be excluded, false otherwise
	 */
	public static void addQueryFilter(StringBuilder sqlQuery, Expression expression, String queryFilter,
			boolean isExclusion) {
		if (StringUtils.isBlank(queryFilter)) {
			throw new TradistaTechnicalException("The query filter is mandatory.");
		}
		String filter = (isExclusion ? " NOT " : StringUtils.EMPTY) + IN + "(" + queryFilter + ")";
		addFreeTextFilter(sqlQuery, expression, filter);
	}

	/**
	 * Add a filter to a SQL query
	 * 
	 * @param isExclusion  true if the value should be excluded, false otherwise
	 * @param sqlQuery     the sql query to enrich
	 * @param expression   the expression concerned by the filter
	 * @param value        the filter value
	 * @param isLowerBound optional parameter, used to express ">=" (true) or "<="
	 *                     (false) (used for LocalDate and LocalDateTime values)
	 */
	public static void addFilter(boolean isExclusion, StringBuilder sqlQuery, Expression expression, Object value,
			boolean... isLowerBound) {
		StringBuilder errMsg = new StringBuilder();
		String filterSqlQuery = StringUtils.EMPTY;
		String eqOperator = isExclusion ? " <> " : " =";
		if (StringUtils.isBlank(sqlQuery)) {
			errMsg.append(String.format(THE_SQL_QUERY_IS_MANDATORY));
		}
		if (expression == null) {
			errMsg.append(THE_EXPRESSION_IS_MANDATORY);
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		if (value instanceof Long) {
			// long values that are not positive will be treated as null (ie no filter)
			if ((long) value <= 0) {
				value = null;
			}
		}
		if (value instanceof Collection) {
			if (CollectionUtils.isEmpty((Collection<?>) value)) {
				value = null;
			}
		}
		if (value != null) {
			if (sqlQuery.indexOf(WHERE) != -1) {
				filterSqlQuery = AND;
			} else {
				filterSqlQuery = WHERE;
			}
			String operator;
			if (isLowerBound.length > 0) {
				boolean inclusive = isLowerBound.length > 1 ? isLowerBound[1] : true;
				if (isLowerBound[0]) {
					operator = inclusive ? " >= " : " > ";
				} else {
					operator = inclusive ? " <= " : " < ";
				}
			} else {
				operator = eqOperator;
			}
			switch (value) {
			case Collection<?> c ->
				filterSqlQuery += expression.getRepresentation() + (isExclusion ? " NOT " : StringUtils.EMPTY) + IN
						+ "(" + wrapWithQuotes(String.join("','", c.toArray(String[]::new))) + ")";
			case LocalDate ld -> {
				String formattedValue = DateTimeFormatter.ofPattern(MM_DD_YYYY).format(ld);
				filterSqlQuery += expression.getRepresentation() + operator + wrapWithQuotes(formattedValue);
			}
			case LocalDateTime ldt -> {
				String formattedValue = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS).format(ldt);
				filterSqlQuery += expression.getRepresentation() + operator + wrapWithQuotes(formattedValue);
			}
			case Enum<?> e -> filterSqlQuery += expression.getRepresentation() + eqOperator + wrapWithQuotes(e.name());
			case Number n -> filterSqlQuery += expression.getRepresentation() + operator + n;
			default -> filterSqlQuery += expression.getRepresentation() + eqOperator + wrapWithQuotes(value.toString());
			}
		}
		sqlQuery.append(filterSqlQuery);
	}

	/**
	 * Add a "IS NULL" filter to a SQL query
	 * 
	 * @param sqlQuery   the sql query to enrich
	 * @param expression the expression concerned by the filter
	 */
	public static void addIsNullFilter(StringBuilder sqlQuery, Expression expression) {
		addFreeTextFilter(sqlQuery, expression, "IS NULL");
	}

	/**
	 * Add a "IS NOT NULL" filter to a SQL query
	 * 
	 * @param sqlQuery   the sql query to enrich
	 * @param expression the expression concerned by the filter
	 */
	public static void addIsNotNullFilter(StringBuilder sqlQuery, Expression expression) {
		addFreeTextFilter(sqlQuery, expression, "IS NOT NULL");
	}

	/**
	 * Internal utility method to add a filter on a field with a free text
	 * 
	 * @param sqlQuery   the sql query to enrich
	 * @param expression the field concerned by the filter
	 * @param freeText   the SQL fragment to add
	 */
	private static void addFreeTextFilter(StringBuilder sqlQuery, Expression expression, String freeText) {
		StringBuilder errMsg = new StringBuilder();
		String filterSqlQuery;
		if (StringUtils.isBlank(sqlQuery)) {
			errMsg.append(String.format(THE_SQL_QUERY_IS_MANDATORY));
		}
		if (expression == null) {
			errMsg.append(THE_EXPRESSION_IS_MANDATORY);
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		if (sqlQuery.indexOf(WHERE) != -1) {
			filterSqlQuery = AND;
		} else {
			filterSqlQuery = WHERE;
		}
		filterSqlQuery += expression.getRepresentation() + StringUtils.SPACE + freeText;

		sqlQuery.append(filterSqlQuery);
	}

	/**
	 * Add a filter to a SQL query
	 * 
	 * @param sqlQuery     the sql query to enrich
	 * @param expression   the expression concerned by the filter
	 * @param value        the filter value
	 * @param isLowerBound optional parameter, used to express ">=" (true) or "<="
	 *                     (false) (used for LocalDate and LocalDateTime values)
	 */
	public static void addFilter(StringBuilder sqlQuery, Expression expression, Object value, boolean... isLowerBound) {
		// By Default, we do not exclude
		addFilter(false, sqlQuery, expression, value, isLowerBound);
	}

	public static void addParameterizedFilter(StringBuilder sqlQuery, Expression expression, boolean... isLowerBound) {
		String operator;
		if (isLowerBound.length > 0) {
			boolean inclusive = isLowerBound.length > 1 ? isLowerBound[1] : true;
			if (isLowerBound[0]) {
				operator = inclusive ? " >= " : " > ";
			} else {
				operator = inclusive ? " <= " : " < ";
			}
		} else {
			operator = " = ";
		}
		addFreeTextFilter(sqlQuery, expression, operator + "?");
	}

	public static String wrapWithQuotes(String value) {
		if (value == null) {
			throw new TradistaTechnicalException("Cannot wrap with quotes a null value.");
		}
		return StringUtils.wrap(value, '\'');
	}

	public static PreparedStatement buildInsertPreparedStatement(Connection con, Table table, Field... fields) {
		StringBuilder errorMessage = new StringBuilder();
		if (con == null) {
			errorMessage.append(String.format("The connection is mandatory.%n"));
		}

		if (table == null) {
			errorMessage.append(String.format("The table is mandatory.%n"));
		}

		if (ArrayUtils.isEmpty(fields)) {
			errorMessage.append("The fields are mandatory.");
		} else {
			if (table != null) {
				for (Field field : fields) {
					if (!field.getTable().equals(table)) {
						errorMessage.append(String.format(THE_FIELD_DOESNT_HAVE_THE_EXPECTED_TABLE, field,
								field.getTable(), table));
					}
				}
			}
		}
		if (!errorMessage.isEmpty()) {
			throw new TradistaTechnicalException(errorMessage.toString());
		}
		StringBuilder insertSQL = new StringBuilder();
		insertSQL.append("INSERT INTO ");
		insertSQL.append(table);
		insertSQL.append(" (");
		insertSQL.append(String.join(",", Arrays.stream(fields).map(Field::getName).toArray(String[]::new)));
		insertSQL.append(") VALUES (");
		String[] questionmarksArray = new String[fields.length];
		Arrays.fill(questionmarksArray, "?");
		insertSQL.append(String.join(",", questionmarksArray));
		insertSQL.append(")");

		logger.debug("Generated insert statement: {}", insertSQL);

		try {
			return con.prepareStatement(insertSQL.toString(), Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle.getMessage());
		}
	}

	public static PreparedStatement buildUpdatePreparedStatement(Connection con, Field filter, Table table,
			Field... fields) {
		StringBuilder errorMessage = new StringBuilder();
		if (con == null) {
			errorMessage.append(String.format("The connection is mandatory.%n"));
		}

		if (table == null) {
			errorMessage.append(String.format("The table is mandatory.%n"));
		}

		if (ArrayUtils.isEmpty(fields)) {
			errorMessage.append("The fields are mandatory.");
		} else {
			if (table != null) {
				for (Field field : fields) {
					if (!field.getTable().equals(table)) {
						errorMessage.append(String.format(THE_FIELD_DOESNT_HAVE_THE_EXPECTED_TABLE, field,
								field.getTable(), table));
					}
				}
			}
		}
		if (filter != null) {
			if (!filter.getTable().equals(table)) {
				errorMessage.append(
						String.format(THE_FIELD_DOESNT_HAVE_THE_EXPECTED_TABLE, filter, filter.getTable(), table));
			}
		}
		if (!errorMessage.isEmpty()) {
			throw new TradistaTechnicalException(errorMessage.toString());
		}
		StringBuilder updateSQL = new StringBuilder();
		updateSQL.append("UPDATE ");
		updateSQL.append(table);
		updateSQL.append(" SET ");
		updateSQL.append(String.join(",", Arrays.stream(fields).map(f -> f.getName() + "=?").toArray(String[]::new)));

		if (filter != null) {
			updateSQL.append(WHERE);
			updateSQL.append(filter.getFullName());
			updateSQL.append("=?");
		}

		logger.debug("Generated update statement: {}", updateSQL);

		try {
			return con.prepareStatement(updateSQL.toString(), Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle.getMessage());
		}
	}

}