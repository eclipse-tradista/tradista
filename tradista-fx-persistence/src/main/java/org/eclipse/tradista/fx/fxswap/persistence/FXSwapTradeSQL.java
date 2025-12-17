package org.eclipse.tradista.fx.fxswap.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.fx.fxswap.model.FXSwapTrade;

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

public class FXSwapTradeSQL {

	public static FXSwapTrade getTradeById(long id) {

		FXSwapTrade fxswapTrade = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(
						"SELECT * FROM FXSWAP_TRADE, TRADE WHERE FXSWAP_TRADE_ID = ? AND FXSWAP_TRADE_ID = ID ")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {

					if (fxswapTrade == null) {
						fxswapTrade = new FXSwapTrade();
					}

					fxswapTrade.setCurrencyOne(CurrencySQL.getCurrencyById(results.getLong("currency_one_id")));
					fxswapTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					fxswapTrade.setSettlementDateForward(results.getDate("settlement_date_forward").toLocalDate());
					fxswapTrade.setAmountOneSpot(results.getBigDecimal("amount_one_spot"));
					fxswapTrade.setAmountOneForward(results.getBigDecimal("amount_one_forward"));
					fxswapTrade.setAmount(results.getBigDecimal("amount"));
					fxswapTrade.setAmountTwoForward(results.getBigDecimal("amount_two_forward"));
					fxswapTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					fxswapTrade.setBuySell(results.getBoolean("buy_sell"));
					fxswapTrade.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					fxswapTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					fxswapTrade.setId(id);
					fxswapTrade.setSettlementDate(results.getDate("settlement_date").toLocalDate());
					fxswapTrade.setTradeDate(results.getDate("trade_date").toLocalDate());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return fxswapTrade;
	}

	public static FXSwapTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		FXSwapTrade fxswapTrade = null;
		try {
			if (rs.getLong("fxswap_trade_id") == 0) {
				return null;
			}

			fxswapTrade = new FXSwapTrade();
			TradeSQL.setTradeCommonFields(fxswapTrade, rs);
			fxswapTrade.setCurrencyOne(CurrencySQL.getCurrencyById(rs.getLong("fxswap_currency_one_id")));
			fxswapTrade.setCurrency(CurrencySQL.getCurrencyById(rs.getLong("currency_id")));
			fxswapTrade.setSettlementDateForward(rs.getDate("settlement_date_forward").toLocalDate());
			fxswapTrade.setAmountOneSpot(rs.getBigDecimal("amount_one_spot"));
			fxswapTrade.setAmountOneForward(rs.getBigDecimal("amount_one_forward"));
			fxswapTrade.setAmount(rs.getBigDecimal("amount"));
			fxswapTrade.setAmountTwoForward(rs.getBigDecimal("amount_two_forward"));
			fxswapTrade.setBook(BookSQL.getBookById(rs.getLong("book_id")));
			fxswapTrade.setBuySell(rs.getBoolean("buy_sell"));
			fxswapTrade.setCounterparty(LegalEntitySQL.getLegalEntityById(rs.getLong("counterparty_id")));
			fxswapTrade.setCreationDate(rs.getDate("creation_date").toLocalDate());
			fxswapTrade.setSettlementDate(rs.getDate("settlement_date").toLocalDate());
			fxswapTrade.setTradeDate(rs.getDate("trade_date").toLocalDate());
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return fxswapTrade;
	}

	public static long saveFXSwapTrade(FXSwapTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, SETTLEMENT_DATE, PRODUCT_ID, COUNTERPARTY_ID, CURRENCY_ID, AMOUNT, BOOK_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, SETTLEMENT_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, CURRENCY_ID=?, AMOUNT=?, BOOK_ID=? WHERE ID = ?");
				PreparedStatement stmtSaveFXSwapTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO FXSWAP_TRADE(CURRENCY_ONE_ID, SETTLEMENT_DATE_FORWARD, AMOUNT_ONE_FORWARD, AMOUNT_ONE_SPOT, AMOUNT_TWO_FORWARD, FXSWAP_TRADE_ID) VALUES (?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE FXSWAP_TRADE SET CURRENCY_ONE_ID=?, SETTLEMENT_DATE_FORWARD=?, AMOUNT_ONE_FORWARD=?, AMOUNT_ONE_SPOT=?, AMOUNT_TWO_FORWARD=? WHERE FXSWAP_TRADE_ID=?")) {
			boolean isBuy = trade.isBuy();
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(9, java.sql.Date.valueOf(trade.getCreationDate()));
			} else {
				stmtSaveTrade.setLong(9, trade.getId());
			}
			stmtSaveTrade.setBoolean(1, isBuy);
			stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			stmtSaveTrade.setDate(3, java.sql.Date.valueOf(trade.getSettlementDate()));
			stmtSaveTrade.setNull(4, java.sql.Types.BIGINT);
			stmtSaveTrade.setLong(5, trade.getCounterparty().getId());
			stmtSaveTrade.setLong(6, trade.getCurrency().getId());
			stmtSaveTrade.setBigDecimal(7, trade.getAmount());
			stmtSaveTrade.setLong(8, trade.getBook().getId());
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

			stmtSaveFXSwapTrade.setLong(1, trade.getCurrencyOne().getId());
			stmtSaveFXSwapTrade.setDate(2, java.sql.Date.valueOf(trade.getSettlementDateForward()));
			stmtSaveFXSwapTrade.setBigDecimal(3, trade.getAmountOneForward());
			stmtSaveFXSwapTrade.setBigDecimal(4, trade.getAmountOneSpot());
			stmtSaveFXSwapTrade.setBigDecimal(5, trade.getAmountTwoForward());
			stmtSaveFXSwapTrade.setLong(6, tradeId);
			stmtSaveFXSwapTrade.executeUpdate();
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}
}