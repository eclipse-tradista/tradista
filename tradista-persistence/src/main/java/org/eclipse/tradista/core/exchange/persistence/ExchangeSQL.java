package org.eclipse.tradista.core.exchange.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CALENDAR_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CODE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tradista.core.calendar.persistence.CalendarSQL;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
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

	public static final Field ID_FIELD = new Field(ID);
	public static final Field CODE_FIELD = new Field(CODE);
	private static final Field NAME_FIELD = new Field(NAME);
	private static final Field IS_OTC_FIELD = new Field("IS_OTC");
	private static final Field CALENDAR_ID_FIELD = new Field(CALENDAR_ID);

	private static final Field[] EXCHANGE_FIELDS = { ID_FIELD, CODE_FIELD, NAME_FIELD, IS_OTC_FIELD,
			CALENDAR_ID_FIELD };
	private static final Field[] EXCHANGE_FIELDS_FOR_INSERT = { CODE_FIELD, NAME_FIELD, IS_OTC_FIELD,
			CALENDAR_ID_FIELD };
	private static final Field[] EXCHANGE_FIELDS_FOR_UPDATE = { NAME_FIELD, IS_OTC_FIELD, CALENDAR_ID_FIELD };

	public static final Table EXCHANGE_TABLE = new Table("EXCHANGE", EXCHANGE_FIELDS);

	private static final String SELECT_QUERY = TradistaDBUtil.buildSelectQuery(EXCHANGE_TABLE);

	public static Exchange getExchangeById(long id) {
		Exchange exchange = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetExchangeById = con.prepareStatement(sqlQuery.toString())) {
			stmtGetExchangeById.setLong(1, id);
			try (ResultSet results = stmtGetExchangeById.executeQuery()) {
				while (results.next()) {
					exchange = buildExchange(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return exchange;
	}

	public static Exchange getExchangeByCode(String code) {
		Exchange exchange = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, CODE_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetExchangeByCode = con.prepareStatement(sqlQuery.toString())) {
			stmtGetExchangeByCode.setString(1, code);
			try (ResultSet results = stmtGetExchangeByCode.executeQuery()) {
				while (results.next()) {
					exchange = buildExchange(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return exchange;
	}

	public static Exchange getExchangeByName(String name) {
		Exchange exchange = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, NAME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetExchangeByName = con.prepareStatement(sqlQuery.toString())) {
			stmtGetExchangeByName.setString(1, name);
			try (ResultSet results = stmtGetExchangeByName.executeQuery()) {
				while (results.next()) {
					exchange = buildExchange(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return exchange;
	}

	public static Set<Exchange> getAllExchanges() {
		Set<Exchange> exchanges = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllExchanges = con.prepareStatement(SELECT_QUERY);
				ResultSet results = stmtGetAllExchanges.executeQuery()) {
			while (results.next()) {
				if (exchanges == null) {
					exchanges = new TreeSet<>();
				}
				exchanges.add(buildExchange(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return exchanges;
	}

	public static long saveExchange(Exchange exchange) {
		long id = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveExchange = (exchange.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, EXCHANGE_TABLE, EXCHANGE_FIELDS_FOR_INSERT)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, CODE_FIELD, EXCHANGE_TABLE,
								EXCHANGE_FIELDS_FOR_UPDATE)) {
			if (exchange.getId() == 0) {
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
						throw new SQLException("Creating exchange failed, no generated key obtained.");
					}
				}
			} else {
				id = exchange.getId();
				stmtSaveExchange.setString(1, exchange.getName());
				stmtSaveExchange.setBoolean(2, exchange.isOtc());
				if (exchange.getCalendar() != null) {
					stmtSaveExchange.setLong(3, exchange.getCalendar().getId());
				} else {
					stmtSaveExchange.setNull(3, Types.BIGINT);
				}
				stmtSaveExchange.setString(4, exchange.getCode());
				stmtSaveExchange.executeUpdate();
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		exchange.setId(id);
		return id;
	}

	private static Exchange buildExchange(ResultSet results) throws SQLException {
		Exchange exchange = new Exchange(results.getString(CODE_FIELD.getName()));
		exchange.setId(results.getLong(ID_FIELD.getName()));
		exchange.setName(results.getString(NAME_FIELD.getName()));
		exchange.setOtc(results.getBoolean(IS_OTC_FIELD.getName()));
		exchange.setCalendar(CalendarSQL.getCalendarById(results.getLong(CALENDAR_ID_FIELD.getName())));
		return exchange;
	}

}