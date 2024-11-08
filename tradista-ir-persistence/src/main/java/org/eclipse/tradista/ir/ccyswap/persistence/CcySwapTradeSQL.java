package org.eclipse.tradista.ir.ccyswap.persistence;

import java.sql.Connection;
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
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;

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

public class CcySwapTradeSQL {

	public static CcySwapTrade getTradeById(long id) {

		CcySwapTrade ccyswapTrade = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con
						.prepareStatement("SELECT * FROM CCYSWAP_TRADE, IRSWAP_TRADE, TRADE WHERE "
								+ "CCYSWAP_TRADE_ID = ? AND IRSWAP_TRADE_ID = ID AND IRSWAP_TRADE_ID = CCYSWAP_TRADE_ID")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (ccyswapTrade == null) {
						ccyswapTrade = new CcySwapTrade();
					}

					ccyswapTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					ccyswapTrade.setId(results.getLong("id"));
					ccyswapTrade.setBuySell(results.getBoolean("buy_sell"));
					ccyswapTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					java.sql.Date tradeDate = results.getDate("trade_date");
					if (tradeDate != null) {
						ccyswapTrade.setTradeDate(tradeDate.toLocalDate());
					}
					java.sql.Date settlementDate = results.getDate("settlement_date");
					if (settlementDate != null) {
						ccyswapTrade.setSettlementDate(settlementDate.toLocalDate());
					}
					java.sql.Date maturityDate = results.getDate("maturity_date");
					if (maturityDate != null) {
						ccyswapTrade.setMaturityDate(maturityDate.toLocalDate());
					}
					String maturityTenorString = results.getString("maturity_tenor");
					if (maturityTenorString != null) {
						ccyswapTrade.setMaturityTenor(Tenor.valueOf(maturityTenorString));
					}
					ccyswapTrade.setAmount(results.getBigDecimal("amount"));
					ccyswapTrade.setPaymentFrequency(Tenor.valueOf(results.getString("payment_frequency")));
					ccyswapTrade.setReceptionFrequency(Tenor.valueOf(results.getString("reception_frequency")));
					ccyswapTrade.setPaymentSpread(results.getBigDecimal("payment_spread"));
					ccyswapTrade.setReceptionSpread(results.getBigDecimal("reception_spread"));
					ccyswapTrade.setCurrencyTwo(CurrencySQL.getCurrencyById(results.getLong("currency_two_id")));
					ccyswapTrade.setNotionalAmountTwo(results.getBigDecimal("notional_amount_two"));
					ccyswapTrade.setPaymentFixedInterestRate(results.getBigDecimal("payment_fixed_interest_rate"));
					ccyswapTrade.setReceptionReferenceRateIndex(
							IndexSQL.getIndexById(results.getLong("reception_reference_rate_index_id")));
					ccyswapTrade.setReceptionReferenceRateIndexTenor(
							Tenor.valueOf(results.getString("reception_reference_rate_index_tenor")));
					ccyswapTrade.setInterestsToPayFixed(results.getLong("payment_reference_rate_index_id") == 0);
					if (!ccyswapTrade.isInterestsToPayFixed()) {
						ccyswapTrade.setPaymentReferenceRateIndexTenor(
								Tenor.valueOf(results.getString("payment_reference_rate_index_tenor")));
						ccyswapTrade.setPaymentReferenceRateIndex(
								IndexSQL.getIndexById(results.getLong("payment_reference_rate_index_id")));
						ccyswapTrade.setPaymentInterestFixing(
								InterestPayment.valueOf(results.getString("payment_interest_fixing")));
					}
					ccyswapTrade.setPaymentDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong("payment_day_count_convention_id")));
					ccyswapTrade.setReceptionDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong("reception_day_count_convention_id")));
					ccyswapTrade.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					ccyswapTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					ccyswapTrade.setPaymentInterestPayment(
							InterestPayment.valueOf(results.getString("payment_interest_payment")));
					ccyswapTrade.setReceptionInterestPayment(
							InterestPayment.valueOf(results.getString("reception_interest_payment")));
					ccyswapTrade.setReceptionInterestFixing(
							InterestPayment.valueOf(results.getString("reception_interest_fixing")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return ccyswapTrade;
	}

	public static CcySwapTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		CcySwapTrade ccyswapTrade = null;
		try {
			if (rs.getLong("ccyswap_trade_id") == 0) {
				return null;
			}

			ccyswapTrade = new CcySwapTrade();
			java.sql.Date maturityDate = rs.getDate("irswap_maturity_date");
			if (maturityDate != null) {
				ccyswapTrade.setMaturityDate(maturityDate.toLocalDate());
			}
			String maturityTenorString = rs.getString("maturity_tenor");
			if (maturityTenorString != null) {
				ccyswapTrade.setMaturityTenor(Tenor.valueOf(maturityTenorString));
			}
			ccyswapTrade.setPaymentFrequency(Tenor.valueOf(rs.getString("irswap_payment_frequency")));
			ccyswapTrade.setReceptionFrequency(Tenor.valueOf(rs.getString("irswap_reception_frequency")));
			ccyswapTrade.setPaymentSpread(rs.getBigDecimal("payment_spread"));
			ccyswapTrade.setReceptionSpread(rs.getBigDecimal("reception_spread"));
			ccyswapTrade.setCurrencyTwo(CurrencySQL.getCurrencyById(rs.getLong("currency_two_id")));
			ccyswapTrade.setNotionalAmountTwo(rs.getBigDecimal("notional_amount_two"));
			ccyswapTrade.setPaymentFixedInterestRate(rs.getBigDecimal("payment_fixed_interest_rate"));
			ccyswapTrade.setReceptionReferenceRateIndex(
					IndexSQL.getIndexById(rs.getLong("reception_reference_rate_index_id")));
			ccyswapTrade.setReceptionReferenceRateIndexTenor(
					Tenor.valueOf(rs.getString("reception_reference_rate_index_tenor")));
			ccyswapTrade.setInterestsToPayFixed(rs.getLong("payment_reference_rate_index_id") == 0);
			if (!ccyswapTrade.isInterestsToPayFixed()) {
				ccyswapTrade.setPaymentReferenceRateIndexTenor(
						Tenor.valueOf(rs.getString("payment_reference_rate_index_tenor")));
				ccyswapTrade.setPaymentReferenceRateIndex(
						IndexSQL.getIndexById(rs.getLong("payment_reference_rate_index_id")));
				ccyswapTrade.setPaymentInterestFixing(InterestPayment.valueOf(rs.getString("payment_interest_fixing")));
			}
			ccyswapTrade.setPaymentDayCountConvention(
					DayCountConventionSQL.getDayCountConventionById(rs.getLong("payment_day_count_convention_id")));
			ccyswapTrade.setReceptionDayCountConvention(
					DayCountConventionSQL.getDayCountConventionById(rs.getLong("reception_day_count_convention_id")));

			ccyswapTrade.setPaymentInterestPayment(InterestPayment.valueOf(rs.getString("payment_interest_payment")));
			ccyswapTrade
					.setReceptionInterestPayment(InterestPayment.valueOf(rs.getString("reception_interest_payment")));
			ccyswapTrade.setReceptionInterestFixing(InterestPayment.valueOf(rs.getString("reception_interest_fixing")));

			// Commmon fields
			TradeSQL.setTradeCommonFields(ccyswapTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return ccyswapTrade;
	}

	public static long saveCcySwapTrade(CcySwapTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, SETTLEMENT_DATE, PRODUCT_ID, COUNTERPARTY_ID, CURRENCY_ID, AMOUNT, BOOK_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, SETTLEMENT_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, CURRENCY_ID=?, AMOUNT=?, BOOK_ID=? WHERE ID = ?");
				PreparedStatement stmtSaveIrSwapTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO IRSWAP_TRADE(MATURITY_DATE, PAYMENT_FREQUENCY, RECEPTION_FREQUENCY, PAYMENT_FIXED_INTEREST_RATE, PAYMENT_REFERENCE_RATE_INDEX_ID, RECEPTION_REFERENCE_RATE_INDEX_ID, PAYMENT_REFERENCE_RATE_INDEX_TENOR, RECEPTION_REFERENCE_RATE_INDEX_TENOR, PAYMENT_SPREAD, RECEPTION_SPREAD, PAYMENT_DAY_COUNT_CONVENTION_ID, RECEPTION_DAY_COUNT_CONVENTION_ID, MATURITY_TENOR, PAYMENT_INTEREST_PAYMENT, RECEPTION_INTEREST_PAYMENT, PAYMENT_INTEREST_FIXING, RECEPTION_INTEREST_FIXING, IRSWAP_TRADE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE IRSWAP_TRADE SET MATURITY_DATE=?, PAYMENT_FREQUENCY=?, RECEPTION_FREQUENCY=?, PAYMENT_FIXED_INTEREST_RATE=?, PAYMENT_REFERENCE_RATE_INDEX_ID=?, RECEPTION_REFERENCE_RATE_INDEX_ID=?, PAYMENT_REFERENCE_RATE_INDEX_TENOR=?, RECEPTION_REFERENCE_RATE_INDEX_TENOR=?, PAYMENT_SPREAD=?, RECEPTION_SPREAD=?, PAYMENT_DAY_COUNT_CONVENTION_ID=?, RECEPTION_DAY_COUNT_CONVENTION_ID=?, MATURITY_TENOR=?, PAYMENT_INTEREST_PAYMENT=?, RECEPTION_INTEREST_PAYMENT=?, PAYMENT_INTEREST_FIXING=?, RECEPTION_INTEREST_FIXING=? WHERE IRSWAP_TRADE_ID = ?");
				PreparedStatement stmtSaveCcySwapTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO CCYSWAP_TRADE(CURRENCY_TWO_ID, NOTIONAL_AMOUNT_TWO, CCYSWAP_TRADE_ID) VALUES (?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE CCYSWAP_TRADE SET CURRENCY_TWO_ID=?, NOTIONAL_AMOUNT_TWO=? WHERE CCYSWAP_TRADE_ID = ?")) {
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(9, java.sql.Date.valueOf(LocalDate.now()));
			} else {
				stmtSaveTrade.setLong(9, trade.getId());
			}
			stmtSaveTrade.setBoolean(1, trade.isBuy());
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

			stmtSaveIrSwapTrade.setDate(1, java.sql.Date.valueOf(trade.getMaturityDate()));
			stmtSaveIrSwapTrade.setString(2, trade.getPaymentFrequency().name());
			stmtSaveIrSwapTrade.setString(3, trade.getReceptionFrequency().name());
			if (trade.isInterestsToPayFixed()) {
				stmtSaveIrSwapTrade.setBigDecimal(4, trade.getPaymentFixedInterestRate());
				stmtSaveIrSwapTrade.setNull(5, java.sql.Types.BIGINT);
				stmtSaveIrSwapTrade.setNull(7, java.sql.Types.VARCHAR);
				stmtSaveIrSwapTrade.setNull(16, java.sql.Types.VARCHAR);
			} else {
				stmtSaveIrSwapTrade.setNull(4, java.sql.Types.BIGINT);
				stmtSaveIrSwapTrade.setLong(5, trade.getPaymentReferenceRateIndex().getId());
				stmtSaveIrSwapTrade.setString(7, trade.getPaymentReferenceRateIndexTenor().name());
				stmtSaveIrSwapTrade.setString(16, trade.getPaymentInterestFixing().name());
			}
			stmtSaveIrSwapTrade.setLong(6, trade.getReceptionReferenceRateIndex().getId());
			stmtSaveIrSwapTrade.setString(8, trade.getReceptionReferenceRateIndexTenor().name());
			stmtSaveIrSwapTrade.setBigDecimal(9, trade.getPaymentSpread());
			stmtSaveIrSwapTrade.setBigDecimal(10, trade.getReceptionSpread());
			stmtSaveIrSwapTrade.setLong(11, trade.getPaymentDayCountConvention().getId());
			stmtSaveIrSwapTrade.setLong(12, trade.getReceptionDayCountConvention().getId());
			if (trade.getMaturityTenor() != null) {
				stmtSaveIrSwapTrade.setString(13, trade.getMaturityTenor().name());
			} else {
				stmtSaveIrSwapTrade.setNull(13, Types.VARCHAR);
			}
			stmtSaveIrSwapTrade.setString(14, trade.getPaymentInterestPayment().name());
			stmtSaveIrSwapTrade.setString(15, trade.getReceptionInterestPayment().name());
			stmtSaveIrSwapTrade.setString(17, trade.getReceptionInterestFixing().name());
			stmtSaveIrSwapTrade.setLong(18, tradeId);
			stmtSaveIrSwapTrade.executeUpdate();

			stmtSaveCcySwapTrade.setLong(1, trade.getCurrencyTwo().getId());
			stmtSaveCcySwapTrade.setBigDecimal(2, trade.getNotionalAmountTwo());
			stmtSaveCcySwapTrade.setLong(3, tradeId);
			stmtSaveCcySwapTrade.executeUpdate();

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}
}