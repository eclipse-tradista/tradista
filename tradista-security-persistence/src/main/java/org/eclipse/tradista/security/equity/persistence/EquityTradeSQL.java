package org.eclipse.tradista.security.equity.persistence;

import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.PRODUCT_ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_TABLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.security.equity.model.EquityTrade;

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

public class EquityTradeSQL {

	private static final Field EQUITY_TRADE_ID_FIELD = new Field("EQUITY_TRADE_ID");
	private static final Field QUANTITY_FIELD = new Field(TradistaDBConstants.QUANTITY);

	private static final Field[] EQUITY_TRADE_FIELDS = { EQUITY_TRADE_ID_FIELD, QUANTITY_FIELD };

	private static final Field[] EQUITY_TRADE_FIELDS_FOR_INSERT = { QUANTITY_FIELD, EQUITY_TRADE_ID_FIELD };

	private static final Field[] EQUITY_TRADE_FIELDS_FOR_UPDATE = { QUANTITY_FIELD };

	private static final Table EQUITY_TRADE_TABLE = new Table("EQUITY_TRADE", EQUITY_TRADE_FIELDS);

	private static final Join TRADE_AND_EQUITY_TRADE_INNER_JOIN = Join.innerEq(TRADE_TABLE, ID_FIELD,
			EQUITY_TRADE_ID_FIELD);

	private static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(EQUITY_TRADE_TABLE,
			TRADE_AND_EQUITY_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, EQUITY_TRADE_TABLE, EQUITY_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, EQUITY_TRADE_ID_FIELD, EQUITY_TRADE_TABLE,
				EQUITY_TRADE_FIELDS_FOR_UPDATE);
	}

	public static EquityTrade getTradeById(long id, boolean includeUnderlying) {
		EquityTrade equityTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, EQUITY_TRADE_ID_FIELD);
		if (!includeUnderlying) {
			TradistaDBUtil.addIsNotNullFilter(query, TradeSQL.TRADE_DATE_FIELD);
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (equityTrade == null) {
						equityTrade = EquityTrade.of(TradeSQL.getCreationTime(results));
					}
					TradeSQL.setTradeCommonFields(equityTrade, results);
					equityTrade.setProduct(EquitySQL.getEquityById(results.getLong(PRODUCT_ID_FIELD.getName())));
					equityTrade.setQuantity(results.getBigDecimal(QUANTITY_FIELD.getName()));
				}
			}
		} catch (SQLException | TradistaBusinessException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equityTrade;
	}

	public static long saveEquityTrade(EquityTrade trade) {
		long tradeId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveEquityTrade = (trade.getId() == 0) ? getInsertStatement(con)
						: getUpdateStatement(con)) {
			TradeSQL.setPreparedStatementCommonFields(trade, stmtSaveTrade);
			stmtSaveTrade.executeUpdate();

			if (trade.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveTrade.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						tradeId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating trade failed, no generated key obtained.");
					}
				}
			} else {
				tradeId = trade.getId();
			}

			stmtSaveEquityTrade.setBigDecimal(1, trade.getQuantity());
			stmtSaveEquityTrade.setLong(2, tradeId);
			stmtSaveEquityTrade.executeUpdate();
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		trade.setId(tradeId);
		return tradeId;
	}

	public static EquityTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		EquityTrade equityTrade = null;
		try {
			// We ensure that the deal is an Equity Spot.
			if (rs.getLong("equity_trade_id") == 0) {
				return null;
			}
			equityTrade = EquityTrade.of(TradeSQL.getCreationTime(rs));
			equityTrade.setProduct(EquitySQL.getEquityById(rs.getLong(PRODUCT_ID_FIELD.getName())));
			equityTrade.setQuantity(rs.getBigDecimal("equity_quantity"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(equityTrade, rs);

		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return equityTrade;
	}

	public static List<EquityTrade> getEquityTradesBeforeTradeDateByEquityAndBookIds(LocalDate date, long equityId,
			long bookId) {
		List<EquityTrade> equityTrades = null;

		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addIsNotNullFilter(query, TradeSQL.TRADE_DATE_FIELD);
		TradistaDBUtil.addFilter(query, TradeSQL.TRADE_DATE_FIELD, date, false);

		if (equityId > 0) {
			TradistaDBUtil.addFilter(query, TradeSQL.PRODUCT_ID_FIELD, equityId);
		}
		if (bookId > 0) {
			TradistaDBUtil.addFilter(query, TradeSQL.BOOK_ID_FIELD, bookId);
		}

		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetTradesBeforeTradeDate = con.createStatement();
				ResultSet results = stmtGetTradesBeforeTradeDate.executeQuery(query.toString())) {

			while (results.next()) {
				if (equityTrades == null) {
					equityTrades = new ArrayList<>();
				}
				EquityTrade equityTrade = EquityTrade.of(TradeSQL.getCreationTime(results));
				TradeSQL.setTradeCommonFields(equityTrade, results);
				equityTrade.setProduct(EquitySQL.getEquityById(results.getLong(PRODUCT_ID_FIELD.getName())));
				equityTrade.setQuantity(results.getBigDecimal(QUANTITY_FIELD.getName()));
				equityTrades.add(equityTrade);
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return equityTrades;
	}

}