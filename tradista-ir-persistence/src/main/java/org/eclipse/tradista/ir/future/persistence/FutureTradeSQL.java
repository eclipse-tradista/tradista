package org.eclipse.tradista.ir.future.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUANTITY;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.BOOK_ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.PRODUCT_ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_DATE_FIELD;
import static org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL.IRFORWARD_TRADE_ID_FIELD;
import static org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL.IRFORWARD_TRADE_TABLE;
import static org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL.MATURITY_DATE_FIELD;
import static org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL.TRADE_AND_IRFORWARD_TRADE_INNER_JOIN;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.ir.future.model.FutureTrade;

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

public class FutureTradeSQL {

	public static final Field FUTURE_TRADE_ID_FIELD = new Field("FUTURE_TRADE_ID");
	public static final Field QUANTITY_FIELD = new Field(QUANTITY);

	private static final Field[] FUTURE_TRADE_FIELDS = { FUTURE_TRADE_ID_FIELD, QUANTITY_FIELD };

	private static final Field[] FUTURE_TRADE_FIELDS_FOR_INSERT = { QUANTITY_FIELD, FUTURE_TRADE_ID_FIELD };

	private static final Field[] FUTURE_TRADE_FIELDS_FOR_UPDATE = { QUANTITY_FIELD };

	public static final Table FUTURE_TRADE_TABLE = new Table("FUTURE_TRADE", FUTURE_TRADE_FIELDS);

	public static final Join IRFORWARD_TRADE_AND_FUTURE_TRADE_INNER_JOIN = Join.innerEq(IRFORWARD_TRADE_TABLE,
			IRFORWARD_TRADE_ID_FIELD, FUTURE_TRADE_ID_FIELD);

	private static final Field[] IRFORWARD_FOR_FUTURE_FIELDS_FOR_INSERT = { MATURITY_DATE_FIELD,
			IRFORWARD_TRADE_ID_FIELD };

	private static final Field[] IRFORWARD_FOR_FUTURE_FIELDS_FOR_UPDATE = { MATURITY_DATE_FIELD };

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(FUTURE_TRADE_TABLE,
			IRFORWARD_TRADE_AND_FUTURE_TRADE_INNER_JOIN, TRADE_AND_IRFORWARD_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, FUTURE_TRADE_TABLE, FUTURE_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, FUTURE_TRADE_ID_FIELD, FUTURE_TRADE_TABLE,
				FUTURE_TRADE_FIELDS_FOR_UPDATE);
	}

	public static PreparedStatement getIRForwardInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, IRFORWARD_TRADE_TABLE,
				IRFORWARD_FOR_FUTURE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getIRForwardUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, IRFORWARD_TRADE_ID_FIELD, IRFORWARD_TRADE_TABLE,
				IRFORWARD_FOR_FUTURE_FIELDS_FOR_UPDATE);
	}

	public static FutureTrade getTradeById(long id) {

		FutureTrade futureTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, FUTURE_TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (futureTrade == null) {
						futureTrade = FutureTrade.of(TradeSQL.getCreationTime(results));
					}

					TradeSQL.setTradeCommonFields(futureTrade, results);
					java.sql.Date maturityDate = results.getDate(MATURITY_DATE_FIELD.getName());
					if (maturityDate != null) {
						futureTrade.setMaturityDate(maturityDate.toLocalDate());
					}
					futureTrade.setProduct(FutureSQL.getFutureById(results.getLong(PRODUCT_ID_FIELD.getName())));
					futureTrade.setQuantity(results.getBigDecimal(QUANTITY_FIELD.getName()));
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return futureTrade;
	}

	public static FutureTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		FutureTrade futureTrade = null;
		try {
			if (rs.getLong("future_trade_id") == 0) {
				return null;
			}

			futureTrade = FutureTrade.of(TradeSQL.getCreationTime(rs));
			java.sql.Date maturityDate = rs.getDate("irforward_maturity_date");
			if (maturityDate != null) {
				futureTrade.setMaturityDate(maturityDate.toLocalDate());
			}
			futureTrade.setProduct(FutureSQL.getFutureById(rs.getLong("product_id")));
			futureTrade.setQuantity(rs.getBigDecimal("future_quantity"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(futureTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return futureTrade;
	}

	public static long saveFutureTrade(FutureTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveIRForwardTrade = (trade.getId() == 0) ? getIRForwardInsertStatement(con)
						: getIRForwardUpdateStatement(con);
				PreparedStatement stmtSaveFutureTrade = (trade.getId() == 0) ? getInsertStatement(con)
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

			if (trade.getMaturityDate() != null) {
				stmtSaveIRForwardTrade.setDate(1, java.sql.Date.valueOf(trade.getMaturityDate()));
			} else {
				stmtSaveIRForwardTrade.setNull(1, Types.DATE);
			}
			stmtSaveIRForwardTrade.setLong(2, tradeId);
			stmtSaveIRForwardTrade.executeUpdate();

			stmtSaveFutureTrade.setBigDecimal(1, trade.getQuantity());
			stmtSaveFutureTrade.setLong(2, tradeId);
			stmtSaveFutureTrade.executeUpdate();

		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		trade.setId(tradeId);
		return tradeId;
	}

	public static List<FutureTrade> getFutureTradesBeforeTradeDateByFutureAndBookIds(LocalDate date, long futureId,
			long bookId) {

		List<FutureTrade> futureTrades = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addFilter(query, TRADE_DATE_FIELD, date, false);
		if (futureId > 0) {
			TradistaDBUtil.addFilter(query, PRODUCT_ID_FIELD, futureId);
		}
		if (bookId > 0) {
			TradistaDBUtil.addFilter(query, BOOK_ID_FIELD, bookId);
		}

		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetTradesBeforeTradeDate = con.createStatement()) {
			try (ResultSet results = stmtGetTradesBeforeTradeDate.executeQuery(query.toString())) {
				while (results.next()) {
					if (futureTrades == null) {
						futureTrades = new ArrayList<>();
					}
					FutureTrade futureTrade = FutureTrade.of(TradeSQL.getCreationTime(results));

					TradeSQL.setTradeCommonFields(futureTrade, results);
					java.sql.Date maturityDate = results.getDate(MATURITY_DATE_FIELD.getName());
					if (maturityDate != null) {
						futureTrade.setMaturityDate(maturityDate.toLocalDate());
					}
					futureTrade.setProduct(FutureSQL.getFutureById(results.getLong(PRODUCT_ID_FIELD.getName())));
					futureTrade.setQuantity(results.getBigDecimal(QUANTITY_FIELD.getName()));

					futureTrades.add(futureTrade);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return futureTrades;

	}
}