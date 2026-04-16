package org.eclipse.tradista.core.position.persistence;

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
import java.sql.Date;
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
import org.eclipse.tradista.core.error.persistence.ErrorSQL;
import org.eclipse.tradista.core.position.model.PositionCalculationError;
import org.eclipse.tradista.core.product.persistence.ProductSQL;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;

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

public class PositionCalculationErrorSQL {

	private static final Field ERROR_ID_FIELD = new Field(ERROR_ID);

	private static final Field VALUE_DATE_FIELD = new Field("VALUE_DATE");

	private static final Field POSITION_DEFINITION_ID_FIELD = new Field("POSITION_DEFINITION_ID");

	private static final Field TRADE_ID_FIELD = new Field("TRADE_ID");

	private static final Field PRODUCT_ID_FIELD = new Field("PRODUCT_ID");

	private static final Field[] POSITION_CALCULATION_ERROR_FIELDS = new Field[] { ERROR_ID_FIELD, VALUE_DATE_FIELD,
			POSITION_DEFINITION_ID_FIELD, TRADE_ID_FIELD, PRODUCT_ID_FIELD };

	private static final Table POSITION_CALCULATION_ERROR_TABLE = new Table("POSITION_CALCULATION_ERROR",
			POSITION_CALCULATION_ERROR_FIELDS);

	private static final Join ERROR_AND_POSITION_CALCULATION_ERROR_INNER_JOIN = Join.innerEq(ERROR_TABLE, ID_FIELD,
			ERROR_ID_FIELD);

	public static boolean savePositionCalculationErrors(List<PositionCalculationError> errors) {
		boolean bSaved = true;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveErrors = TradistaDBUtil.buildInsertPreparedStatement(con, ERROR_TABLE,
						TYPE_FIELD, MESSAGE_FIELD, STATUS_FIELD, ERROR_DATE_FIELD, SOLVING_DATE_FIELD);
				PreparedStatement stmtSavePositionCalculationErrors = TradistaDBUtil.buildInsertPreparedStatement(con,
						POSITION_CALCULATION_ERROR_TABLE, ERROR_ID_FIELD, VALUE_DATE_FIELD,
						POSITION_DEFINITION_ID_FIELD, TRADE_ID_FIELD, PRODUCT_ID_FIELD);
				PreparedStatement stmtUpdateErrors = TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD,
						ERROR_TABLE, TYPE_FIELD, MESSAGE_FIELD, STATUS_FIELD, ERROR_DATE_FIELD, SOLVING_DATE_FIELD);
				PreparedStatement stmtUpdatePositionCalculationErrors = TradistaDBUtil.buildUpdatePreparedStatement(con,
						ERROR_ID_FIELD, POSITION_CALCULATION_ERROR_TABLE, VALUE_DATE_FIELD,
						POSITION_DEFINITION_ID_FIELD, TRADE_ID_FIELD, PRODUCT_ID_FIELD)) {
			for (PositionCalculationError error : errors) {
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
					stmtSavePositionCalculationErrors.setLong(1, error.getId());
					stmtSavePositionCalculationErrors.setDate(2, Date.valueOf(error.getValueDate()));
					stmtSavePositionCalculationErrors.setLong(3, error.getPositionDefinition().getId());
					if (error.getTrade() != null) {
						stmtSavePositionCalculationErrors.setLong(4, error.getTrade().getId());
					} else {
						stmtSavePositionCalculationErrors.setNull(4, java.sql.Types.BIGINT);
					}
					if (error.getProduct() != null) {
						stmtSavePositionCalculationErrors.setLong(5, error.getProduct().getId());
					} else {
						stmtSavePositionCalculationErrors.setNull(5, java.sql.Types.BIGINT);
					}
					stmtSavePositionCalculationErrors.addBatch();
				} else {
					stmtUpdatePositionCalculationErrors.setDate(1, Date.valueOf(error.getValueDate()));
					stmtUpdatePositionCalculationErrors.setLong(2, error.getPositionDefinition().getId());
					if (error.getTrade() != null) {
						stmtUpdatePositionCalculationErrors.setLong(3, error.getTrade().getId());
					} else {
						stmtUpdatePositionCalculationErrors.setNull(3, java.sql.Types.BIGINT);
					}
					if (error.getProduct() != null) {
						stmtUpdatePositionCalculationErrors.setLong(4, error.getProduct().getId());
					} else {
						stmtUpdatePositionCalculationErrors.setNull(4, java.sql.Types.BIGINT);
					}
					stmtUpdatePositionCalculationErrors.setLong(5, error.getId());
					stmtUpdatePositionCalculationErrors.addBatch();

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
			stmtSavePositionCalculationErrors.executeBatch();
			stmtUpdateErrors.executeBatch();
			stmtUpdatePositionCalculationErrors.executeBatch();
			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static void solvePositionCalculationError(Set<Long> solved, LocalDate date) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSolvePositionCalculationErrors = con.prepareStatement("UPDATE " + ERROR_TABLE
						+ " SET " + STATUS_FIELD + " = ?, " + SOLVING_DATE_FIELD + " = ? " + WHERE + ID_FIELD + IN
						+ "(SELECT ERROR_ID FROM POSITION_CALCULATION_ERROR WHERE POSITION_DEFINITION_ID = ?)")) {
			for (long id : solved) {
				stmtSolvePositionCalculationErrors.setString(1,
						org.eclipse.tradista.core.error.model.Error.Status.SOLVED.name());
				stmtSolvePositionCalculationErrors.setDate(2, java.sql.Date.valueOf(date));
				stmtSolvePositionCalculationErrors.setLong(3, id);
				stmtSolvePositionCalculationErrors.addBatch();
			}
			stmtSolvePositionCalculationErrors.executeBatch();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static void solvePositionCalculationError(long positionDefinitionId, LocalDate date) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSolvePositionCalculationError = con.prepareStatement("UPDATE " + ERROR_TABLE
						+ " SET " + STATUS_FIELD + " = ?, " + SOLVING_DATE_FIELD + " = ? " + WHERE + ID_FIELD + IN
						+ "(SELECT ERROR_ID FROM POSITION_CALCULATION_ERROR WHERE POSITION_DEFINITION_ID = ?)")) {
			stmtSolvePositionCalculationError.setString(1,
					org.eclipse.tradista.core.error.model.Error.Status.SOLVED.name());
			stmtSolvePositionCalculationError.setDate(2, java.sql.Date.valueOf(date));
			stmtSolvePositionCalculationError.setLong(3, positionDefinitionId);
			stmtSolvePositionCalculationError.executeUpdate();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static List<PositionCalculationError> getPositionCalculationErrors(long positionDefinitionId, Status status,
			long tradeId, long productId, LocalDate valueDateFrom, LocalDate valueDateTo, LocalDate errorDateFrom,
			LocalDate errorDateTo, LocalDate solvingDateFrom, LocalDate solvingDateTo) {
		List<PositionCalculationError> positionCalculationErrors = null;

		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetPositionCalculationErrors = con.createStatement()) {
			StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(POSITION_CALCULATION_ERROR_TABLE,
					ERROR_AND_POSITION_CALCULATION_ERROR_INNER_JOIN));

			if (valueDateFrom != null) {
				TradistaDBUtil.addFilter(sqlQuery, VALUE_DATE_FIELD, valueDateFrom, true);
			}
			if (valueDateTo != null) {
				TradistaDBUtil.addFilter(sqlQuery, VALUE_DATE_FIELD, valueDateTo, false);
			}

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

			if (positionDefinitionId != 0) {
				TradistaDBUtil.addFilter(sqlQuery, POSITION_DEFINITION_ID_FIELD, positionDefinitionId);
			}

			if (status != null) {
				TradistaDBUtil.addFilter(sqlQuery, STATUS_FIELD, status.name());
			}

			if (tradeId > 0) {
				TradistaDBUtil.addFilter(sqlQuery, TRADE_ID_FIELD, tradeId);
			}

			if (productId > 0) {
				TradistaDBUtil.addFilter(sqlQuery, PRODUCT_ID_FIELD, productId);
			}

			try (ResultSet results = stmtGetPositionCalculationErrors.executeQuery(sqlQuery.toString())) {
				while (results.next()) {
					if (positionCalculationErrors == null) {
						positionCalculationErrors = new ArrayList<>();
					}
					PositionCalculationError positionCalculationError = new PositionCalculationError();
					positionCalculationError.setId(results.getLong(ID_FIELD.getName()));
					positionCalculationError.setErrorMessage(results.getString(MESSAGE_FIELD.getName()));
					Timestamp solvingDate = results.getTimestamp(SOLVING_DATE_FIELD.getName());
					if (solvingDate != null) {
						positionCalculationError.setSolvingDate(solvingDate.toLocalDateTime());
					}
					positionCalculationError
							.setErrorDate(results.getTimestamp(ERROR_DATE_FIELD.getName()).toLocalDateTime());
					positionCalculationError.setValueDate(results.getDate(VALUE_DATE_FIELD.getName()).toLocalDate());
					positionCalculationError.setStatus(Status.valueOf(results.getString(STATUS_FIELD.getName())));
					if (results.getLong(TRADE_ID_FIELD.getName()) > 0) {
						positionCalculationError
								.setTrade(TradeSQL.getTradeById(results.getLong(TRADE_ID_FIELD.getName()), false));
					}
					if (results.getLong(PRODUCT_ID_FIELD.getName()) > 0) {
						positionCalculationError
								.setProduct(ProductSQL.getProductById(results.getLong(PRODUCT_ID_FIELD.getName())));
					}
					positionCalculationError.setPositionDefinition(PositionDefinitionSQL
							.getPositionDefinitionById(results.getLong(POSITION_DEFINITION_ID_FIELD.getName())));
					positionCalculationErrors.add(positionCalculationError);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle.getMessage());
		}

		return positionCalculationErrors;
	}

	public static void deleteErrors(Set<Long> ids) {
		if (ids != null && !ids.isEmpty()) {
			try (Connection con = TradistaDB.getConnection();
					PreparedStatement stmtDeleteErrors = TradistaDBUtil.buildDeletePreparedStatement(con,
							POSITION_CALCULATION_ERROR_TABLE, ERROR_ID_FIELD);) {
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