package org.eclipse.tradista.ir.future.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MM_DD_YYYY;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.ir.future.model.FutureTrade;

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

public class FutureTradeSQL {

	public static FutureTrade getTradeById(long id) {

		FutureTrade futureTrade = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con
						.prepareStatement("SELECT * FROM IRFORWARD_TRADE, TRADE, FUTURE_TRADE WHERE "
								+ "IRFORWARD_TRADE_ID = ? AND IRFORWARD_TRADE_ID = FUTURE_TRADE_ID AND TRADE.ID = IRFORWARD_TRADE_ID ")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (futureTrade == null) {
						futureTrade = new FutureTrade();
					}

					TradeSQL.setTradeCommonFields(futureTrade, results);
					java.sql.Date maturityDate = results.getDate("maturity_date");
					if (maturityDate != null) {
						futureTrade.setMaturityDate(maturityDate.toLocalDate());
					}
					futureTrade.setProduct(FutureSQL.getFutureById(results.getLong("product_id")));
					futureTrade.setQuantity(results.getBigDecimal("quantity"));
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return futureTrade;
	}

	public static FutureTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		FutureTrade futureTrade = null;
		try {
			if (rs.getLong("future_trade_id") == 0) {
				return null;
			}

			futureTrade = new FutureTrade();
			java.sql.Date maturityDate = rs.getDate("irforward_maturity_date");
			if (maturityDate != null) {
				futureTrade.setMaturityDate(maturityDate.toLocalDate());
			}
			futureTrade.setProduct(FutureSQL.getFutureById(rs.getLong("product_id")));
			futureTrade.setQuantity(rs.getBigDecimal("future_quantity"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(futureTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return futureTrade;
	}

	public static long saveFutureTrade(FutureTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, SETTLEMENT_DATE, PRODUCT_ID, COUNTERPARTY_ID, AMOUNT, BOOK_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, SETTLEMENT_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, AMOUNT=?, BOOK_ID=? WHERE ID=?");
				PreparedStatement stmtSaveIRForwardTrade = (trade.getId() == 0)
						? con.prepareStatement(
								"INSERT INTO IRFORWARD_TRADE(MATURITY_DATE, IRFORWARD_TRADE_ID) VALUES (?, ?)")
						: con.prepareStatement(
								"UPDATE IRFORWARD_TRADE SET MATURITY_DATE=? WHERE IRFORWARD_TRADE_ID = ?");
				PreparedStatement stmtSaveFutureTrade = (trade.getId() == 0)
						? con.prepareStatement("INSERT INTO FUTURE_TRADE(QUANTITY, FUTURE_TRADE_ID) VALUES (?, ?)")
						: con.prepareStatement("UPDATE FUTURE_TRADE SET QUANTITY=? WHERE FUTURE_TRADE_ID=?")) {
			boolean isBuy = trade.isBuy();
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(8, java.sql.Date.valueOf(trade.getCreationDate()));
			} else {
				stmtSaveTrade.setLong(8, trade.getId());
			}

			stmtSaveTrade.setBoolean(1, isBuy);
			if (trade.getTradeDate() != null) {
				stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			} else {
				stmtSaveTrade.setNull(2, java.sql.Types.DATE);
			}
			stmtSaveTrade.setDate(3, java.sql.Date.valueOf(trade.getSettlementDate()));
			stmtSaveTrade.setLong(4, trade.getProduct().getId());
			stmtSaveTrade.setLong(5, trade.getCounterparty().getId());
			stmtSaveTrade.setBigDecimal(6, trade.getAmount());
			stmtSaveTrade.setLong(7, trade.getBook().getId());
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

			stmtSaveIRForwardTrade.setDate(1, java.sql.Date.valueOf(trade.getMaturityDate()));
			stmtSaveIRForwardTrade.setLong(2, tradeId);
			stmtSaveIRForwardTrade.executeUpdate();

			stmtSaveFutureTrade.setBigDecimal(1, trade.getQuantity());
			stmtSaveFutureTrade.setLong(2, tradeId);
			stmtSaveFutureTrade.executeUpdate();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		trade.setId(tradeId);
		return tradeId;
	}

	public static List<FutureTrade> getFutureTradesBeforeTradeDateByFutureAndBookIds(LocalDate date, long futureId,
			long bookId) {

		List<FutureTrade> futureTrades = null;
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetTradesBeforeTradeDate = con.createStatement()) {
			String query = "SELECT * FROM IRFORWARD_TRADE, TRADE, FUTURE_TRADE WHERE "
					+ "IRFORWARD_TRADE_ID = FUTURE_TRADE_ID AND TRADE.ID = IRFORWARD_TRADE_ID AND TRADE.TRADE_DATE <= '"
					+ DateTimeFormatter.ofPattern(MM_DD_YYYY).format(date) + "'";

			if (futureId > 0) {
				query += " AND TRADE.PRODUCT_ID = " + futureId;
			}
			if (bookId > 0) {
				query += " AND TRADE.BOOK_ID = " + bookId;
			}
			try (ResultSet results = stmtGetTradesBeforeTradeDate.executeQuery(query)) {
				while (results.next()) {
					if (futureTrades == null) {
						futureTrades = new ArrayList<>();
					}
					FutureTrade futureTrade = new FutureTrade();

					TradeSQL.setTradeCommonFields(futureTrade, results);
					java.sql.Date maturityDate = results.getDate("maturity_date");
					if (maturityDate != null) {
						futureTrade.setMaturityDate(maturityDate.toLocalDate());
					}
					futureTrade.setProduct(FutureSQL.getFutureById(results.getLong("product_id")));
					futureTrade.setQuantity(results.getBigDecimal("quantity"));

					futureTrades.add(futureTrade);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return futureTrades;

	}
}