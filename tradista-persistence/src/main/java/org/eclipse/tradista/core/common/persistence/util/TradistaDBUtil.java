package org.eclipse.tradista.core.common.persistence.util;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.AND;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.FROM;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MM_DD_YYYY;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.SELECT;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.WHERE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.YYYY_MM_DD_HH_MM_SS;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
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

	private TradistaDBUtil() {
	}

	public static String buildSelectQuery(Field[] fields, Table... tables) {
		StringBuilder errMsg = new StringBuilder();
		StringBuilder select = new StringBuilder(SELECT);
		if (ArrayUtils.isEmpty(fields)) {
			errMsg.append(String.format("The fields cannot be null or empty.%n"));
		}
		if (tables == null) {
			errMsg.append(String.format("The table cannot be null.%n"));
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		select.append(StringUtils.join(Arrays.stream(fields).map(f -> f.toString()).toList(), ","));
		select.append(FROM + StringUtils.join(Arrays.stream(tables).map(Table::name).toList(), ","));
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
					select.append(sqlKeyword).append(previousTable.name()).append(".").append(previousTable.id())
							.append(" = ").append(t.name()).append(".").append(t.id());
				}
				previousTable = t;
			}
		}
		return select.toString();
	}

	public static StringBuilder addFilter(StringBuilder sqlQuery, Field field, Object value, boolean... isLowerBound) {
		StringBuilder errMsg = new StringBuilder();
		String filterSqlQuery = StringUtils.EMPTY;
		if (StringUtils.isBlank(sqlQuery)) {
			errMsg.append(String.format("The SQL query cannot be null or empty.%n"));
		}
		if (field == null) {
			errMsg.append("The field is mandatory.%n");
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
			switch (value) {
			case Collection<?> c -> filterSqlQuery += field + " IN ('" + StringUtils.join(c, "','") + "')";
			case LocalDate ld -> {
				String operator;
				String formattedValue = DateTimeFormatter.ofPattern(MM_DD_YYYY).format(ld);
				if (isLowerBound != null) {
					if (isLowerBound[0]) {
						operator = " >= ";
					} else {
						operator = " <= ";
					}
				} else {
					operator = " = ";
				}
				filterSqlQuery += field + operator + "'" + formattedValue + "'";
			}
			case LocalDateTime ldt -> {
				String operator;
				String formattedValue = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS).format(ldt);
				if (isLowerBound != null) {
					if (isLowerBound[0]) {
						operator = " >= ";
					} else {
						operator = " <= ";
					}
				} else {
					operator = " = ";
				}
				filterSqlQuery += field + operator + "'" + formattedValue + "'";
			}
			case Enum<?> e -> filterSqlQuery += field + "=" + e.name();
			default -> filterSqlQuery += field + "=" + value.toString();
			}
		}
		return sqlQuery.append(filterSqlQuery);
	}

}