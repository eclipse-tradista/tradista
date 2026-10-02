package org.eclipse.tradista.fx.fxoption.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MATURITY_DATE;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_TABLE;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
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

	public static final Field VANILLA_OPTION_TRADE_ID_FIELD = new Field("VANILLA_OPTION_TRADE_ID");
	public static final Field STYLE_FIELD = new Field("STYLE");
	public static final Field TYPE_FIELD = new Field(TradistaDBConstants.TYPE);
	public static final Field STRIKE_FIELD = new Field("STRIKE");
	public static final Field MATURITY_DATE_FIELD = new Field(MATURITY_DATE);
	public static final Field EXERCISE_DATE_FIELD = new Field("EXERCISE_DATE");
	public static final Field UNDERLYING_TRADE_ID_FIELD = new Field("UNDERLYING_TRADE_ID");
	public static final Field SETTLEMENT_TYPE_FIELD = new Field("SETTLEMENT_TYPE");
	public static final Field SETTLEMENT_DATE_OFFSET_FIELD = new Field("SETTLEMENT_DATE_OFFSET");
	public static final Field QUANTITY_FIELD = new Field(TradistaDBConstants.QUANTITY);

	private static final Field[] VANILLA_OPTION_TRADE_FIELDS = { VANILLA_OPTION_TRADE_ID_FIELD, STYLE_FIELD, TYPE_FIELD,
			STRIKE_FIELD, MATURITY_DATE_FIELD, EXERCISE_DATE_FIELD, UNDERLYING_TRADE_ID_FIELD, SETTLEMENT_TYPE_FIELD,
			SETTLEMENT_DATE_OFFSET_FIELD, QUANTITY_FIELD };

	private static final Field[] VANILLA_OPTION_TRADE_FIELDS_FOR_INSERT = { STYLE_FIELD, TYPE_FIELD,
			MATURITY_DATE_FIELD, EXERCISE_DATE_FIELD, UNDERLYING_TRADE_ID_FIELD, SETTLEMENT_TYPE_FIELD,
			SETTLEMENT_DATE_OFFSET_FIELD, STRIKE_FIELD, VANILLA_OPTION_TRADE_ID_FIELD };

	private static final Field[] VANILLA_OPTION_TRADE_FIELDS_FOR_UPDATE = { STYLE_FIELD, TYPE_FIELD,
			MATURITY_DATE_FIELD, EXERCISE_DATE_FIELD, UNDERLYING_TRADE_ID_FIELD, SETTLEMENT_TYPE_FIELD,
			SETTLEMENT_DATE_OFFSET_FIELD, STRIKE_FIELD };

	public static final Table VANILLA_OPTION_TRADE_TABLE = new Table("VANILLA_OPTION_TRADE",
			VANILLA_OPTION_TRADE_FIELDS);

	public static final Join TRADE_AND_VANILLA_OPTION_TRADE_INNER_JOIN = Join.innerEq(TRADE_TABLE, ID_FIELD,
			VANILLA_OPTION_TRADE_ID_FIELD);

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(VANILLA_OPTION_TRADE_TABLE,
			TRADE_AND_VANILLA_OPTION_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, VANILLA_OPTION_TRADE_TABLE,
				VANILLA_OPTION_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, VANILLA_OPTION_TRADE_ID_FIELD,
				VANILLA_OPTION_TRADE_TABLE, VANILLA_OPTION_TRADE_FIELDS_FOR_UPDATE);
	}

	public static FXOptionTrade getTradeById(long id) {

		FXOptionTrade fxOptionTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, VANILLA_OPTION_TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {

				while (results.next()) {

					if (fxOptionTrade == null) {
						fxOptionTrade = new FXOptionTrade.Builder().creationTime(TradeSQL.getCreationTime(results))
								.build();
					}

					TradeSQL.setTradeCommonFields(fxOptionTrade, results);
					fxOptionTrade.setStyle(VanillaOptionTrade.Style.valueOf(results.getString(STYLE_FIELD.getName())));
					fxOptionTrade.setType(OptionTrade.Type.valueOf(results.getString(TYPE_FIELD.getName())));
					fxOptionTrade.setSettlementType(
							OptionTrade.SettlementType.valueOf(results.getString(SETTLEMENT_TYPE_FIELD.getName())));
					fxOptionTrade.setSettlementDateOffset(results.getInt(SETTLEMENT_DATE_OFFSET_FIELD.getName()));
					fxOptionTrade.setStrike(results.getBigDecimal(STRIKE_FIELD.getName()));
					fxOptionTrade.setMaturityDate(results.getDate(MATURITY_DATE_FIELD.getName()).toLocalDate());
					Date exerciseDate = results.getDate(EXERCISE_DATE_FIELD.getName());
					if (exerciseDate != null) {
						fxOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
					}

					// Building the underlying
					FXTrade underlying = FXTradeSQL.getTradeById(results.getLong(UNDERLYING_TRADE_ID_FIELD.getName()),
							true);
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
			if ((rs.getLong(VANILLA_OPTION_TRADE_ID_FIELD.getName()) == 0)
					|| (rs.getLong("underlying_fxspot_trade_id") == 0)) {
				return null;
			}

			fxOptionTrade = new FXOptionTrade.Builder().creationTime(TradeSQL.getCreationTime(rs)).build();
			fxOptionTrade.setStyle(VanillaOptionTrade.Style.valueOf(rs.getString(STYLE_FIELD.getName())));
			fxOptionTrade.setType(OptionTrade.Type.valueOf(rs.getString(TYPE_FIELD.getName())));
			fxOptionTrade.setSettlementType(
					OptionTrade.SettlementType.valueOf(rs.getString(SETTLEMENT_TYPE_FIELD.getName())));
			fxOptionTrade.setSettlementDateOffset(rs.getInt(SETTLEMENT_DATE_OFFSET_FIELD.getName()));
			fxOptionTrade.setStrike(rs.getBigDecimal(STRIKE_FIELD.getName()));
			fxOptionTrade.setMaturityDate(rs.getDate("option_maturity_date").toLocalDate());
			Date exerciseDate = rs.getDate(EXERCISE_DATE_FIELD.getName());
			if (exerciseDate != null) {
				fxOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
			}
			// Commmon fields
			TradeSQL.setTradeCommonFields(fxOptionTrade, rs);

			// Building the underlying
			java.sql.Timestamp undCreationTime = rs.getTimestamp("und_fxspot_creation_time");
			FXTrade.Builder undBuilder = new FXTrade.Builder();
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
			java.sql.Date undSettlementDate = rs.getDate("und_fxspot_settlement_date");
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
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		trade.setId(tradeId);
		return tradeId;
	}
}