package org.eclipse.tradista.ir.irforward.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.daycountconvention.persistence.DayCountConventionSQL;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.irforward.model.IRForwardTrade;

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

public class IRForwardTradeSQL {

	public static IRForwardTrade<Product> getTradeById(long id) {
		IRForwardTrade<Product> irforwardTrade = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement("SELECT * FROM IRFORWARD_TRADE, TRADE WHERE "
						+ "IRFORWARD_TRADE_ID = ? AND IRFORWARD_TRADE_ID = ID ")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (irforwardTrade == null) {
						irforwardTrade = new IRForwardTrade<Product>();
					}

					irforwardTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					irforwardTrade.setId(results.getLong("id"));
					irforwardTrade.setBuySell(results.getBoolean("buy_sell"));
					irforwardTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					java.sql.Date tradeDate = results.getDate("trade_date");
					if (tradeDate != null) {
						irforwardTrade.setTradeDate(tradeDate.toLocalDate());
					}
					java.sql.Date settlementDate = results.getDate("settlement_date");
					if (settlementDate != null) {
						irforwardTrade.setSettlementDate(settlementDate.toLocalDate());
					}
					java.sql.Date maturityDate = results.getDate("maturity_date");
					if (maturityDate != null) {
						irforwardTrade.setMaturityDate(maturityDate.toLocalDate());
					}
					irforwardTrade.setAmount(results.getBigDecimal("amount"));
					irforwardTrade.setFrequency(Tenor.valueOf(results.getString("frequency")));
					irforwardTrade
							.setReferenceRateIndex(IndexSQL.getIndexById(results.getLong("reference_rate_index_id")));
					irforwardTrade
							.setReferenceRateIndexTenor(Tenor.valueOf(results.getString("reference_rate_index_tenor")));
					irforwardTrade.setDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong("day_count_convention_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return irforwardTrade;
	}

	public static long saveIRForwardTrade(IRForwardTrade<Product> trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, SETTLEMENT_DATE, PRODUCT_ID, COUNTERPARTY_ID, CURRENCY_ID, AMOUNT, BOOK_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, SETTLEMENT_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, CURRENCY_ID=?, AMOUNT=?, BOOK_ID=? WHERE ID = ?");
				PreparedStatement stmtSaveIRForwardTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO IRFORWARD_TRADE(MATURITY_DATE, FREQUENCY, REFERENCE_RATE_INDEX_ID, REFERENCE_RATE_INDEX_TENOR, DAY_COUNT_CONVENTION_ID, INTEREST_PAYMENT, INTEREST_FIXING, IRFORWARD_TRADE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE IRFORWARD_TRADE SET MATURITY_DATE=?, FREQUENCY=?, REFERENCE_RATE_INDEX_ID=?, REFERENCE_RATE_INDEX_TENOR=?, DAY_COUNT_CONVENTION_ID=?, INTEREST_PAYMENT=?, INTEREST_FIXING=? WHERE IRFORWARD_TRADE_ID = ?")) {
			boolean isBuy = trade.isBuy();
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(9, java.sql.Date.valueOf(LocalDate.now()));
			} else {
				stmtSaveTrade.setLong(9, trade.getId());
			}
			stmtSaveTrade.setBoolean(1, isBuy);

			if (trade.getTradeDate() != null) {
				stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			} else {
				stmtSaveTrade.setNull(2, java.sql.Types.DATE);
			}
			stmtSaveTrade.setDate(3, java.sql.Date.valueOf(trade.getSettlementDate()));
			stmtSaveTrade.setLong(5, trade.getCounterparty().getId());
			stmtSaveTrade.setLong(6, trade.getCurrency().getId());
			stmtSaveTrade.setNull(4, java.sql.Types.BIGINT);
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

			stmtSaveIRForwardTrade.setDate(1, java.sql.Date.valueOf(trade.getMaturityDate()));
			stmtSaveIRForwardTrade.setString(2, trade.getFrequency().name());
			stmtSaveIRForwardTrade.setLong(3, trade.getReferenceRateIndex().getId());
			stmtSaveIRForwardTrade.setString(4, trade.getReferenceRateIndexTenor().name());
			stmtSaveIRForwardTrade.setLong(5, trade.getDayCountConvention().getId());
			stmtSaveIRForwardTrade.setString(6, trade.getInterestPayment().name());
			stmtSaveIRForwardTrade.setString(7, trade.getInterestFixing().name());
			stmtSaveIRForwardTrade.setLong(8, tradeId);
			stmtSaveIRForwardTrade.executeUpdate();

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}
}