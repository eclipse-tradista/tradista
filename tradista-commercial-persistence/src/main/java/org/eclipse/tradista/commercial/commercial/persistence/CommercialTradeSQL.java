package org.eclipse.tradista.commercial.commercial.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.AMOUNT;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.SETTLEMENT_DATE;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_TABLE;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.commercial.commercial.model.CommercialTrade.CommercialCashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.springframework.util.CollectionUtils;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public class CommercialTradeSQL {

	public static final Field COMMERCIAL_TRADE_ID_FIELD = new Field("COMMERCIAL_TRADE_ID");
	public static final Field DOCUMENT_REFERENCE_FIELD = new Field("DOCUMENT_REFERENCE");

	private static final Field[] COMMERCIAL_TRADE_FIELDS = { COMMERCIAL_TRADE_ID_FIELD, DOCUMENT_REFERENCE_FIELD };

	private static final Field[] COMMERCIAL_TRADE_FIELDS_FOR_INSERT = { DOCUMENT_REFERENCE_FIELD,
			COMMERCIAL_TRADE_ID_FIELD };

	private static final Field[] COMMERCIAL_TRADE_FIELDS_FOR_UPDATE = { DOCUMENT_REFERENCE_FIELD };

	public static final Table COMMERCIAL_TRADE_TABLE = new Table("COMMERCIAL_TRADE", COMMERCIAL_TRADE_FIELDS);

	public static final Field CASHFLOW_COMMERCIAL_TRADE_ID_FIELD = new Field("COMMERCIAL_TRADE_ID");
	public static final Field SETTLEMENT_DATE_FIELD = new Field(SETTLEMENT_DATE);
	public static final Field AMOUNT_FIELD = new Field(AMOUNT);

	private static final Field[] COMMERCIAL_CASHFLOW_FIELDS = { CASHFLOW_COMMERCIAL_TRADE_ID_FIELD,
			SETTLEMENT_DATE_FIELD, AMOUNT_FIELD };

	private static final Field[] COMMERCIAL_CASHFLOW_FIELDS_FOR_INSERT = { CASHFLOW_COMMERCIAL_TRADE_ID_FIELD,
			SETTLEMENT_DATE_FIELD, AMOUNT_FIELD };

	public static final Table COMMERCIAL_CASHFLOW_TABLE = new Table("COMMERCIAL_CASHFLOW", COMMERCIAL_CASHFLOW_FIELDS);

	private static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, COMMERCIAL_TRADE_TABLE,
				COMMERCIAL_TRADE_FIELDS_FOR_INSERT);
	}

	private static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, COMMERCIAL_TRADE_ID_FIELD, COMMERCIAL_TRADE_TABLE,
				COMMERCIAL_TRADE_FIELDS_FOR_UPDATE);
	}

	public static long saveTrade(CommercialTrade trade) throws TradistaBusinessException {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveCommercialTrade = (trade.getId() == 0) ? getInsertStatement(con)
						: getUpdateStatement(con)) {
			boolean isNewTrade = (trade.getId() == 0);
			if (!isNewTrade) {
				tradeId = trade.getId();
			}
			TradeSQL.setPreparedStatementCommonFields(trade, stmtSaveTrade);
			stmtSaveTrade.executeUpdate();

			if (isNewTrade) {
				try (ResultSet generatedKeys = stmtSaveTrade.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						tradeId = generatedKeys.getLong(1);
						trade.setId(tradeId);
					} else {
						throw new SQLException("Creating trade failed, no ID obtained.");
					}
				}
			} else {
				try (PreparedStatement stmtDeleteItems = TradistaDBUtil.buildDeletePreparedStatement(con,
						COMMERCIAL_CASHFLOW_TABLE, CASHFLOW_COMMERCIAL_TRADE_ID_FIELD)) {
					stmtDeleteItems.setLong(1, tradeId);
					stmtDeleteItems.executeUpdate();
				}
			}

			stmtSaveCommercialTrade.setString(1, trade.getDocumentReference());
			stmtSaveCommercialTrade.setLong(2, tradeId);
			stmtSaveCommercialTrade.executeUpdate();

			if (!CollectionUtils.isEmpty(trade.getItems())) {
				try (PreparedStatement stmtSaveItem = TradistaDBUtil.buildInsertPreparedStatement(con,
						COMMERCIAL_CASHFLOW_TABLE, COMMERCIAL_CASHFLOW_FIELDS_FOR_INSERT)) {
					for (CommercialCashFlow cf : trade.getItems()) {
						stmtSaveItem.clearParameters();
						stmtSaveItem.setLong(1, tradeId);
						stmtSaveItem.setDate(2, Date.valueOf(cf.getSettlementDate()));
						stmtSaveItem.setBigDecimal(3, cf.getAmount());
						stmtSaveItem.addBatch();
					}
					stmtSaveItem.executeBatch();
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return tradeId;
	}

	public static CommercialTrade getTradeById(long id) {
		CommercialTrade trade = null;
		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(COMMERCIAL_TRADE_TABLE,
				Join.innerEq(TRADE_TABLE, ID_FIELD, COMMERCIAL_TRADE_ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sqlQuery, COMMERCIAL_TRADE_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTrade = con.prepareStatement(sqlQuery.toString())) {
			stmtGetTrade.setLong(1, id);
			try (ResultSet results = stmtGetTrade.executeQuery()) {
				if (results.next()) {
					trade = CommercialTrade.create();
					TradeSQL.setTradeCommonFields(trade, results);
					trade.setDocumentReference(results.getString(DOCUMENT_REFERENCE_FIELD.getName()));
					trade.setItems(getItemsByTradeId(con, id));
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return trade;
	}

	public static CommercialTrade getTrade(ResultSet rs) {
		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}
		CommercialTrade trade = null;
		try {
			long tradeId = rs.getLong(COMMERCIAL_TRADE_ID_FIELD.getName());
			if (tradeId == 0) {
				return null;
			}
			trade = CommercialTrade.create();
			TradeSQL.setTradeCommonFields(trade, rs);
			trade.setDocumentReference(rs.getString(DOCUMENT_REFERENCE_FIELD.getName()));
			try (Connection con = TradistaDB.getConnection()) {
				trade.setItems(getItemsByTradeId(con, tradeId));
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return trade;
	}

	private static List<CommercialCashFlow> getItemsByTradeId(Connection con, long tradeId) throws SQLException {
		List<CommercialCashFlow> items = new ArrayList<>();
		StringBuilder sqlQuery = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(COMMERCIAL_CASHFLOW_FIELDS, COMMERCIAL_CASHFLOW_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlQuery, CASHFLOW_COMMERCIAL_TRADE_ID_FIELD);
		sqlQuery.append(" ORDER BY ").append(SETTLEMENT_DATE_FIELD.getName());
		try (PreparedStatement stmtGetItems = con.prepareStatement(sqlQuery.toString())) {
			stmtGetItems.setLong(1, tradeId);
			try (ResultSet rs = stmtGetItems.executeQuery()) {
				while (rs.next()) {
					CommercialCashFlow item = new CommercialCashFlow();
					item.setSettlementDate(rs.getDate(SETTLEMENT_DATE_FIELD.getName()).toLocalDate());
					item.setAmount(rs.getBigDecimal(AMOUNT_FIELD.getName()));
					items.add(item);
				}
			}
		}
		return items;
	}

}