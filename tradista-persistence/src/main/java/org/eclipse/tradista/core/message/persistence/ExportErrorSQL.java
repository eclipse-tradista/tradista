package org.eclipse.tradista.core.message.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ERROR_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
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
import org.eclipse.tradista.core.message.model.ExportError;
import org.eclipse.tradista.core.message.model.ExportError.ExportErrorType;
import org.springframework.util.CollectionUtils;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public class ExportErrorSQL {

	private static final Field ERROR_ID_FIELD = new Field(ERROR_ID);

	private static final Field MESSAGE_ID_FIELD = new Field("MESSAGE_ID");

	private static final Field EXPORT_ERROR_TYPE_FIELD = new Field("EXPORT_ERROR_TYPE");

	private static final Field[] EXPORT_ERROR_FIELDS = { ERROR_ID_FIELD, MESSAGE_ID_FIELD, EXPORT_ERROR_TYPE_FIELD };

	private static final Table EXPORT_ERROR_TABLE = new Table("EXPORT_ERROR", EXPORT_ERROR_FIELDS);

	private static final Join ERROR_AND_EXPORT_ERROR_INNER_JOIN = Join.inner(ID_FIELD, ERROR_ID_FIELD);

	private static final String SELECT_QUERY = TradistaDBUtil.buildSelectQuery(EXPORT_ERROR_TABLE,
			ERROR_AND_EXPORT_ERROR_INNER_JOIN);

	public static long saveExportError(ExportError error) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveError = TradistaDBUtil.buildInsertPreparedStatement(con, ERROR_TABLE,
						TYPE_FIELD, MESSAGE_FIELD, STATUS_FIELD, ERROR_DATE_FIELD, SOLVING_DATE_FIELD);
				PreparedStatement stmtSaveExportError = TradistaDBUtil.buildInsertPreparedStatement(con,
						EXPORT_ERROR_TABLE, ERROR_ID_FIELD, MESSAGE_ID_FIELD, EXPORT_ERROR_TYPE_FIELD);
				PreparedStatement stmtUpdateError = TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD,
						ERROR_TABLE, TYPE_FIELD, MESSAGE_FIELD, STATUS_FIELD, ERROR_DATE_FIELD, SOLVING_DATE_FIELD);
				PreparedStatement stmtUpdateExportError = TradistaDBUtil.buildUpdatePreparedStatement(con,
						ERROR_ID_FIELD, EXPORT_ERROR_TABLE, MESSAGE_ID_FIELD, EXPORT_ERROR_TYPE_FIELD)) {
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
				stmtSaveExportError.setLong(1, error.getId());
				stmtSaveExportError.setLong(2, error.getMessage().getId());
				stmtSaveExportError.setString(3, error.getExportErrorType().name());
				stmtSaveExportError.executeUpdate();
			} else {
				stmtUpdateExportError.setLong(1, error.getMessage().getId());
				stmtUpdateExportError.setString(2, error.getExportErrorType().name());
				stmtUpdateExportError.setLong(3, error.getId());
				stmtUpdateExportError.executeUpdate();

				stmtUpdateError.setString(1, error.getType());
				stmtUpdateError.setString(2, error.getErrorMessage());
				stmtUpdateError.setString(3, error.getStatus().name());
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

	public static List<ExportError> getExportErrors(Set<String> exporterTypes, Set<String> exporterNames,
			long messageId, Status status, ExportErrorType exportErrorType, LocalDate errorDateFrom,
			LocalDate errorDateTo, LocalDate solvingDateFrom, LocalDate solvingDateTo) {
		List<ExportError> exportErrors = null;

		try (Connection con = TradistaDB.getConnection(); Statement stmtGetExportErrors = con.createStatement()) {
			StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);

			if (!CollectionUtils.isEmpty(exporterTypes)) {
				StringBuilder queryFilter = new StringBuilder(
						TradistaDBUtil.buildSelectQuery(new Field[] { MessageSQL.ID_FIELD }, MessageSQL.MESSAGE_TABLE));
				TradistaDBUtil.addFilter(queryFilter, MessageSQL.TYPE_FIELD, exporterTypes);
				TradistaDBUtil.addQueryFilter(sqlQuery, MESSAGE_ID_FIELD, queryFilter.toString(), false);
			}

			if (!CollectionUtils.isEmpty(exporterNames)) {
				StringBuilder queryFilter = new StringBuilder(
						TradistaDBUtil.buildSelectQuery(new Field[] { MessageSQL.ID_FIELD }, MessageSQL.MESSAGE_TABLE));
				TradistaDBUtil.addFilter(queryFilter, MessageSQL.INTERFACE_NAME_FIELD, exporterNames);
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

			TradistaDBUtil.addFilter(sqlQuery, EXPORT_ERROR_TYPE_FIELD, exportErrorType);

			try (ResultSet results = stmtGetExportErrors.executeQuery(sqlQuery.toString())) {
				while (results.next()) {
					if (exportErrors == null) {
						exportErrors = new ArrayList<>();
					}
					ExportError exportError = new ExportError();
					exportError.setId(results.getLong(ID));
					exportError.setErrorMessage(results.getString(MESSAGE_FIELD.getName()));
					Timestamp solvingDate = results.getTimestamp(SOLVING_DATE_FIELD.getName());
					if (solvingDate != null) {
						exportError.setSolvingDate(solvingDate.toLocalDateTime());
					}
					exportError.setErrorDate(results.getTimestamp(ERROR_DATE_FIELD.getName()).toLocalDateTime());
					exportError.setStatus(Status.valueOf(results.getString(STATUS)));
					exportError.setMessage(MessageSQL.getMessageById(results.getLong(MESSAGE_ID_FIELD.getName())));
					exportError.setExportErrorType(
							ExportErrorType.valueOf(results.getString(EXPORT_ERROR_TYPE_FIELD.getName())));
					exportErrors.add(exportError);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle.getMessage());
		}

		return exportErrors;
	}

	public static ExportError getExportError(long msgId, ExportErrorType exportErrorType) {
		ExportError exportError = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);

		TradistaDBUtil.addParameterizedFilter(sqlQuery, MESSAGE_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, EXPORT_ERROR_TYPE_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetExportError = con.prepareStatement(sqlQuery.toString())) {

			stmtGetExportError.setLong(1, msgId);
			stmtGetExportError.setString(2, exportErrorType.name());
			try (ResultSet results = stmtGetExportError.executeQuery()) {
				while (results.next()) {
					exportError = new ExportError();
					exportError.setId(results.getLong(ID_FIELD.getName()));
					exportError.setErrorMessage(results.getString(MESSAGE_FIELD.getName()));
					Timestamp solvingDate = results.getTimestamp(SOLVING_DATE_FIELD.getName());
					if (solvingDate != null) {
						exportError.setSolvingDate(solvingDate.toLocalDateTime());
					}
					exportError.setErrorDate(results.getTimestamp(ERROR_DATE_FIELD.getName()).toLocalDateTime());
					exportError.setStatus(Status.valueOf(results.getString(STATUS)));
					exportError.setMessage(MessageSQL.getMessageById(results.getLong(MESSAGE_ID_FIELD.getName())));
					exportError.setExportErrorType(
							ExportErrorType.valueOf(results.getString(EXPORT_ERROR_TYPE_FIELD.getName())));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle.getMessage());
		}

		return exportError;
	}

}