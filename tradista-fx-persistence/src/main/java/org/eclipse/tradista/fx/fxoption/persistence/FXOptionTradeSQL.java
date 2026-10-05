package org.eclipse.tradista.fx.fxoption.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.core.trade.persistence.VanillaOptionTradeSQL;
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

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(
			VanillaOptionTradeSQL.VANILLA_OPTION_TRADE_TABLE,
			VanillaOptionTradeSQL.TRADE_AND_VANILLA_OPTION_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return VanillaOptionTradeSQL.getInsertStatement(con);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return VanillaOptionTradeSQL.getUpdateStatement(con);
	}

	public static FXOptionTrade getTradeById(long id) {

		FXOptionTrade fxOptionTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, VanillaOptionTradeSQL.VANILLA_OPTION_TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {

				while (results.next()) {

					if (fxOptionTrade == null) {
						fxOptionTrade = FXOptionTrade.of(TradeSQL.getCreationTime(results));
					}

					TradeSQL.setTradeCommonFields(fxOptionTrade, results);
					VanillaOptionTradeSQL.setVanillaOptionTradeCommonFields(fxOptionTrade, results);

					// Building the underlying
					FXTrade underlying = FXTradeSQL.getTradeById(
							results.getLong(VanillaOptionTradeSQL.UNDERLYING_TRADE_ID_FIELD.getName()), true);
					fxOptionTrade.setUnderlying(underlying);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return fxOptionTrade;
	}

	public static FXOptionTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		FXOptionTrade fxOptionTrade = null;

		try {
			if ((rs.getLong(VanillaOptionTradeSQL.VANILLA_OPTION_TRADE_ID_FIELD.getName()) == 0)
					|| (rs.getLong("underlying_fxspot_trade_id") == 0)) {
				return null;
			}

			fxOptionTrade = FXOptionTrade.of(TradeSQL.getCreationTime(rs));
			VanillaOptionTradeSQL.setVanillaOptionTradeCommonFields(fxOptionTrade, rs);
			// Commmon fields
			TradeSQL.setTradeCommonFields(fxOptionTrade, rs);

			// Building the underlying
			Timestamp undCreationTime = rs.getTimestamp("und_fxspot_creation_time");
			FXTrade.Builder undBuilder = FXTrade.builder();
			if (undCreationTime != null) {
				undBuilder.creationTime(undCreationTime.toInstant());
			}
			FXTrade underlying = undBuilder.build();
			underlying.setId(rs.getLong("UNDERLYING_FXSPOT_TRADE_ID"));
			underlying.setCurrencyOne(CurrencySQL.getCurrencyById(rs.getLong("UNDERLYING_FXSPOT_currency_one_id")));
			underlying.setCurrency(CurrencySQL.getCurrencyById(rs.getLong("und_fxspot_currency_id")));
			underlying.setAmountOne(rs.getBigDecimal("UNDERLYING_FXSPOT_amount_one"));
			underlying.setAmount(rs.getBigDecimal("und_fxspot_amount"));
			underlying.setBuySell(rs.getBoolean("und_fxspot_buy_sell"));
			underlying.setBook(BookSQL.getBookById(rs.getLong("und_fxspot_book_id")));
			Date undSettlementDate = rs.getDate("und_fxspot_settlement_date");
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

	public static long saveFXOptionTrade(FXOptionTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveFXOptionTrade = (trade.getId() == 0) ? getInsertStatement(con)
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
			long underlyingId = FXTradeSQL.saveFXTrade(trade.getUnderlying());

			VanillaOptionTradeSQL.setPreparedStatementVanillaOptionFields(trade, stmtSaveFXOptionTrade, underlyingId,
					tradeId);
			stmtSaveFXOptionTrade.executeUpdate();
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		trade.setId(tradeId);
		return tradeId;
	}
}