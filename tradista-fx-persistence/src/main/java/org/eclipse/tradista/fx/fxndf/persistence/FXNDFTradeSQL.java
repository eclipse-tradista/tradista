package org.eclipse.tradista.fx.fxndf.persistence;

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
import org.eclipse.tradista.fx.fxndf.model.FXNDFTrade;

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

public class FXNDFTradeSQL {

	public static FXNDFTrade getTradeById(long id) {

		FXNDFTrade fxndfTrade = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(
						"SELECT * FROM TRADE, FXNDF_TRADE WHERE " + "FXNDF_TRADE_ID = ? AND ID = FXNDF_TRADE_ID")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {

				while (results.next()) {

					if (fxndfTrade == null) {
						fxndfTrade = new FXNDFTrade();
					}

					fxndfTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					fxndfTrade.setNonDeliverableCurrency(
							CurrencySQL.getCurrencyById(results.getLong("non_deliverable_currency_id")));
					fxndfTrade.setNdfRate(results.getBigDecimal("ndf_rate"));
					fxndfTrade.setAmount(results.getBigDecimal("amount"));
					fxndfTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					fxndfTrade.setBuySell(results.getBoolean("buy_sell"));
					fxndfTrade.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					fxndfTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					fxndfTrade.setId(results.getLong("id"));
					fxndfTrade.setSettlementDate(results.getDate("settlement_date").toLocalDate());
					fxndfTrade.setTradeDate(results.getDate("trade_date").toLocalDate());
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return fxndfTrade;
	}

	public static FXNDFTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		FXNDFTrade fxndfTrade = null;
		try {
			if (rs.getLong("fxndf_trade_id") == 0) {
				return null;
			}

			fxndfTrade = new FXNDFTrade();
			fxndfTrade
					.setNonDeliverableCurrency(CurrencySQL.getCurrencyById(rs.getLong("non_deliverable_currency_id")));
			fxndfTrade.setNdfRate(rs.getBigDecimal("ndf_rate"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(fxndfTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return fxndfTrade;
	}

	public static long saveFXNDFTrade(FXNDFTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, SETTLEMENT_DATE, PRODUCT_ID, COUNTERPARTY_ID, CURRENCY_ID, AMOUNT, BOOK_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, SETTLEMENT_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, CURRENCY_ID=?, AMOUNT=?, BOOK_ID=? WHERE ID=?");
				PreparedStatement stmtSaveFXNDFTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO FXNDF_TRADE(NON_DELIVERABLE_CURRENCY_ID, NDF_RATE, FXNDF_TRADE_ID) VALUES (?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE FXNDF_TRADE SET NON_DELIVERABLE_CURRENCY_ID=?, NDF_RATE=? WHERE FXNDF_TRADE_ID=?")) {
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
						throw new SQLException("Creating trade failed, no generated key obtained.");
					}
				}
			} else {
				tradeId = trade.getId();
			}

			stmtSaveFXNDFTrade.setLong(1, trade.getNonDeliverableCurrency().getId());
			stmtSaveFXNDFTrade.setBigDecimal(2, trade.getNdfRate());
			stmtSaveFXNDFTrade.setLong(3, tradeId);
			stmtSaveFXNDFTrade.executeUpdate();
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}

}