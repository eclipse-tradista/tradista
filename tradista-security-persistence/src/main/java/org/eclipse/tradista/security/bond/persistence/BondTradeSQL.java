package org.eclipse.tradista.security.bond.persistence;

import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_TABLE;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.PRODUCT_ID_FIELD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.security.bond.model.BondTrade;

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

public class BondTradeSQL {

	private static final Field BOND_TRADE_ID_FIELD = new Field("BOND_TRADE_ID");
	private static final Field QUANTITY_FIELD = new Field(TradistaDBConstants.QUANTITY);

	private static final Field[] BOND_TRADE_FIELDS = { BOND_TRADE_ID_FIELD, QUANTITY_FIELD };

	private static final Field[] BOND_TRADE_FIELDS_FOR_INSERT = { QUANTITY_FIELD, BOND_TRADE_ID_FIELD };

	private static final Field[] BOND_TRADE_FIELDS_FOR_UPDATE = { QUANTITY_FIELD };

	private static final Table BOND_TRADE_TABLE = new Table("BOND_TRADE", BOND_TRADE_FIELDS);

	private static final Join TRADE_AND_BOND_TRADE_INNER_JOIN = Join.innerEq(TRADE_TABLE, ID_FIELD,
			BOND_TRADE_ID_FIELD);

	private static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(BOND_TRADE_TABLE,
			TRADE_AND_BOND_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, BOND_TRADE_TABLE, BOND_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, BOND_TRADE_ID_FIELD, BOND_TRADE_TABLE,
				BOND_TRADE_FIELDS_FOR_UPDATE);
	}

	public static BondTrade getTradeById(long id) {
		BondTrade bondTrade = null;

		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, BOND_TRADE_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (bondTrade == null) {
						bondTrade = new BondTrade();
					}
					TradeSQL.setTradeCommonFields(bondTrade, results);
					bondTrade.setProduct(BondSQL.getBondById(results.getLong(PRODUCT_ID_FIELD.getName())));
					bondTrade.setQuantity(results.getBigDecimal(QUANTITY_FIELD.getName()));
				}
			}
		} catch (SQLException | TradistaBusinessException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bondTrade;
	}

	public static long saveBondTrade(BondTrade trade) {
		long tradeId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveBondTrade = (trade.getId() == 0) ? getInsertStatement(con)
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
			stmtSaveBondTrade.setBigDecimal(1, trade.getQuantity());
			stmtSaveBondTrade.setLong(2, tradeId);
			stmtSaveBondTrade.executeUpdate();
		} catch (SQLException | TradistaBusinessException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}

	public static BondTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		BondTrade bondTrade = null;
		try {
			if (rs.getLong("bond_trade_id") == 0) {
				return null;
			}
			bondTrade = new BondTrade();
			bondTrade.setProduct(BondSQL.getBondById(rs.getLong(PRODUCT_ID_FIELD.getName())));
			bondTrade.setQuantity(rs.getBigDecimal("bond_quantity"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(bondTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return bondTrade;
	}

	public static List<BondTrade> getBondTradesBeforeTradeDateByBondAndBookIds(LocalDate date, long bondId,
			long bookId) {
		List<BondTrade> bondTrades = null;

		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addFilter(query, TradeSQL.TRADE_DATE_FIELD, date, false);

		if (bondId > 0) {
			TradistaDBUtil.addFilter(query, TradeSQL.PRODUCT_ID_FIELD, bondId);
		}
		if (bookId > 0) {
			TradistaDBUtil.addFilter(query, TradeSQL.BOOK_ID_FIELD, bookId);
		}

		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetTradesBeforeTradeDateByBondAndBookIds = con.createStatement();
				ResultSet results = stmtGetTradesBeforeTradeDateByBondAndBookIds.executeQuery(query.toString())) {
			while (results.next()) {
				if (bondTrades == null) {
					bondTrades = new ArrayList<>();
				}
				BondTrade bondTrade = new BondTrade();
				TradeSQL.setTradeCommonFields(bondTrade, results);
				bondTrade.setProduct(BondSQL.getBondById(results.getLong(PRODUCT_ID_FIELD.getName())));
				bondTrade.setQuantity(results.getBigDecimal(QUANTITY_FIELD.getName()));
				bondTrades.add(bondTrade);
			}
		} catch (SQLException | TradistaBusinessException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bondTrades;
	}

}