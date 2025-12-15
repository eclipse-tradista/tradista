package org.eclipse.tradista.ir.irswap.persistence;

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
import org.eclipse.tradista.ir.irswap.model.IRSwapTrade;
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;

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

public class IRSwapTradeSQL {

	public static SingleCurrencyIRSwapTrade getTradeById(long id, boolean includeUnderlying) {
		SingleCurrencyIRSwapTrade irswapTrade = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = (includeUnderlying)
						? con.prepareStatement("SELECT * FROM IRSWAP_TRADE, TRADE WHERE "
								+ "IRSWAP_TRADE_ID = ? AND IRSWAP_TRADE_ID = ID AND NOT EXISTS (SELECT 1 FROM CCYSWAP_TRADE WHERE  CCYSWAP_TRADE_ID = ID)")
						: con.prepareStatement("SELECT * FROM IRSWAP_TRADE, TRADE WHERE "
								+ "IRSWAP_TRADE_ID = ? AND IRSWAP_TRADE_ID = ID AND NOT EXISTS (SELECT 1 FROM CCYSWAP_TRADE WHERE  CCYSWAP_TRADE_ID = ID)"
								+ " AND TRADE.TRADE_DATE IS NOT NULL")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {

				while (results.next()) {
					if (irswapTrade == null) {
						irswapTrade = new SingleCurrencyIRSwapTrade();
					}

					irswapTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					irswapTrade.setId(results.getLong("id"));
					irswapTrade.setBuySell(results.getBoolean("buy_sell"));
					irswapTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					java.sql.Date tradeDate = results.getDate("trade_date");
					if (tradeDate != null) {
						irswapTrade.setTradeDate(tradeDate.toLocalDate());
					}
					java.sql.Date settlementDate = results.getDate("settlement_date");
					if (settlementDate != null) {
						irswapTrade.setSettlementDate(settlementDate.toLocalDate());
					}
					java.sql.Date maturityDate = results.getDate("maturity_date");
					if (maturityDate != null) {
						irswapTrade.setMaturityDate(maturityDate.toLocalDate());
					}
					String maturityTenorString = results.getString("maturity_tenor");
					if (maturityTenorString != null) {
						irswapTrade.setMaturityTenor(Tenor.valueOf(maturityTenorString));
					}
					irswapTrade.setAmount(results.getBigDecimal("amount"));
					irswapTrade.setPaymentFrequency(Tenor.valueOf(results.getString("payment_frequency")));
					irswapTrade.setReceptionFrequency(Tenor.valueOf(results.getString("reception_frequency")));
					irswapTrade.setReceptionSpread(results.getBigDecimal("reception_spread"));
					irswapTrade.setPaymentFixedInterestRate(results.getBigDecimal("payment_fixed_interest_rate"));
					irswapTrade.setReceptionReferenceRateIndex(
							IndexSQL.getIndexById(results.getLong("reception_reference_rate_index_id")));
					irswapTrade.setReceptionReferenceRateIndexTenor(
							Tenor.valueOf(results.getString("reception_reference_rate_index_tenor")));
					irswapTrade.setInterestsToPayFixed(results.getLong("payment_reference_rate_index_id") == 0);
					if (!irswapTrade.isInterestsToPayFixed()) {
						irswapTrade.setPaymentReferenceRateIndexTenor(
								Tenor.valueOf(results.getString("payment_reference_rate_index_tenor")));
						irswapTrade.setPaymentReferenceRateIndex(
								IndexSQL.getIndexById(results.getLong("payment_reference_rate_index_id")));
						irswapTrade.setPaymentSpread(results.getBigDecimal("payment_spread"));
						irswapTrade.setPaymentInterestFixing(
								InterestPayment.valueOf(results.getString("payment_interest_fixing")));
					}
					irswapTrade.setPaymentDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong("payment_day_count_convention_id")));
					irswapTrade.setReceptionDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong("reception_day_count_convention_id")));
					irswapTrade.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					irswapTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					irswapTrade.setPaymentInterestPayment(
							InterestPayment.valueOf(results.getString("payment_interest_payment")));
					irswapTrade.setReceptionInterestPayment(
							InterestPayment.valueOf(results.getString("reception_interest_payment")));
					irswapTrade.setReceptionInterestFixing(
							InterestPayment.valueOf(results.getString("reception_interest_fixing")));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return irswapTrade;
	}

	public static long saveIRSwapTrade(SingleCurrencyIRSwapTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, SETTLEMENT_DATE, PRODUCT_ID, COUNTERPARTY_ID, CURRENCY_ID, AMOUNT, BOOK_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, SETTLEMENT_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, CURRENCY_ID=?, AMOUNT=?, BOOK_ID=? WHERE ID=?");
				PreparedStatement stmtSaveIRSwapTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO IRSWAP_TRADE(MATURITY_DATE, PAYMENT_FREQUENCY, RECEPTION_FREQUENCY, PAYMENT_FIXED_INTEREST_RATE, PAYMENT_REFERENCE_RATE_INDEX_ID, RECEPTION_REFERENCE_RATE_INDEX_ID, PAYMENT_REFERENCE_RATE_INDEX_TENOR, RECEPTION_REFERENCE_RATE_INDEX_TENOR, PAYMENT_SPREAD, RECEPTION_SPREAD, PAYMENT_DAY_COUNT_CONVENTION_ID, RECEPTION_DAY_COUNT_CONVENTION_ID, MATURITY_TENOR, PAYMENT_INTEREST_PAYMENT, PAYMENT_INTEREST_FIXING, RECEPTION_INTEREST_PAYMENT, RECEPTION_INTEREST_FIXING, IRSWAP_TRADE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE IRSWAP_TRADE SET MATURITY_DATE=?, PAYMENT_FREQUENCY=?, RECEPTION_FREQUENCY=?, PAYMENT_FIXED_INTEREST_RATE=?, PAYMENT_REFERENCE_RATE_INDEX_ID=?, RECEPTION_REFERENCE_RATE_INDEX_ID=?, PAYMENT_REFERENCE_RATE_INDEX_TENOR=?, RECEPTION_REFERENCE_RATE_INDEX_TENOR=?, PAYMENT_SPREAD=?, RECEPTION_SPREAD=?, PAYMENT_DAY_COUNT_CONVENTION_ID=?, RECEPTION_DAY_COUNT_CONVENTION_ID=?, MATURITY_TENOR=?, PAYMENT_INTEREST_PAYMENT=?, PAYMENT_INTEREST_FIXING=?, RECEPTION_INTEREST_PAYMENT=?, RECEPTION_INTEREST_FIXING=? WHERE IRSWAP_TRADE_ID=?")) {
			boolean isBuy = trade.isBuy();
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(9, java.sql.Date.valueOf(trade.getCreationDate()));
			} else {
				stmtSaveTrade.setLong(9, trade.getId());
			}
			stmtSaveTrade.setBoolean(1, isBuy);

			if (trade.getTradeDate() != null) {
				stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			} else {
				stmtSaveTrade.setNull(2, java.sql.Types.DATE);
			}
			if (trade.getSettlementDate() != null) {
				stmtSaveTrade.setDate(3, java.sql.Date.valueOf(trade.getSettlementDate()));
			} else {
				stmtSaveTrade.setNull(3, java.sql.Types.DATE);
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
						throw new SQLException("Creation of IR Swap Trade failed, no generated key obtained.");
					}
				}
			} else {
				tradeId = trade.getId();
			}
			// Maturity date can be null when the irswap is the underlying of a not
			// exercised option.
			LocalDate maturityDate = trade.getMaturityDate();
			if (maturityDate != null) {
				stmtSaveIRSwapTrade.setDate(1, java.sql.Date.valueOf(maturityDate));
			} else {
				stmtSaveIRSwapTrade.setNull(1, Types.DATE);
			}
			stmtSaveIRSwapTrade.setString(2, trade.getPaymentFrequency().name());
			stmtSaveIRSwapTrade.setString(3, trade.getReceptionFrequency().name());
			if (trade.isInterestsToPayFixed()) {
				stmtSaveIRSwapTrade.setBigDecimal(4, trade.getPaymentFixedInterestRate());
				stmtSaveIRSwapTrade.setNull(5, java.sql.Types.BIGINT);
			} else {
				stmtSaveIRSwapTrade.setNull(4, java.sql.Types.BIGINT);
				stmtSaveIRSwapTrade.setLong(5, trade.getPaymentReferenceRateIndex().getId());
			}

			stmtSaveIRSwapTrade.setLong(6, trade.getReceptionReferenceRateIndex().getId());
			Tenor paymentReferenceRateIndexTenor = trade.getPaymentReferenceRateIndexTenor();
			if (paymentReferenceRateIndexTenor != null) {
				stmtSaveIRSwapTrade.setString(7, paymentReferenceRateIndexTenor.name());
			} else {
				stmtSaveIRSwapTrade.setNull(7, java.sql.Types.VARCHAR);
			}
			stmtSaveIRSwapTrade.setString(8, trade.getReceptionReferenceRateIndexTenor().name());
			stmtSaveIRSwapTrade.setBigDecimal(9, trade.getPaymentSpread());
			stmtSaveIRSwapTrade.setBigDecimal(10, trade.getReceptionSpread());
			stmtSaveIRSwapTrade.setLong(11, trade.getPaymentDayCountConvention().getId());
			stmtSaveIRSwapTrade.setLong(12, trade.getReceptionDayCountConvention().getId());
			if (trade.getMaturityTenor() != null) {
				stmtSaveIRSwapTrade.setString(13, trade.getMaturityTenor().name());
			} else {
				stmtSaveIRSwapTrade.setNull(13, Types.VARCHAR);
			}
			stmtSaveIRSwapTrade.setString(14, trade.getPaymentInterestPayment().name());
			if (trade.getPaymentInterestFixing() != null) {
				stmtSaveIRSwapTrade.setString(15, trade.getPaymentInterestFixing().name());
			} else {
				stmtSaveIRSwapTrade.setNull(15, Types.VARCHAR);
			}
			stmtSaveIRSwapTrade.setString(16, trade.getReceptionInterestPayment().name());
			stmtSaveIRSwapTrade.setString(17, trade.getReceptionInterestFixing().name());
			stmtSaveIRSwapTrade.setLong(18, tradeId);
			stmtSaveIRSwapTrade.executeUpdate();

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}

	public static IRSwapTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		IRSwapTrade irswapTrade = null;
		try {

			// We ensure that the deal is an IRSwap.
			if (rs.getLong("irswap_trade_id") == 0) {
				return null;
			}

			irswapTrade = new SingleCurrencyIRSwapTrade();
			java.sql.Date maturityDate = rs.getDate("irswap_maturity_date");
			if (maturityDate != null) {
				irswapTrade.setMaturityDate(maturityDate.toLocalDate());
			}
			String maturityTenorString = rs.getString("irswap_maturity_tenor");
			if (maturityTenorString != null) {
				irswapTrade.setMaturityTenor(Tenor.valueOf(maturityTenorString));
			}
			irswapTrade.setPaymentFrequency(Tenor.valueOf(rs.getString("irswap_payment_frequency")));
			irswapTrade.setReceptionFrequency(Tenor.valueOf(rs.getString("irswap_reception_frequency")));
			irswapTrade.setReceptionReferenceRateIndexTenor(
					Tenor.valueOf(rs.getString("reception_reference_rate_index_tenor")));

			irswapTrade.setReceptionSpread(rs.getBigDecimal("reception_spread"));
			irswapTrade.setPaymentFixedInterestRate(rs.getBigDecimal("payment_fixed_interest_rate"));
			irswapTrade.setReceptionReferenceRateIndex(
					IndexSQL.getIndexById(rs.getLong("reception_reference_rate_index_id")));
			irswapTrade.setInterestsToPayFixed(rs.getLong("payment_reference_rate_index_id") == 0);
			if (!irswapTrade.isInterestsToPayFixed()) {
				irswapTrade.setPaymentReferenceRateIndexTenor(
						Tenor.valueOf(rs.getString("payment_reference_rate_index_tenor")));
				irswapTrade.setPaymentReferenceRateIndex(
						IndexSQL.getIndexById(rs.getLong("payment_reference_rate_index_id")));
				irswapTrade.setPaymentInterestFixing(InterestPayment.valueOf(rs.getString("payment_interest_fixing")));
				irswapTrade.setPaymentSpread(rs.getBigDecimal("payment_spread"));
			}
			irswapTrade.setPaymentDayCountConvention(
					DayCountConventionSQL.getDayCountConventionById(rs.getLong("payment_day_count_convention_id")));
			irswapTrade.setReceptionDayCountConvention(
					DayCountConventionSQL.getDayCountConventionById(rs.getLong("reception_day_count_convention_id")));
			irswapTrade.setPaymentInterestPayment(InterestPayment.valueOf(rs.getString("payment_interest_payment")));
			irswapTrade
					.setReceptionInterestPayment(InterestPayment.valueOf(rs.getString("reception_interest_payment")));
			irswapTrade.setReceptionInterestFixing(InterestPayment.valueOf(rs.getString("reception_interest_fixing")));

			// Commmon fields
			TradeSQL.setTradeCommonFields(irswapTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return irswapTrade;
	}

}