package org.eclipse.tradista.fx.fxndf.persistence;

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
import org.eclipse.tradista.fx.fxndf.model.FXNDFTrade;

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

public class FXNDFTradeSQL {

	public static final Field FXNDF_TRADE_ID_FIELD = new Field("FXNDF_TRADE_ID");
	public static final Field NON_DELIVERABLE_CURRENCY_ID_FIELD = new Field("NON_DELIVERABLE_CURRENCY_ID");
	public static final Field NDF_RATE_FIELD = new Field("NDF_RATE");

	private static final Field[] FXNDF_TRADE_FIELDS = { FXNDF_TRADE_ID_FIELD, NON_DELIVERABLE_CURRENCY_ID_FIELD,
			NDF_RATE_FIELD };

	private static final Field[] FXNDF_TRADE_FIELDS_FOR_INSERT = { NON_DELIVERABLE_CURRENCY_ID_FIELD, NDF_RATE_FIELD,
			FXNDF_TRADE_ID_FIELD };

	private static final Field[] FXNDF_TRADE_FIELDS_FOR_UPDATE = { NON_DELIVERABLE_CURRENCY_ID_FIELD, NDF_RATE_FIELD };

	public static final Table FXNDF_TRADE_TABLE = new Table("FXNDF_TRADE", FXNDF_TRADE_FIELDS);

	public static final Join TRADE_AND_FXNDF_TRADE_INNER_JOIN = Join.innerEq(TRADE_TABLE, ID_FIELD,
			FXNDF_TRADE_ID_FIELD);

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(FXNDF_TRADE_TABLE,
			TRADE_AND_FXNDF_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, FXNDF_TRADE_TABLE, FXNDF_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, FXNDF_TRADE_ID_FIELD, FXNDF_TRADE_TABLE,
				FXNDF_TRADE_FIELDS_FOR_UPDATE);
	}

	public static FXNDFTrade getTradeById(long id) {

		FXNDFTrade fxndfTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, FXNDF_TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {

				while (results.next()) {

					if (fxndfTrade == null) {
						fxndfTrade = new FXNDFTrade();
					}

					TradeSQL.setTradeCommonFields(fxndfTrade, results);
					fxndfTrade.setNonDeliverableCurrency(
							CurrencySQL.getCurrencyById(results.getLong(NON_DELIVERABLE_CURRENCY_ID_FIELD.getName())));
					fxndfTrade.setNdfRate(results.getBigDecimal(NDF_RATE_FIELD.getName()));
				}
			}
		} catch (SQLException | TradistaBusinessException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return fxndfTrade;
	}

	public static FXNDFTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		FXNDFTrade fxndfTrade = null;
		try {
			if (rs.getLong("fxndf_trade_id") == 0) {
				return null;
			}

			fxndfTrade = new FXNDFTrade();
			fxndfTrade
					.setNonDeliverableCurrency(CurrencySQL.getCurrencyById(rs.getLong("non_deliverable_currency_id")));
			fxndfTrade.setNdfRate(rs.getBigDecimal("ndf_rate"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(fxndfTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return fxndfTrade;
	}

	public static long saveFXNDFTrade(FXNDFTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveFXNDFTrade = (trade.getId() == 0) ? getInsertStatement(con)
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

			stmtSaveFXNDFTrade.setLong(1, trade.getNonDeliverableCurrency().getId());
			stmtSaveFXNDFTrade.setBigDecimal(2, trade.getNdfRate());
			stmtSaveFXNDFTrade.setLong(3, tradeId);
			stmtSaveFXNDFTrade.executeUpdate();
		} catch (SQLException | TradistaBusinessException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}

}