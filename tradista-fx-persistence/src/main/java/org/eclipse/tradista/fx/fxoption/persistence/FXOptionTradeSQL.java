package org.eclipse.tradista.fx.fxoption.persistence;

import java.sql.Connection;
import java.sql.Date;
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
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fx.persistence.FXTradeSQL;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;

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

public class FXOptionTradeSQL {

	public static FXOptionTrade getTradeById(long id) {

		FXOptionTrade fxOptionTrade = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(
						"SELECT TRADE.*, VANILLA_OPTION_TRADE.*, FXSPOT_TRADE.*, UND_TRADE.AMOUNT UND_AMOUNT, UND_TRADE.CURRENCY_ID UND_CURRENCY_ID, UND_TRADE.SETTLEMENT_DATE UND_SETTLEMENT_DATE, UND_TRADE.TRADE_DATE UND_TRADE_DATE FROM TRADE, VANILLA_OPTION_TRADE, FXSPOT_TRADE, TRADE UND_TRADE WHERE "
								+ "TRADE.ID = VANILLA_OPTION_TRADE_ID AND VANILLA_OPTION_TRADE_ID = ? AND UNDERLYING_TRADE_ID = FXSPOT_TRADE_ID AND FXSPOT_TRADE_ID = UND_TRADE.ID")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {

				while (results.next()) {

					if (fxOptionTrade == null) {
						fxOptionTrade = new FXOptionTrade();
					}

					fxOptionTrade.setStyle(getStyle(results.getString("style")));
					fxOptionTrade.setType(OptionTrade.Type.valueOf(results.getString("type")));
					fxOptionTrade.setAmount(results.getBigDecimal("amount"));
					fxOptionTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					fxOptionTrade.setSettlementType(
							OptionTrade.SettlementType.valueOf(results.getString("settlement_type")));
					fxOptionTrade.setSettlementDateOffset(results.getInt("settlement_date_offset"));
					fxOptionTrade.setStrike(results.getBigDecimal("strike"));
					fxOptionTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					fxOptionTrade.setBuySell(results.getBoolean("buy_sell"));
					fxOptionTrade
							.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					fxOptionTrade.setMaturityDate(results.getDate("maturity_date").toLocalDate());
					fxOptionTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					fxOptionTrade.setId(results.getLong("VANILLA_OPTION_TRADE_ID"));
					fxOptionTrade.setTradeDate(results.getDate("trade_date").toLocalDate());
					fxOptionTrade.setSettlementDate(results.getDate("settlement_date").toLocalDate());
					Date exerciseDate = results.getDate("exercise_date");
					if (exerciseDate != null) {
						fxOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
					}

					// Building the underlying
					FXTrade underlying = new FXTrade();
					underlying.setId(results.getLong("UNDERLYING_TRADE_ID"));
					underlying.setCurrencyOne(CurrencySQL.getCurrencyById(results.getLong("currency_one_id")));
					underlying.setCurrency(CurrencySQL.getCurrencyById(results.getLong("und_currency_id")));
					underlying.setAmountOne(results.getBigDecimal("amount_one"));
					underlying.setAmount(results.getBigDecimal("und_amount"));
					underlying.setBuySell(results.getBoolean("buy_sell"));
					underlying.setBook(BookSQL.getBookById(results.getLong("book_id")));
					java.sql.Date undSettlementDate = results.getDate("und_settlement_date");
					if (undSettlementDate != null) {
						underlying.setSettlementDate(undSettlementDate.toLocalDate());
					}
					Date undTradeDate = results.getDate("und_trade_date");
					if (undTradeDate != null) {
						underlying.setTradeDate(undTradeDate.toLocalDate());
					}
					underlying.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));

					fxOptionTrade.setUnderlying(underlying);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return fxOptionTrade;
	}

	public static FXOptionTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		FXOptionTrade fxOptionTrade = null;

		try {
			if ((rs.getLong("vanilla_option_trade_id") == 0) || (rs.getLong("underlying_fxspot_trade_id") == 0)) {
				return null;
			}

			fxOptionTrade = new FXOptionTrade();
			fxOptionTrade.setStyle(getStyle(rs.getString("style")));
			fxOptionTrade.setType(OptionTrade.Type.valueOf(rs.getString("type")));
			fxOptionTrade.setSettlementType(OptionTrade.SettlementType.valueOf(rs.getString("settlement_type")));
			fxOptionTrade.setSettlementDateOffset(rs.getInt("settlement_date_offset"));
			fxOptionTrade.setStrike(rs.getBigDecimal("strike"));
			fxOptionTrade.setMaturityDate(rs.getDate("option_maturity_date").toLocalDate());
			Date exerciseDate = rs.getDate("exercise_date");
			if (exerciseDate != null) {
				fxOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
			}
			// Commmon fields
			TradeSQL.setTradeCommonFields(fxOptionTrade, rs);

			// Building the underlying
			FXTrade underlying = new FXTrade();
			underlying.setId(rs.getLong("UNDERLYING_FXSPOT_TRADE_ID"));
			underlying.setCurrencyOne(CurrencySQL.getCurrencyById(rs.getLong("UNDERLYING_FXSPOT_currency_one_id")));
			underlying.setCurrency(CurrencySQL.getCurrencyById(rs.getLong("und_fxspot_currency_id")));
			underlying.setAmountOne(rs.getBigDecimal("UNDERLYING_FXSPOT_amount_one"));
			underlying.setAmount(rs.getBigDecimal("und_fxspot_amount"));
			underlying.setBuySell(rs.getBoolean("und_fxspot_buy_sell"));
			underlying.setBook(BookSQL.getBookById(rs.getLong("und_fxspot_book_id")));
			java.sql.Date undSettlementDate = rs.getDate("und_fxspot_settlement_date");
			underlying.setCreationDate(rs.getDate("und_fxspot_creation_date").toLocalDate());
			if (undSettlementDate != null) {
				underlying.setSettlementDate(undSettlementDate.toLocalDate());
			}
			Date undTradeDate = rs.getDate("und_fxspot_trade_date");
			if (undTradeDate != null) {
				underlying.setTradeDate(undTradeDate.toLocalDate());
			}
			underlying.setCounterparty(LegalEntitySQL.getLegalEntityById(rs.getLong("und_fxspot_counterparty_id")));

			fxOptionTrade.setUnderlying(underlying);
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return fxOptionTrade;
	}

	private static VanillaOptionTrade.Style getStyle(String name) {
		if (name.equals("EUROPEAN")) {
			return VanillaOptionTrade.Style.EUROPEAN;
		} else
			return VanillaOptionTrade.Style.AMERICAN;
	}

	public static long saveFXOptionTrade(FXOptionTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, SETTLEMENT_DATE, PRODUCT_ID, COUNTERPARTY_ID, AMOUNT, CURRENCY_ID, BOOK_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, SETTLEMENT_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, AMOUNT=?, CURRENCY_ID=?, BOOK_ID=? WHERE ID=?");
				PreparedStatement stmtSaveFXOptionTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO VANILLA_OPTION_TRADE(STYLE, TYPE, MATURITY_DATE, EXERCISE_DATE, UNDERLYING_TRADE_ID, SETTLEMENT_TYPE, SETTLEMENT_DATE_OFFSET, STRIKE, VANILLA_OPTION_TRADE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE VANILLA_OPTION_TRADE SET STYLE=?, TYPE=?, MATURITY_DATE=?, EXERCISE_DATE=?, UNDERLYING_TRADE_ID=?, SETTLEMENT_TYPE=?, SETTLEMENT_DATE_OFFSET=?, STRIKE=? WHERE VANILLA_OPTION_TRADE_ID=?")) {
			boolean isBuy = trade.isBuy();
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(9, java.sql.Date.valueOf(trade.getCreationDate()));
			} else {
				stmtSaveTrade.setLong(9, trade.getId());
			}
			stmtSaveTrade.setBoolean(1, isBuy);
			stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			stmtSaveTrade.setDate(3, java.sql.Date.valueOf(trade.getSettlementDate()));
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
			long underlyingId = FXTradeSQL.saveFXTrade(trade.getUnderlying());

			stmtSaveFXOptionTrade.setString(1, trade.getStyle().name());
			stmtSaveFXOptionTrade.setString(2, trade.getType().name());
			stmtSaveFXOptionTrade.setDate(3, java.sql.Date.valueOf(trade.getMaturityDate()));
			LocalDate exerciseDate = trade.getExerciseDate();
			if (exerciseDate != null) {
				stmtSaveFXOptionTrade.setDate(4, java.sql.Date.valueOf(exerciseDate));
			} else {
				stmtSaveFXOptionTrade.setNull(4, java.sql.Types.DATE);
			}
			stmtSaveFXOptionTrade.setLong(5, underlyingId);
			stmtSaveFXOptionTrade.setString(6, trade.getSettlementType().name());
			stmtSaveFXOptionTrade.setInt(7, trade.getSettlementDateOffset());
			stmtSaveFXOptionTrade.setBigDecimal(8, trade.getStrike());
			stmtSaveFXOptionTrade.setLong(9, tradeId);
			stmtSaveFXOptionTrade.executeUpdate();
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}
}