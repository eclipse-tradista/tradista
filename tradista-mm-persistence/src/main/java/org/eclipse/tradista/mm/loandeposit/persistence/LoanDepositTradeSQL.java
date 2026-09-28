package org.eclipse.tradista.mm.loandeposit.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.END_DATE;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_TABLE;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.daycountconvention.persistence.DayCountConventionSQL;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.mm.loandeposit.model.DepositTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade.InterestType;
import org.eclipse.tradista.mm.loandeposit.model.LoanTrade;

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

	public static final Field LOAN_DEPOSIT_TRADE_ID_FIELD = new Field("LOAN_DEPOSIT_TRADE_ID");
	public static final Field FIXED_RATE_FIELD = new Field("FIXED_RATE");
	public static final Field FLOATING_RATE_INDEX_ID_FIELD = new Field("FLOATING_RATE_INDEX_ID");
	public static final Field FLOATING_RATE_INDEX_TENOR_FIELD = new Field("FLOATING_RATE_INDEX_TENOR");
	public static final Field DAY_COUNT_CONVENTION_ID_FIELD = new Field("DAY_COUNT_CONVENTION_ID");
	public static final Field PAYMENT_FREQUENCY_FIELD = new Field("PAYMENT_FREQUENCY");
	public static final Field END_DATE_FIELD = new Field(END_DATE);
	public static final Field FIXING_PERIOD_FIELD = new Field("FIXING_PERIOD");
	public static final Field SPREAD_FIELD = new Field("SPREAD");
	public static final Field DIRECTION_FIELD = new Field("DIRECTION");
	public static final Field MATURITY_FIELD = new Field("MATURITY");
	public static final Field INTEREST_TYPE_FIELD = new Field("INTEREST_TYPE");
	public static final Field COMPOUND_PERIOD_FIELD = new Field("COMPOUND_PERIOD");
	public static final Field INTEREST_PAYMENT_FIELD = new Field("INTEREST_PAYMENT");
	public static final Field INTEREST_FIXING_FIELD = new Field("INTEREST_FIXING");

	private static final Field[] LOAN_DEPOSIT_TRADE_FIELDS = { LOAN_DEPOSIT_TRADE_ID_FIELD, FIXED_RATE_FIELD,
			FLOATING_RATE_INDEX_ID_FIELD, FLOATING_RATE_INDEX_TENOR_FIELD, DAY_COUNT_CONVENTION_ID_FIELD,
			PAYMENT_FREQUENCY_FIELD, END_DATE_FIELD, FIXING_PERIOD_FIELD, SPREAD_FIELD, DIRECTION_FIELD, MATURITY_FIELD,
			INTEREST_TYPE_FIELD, COMPOUND_PERIOD_FIELD, INTEREST_PAYMENT_FIELD, INTEREST_FIXING_FIELD };

	private static final Field[] LOAN_DEPOSIT_TRADE_FIELDS_FOR_INSERT = { FIXED_RATE_FIELD,
			FLOATING_RATE_INDEX_ID_FIELD, FLOATING_RATE_INDEX_TENOR_FIELD, DAY_COUNT_CONVENTION_ID_FIELD,
			PAYMENT_FREQUENCY_FIELD, END_DATE_FIELD, FIXING_PERIOD_FIELD, SPREAD_FIELD, DIRECTION_FIELD, MATURITY_FIELD,
			INTEREST_TYPE_FIELD, COMPOUND_PERIOD_FIELD, INTEREST_PAYMENT_FIELD, INTEREST_FIXING_FIELD,
			LOAN_DEPOSIT_TRADE_ID_FIELD };

	private static final Field[] LOAN_DEPOSIT_TRADE_FIELDS_FOR_UPDATE = { FIXED_RATE_FIELD,
			FLOATING_RATE_INDEX_ID_FIELD, FLOATING_RATE_INDEX_TENOR_FIELD, DAY_COUNT_CONVENTION_ID_FIELD,
			PAYMENT_FREQUENCY_FIELD, END_DATE_FIELD, FIXING_PERIOD_FIELD, SPREAD_FIELD, DIRECTION_FIELD, MATURITY_FIELD,
			INTEREST_TYPE_FIELD, COMPOUND_PERIOD_FIELD, INTEREST_PAYMENT_FIELD, INTEREST_FIXING_FIELD };

	public static final Table LOAN_DEPOSIT_TRADE_TABLE = new Table("LOAN_DEPOSIT_TRADE", LOAN_DEPOSIT_TRADE_FIELDS);

	public static final Join TRADE_AND_LOAN_DEPOSIT_TRADE_INNER_JOIN = Join.innerEq(TRADE_TABLE, ID_FIELD,
			LOAN_DEPOSIT_TRADE_ID_FIELD);

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(LOAN_DEPOSIT_TRADE_TABLE,
			TRADE_AND_LOAN_DEPOSIT_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, LOAN_DEPOSIT_TRADE_TABLE,
				LOAN_DEPOSIT_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, LOAN_DEPOSIT_TRADE_ID_FIELD, LOAN_DEPOSIT_TRADE_TABLE,
				LOAN_DEPOSIT_TRADE_FIELDS_FOR_UPDATE);
	}

	public static LoanDepositTrade getTradeById(long id) {
		LoanDepositTrade mmTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, LOAN_DEPOSIT_TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (mmTrade == null) {
						if (results.getString(DIRECTION_FIELD.getName()).equals(LoanDepositTrade.Direction.LOAN.name())) {
							mmTrade = new LoanTrade();
						} else {
							mmTrade = new DepositTrade();
						}
					}
					TradeSQL.setTradeCommonFields(mmTrade, results);
					mmTrade.setDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong(DAY_COUNT_CONVENTION_ID_FIELD.getName())));
					mmTrade.setEndDate(results.getDate(END_DATE_FIELD.getName()).toLocalDate());
					String maturity = results.getString(MATURITY_FIELD.getName());
					if (maturity != null) {
						mmTrade.setMaturity(Tenor.valueOf(maturity));
					}
					mmTrade.setInterestType(InterestType.valueOf(results.getString(INTEREST_TYPE_FIELD.getName())));
					String compoundPeriod = results.getString(COMPOUND_PERIOD_FIELD.getName());
					if (compoundPeriod != null) {
						mmTrade.setMaturity(Tenor.valueOf(compoundPeriod));
					}
					BigDecimal fixedRate = results.getBigDecimal(FIXED_RATE_FIELD.getName());
					if (fixedRate != null) {
						mmTrade.setFixedRate(fixedRate);
					} else {
						mmTrade.setFloatingRateIndex(IndexSQL.getIndexById(results.getLong(FLOATING_RATE_INDEX_ID_FIELD.getName())));
						mmTrade.setFloatingRateIndexTenor(
								Tenor.valueOf(results.getString(FLOATING_RATE_INDEX_TENOR_FIELD.getName())));
						mmTrade.setFixingPeriod(Tenor.valueOf(results.getString(FIXING_PERIOD_FIELD.getName())));
						mmTrade.setSpread(results.getBigDecimal(SPREAD_FIELD.getName()));
						mmTrade.setInterestFixing(InterestPayment.valueOf(results.getString(INTEREST_FIXING_FIELD.getName())));
					}
					mmTrade.setPaymentFrequency(Tenor.valueOf(results.getString(PAYMENT_FREQUENCY_FIELD.getName())));
					mmTrade.setInterestPayment(InterestPayment.valueOf(results.getString(INTEREST_PAYMENT_FIELD.getName())));
				}
			}
		} catch (SQLException | TradistaBusinessException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return mmTrade;
	}

	public static long saveLoanDepositTrade(LoanDepositTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveLoanDepositTrade = (trade.getId() == 0) ? getInsertStatement(con)
						: getUpdateStatement(con)) {
			String direction;
			if (trade instanceof LoanTrade) {
				direction = LoanDepositTrade.Direction.LOAN.name();
			} else {
				direction = LoanDepositTrade.Direction.DEPOSIT.name();
			}
			TradeSQL.setPreparedStatementCommonFields(trade, stmtSaveTrade);
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
		} catch (SQLException | TradistaBusinessException sqle) {
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