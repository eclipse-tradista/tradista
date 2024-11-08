package org.eclipse.tradista.core.exchange.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tradista.core.calendar.persistence.CalendarSQL;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.exchange.model.Exchange;

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

public class ExchangeSQL {

	public static Exchange getExchangeById(long id) {
		Exchange exchange = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetExchangeById = con
						.prepareStatement("SELECT * FROM EXCHANGE WHERE EXCHANGE.ID = ? ")) {
			stmtGetExchangeById.setLong(1, id);
			try (ResultSet results = stmtGetExchangeById.executeQuery()) {
				while (results.next()) {
					exchange = new Exchange(results.getString("code"));
					exchange.setId(results.getLong("id"));
					exchange.setName(results.getString("name"));
					exchange.setOtc(results.getBoolean("is_otc"));
					exchange.setCalendar(CalendarSQL.getCalendarById(results.getLong("calendar_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return exchange;
	}

	public static Exchange getExchangeByCode(String code) {
		Exchange exchange = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetExchangeByCode = con
						.prepareStatement("SELECT * FROM EXCHANGE WHERE EXCHANGE.CODE = ? ")) {
			stmtGetExchangeByCode.setString(1, code);
			try (ResultSet results = stmtGetExchangeByCode.executeQuery()) {
				while (results.next()) {
					exchange = new Exchange(results.getString("code"));
					exchange.setId(results.getLong("id"));
					exchange.setName(results.getString("name"));
					exchange.setOtc(results.getBoolean("is_otc"));
					exchange.setCalendar(CalendarSQL.getCalendarById(results.getLong("calendar_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return exchange;
	}

	public static Exchange getExchangeByName(String name) {
		Exchange exchange = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetExchangeByName = con
						.prepareStatement("SELECT * FROM EXCHANGE WHERE NAME = ? ")) {
			stmtGetExchangeByName.setString(1, name);
			try (ResultSet results = stmtGetExchangeByName.executeQuery()) {
				while (results.next()) {
					exchange = new Exchange(results.getString("code"));
					exchange.setId(results.getLong("id"));
					exchange.setName(results.getString("name"));
					exchange.setOtc(results.getBoolean("is_otc"));
					exchange.setCalendar(CalendarSQL.getCalendarById(results.getLong("calendar_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return exchange;
	}

	public static Set<Exchange> getAllExchanges() {
		Set<Exchange> exchanges = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllExchanges = con.prepareStatement("SELECT * FROM EXCHANGE");
				ResultSet results = stmtGetAllExchanges.executeQuery()) {
			while (results.next()) {
				if (exchanges == null) {
					exchanges = new TreeSet<Exchange>();
				}
				Exchange exchange = new Exchange(results.getString("code"));
				exchange.setId(results.getLong("id"));
				exchange.setName(results.getString("name"));
				exchange.setOtc(results.getBoolean("is_otc"));
				exchange.setCalendar(CalendarSQL.getCalendarById(results.getLong("calendar_id")));
				exchanges.add(exchange);
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return exchanges;
	}

	public static long saveExchange(Exchange exchange) {
		long id = 0;
		try {
			try (Connection con = TradistaDB.getConnection()) {
				boolean exists = exchange.getId() != 0;
				if (!exists) {
					// The exchange didn't exist
					try (PreparedStatement stmtSaveExchange = con.prepareStatement(
							"INSERT INTO EXCHANGE(CODE, NAME, IS_OTC, CALENDAR_ID) VALUES (?, ?, ?, ?) ",
							Statement.RETURN_GENERATED_KEYS)) {
						stmtSaveExchange.setString(1, exchange.getCode());
						stmtSaveExchange.setString(2, exchange.getName());
						stmtSaveExchange.setBoolean(3, exchange.isOtc());
						if (exchange.getCalendar() != null) {
							stmtSaveExchange.setLong(4, exchange.getCalendar().getId());
						} else {
							stmtSaveExchange.setNull(4, Types.BIGINT);
						}
						stmtSaveExchange.executeUpdate();
						try (ResultSet generatedKeys = stmtSaveExchange.getGeneratedKeys()) {
							if (generatedKeys.next()) {
								id = generatedKeys.getLong(1);
							} else {
								throw new SQLException("Creating user failed, no generated key obtained.");
							}
						}
					}
				} else {
					id = exchange.getId();
					try (PreparedStatement stmtUpdateExchange = con.prepareStatement(
							"UPDATE EXCHANGE SET NAME = ?, IS_OTC = ?, CALENDAR_ID = ? WHERE CODE = ? ",
							Statement.RETURN_GENERATED_KEYS)) {
						stmtUpdateExchange.setString(1, exchange.getName());
						stmtUpdateExchange.setBoolean(2, exchange.isOtc());
						if (exchange.getCalendar() != null) {
							stmtUpdateExchange.setLong(3, exchange.getCalendar().getId());
						} else {
							stmtUpdateExchange.setNull(3, Types.BIGINT);
						}
						stmtUpdateExchange.setString(4, exchange.getCode());
						stmtUpdateExchange.executeUpdate();
					}
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		exchange.setId(id);
		return id;
	}

}