package org.eclipse.tradista.core.message.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.AND;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.IN;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.FROM;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.SELECT;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.STATUS;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TYPE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.WHERE;
import static org.eclipse.tradista.core.error.persistence.ErrorSQL.ERROR_DATE_FIELD;
import static org.eclipse.tradista.core.error.persistence.ErrorSQL.ERROR_TABLE;
import static org.eclipse.tradista.core.error.persistence.ErrorSQL.ID_FIELD;
import static org.eclipse.tradista.core.error.persistence.ErrorSQL.MESSAGE_FIELD;
import static org.eclipse.tradista.core.error.persistence.ErrorSQL.SOLVING_DATE_FIELD;
import static org.eclipse.tradista.core.error.persistence.ErrorSQL.STATUS_FIELD;
import static org.eclipse.tradista.core.error.persistence.ErrorSQL.TYPE_FIELD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.message.model.ImportError;
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

public class ImportErrorSQL {

	public static final String ERROR_ID = "ERROR_ID";

	private static final Table IMPORT_ERROR_TABLE = new Table("IMPORT_ERROR", ID);

	private static final Field MESSAGE_ID_FIELD = new Field("MESSAGE_ID", IMPORT_ERROR_TABLE);

	private static final Field[] FIELDS = { ID_FIELD, TYPE_FIELD, STATUS_FIELD, MESSAGE_FIELD, ERROR_DATE_FIELD,
			SOLVING_DATE_FIELD, MESSAGE_ID_FIELD };

	private static final String SELECT_QUERY = TradistaDBUtil.buildSelectQuery(FIELDS, IMPORT_ERROR_TABLE, ERROR_TABLE);

	public static long saveImportError(ImportError error) {
		long errorId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveError = con.prepareStatement(
						"INSERT INTO ERROR(TYPE, MESSAGE, STATUS, ERROR_DATE) VALUES(?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtSaveImportError = con.prepareStatement("INSERT INTO IMPORT_ERROR VALUES(?, ?)");
				PreparedStatement stmtUpdateError = con
						.prepareStatement("UPDATE ERROR SET TYPE=?, MESSAGE=?, STATUS=?, ERROR_DATE=? WHERE ID=?");
				PreparedStatement stmtUpdateImportError = con
						.prepareStatement("UPDATE IMPORT_ERROR SET MESSAGE_ID = ? WHERE ERROR_ID=?")) {
			if (error.getId() == 0) {
				stmtSaveError.setString(1, error.getType());
				stmtSaveError.setString(2, error.getErrorMessage());
				stmtSaveError.setString(3, error.getStatus().name());
				stmtSaveError.setTimestamp(4, java.sql.Timestamp.valueOf(error.getErrorDate()));
				stmtSaveError.executeUpdate();
				if (error.getId() == 0) {
					try (ResultSet generatedKeys = stmtSaveError.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							errorId = generatedKeys.getLong(1);
						} else {
							throw new SQLException("Creating error failed, no generated key obtained.");
						}
					}
				} else {
					errorId = error.getId();
				}
				stmtSaveImportError.setLong(1, error.getId());
				stmtSaveImportError.setLong(2, error.getMessage().getId());
				stmtSaveImportError.executeUpdate();
			} else {
				stmtUpdateImportError.setLong(1, error.getMessage().getId());
				stmtUpdateImportError.setLong(2, error.getId());
				stmtUpdateImportError.executeUpdate();

				stmtUpdateError.setString(1, error.getType());
				stmtUpdateError.setString(2, error.getErrorMessage());
				stmtUpdateError.setString(3, error.getStatus().name());
				stmtUpdateError.setTimestamp(4, java.sql.Timestamp.valueOf(error.getErrorDate()));
				stmtUpdateError.setLong(5, error.getId());
				stmtUpdateError.executeUpdate();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		error.setId(errorId);
		return errorId;
	}

	public static List<ImportError> getImportErrors(Set<String> importerTypes, Set<String> importerNames,
			long messageId, Status status, LocalDate errorDateFrom, LocalDate errorDateTo, LocalDate solvingDateFrom,
			LocalDate solvingDateTo) {
		List<ImportError> importErrors = null;

		try (Connection con = TradistaDB.getConnection(); Statement stmtGetImportErrors = con.createStatement()) {
			StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);

			String importerTypesSqlQuery = StringUtils.EMPTY;
			if (!CollectionUtils.isEmpty(importerTypes)) {
				String importerTypesSql = StringUtils.join(importerTypes, "','");
				if (sqlQuery.indexOf(WHERE) != -1) {
					importerTypesSqlQuery = AND;
				} else {
					importerTypesSqlQuery = WHERE;
				}
				importerTypesSqlQuery += MESSAGE_ID_FIELD + IN + " (" + SELECT + ID + FROM + MessageSQL.MESSAGE_TABLE
						+ WHERE + ID + " = " + MESSAGE_ID_FIELD + AND + TYPE + IN + " ('" + importerTypesSql + "'))";
			}

			sqlQuery.append(importerTypesSqlQuery);

			String importerNamesSqlQuery = StringUtils.EMPTY;
			if (!CollectionUtils.isEmpty(importerNames)) {
				String importerNamesSql = StringUtils.join(importerNames, "','");
				if (sqlQuery.indexOf(WHERE) != -1) {
					importerNamesSqlQuery = AND;
				} else {
					importerNamesSqlQuery = WHERE;
				}
				importerNamesSqlQuery += MESSAGE_ID_FIELD + IN + " (" + SELECT + ID + FROM + MessageSQL.MESSAGE_TABLE
						+ WHERE + ID + " = " + MESSAGE_ID_FIELD + AND + MessageSQL.INTERFACE_NAME_FIELD + IN + " ('"
						+ importerNamesSql + "'))";
			}

			sqlQuery.append(importerNamesSqlQuery);

			sqlQuery = TradistaDBUtil.addFilter(sqlQuery, ERROR_DATE_FIELD, errorDateFrom, true);
			sqlQuery = TradistaDBUtil.addFilter(sqlQuery, ERROR_DATE_FIELD, errorDateTo, false);
			sqlQuery = TradistaDBUtil.addFilter(sqlQuery, SOLVING_DATE_FIELD, solvingDateFrom, true);
			sqlQuery = TradistaDBUtil.addFilter(sqlQuery, SOLVING_DATE_FIELD, solvingDateTo, false);

			String messageSqlQuery = StringUtils.EMPTY;

			if (messageId > 0) {
				if (sqlQuery.indexOf(WHERE) != -1) {
					messageSqlQuery = AND;
				} else {
					messageSqlQuery = WHERE;
				}
				messageSqlQuery += MESSAGE_ID_FIELD + IN + " (" + SELECT + ID + FROM + MessageSQL.MESSAGE_TABLE + WHERE
						+ ID + " = " + messageId + ")";
			}

			sqlQuery.append(messageSqlQuery);

			sqlQuery = TradistaDBUtil.addFilter(sqlQuery, STATUS_FIELD, status);

			try (ResultSet results = stmtGetImportErrors.executeQuery(sqlQuery.toString())) {
				while (results.next()) {
					if (importErrors == null) {
						importErrors = new ArrayList<>();
					}
					ImportError importError = new ImportError();
					importError.setId(results.getLong(ID));
					importError.setErrorMessage(results.getString(MESSAGE_FIELD.name()));
					Timestamp solvingDate = results.getTimestamp(SOLVING_DATE_FIELD.name());
					if (solvingDate != null) {
						importError.setSolvingDate(solvingDate.toLocalDateTime());
					}
					importError.setErrorDate(results.getTimestamp(ERROR_DATE_FIELD.name()).toLocalDateTime());
					importError.setStatus(Status.valueOf(results.getString(STATUS)));
					importError.setMessage(MessageSQL.getMessageById(results.getLong(MESSAGE_ID_FIELD.name())));
					importErrors.add(importError);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle.getMessage());
		}

		return importErrors;
	}

}