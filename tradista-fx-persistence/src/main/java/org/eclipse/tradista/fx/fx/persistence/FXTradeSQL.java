package org.eclipse.tradista.fx.fx.persistence;

import java.sql.Connection;
import java.sql.Date;
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
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fx.service.FXTradeBusinessDelegate;

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

public class FXTradeSQL {

	public static FXTrade getTradeById(long id, boolean includeUnderlying) {
		FXTrade fxspotTrade = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = (includeUnderlying)
						? con.prepareStatement(
								"SELECT * FROM FXSPOT_TRADE, TRADE WHERE FXSPOT_TRADE_ID = ? AND FXSPOT_TRADE_ID = ID")
						: con.prepareStatement(
								"SELECT * FROM FXSPOT_TRADE, TRADE WHERE FXSPOT_TRADE_ID = ? AND FXSPOT_TRADE_ID = ID AND TRADE.TRADE_DATE IS NOT NULL")) {
			FXTradeBusinessDelegate fxTradeBusinessDelegate = new FXTradeBusinessDelegate();
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {

				while (results.next()) {

					if (fxspotTrade == null) {
						fxspotTrade = new FXTrade();
					}

					fxspotTrade.setCurrencyOne(CurrencySQL.getCurrencyById(results.getLong("currency_one_id")));
					fxspotTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					fxspotTrade.setAmountOne(results.getBigDecimal("amount_one"));
					fxspotTrade.setId(results.getLong("id"));
					fxspotTrade.setAmount(results.getBigDecimal("amount"));
					fxspotTrade.setBuySell(results.getBoolean("buy_sell"));
					fxspotTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					fxspotTrade.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					Date settlementDate = results.getDate("settlement_date");
					if (settlementDate != null) {
						fxspotTrade.setSettlementDate(settlementDate.toLocalDate());
					}
					Date tradeDate = results.getDate("trade_date");
					if (tradeDate != null) {
						fxspotTrade.setTradeDate(tradeDate.toLocalDate());
					}
					fxspotTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
				}
			}
			if (fxspotTrade != null) {
				try {
					fxTradeBusinessDelegate.determinateType(fxspotTrade);
				} catch (TradistaBusinessException tbe) {
					// Should not appear here.
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return fxspotTrade;
	}

	public static FXTrade getTrade(ResultSet rs) {

		FXTrade fxspotTrade = null;
		FXTradeBusinessDelegate fxTradeBusinessDelegate = new FXTradeBusinessDelegate();
		try {

			// We ensure that the deal is a FX Spot.
			if (rs.getLong("fxspot_trade_id") == 0) {
				return null;
			}

			fxspotTrade = new FXTrade();

			fxspotTrade.setCurrencyOne(CurrencySQL.getCurrencyById(rs.getLong("fxspot_currency_one_id")));
			fxspotTrade.setAmountOne(rs.getBigDecimal("amount_one"));

			TradeSQL.setTradeCommonFields(fxspotTrade, rs);

			fxTradeBusinessDelegate.determinateType(fxspotTrade);

		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return fxspotTrade;
	}

	public static long saveFXTrade(FXTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, SETTLEMENT_DATE, PRODUCT_ID, COUNTERPARTY_ID, CURRENCY_ID, AMOUNT, BOOK_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, SETTLEMENT_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, CURRENCY_ID=?, AMOUNT=?, BOOK_ID=? WHERE ID = ?");
				PreparedStatement stmtSaveFXSpotTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO FXSPOT_TRADE(CURRENCY_ONE_ID, AMOUNT_ONE, FXSPOT_TRADE_ID) VALUES (?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE FXSPOT_TRADE SET CURRENCY_ONE_ID=?, AMOUNT_ONE=? WHERE FXSPOT_TRADE_ID=?")) {
			boolean isBuy = trade.isBuy();
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(9, java.sql.Date.valueOf(LocalDate.now()));
			} else {
				stmtSaveTrade.setLong(9, trade.getId());
			}
			stmtSaveTrade.setBoolean(1, isBuy);
			if (trade.getTradeDate() == null) {
				stmtSaveTrade.setNull(2, java.sql.Types.DATE);
			} else {
				stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			}
			if (trade.getSettlementDate() == null) {
				stmtSaveTrade.setNull(3, java.sql.Types.DATE);
			} else {
				stmtSaveTrade.setDate(3, java.sql.Date.valueOf(trade.getSettlementDate()));
			}
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
						throw new SQLException("Creating user failed, no generated key obtained.");
					}
				}
			} else {
				tradeId = trade.getId();
			}

			stmtSaveFXSpotTrade.setLong(1, trade.getCurrencyOne().getId());
			stmtSaveFXSpotTrade.setBigDecimal(2, trade.getAmountOne());
			stmtSaveFXSpotTrade.setLong(3, tradeId);
			stmtSaveFXSpotTrade.executeUpdate();
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}

}