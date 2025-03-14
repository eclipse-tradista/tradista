package org.eclipse.tradista.ir.ircapfloorcollar.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.daycountconvention.persistence.DayCountConventionSQL;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;
import org.eclipse.tradista.ir.irforward.model.IRForwardTrade;
import org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL;

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

public class IRCapFloorCollarTradeSQL {

	public static IRCapFloorCollarTrade getTradeById(long id) {
		IRCapFloorCollarTrade irCapFloorCollarTrade = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement("SELECT TRADE.*, "
						+ "IRCAP_FLOOR_COLLAR_TRADE.*," + " FWD_TRADE.AMOUNT FWD_AMOUNT," + " IRFORWARD_TRADE.*"
						+ "  FROM TRADE, IRCAP_FLOOR_COLLAR_TRADE, IRFORWARD_TRADE, TRADE FWD_TRADE WHERE "
						+ "TRADE.ID = IRCAP_FLOOR_COLLAR_TRADE_ID " + "AND IRCAP_FLOOR_COLLAR_TRADE_ID = ?"
						+ " AND IRCAP_FLOOR_COLLAR_TRADE.IRFORWARD_TRADE_ID = IRFORWARD_TRADE.IRFORWARD_TRADE_ID"
						+ " AND IRFORWARD_TRADE.IRFORWARD_TRADE_ID = FWD_TRADE.ID")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (irCapFloorCollarTrade == null) {
						irCapFloorCollarTrade = new IRCapFloorCollarTrade();
					}

					irCapFloorCollarTrade.setId(id);
					irCapFloorCollarTrade.setAmount(results.getBigDecimal("amount"));
					irCapFloorCollarTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					irCapFloorCollarTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					irCapFloorCollarTrade.setBuySell(results.getBoolean("BUY_SELL"));
					irCapFloorCollarTrade.setCapStrike(results.getBigDecimal("CAP_STRIKE"));
					irCapFloorCollarTrade.setFloorStrike(results.getBigDecimal("FLOOR_STRIKE"));
					irCapFloorCollarTrade.setCreationDate(results.getDate("CREATION_DATE").toLocalDate());
					java.sql.Date tradeDate = results.getDate("trade_date");
					if (tradeDate != null) {
						irCapFloorCollarTrade.setTradeDate(tradeDate.toLocalDate());
					}
					java.sql.Date settlementDate = results.getDate("settlement_date");
					if (settlementDate != null) {
						irCapFloorCollarTrade.setSettlementDate(settlementDate.toLocalDate());
					}
					irCapFloorCollarTrade
							.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));

					// Building the IRForward
					IRForwardTrade<Product> irForward = new IRForwardTrade<>();
					irForward.setId(results.getLong("IRFORWARD_TRADE_ID"));
					irForward.setBook(BookSQL.getBookById(results.getLong("book_id")));
					irForward.setAmount(results.getBigDecimal("FWD_AMOUNT"));
					irForward.setBuySell(results.getBoolean("BUY_SELL"));
					irForward.setCreationDate(results.getDate("CREATION_DATE").toLocalDate());
					irForward.setCurrency(CurrencySQL.getCurrencyById(results.getLong("CURRENCY_ID")));
					irForward.setFrequency(Tenor.valueOf(results.getString("frequency")));
					java.sql.Date maturityDate = results.getDate("maturity_date");
					if (maturityDate != null) {
						irForward.setMaturityDate(maturityDate.toLocalDate());
					}
					irForward.setDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong("day_count_convention_id")));
					irForward.setReferenceRateIndex(IndexSQL.getIndexById(results.getLong("reference_rate_index_id")));
					irForward
							.setReferenceRateIndexTenor(Tenor.valueOf(results.getString("reference_rate_index_tenor")));
					irForward.setInterestPayment(InterestPayment.valueOf(results.getString("interest_payment")));
					irForward.setInterestFixing(InterestPayment.valueOf(results.getString("interest_fixing")));
					tradeDate = results.getDate("trade_date");
					if (tradeDate != null) {
						irForward.setTradeDate(tradeDate.toLocalDate());
					}
					settlementDate = results.getDate("settlement_date");
					if (settlementDate != null) {
						irForward.setSettlementDate(settlementDate.toLocalDate());
					}
					irForward.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));

					irCapFloorCollarTrade.setIrForwardTrade(irForward);

				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return irCapFloorCollarTrade;
	}

	public static IRCapFloorCollarTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		IRCapFloorCollarTrade irCapFloorCollarTrade = null;
		try {
			if (rs.getLong("ircap_floor_collar_trade_id") == 0) {
				return null;
			}
			irCapFloorCollarTrade = new IRCapFloorCollarTrade();
			irCapFloorCollarTrade.setCapStrike(rs.getBigDecimal("CAP_STRIKE"));
			irCapFloorCollarTrade.setFloorStrike(rs.getBigDecimal("FLOOR_STRIKE"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(irCapFloorCollarTrade, rs);

			// Building the IRForward
			IRForwardTrade<Product> irForward = new IRForwardTrade<>();
			irForward.setId(rs.getLong("UND_IRFORWARD_ID"));
			irForward.setAmount(rs.getBigDecimal("UND_IRFORWARD_AMOUNT"));
			irForward.setBook(BookSQL.getBookById(rs.getLong("UND_IRFORWARD_BOOK_ID")));
			irForward.setBuySell(rs.getBoolean("UND_IRFORWARD_BUY_SELL"));
			irForward.setCreationDate(rs.getDate("UND_IRFORWARD_CREATION_DATE").toLocalDate());
			irForward.setCurrency(CurrencySQL.getCurrencyById(rs.getLong("UND_IRFORWARD_CURRENCY_ID")));
			irForward.setFrequency(Tenor.valueOf(rs.getString("fwd_frequency")));
			java.sql.Date maturityDate = rs.getDate("fwd_maturity_date");
			if (maturityDate != null) {
				irForward.setMaturityDate(maturityDate.toLocalDate());
			}
			irForward.setDayCountConvention(
					DayCountConventionSQL.getDayCountConventionById(rs.getLong("fwd_day_count_convention_id")));
			irForward.setReferenceRateIndex(IndexSQL.getIndexById(rs.getLong("fwd_reference_rate_index_id")));
			irForward.setReferenceRateIndexTenor(Tenor.valueOf(rs.getString("fwd_reference_rate_index_tenor")));
			irForward.setInterestPayment(InterestPayment.valueOf(rs.getString("fwd_interest_payment")));
			irForward.setInterestFixing(InterestPayment.valueOf(rs.getString("fwd_interest_fixing")));
			Date undTradeDate = rs.getDate("und_irforward_trade_date");
			if (undTradeDate != null) {
				irForward.setTradeDate(undTradeDate.toLocalDate());
			}
			Date undSettlementDate = rs.getDate("und_irforward_settlement_date");
			if (undSettlementDate != null) {
				irForward.setSettlementDate(undSettlementDate.toLocalDate());
			}
			irForward.setCounterparty(irCapFloorCollarTrade.getCounterparty());

			irCapFloorCollarTrade.setIrForwardTrade(irForward);
		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return irCapFloorCollarTrade;
	}

	public static long saveIRCapFloorCollarTrade(IRCapFloorCollarTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, SETTLEMENT_DATE, PRODUCT_ID, COUNTERPARTY_ID, AMOUNT, CURRENCY_ID, BOOK_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, SETTLEMENT_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, AMOUNT=?, CURRENCY_ID=?, BOOK_ID=? WHERE ID = ?");
				PreparedStatement stmtSaveIRCapFloorCollarTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO IRCAP_FLOOR_COLLAR_TRADE(CAP_STRIKE, FLOOR_STRIKE, IRFORWARD_TRADE_ID, IRCAP_FLOOR_COLLAR_TRADE_ID) VALUES (?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE IRCAP_FLOOR_COLLAR_TRADE SET CAP_STRIKE=?, FLOOR_STRIKE=?, IRFORWARD_TRADE_ID=? WHERE IRCAP_FLOOR_COLLAR_TRADE_ID = ?")) {
			boolean isBuy = trade.isBuy();
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(9, java.sql.Date.valueOf(LocalDate.now()));
			} else {
				stmtSaveTrade.setLong(9, trade.getId());
			}
			stmtSaveTrade.setBoolean(1, isBuy);
			stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			stmtSaveTrade.setDate(3, java.sql.Date.valueOf(trade.getSettlementDate()));
			stmtSaveTrade.setLong(5, trade.getCounterparty().getId());
			stmtSaveTrade.setNull(4, java.sql.Types.BIGINT);
			stmtSaveTrade.setBigDecimal(6, trade.getAmount());
			stmtSaveTrade.setLong(7, trade.getCurrency().getId());
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

			// IR Forward saving
			long irForwardId = IRForwardTradeSQL.saveIRForwardTrade(trade.getIrForwardTrade());

			if (trade.getCapStrike() == null) {
				stmtSaveIRCapFloorCollarTrade.setNull(1, Types.DECIMAL);
			} else {
				stmtSaveIRCapFloorCollarTrade.setBigDecimal(1, trade.getCapStrike());
			}
			if (trade.getFloorStrike() == null) {
				stmtSaveIRCapFloorCollarTrade.setNull(2, Types.DECIMAL);
			} else {
				stmtSaveIRCapFloorCollarTrade.setBigDecimal(2, trade.getFloorStrike());
			}
			stmtSaveIRCapFloorCollarTrade.setLong(3, irForwardId);
			stmtSaveIRCapFloorCollarTrade.setLong(4, tradeId);
			stmtSaveIRCapFloorCollarTrade.executeUpdate();

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}

}