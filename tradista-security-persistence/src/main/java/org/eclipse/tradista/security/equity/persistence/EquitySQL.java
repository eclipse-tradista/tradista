package org.eclipse.tradista.security.equity.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.exchange.persistence.ExchangeSQL;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.security.equity.model.Equity;

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

public class EquitySQL {

	public static long saveEquity(Equity equity) {
		long productId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveProduct = (equity.getId() == 0)
						? con.prepareStatement("INSERT INTO PRODUCT(CREATION_DATE, EXCHANGE_ID) VALUES (?, ?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement("UPDATE PRODUCT SET CREATION_DATE=?, EXCHANGE_ID=? WHERE ID=?");
				PreparedStatement stmtSaveSecurity = (equity.getId() == 0) ? con.prepareStatement(
						"INSERT INTO SECURITY(ISSUER_ID, ISIN, CURRENCY_ID, ISSUE_DATE, ISSUE_PRICE, PRODUCT_ID) VALUES (?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE SECURITY SET ISSUER_ID=?, ISIN=?, CURRENCY_ID=?, ISSUE_DATE=?, ISSUE_PRICE=? WHERE PRODUCT_ID=?");
				PreparedStatement stmtSaveEquity = (equity.getId() == 0) ? con.prepareStatement(
						"INSERT INTO EQUITY(TRADING_SIZE, TOTAL_ISSUED, PAY_DIVIDEND, DIVIDEND_CURRENCY_ID, DIVIDEND_FREQUENCY, ACTIVE_FROM, ACTIVE_TO, PRODUCT_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE EQUITY SET TRADING_SIZE=?, TOTAL_ISSUED=?, PAY_DIVIDEND=?, DIVIDEND_CURRENCY_ID=?, DIVIDEND_FREQUENCY=?, ACTIVE_FROM=?, ACTIVE_TO=? WHERE PRODUCT_ID=?")) {
			if (equity.getId() != 0) {
				stmtSaveProduct.setLong(3, equity.getId());
			}
			stmtSaveProduct.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
			stmtSaveProduct.setLong(2, equity.getExchange().getId());
			stmtSaveProduct.executeUpdate();

			if (equity.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveProduct.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						productId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating product failed, no generated key obtained.");
					}
				}
			} else {
				productId = equity.getId();
			}

			stmtSaveSecurity.setLong(1, equity.getIssuerId());
			stmtSaveSecurity.setString(2, equity.getIsin());
			stmtSaveSecurity.setLong(3, equity.getCurrencyId());
			stmtSaveSecurity.setDate(4, java.sql.Date.valueOf(equity.getIssueDate()));
			stmtSaveSecurity.setBigDecimal(5, equity.getIssuePrice());
			stmtSaveSecurity.setLong(6, productId);
			stmtSaveSecurity.executeUpdate();

			stmtSaveEquity.setLong(1, equity.getTradingSize());
			stmtSaveEquity.setLong(2, equity.getTotalIssued());
			stmtSaveEquity.setBoolean(3, equity.isPayDividend());
			if (equity.isPayDividend()) {
				stmtSaveEquity.setLong(4, equity.getDividendCurrency().getId());
				stmtSaveEquity.setString(5, equity.getDividendFrequency().name());
			} else {
				stmtSaveEquity.setNull(4, Types.BIGINT);
				stmtSaveEquity.setNull(5, Types.VARCHAR);
			}
			stmtSaveEquity.setDate(6, java.sql.Date.valueOf(equity.getActiveFrom()));
			stmtSaveEquity.setDate(7, java.sql.Date.valueOf(equity.getActiveTo()));
			stmtSaveEquity.setLong(8, productId);
			stmtSaveEquity.executeUpdate();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		equity.setId(productId);
		return productId;
	}

	public static Set<Equity> getEquitiesByCreationDate(LocalDate date) {
		Set<Equity> equities = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquitiesByCreationDate = con.prepareStatement(
						"SELECT EQUITY.PRODUCT_ID ID, SECURITY.ISIN, EQUITY.TRADING_SIZE TRADING_SIZE,"
								+ "EQUITY.TOTAL_ISSUED TOTAL_ISSUED, EQUITY.PAY_DIVIDEND PAY_DIVIDEND,"
								+ "EQUITY.DIVIDEND_CURRENCY_ID DIVIDEND_CURRENCY_ID, EQUITY.DIVIDEND_FREQUENCY DIVIDEND_FREQUENCY, EQUITY.ACTIVE_FROM ACTIVE_FROM, EQUITY.ACTIVE_TO ACTIVE_TO, "
								+ "PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
								+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID "
								+ "FROM EQUITY, PRODUCT, SECURITY WHERE "
								+ "SECURITY.PRODUCT_ID = PRODUCT.ID AND EQUITY.PRODUCT_ID = PRODUCT.ID AND CREATION_DATE = ? ")) {
			stmtGetEquitiesByCreationDate.setDate(1, java.sql.Date.valueOf(date));
			try (ResultSet results = stmtGetEquitiesByCreationDate.executeQuery()) {
				while (results.next()) {
					if (equities == null) {
						equities = new HashSet<Equity>();
					}
					Equity equity = new Equity(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
							results.getString("isin"));
					equity.setId(results.getLong("id"));
					equity.setActiveFrom(results.getDate("active_from").toLocalDate());
					equity.setActiveTo(results.getDate("active_to").toLocalDate());
					equity.setCreationDate(results.getDate("creation_date").toLocalDate());
					equity.setPayDividend(results.getBoolean("pay_dividend"));
					if (equity.isPayDividend()) {
						equity.setDividendCurrency(
								CurrencySQL.getCurrencyById(results.getLong("dividend_currency_id")));
						equity.setDividendFrequency(Tenor.valueOf(results.getString("dividend_frequency")));
					}
					equity.setTotalIssued(results.getLong("total_issued"));
					equity.setTradingSize(results.getLong("trading_size"));
					equity.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
					equity.setIssueDate(results.getDate("issue_date").toLocalDate());
					equity.setIssuePrice(results.getBigDecimal("issue_price"));
					equity.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					equities.add(equity);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equities;
	}

	public static Set<Equity> getEquitiesByDates(LocalDate minCreationDate, LocalDate maxCreationDate,
			LocalDate minActiveDate, LocalDate maxActiveDate) {
		Set<Equity> equities = null;

		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {
			String query = "SELECT EQUITY.PRODUCT_ID ID, SECURITY.ISIN, EQUITY.TRADING_SIZE TRADING_SIZE,"
					+ "EQUITY.TOTAL_ISSUED TOTAL_ISSUED, EQUITY.PAY_DIVIDEND PAY_DIVIDEND,"
					+ "EQUITY.DIVIDEND_CURRENCY_ID DIVIDEND_CURRENCY_ID, EQUITY.DIVIDEND_FREQUENCY DIVIDEND_FREQUENCY, EQUITY.ACTIVE_FROM ACTIVE_FROM, EQUITY.ACTIVE_TO ACTIVE_TO, "
					+ "PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
					+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID "
					+ "FROM EQUITY, PRODUCT, SECURITY WHERE "
					+ "EQUITY.PRODUCT_ID = SECURITY.PRODUCT_ID AND SECURITY.PRODUCT_ID = PRODUCT.ID";
			if (minCreationDate != null || maxCreationDate != null || minActiveDate != null || maxActiveDate != null) {
				if (minCreationDate != null) {
					query += " AND CREATION_DATE >= '"
							+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(minCreationDate) + "'";
				}
				if (maxCreationDate != null) {
					query += " AND CREATION_DATE <= '"
							+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(maxCreationDate) + "'";
				}
				if (minActiveDate != null) {
					query += " AND ACTIVE_FROM >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(minActiveDate)
							+ "'";
				}
				if (maxActiveDate != null) {
					query += " AND ACTIVE_TO <= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(maxActiveDate)
							+ "'";
				}
			}
			try (ResultSet results = stmt.executeQuery(query)) {
				while (results.next()) {
					if (equities == null) {
						equities = new HashSet<Equity>();
					}
					Equity equity = new Equity(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
							results.getString("isin"));
					equity.setId(results.getLong("id"));
					equity.setActiveFrom(results.getDate("active_from").toLocalDate());
					equity.setActiveTo(results.getDate("active_to").toLocalDate());
					equity.setCreationDate(results.getDate("creation_date").toLocalDate());
					equity.setPayDividend(results.getBoolean("pay_dividend"));
					if (equity.isPayDividend()) {
						equity.setDividendCurrency(
								CurrencySQL.getCurrencyById(results.getLong("dividend_currency_id")));
						equity.setDividendFrequency(Tenor.valueOf(results.getString("dividend_frequency")));
					}
					equity.setTotalIssued(results.getLong("total_issued"));
					equity.setTradingSize(results.getLong("trading_size"));
					equity.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
					equity.setIssueDate(results.getDate("issue_date").toLocalDate());
					equity.setIssuePrice(results.getBigDecimal("issue_price"));
					equity.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					equities.add(equity);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equities;
	}

	public static Set<Equity> getAllEquities() {
		Set<Equity> equities = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllEquities = con.prepareStatement(
						"SELECT EQUITY.PRODUCT_ID ID, SECURITY.ISIN, EQUITY.TRADING_SIZE TRADING_SIZE,"
								+ "EQUITY.TOTAL_ISSUED TOTAL_ISSUED, EQUITY.PAY_DIVIDEND PAY_DIVIDEND,"
								+ "EQUITY.DIVIDEND_CURRENCY_ID DIVIDEND_CURRENCY_ID, EQUITY.DIVIDEND_FREQUENCY DIVIDEND_FREQUENCY, EQUITY.ACTIVE_FROM ACTIVE_FROM, EQUITY.ACTIVE_TO ACTIVE_TO, "
								+ "PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
								+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID "
								+ "FROM EQUITY, PRODUCT, SECURITY WHERE EQUITY.PRODUCT_ID = SECURITY.PRODUCT_ID"
								+ " AND SECURITY.PRODUCT_ID = PRODUCT.ID");
				ResultSet results = stmtGetAllEquities.executeQuery()) {
			while (results.next()) {
				if (equities == null) {
					equities = new HashSet<Equity>();
				}
				Equity equity = new Equity(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
						results.getString("isin"));
				equity.setId(results.getLong("id"));
				equity.setActiveFrom(results.getDate("active_from").toLocalDate());
				equity.setActiveTo(results.getDate("active_to").toLocalDate());
				equity.setCreationDate(results.getDate("creation_date").toLocalDate());
				equity.setPayDividend(results.getBoolean("pay_dividend"));
				if (equity.isPayDividend()) {
					equity.setDividendCurrency(CurrencySQL.getCurrencyById(results.getLong("dividend_currency_id")));
					equity.setDividendFrequency(Tenor.valueOf(results.getString("dividend_frequency")));
				}
				equity.setTotalIssued(results.getLong("total_issued"));
				equity.setTradingSize(results.getLong("trading_size"));
				equity.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
				equity.setIssueDate(results.getDate("issue_date").toLocalDate());
				equity.setIssuePrice(results.getBigDecimal("issue_price"));
				equity.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
				equities.add(equity);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equities;
	}

	public static Equity getEquityById(long id) {
		Equity equity = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityById = con.prepareStatement(
						"SELECT EQUITY.PRODUCT_ID ID, SECURITY.ISIN ISIN, EQUITY.TRADING_SIZE TRADING_SIZE,"
								+ "EQUITY.TOTAL_ISSUED TOTAL_ISSUED, EQUITY.PAY_DIVIDEND PAY_DIVIDEND,"
								+ "EQUITY.DIVIDEND_CURRENCY_ID DIVIDEND_CURRENCY_ID, EQUITY.DIVIDEND_FREQUENCY DIVIDEND_FREQUENCY, EQUITY.ACTIVE_FROM ACTIVE_FROM, EQUITY.ACTIVE_TO ACTIVE_TO, "
								+ "PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
								+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID "
								+ "FROM EQUITY, PRODUCT, SECURITY WHERE "
								+ "EQUITY.PRODUCT_ID = SECURITY.PRODUCT_ID AND SECURITY.PRODUCT_ID = PRODUCT.ID AND EQUITY.PRODUCT_ID = ? ")) {
			stmtGetEquityById.setLong(1, id);
			try (ResultSet results = stmtGetEquityById.executeQuery()) {
				while (results.next()) {
					if (equity == null) {
						equity = new Equity(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
								results.getString("isin"));
					}
					equity.setId(results.getLong("id"));
					equity.setActiveFrom(results.getDate("active_from").toLocalDate());
					equity.setActiveTo(results.getDate("active_to").toLocalDate());
					equity.setCreationDate(results.getDate("creation_date").toLocalDate());
					equity.setPayDividend(results.getBoolean("pay_dividend"));
					if (equity.isPayDividend()) {
						equity.setDividendCurrency(
								CurrencySQL.getCurrencyById(results.getLong("dividend_currency_id")));
						equity.setDividendFrequency(Tenor.valueOf(results.getString("dividend_frequency")));
					}
					equity.setTotalIssued(results.getLong("total_issued"));
					equity.setTradingSize(results.getLong("trading_size"));
					equity.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
					equity.setIssueDate(results.getDate("issue_date").toLocalDate());
					equity.setIssuePrice(results.getBigDecimal("issue_price"));
					equity.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equity;
	}

	public static Set<Equity> getEquitiesByIsin(String isin) {
		Set<Equity> equities = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquitiesByIsin = con.prepareStatement(
						"SELECT EQUITY.PRODUCT_ID ID, SECURITY.ISIN, EQUITY.TRADING_SIZE TRADING_SIZE,"
								+ "EQUITY.TOTAL_ISSUED TOTAL_ISSUED, EQUITY.PAY_DIVIDEND PAY_DIVIDEND,"
								+ "EQUITY.DIVIDEND_CURRENCY_ID DIVIDEND_CURRENCY_ID, EQUITY.DIVIDEND_FREQUENCY DIVIDEND_FREQUENCY, EQUITY.ACTIVE_FROM ACTIVE_FROM, EQUITY.ACTIVE_TO ACTIVE_TO, "
								+ "PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
								+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID "
								+ "FROM EQUITY, PRODUCT, SECURITY WHERE EQUITY.PRODUCT_ID = SECURITY.PRODUCT_ID"
								+ " AND SECURITY.PRODUCT_ID = PRODUCT.ID AND SECURITY.ISIN = ?")) {
			stmtGetEquitiesByIsin.setString(1, isin);
			try (ResultSet results = stmtGetEquitiesByIsin.executeQuery()) {
				while (results.next()) {
					if (equities == null) {
						equities = new HashSet<Equity>();
					}
					Equity equity = new Equity(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
							results.getString("isin"));
					equity.setId(results.getLong("id"));
					equity.setActiveFrom(results.getDate("active_from").toLocalDate());
					equity.setActiveTo(results.getDate("active_to").toLocalDate());
					equity.setCreationDate(results.getDate("creation_date").toLocalDate());
					equity.setPayDividend(results.getBoolean("pay_dividend"));
					if (equity.isPayDividend()) {
						equity.setDividendCurrency(
								CurrencySQL.getCurrencyById(results.getLong("dividend_currency_id")));
						equity.setDividendFrequency(Tenor.valueOf(results.getString("dividend_frequency")));
					}
					equity.setTotalIssued(results.getLong("total_issued"));
					equity.setTradingSize(results.getLong("trading_size"));
					equity.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
					equity.setIssueDate(results.getDate("issue_date").toLocalDate());
					equity.setIssuePrice(results.getBigDecimal("issue_price"));
					equity.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					equities.add(equity);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equities;
	}

	public static Equity getEquityByIsinAndExchangeCode(String isin, String exchangeCode) {
		Equity equity = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityByIsinAndExchangeCode = con.prepareStatement(
						"SELECT EQUITY.PRODUCT_ID ID, SECURITY.ISIN, EQUITY.TRADING_SIZE TRADING_SIZE,"
								+ "EQUITY.TOTAL_ISSUED TOTAL_ISSUED, EQUITY.PAY_DIVIDEND PAY_DIVIDEND,"
								+ "EQUITY.DIVIDEND_CURRENCY_ID DIVIDEND_CURRENCY_ID, EQUITY.DIVIDEND_FREQUENCY DIVIDEND_FREQUENCY, EQUITY.ACTIVE_FROM ACTIVE_FROM, EQUITY.ACTIVE_TO ACTIVE_TO, "
								+ "PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
								+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID "
								+ "FROM EQUITY, PRODUCT, SECURITY, EXCHANGE WHERE "
								+ "EQUITY.PRODUCT_ID = SECURITY.PRODUCT_ID"
								+ " AND SECURITY.PRODUCT_ID = PRODUCT.ID AND SECURITY.ISIN = ?"
								+ " AND PRODUCT.EXCHANGE_ID = EXCHANGE.ID AND EXCHANGE.CODE = ?")) {
			stmtGetEquityByIsinAndExchangeCode.setString(1, isin);
			stmtGetEquityByIsinAndExchangeCode.setString(2, exchangeCode);
			try (ResultSet results = stmtGetEquityByIsinAndExchangeCode.executeQuery()) {
				while (results.next()) {
					equity = new Equity(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
							results.getString("isin"));
					equity.setId(results.getLong("id"));
					equity.setActiveFrom(results.getDate("active_from").toLocalDate());
					equity.setActiveTo(results.getDate("active_to").toLocalDate());
					equity.setCreationDate(results.getDate("creation_date").toLocalDate());
					equity.setPayDividend(results.getBoolean("pay_dividend"));
					if (equity.isPayDividend()) {
						equity.setDividendFrequency(Tenor.valueOf(results.getString("dividend_frequency")));
						equity.setDividendCurrency(
								CurrencySQL.getCurrencyById(results.getLong("dividend_currency_id")));
					}
					equity.setTotalIssued(results.getLong("total_issued"));
					equity.setTradingSize(results.getLong("trading_size"));
					equity.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
					equity.setIssueDate(results.getDate("issue_date").toLocalDate());
					equity.setIssuePrice(results.getBigDecimal("issue_price"));
					equity.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equity;
	}

}