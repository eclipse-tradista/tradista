package org.eclipse.tradista.core.dailypnl.persistence;

import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.calendar.persistence.CalendarSQL;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.dailypnl.model.DailyPnl;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.persistence.PositionDefinitionSQL;

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

public class DailyPnlSQL {

	public static long saveDailyPnl(DailyPnl dailyPnl) {

		ConfigurationBusinessDelegate configurationBusinessDelegate = new ConfigurationBusinessDelegate();

		// 1. Check if the daily pnl already exists

		boolean exists = dailyPnl.getId() != 0;

		long dailyPnlId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveDailyPnl = (!exists) ? con.prepareStatement(
						"INSERT INTO DAILY_PNL(POSITION_DEFINITION_ID, CALENDAR_ID, PNL, REALIZED_PNL, UNREALIZED_PNL, VALUE_DATE) VALUES(?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE DAILY_PNL SET POSITION_DEFINITION_ID=?, CALENDAR_ID=?, PNL=?, REALIZED_PNL=?, UNREALIZED_PNL=?, VALUE_DATE=? WHERE ID=?")) {
			short scale = configurationBusinessDelegate.getScale();
			RoundingMode roundingMode = configurationBusinessDelegate.getRoundingMode();
			if (!exists) {
				// 3. If the position doesn't exist, save it
				stmtSaveDailyPnl.setLong(1, dailyPnl.getPositionDefinition().getId());
				stmtSaveDailyPnl.setLong(2, dailyPnl.getCalendar().getId());
				// Derby does not support decimal with a
				// precision greater than 31.
				stmtSaveDailyPnl.setBigDecimal(3, dailyPnl.getPnl().setScale(scale, roundingMode));
				stmtSaveDailyPnl.setBigDecimal(4, dailyPnl.getRealizedPnl().setScale(scale, roundingMode));
				stmtSaveDailyPnl.setBigDecimal(5, dailyPnl.getUnrealizedPnl().setScale(scale, roundingMode));
				stmtSaveDailyPnl.setDate(6, Date.valueOf(dailyPnl.getValueDate()));
				stmtSaveDailyPnl.executeUpdate();
				try (ResultSet generatedKeys = stmtSaveDailyPnl.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						dailyPnlId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating daily pnl failed, no generated key obtained.");
					}
				}
			} else {
				// The position exists, so we update it
				stmtSaveDailyPnl.setLong(1, dailyPnl.getPositionDefinition().getId());
				stmtSaveDailyPnl.setLong(2, dailyPnl.getCalendar().getId());
				// Derby does not support decimal with a
				// precision greater than 31.
				stmtSaveDailyPnl.setBigDecimal(3, dailyPnl.getPnl().setScale(scale, roundingMode));
				stmtSaveDailyPnl.setBigDecimal(4, dailyPnl.getRealizedPnl().setScale(scale, roundingMode));
				stmtSaveDailyPnl.setBigDecimal(5, dailyPnl.getUnrealizedPnl().setScale(scale, roundingMode));
				stmtSaveDailyPnl.setDate(6, Date.valueOf(dailyPnl.getValueDate()));
				stmtSaveDailyPnl.setLong(7, dailyPnl.getId());

				stmtSaveDailyPnl.executeUpdate();

				dailyPnlId = dailyPnl.getId();

			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		dailyPnl.setId(dailyPnlId);
		return dailyPnlId;
	}

	public static DailyPnl getDailyPnlByPositionDefinitionCalendarAndValueDate(PositionDefinition positionDefinition,
			Calendar calendar, LocalDate valueDate) {
		DailyPnl dailyPnl = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetDailyPnlByPositionDefinitionCalendarAndValueDate = con.prepareStatement(
						"SELECT * FROM DAILY_PNL WHERE POSITION_DEFINITION_ID =? AND CALENDAR_ID=? AND VALUE_DATE=?")) {
			stmtGetDailyPnlByPositionDefinitionCalendarAndValueDate.setLong(1, positionDefinition.getId());
			stmtGetDailyPnlByPositionDefinitionCalendarAndValueDate.setLong(2, calendar.getId());
			stmtGetDailyPnlByPositionDefinitionCalendarAndValueDate.setDate(3, Date.valueOf(valueDate));
			try (ResultSet results = stmtGetDailyPnlByPositionDefinitionCalendarAndValueDate.executeQuery()) {
				while (results.next()) {
					dailyPnl = new DailyPnl(
							PositionDefinitionSQL.getPositionDefinitionById(results.getLong("position_definition_id")),
							CalendarSQL.getCalendarById(results.getLong("calendar_id")),
							results.getDate("value_date").toLocalDate());
					dailyPnl.setId(results.getLong("id"));
					dailyPnl.setPnl(results.getBigDecimal("pnl"));
					dailyPnl.setRealizedPnl(results.getBigDecimal("realized_pnl"));
					dailyPnl.setUnrealizedPnl(results.getBigDecimal("unrealized_pnl"));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return dailyPnl;
	}

	public static Set<DailyPnl> getDailyPnlsByPositionDefinitionCalendarAndValueDate(long positionDefinitionId,
			String calendarCode, LocalDate valueDateFrom, LocalDate valueDateTo) {
		Set<DailyPnl> dailyPnls = null;
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetDailyPnlsByPositionDefinitionCalendarAndValueDate = con.createStatement()) {
			String sqlQuery = "SELECT * FROM DAILY_PNL";

			if (positionDefinitionId != 0) {
				sqlQuery += " WHERE POSITION_DEFINITION_ID =" + positionDefinitionId;
			}

			if (!StringUtils.isEmpty(calendarCode)) {
				if (sqlQuery.contains("WHERE")) {
					sqlQuery += " AND ";
				} else {
					sqlQuery += " WHERE ";
				}
				sqlQuery += " CALENDAR_ID IN (SELECT ID FROM CALENDAR WHERE CODE='" + calendarCode + "')";
			}
			String dateSqlQuery = "";
			if (valueDateFrom != null && valueDateTo != null) {
				if (sqlQuery.contains("WHERE")) {
					dateSqlQuery += " AND ";
				} else {
					dateSqlQuery += " WHERE ";
				}
				dateSqlQuery += " VALUE_DATE >=" + "'" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(valueDateFrom)
						+ "'" + " AND VALUE_DATE <= " + "'"
						+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(valueDateTo) + "'";
			} else {
				if (valueDateFrom == null && valueDateTo != null) {
					if (sqlQuery.contains("WHERE")) {
						dateSqlQuery += " AND ";
					} else {
						dateSqlQuery += " WHERE ";
					}
					dateSqlQuery += " VALUE_DATE <= " + "'"
							+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(valueDateTo) + "'";
				}
				if (valueDateFrom != null && valueDateTo == null) {
					if (sqlQuery.contains("WHERE")) {
						dateSqlQuery += " AND ";
					} else {
						dateSqlQuery += " WHERE ";
					}
					dateSqlQuery += " VALUE_DATE >= " + "'"
							+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(valueDateFrom) + "'";
				}
				if (valueDateFrom == null && valueDateTo == null) {
					dateSqlQuery = "";
				}
			}
			sqlQuery += dateSqlQuery;
			try (ResultSet results = stmtGetDailyPnlsByPositionDefinitionCalendarAndValueDate.executeQuery(sqlQuery)) {
				while (results.next()) {
					if (dailyPnls == null) {
						dailyPnls = new HashSet<DailyPnl>();
					}
					DailyPnl dailyPnl = new DailyPnl(
							PositionDefinitionSQL.getPositionDefinitionById(results.getLong("position_definition_id")),
							CalendarSQL.getCalendarById(results.getLong("calendar_id")),
							results.getDate("value_date").toLocalDate());
					dailyPnl.setId(results.getLong("id"));
					dailyPnl.setPnl(results.getBigDecimal("pnl"));
					dailyPnl.setRealizedPnl(results.getBigDecimal("realized_pnl"));
					dailyPnl.setUnrealizedPnl(results.getBigDecimal("unrealized_pnl"));
					dailyPnls.add(dailyPnl);
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return dailyPnls;
	}

}