package org.eclipse.tradista.fx.fx.persistence;

import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_DATE_FIELD;
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
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fx.service.FXTradeBusinessDelegate;

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

public class FXTradeSQL {

	public static final Field FXSPOT_TRADE_ID_FIELD = new Field("FXSPOT_TRADE_ID");
	public static final Field CURRENCY_ONE_ID_FIELD = new Field("CURRENCY_ONE_ID");
	public static final Field AMOUNT_ONE_FIELD = new Field("AMOUNT_ONE");

	private static final Field[] FXSPOT_TRADE_FIELDS = { FXSPOT_TRADE_ID_FIELD, CURRENCY_ONE_ID_FIELD,
			AMOUNT_ONE_FIELD };

	private static final Field[] FXSPOT_TRADE_FIELDS_FOR_INSERT = { CURRENCY_ONE_ID_FIELD, AMOUNT_ONE_FIELD,
			FXSPOT_TRADE_ID_FIELD };

	private static final Field[] FXSPOT_TRADE_FIELDS_FOR_UPDATE = { CURRENCY_ONE_ID_FIELD, AMOUNT_ONE_FIELD };

	public static final Table FXSPOT_TRADE_TABLE = new Table("FXSPOT_TRADE", FXSPOT_TRADE_FIELDS);

	public static final Join TRADE_AND_FXSPOT_TRADE_INNER_JOIN = Join.innerEq(TRADE_TABLE, ID_FIELD,
			FXSPOT_TRADE_ID_FIELD);

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(FXSPOT_TRADE_TABLE,
			TRADE_AND_FXSPOT_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, FXSPOT_TRADE_TABLE, FXSPOT_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, FXSPOT_TRADE_ID_FIELD, FXSPOT_TRADE_TABLE,
				FXSPOT_TRADE_FIELDS_FOR_UPDATE);
	}

	public static FXTrade getTradeById(long id, boolean includeUnderlying) {
		FXTrade fxspotTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, FXSPOT_TRADE_ID_FIELD);
		if (!includeUnderlying) {
			TradistaDBUtil.addIsNotNullFilter(query, TRADE_DATE_FIELD);
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			FXTradeBusinessDelegate fxTradeBusinessDelegate = new FXTradeBusinessDelegate();
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {

				while (results.next()) {

					if (fxspotTrade == null) {
						fxspotTrade = FXTrade.of(TradeSQL.getCreationTime(results));
					}

					TradeSQL.setTradeCommonFields(fxspotTrade, results);
					fxspotTrade.setCurrencyOne(
							CurrencySQL.getCurrencyById(results.getLong(CURRENCY_ONE_ID_FIELD.getName())));
					fxspotTrade.setAmountOne(results.getBigDecimal(AMOUNT_ONE_FIELD.getName()));
				}
			}
			if (fxspotTrade != null) {
				fxTradeBusinessDelegate.determinateType(fxspotTrade);
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return fxspotTrade;
	}

	public static FXTrade getTrade(ResultSet rs) {

		FXTrade fxspotTrade = null;
		FXTradeBusinessDelegate fxTradeBusinessDelegate = new FXTradeBusinessDelegate();
		try {

			// We ensure that the deal is a FX Spot.
			if (rs.getLong("fxspot_trade_id") == 0) {
				return null;
			}

			fxspotTrade = FXTrade.of(TradeSQL.getCreationTime(rs));

			fxspotTrade.setCurrencyOne(CurrencySQL.getCurrencyById(rs.getLong("fxspot_currency_one_id")));
			fxspotTrade.setAmountOne(rs.getBigDecimal("amount_one"));

			TradeSQL.setTradeCommonFields(fxspotTrade, rs);

			fxTradeBusinessDelegate.determinateType(fxspotTrade);

		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return fxspotTrade;
	}

	public static long saveFXTrade(FXTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveFXSpotTrade = (trade.getId() == 0) ? getInsertStatement(con)
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

			stmtSaveFXSpotTrade.setLong(1, trade.getCurrencyOne().getId());
			stmtSaveFXSpotTrade.setBigDecimal(2, trade.getAmountOne());
			stmtSaveFXSpotTrade.setLong(3, tradeId);
			stmtSaveFXSpotTrade.executeUpdate();
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		trade.setId(tradeId);
		return tradeId;
	}

}