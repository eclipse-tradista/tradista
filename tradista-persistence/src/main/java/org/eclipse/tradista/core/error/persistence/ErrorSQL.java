package org.eclipse.tradista.core.error.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.AND;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.STATUS;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TYPE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.WHERE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.YYYY_MM_DD_HH_MM_SS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.error.model.Error.Status;

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

public class ErrorSQL {

	public static final Table ERROR_TABLE = new Table("ERROR", ID);

	public static final Field ID_FIELD = new Field(ID, ERROR_TABLE);
	public static final Field TYPE_FIELD = new Field(TYPE, ERROR_TABLE);
	public static final Field STATUS_FIELD = new Field(STATUS, ERROR_TABLE);
	public static final Field MESSAGE_FIELD = new Field("MESSAGE", ERROR_TABLE);
	public static final Field ERROR_DATE_FIELD = new Field("ERROR_DATE", ERROR_TABLE);
	public static final Field SOLVING_DATE_FIELD = new Field("SOLVING_DATE", ERROR_TABLE);

	public static void deleteErrors(String errorType, Status status, LocalDate errorDateFrom, LocalDate errorDateTo) {
		Set<String> errorClassNames = TradistaUtil.getAllErrorClassNames();
		Set<Long> ids = getErrorIds(errorType, status, errorDateFrom, errorDateTo);
		try {
			if (ids != null && !ids.isEmpty()) {
				for (String err : errorClassNames) {
					Class<?> persistenceClass = null;
					List<Class<?>> klasses = TradistaUtil.getAllClassesByRegex("[^*]+.persistence." + err + "SQL",
							"org.eclipse.tradista.**");
					if (klasses != null && !klasses.isEmpty()) {
						persistenceClass = klasses.get(0);
						TradistaUtil.callMethod(persistenceClass.getName(), Void.class, "deleteErrors", ids);
					} else {
						throw new TradistaTechnicalException(
								String.format("the persistence class has not been found for this error type: %s", err));
					}
				}
			}
		} catch (TradistaBusinessException tbe) {
			throw new TradistaTechnicalException(tbe);
		}
		ErrorSQL.deleteErrors(ids);
	}

	public static void deleteErrors(Set<Long> ids) {
		if (ids != null && !ids.isEmpty()) {
			try (Connection con = TradistaDB.getConnection();
					PreparedStatement stmtDeleteErrors = con.prepareStatement("DELETE FROM ERROR WHERE ID = ?")) {
				for (long id : ids) {
					stmtDeleteErrors.setLong(1, id);
					stmtDeleteErrors.addBatch();
				}
				stmtDeleteErrors.executeBatch();
			} catch (SQLException sqle) {
				// TODO Manage logs
				sqle.printStackTrace();
				throw new TradistaTechnicalException(sqle);
			}
		}
	}

	public static Set<Long> getErrorIds(String errorType, Status status, LocalDate errorDateFrom,
			LocalDate errorDateTo) {
		Set<Long> ids = null;
		StringBuilder sqlQuery = new StringBuilder("SELECT ID FROM ERROR");
		if (!StringUtils.isEmpty(errorType)) {
			sqlQuery.append(" WHERE TYPE = '" + errorType + "'");
		}
		if (status != null) {
			if (sqlQuery.toString().contains(WHERE)) {
				sqlQuery.append(AND);
			} else {
				sqlQuery.append(WHERE);
			}
			sqlQuery.append(" STATUS = '" + status.name() + "'");
		}
		if (errorDateFrom != null) {
			if (sqlQuery.toString().contains(WHERE)) {
				sqlQuery.append(AND);
			} else {
				sqlQuery.append(WHERE);
			}
			sqlQuery.append(" ERROR_DATE >= '"
					+ DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS).format(errorDateFrom.atStartOfDay()) + "'");
		}
		if (errorDateTo != null) {
			if (sqlQuery.toString().contains(WHERE)) {
				sqlQuery.append(AND);
			} else {
				sqlQuery.append(WHERE);
			}
			sqlQuery.append(" ERROR_DATE < '"
					+ DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS).format(errorDateTo.plusDays(1).atStartOfDay())
					+ "'");
		}
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetErrorIds = con.createStatement();
				ResultSet results = stmtGetErrorIds.executeQuery(sqlQuery.toString())) {
			while (results.next()) {
				if (ids == null) {
					ids = new HashSet<>();
				}
				ids.add(results.getLong("id"));
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return ids;
	}

}