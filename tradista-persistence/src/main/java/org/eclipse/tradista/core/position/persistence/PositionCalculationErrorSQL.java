package org.eclipse.tradista.core.position.persistence;

import java.sql.Connection;
import java.sql.Date;
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

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.error.model.Error.Status;
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

	private static final String SPACE_WHERE_SPACE = " WHERE ";
	private static final String SPACE_AND_SPACE = " AND ";
	private static final String WHERE = "WHERE";
	private static final String MM_DD_YYYY = "MM/dd/yyyy";

	public static boolean savePositionCalculationErrors(List<PositionCalculationError> errors) {
		boolean bSaved = true;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveErrors = con.prepareStatement(
						"INSERT INTO ERROR(TYPE, MESSAGE, STATUS, ERROR_DATE) VALUES(?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtSavePositionCalculationErrors = con
						.prepareStatement("INSERT INTO POSITION_CALCULATION_ERROR VALUES(?, ?, ?, ?, ?)");
				PreparedStatement stmtUpdateErrors = con.prepareStatement(
						"UPDATE ERROR SET TYPE=?, MESSAGE=?, STATUS=?, ERROR_DATE=? WHERE ID=?",
						Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtUpdatePositionCalculationErrors = con.prepareStatement(
						"UPDATE POSITION_CALCULATION_ERROR SET VALUE_DATE=?, POSITION_DEFINITION_ID=?, TRADE_ID=?, PRODUCT_ID=? WHERE ERROR_ID=?")) {
			for (PositionCalculationError error : errors) {
				if (error.getId() == 0) {
					stmtSaveErrors.setString(1, error.getType());
					stmtSaveErrors.setString(2, error.getMessage());
					stmtSaveErrors.setString(3, error.getStatus().name());
					stmtSaveErrors.setTimestamp(4, java.sql.Timestamp.valueOf(error.getErrorDate()));
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
					stmtUpdateErrors.setString(2, error.getMessage());
					stmtUpdateErrors.setString(3, error.getStatus().name());
					stmtUpdateErrors.setTimestamp(4, java.sql.Timestamp.valueOf(error.getErrorDate()));
					stmtUpdateErrors.setLong(5, error.getId());
					stmtUpdateErrors.addBatch();
				}
			}
			stmtSavePositionCalculationErrors.executeBatch();
			stmtUpdateErrors.executeBatch();
			stmtUpdatePositionCalculationErrors.executeBatch();
			bSaved = true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static void solvePositionCalculationError(Set<Long> solved, LocalDate date) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSolvePositionCalculationErrors = con.prepareStatement(
						"UPDATE ERROR SET STATUS = ?, SOLVING_DATE = ? WHERE ID IN (SELECT ERROR_ID FROM POSITION_CALCULATION_ERROR WHERE POSITION_DEFINITION_ID = ?)")) {
			for (long id : solved) {
				stmtSolvePositionCalculationErrors.setString(1,
						org.eclipse.tradista.core.error.model.Error.Status.SOLVED.name());
				stmtSolvePositionCalculationErrors.setDate(2, java.sql.Date.valueOf(date));
				stmtSolvePositionCalculationErrors.setLong(3, id);
				stmtSolvePositionCalculationErrors.addBatch();
			}
			stmtSolvePositionCalculationErrors.executeBatch();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static void solvePositionCalculationError(long positionDefinitionId, LocalDate date) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSolvePositionCalculationError = con.prepareStatement(
						"UPDATE ERROR SET STATUS = ?, SOLVING_DATE = ? WHERE ID IN (SELECT ERROR_ID FROM POSITION_CALCULATION_ERROR WHERE POSITION_DEFINITION_ID = ?)")) {
			stmtSolvePositionCalculationError.setString(1,
					org.eclipse.tradista.core.error.model.Error.Status.SOLVED.name());
			stmtSolvePositionCalculationError.setDate(2, java.sql.Date.valueOf(date));
			stmtSolvePositionCalculationError.setLong(3, positionDefinitionId);
			stmtSolvePositionCalculationError.executeUpdate();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static List<PositionCalculationError> getPositionCalculationErrors(long positionDefinitionId, Status status,
			long tradeId, long productId, LocalDate valueDateFrom, LocalDate valueDateTo, LocalDate errorDateFrom,
			LocalDate errorDateTo, LocalDate solvingDateFrom, LocalDate solvingDateTo) {
		List<PositionCalculationError> positionCalculationErrors = null;

		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetPositionCalculationErrors = con.createStatement()) {
			String sqlQuery = "SELECT * FROM POSITION_CALCULATION_ERROR, ERROR ";
			String dateSqlQuery = "";
			if (valueDateFrom != null && valueDateTo != null) {
				dateSqlQuery = " WHERE VALUE_DATE >=" + "'"
						+ DateTimeFormatter.ofPattern(MM_DD_YYYY).format(valueDateFrom) + "'" + " AND VALUE_DATE <= "
						+ "'" + DateTimeFormatter.ofPattern(MM_DD_YYYY).format(valueDateTo) + "'";
			} else {
				if (valueDateFrom == null && valueDateTo != null) {
					dateSqlQuery = " WHERE VALUE_DATE <= " + "'"
							+ DateTimeFormatter.ofPattern(MM_DD_YYYY).format(valueDateTo) + "'";
				}
				if (valueDateFrom != null && valueDateTo == null) {
					dateSqlQuery = " WHERE VALUE_DATE >= " + "'"
							+ DateTimeFormatter.ofPattern(MM_DD_YYYY).format(valueDateFrom) + "'";
				}
				if (valueDateFrom == null && valueDateTo == null) {
					dateSqlQuery += "";
				}
			}

			if (errorDateFrom != null && errorDateTo != null) {
				if (dateSqlQuery.contains(WHERE)) {
					dateSqlQuery += SPACE_AND_SPACE;
				} else {
					dateSqlQuery += SPACE_WHERE_SPACE;
				}
				dateSqlQuery += " ERROR_DATE >=" + "'" + DateTimeFormatter.ofPattern(MM_DD_YYYY).format(errorDateFrom)
						+ "'" + " AND ERROR_DATE <= " + "'"
						+ DateTimeFormatter.ofPattern(MM_DD_YYYY).format(errorDateTo) + "'";
			} else {
				if (errorDateFrom == null && errorDateTo != null) {
					if (dateSqlQuery.contains(WHERE)) {
						dateSqlQuery += SPACE_AND_SPACE;
					} else {
						dateSqlQuery += SPACE_WHERE_SPACE;
					}
					dateSqlQuery += " ERROR_DATE <= " + "'"
							+ DateTimeFormatter.ofPattern(MM_DD_YYYY).format(errorDateTo) + "'";
				}
				if (errorDateFrom != null && errorDateTo == null) {
					if (dateSqlQuery.contains(WHERE)) {
						dateSqlQuery += SPACE_AND_SPACE;
					} else {
						dateSqlQuery += SPACE_WHERE_SPACE;
					}
					dateSqlQuery += " ERROR_DATE >= " + "'"
							+ DateTimeFormatter.ofPattern(MM_DD_YYYY).format(errorDateFrom) + "'";
				}
				if (errorDateFrom == null && errorDateTo == null) {
					dateSqlQuery += "";
				}
			}

			if (solvingDateFrom != null && solvingDateTo != null) {
				if (dateSqlQuery.contains(WHERE)) {
					dateSqlQuery += SPACE_AND_SPACE;
				} else {
					dateSqlQuery += SPACE_WHERE_SPACE;
				}
				dateSqlQuery += " SOLVING_DATE >=" + "'"
						+ DateTimeFormatter.ofPattern(MM_DD_YYYY).format(solvingDateFrom) + "'"
						+ " AND SOLVING_DATE <= " + "'" + DateTimeFormatter.ofPattern(MM_DD_YYYY).format(solvingDateTo)
						+ "'";
			} else {
				if (solvingDateFrom == null && solvingDateTo != null) {
					if (dateSqlQuery.contains(WHERE)) {
						dateSqlQuery += SPACE_AND_SPACE;
					} else {
						dateSqlQuery += SPACE_WHERE_SPACE;
					}
					dateSqlQuery += " SOLVING_DATE <= " + "'"
							+ DateTimeFormatter.ofPattern(MM_DD_YYYY).format(solvingDateTo) + "'";
				}
				if (solvingDateFrom != null && solvingDateTo == null) {
					if (dateSqlQuery.contains(WHERE)) {
						dateSqlQuery += SPACE_AND_SPACE;
					} else {
						dateSqlQuery += SPACE_WHERE_SPACE;
					}
					dateSqlQuery += " SOLVING_DATE >= " + "'"
							+ DateTimeFormatter.ofPattern(MM_DD_YYYY).format(solvingDateFrom) + "'";
				}
				if (solvingDateFrom == null && solvingDateTo == null) {
					dateSqlQuery += "";
				}
			}

			sqlQuery += dateSqlQuery;

			String posDefSqlQuery = "";

			if (positionDefinitionId != 0) {
				if (sqlQuery.contains(WHERE)) {
					posDefSqlQuery = " AND";
				} else {
					posDefSqlQuery = SPACE_WHERE_SPACE;
				}
				posDefSqlQuery += " POSITION_DEFINITION_ID = " + positionDefinitionId;
			}

			sqlQuery += posDefSqlQuery;

			String statusSqlQuery = "";

			if (status != null) {
				if (sqlQuery.contains(WHERE)) {
					statusSqlQuery = " AND";
				} else {
					statusSqlQuery = " WHERE";
				}
				statusSqlQuery += " STATUS = '" + status.name() + "'";
			}

			sqlQuery += statusSqlQuery;

			String tradeIdSqlQuery = "";

			if (tradeId > 0) {
				if (sqlQuery.contains(WHERE)) {
					tradeIdSqlQuery = " AND";
				} else {
					tradeIdSqlQuery = " WHERE";
				}
				tradeIdSqlQuery += " TRADE_ID = " + tradeId;
			}

			sqlQuery += tradeIdSqlQuery;

			String productIdSqlQuery = "";

			if (productId > 0) {
				if (sqlQuery.contains(WHERE)) {
					productIdSqlQuery = " AND";
				} else {
					productIdSqlQuery = " WHERE";
				}
				productIdSqlQuery += " TRADE_ID = " + tradeId;
			}

			sqlQuery += productIdSqlQuery;

			String joinSqlQuery = "";

			if (sqlQuery.contains(WHERE)) {
				joinSqlQuery = " AND";
			} else {
				joinSqlQuery = " WHERE";
			}
			joinSqlQuery += " ID = ERROR_ID";

			sqlQuery += joinSqlQuery;

			try (ResultSet results = stmtGetPositionCalculationErrors.executeQuery(sqlQuery)) {
				while (results.next()) {
					if (positionCalculationErrors == null) {
						positionCalculationErrors = new ArrayList<>();
					}
					PositionCalculationError positionCalculationError = new PositionCalculationError();
					positionCalculationError.setId(results.getLong("id"));
					positionCalculationError.setMessage(results.getString("message"));
					Timestamp solvingDate = results.getTimestamp("solving_date");
					if (solvingDate != null) {
						positionCalculationError.setSolvingDate(solvingDate.toLocalDateTime());
					}
					positionCalculationError.setErrorDate(results.getTimestamp("error_date").toLocalDateTime());
					positionCalculationError.setValueDate(results.getDate("value_date").toLocalDate());
					positionCalculationError.setStatus(Status.valueOf(results.getString("status")));
					if (results.getLong("trade_id") > 0) {
						positionCalculationError.setTrade(TradeSQL.getTradeById(results.getLong("trade_id"), false));
					}
					if (results.getLong("product_id") > 0) {
						positionCalculationError.setProduct(ProductSQL.getProductById(results.getLong("product_id")));
					}
					positionCalculationError.setPositionDefinition(
							PositionDefinitionSQL.getPositionDefinitionById(results.getLong("position_definition_id")));
					positionCalculationErrors.add(positionCalculationError);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return positionCalculationErrors;
	}

	public static void deleteErrors(Set<Long> ids) {
		if (ids != null && !ids.isEmpty()) {
			try (Connection con = TradistaDB.getConnection();
					PreparedStatement stmtDeleteErrors = con
							.prepareStatement("DELETE FROM POSITION_CALCULATION_ERROR WHERE ERROR_ID = ?")) {
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

}