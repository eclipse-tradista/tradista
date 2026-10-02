package org.eclipse.tradista.security.equityoption.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MATURITY_DATE;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.PRODUCT_ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_TABLE;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.persistence.EquitySQL;
import org.eclipse.tradista.security.equity.persistence.EquityTradeSQL;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;

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

public class EquityOptionTradeSQL {

	// VANILLA_OPTION_TRADE table and fields
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

	private static final Field[] VANILLA_OPTION_TRADE_FIELDS_FOR_INSERT = { STYLE_FIELD, TYPE_FIELD, STRIKE_FIELD,
			MATURITY_DATE_FIELD, EXERCISE_DATE_FIELD, UNDERLYING_TRADE_ID_FIELD, SETTLEMENT_TYPE_FIELD,
			SETTLEMENT_DATE_OFFSET_FIELD, QUANTITY_FIELD, VANILLA_OPTION_TRADE_ID_FIELD };

	private static final Field[] VANILLA_OPTION_TRADE_FIELDS_FOR_UPDATE = { STYLE_FIELD, TYPE_FIELD, STRIKE_FIELD,
			MATURITY_DATE_FIELD, EXERCISE_DATE_FIELD, UNDERLYING_TRADE_ID_FIELD, SETTLEMENT_TYPE_FIELD,
			SETTLEMENT_DATE_OFFSET_FIELD, QUANTITY_FIELD };

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

	public static EquityOptionTrade getTradeById(long id) {
		EquityOptionTrade equityOptionTrade = null;

		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, VANILLA_OPTION_TRADE_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (equityOptionTrade == null) {
						equityOptionTrade = new EquityOptionTrade.Builder()
								.creationTime(TradeSQL.getCreationTime(results)).build();
					}

					TradeSQL.setTradeCommonFields(equityOptionTrade, results);
					equityOptionTrade
							.setStyle(VanillaOptionTrade.Style.valueOf(results.getString(STYLE_FIELD.getName())));
					equityOptionTrade.setType(OptionTrade.Type.valueOf(results.getString(TYPE_FIELD.getName())));
					equityOptionTrade.setStrike(results.getBigDecimal(STRIKE_FIELD.getName()));
					long productId = results.getLong(PRODUCT_ID_FIELD.getName());
					if (productId != 0) {
						equityOptionTrade.setEquityOption(EquityOptionSQL.getEquityOptionById(productId));
					}
					equityOptionTrade.setSettlementType(
							OptionTrade.SettlementType.valueOf(results.getString(SETTLEMENT_TYPE_FIELD.getName())));
					equityOptionTrade.setSettlementDateOffset(results.getInt(SETTLEMENT_DATE_OFFSET_FIELD.getName()));
					equityOptionTrade.setMaturityDate(results.getDate(MATURITY_DATE_FIELD.getName()).toLocalDate());
					Date exerciseDate = results.getDate(EXERCISE_DATE_FIELD.getName());
					if (exerciseDate != null) {
						equityOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
					}
					equityOptionTrade.setQuantity(results.getBigDecimal(QUANTITY_FIELD.getName()));

					// Building the underlying
					EquityTrade underlying = EquityTradeSQL
							.getTradeById(results.getLong(UNDERLYING_TRADE_ID_FIELD.getName()), true);
					equityOptionTrade.setUnderlying(underlying);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return equityOptionTrade;
	}

	public static long saveEquityOptionTrade(EquityOptionTrade trade) {
		long tradeId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveEquityOptionTrade = (trade.getId() == 0) ? getInsertStatement(con)
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
			long underlyingId = EquityTradeSQL.saveEquityTrade(trade.getUnderlying());

			// Parameters follow VANILLA_OPTION_TRADE_FIELDS_FOR_INSERT order:
			// STYLE, TYPE, STRIKE, MATURITY_DATE, EXERCISE_DATE, UNDERLYING_TRADE_ID,
			// SETTLEMENT_TYPE, SETTLEMENT_DATE_OFFSET, QUANTITY, VANILLA_OPTION_TRADE_ID
			stmtSaveEquityOptionTrade.setString(1, trade.getStyle().name());
			stmtSaveEquityOptionTrade.setString(2, trade.getType().name());
			stmtSaveEquityOptionTrade.setBigDecimal(3, trade.getStrike());
			stmtSaveEquityOptionTrade.setDate(4, java.sql.Date.valueOf(trade.getMaturityDate()));
			if (trade.getExerciseDate() != null) {
				stmtSaveEquityOptionTrade.setDate(5, java.sql.Date.valueOf(trade.getExerciseDate()));
			} else {
				stmtSaveEquityOptionTrade.setNull(5, Types.DATE);
			}
			stmtSaveEquityOptionTrade.setLong(6, underlyingId);
			stmtSaveEquityOptionTrade.setString(7, trade.getSettlementType().name());
			stmtSaveEquityOptionTrade.setInt(8, trade.getSettlementDateOffset());
			stmtSaveEquityOptionTrade.setBigDecimal(9, trade.getQuantity());
			stmtSaveEquityOptionTrade.setLong(10, tradeId);
			stmtSaveEquityOptionTrade.executeUpdate();
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		trade.setId(tradeId);
		return tradeId;
	}

	public static EquityOptionTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		EquityOptionTrade equityOptionTrade = null;
		try {
			if ((rs.getLong(VANILLA_OPTION_TRADE_ID_FIELD.getName()) == 0)
					|| (rs.getLong("underlying_equity_trade_id") == 0)) {
				return null;
			}
			equityOptionTrade = new EquityOptionTrade.Builder().creationTime(TradeSQL.getCreationTime(rs)).build();
			equityOptionTrade.setStyle(VanillaOptionTrade.Style.valueOf((rs.getString(STYLE_FIELD.getName()))));
			equityOptionTrade.setType(OptionTrade.Type.valueOf(rs.getString(TYPE_FIELD.getName())));
			equityOptionTrade.setStrike(rs.getBigDecimal(STRIKE_FIELD.getName()));
			long productId = rs.getLong(PRODUCT_ID_FIELD.getName());
			if (productId != 0) {
				equityOptionTrade.setEquityOption(EquityOptionSQL.getEquityOptionById(productId));
			}
			equityOptionTrade.setSettlementType(
					OptionTrade.SettlementType.valueOf(rs.getString(SETTLEMENT_TYPE_FIELD.getName())));
			equityOptionTrade.setSettlementDateOffset(rs.getInt(SETTLEMENT_DATE_OFFSET_FIELD.getName()));
			equityOptionTrade.setMaturityDate(rs.getDate("option_maturity_date").toLocalDate());
			Date exerciseDate = rs.getDate(EXERCISE_DATE_FIELD.getName());
			if (exerciseDate != null) {
				equityOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
			}
			equityOptionTrade.setQuantity(rs.getBigDecimal("option_quantity"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(equityOptionTrade, rs);

			// Building the underlying
			java.sql.Timestamp undCreationTime = rs.getTimestamp("UND_EQUITY_creation_time");
			EquityTrade.Builder undBuilder = new EquityTrade.Builder();
			if (undCreationTime != null) {
				undBuilder.creationTime(undCreationTime.toInstant());
			}
			EquityTrade underlying = undBuilder.build();
			underlying.setId(rs.getLong("UNDERLYING_EQUITY_TRADE_ID"));
			underlying.setProduct(EquitySQL.getEquityById(rs.getLong("UND_EQUITY_PRODUCT_ID")));
			underlying.setAmount(rs.getBigDecimal("UND_EQUITY_AMOUNT"));
			underlying.setBook(BookSQL.getBookById(rs.getLong("book_id")));
			underlying.setBuySell(rs.getBoolean("UND_EQUITY_buy_sell"));
			underlying.setCounterparty(LegalEntitySQL.getLegalEntityById(rs.getLong("UND_EQUITY_counterparty_id")));
			underlying.setQuantity(rs.getBigDecimal("UNDERLYING_EQUITY_QUANTITY"));
			Date undSettleDate = rs.getDate("und_equity_settlement_date");
			if (undSettleDate != null) {
				underlying.setSettlementDate(undSettleDate.toLocalDate());
			}
			Date undTradeDate = rs.getDate("und_equity_trade_date");
			if (undTradeDate != null) {
				underlying.setTradeDate(undTradeDate.toLocalDate());
			}

			equityOptionTrade.setUnderlying(underlying);

		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return equityOptionTrade;
	}

	public static List<EquityOptionTrade> getEquityOptionTradesBeforeTradeDateByEquityOptionAndBookIds(
			LocalDate tradeDate, long equityOptionId, long bookId) {
		List<EquityOptionTrade> equityOptionTrades = null;

		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addFilter(query, TradeSQL.TRADE_DATE_FIELD, tradeDate, false);

		if (equityOptionId > 0) {
			TradistaDBUtil.addFilter(query, TradeSQL.PRODUCT_ID_FIELD, equityOptionId);
		}
		if (bookId > 0) {
			TradistaDBUtil.addFilter(query, TradeSQL.BOOK_ID_FIELD, bookId);
		}

		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetTradesBeforeTradeDateByEquityOptionAndBookIds = con.createStatement();
				ResultSet results = stmtGetTradesBeforeTradeDateByEquityOptionAndBookIds
						.executeQuery(query.toString())) {
			while (results.next()) {
				if (equityOptionTrades == null) {
					equityOptionTrades = new ArrayList<>();
				}

				EquityOptionTrade equityOptionTrade = new EquityOptionTrade.Builder()
						.creationTime(TradeSQL.getCreationTime(results)).build();
				TradeSQL.setTradeCommonFields(equityOptionTrade, results);
				equityOptionTrade.setStyle(VanillaOptionTrade.Style.valueOf(results.getString(STYLE_FIELD.getName())));
				equityOptionTrade.setType(OptionTrade.Type.valueOf(results.getString(TYPE_FIELD.getName())));
				equityOptionTrade.setStrike(results.getBigDecimal(STRIKE_FIELD.getName()));
				long productId = results.getLong(PRODUCT_ID_FIELD.getName());
				if (productId != 0) {
					equityOptionTrade.setEquityOption(EquityOptionSQL.getEquityOptionById(productId));
				}
				equityOptionTrade.setSettlementType(
						OptionTrade.SettlementType.valueOf(results.getString(SETTLEMENT_TYPE_FIELD.getName())));
				equityOptionTrade.setSettlementDateOffset(results.getInt(SETTLEMENT_DATE_OFFSET_FIELD.getName()));
				equityOptionTrade.setMaturityDate(results.getDate(MATURITY_DATE_FIELD.getName()).toLocalDate());
				Date exerciseDate = results.getDate(EXERCISE_DATE_FIELD.getName());
				if (exerciseDate != null) {
					equityOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
				}
				equityOptionTrade.setQuantity(results.getBigDecimal(QUANTITY_FIELD.getName()));

				// Building the underlying
				EquityTrade underlying = EquityTradeSQL
						.getTradeById(results.getLong(UNDERLYING_TRADE_ID_FIELD.getName()), true);
				equityOptionTrade.setUnderlying(underlying);

				equityOptionTrades.add(equityOptionTrade);
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return equityOptionTrades;
	}

}