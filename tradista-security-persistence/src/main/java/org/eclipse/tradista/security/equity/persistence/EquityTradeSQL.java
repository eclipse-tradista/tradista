package org.eclipse.tradista.security.equity.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
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

	public static EquityTrade getTradeById(long id, boolean includeUnderlying) {
		EquityTrade equityTrade = null;
		String sql = "SELECT * FROM EQUITY_TRADE, TRADE WHERE "
				+ "TRADE.ID = EQUITY_TRADE.EQUITY_TRADE_ID AND EQUITY_TRADE.EQUITY_TRADE_ID = ?";
		if (!includeUnderlying) {
			sql += " AND TRADE.TRADE_DATE IS NOT NULL";
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(sql)) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (equityTrade == null) {
						equityTrade = new EquityTrade();
					}
					equityTrade.setProduct(EquitySQL.getEquityById(results.getLong("product_id")));
					equityTrade.setAmount(results.getBigDecimal("amount"));
					equityTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					equityTrade.setBuySell(results.getBoolean("buy_sell"));
					equityTrade.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					equityTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					equityTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					equityTrade.setId(results.getLong("id"));
					equityTrade.setQuantity(results.getBigDecimal("quantity"));
					Date settlementDate = results.getDate("settlement_date");
					if (settlementDate != null) {
						equityTrade.setSettlementDate(settlementDate.toLocalDate());
					}
					Date tradeDate = results.getDate("trade_date");
					if (tradeDate != null) {
						equityTrade.setTradeDate(tradeDate.toLocalDate());
					}
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equityTrade;
	}

	public static long saveEquityTrade(EquityTrade trade) {
		long tradeId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, PRODUCT_ID, COUNTERPARTY_ID, AMOUNT, BOOK_ID, SETTLEMENT_DATE, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, AMOUNT=?, BOOK_ID=?, SETTLEMENT_DATE=? WHERE ID = ?");
				PreparedStatement stmtSaveEquityTrade = (trade.getId() == 0)
						? con.prepareStatement("INSERT INTO EQUITY_TRADE(QUANTITY, EQUITY_TRADE_ID) VALUES (?, ?) ")
						: con.prepareStatement("UPDATE EQUITY_TRADE SET QUANTITY = ? WHERE EQUITY_TRADE_ID = ?")) {
			boolean isBuy = trade.isBuy();
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(8, java.sql.Date.valueOf(trade.getCreationDate()));
			} else {
				stmtSaveTrade.setLong(8, trade.getId());
			}
			stmtSaveTrade.setBoolean(1, isBuy);
			if (trade.getTradeDate() == null) {
				stmtSaveTrade.setNull(2, java.sql.Types.DATE);
			} else {
				stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			}
			stmtSaveTrade.setLong(3, trade.getProductId());
			stmtSaveTrade.setLong(4, trade.getCounterparty().getId());
			stmtSaveTrade.setBigDecimal(5, trade.getAmount());
			stmtSaveTrade.setLong(6, trade.getBook().getId());
			if (trade.getSettlementDate() == null) {
				stmtSaveTrade.setNull(7, java.sql.Types.DATE);
			} else {
				stmtSaveTrade.setDate(7, java.sql.Date.valueOf(trade.getSettlementDate()));
			}
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
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
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
			equityTrade = new EquityTrade();
			equityTrade.setProduct(EquitySQL.getEquityById(rs.getLong("product_id")));
			equityTrade.setQuantity(rs.getBigDecimal("equity_quantity"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(equityTrade, rs);

		} catch (SQLException | TradistaBusinessException e) {
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return equityTrade;
	}

	public static List<EquityTrade> getEquityTradesBeforeTradeDateByEquityAndBookIds(LocalDate date, long equityId,
			long bookId) {
		List<EquityTrade> equityTrades = null;

		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetTradesBeforeTradeDate = con.createStatement()) {
			String query = "SELECT * FROM EQUITY_TRADE, TRADE WHERE "
					+ "TRADE.ID = EQUITY_TRADE.EQUITY_TRADE_ID AND TRADE_DATE IS NOT NULL AND TRADE_DATE <= '"
					+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date) + "'";

			if (equityId > 0) {
				query += " AND TRADE.PRODUCT_ID = " + equityId;
			}
			if (bookId > 0) {
				query += " AND TRADE.BOOK_ID = " + bookId;
			}

			try (ResultSet results = stmtGetTradesBeforeTradeDate.executeQuery(query)) {

				while (results.next()) {
					if (equityTrades == null) {
						equityTrades = new ArrayList<>();
					}
					EquityTrade equityTrade = new EquityTrade();
					equityTrade.setProduct(EquitySQL.getEquityById(results.getLong("product_id")));
					equityTrade.setAmount(results.getBigDecimal("amount"));
					equityTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					equityTrade.setBuySell(results.getBoolean("buy_sell"));
					equityTrade.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					equityTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					equityTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					equityTrade.setId(results.getLong("id"));
					equityTrade.setQuantity(results.getBigDecimal("quantity"));
					equityTrade.setSettlementDate(results.getDate("settlement_date").toLocalDate());
					equityTrade.setTradeDate(results.getDate("trade_date").toLocalDate());
					equityTrades.add(equityTrade);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equityTrades;
	}

}