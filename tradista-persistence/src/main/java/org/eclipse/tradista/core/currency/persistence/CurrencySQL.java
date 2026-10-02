package org.eclipse.tradista.core.currency.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CALENDAR_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.calendar.persistence.CalendarSQL;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.currency.model.Currency;

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

public class CurrencySQL {

	public static final Field ID_FIELD = new Field(ID);
	public static final Field ISO_CODE_FIELD = new Field("ISO_CODE");
	private static final Field NAME_FIELD = new Field(NAME);
	private static final Field NON_DELIVERABLE_FIELD = new Field("NON_DELIVERABLE");
	private static final Field FIXING_DATE_OFFSET_FIELD = new Field("FIXING_DATE_OFFSET");
	private static final Field CALENDAR_ID_FIELD = new Field(CALENDAR_ID);

	private static final Field[] CURRENCY_FIELDS = { ID_FIELD, ISO_CODE_FIELD, NAME_FIELD, NON_DELIVERABLE_FIELD,
			FIXING_DATE_OFFSET_FIELD, CALENDAR_ID_FIELD };
	private static final Field[] CURRENCY_FIELDS_FOR_INSERT = { ISO_CODE_FIELD, NAME_FIELD, NON_DELIVERABLE_FIELD,
			FIXING_DATE_OFFSET_FIELD, CALENDAR_ID_FIELD };
	private static final Field[] CURRENCY_FIELDS_FOR_UPDATE = { ISO_CODE_FIELD, NAME_FIELD, NON_DELIVERABLE_FIELD,
			FIXING_DATE_OFFSET_FIELD, CALENDAR_ID_FIELD };

	public static final Table CURRENCY_TABLE = new Table("CURRENCY", CURRENCY_FIELDS);

	private static final String SELECT_QUERY = TradistaDBUtil.buildSelectQuery(CURRENCY_TABLE);

	public static Currency getCurrencyById(long id) {
		Currency currency = null;
		StringBuilder sql = new StringBuilder(SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCurrencyById = con.prepareStatement(sql.toString())) {
			stmtGetCurrencyById.setLong(1, id);
			try (ResultSet results = stmtGetCurrencyById.executeQuery()) {
				while (results.next()) {
					currency = buildCurrency(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return currency;
	}

	public static Currency getCurrencyByName(String name) {
		Currency currency = null;
		StringBuilder sql = new StringBuilder(SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCurrencyByName = con.prepareStatement(sql.toString())) {
			stmtGetCurrencyByName.setString(1, name);
			try (ResultSet results = stmtGetCurrencyByName.executeQuery()) {
				while (results.next()) {
					currency = buildCurrency(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return currency;
	}

	public static Set<Currency> getAllCurrencies() {
		Set<Currency> currencies = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllCurrencies = con.prepareStatement(SELECT_QUERY);
				ResultSet results = stmtGetAllCurrencies.executeQuery()) {
			while (results.next()) {
				if (currencies == null) {
					currencies = new HashSet<>();
				}
				currencies.add(buildCurrency(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return currencies;
	}

	public static long saveCurrency(Currency currency) {
		long currencyId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveCurrency = (currency.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, CURRENCY_TABLE, CURRENCY_FIELDS_FOR_INSERT)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, CURRENCY_TABLE,
								CURRENCY_FIELDS_FOR_UPDATE)) {
			stmtSaveCurrency.setString(1, currency.getIsoCode());
			stmtSaveCurrency.setString(2, currency.getName());
			stmtSaveCurrency.setBoolean(3, currency.isNonDeliverable());
			if (currency.isNonDeliverable()) {
				stmtSaveCurrency.setInt(4, currency.getFixingDateOffset());
			} else {
				stmtSaveCurrency.setNull(4, Types.INTEGER);
			}
			if (currency.getCalendar() != null) {
				stmtSaveCurrency.setLong(5, currency.getCalendar().getId());
			} else {
				stmtSaveCurrency.setNull(5, Types.BIGINT);
			}
			if (currency.getId() != 0) {
				stmtSaveCurrency.setLong(6, currency.getId());
			}
			stmtSaveCurrency.executeUpdate();

			if (currency.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveCurrency.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						currencyId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating currency failed, no generated key obtained.");
					}
				}
			} else {
				currencyId = currency.getId();
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		currency.setId(currencyId);
		return currencyId;
	}

	public static boolean currencyExists(String isoCode) {
		boolean exists = false;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(ID_FIELD, CURRENCY_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, ISO_CODE_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtCurrencyExists = con.prepareStatement(sql.toString())) {
			stmtCurrencyExists.setString(1, isoCode);
			try (ResultSet results = stmtCurrencyExists.executeQuery()) {
				if (results.next()) {
					exists = true;
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return exists;
	}

	public static Currency getCurrencyByIsoCode(String isoCode) {
		Currency currency = null;
		StringBuilder sql = new StringBuilder(SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, ISO_CODE_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCurrencyByIsoCode = con.prepareStatement(sql.toString())) {
			stmtGetCurrencyByIsoCode.setString(1, isoCode);
			try (ResultSet results = stmtGetCurrencyByIsoCode.executeQuery()) {
				while (results.next()) {
					currency = buildCurrency(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return currency;
	}

	public static Set<Currency> getDeliverableCurrencies() {
		Set<Currency> currencies = null;
		StringBuilder sql = new StringBuilder(SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, NON_DELIVERABLE_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetDeliverableCurrencies = con.prepareStatement(sql.toString())) {
			stmtGetDeliverableCurrencies.setBoolean(1, false);
			try (ResultSet results = stmtGetDeliverableCurrencies.executeQuery()) {
				while (results.next()) {
					if (currencies == null) {
						currencies = new HashSet<>();
					}
					currencies.add(buildCurrency(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return currencies;
	}

	public static Set<Currency> getNonDeliverableCurrencies() {
		Set<Currency> currencies = null;
		StringBuilder sql = new StringBuilder(SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, NON_DELIVERABLE_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetNonDeliverableCurrencies = con.prepareStatement(sql.toString())) {
			stmtGetNonDeliverableCurrencies.setBoolean(1, true);
			try (ResultSet results = stmtGetNonDeliverableCurrencies.executeQuery()) {
				while (results.next()) {
					if (currencies == null) {
						currencies = new HashSet<>();
					}
					currencies.add(buildCurrency(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return currencies;
	}

	private static Currency buildCurrency(ResultSet results) throws SQLException {
		Currency currency = new Currency(results.getString(ISO_CODE_FIELD.getName()));
		currency.setId(results.getLong(ID_FIELD.getName()));
		currency.setName(results.getString(NAME_FIELD.getName()));
		currency.setNonDeliverable(results.getBoolean(NON_DELIVERABLE_FIELD.getName()));
		currency.setFixingDateOffset(results.getInt(FIXING_DATE_OFFSET_FIELD.getName()));
		currency.setCalendar(CalendarSQL.getCalendarById(results.getLong(CALENDAR_ID_FIELD.getName())));
		return currency;
	}

}