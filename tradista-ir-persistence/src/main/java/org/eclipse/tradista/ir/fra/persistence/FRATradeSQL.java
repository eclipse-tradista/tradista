package org.eclipse.tradista.ir.fra.persistence;

import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_TABLE;
import static org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL.DAY_COUNT_CONVENTION_ID_FIELD;
import static org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL.IRFORWARD_TRADE_ID_FIELD;
import static org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL.IRFORWARD_TRADE_TABLE;
import static org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL.MATURITY_DATE_FIELD;
import static org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL.REFERENCE_RATE_INDEX_ID_FIELD;
import static org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL.REFERENCE_RATE_INDEX_TENOR_FIELD;
import static org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL.TRADE_AND_IRFORWARD_TRADE_INNER_JOIN;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.daycountconvention.persistence.DayCountConventionSQL;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.ir.fra.model.FRATrade;

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

public class FRATradeSQL {

	public static final Field FRA_TRADE_ID_FIELD = new Field("FRA_TRADE_ID");
	public static final Field START_DATE_FIELD = new Field("START_DATE");
	public static final Field FIXED_RATE_FIELD = new Field("FIXED_RATE");

	private static final Field[] FRA_TRADE_FIELDS = { FRA_TRADE_ID_FIELD, START_DATE_FIELD, FIXED_RATE_FIELD };

	private static final Field[] FRA_TRADE_FIELDS_FOR_INSERT = { FIXED_RATE_FIELD, START_DATE_FIELD,
			FRA_TRADE_ID_FIELD };

	private static final Field[] FRA_TRADE_FIELDS_FOR_UPDATE = { FIXED_RATE_FIELD, START_DATE_FIELD };

	public static final Table FRA_TRADE_TABLE = new Table("FRA_TRADE", FRA_TRADE_FIELDS);

	public static final Join IRFORWARD_TRADE_AND_FRA_TRADE_INNER_JOIN = Join.innerEq(IRFORWARD_TRADE_TABLE,
			IRFORWARD_TRADE_ID_FIELD, FRA_TRADE_ID_FIELD);

	private static final Field[] IRFORWARD_FOR_FRA_FIELDS_FOR_INSERT = { MATURITY_DATE_FIELD,
			REFERENCE_RATE_INDEX_ID_FIELD, REFERENCE_RATE_INDEX_TENOR_FIELD, DAY_COUNT_CONVENTION_ID_FIELD,
			IRFORWARD_TRADE_ID_FIELD };

	private static final Field[] IRFORWARD_FOR_FRA_FIELDS_FOR_UPDATE = { MATURITY_DATE_FIELD,
			REFERENCE_RATE_INDEX_ID_FIELD, REFERENCE_RATE_INDEX_TENOR_FIELD, DAY_COUNT_CONVENTION_ID_FIELD };

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(FRA_TRADE_TABLE,
			IRFORWARD_TRADE_AND_FRA_TRADE_INNER_JOIN, TRADE_AND_IRFORWARD_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, FRA_TRADE_TABLE, FRA_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, FRA_TRADE_ID_FIELD, FRA_TRADE_TABLE,
				FRA_TRADE_FIELDS_FOR_UPDATE);
	}

	public static PreparedStatement getIRForwardInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, IRFORWARD_TRADE_TABLE,
				IRFORWARD_FOR_FRA_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getIRForwardUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, IRFORWARD_TRADE_ID_FIELD, IRFORWARD_TRADE_TABLE,
				IRFORWARD_FOR_FRA_FIELDS_FOR_UPDATE);
	}

	public static FRATrade getTradeById(long id) {
		FRATrade fraTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, FRA_TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (fraTrade == null) {
						fraTrade = new FRATrade();
					}

					TradeSQL.setTradeCommonFields(fraTrade, results);
					java.sql.Date maturityDate = results.getDate(MATURITY_DATE_FIELD.getName());
					if (maturityDate != null) {
						fraTrade.setMaturityDate(maturityDate.toLocalDate());
					}
					java.sql.Date startDate = results.getDate(START_DATE_FIELD.getName());
					if (startDate != null) {
						fraTrade.setStartDate(startDate.toLocalDate());
					}
					long indexId = results.getLong(REFERENCE_RATE_INDEX_ID_FIELD.getName());
					if (indexId != 0) {
						fraTrade.setReferenceRateIndex(IndexSQL.getIndexById(indexId));
					}
					String tenor = results.getString(REFERENCE_RATE_INDEX_TENOR_FIELD.getName());
					if (tenor != null) {
						fraTrade.setReferenceRateIndexTenor(Tenor.valueOf(tenor));
					}
					long dccId = results.getLong(DAY_COUNT_CONVENTION_ID_FIELD.getName());
					if (dccId != 0) {
						fraTrade.setDayCountConvention(DayCountConventionSQL.getDayCountConventionById(dccId));
					}
					fraTrade.setFixedRate(results.getBigDecimal(FIXED_RATE_FIELD.getName()));
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return fraTrade;
	}

	public static FRATrade getTrade(ResultSet rs) {

		FRATrade fraTrade = null;
		try {
			if (rs.getLong("fra_trade_id") == 0) {
				return null;
			}

			fraTrade = new FRATrade();

			java.sql.Date maturityDate = rs.getDate("irforward_maturity_date");
			if (maturityDate != null) {
				fraTrade.setMaturityDate(maturityDate.toLocalDate());
			}

			fraTrade.setReferenceRateIndex(IndexSQL.getIndexById(rs.getLong("irforward_reference_rate_index_id")));
			fraTrade.setReferenceRateIndexTenor(Tenor.valueOf(rs.getString("irforward_reference_rate_index_tenor")));
			fraTrade.setDayCountConvention(
					DayCountConventionSQL.getDayCountConventionById(rs.getLong("irforward_day_count_convention_id")));
			fraTrade.setFixedRate(rs.getBigDecimal("fra_fixed_rate"));
			fraTrade.setStartDate(rs.getDate("start_date").toLocalDate());

			// Commmon fields
			TradeSQL.setTradeCommonFields(fraTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return fraTrade;
	}

	public static long saveFRATrade(FRATrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveIRForwardTrade = (trade.getId() == 0) ? getIRForwardInsertStatement(con)
						: getIRForwardUpdateStatement(con);
				PreparedStatement stmtSaveFRATrade = (trade.getId() == 0) ? getInsertStatement(con)
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
			if (trade.getReferenceRateIndex() != null) {
				stmtSaveIRForwardTrade.setLong(2, trade.getReferenceRateIndex().getId());
			} else {
				stmtSaveIRForwardTrade.setNull(2, Types.BIGINT);
			}
			if (trade.getReferenceRateIndexTenor() != null) {
				stmtSaveIRForwardTrade.setString(3, trade.getReferenceRateIndexTenor().name());
			} else {
				stmtSaveIRForwardTrade.setNull(3, Types.VARCHAR);
			}
			if (trade.getDayCountConvention() != null) {
				stmtSaveIRForwardTrade.setLong(4, trade.getDayCountConvention().getId());
			} else {
				stmtSaveIRForwardTrade.setNull(4, Types.BIGINT);
			}
			stmtSaveIRForwardTrade.setLong(5, tradeId);
			stmtSaveIRForwardTrade.executeUpdate();

			stmtSaveFRATrade.setBigDecimal(1, trade.getFixedRate());
			if (trade.getStartDate() != null) {
				stmtSaveFRATrade.setDate(2, java.sql.Date.valueOf(trade.getStartDate()));
			} else {
				stmtSaveFRATrade.setNull(2, Types.DATE);
			}
			stmtSaveFRATrade.setLong(3, tradeId);
			stmtSaveFRATrade.executeUpdate();

		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}
		trade.setId(tradeId);
		return tradeId;
	}
}