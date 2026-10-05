package org.eclipse.tradista.security.equityoption.persistence;

import static org.eclipse.tradista.core.trade.persistence.TradeSQL.PRODUCT_ID_FIELD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.core.trade.persistence.VanillaOptionTradeSQL;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.persistence.EquitySQL;
import org.eclipse.tradista.security.equity.persistence.EquityTradeSQL;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;

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

public class EquityOptionTradeSQL {

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(
			VanillaOptionTradeSQL.VANILLA_OPTION_TRADE_TABLE,
			VanillaOptionTradeSQL.TRADE_AND_VANILLA_OPTION_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return VanillaOptionTradeSQL.getInsertStatement(con);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return VanillaOptionTradeSQL.getUpdateStatement(con);
	}

	public static EquityOptionTrade getTradeById(long id) {
		EquityOptionTrade equityOptionTrade = null;

		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, VanillaOptionTradeSQL.VANILLA_OPTION_TRADE_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (equityOptionTrade == null) {
						equityOptionTrade = EquityOptionTrade.of(TradeSQL.getCreationTime(results));
					}

					TradeSQL.setTradeCommonFields(equityOptionTrade, results);
					VanillaOptionTradeSQL.setVanillaOptionTradeCommonFields(equityOptionTrade, results);
					long productId = results.getLong(PRODUCT_ID_FIELD.getName());
					if (productId != 0) {
						equityOptionTrade.setEquityOption(EquityOptionSQL.getEquityOptionById(productId));
					}
					equityOptionTrade
							.setQuantity(results.getBigDecimal(VanillaOptionTradeSQL.QUANTITY_FIELD.getName()));

					// Building the underlying
					EquityTrade underlying = EquityTradeSQL.getTradeById(
							results.getLong(VanillaOptionTradeSQL.UNDERLYING_TRADE_ID_FIELD.getName()), true);
					equityOptionTrade.setUnderlying(underlying);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return equityOptionTrade;
	}

	public static long saveEquityOptionTrade(EquityOptionTrade trade) {
		long tradeId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveEquityOptionTrade = (trade.getId() == 0) ? getInsertStatement(con)
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

			// Underlying saving
			long underlyingId = EquityTradeSQL.saveEquityTrade(trade.getUnderlying());

			VanillaOptionTradeSQL.setPreparedStatementVanillaOptionFields(trade, stmtSaveEquityOptionTrade,
					underlyingId, tradeId, trade.getQuantity());
			stmtSaveEquityOptionTrade.executeUpdate();
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		trade.setId(tradeId);
		return tradeId;
	}

	public static EquityOptionTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		EquityOptionTrade equityOptionTrade = null;
		try {
			if ((rs.getLong(VanillaOptionTradeSQL.VANILLA_OPTION_TRADE_ID_FIELD.getName()) == 0)
					|| (rs.getLong("underlying_equity_trade_id") == 0)) {
				return null;
			}
			equityOptionTrade = EquityOptionTrade.of(TradeSQL.getCreationTime(rs));
			VanillaOptionTradeSQL.setVanillaOptionTradeCommonFields(equityOptionTrade, rs);
			long productId = rs.getLong(PRODUCT_ID_FIELD.getName());
			if (productId != 0) {
				equityOptionTrade.setEquityOption(EquityOptionSQL.getEquityOptionById(productId));
			}
			equityOptionTrade.setQuantity(rs.getBigDecimal("option_quantity"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(equityOptionTrade, rs);

			// Building the underlying
			java.sql.Timestamp undCreationTime = rs.getTimestamp("UND_EQUITY_creation_time");
			EquityTrade.Builder undBuilder = EquityTrade.builder();
			if (undCreationTime != null) {
				undBuilder.creationTime(undCreationTime.toInstant());
			}
			EquityTrade underlying = undBuilder.build();
			underlying.setId(rs.getLong("UNDERLYING_EQUITY_TRADE_ID"));
			underlying.setProduct(EquitySQL.getEquityById(rs.getLong("UND_EQUITY_PRODUCT_ID")));
			underlying.setAmount(rs.getBigDecimal("UND_EQUITY_AMOUNT"));
			underlying.setBook(BookSQL.getBookById(rs.getLong("book_id")));
			underlying.setBuySell(rs.getBoolean("UND_EQUITY_buy_sell"));
			underlying.setCounterparty(LegalEntitySQL.getLegalEntityById(rs.getLong("UND_EQUITY_counterparty_id")));
			underlying.setQuantity(rs.getBigDecimal("UNDERLYING_EQUITY_QUANTITY"));
			Date undSettleDate = rs.getDate("und_equity_settlement_date");
			if (undSettleDate != null) {
				underlying.setSettlementDate(undSettleDate.toLocalDate());
			}
			Date undTradeDate = rs.getDate("und_equity_trade_date");
			if (undTradeDate != null) {
				underlying.setTradeDate(undTradeDate.toLocalDate());
			}

			equityOptionTrade.setUnderlying(underlying);

		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return equityOptionTrade;
	}

	public static List<EquityOptionTrade> getEquityOptionTradesBeforeTradeDateByEquityOptionAndBookIds(
			LocalDate tradeDate, long equityOptionId, long bookId) {
		List<EquityOptionTrade> equityOptionTrades = null;

		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addFilter(query, TradeSQL.TRADE_DATE_FIELD, tradeDate, false);

		if (equityOptionId > 0) {
			TradistaDBUtil.addFilter(query, TradeSQL.PRODUCT_ID_FIELD, equityOptionId);
		}
		if (bookId > 0) {
			TradistaDBUtil.addFilter(query, TradeSQL.BOOK_ID_FIELD, bookId);
		}

		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetTradesBeforeTradeDateByEquityOptionAndBookIds = con.createStatement();
				ResultSet results = stmtGetTradesBeforeTradeDateByEquityOptionAndBookIds
						.executeQuery(query.toString())) {
			while (results.next()) {
				if (equityOptionTrades == null) {
					equityOptionTrades = new ArrayList<>();
				}

				EquityOptionTrade equityOptionTrade = EquityOptionTrade.of(TradeSQL.getCreationTime(results));
				TradeSQL.setTradeCommonFields(equityOptionTrade, results);
				VanillaOptionTradeSQL.setVanillaOptionTradeCommonFields(equityOptionTrade, results);
				long productId = results.getLong(PRODUCT_ID_FIELD.getName());
				if (productId != 0) {
					equityOptionTrade.setEquityOption(EquityOptionSQL.getEquityOptionById(productId));
				}
				equityOptionTrade.setQuantity(results.getBigDecimal(VanillaOptionTradeSQL.QUANTITY_FIELD.getName()));

				// Building the underlying
				EquityTrade underlying = EquityTradeSQL
						.getTradeById(results.getLong(VanillaOptionTradeSQL.UNDERLYING_TRADE_ID_FIELD.getName()), true);
				equityOptionTrade.setUnderlying(underlying);

				equityOptionTrades.add(equityOptionTrade);
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return equityOptionTrades;
	}

}