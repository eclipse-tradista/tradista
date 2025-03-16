package org.eclipse.tradista.ir.irswapoption.persistence;

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

	public static IRSwapOptionTrade getTradeById(long id) {

		IRSwapOptionTrade irSwapOptionTrade = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement("SELECT TRADE.*, " + "VANILLA_OPTION_TRADE.*,"
						+ " IRSWAP_TRADE.*, IRSWAP_OPTION_TRADE.*," + " UND_TRADE.AMOUNT UND_AMOUNT,"
						+ " UND_TRADE.BUY_SELL UND_BUY_SELL," + " UND_TRADE.CREATION_DATE UND_CREATION_DATE,"
						+ " UND_TRADE.settlement_date UND_SETTLEMENT_DATE," + " UND_TRADE.trade_date UND_TRADE_DATE,"
						+ " UND_TRADE.CURRENCY_ID UND_CURRENCY_ID, IRSWAP_TRADE.MATURITY_DATE UND_MATURITY_DATE"
						+ "  FROM TRADE, TRADE UND_TRADE, VANILLA_OPTION_TRADE, IRSWAP_TRADE, IRSWAP_OPTION_TRADE WHERE "
						+ "TRADE.ID = VANILLA_OPTION_TRADE_ID " + "AND VANILLA_OPTION_TRADE_ID = ?"
						+ " AND IRSWAP_TRADE_ID = UND_TRADE.ID"
						+ " AND UNDERLYING_TRADE_ID = IRSWAP_TRADE_ID AND VANILLA_OPTION_TRADE_ID = IRSWAP_OPTION_TRADE_ID")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (irSwapOptionTrade == null) {
						irSwapOptionTrade = new IRSwapOptionTrade();
					}

					irSwapOptionTrade.setId(id);
					irSwapOptionTrade.setStyle(getStyle(results.getString("style")));
					irSwapOptionTrade.setType(OptionTrade.Type.valueOf(results.getString("type")));
					irSwapOptionTrade.setAmount(results.getBigDecimal("amount"));
					irSwapOptionTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					irSwapOptionTrade.setSettlementType(
							OptionTrade.SettlementType.valueOf(results.getString("settlement_type")));
					irSwapOptionTrade.setSettlementDateOffset(results.getInt("settlement_date_offset"));
					irSwapOptionTrade.setStrike(results.getBigDecimal("strike"));
					irSwapOptionTrade.setBuySell(results.getBoolean("BUY_SELL"));
					irSwapOptionTrade
							.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					irSwapOptionTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					irSwapOptionTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					irSwapOptionTrade.setMaturityDate(results.getDate("maturity_date").toLocalDate());
					irSwapOptionTrade.setSettlementDate(results.getDate("settlement_date").toLocalDate());
					Date exerciseDate = results.getDate("exercise_date");
					if (exerciseDate != null) {
						irSwapOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
					}
					long alternativeCashSettlementReferenceRateIndexId = results
							.getLong("alternative_cash_settlement_reference_rate_index_id");
					if (alternativeCashSettlementReferenceRateIndexId > 0) {
						irSwapOptionTrade.setAlternativeCashSettlementReferenceRateIndex(
								IndexSQL.getIndexById(alternativeCashSettlementReferenceRateIndexId));
					}
					String alternativeCashSettlementReferenceRateIndexTenor = results
							.getString("alternative_cash_settlement_reference_rate_index_tenor");
					if (alternativeCashSettlementReferenceRateIndexTenor != null) {
						irSwapOptionTrade.setAlternativeCashSettlementReferenceRateIndexTenor(
								Tenor.valueOf(alternativeCashSettlementReferenceRateIndexTenor));
					}
					irSwapOptionTrade.setCashSettlementAmount(results.getBigDecimal("cash_settlement_amount"));

					// Building the underlying
					SingleCurrencyIRSwapTrade underlying = new SingleCurrencyIRSwapTrade();
					underlying.setId(results.getLong("UNDERLYING_TRADE_ID"));
					underlying.setAmount(results.getBigDecimal("UND_AMOUNT"));
					underlying.setBuySell(results.getBoolean("UND_BUY_SELL"));
					underlying.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					underlying.setBook(BookSQL.getBookById(results.getLong("book_id")));
					underlying.setCreationDate(results.getDate("UND_CREATION_DATE").toLocalDate());
					underlying.setCurrency(CurrencySQL.getCurrencyById(results.getLong("UND_CURRENCY_ID")));
					underlying.setPaymentFrequency(Tenor.valueOf(results.getString("payment_frequency")));
					underlying.setReceptionFrequency(Tenor.valueOf(results.getString("reception_frequency")));
					underlying.setInterestsToPayFixed((results.getLong("payment_reference_rate_index_id") == 0));
					java.sql.Date maturityDate = results.getDate("und_maturity_date");
					if (maturityDate != null) {
						underlying.setMaturityDate(maturityDate.toLocalDate());
					}
					underlying.setPaymentDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong("payment_day_count_convention_id")));
					underlying.setPaymentFixedInterestRate(results.getBigDecimal("payment_fixed_interest_rate"));
					underlying.setReceptionDayCountConvention(DayCountConventionSQL
							.getDayCountConventionById(results.getLong("reception_day_count_convention_id")));
					underlying.setReceptionReferenceRateIndex(
							IndexSQL.getIndexById(results.getLong("reception_reference_rate_index_id")));
					if (!underlying.isInterestsToPayFixed()) {
						underlying.setPaymentReferenceRateIndex(
								IndexSQL.getIndexById(results.getLong("payment_reference_rate_index_id")));
						underlying.setPaymentReferenceRateIndexTenor(
								Tenor.valueOf(results.getString("payment_reference_rate_index_tenor")));
						underlying.setPaymentInterestFixing(
								InterestPayment.valueOf(results.getString("payment_interest_fixing")));
						underlying.setPaymentSpread(results.getBigDecimal("payment_spread"));
					}
					underlying.setReceptionReferenceRateIndexTenor(
							Tenor.valueOf(results.getString("reception_reference_rate_index_tenor")));
					underlying.setReceptionSpread(results.getBigDecimal("reception_spread"));
					java.sql.Date tradeDate = results.getDate("und_trade_date");
					if (tradeDate != null) {
						underlying.setTradeDate(tradeDate.toLocalDate());
					}
					java.sql.Date settlementDate = results.getDate("und_settlement_date");
					if (settlementDate != null) {
						underlying.setSettlementDate(settlementDate.toLocalDate());
					}
					underlying.setPaymentInterestPayment(
							InterestPayment.valueOf(results.getString("payment_interest_payment")));
					underlying.setReceptionInterestPayment(
							InterestPayment.valueOf(results.getString("reception_interest_payment")));
					underlying.setReceptionInterestFixing(
							InterestPayment.valueOf(results.getString("reception_interest_fixing")));

					irSwapOptionTrade.setUnderlying(underlying);

				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
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
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, SETTLEMENT_DATE, PRODUCT_ID, COUNTERPARTY_ID, AMOUNT, CURRENCY_ID, BOOK_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, SETTLEMENT_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, AMOUNT=?, CURRENCY_ID=?, BOOK_ID=? WHERE ID=?");
				PreparedStatement stmtSaveVanillaOptionTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO VANILLA_OPTION_TRADE(STYLE, TYPE, MATURITY_DATE, EXERCISE_DATE, UNDERLYING_TRADE_ID, SETTLEMENT_TYPE, SETTLEMENT_DATE_OFFSET, STRIKE, VANILLA_OPTION_TRADE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE VANILLA_OPTION_TRADE SET STYLE=?, TYPE=?, MATURITY_DATE=?, EXERCISE_DATE=?, UNDERLYING_TRADE_ID=?, SETTLEMENT_TYPE=?, SETTLEMENT_DATE_OFFSET=?, STRIKE=? WHERE VANILLA_OPTION_TRADE_ID=?");
				PreparedStatement stmtSaveIRSwapOptionTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO IRSWAP_OPTION_TRADE(CASH_SETTLEMENT_AMOUNT, ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_ID, ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_TENOR, IRSWAP_OPTION_TRADE_ID) VALUES (?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE IRSWAP_OPTION_TRADE SET CASH_SETTLEMENT_AMOUNT=?, ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_ID=?, ALTERNATIVE_CASH_SETTLEMENT_REFERENCE_RATE_INDEX_TENOR=?  WHERE IRSWAP_OPTION_TRADE_ID=?")) {
			boolean isBuy = trade.isBuy();
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(9, java.sql.Date.valueOf(LocalDate.now()));
			} else {
				stmtSaveTrade.setLong(9, trade.getId());
			}
			stmtSaveTrade.setBoolean(1, isBuy);
			stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			stmtSaveTrade.setDate(3, java.sql.Date.valueOf(trade.getMaturityDate()));
			stmtSaveTrade.setNull(4, java.sql.Types.BIGINT);
			stmtSaveTrade.setLong(5, trade.getCounterparty().getId());
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
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
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
			underlying.setCreationDate(rs.getDate("UND_IRSWAP_CREATION_DATE").toLocalDate());
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
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return irSwapOptionTrade;
	}

}