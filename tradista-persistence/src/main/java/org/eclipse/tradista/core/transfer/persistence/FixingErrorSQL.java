package org.eclipse.tradista.core.transfer.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ERROR_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.IN;
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
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
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
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.FixingError;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

public class FixingErrorSQL {

	private static final Field ERROR_ID_FIELD = new Field(ERROR_ID);

	private static final Field TRANSFER_ID_FIELD = new Field("TRANSFER_ID");

	private static final Field[] FIXING_ERROR_FIELDS = new Field[] { ERROR_ID_FIELD, TRANSFER_ID_FIELD };

	private static final Table FIXING_ERROR_TABLE = new Table("FIXING_ERROR", FIXING_ERROR_FIELDS);

	private static final Join ERROR_AND_FIXING_ERROR_INNER_JOIN = Join.innerEq(ERROR_TABLE, ID_FIELD, ERROR_ID_FIELD);

	public static boolean saveFixingErrors(List<FixingError> errors) {
		boolean bSaved = false;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveErrors = TradistaDBUtil.buildInsertPreparedStatement(con, ERROR_TABLE,
						TYPE_FIELD, MESSAGE_FIELD, STATUS_FIELD, ERROR_DATE_FIELD, SOLVING_DATE_FIELD);
				PreparedStatement stmtSaveFixingErrors = TradistaDBUtil.buildInsertPreparedStatement(con,
						FIXING_ERROR_TABLE, ERROR_ID_FIELD, TRANSFER_ID_FIELD);
				PreparedStatement stmtUpdateErrors = TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD,
						ERROR_TABLE, TYPE_FIELD, MESSAGE_FIELD, STATUS_FIELD, ERROR_DATE_FIELD, SOLVING_DATE_FIELD);
				PreparedStatement stmtUpdateFixingErrors = TradistaDBUtil.buildUpdatePreparedStatement(con,
						ERROR_ID_FIELD, FIXING_ERROR_TABLE, TRANSFER_ID_FIELD)) {
			for (FixingError error : errors) {
				if (error.getId() == 0) {
					stmtSaveErrors.setString(1, error.getType());
					stmtSaveErrors.setString(2, error.getErrorMessage());
					stmtSaveErrors.setString(3, error.getStatus().name());
					stmtSaveErrors.setTimestamp(4, java.sql.Timestamp.valueOf(error.getErrorDate()));
					if (error.getSolvingDate() != null) {
						stmtSaveErrors.setTimestamp(5, java.sql.Timestamp.valueOf(error.getSolvingDate()));
					} else {
						stmtSaveErrors.setNull(5, Types.TIMESTAMP);
					}

					stmtSaveErrors.executeUpdate();
					try (ResultSet generatedKeys = stmtSaveErrors.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							error.setId(generatedKeys.getLong(1));
						} else {
							throw new SQLException("Creating error failed, no generated key obtained.");
						}
					}
					stmtSaveFixingErrors.setLong(1, error.getId());
					stmtSaveFixingErrors.setLong(2, error.getCashTransfer().getId());
					stmtSaveFixingErrors.addBatch();
				} else {
					stmtUpdateFixingErrors.setLong(1, error.getCashTransfer().getId());
					stmtUpdateFixingErrors.setLong(2, error.getId());
					stmtUpdateFixingErrors.addBatch();

					stmtUpdateErrors.setString(1, error.getType());
					stmtUpdateErrors.setString(2, error.getErrorMessage());
					stmtUpdateErrors.setString(3, error.getStatus().name());
					stmtUpdateErrors.setTimestamp(4, java.sql.Timestamp.valueOf(error.getErrorDate()));
					if (error.getSolvingDate() != null) {
						stmtUpdateErrors.setTimestamp(5, java.sql.Timestamp.valueOf(error.getSolvingDate()));
					} else {
						stmtUpdateErrors.setNull(5, Types.TIMESTAMP);
					}
					stmtUpdateErrors.setLong(6, error.getId());
					stmtUpdateErrors.addBatch();
				}
			}
			stmtSaveFixingErrors.executeBatch();
			stmtUpdateErrors.executeBatch();
			stmtUpdateFixingErrors.executeBatch();
			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return bSaved;
	}

	public static void solveFixingError(Set<Long> solved, LocalDate date) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSolveFixingErrors = con.prepareStatement("UPDATE " + ERROR_TABLE + " SET "
						+ STATUS_FIELD + " = ?, " + SOLVING_DATE_FIELD + "  = ? " + WHERE + ID_FIELD + IN
						+ " (SELECT ERROR_ID FROM FIXING_ERROR WHERE TRANSFER_ID = ?)")) {
			for (long id : solved) {
				stmtSolveFixingErrors.setString(1, org.eclipse.tradista.core.error.model.Error.Status.SOLVED.name());
				stmtSolveFixingErrors.setDate(2, java.sql.Date.valueOf(date));
				stmtSolveFixingErrors.setLong(3, id);
				stmtSolveFixingErrors.addBatch();
			}
			stmtSolveFixingErrors.executeBatch();
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static void solveFixingError(long transferId, LocalDate date) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSolveFixingError = con.prepareStatement("UPDATE " + ERROR_TABLE + " SET "
						+ STATUS_FIELD + "  = ?, " + SOLVING_DATE_FIELD + " = ? " + WHERE + ID_FIELD + IN
						+ " (SELECT ERROR_ID FROM TRANSFER_ERROR WHERE TRANSFER_ID = ?)")) {
			stmtSolveFixingError.setString(1, org.eclipse.tradista.core.error.model.Error.Status.SOLVED.name());
			stmtSolveFixingError.setDate(2, java.sql.Date.valueOf(date));
			stmtSolveFixingError.setLong(3, transferId);
			stmtSolveFixingError.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static List<FixingError> getFixingErrors(long transferId, Status status, LocalDate errorDateFrom,
			LocalDate errorDateTo, LocalDate solvingDateFrom, LocalDate solvingDateTo) {
		List<FixingError> fixingErrors = null;

		try (Connection con = TradistaDB.getConnection(); Statement stmtGetFixingErrors = con.createStatement()) {
			StringBuilder sqlQuery = new StringBuilder(
					TradistaDBUtil.buildSelectQuery(FIXING_ERROR_TABLE, ERROR_AND_FIXING_ERROR_INNER_JOIN));

			if (errorDateFrom != null) {
				TradistaDBUtil.addFilter(sqlQuery, ERROR_DATE_FIELD, errorDateFrom.atStartOfDay(), true);
			}
			if (errorDateTo != null) {
				TradistaDBUtil.addFilter(sqlQuery, ERROR_DATE_FIELD, errorDateTo.atTime(LocalTime.MAX), false);
			}

			if (solvingDateFrom != null) {
				TradistaDBUtil.addFilter(sqlQuery, SOLVING_DATE_FIELD, solvingDateFrom.atStartOfDay(), true);
			}
			if (solvingDateTo != null) {
				TradistaDBUtil.addFilter(sqlQuery, SOLVING_DATE_FIELD, solvingDateTo.atTime(LocalTime.MAX), false);
			}

			if (transferId > 0) {
				StringBuilder queryFilter = new StringBuilder(
						TradistaDBUtil.buildSelectQuery(TransferSQL.ID_FIELD, TransferSQL.TRANSFER_TABLE));
				TradistaDBUtil.addFilter(queryFilter, TransferSQL.ID_FIELD, transferId);
				TradistaDBUtil.addQueryFilter(sqlQuery, TRANSFER_ID_FIELD, queryFilter.toString(), false);
			}

			if (status != null) {
				TradistaDBUtil.addFilter(sqlQuery, STATUS_FIELD, status);
			}

			try (ResultSet results = stmtGetFixingErrors.executeQuery(sqlQuery.toString())) {
				while (results.next()) {
					if (fixingErrors == null) {
						fixingErrors = new ArrayList<>();
					}
					FixingError fixingError = new FixingError();
					fixingError.setId(results.getLong(ID_FIELD.getName()));
					fixingError.setErrorMessage(results.getString(MESSAGE_FIELD.getName()));
					Timestamp solvingDate = results.getTimestamp(SOLVING_DATE_FIELD.getName());
					if (solvingDate != null) {
						fixingError.setSolvingDate(solvingDate.toLocalDateTime());
					}
					fixingError.setErrorDate(results.getTimestamp(ERROR_DATE_FIELD.getName()).toLocalDateTime());
					fixingError.setStatus(Status.valueOf(results.getString(STATUS_FIELD.getName())));
					fixingError.setCashTransfer(
							(CashTransfer) TransferSQL.getTransferById(results.getLong(TRANSFER_ID_FIELD.getName())));
					fixingErrors.add(fixingError);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle.getMessage());
		}

		return fixingErrors;
	}

	public static void deleteErrors(Set<Long> ids) {
		if (ids != null && !ids.isEmpty()) {
			try (Connection con = TradistaDB.getConnection();
					PreparedStatement stmtDeleteErrors = TradistaDBUtil.buildDeletePreparedStatement(con,
							FIXING_ERROR_TABLE, ERROR_ID_FIELD)) {
				for (long id : ids) {
					stmtDeleteErrors.setLong(1, id);
					stmtDeleteErrors.addBatch();
				}
				stmtDeleteErrors.executeBatch();
			} catch (SQLException sqle) {
				throw new TradistaTechnicalException(sqle);
			}
		}
	}

}