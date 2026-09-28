package org.eclipse.tradista.fx.fxswap.persistence;

import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_TABLE;

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
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.fx.fxswap.model.FXSwapTrade;

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

public class FXSwapTradeSQL {

	public static final Field FXSWAP_TRADE_ID_FIELD = new Field("FXSWAP_TRADE_ID");
	public static final Field CURRENCY_ONE_ID_FIELD = new Field("CURRENCY_ONE_ID");
	public static final Field SETTLEMENT_DATE_FORWARD_FIELD = new Field("SETTLEMENT_DATE_FORWARD");
	public static final Field AMOUNT_ONE_FORWARD_FIELD = new Field("AMOUNT_ONE_FORWARD");
	public static final Field AMOUNT_ONE_SPOT_FIELD = new Field("AMOUNT_ONE_SPOT");
	public static final Field AMOUNT_TWO_FORWARD_FIELD = new Field("AMOUNT_TWO_FORWARD");

	private static final Field[] FXSWAP_TRADE_FIELDS = { FXSWAP_TRADE_ID_FIELD, CURRENCY_ONE_ID_FIELD,
			SETTLEMENT_DATE_FORWARD_FIELD, AMOUNT_ONE_FORWARD_FIELD, AMOUNT_ONE_SPOT_FIELD, AMOUNT_TWO_FORWARD_FIELD };

	private static final Field[] FXSWAP_TRADE_FIELDS_FOR_INSERT = { CURRENCY_ONE_ID_FIELD,
			SETTLEMENT_DATE_FORWARD_FIELD, AMOUNT_ONE_FORWARD_FIELD, AMOUNT_ONE_SPOT_FIELD, AMOUNT_TWO_FORWARD_FIELD,
			FXSWAP_TRADE_ID_FIELD };

	private static final Field[] FXSWAP_TRADE_FIELDS_FOR_UPDATE = { CURRENCY_ONE_ID_FIELD,
			SETTLEMENT_DATE_FORWARD_FIELD, AMOUNT_ONE_FORWARD_FIELD, AMOUNT_ONE_SPOT_FIELD, AMOUNT_TWO_FORWARD_FIELD };

	public static final Table FXSWAP_TRADE_TABLE = new Table("FXSWAP_TRADE", FXSWAP_TRADE_FIELDS);

	public static final Join TRADE_AND_FXSWAP_TRADE_INNER_JOIN = Join.innerEq(TRADE_TABLE, ID_FIELD,
			FXSWAP_TRADE_ID_FIELD);

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(FXSWAP_TRADE_TABLE,
			TRADE_AND_FXSWAP_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, FXSWAP_TRADE_TABLE, FXSWAP_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, FXSWAP_TRADE_ID_FIELD, FXSWAP_TRADE_TABLE,
				FXSWAP_TRADE_FIELDS_FOR_UPDATE);
	}

	public static FXSwapTrade getTradeById(long id) {

		FXSwapTrade fxswapTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, FXSWAP_TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {

					if (fxswapTrade == null) {
						fxswapTrade = new FXSwapTrade();
					}

					TradeSQL.setTradeCommonFields(fxswapTrade, results);
					fxswapTrade.setCurrencyOne(CurrencySQL.getCurrencyById(results.getLong(CURRENCY_ONE_ID_FIELD.getName())));
					fxswapTrade.setSettlementDateForward(
							results.getDate(SETTLEMENT_DATE_FORWARD_FIELD.getName()).toLocalDate());
					fxswapTrade.setAmountOneSpot(results.getBigDecimal(AMOUNT_ONE_SPOT_FIELD.getName()));
					fxswapTrade.setAmountOneForward(results.getBigDecimal(AMOUNT_ONE_FORWARD_FIELD.getName()));
					fxswapTrade.setAmountTwoForward(results.getBigDecimal(AMOUNT_TWO_FORWARD_FIELD.getName()));
				}
			}
		} catch (SQLException | TradistaBusinessException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return fxswapTrade;
	}

	public static FXSwapTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		FXSwapTrade fxswapTrade = null;
		try {
			if (rs.getLong("fxswap_trade_id") == 0) {
				return null;
			}

			fxswapTrade = new FXSwapTrade();
			TradeSQL.setTradeCommonFields(fxswapTrade, rs);
			fxswapTrade.setCurrencyOne(CurrencySQL.getCurrencyById(rs.getLong("fxswap_currency_one_id")));
			fxswapTrade.setSettlementDateForward(rs.getDate("settlement_date_forward").toLocalDate());
			fxswapTrade.setAmountOneSpot(rs.getBigDecimal("amount_one_spot"));
			fxswapTrade.setAmountOneForward(rs.getBigDecimal("amount_one_forward"));
			fxswapTrade.setAmountTwoForward(rs.getBigDecimal("amount_two_forward"));
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return fxswapTrade;
	}

	public static long saveFXSwapTrade(FXSwapTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveFXSwapTrade = (trade.getId() == 0) ? getInsertStatement(con)
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

			stmtSaveFXSwapTrade.setLong(1, trade.getCurrencyOne().getId());
			stmtSaveFXSwapTrade.setDate(2, java.sql.Date.valueOf(trade.getSettlementDateForward()));
			stmtSaveFXSwapTrade.setBigDecimal(3, trade.getAmountOneForward());
			stmtSaveFXSwapTrade.setBigDecimal(4, trade.getAmountOneSpot());
			stmtSaveFXSwapTrade.setBigDecimal(5, trade.getAmountTwoForward());
			stmtSaveFXSwapTrade.setLong(6, tradeId);
			stmtSaveFXSwapTrade.executeUpdate();
		} catch (SQLException | TradistaBusinessException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}
}