package org.eclipse.tradista.ir.irswapoption.persistence;

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
import org.eclipse.tradista.core.trade.persistence.VanillaOptionTradeSQL;
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

	public static final Table IRSWAP_OPTION_TRADE_TABLE = new Table("IRSWAP_OPTION_TRADE", IRSWAP_OPTION_TRADE_FIELDS);

	public static final Join VANILLA_OPTION_TRADE_AND_IRSWAP_OPTION_TRADE_INNER_JOIN = Join.innerEq(
			VanillaOptionTradeSQL.VANILLA_OPTION_TRADE_TABLE, VanillaOptionTradeSQL.VANILLA_OPTION_TRADE_ID_FIELD,
			IRSWAP_OPTION_TRADE_ID_FIELD);

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(IRSWAP_OPTION_TRADE_TABLE,
			VANILLA_OPTION_TRADE_AND_IRSWAP_OPTION_TRADE_INNER_JOIN,
			VanillaOptionTradeSQL.TRADE_AND_VANILLA_OPTION_TRADE_INNER_JOIN);

	public static PreparedStatement getVanillaOptionInsertStatement(Connection con) {
		return VanillaOptionTradeSQL.getInsertStatement(con);
	}

	public static PreparedStatement getVanillaOptionUpdateStatement(Connection con) {
		return VanillaOptionTradeSQL.getUpdateStatement(con);
	}

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, IRSWAP_OPTION_TRADE_TABLE,
				IRSWAP_OPTION_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, IRSWAP_OPTION_TRADE_ID_FIELD, IRSWAP_OPTION_TRADE_TABLE,
				IRSWAP_OPTION_TRADE_FIELDS_FOR_UPDATE);
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
						irSwapOptionTrade = IRSwapOptionTrade.of(TradeSQL.getCreationTime(results));
					}

					TradeSQL.setTradeCommonFields(irSwapOptionTrade, results);
					VanillaOptionTradeSQL.setVanillaOptionTradeCommonFields(irSwapOptionTrade, results);
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
					SingleCurrencyIRSwapTrade underlying = IRSwapTradeSQL.getTradeById(
							results.getLong(VanillaOptionTradeSQL.UNDERLYING_TRADE_ID_FIELD.getName()), true);
					irSwapOptionTrade.setUnderlying(underlying);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return irSwapOptionTrade;
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

			VanillaOptionTradeSQL.setPreparedStatementVanillaOptionFields(trade, stmtSaveVanillaOptionTrade,
					underlyingId, tradeId);
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
			if ((rs.getLong(VanillaOptionTradeSQL.VANILLA_OPTION_TRADE_ID_FIELD.getName()) == 0)
					|| (rs.getLong("underlying_irswap_trade_id") == 0)) {
				return null;
			}

			irSwapOptionTrade = IRSwapOptionTrade.of(TradeSQL.getCreationTime(rs));
			VanillaOptionTradeSQL.setVanillaOptionTradeCommonFields(irSwapOptionTrade, rs);
			long alternativeCashSettlementReferenceRateIndexId = rs
					.getLong(ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_ID_FIELD.getName());
			if (alternativeCashSettlementReferenceRateIndexId > 0) {
				irSwapOptionTrade.setAlternativeCashSettlementReferenceRateIndex(
						IndexSQL.getIndexById(alternativeCashSettlementReferenceRateIndexId));
			}
			String alternativeCashSettlementReferenceRateIndexTenor = rs
					.getString(ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_TENOR_FIELD.getName());
			if (alternativeCashSettlementReferenceRateIndexTenor != null) {
				irSwapOptionTrade.setAlternativeCashSettlementReferenceRateIndexTenor(
						Tenor.valueOf(alternativeCashSettlementReferenceRateIndexTenor));
			}
			irSwapOptionTrade.setCashSettlementAmount(rs.getBigDecimal(CASH_SETTLEMENT_AMOUNT_FIELD.getName()));

			// Commmon fields
			TradeSQL.setTradeCommonFields(irSwapOptionTrade, rs);

			// Building the underlying
			java.sql.Timestamp undCreationTime = rs.getTimestamp("UND_IRSWAP_CREATION_TIME");
			SingleCurrencyIRSwapTrade.Builder undBuilder = SingleCurrencyIRSwapTrade.builder();
			if (undCreationTime != null) {
				undBuilder.creationTime(undCreationTime.toInstant());
			}
			SingleCurrencyIRSwapTrade underlying = undBuilder.build();
			underlying.setId(rs.getLong("UNDERLYING_IRSWAP_TRADE_ID"));
			underlying.setAmount(rs.getBigDecimal("UND_IRSWAP_AMOUNT"));
			underlying.setBuySell(rs.getBoolean("UND_IRSWAP_BUY_SELL"));
			underlying.setCounterparty(LegalEntitySQL.getLegalEntityById(rs.getLong("UND_IRSWAP_counterparty_id")));
			underlying.setBook(BookSQL.getBookById(rs.getLong("UND_IRSWAP_book_id")));
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