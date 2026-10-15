package org.eclipse.tradista.security.equity.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CREATION_TIME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRODUCT_ID;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.exchange.persistence.ExchangeSQL;
import org.eclipse.tradista.core.product.persistence.ProductSQL;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.security.common.persistence.SecuritySQL;
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

	private static final Field CREATION_TIME_FIELD = new Field(CREATION_TIME);

	private static final Field EQUITY_PRODUCT_ID_FIELD = new Field(PRODUCT_ID);
	private static final Field TRADING_SIZE_FIELD = new Field("TRADING_SIZE");
	private static final Field TOTAL_ISSUED_FIELD = new Field("TOTAL_ISSUED");
	private static final Field PAY_DIVIDEND_FIELD = new Field("PAY_DIVIDEND");
	private static final Field DIVIDEND_CURRENCY_ID_FIELD = new Field("DIVIDEND_CURRENCY_ID");
	private static final Field DIVIDEND_FREQUENCY_FIELD = new Field("DIVIDEND_FREQUENCY");
	private static final Field ACTIVE_FROM_FIELD = new Field("ACTIVE_FROM");
	private static final Field ACTIVE_TO_FIELD = new Field("ACTIVE_TO");

	private static final Field[] EQUITY_FIELDS = { TRADING_SIZE_FIELD, TOTAL_ISSUED_FIELD, PAY_DIVIDEND_FIELD,
			DIVIDEND_CURRENCY_ID_FIELD, DIVIDEND_FREQUENCY_FIELD, ACTIVE_FROM_FIELD, ACTIVE_TO_FIELD,
			EQUITY_PRODUCT_ID_FIELD };
	public static final Table EQUITY_TABLE = new Table("EQUITY", EQUITY_FIELDS);

	private static final Field[] EQUITY_FIELDS_FOR_UPDATE = { TRADING_SIZE_FIELD, TOTAL_ISSUED_FIELD,
			PAY_DIVIDEND_FIELD, DIVIDEND_CURRENCY_ID_FIELD, DIVIDEND_FREQUENCY_FIELD, ACTIVE_FROM_FIELD,
			ACTIVE_TO_FIELD };

	private static final Join EQUITY_PRODUCT_JOIN = Join.innerEq(ProductSQL.PRODUCT_TABLE, EQUITY_PRODUCT_ID_FIELD,
			ProductSQL.ID_FIELD);
	private static final Join EQUITY_SECURITY_JOIN = Join.innerEq(SecuritySQL.SECURITY_TABLE, EQUITY_PRODUCT_ID_FIELD,
			SecuritySQL.SECURITY_PRODUCT_ID_FIELD);

	private static final String BASE_SELECT_QUERY = TradistaDBUtil.buildSelectQuery(EQUITY_TABLE, EQUITY_PRODUCT_JOIN,
			EQUITY_SECURITY_JOIN);

	public static long saveEquity(Equity equity) {
		long productId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveProduct = (equity.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, ProductSQL.PRODUCT_TABLE,
								ProductSQL.PRODUCT_FIELDS_FOR_INSERT)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ProductSQL.ID_FIELD,
								ProductSQL.PRODUCT_TABLE, ProductSQL.PRODUCT_FIELDS_FOR_UPDATE);
				PreparedStatement stmtSaveSecurity = (equity.getId() == 0) ? SecuritySQL.getInsertStatement(con)
						: SecuritySQL.getUpdateStatement(con);
				PreparedStatement stmtSaveEquity = (equity.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, EQUITY_TABLE, EQUITY_FIELDS)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, EQUITY_PRODUCT_ID_FIELD, EQUITY_TABLE,
								EQUITY_FIELDS_FOR_UPDATE)) {
			if (equity.getId() == 0) {
				stmtSaveProduct.setTimestamp(1, Timestamp.from(equity.getCreationTime()));
				stmtSaveProduct.setTimestamp(2, Timestamp.from(equity.getLastUpdateTime()));
				stmtSaveProduct.setLong(3, equity.getExchange().getId());
			} else {
				stmtSaveProduct.setTimestamp(1, Timestamp.from(Instant.now()));
				stmtSaveProduct.setLong(2, equity.getExchange().getId());
				stmtSaveProduct.setLong(3, equity.getId());
			}
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

			SecuritySQL.setPreparedStatementSecurityFields(equity, stmtSaveSecurity, productId);
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
			stmtSaveEquity.setDate(6, Date.valueOf(equity.getActiveFrom()));
			stmtSaveEquity.setDate(7, Date.valueOf(equity.getActiveTo()));
			stmtSaveEquity.setLong(8, productId);
			stmtSaveEquity.executeUpdate();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		equity.setId(productId);
		return productId;
	}

	public static Set<Equity> getEquitiesByCreationDate(LocalDate date) {
		Set<Equity> equities = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, CREATION_TIME_FIELD, true);
		TradistaDBUtil.addParameterizedFilter(sql, CREATION_TIME_FIELD, false);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquitiesByCreationDate = con.prepareStatement(sql.toString())) {
			stmtGetEquitiesByCreationDate.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
			stmtGetEquitiesByCreationDate.setTimestamp(2, Timestamp.valueOf(date.atTime(23, 59, 59, 999999999)));
			try (ResultSet results = stmtGetEquitiesByCreationDate.executeQuery()) {
				while (results.next()) {
					if (equities == null) {
						equities = new HashSet<>();
					}
					equities.add(buildEquity(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equities;
	}

	public static Set<Equity> getEquitiesByDates(LocalDate minCreationDate, LocalDate maxCreationDate,
			LocalDate minActiveDate, LocalDate maxActiveDate) {
		Set<Equity> equities = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		if (minCreationDate != null) {
			TradistaDBUtil.addFilter(sql, CREATION_TIME_FIELD, minCreationDate.atStartOfDay(), true);
		}
		if (maxCreationDate != null) {
			TradistaDBUtil.addFilter(sql, CREATION_TIME_FIELD, maxCreationDate.atTime(23, 59, 59), false);
		}
		if (minActiveDate != null) {
			TradistaDBUtil.addFilter(sql, ACTIVE_FROM_FIELD, minActiveDate, true);
		}
		if (maxActiveDate != null) {
			TradistaDBUtil.addFilter(sql, ACTIVE_TO_FIELD, maxActiveDate, false);
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString());
				ResultSet results = stmt.executeQuery()) {
			while (results.next()) {
				if (equities == null) {
					equities = new HashSet<>();
				}
				equities.add(buildEquity(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equities;
	}

	public static Set<Equity> getAllEquities() {
		Set<Equity> equities = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllEquities = con.prepareStatement(BASE_SELECT_QUERY);
				ResultSet results = stmtGetAllEquities.executeQuery()) {
			while (results.next()) {
				if (equities == null) {
					equities = new HashSet<>();
				}
				equities.add(buildEquity(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equities;
	}

	public static Equity getEquityById(long id) {
		Equity equity = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, ProductSQL.ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityById = con.prepareStatement(sql.toString())) {
			stmtGetEquityById.setLong(1, id);
			try (ResultSet results = stmtGetEquityById.executeQuery()) {
				while (results.next()) {
					equity = buildEquity(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equity;
	}

	public static Set<Equity> getEquitiesByIsin(String isin) {
		Set<Equity> equities = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, SecuritySQL.ISIN_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquitiesByIsin = con.prepareStatement(sql.toString())) {
			stmtGetEquitiesByIsin.setString(1, isin);
			try (ResultSet results = stmtGetEquitiesByIsin.executeQuery()) {
				while (results.next()) {
					if (equities == null) {
						equities = new HashSet<>();
					}
					equities.add(buildEquity(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equities;
	}

	public static Equity getEquityByIsinAndExchangeCode(String isin, String exchangeCode) {
		Equity equity = null;
		StringBuilder sql = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(EQUITY_TABLE, EQUITY_PRODUCT_JOIN, EQUITY_SECURITY_JOIN,
						Join.innerEq(ExchangeSQL.EXCHANGE_TABLE, ProductSQL.EXCHANGE_ID_FIELD, ExchangeSQL.ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sql, SecuritySQL.ISIN_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, ExchangeSQL.CODE_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityByIsinAndExchangeCode = con.prepareStatement(sql.toString())) {
			stmtGetEquityByIsinAndExchangeCode.setString(1, isin);
			stmtGetEquityByIsinAndExchangeCode.setString(2, exchangeCode);
			try (ResultSet results = stmtGetEquityByIsinAndExchangeCode.executeQuery()) {
				while (results.next()) {
					equity = buildEquity(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equity;
	}

	private static Equity buildEquity(ResultSet results) throws SQLException {
		Equity.Builder builder = Equity
				.builder(ExchangeSQL.getExchangeById(results.getLong(ProductSQL.EXCHANGE_ID_FIELD.getName())),
						results.getString(SecuritySQL.ISIN_FIELD.getName()))
				.id(results.getLong(ProductSQL.ID_FIELD.getName()));
		Timestamp creationTimestamp = results.getTimestamp(CREATION_TIME_FIELD.getName());
		if (creationTimestamp != null) {
			builder.creationTime(creationTimestamp.toInstant());
		}
		Equity equity = builder.build();
		equity.setActiveFrom(results.getDate(ACTIVE_FROM_FIELD.getName()).toLocalDate());
		equity.setActiveTo(results.getDate(ACTIVE_TO_FIELD.getName()).toLocalDate());
		equity.setPayDividend(results.getBoolean(PAY_DIVIDEND_FIELD.getName()));
		if (equity.isPayDividend()) {
			equity.setDividendCurrency(
					CurrencySQL.getCurrencyById(results.getLong(DIVIDEND_CURRENCY_ID_FIELD.getName())));
			equity.setDividendFrequency(Tenor.valueOf(results.getString(DIVIDEND_FREQUENCY_FIELD.getName())));
		}
		equity.setTotalIssued(results.getLong(TOTAL_ISSUED_FIELD.getName()));
		equity.setTradingSize(results.getLong(TRADING_SIZE_FIELD.getName()));
		SecuritySQL.setSecurityCommonFields(equity, results);
		return equity;
	}

}