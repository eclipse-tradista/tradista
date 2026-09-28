package org.eclipse.tradista.ir.irswapoption.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MATURITY_DATE;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_TABLE;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

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
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;
import org.eclipse.tradista.ir.irswap.persistence.IRSwapTradeSQL;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;

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

public class IRSwapOptionTradeSQL {

	// VANILLA_OPTION_TRADE table and fields
	public static final Field VANILLA_OPTION_TRADE_ID_FIELD = new Field("VANILLA_OPTION_TRADE_ID");
	public static final Field STYLE_FIELD = new Field("STYLE");
	public static final Field TYPE_FIELD = new Field("TYPE");
	public static final Field STRIKE_FIELD = new Field("STRIKE");
	public static final Field MATURITY_DATE_FIELD = new Field(MATURITY_DATE);
	public static final Field EXERCISE_DATE_FIELD = new Field("EXERCISE_DATE");
	public static final Field UNDERLYING_TRADE_ID_FIELD = new Field("UNDERLYING_TRADE_ID");
	public static final Field SETTLEMENT_TYPE_FIELD = new Field("SETTLEMENT_TYPE");
	public static final Field SETTLEMENT_DATE_OFFSET_FIELD = new Field("SETTLEMENT_DATE_OFFSET");
	public static final Field QUANTITY_FIELD = new Field("QUANTITY");

	private static final Field[] VANILLA_OPTION_TRADE_FIELDS = { VANILLA_OPTION_TRADE_ID_FIELD, STYLE_FIELD, TYPE_FIELD,
			STRIKE_FIELD, MATURITY_DATE_FIELD, EXERCISE_DATE_FIELD, UNDERLYING_TRADE_ID_FIELD, SETTLEMENT_TYPE_FIELD,
			SETTLEMENT_DATE_OFFSET_FIELD, QUANTITY_FIELD };

	private static final Field[] VANILLA_OPTION_TRADE_FIELDS_FOR_INSERT = { STYLE_FIELD, TYPE_FIELD, MATURITY_DATE_FIELD,
			EXERCISE_DATE_FIELD, UNDERLYING_TRADE_ID_FIELD, SETTLEMENT_TYPE_FIELD, SETTLEMENT_DATE_OFFSET_FIELD,
			STRIKE_FIELD, VANILLA_OPTION_TRADE_ID_FIELD };

	private static final Field[] VANILLA_OPTION_TRADE_FIELDS_FOR_UPDATE = { STYLE_FIELD, TYPE_FIELD, MATURITY_DATE_FIELD,
			EXERCISE_DATE_FIELD, UNDERLYING_TRADE_ID_FIELD, SETTLEMENT_TYPE_FIELD, SETTLEMENT_DATE_OFFSET_FIELD,
			STRIKE_FIELD };

	public static final Table VANILLA_OPTION_TRADE_TABLE = new Table("VANILLA_OPTION_TRADE",
			VANILLA_OPTION_TRADE_FIELDS);

	// IRSWAP_OPTION_TRADE table and fields
	public static final Field IRSWAP_OPTION_TRADE_ID_FIELD = new Field("IRSWAP_OPTION_TRADE_ID");
	public static final Field CASH_SETTLEMENT_AMOUNT_FIELD = new Field("CASH_SETTLEMENT_AMOUNT");
	public static final Field ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_ID_FIELD = new Field(
			"ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_ID");
	public static final Field ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_TENOR_FIELD = new Field(
			"ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_TENOR");

	private static final Field[] IRSWAP_OPTION_TRADE_FIELDS = { IRSWAP_OPTION_TRADE_ID_FIELD,
			CASH_SETTLEMENT_AMOUNT_FIELD, ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_ID_FIELD,
			ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_TENOR_FIELD };

	private static final Field[] IRSWAP_OPTION_TRADE_FIELDS_FOR_INSERT = { CASH_SETTLEMENT_AMOUNT_FIELD,
			ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_ID_FIELD,
			ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_TENOR_FIELD, IRSWAP_OPTION_TRADE_ID_FIELD };

	private static final Field[] IRSWAP_OPTION_TRADE_FIELDS_FOR_UPDATE = { CASH_SETTLEMENT_AMOUNT_FIELD,
			ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_ID_FIELD,
			ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_TENOR_FIELD };

	public static final Table IRSWAP_OPTION_TRADE_TABLE = new Table("IRSWAP_OPTION_TRADE",
			IRSWAP_OPTION_TRADE_FIELDS);

	public static final Join TRADE_AND_VANILLA_OPTION_TRADE_INNER_JOIN = Join.innerEq(TRADE_TABLE, ID_FIELD,
			VANILLA_OPTION_TRADE_ID_FIELD);

	public static final Join VANILLA_OPTION_TRADE_AND_IRSWAP_OPTION_TRADE_INNER_JOIN = Join.innerEq(
			VANILLA_OPTION_TRADE_TABLE, VANILLA_OPTION_TRADE_ID_FIELD, IRSWAP_OPTION_TRADE_ID_FIELD);

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(IRSWAP_OPTION_TRADE_TABLE,
			VANILLA_OPTION_TRADE_AND_IRSWAP_OPTION_TRADE_INNER_JOIN, TRADE_AND_VANILLA_OPTION_TRADE_INNER_JOIN);

	public static PreparedStatement getVanillaOptionInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, VANILLA_OPTION_TRADE_TABLE,
				VANILLA_OPTION_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getVanillaOptionUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, VANILLA_OPTION_TRADE_ID_FIELD,
				VANILLA_OPTION_TRADE_TABLE, VANILLA_OPTION_TRADE_FIELDS_FOR_UPDATE);
	}

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, IRSWAP_OPTION_TRADE_TABLE,
				IRSWAP_OPTION_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, IRSWAP_OPTION_TRADE_ID_FIELD,
				IRSWAP_OPTION_TRADE_TABLE, IRSWAP_OPTION_TRADE_FIELDS_FOR_UPDATE);
	}

	public static IRSwapOptionTrade getTradeById(long id) {

		IRSwapOptionTrade irSwapOptionTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, IRSWAP_OPTION_TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (irSwapOptionTrade == null) {
						irSwapOptionTrade = new IRSwapOptionTrade();
					}

					TradeSQL.setTradeCommonFields(irSwapOptionTrade, results);
					irSwapOptionTrade.setStyle(getStyle(results.getString(STYLE_FIELD.getName())));
					irSwapOptionTrade.setType(OptionTrade.Type.valueOf(results.getString(TYPE_FIELD.getName())));
					irSwapOptionTrade.setSettlementType(
							OptionTrade.SettlementType.valueOf(results.getString(SETTLEMENT_TYPE_FIELD.getName())));
					irSwapOptionTrade.setSettlementDateOffset(results.getInt(SETTLEMENT_DATE_OFFSET_FIELD.getName()));
					irSwapOptionTrade.setStrike(results.getBigDecimal(STRIKE_FIELD.getName()));
					irSwapOptionTrade.setMaturityDate(results.getDate(MATURITY_DATE_FIELD.getName()).toLocalDate());
					Date exerciseDate = results.getDate(EXERCISE_DATE_FIELD.getName());
					if (exerciseDate != null) {
						irSwapOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
					}
					long alternativeCashSettlementReferenceRateIndexId = results
							.getLong(ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_ID_FIELD.getName());
					if (alternativeCashSettlementReferenceRateIndexId > 0) {
						irSwapOptionTrade.setAlternativeCashSettlementReferenceRateIndex(
								IndexSQL.getIndexById(alternativeCashSettlementReferenceRateIndexId));
					}
					String alternativeCashSettlementReferenceRateIndexTenor = results
							.getString(ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_TENOR_FIELD.getName());
					if (alternativeCashSettlementReferenceRateIndexTenor != null) {
						irSwapOptionTrade.setAlternativeCashSettlementReferenceRateIndexTenor(
								Tenor.valueOf(alternativeCashSettlementReferenceRateIndexTenor));
					}
					irSwapOptionTrade
							.setCashSettlementAmount(results.getBigDecimal(CASH_SETTLEMENT_AMOUNT_FIELD.getName()));

					// Building the underlying
					SingleCurrencyIRSwapTrade underlying = IRSwapTradeSQL
							.getTradeById(results.getLong(UNDERLYING_TRADE_ID_FIELD.getName()), true);
					irSwapOptionTrade.setUnderlying(underlying);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return irSwapOptionTrade;
	}

	private static VanillaOptionTrade.Style getStyle(String name) {
		if (name.equals("EUROPEAN")) {
			return VanillaOptionTrade.Style.EUROPEAN;
		} else
			return VanillaOptionTrade.Style.AMERICAN;
	}

	public static long saveIRSwapOptionTrade(IRSwapOptionTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveVanillaOptionTrade = (trade.getId() == 0)
						? getVanillaOptionInsertStatement(con)
						: getVanillaOptionUpdateStatement(con);
				PreparedStatement stmtSaveIRSwapOptionTrade = (trade.getId() == 0) ? getInsertStatement(con)
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

			// Underlying saving
			long underlyingId = IRSwapTradeSQL.saveIRSwapTrade(trade.getUnderlying());

			stmtSaveVanillaOptionTrade.setString(1, trade.getStyle().name());
			stmtSaveVanillaOptionTrade.setString(2, trade.getType().name());
			stmtSaveVanillaOptionTrade.setDate(3, java.sql.Date.valueOf(trade.getMaturityDate()));
			LocalDate exerciseDate = trade.getExerciseDate();
			if (exerciseDate != null) {
				stmtSaveVanillaOptionTrade.setDate(4, java.sql.Date.valueOf(exerciseDate));
			} else {
				stmtSaveVanillaOptionTrade.setNull(4, java.sql.Types.DATE);
			}
			stmtSaveVanillaOptionTrade.setLong(5, underlyingId);
			stmtSaveVanillaOptionTrade.setString(6, trade.getSettlementType().name());
			stmtSaveVanillaOptionTrade.setInt(7, trade.getSettlementDateOffset());
			stmtSaveVanillaOptionTrade.setBigDecimal(8, trade.getStrike());
			stmtSaveVanillaOptionTrade.setLong(9, tradeId);
			stmtSaveVanillaOptionTrade.executeUpdate();

			if (trade.getCashSettlementAmount() != null) {
				stmtSaveIRSwapOptionTrade.setBigDecimal(1, trade.getCashSettlementAmount());
			} else {
				stmtSaveIRSwapOptionTrade.setNull(1, Types.DECIMAL);
			}
			if (trade.getAlternativeCashSettlementReferenceRateIndex() != null) {
				stmtSaveIRSwapOptionTrade.setLong(2, trade.getAlternativeCashSettlementReferenceRateIndex().getId());
			} else {
				stmtSaveIRSwapOptionTrade.setNull(2, Types.BIGINT);
			}
			if (trade.getAlternativeCashSettlementReferenceRateIndexTenor() != null) {
				stmtSaveIRSwapOptionTrade.setString(3,
						trade.getAlternativeCashSettlementReferenceRateIndexTenor().name());
			} else {
				stmtSaveIRSwapOptionTrade.setNull(3, Types.VARCHAR);
			}
			stmtSaveIRSwapOptionTrade.setLong(4, tradeId);
			stmtSaveIRSwapOptionTrade.executeUpdate();
		} catch (SQLException | TradistaBusinessException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}

	public static IRSwapOptionTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		IRSwapOptionTrade irSwapOptionTrade = null;
		try {
			if ((rs.getLong("vanilla_option_trade_id") == 0) || (rs.getLong("underlying_irswap_trade_id") == 0)) {
				return null;
			}

			irSwapOptionTrade = new IRSwapOptionTrade();
			irSwapOptionTrade.setStyle(getStyle(rs.getString("style")));
			irSwapOptionTrade.setType(OptionTrade.Type.valueOf(rs.getString("type")));
			irSwapOptionTrade.setSettlementType(OptionTrade.SettlementType.valueOf(rs.getString("settlement_type")));
			irSwapOptionTrade.setSettlementDateOffset(rs.getInt("settlement_date_offset"));
			irSwapOptionTrade.setStrike(rs.getBigDecimal("strike"));
			irSwapOptionTrade.setMaturityDate(rs.getDate("option_maturity_date").toLocalDate());
			Date exerciseDate = rs.getDate("exercise_date");
			if (exerciseDate != null) {
				irSwapOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
			}
			long alternativeCashSettlementReferenceRateIndexId = rs
					.getLong("alternative_cash_settlement_reference_rate_index_id");
			if (alternativeCashSettlementReferenceRateIndexId > 0) {
				irSwapOptionTrade.setAlternativeCashSettlementReferenceRateIndex(
						IndexSQL.getIndexById(alternativeCashSettlementReferenceRateIndexId));
			}
			String alternativeCashSettlementReferenceRateIndexTenor = rs
					.getString("alternative_cash_settlement_reference_rate_index_tenor");
			if (alternativeCashSettlementReferenceRateIndexTenor != null) {
				irSwapOptionTrade.setAlternativeCashSettlementReferenceRateIndexTenor(
						Tenor.valueOf(alternativeCashSettlementReferenceRateIndexTenor));
			}
			irSwapOptionTrade.setCashSettlementAmount(rs.getBigDecimal("cash_settlement_amount"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(irSwapOptionTrade, rs);

			// Building the underlying
			SingleCurrencyIRSwapTrade underlying = new SingleCurrencyIRSwapTrade();
			underlying.setId(rs.getLong("UNDERLYING_IRSWAP_TRADE_ID"));
			underlying.setAmount(rs.getBigDecimal("UND_IRSWAP_AMOUNT"));
			underlying.setBuySell(rs.getBoolean("UND_IRSWAP_BUY_SELL"));
			underlying.setCounterparty(LegalEntitySQL.getLegalEntityById(rs.getLong("UND_IRSWAP_counterparty_id")));
			underlying.setBook(BookSQL.getBookById(rs.getLong("UND_IRSWAP_book_id")));
			java.sql.Timestamp undCreationTime = rs.getTimestamp("UND_IRSWAP_CREATION_TIME");
			if (undCreationTime != null) {
				underlying.setCreationDate(undCreationTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
			}
			underlying.setCurrency(CurrencySQL.getCurrencyById(rs.getLong("UND_IRSWAP_CURRENCY_ID")));
			underlying.setPaymentFrequency(Tenor.valueOf(rs.getString("UNDERLYING_IRSWAP_payment_frequency")));
			underlying.setReceptionFrequency(Tenor.valueOf(rs.getString("UNDERLYING_IRSWAP_reception_frequency")));
			underlying.setInterestsToPayFixed((rs.getLong("UNDERLYING_IRSWAP_payment_reference_rate_index_id") == 0));
			java.sql.Date maturityDate = rs.getDate("UNDERLYING_IRSWAP_maturity_date");
			if (maturityDate != null) {
				underlying.setMaturityDate(maturityDate.toLocalDate());
			}
			underlying.setPaymentDayCountConvention(DayCountConventionSQL
					.getDayCountConventionById(rs.getLong("UNDERLYING_IRSWAP_payment_day_count_convention_id")));
			underlying.setPaymentFixedInterestRate(rs.getBigDecimal("UNDERLYING_IRSWAP_payment_fixed_interest_rate"));
			if (!underlying.isInterestsToPayFixed()) {
				underlying.setPaymentReferenceRateIndex(
						IndexSQL.getIndexById(rs.getLong("UNDERLYING_IRSWAP_payment_reference_rate_index_id")));
				underlying.setPaymentReferenceRateIndexTenor(
						Tenor.valueOf(rs.getString("UNDERLYING_IRSWAP_payment_reference_rate_index_tenor")));
				underlying.setPaymentInterestFixing(
						InterestPayment.valueOf(rs.getString("UNDERLYING_IRSWAP_payment_interest_fixing")));
				underlying.setPaymentSpread(rs.getBigDecimal("UNDERLYING_IRSWAP_payment_spread"));
			}
			underlying.setReceptionDayCountConvention(DayCountConventionSQL
					.getDayCountConventionById(rs.getLong("UNDERLYING_IRSWAP_reception_day_count_convention_id")));
			underlying.setReceptionReferenceRateIndex(
					IndexSQL.getIndexById(rs.getLong("UNDERLYING_IRSWAP_reception_reference_rate_index_id")));
			underlying.setReceptionReferenceRateIndexTenor(
					Tenor.valueOf(rs.getString("UNDERLYING_IRSWAP_reception_reference_rate_index_tenor")));
			underlying.setReceptionSpread(rs.getBigDecimal("UNDERLYING_IRSWAP_reception_spread"));
			underlying.setPaymentInterestPayment(
					InterestPayment.valueOf(rs.getString("UNDERLYING_IRSWAP_payment_interest_payment")));
			underlying.setReceptionInterestPayment(
					InterestPayment.valueOf(rs.getString("UNDERLYING_IRSWAP_reception_interest_payment")));
			underlying.setReceptionInterestFixing(
					InterestPayment.valueOf(rs.getString("UNDERLYING_IRSWAP_reception_interest_fixing")));
			java.sql.Date undTradeDate = rs.getDate("UND_IRSWAP_trade_date");
			if (undTradeDate != null) {
				underlying.setTradeDate(undTradeDate.toLocalDate());
			}
			java.sql.Date undSettlementDate = rs.getDate("UND_IRSWAP_settlement_date");
			if (undSettlementDate != null) {
				underlying.setSettlementDate(undSettlementDate.toLocalDate());
			}

			irSwapOptionTrade.setUnderlying(underlying);
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return irSwapOptionTrade;
	}

}