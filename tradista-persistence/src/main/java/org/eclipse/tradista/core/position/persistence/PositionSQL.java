package org.eclipse.tradista.core.position.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.position.model.Position;

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

public class PositionSQL {

	public static long savePosition(Position position) {
		ConfigurationBusinessDelegate cbs = new ConfigurationBusinessDelegate();
		// 1. Check if the position already exists
		boolean exists = position.getId() != 0;
		long positionId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSavePosition = (!exists) ? con.prepareStatement(
						"INSERT INTO POSITION(POSITION_DEFINITION_ID, PNL, REALIZED_PNL, UNREALIZED_PNL, QUANTITY, AVERAGE_PRICE, VALUE_DATETIME) VALUES(?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE POSITION SET POSITION_DEFINITION_ID=?, PNL=?, REALIZED_PNL=?, UNREALIZED_PNL=?, QUANTITY=?, AVERAGE_PRICE=?, VALUE_DATETIME=? WHERE ID=?")) {
			stmtSavePosition.setLong(1, position.getPositionDefinition().getId());
			stmtSavePosition.setBigDecimal(2, position.getPnl().setScale(cbs.getScale(), cbs.getRoundingMode()));
			stmtSavePosition.setBigDecimal(3,
					position.getRealizedPnl().setScale(cbs.getScale(), cbs.getRoundingMode()));
			stmtSavePosition.setBigDecimal(4,
					position.getUnrealizedPnl().setScale(cbs.getScale(), cbs.getRoundingMode()));
			if (position.getQuantity() != null) {
				stmtSavePosition.setBigDecimal(5,
						position.getQuantity().setScale(cbs.getScale(), cbs.getRoundingMode()));
			} else {
				stmtSavePosition.setNull(5, Types.DECIMAL);
			}
			if (position.getAveragePrice() != null) {
				stmtSavePosition.setBigDecimal(6,
						position.getAveragePrice().setScale(cbs.getScale(), cbs.getRoundingMode()));
			} else {
				stmtSavePosition.setNull(6, Types.DECIMAL);
			}
			stmtSavePosition.setTimestamp(7, Timestamp.valueOf(position.getValueDateTime()));
			if (!exists) {
				// 2. If the position doesn't exist, save it
				stmtSavePosition.executeUpdate();
				try (ResultSet generatedKeys = stmtSavePosition.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						positionId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating position failed, no generated key obtained.");
					}
				}
			} else {
				// The position exists, so we update it
				stmtSavePosition.setLong(8, position.getId());
				stmtSavePosition.executeUpdate();
				positionId = position.getId();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return positionId;
	}

	public static void savePositions(List<Position> positions) {
		ConfigurationBusinessDelegate cbs = new ConfigurationBusinessDelegate();
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSavePosition = con.prepareStatement(
						"INSERT INTO POSITION(POSITION_DEFINITION_ID, PNL, REALIZED_PNL, UNREALIZED_PNL, QUANTITY, AVERAGE_PRICE, VALUE_DATETIME) VALUES(?, ?, ?, ?, ?, ?, ?)");
				PreparedStatement stmtUpdatePosition = con.prepareStatement(
						"UPDATE POSITION SET POSITION_DEFINITION_ID=?, PNL=?, REALIZED_PNL=?, UNREALIZED_PNL=?, QUANTITY=?, AVERAGE_PRICE=?, VALUE_DATETIME=? WHERE ID=?")) {
			for (Position position : positions) {
				// 1. Check if the position already exists
				boolean exists = position.getId() != 0;
				if (!exists) {
					// 2. If the position doesn't exist, save it
					stmtSavePosition.setLong(1, position.getPositionDefinition().getId());
					stmtSavePosition.setBigDecimal(2,
							position.getPnl().setScale(cbs.getScale(), cbs.getRoundingMode()));
					stmtSavePosition.setBigDecimal(3,
							position.getRealizedPnl().setScale(cbs.getScale(), cbs.getRoundingMode()));
					stmtSavePosition.setBigDecimal(4,
							position.getUnrealizedPnl().setScale(cbs.getScale(), cbs.getRoundingMode()));
					if (position.getQuantity() != null) {
						stmtSavePosition.setBigDecimal(5,
								position.getQuantity().setScale(cbs.getScale(), cbs.getRoundingMode()));
					} else {
						stmtSavePosition.setNull(5, Types.DECIMAL);
					}
					if (position.getAveragePrice() != null) {
						stmtSavePosition.setBigDecimal(6,
								position.getAveragePrice().setScale(cbs.getScale(), cbs.getRoundingMode()));
					} else {
						stmtSavePosition.setNull(6, Types.DECIMAL);
					}
					stmtSavePosition.setTimestamp(7, Timestamp.valueOf(position.getValueDateTime()));
					stmtSavePosition.addBatch();
				} else {
					// The position exists, so we update it
					stmtUpdatePosition.setLong(1, position.getPositionDefinition().getId());
					stmtUpdatePosition.setBigDecimal(2,
							position.getPnl().setScale(cbs.getScale(), cbs.getRoundingMode()));
					stmtUpdatePosition.setBigDecimal(3,
							position.getRealizedPnl().setScale(cbs.getScale(), cbs.getRoundingMode()));
					stmtUpdatePosition.setBigDecimal(4,
							position.getUnrealizedPnl().setScale(cbs.getScale(), cbs.getRoundingMode()));
					if (position.getQuantity() != null) {
						stmtUpdatePosition.setBigDecimal(5,
								position.getQuantity().setScale(cbs.getScale(), cbs.getRoundingMode()));
					} else {
						stmtUpdatePosition.setNull(5, Types.DECIMAL);
					}
					if (position.getAveragePrice() != null) {
						stmtUpdatePosition.setBigDecimal(6,
								position.getAveragePrice().setScale(cbs.getScale(), cbs.getRoundingMode()));
					} else {
						stmtUpdatePosition.setNull(6, Types.DECIMAL);
					}
					stmtUpdatePosition.setTimestamp(7, Timestamp.valueOf(position.getValueDateTime()));
					stmtUpdatePosition.setLong(8, position.getId());
					stmtUpdatePosition.addBatch();
				}
			}
			stmtSavePosition.executeBatch();
			stmtUpdatePosition.executeBatch();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static List<Position> getPositionsByDefinitionAndValueDates(long positionDefinitionId,
			LocalDate valueDateFrom, LocalDate valueDateTo) {
		List<Position> positions = null;

		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetPositionsByDefinitionAndValueDates = con.createStatement()) {
			String sqlQuery = "SELECT * FROM POSITION ";
			String dateSqlQuery = "";
			if (valueDateFrom != null && valueDateTo != null) {
				dateSqlQuery = " WHERE VALUE_DATETIME >=" + "'"
						+ DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
								.format(LocalDateTime.of(valueDateFrom, LocalTime.MIN))
						+ "'" + " AND VALUE_DATETIME <= " + "'" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
								.format(LocalDateTime.of(valueDateTo, LocalTime.MAX))
						+ "'";
			} else {
				if (valueDateFrom == null && valueDateTo != null) {
					dateSqlQuery = " WHERE VALUE_DATETIME <= " + "'" + DateTimeFormatter
							.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.of(valueDateTo, LocalTime.MAX))
							+ "'";
				}
				if (valueDateFrom != null && valueDateTo == null) {
					dateSqlQuery = " WHERE VALUE_DATETIME >= " + "'" + DateTimeFormatter
							.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.of(valueDateFrom, LocalTime.MIN))
							+ "'";
				}
				if (valueDateFrom == null && valueDateTo == null) {
					dateSqlQuery = "";
				}
			}
			String posDefSqlQuery = "";
			if (positionDefinitionId != 0) {
				if (!dateSqlQuery.isEmpty()) {
					posDefSqlQuery = " AND";
				} else {
					posDefSqlQuery = " WHERE";
				}
				posDefSqlQuery += " POSITION_DEFINITION_ID = " + positionDefinitionId;
			}
			sqlQuery = sqlQuery + dateSqlQuery + posDefSqlQuery;
			try (ResultSet results = stmtGetPositionsByDefinitionAndValueDates.executeQuery(sqlQuery)) {
				while (results.next()) {
					if (positions == null) {
						positions = new ArrayList<Position>();
					}
					Position position = new Position();
					position.setId(results.getLong("id"));
					position.setPnl(results.getBigDecimal("pnl"));
					position.setRealizedPnl(results.getBigDecimal("realized_pnl"));
					position.setUnrealizedPnl(results.getBigDecimal("unrealized_pnl"));
					position.setQuantity(results.getBigDecimal("quantity"));
					position.setAveragePrice(results.getBigDecimal("average_price"));
					position.setPositionDefinition(
							PositionDefinitionSQL.getPositionDefinitionById(results.getLong("position_definition_id")));
					position.setValueDateTime(results.getTimestamp("value_datetime").toLocalDateTime());
					positions.add(position);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return positions;
	}

	public static Position getLastPositionByDefinitionNameAndValueDate(String positionDefinitionName,
			LocalDate valueDate) {
		Position position = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetLastPositionByDefinitionNameAndValueDate = con.prepareStatement(
						"SELECT * FROM POSITION WHERE VALUE_DATETIME = (SELECT MAX(VALUE_DATETIME) FROM POSITION WHERE POSITION_DEFINITION_ID IN (SELECT ID FROM POSITION_DEFINITION WHERE NAME = ?)"
								+ " AND VALUE_DATETIME >= ? AND VALUE_DATETIME < ?) ORDER BY ID DESC")) {
			stmtGetLastPositionByDefinitionNameAndValueDate.setString(1, positionDefinitionName);
			stmtGetLastPositionByDefinitionNameAndValueDate.setDate(2, Date.valueOf(valueDate));
			stmtGetLastPositionByDefinitionNameAndValueDate.setDate(3, Date.valueOf(valueDate.plusDays(1)));
			try (ResultSet results = stmtGetLastPositionByDefinitionNameAndValueDate.executeQuery()) {
				while (results.next()) {
					position = new Position();
					position.setId(results.getLong("id"));
					position.setPnl(results.getBigDecimal("pnl"));
					position.setRealizedPnl(results.getBigDecimal("realized_pnl"));
					position.setUnrealizedPnl(results.getBigDecimal("unrealized_pnl"));
					position.setQuantity(results.getBigDecimal("quantity"));
					position.setAveragePrice(results.getBigDecimal("average_price"));
					position.setPositionDefinition(
							PositionDefinitionSQL.getPositionDefinitionById(results.getLong("position_definition_id")));
					position.setValueDateTime(results.getTimestamp("value_datetime").toLocalDateTime());
					break;
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return position;

	}

}