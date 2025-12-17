package org.eclipse.tradista.security.bond.persistence;

import java.sql.Connection;
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
import org.eclipse.tradista.security.bond.model.BondTrade;

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

public class BondTradeSQL {

	public static BondTrade getTradeById(long id) {
		BondTrade bondTrade = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement("SELECT * FROM BOND_TRADE, TRADE WHERE "
						+ "TRADE.ID = BOND_TRADE.BOND_TRADE_ID AND BOND_TRADE.BOND_TRADE_ID = ? ")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (bondTrade == null) {
						bondTrade = new BondTrade();
					}
					bondTrade.setProduct(BondSQL.getBondById(results.getLong("product_id")));
					bondTrade.setQuantity(results.getBigDecimal("quantity"));
					bondTrade.setAmount(results.getBigDecimal("amount"));
					bondTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					bondTrade.setBuySell(results.getBoolean("buy_sell"));
					bondTrade.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					bondTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					bondTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					bondTrade.setId(results.getLong("id"));
					bondTrade.setSettlementDate(results.getDate("settlement_date").toLocalDate());
					bondTrade.setTradeDate(results.getDate("trade_date").toLocalDate());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bondTrade;
	}

	public static long saveBondTrade(BondTrade trade) {
		long tradeId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, PRODUCT_ID, COUNTERPARTY_ID, AMOUNT, BOOK_ID, SETTLEMENT_DATE, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, AMOUNT=?, BOOK_ID=?, SETTLEMENT_DATE=? WHERE ID = ?");
				PreparedStatement stmtSaveBondTrade = (trade.getId() == 0)
						? con.prepareStatement("INSERT INTO BOND_TRADE(QUANTITY, BOND_TRADE_ID) VALUES (?, ?) ")
						: con.prepareStatement("UPDATE BOND_TRADE SET QUANTITY = ? WHERE BOND_TRADE_ID = ?")) {
			boolean isBuy = trade.isBuy();
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(8, java.sql.Date.valueOf(trade.getCreationDate()));
			} else {
				stmtSaveTrade.setLong(8, trade.getId());
			}
			stmtSaveTrade.setBoolean(1, isBuy);
			stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			stmtSaveTrade.setLong(3, trade.getProductId());
			stmtSaveTrade.setLong(4, trade.getCounterparty().getId());
			stmtSaveTrade.setBigDecimal(5, trade.getAmount());
			stmtSaveTrade.setLong(6, trade.getBook().getId());
			stmtSaveTrade.setDate(7, java.sql.Date.valueOf(trade.getSettlementDate()));
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
			stmtSaveBondTrade.setBigDecimal(1, trade.getQuantity());
			stmtSaveBondTrade.setLong(2, tradeId);
			stmtSaveBondTrade.executeUpdate();
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}

	public static BondTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		BondTrade bondTrade = null;
		try {
			if (rs.getLong("bond_trade_id") == 0) {
				return null;
			}
			bondTrade = new BondTrade();
			bondTrade.setProduct(BondSQL.getBondById(rs.getLong("product_id")));
			bondTrade.setQuantity(rs.getBigDecimal("bond_quantity"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(bondTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return bondTrade;
	}

	public static List<BondTrade> getBondTradesBeforeTradeDateByBondAndBookIds(LocalDate date, long bondId,
			long bookId) {
		List<BondTrade> bondTrades = null;

		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetTradesBeforeTradeDateByBondAndBookIds = con.createStatement()) {
			String query = "SELECT * FROM BOND_TRADE, TRADE WHERE "
					+ "TRADE.ID = BOND_TRADE.BOND_TRADE_ID AND TRADE.TRADE_DATE <= '"
					+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date) + "'";

			if (bondId > 0) {
				query += " AND TRADE.PRODUCT_ID = " + bondId;
			}
			if (bookId > 0) {
				query += " AND TRADE.BOOK_ID = " + bookId;
			}
			try (ResultSet results = stmtGetTradesBeforeTradeDateByBondAndBookIds.executeQuery(query)) {
				while (results.next()) {
					if (bondTrades == null) {
						bondTrades = new ArrayList<>();
					}
					BondTrade bondTrade = new BondTrade();
					bondTrade.setProduct(BondSQL.getBondById(results.getLong("product_id")));
					bondTrade.setQuantity(results.getBigDecimal("quantity"));
					bondTrade.setAmount(results.getBigDecimal("amount"));
					bondTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					bondTrade.setBuySell(results.getBoolean("buy_sell"));
					bondTrade.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					bondTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					bondTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					bondTrade.setId(results.getLong("id"));
					bondTrade.setSettlementDate(results.getDate("settlement_date").toLocalDate());
					bondTrade.setTradeDate(results.getDate("trade_date").toLocalDate());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bondTrades;
	}

}