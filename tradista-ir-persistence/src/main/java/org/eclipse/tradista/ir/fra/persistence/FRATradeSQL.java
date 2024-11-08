package org.eclipse.tradista.ir.fra.persistence;

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
import org.eclipse.tradista.core.daycountconvention.persistence.DayCountConventionSQL;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.ir.fra.model.FRATrade;

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

public class FRATradeSQL {

	public static FRATrade getTradeById(long id) {

		FRATrade fraTrade = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con
						.prepareStatement("SELECT * FROM IRFORWARD_TRADE, TRADE, FRA_TRADE WHERE "
								+ "IRFORWARD_TRADE_ID = ? AND IRFORWARD_TRADE_ID = FRA_TRADE_ID AND FRA_TRADE_ID = TRADE.ID ")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (fraTrade == null) {
						fraTrade = new FRATrade();
					}

					fraTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					fraTrade.setId(results.getLong("id"));
					fraTrade.setBuySell(results.getBoolean("buy_sell"));
					fraTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					java.sql.Date tradeDate = results.getDate("trade_date");
					if (tradeDate != null) {
						fraTrade.setTradeDate(tradeDate.toLocalDate());
					}
					java.sql.Date settlementDate = results.getDate("settlement_date");
					if (settlementDate != null) {
						fraTrade.setSettlementDate(settlementDate.toLocalDate());
					}
					java.sql.Date maturityDate = results.getDate("maturity_date");
					if (maturityDate != null) {
						fraTrade.setMaturityDate(maturityDate.toLocalDate());
					}
					fraTrade.setStartDate(results.getDate("start_date").toLocalDate());
					fraTrade.setAmount(results.getBigDecimal("amount"));
					fraTrade.setReferenceRateIndex(IndexSQL.getIndexById(results.getLong("reference_rate_index_id")));
					fraTrade.setReferenceRateIndexTenor(Tenor.valueOf(results.getString("reference_rate_index_tenor")));
					fraTrade.setDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong("day_count_convention_id")));
					fraTrade.setFixedRate(results.getBigDecimal("fixed_rate"));
					fraTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					fraTrade.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return fraTrade;
	}

	public static FRATrade getTrade(ResultSet rs) {

		FRATrade fraTrade = null;
		try {
			if (rs.getLong("fra_trade_id") == 0) {
				return null;
			}

			fraTrade = new FRATrade();

			java.sql.Date maturityDate = rs.getDate("irforward_maturity_date");
			if (maturityDate != null) {
				fraTrade.setMaturityDate(maturityDate.toLocalDate());
			}

			fraTrade.setReferenceRateIndex(IndexSQL.getIndexById(rs.getLong("irforward_reference_rate_index_id")));
			fraTrade.setReferenceRateIndexTenor(Tenor.valueOf(rs.getString("irforward_reference_rate_index_tenor")));
			fraTrade.setDayCountConvention(
					DayCountConventionSQL.getDayCountConventionById(rs.getLong("irforward_day_count_convention_id")));
			fraTrade.setFixedRate(rs.getBigDecimal("fra_fixed_rate"));
			fraTrade.setStartDate(rs.getDate("start_date").toLocalDate());

			// Commmon fields
			TradeSQL.setTradeCommonFields(fraTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return fraTrade;
	}

	public static long saveFRATrade(FRATrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, SETTLEMENT_DATE, PRODUCT_ID, COUNTERPARTY_ID, CURRENCY_ID, AMOUNT, BOOK_ID,  CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, SETTLEMENT_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, CURRENCY_ID=?, AMOUNT=?, BOOK_ID=? WHERE ID=?");
				PreparedStatement stmtSaveIRForwardTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO IRFORWARD_TRADE(MATURITY_DATE, REFERENCE_RATE_INDEX_ID, REFERENCE_RATE_INDEX_TENOR, DAY_COUNT_CONVENTION_ID, IRFORWARD_TRADE_ID) VALUES (?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE IRFORWARD_TRADE SET MATURITY_DATE=?, REFERENCE_RATE_INDEX_ID=?, REFERENCE_RATE_INDEX_TENOR=?, DAY_COUNT_CONVENTION_ID=? WHERE IRFORWARD_TRADE_ID=?");
				PreparedStatement stmtSaveFRATrade = (trade.getId() == 0)
						? con.prepareStatement(
								"INSERT INTO FRA_TRADE(FIXED_RATE, START_DATE, FRA_TRADE_ID) VALUES (?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE FRA_TRADE SET FIXED_RATE=?, START_DATE=? WHERE FRA_TRADE_ID=?")) {
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

			stmtSaveIRForwardTrade.setDate(1, java.sql.Date.valueOf(trade.getMaturityDate()));
			stmtSaveIRForwardTrade.setLong(2, trade.getReferenceRateIndex().getId());
			stmtSaveIRForwardTrade.setString(3, trade.getReferenceRateIndexTenor().name());
			stmtSaveIRForwardTrade.setLong(4, trade.getDayCountConvention().getId());
			stmtSaveIRForwardTrade.setLong(5, tradeId);
			stmtSaveIRForwardTrade.executeUpdate();

			stmtSaveFRATrade.setBigDecimal(1, trade.getFixedRate());
			stmtSaveFRATrade.setDate(2, java.sql.Date.valueOf(trade.getStartDate()));
			stmtSaveFRATrade.setLong(3, tradeId);
			stmtSaveFRATrade.executeUpdate();

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}
}