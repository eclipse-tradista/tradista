package org.eclipse.tradista.mm.loandeposit.persistence;

import java.math.BigDecimal;
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
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.mm.loandeposit.model.DepositTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade.InterestType;

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

public class LoanDepositTradeSQL {

	public static LoanDepositTrade getTradeById(long id) {
		LoanDepositTrade mmTrade = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con
						.prepareStatement("SELECT *  FROM LOAN_DEPOSIT_TRADE, TRADE WHERE "
								+ "TRADE.ID = LOAN_DEPOSIT_TRADE.LOAN_DEPOSIT_TRADE_ID AND LOAN_DEPOSIT_TRADE.LOAN_DEPOSIT_TRADE_ID = ? ")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (mmTrade == null) {
						if (results.getString("direction").equals(LoanDepositTrade.Direction.LOAN.name())) {
							mmTrade = new LoanTrade();
						} else {
							mmTrade = new DepositTrade();
						}
					}
					mmTrade.setAmount(results.getBigDecimal("amount"));
					mmTrade.setBuySell(results.getBoolean("buy_sell"));
					mmTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					mmTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					mmTrade.setDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong("day_count_convention_id")));
					mmTrade.setEndDate(results.getDate("end_date").toLocalDate());
					String maturity = results.getString("maturity");
					if (maturity != null) {
						mmTrade.setMaturity(Tenor.valueOf(maturity));
					}
					mmTrade.setInterestType(InterestType.valueOf(results.getString("interest_type")));
					String compoundPeriod = results.getString("compound_period");
					if (compoundPeriod != null) {
						mmTrade.setMaturity(Tenor.valueOf(compoundPeriod));
					}
					BigDecimal fixedRate = results.getBigDecimal("fixed_rate");
					if (fixedRate != null) {
						mmTrade.setFixedRate(fixedRate);
					} else {
						mmTrade.setFloatingRateIndex(IndexSQL.getIndexById(results.getLong("floating_rate_index_id")));
						mmTrade.setFloatingRateIndexTenor(
								Tenor.valueOf(results.getString("floating_rate_index_tenor")));
						mmTrade.setFixingPeriod(Tenor.valueOf(results.getString("fixing_period")));
						mmTrade.setSpread(results.getBigDecimal("spread"));
						mmTrade.setInterestFixing(InterestPayment.valueOf(results.getString("interest_fixing")));
					}
					mmTrade.setPaymentFrequency(Tenor.valueOf(results.getString("payment_frequency")));
					mmTrade.setInterestPayment(InterestPayment.valueOf(results.getString("interest_payment")));
					mmTrade.setSettlementDate(results.getDate("settlement_date").toLocalDate());
					mmTrade.setTradeDate(results.getDate("trade_date").toLocalDate());
					mmTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					mmTrade.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					mmTrade.setId(results.getLong("id"));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return mmTrade;
	}

	public static long saveLoanDepositTrade(LoanDepositTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, PRODUCT_ID, COUNTERPARTY_ID, AMOUNT, CURRENCY_ID, SETTLEMENT_DATE, BOOK_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, AMOUNT=?, CURRENCY_ID=?, SETTLEMENT_DATE=?, BOOK_ID=? WHERE ID=?");
				PreparedStatement stmtSaveLoanDepositTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO LOAN_DEPOSIT_TRADE(FIXED_RATE, FLOATING_RATE_INDEX_ID, FLOATING_RATE_INDEX_TENOR, DAY_COUNT_CONVENTION_ID, PAYMENT_FREQUENCY, END_DATE, FIXING_PERIOD, SPREAD, DIRECTION, MATURITY, INTEREST_TYPE, COMPOUND_PERIOD, INTEREST_PAYMENT, INTEREST_FIXING, LOAN_DEPOSIT_TRADE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE LOAN_DEPOSIT_TRADE SET FIXED_RATE=?, FLOATING_RATE_INDEX_ID=?, FLOATING_RATE_INDEX_TENOR=?, DAY_COUNT_CONVENTION_ID=?, PAYMENT_FREQUENCY=?, END_DATE=?, FIXING_PERIOD=?, SPREAD=?, DIRECTION=?, MATURITY=?, INTEREST_TYPE=?, COMPOUND_PERIOD=?, INTEREST_PAYMENT=?, INTEREST_FIXING=? WHERE LOAN_DEPOSIT_TRADE_ID=?")) {
			boolean isBuy = trade.isBuy();
			String direction;
			if (trade instanceof LoanTrade) {
				direction = LoanDepositTrade.Direction.LOAN.name();
			} else {
				direction = LoanDepositTrade.Direction.DEPOSIT.name();
			}
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(9, java.sql.Date.valueOf(LocalDate.now()));
			} else {
				stmtSaveTrade.setLong(9, trade.getId());
			}
			stmtSaveTrade.setBoolean(1, isBuy);
			stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			stmtSaveTrade.setNull(3, java.sql.Types.BIGINT);
			stmtSaveTrade.setLong(4, trade.getCounterparty().getId());
			stmtSaveTrade.setBigDecimal(5, trade.getAmount());
			stmtSaveTrade.setLong(6, trade.getCurrency().getId());
			stmtSaveTrade.setDate(7, java.sql.Date.valueOf(trade.getSettlementDate()));
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
			if (trade.getFloatingRateIndex() == null) {
				stmtSaveLoanDepositTrade.setBigDecimal(1, trade.getFixedRate());
				stmtSaveLoanDepositTrade.setNull(2, java.sql.Types.BIGINT);
				stmtSaveLoanDepositTrade.setNull(3, java.sql.Types.VARCHAR);
				stmtSaveLoanDepositTrade.setNull(7, java.sql.Types.VARCHAR);
				stmtSaveLoanDepositTrade.setNull(8, java.sql.Types.DECIMAL);
				stmtSaveLoanDepositTrade.setNull(12, java.sql.Types.VARCHAR);
				stmtSaveLoanDepositTrade.setNull(14, java.sql.Types.VARCHAR);
			} else {
				stmtSaveLoanDepositTrade.setNull(1, java.sql.Types.DECIMAL);
				stmtSaveLoanDepositTrade.setLong(2, trade.getFloatingRateIndex().getId());
				stmtSaveLoanDepositTrade.setString(3, trade.getFloatingRateIndexTenor().name());
				stmtSaveLoanDepositTrade.setString(7, trade.getFixingPeriod().name());
				if (trade.getSpread() == null) {
					stmtSaveLoanDepositTrade.setNull(8, java.sql.Types.DECIMAL);
				} else {
					stmtSaveLoanDepositTrade.setBigDecimal(8, trade.getSpread());
				}
				if (trade.getCompoundPeriod() == null) {
					stmtSaveLoanDepositTrade.setNull(12, java.sql.Types.VARCHAR);
				} else {
					stmtSaveLoanDepositTrade.setString(12, trade.getCompoundPeriod().name());
				}
				stmtSaveLoanDepositTrade.setString(14, trade.getInterestFixing().name());
			}
			stmtSaveLoanDepositTrade.setLong(4, trade.getDayCountConvention().getId());
			stmtSaveLoanDepositTrade.setString(5, trade.getPaymentFrequency().name());
			stmtSaveLoanDepositTrade.setDate(6, java.sql.Date.valueOf(trade.getEndDate()));
			stmtSaveLoanDepositTrade.setString(9, direction);
			if (trade.getMaturity() == null) {
				stmtSaveLoanDepositTrade.setNull(10, java.sql.Types.VARCHAR);
			} else {
				stmtSaveLoanDepositTrade.setString(10, trade.getMaturity().name());
			}
			stmtSaveLoanDepositTrade.setString(11, trade.getInterestType().name());
			stmtSaveLoanDepositTrade.setString(13, trade.getInterestPayment().name());
			stmtSaveLoanDepositTrade.setLong(15, tradeId);
			stmtSaveLoanDepositTrade.executeUpdate();
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}

	public static LoanDepositTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		LoanDepositTrade mmTrade = null;
		try {
			if (rs.getLong("loan_deposit_trade_id") == 0) {
				return null;
			}

			if (rs.getString("direction").equals(LoanDepositTrade.Direction.LOAN.name())) {
				mmTrade = new LoanTrade();
			} else {
				mmTrade = new DepositTrade();
			}

			mmTrade.setDayCountConvention(DayCountConventionSQL
					.getDayCountConventionById(rs.getLong("loan_deposit_day_count_convention_id")));
			mmTrade.setEndDate(rs.getDate("end_date").toLocalDate());
			String maturity = rs.getString("maturity");
			if (maturity != null) {
				mmTrade.setMaturity(Tenor.valueOf(maturity));
			}
			mmTrade.setInterestType(InterestType.valueOf(rs.getString("interest_type")));
			String compoundPeriod = rs.getString("compound_period");
			if (compoundPeriod != null) {
				mmTrade.setMaturity(Tenor.valueOf(compoundPeriod));
			}
			BigDecimal fixedRate = rs.getBigDecimal("loan_deposit_fixed_rate");
			if (fixedRate != null) {
				mmTrade.setFixedRate(fixedRate);
			} else {
				mmTrade.setFloatingRateIndex(IndexSQL.getIndexById(rs.getLong("floating_rate_index_id")));
				mmTrade.setFloatingRateIndexTenor(Tenor.valueOf(rs.getString("floating_rate_index_tenor")));
				mmTrade.setFixingPeriod(Tenor.valueOf(rs.getString("fixing_period")));
				mmTrade.setSpread(rs.getBigDecimal("spread"));
				mmTrade.setInterestFixing(InterestPayment.valueOf(rs.getString("loan_deposit_interest_fixing")));
			}
			mmTrade.setPaymentFrequency(Tenor.valueOf(rs.getString("payment_frequency")));
			mmTrade.setInterestPayment(InterestPayment.valueOf(rs.getString("loan_deposit_interest_payment")));

			// Commmon fields
			TradeSQL.setTradeCommonFields(mmTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return mmTrade;
	}

}