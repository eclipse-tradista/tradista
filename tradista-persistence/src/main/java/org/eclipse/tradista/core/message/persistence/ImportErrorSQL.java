package org.eclipse.tradista.core.message.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ERROR_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.STATUS;
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
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.message.model.ImportError;
import org.eclipse.tradista.core.message.model.ImportError.ImportErrorType;
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

	private static final Field ERROR_ID_FIELD = new Field(ERROR_ID);

	private static final Field MESSAGE_ID_FIELD = new Field("MESSAGE_ID");

	private static final Field IMPORT_ERROR_TYPE_FIELD = new Field("IMPORT_ERROR_TYPE");

	private static final Field[] IMPORT_ERROR_FIELDS = { ERROR_ID_FIELD, MESSAGE_ID_FIELD, IMPORT_ERROR_TYPE_FIELD };

	private static final Table IMPORT_ERROR_TABLE = new Table("IMPORT_ERROR", IMPORT_ERROR_FIELDS);

	private static final Join ERROR_AND_IMPORT_ERROR_INNER_JOIN = Join.innerEq(ERROR_TABLE, ID_FIELD, ERROR_ID_FIELD);

	private static final String SELECT_QUERY = TradistaDBUtil.buildSelectQuery(IMPORT_ERROR_TABLE,
			ERROR_AND_IMPORT_ERROR_INNER_JOIN);

	public static long saveImportError(ImportError error) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveError = TradistaDBUtil.buildInsertPreparedStatement(con, ERROR_TABLE,
						TYPE_FIELD, MESSAGE_FIELD, STATUS_FIELD, ERROR_DATE_FIELD, SOLVING_DATE_FIELD);
				PreparedStatement stmtSaveImportError = TradistaDBUtil.buildInsertPreparedStatement(con,
						IMPORT_ERROR_TABLE, ERROR_ID_FIELD, MESSAGE_ID_FIELD, IMPORT_ERROR_TYPE_FIELD);
				PreparedStatement stmtUpdateError = TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD,
						ERROR_TABLE, TYPE_FIELD, MESSAGE_FIELD, STATUS_FIELD, ERROR_DATE_FIELD, SOLVING_DATE_FIELD);
				PreparedStatement stmtUpdateImportError = TradistaDBUtil.buildUpdatePreparedStatement(con,
						ERROR_ID_FIELD, IMPORT_ERROR_TABLE, MESSAGE_ID_FIELD, IMPORT_ERROR_TYPE_FIELD)) {
			if (error.getId() == 0) {
				stmtSaveError.setString(1, error.getType());
				stmtSaveError.setString(2, error.getErrorMessage());
				stmtSaveError.setString(3, error.getStatus().name());
				stmtSaveError.setTimestamp(4, java.sql.Timestamp.valueOf(error.getErrorDate()));
				if (error.getSolvingDate() != null) {
					stmtSaveError.setTimestamp(5, java.sql.Timestamp.valueOf(error.getSolvingDate()));
				} else {
					stmtSaveError.setNull(5, Types.TIMESTAMP);
				}
				stmtSaveError.executeUpdate();
				if (error.getId() == 0) {
					try (ResultSet generatedKeys = stmtSaveError.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							error.setId(generatedKeys.getLong(1));
						} else {
							throw new SQLException("Creating error failed, no generated key obtained.");
						}
					}
				}
				stmtSaveImportError.setLong(1, error.getId());
				stmtSaveImportError.setLong(2, error.getMessage().getId());
				stmtSaveImportError.setString(3, error.getImportErrorType().name());
				stmtSaveImportError.executeUpdate();
			} else {
				stmtUpdateImportError.setLong(1, error.getMessage().getId());
				stmtUpdateImportError.setString(2, error.getImportErrorType().name());
				stmtUpdateImportError.setLong(3, error.getId());
				stmtUpdateImportError.executeUpdate();

				stmtUpdateError.setString(1, error.getType());
				stmtUpdateError.setString(2, error.getErrorMessage());
				stmtUpdateError.setString(3, error.getStatus().name());
				stmtUpdateError.setTimestamp(4, java.sql.Timestamp.valueOf(error.getErrorDate()));
				stmtUpdateError.setTimestamp(4, java.sql.Timestamp.valueOf(error.getErrorDate()));
				if (error.getSolvingDate() != null) {
					stmtUpdateError.setTimestamp(5, java.sql.Timestamp.valueOf(error.getSolvingDate()));
				} else {
					stmtUpdateError.setNull(5, Types.TIMESTAMP);
				}
				stmtUpdateError.setLong(6, error.getId());
				stmtUpdateError.executeUpdate();
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return error.getId();
	}

	public static List<ImportError> getImportErrors(Set<String> importerTypes, Set<String> importerNames,
			long messageId, Status status, ImportErrorType importErrorType, LocalDate errorDateFrom,
			LocalDate errorDateTo, LocalDate solvingDateFrom, LocalDate solvingDateTo) {
		List<ImportError> importErrors = null;

		try (Connection con = TradistaDB.getConnection(); Statement stmtGetImportErrors = con.createStatement()) {
			StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);

			if (!CollectionUtils.isEmpty(importerTypes)) {
				StringBuilder queryFilter = new StringBuilder(
						TradistaDBUtil.buildSelectQuery(MessageSQL.ID_FIELD, MessageSQL.MESSAGE_TABLE));
				TradistaDBUtil.addFilter(queryFilter, MessageSQL.TYPE_FIELD, importerTypes);
				TradistaDBUtil.addQueryFilter(sqlQuery, MESSAGE_ID_FIELD, queryFilter.toString(), false);
			}

			if (!CollectionUtils.isEmpty(importerNames)) {
				StringBuilder queryFilter = new StringBuilder(
						TradistaDBUtil.buildSelectQuery(new Field[] { MessageSQL.ID_FIELD }, MessageSQL.MESSAGE_TABLE));
				TradistaDBUtil.addFilter(queryFilter, MessageSQL.INTERFACE_NAME_FIELD, importerNames);
				TradistaDBUtil.addQueryFilter(sqlQuery, MESSAGE_ID_FIELD, queryFilter.toString(), false);
			}

			TradistaDBUtil.addFilter(sqlQuery, ERROR_DATE_FIELD, errorDateFrom, true);
			TradistaDBUtil.addFilter(sqlQuery, ERROR_DATE_FIELD, errorDateTo, false);
			TradistaDBUtil.addFilter(sqlQuery, SOLVING_DATE_FIELD, solvingDateFrom, true);
			TradistaDBUtil.addFilter(sqlQuery, SOLVING_DATE_FIELD, solvingDateTo, false);

			if (messageId > 0) {
				StringBuilder queryFilter = new StringBuilder(
						TradistaDBUtil.buildSelectQuery(new Field[] { MessageSQL.ID_FIELD }, MessageSQL.MESSAGE_TABLE));
				TradistaDBUtil.addFilter(queryFilter, MessageSQL.ID_FIELD, messageId);
				TradistaDBUtil.addQueryFilter(sqlQuery, MESSAGE_ID_FIELD, queryFilter.toString(), false);
			}

			TradistaDBUtil.addFilter(sqlQuery, STATUS_FIELD, status);

			TradistaDBUtil.addFilter(sqlQuery, IMPORT_ERROR_TYPE_FIELD, importErrorType);

			try (ResultSet results = stmtGetImportErrors.executeQuery(sqlQuery.toString())) {
				while (results.next()) {
					if (importErrors == null) {
						importErrors = new ArrayList<>();
					}
					ImportError importError = new ImportError();
					importError.setId(results.getLong(ID_FIELD.getName()));
					importError.setErrorMessage(results.getString(MESSAGE_FIELD.getName()));
					Timestamp solvingDate = results.getTimestamp(SOLVING_DATE_FIELD.getName());
					if (solvingDate != null) {
						importError.setSolvingDate(solvingDate.toLocalDateTime());
					}
					importError.setErrorDate(results.getTimestamp(ERROR_DATE_FIELD.getName()).toLocalDateTime());
					importError.setStatus(Status.valueOf(results.getString(STATUS)));
					importError.setMessage(MessageSQL.getMessageById(results.getLong(MESSAGE_ID_FIELD.getName())));
					importError.setImportErrorType(
							ImportErrorType.valueOf(results.getString(IMPORT_ERROR_TYPE_FIELD.getName())));
					importErrors.add(importError);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle.getMessage());
		}

		return importErrors;
	}

	public static ImportError getImportError(long msgId, ImportErrorType importErrorType) {
		ImportError importError = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);

		TradistaDBUtil.addParameterizedFilter(sqlQuery, MESSAGE_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, IMPORT_ERROR_TYPE_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetImportError = con.prepareStatement(sqlQuery.toString())) {

			stmtGetImportError.setLong(1, msgId);
			stmtGetImportError.setString(2, importErrorType.name());
			try (ResultSet results = stmtGetImportError.executeQuery()) {
				while (results.next()) {
					importError = new ImportError();
					importError.setId(results.getLong(ID_FIELD.getName()));
					importError.setErrorMessage(results.getString(MESSAGE_FIELD.getName()));
					Timestamp solvingDate = results.getTimestamp(SOLVING_DATE_FIELD.getName());
					if (solvingDate != null) {
						importError.setSolvingDate(solvingDate.toLocalDateTime());
					}
					importError.setErrorDate(results.getTimestamp(ERROR_DATE_FIELD.getName()).toLocalDateTime());
					importError.setStatus(Status.valueOf(results.getString(STATUS)));
					importError.setMessage(MessageSQL.getMessageById(results.getLong(MESSAGE_ID_FIELD.getName())));
					importError.setImportErrorType(
							ImportErrorType.valueOf(results.getString(IMPORT_ERROR_TYPE_FIELD.getName())));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle.getMessage());
		}

		return importError;
	}

}