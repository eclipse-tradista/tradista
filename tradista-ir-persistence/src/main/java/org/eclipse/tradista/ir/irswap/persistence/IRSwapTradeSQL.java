package org.eclipse.tradista.ir.irswap.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MATURITY_DATE;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_DATE_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_TABLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.daycountconvention.persistence.DayCountConventionSQL;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
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

	public static final Field IRSWAP_TRADE_ID_FIELD = new Field("IRSWAP_TRADE_ID");
	public static final Field MATURITY_DATE_FIELD = new Field(MATURITY_DATE);
	public static final Field MATURITY_TENOR_FIELD = new Field("MATURITY_TENOR");
	public static final Field PAYMENT_FREQUENCY_FIELD = new Field("PAYMENT_FREQUENCY");
	public static final Field RECEPTION_FREQUENCY_FIELD = new Field("RECEPTION_FREQUENCY");
	public static final Field PAYMENT_FIXED_INTEREST_RATE_FIELD = new Field("PAYMENT_FIXED_INTEREST_RATE");
	public static final Field PAYMENT_REFERENCE_RATE_INDEX_ID_FIELD = new Field("PAYMENT_REFERENCE_RATE_INDEX_ID");
	public static final Field RECEPTION_REFERENCE_RATE_INDEX_ID_FIELD = new Field("RECEPTION_REFERENCE_RATE_INDEX_ID");
	public static final Field PAYMENT_REFERENCE_RATE_INDEX_TENOR_FIELD = new Field("PAYMENT_REFERENCE_RATE_INDEX_TENOR");
	public static final Field RECEPTION_REFERENCE_RATE_INDEX_TENOR_FIELD = new Field("RECEPTION_REFERENCE_RATE_INDEX_TENOR");
	public static final Field PAYMENT_SPREAD_FIELD = new Field("PAYMENT_SPREAD");
	public static final Field RECEPTION_SPREAD_FIELD = new Field("RECEPTION_SPREAD");
	public static final Field PAYMENT_DAY_COUNT_CONVENTION_ID_FIELD = new Field("PAYMENT_DAY_COUNT_CONVENTION_ID");
	public static final Field RECEPTION_DAY_COUNT_CONVENTION_ID_FIELD = new Field("RECEPTION_DAY_COUNT_CONVENTION_ID");
	public static final Field PAYMENT_INTEREST_PAYMENT_FIELD = new Field("PAYMENT_INTEREST_PAYMENT");
	public static final Field RECEPTION_INTEREST_PAYMENT_FIELD = new Field("RECEPTION_INTEREST_PAYMENT");
	public static final Field PAYMENT_INTEREST_FIXING_FIELD = new Field("PAYMENT_INTEREST_FIXING");
	public static final Field RECEPTION_INTEREST_FIXING_FIELD = new Field("RECEPTION_INTEREST_FIXING");

	private static final Field[] IRSWAP_TRADE_FIELDS = { IRSWAP_TRADE_ID_FIELD, MATURITY_DATE_FIELD,
			MATURITY_TENOR_FIELD, PAYMENT_FREQUENCY_FIELD, RECEPTION_FREQUENCY_FIELD, PAYMENT_FIXED_INTEREST_RATE_FIELD,
			PAYMENT_REFERENCE_RATE_INDEX_ID_FIELD, RECEPTION_REFERENCE_RATE_INDEX_ID_FIELD,
			PAYMENT_REFERENCE_RATE_INDEX_TENOR_FIELD, RECEPTION_REFERENCE_RATE_INDEX_TENOR_FIELD, PAYMENT_SPREAD_FIELD,
			RECEPTION_SPREAD_FIELD, PAYMENT_DAY_COUNT_CONVENTION_ID_FIELD, RECEPTION_DAY_COUNT_CONVENTION_ID_FIELD,
			PAYMENT_INTEREST_PAYMENT_FIELD, RECEPTION_INTEREST_PAYMENT_FIELD, PAYMENT_INTEREST_FIXING_FIELD,
			RECEPTION_INTEREST_FIXING_FIELD };

	private static final Field[] IRSWAP_TRADE_FIELDS_FOR_INSERT = { MATURITY_DATE_FIELD, PAYMENT_FREQUENCY_FIELD,
			RECEPTION_FREQUENCY_FIELD, PAYMENT_FIXED_INTEREST_RATE_FIELD, PAYMENT_REFERENCE_RATE_INDEX_ID_FIELD,
			RECEPTION_REFERENCE_RATE_INDEX_ID_FIELD, PAYMENT_REFERENCE_RATE_INDEX_TENOR_FIELD,
			RECEPTION_REFERENCE_RATE_INDEX_TENOR_FIELD, PAYMENT_SPREAD_FIELD, RECEPTION_SPREAD_FIELD,
			PAYMENT_DAY_COUNT_CONVENTION_ID_FIELD, RECEPTION_DAY_COUNT_CONVENTION_ID_FIELD, MATURITY_TENOR_FIELD,
			PAYMENT_INTEREST_PAYMENT_FIELD, PAYMENT_INTEREST_FIXING_FIELD, RECEPTION_INTEREST_PAYMENT_FIELD,
			RECEPTION_INTEREST_FIXING_FIELD, IRSWAP_TRADE_ID_FIELD };

	private static final Field[] IRSWAP_TRADE_FIELDS_FOR_UPDATE = { MATURITY_DATE_FIELD, PAYMENT_FREQUENCY_FIELD,
			RECEPTION_FREQUENCY_FIELD, PAYMENT_FIXED_INTEREST_RATE_FIELD, PAYMENT_REFERENCE_RATE_INDEX_ID_FIELD,
			RECEPTION_REFERENCE_RATE_INDEX_ID_FIELD, PAYMENT_REFERENCE_RATE_INDEX_TENOR_FIELD,
			RECEPTION_REFERENCE_RATE_INDEX_TENOR_FIELD, PAYMENT_SPREAD_FIELD, RECEPTION_SPREAD_FIELD,
			PAYMENT_DAY_COUNT_CONVENTION_ID_FIELD, RECEPTION_DAY_COUNT_CONVENTION_ID_FIELD, MATURITY_TENOR_FIELD,
			PAYMENT_INTEREST_PAYMENT_FIELD, PAYMENT_INTEREST_FIXING_FIELD, RECEPTION_INTEREST_PAYMENT_FIELD,
			RECEPTION_INTEREST_FIXING_FIELD };

	public static final Table IRSWAP_TRADE_TABLE = new Table("IRSWAP_TRADE", IRSWAP_TRADE_FIELDS);

	public static final Join TRADE_AND_IRSWAP_TRADE_INNER_JOIN = Join.innerEq(TRADE_TABLE, ID_FIELD,
			IRSWAP_TRADE_ID_FIELD);

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(IRSWAP_TRADE_TABLE,
			TRADE_AND_IRSWAP_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, IRSWAP_TRADE_TABLE, IRSWAP_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, IRSWAP_TRADE_ID_FIELD, IRSWAP_TRADE_TABLE,
				IRSWAP_TRADE_FIELDS_FOR_UPDATE);
	}

	public static SingleCurrencyIRSwapTrade getTradeById(long id, boolean includeUnderlying) {
		SingleCurrencyIRSwapTrade irswapTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, IRSWAP_TRADE_ID_FIELD);
		TradistaDBUtil.addQueryFilter(query, IRSWAP_TRADE_ID_FIELD, "SELECT CCYSWAP_TRADE_ID FROM CCYSWAP_TRADE", true);
		if (!includeUnderlying) {
			TradistaDBUtil.addIsNotNullFilter(query, TRADE_DATE_FIELD);
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {

				while (results.next()) {
					if (irswapTrade == null) {
						irswapTrade = new SingleCurrencyIRSwapTrade();
					}

					TradeSQL.setTradeCommonFields(irswapTrade, results);
					java.sql.Date maturityDate = results.getDate(MATURITY_DATE_FIELD.getName());
					if (maturityDate != null) {
						irswapTrade.setMaturityDate(maturityDate.toLocalDate());
					}
					String maturityTenorString = results.getString(MATURITY_TENOR_FIELD.getName());
					if (maturityTenorString != null) {
						irswapTrade.setMaturityTenor(Tenor.valueOf(maturityTenorString));
					}
					irswapTrade.setPaymentFrequency(Tenor.valueOf(results.getString(PAYMENT_FREQUENCY_FIELD.getName())));
					irswapTrade.setReceptionFrequency(Tenor.valueOf(results.getString(RECEPTION_FREQUENCY_FIELD.getName())));
					irswapTrade.setReceptionSpread(results.getBigDecimal(RECEPTION_SPREAD_FIELD.getName()));
					irswapTrade.setPaymentFixedInterestRate(results.getBigDecimal(PAYMENT_FIXED_INTEREST_RATE_FIELD.getName()));
					irswapTrade.setReceptionReferenceRateIndex(
							IndexSQL.getIndexById(results.getLong(RECEPTION_REFERENCE_RATE_INDEX_ID_FIELD.getName())));
					irswapTrade.setReceptionReferenceRateIndexTenor(
							Tenor.valueOf(results.getString(RECEPTION_REFERENCE_RATE_INDEX_TENOR_FIELD.getName())));
					irswapTrade.setInterestsToPayFixed(results.getLong(PAYMENT_REFERENCE_RATE_INDEX_ID_FIELD.getName()) == 0);
					if (!irswapTrade.isInterestsToPayFixed()) {
						irswapTrade.setPaymentReferenceRateIndexTenor(
								Tenor.valueOf(results.getString(PAYMENT_REFERENCE_RATE_INDEX_TENOR_FIELD.getName())));
						irswapTrade.setPaymentReferenceRateIndex(
								IndexSQL.getIndexById(results.getLong(PAYMENT_REFERENCE_RATE_INDEX_ID_FIELD.getName())));
						irswapTrade.setPaymentSpread(results.getBigDecimal(PAYMENT_SPREAD_FIELD.getName()));
						irswapTrade.setPaymentInterestFixing(
								InterestPayment.valueOf(results.getString(PAYMENT_INTEREST_FIXING_FIELD.getName())));
					}
					irswapTrade.setPaymentDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong(PAYMENT_DAY_COUNT_CONVENTION_ID_FIELD.getName())));
					irswapTrade.setReceptionDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong(RECEPTION_DAY_COUNT_CONVENTION_ID_FIELD.getName())));
					irswapTrade.setPaymentInterestPayment(
							InterestPayment.valueOf(results.getString(PAYMENT_INTEREST_PAYMENT_FIELD.getName())));
					irswapTrade.setReceptionInterestPayment(
							InterestPayment.valueOf(results.getString(RECEPTION_INTEREST_PAYMENT_FIELD.getName())));
					irswapTrade.setReceptionInterestFixing(
							InterestPayment.valueOf(results.getString(RECEPTION_INTEREST_FIXING_FIELD.getName())));
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return irswapTrade;
	}

	public static void setPreparedStatementFields(IRSwapTrade trade, PreparedStatement stmtSaveIRSwapTrade,
			long tradeId) throws SQLException {
		// Maturity date can be null when the irswap is the underlying of a not exercised option.
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
		if (trade.getId() == 0) {
			stmtSaveIRSwapTrade.setLong(18, tradeId);
		} else {
			stmtSaveIRSwapTrade.setLong(18, trade.getId());
		}
	}

	public static long saveIRSwapTrade(SingleCurrencyIRSwapTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveIRSwapTrade = (trade.getId() == 0) ? getInsertStatement(con)
						: getUpdateStatement(con)) {
			TradeSQL.setPreparedStatementCommonFields(trade, stmtSaveTrade);
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

			setPreparedStatementFields(trade, stmtSaveIRSwapTrade, tradeId);
			stmtSaveIRSwapTrade.executeUpdate();

		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
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