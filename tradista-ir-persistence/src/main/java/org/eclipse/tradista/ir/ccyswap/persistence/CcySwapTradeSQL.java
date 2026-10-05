package org.eclipse.tradista.ir.ccyswap.persistence;

import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.IRSWAP_TRADE_ID_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.IRSWAP_TRADE_TABLE;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.MATURITY_DATE_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.MATURITY_TENOR_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.PAYMENT_DAY_COUNT_CONVENTION_ID_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.PAYMENT_FIXED_INTEREST_RATE_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.PAYMENT_FREQUENCY_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.PAYMENT_INTEREST_FIXING_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.PAYMENT_INTEREST_PAYMENT_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.PAYMENT_REFERENCE_RATE_INDEX_ID_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.PAYMENT_REFERENCE_RATE_INDEX_TENOR_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.PAYMENT_SPREAD_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.RECEPTION_DAY_COUNT_CONVENTION_ID_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.RECEPTION_FREQUENCY_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.RECEPTION_INTEREST_FIXING_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.RECEPTION_INTEREST_PAYMENT_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.RECEPTION_REFERENCE_RATE_INDEX_ID_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.RECEPTION_REFERENCE_RATE_INDEX_TENOR_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.RECEPTION_SPREAD_FIELD;
import static org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL.TRADE_AND_IRSWAP_TRADE_INNER_JOIN;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;
import org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL;

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

	public static final Field CCYSWAP_TRADE_ID_FIELD = new Field("CCYSWAP_TRADE_ID");
	public static final Field CURRENCY_TWO_ID_FIELD = new Field("CURRENCY_TWO_ID");
	public static final Field NOTIONAL_AMOUNT_TWO_FIELD = new Field("NOTIONAL_AMOUNT_TWO");

	private static final Field[] CCYSWAP_TRADE_FIELDS = { CCYSWAP_TRADE_ID_FIELD, CURRENCY_TWO_ID_FIELD,
			NOTIONAL_AMOUNT_TWO_FIELD };

	private static final Field[] CCYSWAP_TRADE_FIELDS_FOR_INSERT = { CURRENCY_TWO_ID_FIELD, NOTIONAL_AMOUNT_TWO_FIELD,
			CCYSWAP_TRADE_ID_FIELD };

	private static final Field[] CCYSWAP_TRADE_FIELDS_FOR_UPDATE = { CURRENCY_TWO_ID_FIELD, NOTIONAL_AMOUNT_TWO_FIELD };

	public static final Table CCYSWAP_TRADE_TABLE = new Table("CCYSWAP_TRADE", CCYSWAP_TRADE_FIELDS);

	public static final Join IRSWAP_TRADE_AND_CCYSWAP_TRADE_INNER_JOIN = Join.innerEq(IRSWAP_TRADE_TABLE,
			IRSWAP_TRADE_ID_FIELD, CCYSWAP_TRADE_ID_FIELD);

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(CCYSWAP_TRADE_TABLE,
			IRSWAP_TRADE_AND_CCYSWAP_TRADE_INNER_JOIN, TRADE_AND_IRSWAP_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, CCYSWAP_TRADE_TABLE, CCYSWAP_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, CCYSWAP_TRADE_ID_FIELD, CCYSWAP_TRADE_TABLE,
				CCYSWAP_TRADE_FIELDS_FOR_UPDATE);
	}

	public static CcySwapTrade getTradeById(long id) {

		CcySwapTrade ccyswapTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, CCYSWAP_TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (ccyswapTrade == null) {
						ccyswapTrade = CcySwapTrade.of(TradeSQL.getCreationTime(results));
					}

					TradeSQL.setTradeCommonFields(ccyswapTrade, results);
					java.sql.Date maturityDate = results.getDate(MATURITY_DATE_FIELD.getName());
					if (maturityDate != null) {
						ccyswapTrade.setMaturityDate(maturityDate.toLocalDate());
					}
					String maturityTenorString = results.getString(MATURITY_TENOR_FIELD.getName());
					if (maturityTenorString != null) {
						ccyswapTrade.setMaturityTenor(Tenor.valueOf(maturityTenorString));
					}
					ccyswapTrade
							.setPaymentFrequency(Tenor.valueOf(results.getString(PAYMENT_FREQUENCY_FIELD.getName())));
					ccyswapTrade.setReceptionFrequency(
							Tenor.valueOf(results.getString(RECEPTION_FREQUENCY_FIELD.getName())));
					ccyswapTrade.setReceptionSpread(results.getBigDecimal(RECEPTION_SPREAD_FIELD.getName()));
					ccyswapTrade.setCurrencyTwo(
							CurrencySQL.getCurrencyById(results.getLong(CURRENCY_TWO_ID_FIELD.getName())));
					ccyswapTrade.setNotionalAmountTwo(results.getBigDecimal(NOTIONAL_AMOUNT_TWO_FIELD.getName()));
					ccyswapTrade.setPaymentFixedInterestRate(
							results.getBigDecimal(PAYMENT_FIXED_INTEREST_RATE_FIELD.getName()));
					ccyswapTrade.setReceptionReferenceRateIndex(
							IndexSQL.getIndexById(results.getLong(RECEPTION_REFERENCE_RATE_INDEX_ID_FIELD.getName())));
					ccyswapTrade.setReceptionReferenceRateIndexTenor(
							Tenor.valueOf(results.getString(RECEPTION_REFERENCE_RATE_INDEX_TENOR_FIELD.getName())));
					ccyswapTrade.setInterestsToPayFixed(
							results.getLong(PAYMENT_REFERENCE_RATE_INDEX_ID_FIELD.getName()) == 0);
					if (!ccyswapTrade.isInterestsToPayFixed()) {
						ccyswapTrade.setPaymentReferenceRateIndexTenor(
								Tenor.valueOf(results.getString(PAYMENT_REFERENCE_RATE_INDEX_TENOR_FIELD.getName())));
						ccyswapTrade.setPaymentReferenceRateIndex(IndexSQL
								.getIndexById(results.getLong(PAYMENT_REFERENCE_RATE_INDEX_ID_FIELD.getName())));
						ccyswapTrade.setPaymentInterestFixing(
								InterestPayment.valueOf(results.getString(PAYMENT_INTEREST_FIXING_FIELD.getName())));
						ccyswapTrade.setPaymentSpread(results.getBigDecimal(PAYMENT_SPREAD_FIELD.getName()));
					}
					ccyswapTrade.setPaymentDayCountConvention(DayCountConventionSQL.getDayCountConventionById(
							results.getLong(PAYMENT_DAY_COUNT_CONVENTION_ID_FIELD.getName())));
					ccyswapTrade.setReceptionDayCountConvention(DayCountConventionSQL.getDayCountConventionById(
							results.getLong(RECEPTION_DAY_COUNT_CONVENTION_ID_FIELD.getName())));
					ccyswapTrade.setPaymentInterestPayment(
							InterestPayment.valueOf(results.getString(PAYMENT_INTEREST_PAYMENT_FIELD.getName())));
					ccyswapTrade.setReceptionInterestPayment(
							InterestPayment.valueOf(results.getString(RECEPTION_INTEREST_PAYMENT_FIELD.getName())));
					ccyswapTrade.setReceptionInterestFixing(
							InterestPayment.valueOf(results.getString(RECEPTION_INTEREST_FIXING_FIELD.getName())));
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
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

			ccyswapTrade = CcySwapTrade.of(TradeSQL.getCreationTime(rs));
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
				ccyswapTrade.setPaymentSpread(rs.getBigDecimal("payment_spread"));
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
			throw new TradistaTechnicalException(e);
		}

		return ccyswapTrade;
	}

	public static long saveCcySwapTrade(CcySwapTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveIrSwapTrade = (trade.getId() == 0) ? IRSwapTradeSQL.getInsertStatement(con)
						: IRSwapTradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveCcySwapTrade = (trade.getId() == 0) ? getInsertStatement(con)
						: getUpdateStatement(con)) {
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

			IRSwapTradeSQL.setPreparedStatementFields(trade, stmtSaveIrSwapTrade, tradeId);
			stmtSaveIrSwapTrade.executeUpdate();

			stmtSaveCcySwapTrade.setLong(1, trade.getCurrencyTwo().getId());
			stmtSaveCcySwapTrade.setBigDecimal(2, trade.getNotionalAmountTwo());
			stmtSaveCcySwapTrade.setLong(3, tradeId);
			stmtSaveCcySwapTrade.executeUpdate();

		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		trade.setId(tradeId);
		return tradeId;
	}
}