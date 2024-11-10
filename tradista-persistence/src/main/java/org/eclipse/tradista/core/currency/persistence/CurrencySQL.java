package org.eclipse.tradista.core.currency.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.calendar.persistence.CalendarSQL;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
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

	public static Currency getCurrencyById(long id) {
		Currency currency = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCurrencyById = con
						.prepareStatement("SELECT * FROM CURRENCY WHERE CURRENCY.ID = ? ")) {
			stmtGetCurrencyById.setLong(1, id);
			try (ResultSet results = stmtGetCurrencyById.executeQuery()) {
				while (results.next()) {
					currency = new Currency(results.getString("iso_code"));
					currency.setId(results.getLong("id"));
					currency.setName(results.getString("name"));
					currency.setNonDeliverable(results.getBoolean("non_deliverable"));
					currency.setFixingDateOffset(results.getInt("fixing_date_offset"));
					currency.setCalendar(CalendarSQL.getCalendarById(results.getLong("calendar_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return currency;
	}

	public static Currency getCurrencyByName(String name) {
		Currency currency = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCurrencyByName = con
						.prepareStatement("SELECT * FROM CURRENCY WHERE NAME = ? ")) {
			stmtGetCurrencyByName.setString(1, name);
			try (ResultSet results = stmtGetCurrencyByName.executeQuery()) {
				while (results.next()) {
					currency = new Currency(results.getString("iso_code"));
					currency.setId(results.getLong("id"));
					currency.setName(results.getString("name"));
					currency.setNonDeliverable(results.getBoolean("non_deliverable"));
					currency.setFixingDateOffset(results.getInt("fixing_date_offset"));
					currency.setCalendar(CalendarSQL.getCalendarById(results.getLong("calendar_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return currency;
	}

	public static Set<Currency> getAllCurrencies() {
		Set<Currency> currencies = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllCurrencies = con.prepareStatement("SELECT * FROM CURRENCY");
				ResultSet results = stmtGetAllCurrencies.executeQuery()) {
			while (results.next()) {
				if (currencies == null) {
					currencies = new HashSet<Currency>();
				}
				Currency currency = new Currency(results.getString("iso_code"));
				currency.setId(results.getLong("id"));
				currency.setName(results.getString("name"));
				currency.setNonDeliverable(results.getBoolean("non_deliverable"));
				currency.setFixingDateOffset(results.getInt("fixing_date_offset"));
				currency.setCalendar(CalendarSQL.getCalendarById(results.getLong("calendar_id")));
				currencies.add(currency);
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return currencies;
	}

	public static long saveCurrency(Currency currency) {
		long currencyId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveCurrency = (currency.getId() == 0) ? con.prepareStatement(
						"INSERT INTO CURRENCY(ISO_CODE, NAME, NON_DELIVERABLE, FIXING_DATE_OFFSET, CALENDAR_ID) VALUES (?, ?, ?, ?, ?) ",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE CURRENCY SET ISO_CODE=?, NAME=?, NON_DELIVERABLE=?, FIXING_DATE_OFFSET=?, CALENDAR_ID=? WHERE ID=?")) {
			if (currency.getId() != 0) {
				stmtSaveCurrency.setLong(6, currency.getId());
			}
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
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		currency.setId(currencyId);
		return currencyId;
	}

	public static boolean currencyExists(String isoCode) {
		boolean exists = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtCurrencyExists = con
						.prepareStatement("SELECT * FROM CURRENCY WHERE CURRENCY.ISO_CODE = ? ")) {
			stmtCurrencyExists.setString(1, isoCode);
			try (ResultSet results = stmtCurrencyExists.executeQuery()) {
				while (results.next()) {
					exists = true;
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return exists;
	}

	public static Currency getCurrencyByIsoCode(String isoCode) {
		Currency currency = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCurrencyByIsoCode = con
						.prepareStatement("SELECT * FROM CURRENCY WHERE CURRENCY.ISO_CODE = ? ")) {
			stmtGetCurrencyByIsoCode.setString(1, isoCode);
			try (ResultSet results = stmtGetCurrencyByIsoCode.executeQuery()) {
				while (results.next()) {
					currency = new Currency(results.getString("iso_code"));
					currency.setId(results.getLong("id"));
					currency.setName(results.getString("name"));
					currency.setNonDeliverable(results.getBoolean("non_deliverable"));
					currency.setFixingDateOffset(results.getInt("fixing_date_offset"));
					currency.setCalendar(CalendarSQL.getCalendarById(results.getLong("calendar_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return currency;
	}

	public static Set<Currency> getDeliverableCurrencies() {
		Set<Currency> currencies = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetDeliverableCurrencies = con
						.prepareStatement("SELECT * FROM CURRENCY WHERE NON_DELIVERABLE = FALSE");
				ResultSet results = stmtGetDeliverableCurrencies.executeQuery()) {
			while (results.next()) {
				if (currencies == null) {
					currencies = new HashSet<Currency>();
				}
				Currency currency = new Currency(results.getString("iso_code"));
				currency.setId(results.getLong("id"));
				currency.setName(results.getString("name"));
				currency.setNonDeliverable(results.getBoolean("non_deliverable"));
				currency.setFixingDateOffset(results.getInt("fixing_date_offset"));
				currency.setCalendar(CalendarSQL.getCalendarById(results.getLong("calendar_id")));
				currencies.add(currency);
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return currencies;
	}

	public static Set<Currency> getNonDeliverableCurrencies() {
		Set<Currency> currencies = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetNonDeliverableCurrencies = con
						.prepareStatement("SELECT * FROM CURRENCY WHERE NON_DELIVERABLE = TRUE");
				ResultSet results = stmtGetNonDeliverableCurrencies.executeQuery()) {
			while (results.next()) {
				if (currencies == null) {
					currencies = new HashSet<Currency>();
				}
				Currency currency = new Currency(results.getString("iso_code"));
				currency.setId(results.getLong("id"));
				currency.setName(results.getString("name"));
				currency.setNonDeliverable(results.getBoolean("non_deliverable"));
				currency.setFixingDateOffset(results.getInt("fixing_date_offset"));
				currency.setCalendar(CalendarSQL.getCalendarById(results.getLong("calendar_id")));
				currencies.add(currency);
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return currencies;
	}

}