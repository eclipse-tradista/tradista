package org.eclipse.tradista.core.message.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.AND;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.FROM;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.SELECT;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.STATUS;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TYPE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.WHERE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.YYYY_MM_DD_HH_MM_SS;
import static org.eclipse.tradista.core.error.persistence.ErrorSQL.ERROR_DATE;
import static org.eclipse.tradista.core.error.persistence.ErrorSQL.SOLVING_DATE;
import static org.eclipse.tradista.core.message.persistence.MessageSQL.INTERFACE_NAME;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.error.persistence.ErrorSQL;
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

	public static final String MESSAGE_ID = "MESSAGE_ID";
	public static final String ERROR_ID = "ERROR_ID";

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
			String sqlQuery = SELECT + ID + "," + TYPE + "," + STATUS + "," + ErrorSQL.MESSAGE + "," + ERROR_DATE + ","
					+ SOLVING_DATE + "," + MESSAGE_ID + FROM + " IMPORT_ERROR, ERROR ";
			String dateSqlQuery = StringUtils.EMPTY;

			String importerTypesSqlQuery = StringUtils.EMPTY;
			if (!CollectionUtils.isEmpty(importerTypes)) {
				String importerTypesSql = StringUtils.join(importerTypes, "','");
				if (sqlQuery.contains(WHERE)) {
					importerTypesSqlQuery = AND;
				} else {
					importerTypesSqlQuery = WHERE;
				}
				importerTypesSqlQuery += MESSAGE_ID + " IN (" + SELECT + ID + FROM + MessageSQL.MESSAGE + WHERE + ID
						+ " = " + MESSAGE_ID + AND + TYPE + " IN ('" + importerTypesSql + "'))";
			}

			sqlQuery += importerTypesSqlQuery;

			String importerNamesSqlQuery = StringUtils.EMPTY;
			if (!CollectionUtils.isEmpty(importerNames)) {
				String importerNamesSql = StringUtils.join(importerNames, "','");
				if (sqlQuery.contains(WHERE)) {
					importerNamesSqlQuery = AND;
				} else {
					importerNamesSqlQuery = WHERE;
				}
				importerNamesSqlQuery += MESSAGE_ID + " IN (" + SELECT + ID + FROM + MessageSQL.MESSAGE + WHERE + ID
						+ " = " + MESSAGE_ID + AND + INTERFACE_NAME + " IN ('" + importerNamesSql + "'))";
			}

			sqlQuery += importerNamesSqlQuery;

			if (errorDateFrom != null && errorDateTo != null) {
				dateSqlQuery = WHERE + ERROR_DATE + " >=" + "'"
						+ DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS).format(errorDateFrom.atStartOfDay()) + "'"
						+ AND + ERROR_DATE + " < " + "'" + DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS)
								.format(errorDateTo.plusDays(1).atStartOfDay())
						+ "'";
			} else {
				if (errorDateFrom == null && errorDateTo != null) {
					dateSqlQuery = WHERE + ERROR_DATE + " < " + "'" + DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS)
							.format(errorDateTo.plusDays(1).atStartOfDay()) + "'";
				}
				if (errorDateFrom != null && errorDateTo == null) {
					dateSqlQuery = WHERE + ERROR_DATE + "  >= " + "'"
							+ DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS).format(errorDateFrom.atStartOfDay())
							+ "'";
				}
				if (errorDateFrom == null && errorDateTo == null) {
					dateSqlQuery += "";
				}
			}

			if (solvingDateFrom != null && solvingDateTo != null) {
				if (dateSqlQuery.contains(WHERE)) {
					dateSqlQuery += AND;
				} else {
					dateSqlQuery += WHERE;
				}
				dateSqlQuery += SOLVING_DATE + " >=" + "'"
						+ DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS).format(solvingDateFrom.atStartOfDay()) + "'"
						+ AND + SOLVING_DATE + " < " + "'" + DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS)
								.format(solvingDateTo.plusDays(1).atStartOfDay())
						+ "'";
			} else {
				if (solvingDateFrom == null && solvingDateTo != null) {
					if (dateSqlQuery.contains(WHERE)) {
						dateSqlQuery += AND;
					} else {
						dateSqlQuery += WHERE;
					}
					dateSqlQuery += SOLVING_DATE + " < " + "'" + DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS)
							.format(solvingDateTo.plusDays(1).atStartOfDay()) + "'";
				}
				if (solvingDateFrom != null && solvingDateTo == null) {
					if (dateSqlQuery.contains(WHERE)) {
						dateSqlQuery += AND;
					} else {
						dateSqlQuery += WHERE;
					}
					dateSqlQuery += SOLVING_DATE + " >= " + "'"
							+ DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS).format(solvingDateFrom.atStartOfDay())
							+ "'";
				}
				if (solvingDateFrom == null && solvingDateTo == null) {
					dateSqlQuery += StringUtils.EMPTY;
				}
			}

			sqlQuery += dateSqlQuery;

			String messageSqlQuery = StringUtils.EMPTY;

			if (messageId > 0) {
				if (sqlQuery.contains(WHERE)) {
					messageSqlQuery = AND;
				} else {
					messageSqlQuery = WHERE;
				}
				messageSqlQuery += MESSAGE_ID + " IN (" + SELECT + ID + FROM + MessageSQL.MESSAGE + WHERE + ID + " = "
						+ messageId + ")";
			}

			sqlQuery += messageSqlQuery;

			String statusSqlQuery = StringUtils.EMPTY;

			if (status != null) {
				if (sqlQuery.contains(WHERE)) {
					statusSqlQuery = AND;
				} else {
					statusSqlQuery = WHERE;
				}
				statusSqlQuery += STATUS + " = '" + status.name() + "'";
			}

			sqlQuery += statusSqlQuery;
			String joinSqlQuery = "";

			if (sqlQuery.contains(WHERE)) {
				joinSqlQuery = AND;
			} else {
				joinSqlQuery = WHERE;
			}
			joinSqlQuery += ID + " = " + ERROR_ID;

			sqlQuery += joinSqlQuery;

			try (ResultSet results = stmtGetImportErrors.executeQuery(sqlQuery)) {
				while (results.next()) {
					if (importErrors == null) {
						importErrors = new ArrayList<>();
					}
					ImportError importError = new ImportError();
					importError.setId(results.getLong(ID));
					importError.setErrorMessage(results.getString(ErrorSQL.MESSAGE));
					Timestamp solvingDate = results.getTimestamp(SOLVING_DATE);
					if (solvingDate != null) {
						importError.setSolvingDate(solvingDate.toLocalDateTime());
					}
					importError.setErrorDate(results.getTimestamp(ERROR_DATE).toLocalDateTime());
					importError.setStatus(Status.valueOf(results.getString(STATUS)));
					importError.setMessage(MessageSQL.getMessageById(results.getLong(MESSAGE_ID)));
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