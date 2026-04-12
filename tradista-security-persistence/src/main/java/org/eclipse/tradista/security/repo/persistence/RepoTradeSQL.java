package org.eclipse.tradista.security.repo.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.END_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.INDEX_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.INDEX_OFFSET;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.INDEX_TENOR;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NOTICE_PERIOD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.security.repo.model.RepoTrade;

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

public final class RepoTradeSQL {

	public static final Field REPO_TRADE_ID_FIELD = new Field("REPO_TRADE_ID");
	public static final Field REPO_RATE_FIELD = new Field("REPO_RATE");
	public static final Field MARGIN_RATE_FIELD = new Field("MARGIN_RATE");
	public static final Field INDEX_ID_FIELD = new Field(INDEX_ID);
	public static final Field INDEX_TENOR_FIELD = new Field(INDEX_TENOR);
	public static final Field INDEX_OFFSET_FIELD = new Field(INDEX_OFFSET);
	public static final Field END_DATE_FIELD = new Field(END_DATE, "REPO_END_DATE");
	public static final Field RIGHT_OF_SUBSTITUTION_FIELD = new Field("RIGHT_OF_SUBSTITUTION");
	public static final Field RIGHT_OF_REUSE_FIELD = new Field("RIGHT_OF_REUSE");
	public static final Field CROSS_CURRENCY_COLLATERAL_FIELD = new Field("CROSS_CURRENCY_COLLATERAL");
	public static final Field TERMINABLE_ON_DEMAND_FIELD = new Field("TERMINABLE_ON_DEMAND");
	public static final Field NOTICE_PERIOD_FIELD = new Field(NOTICE_PERIOD);

	public static final Field TRADE_ID_FIELD = new Field("TRADE_ID");
	public static final Field DATE_FIELD = new Field(DATE);
	public static final Field REDUCTION_FIELD = new Field("REDUCTION");

	private static final Field[] REPO_TRADE_FIELDS = { REPO_TRADE_ID_FIELD, REPO_RATE_FIELD, MARGIN_RATE_FIELD,
			INDEX_ID_FIELD, INDEX_TENOR_FIELD, INDEX_OFFSET_FIELD, END_DATE_FIELD, RIGHT_OF_SUBSTITUTION_FIELD,
			RIGHT_OF_REUSE_FIELD, CROSS_CURRENCY_COLLATERAL_FIELD, TERMINABLE_ON_DEMAND_FIELD, NOTICE_PERIOD_FIELD };

	private static final Field[] REPO_TRADE_FIELDS_FOR_INSERT = { REPO_RATE_FIELD, MARGIN_RATE_FIELD, INDEX_ID_FIELD,
			INDEX_TENOR_FIELD, INDEX_OFFSET_FIELD, END_DATE_FIELD, RIGHT_OF_SUBSTITUTION_FIELD, RIGHT_OF_REUSE_FIELD,
			CROSS_CURRENCY_COLLATERAL_FIELD, TERMINABLE_ON_DEMAND_FIELD, NOTICE_PERIOD_FIELD, REPO_TRADE_ID_FIELD };

	private static final Field[] REPO_TRADE_FIELDS_FOR_UPDATE = { REPO_RATE_FIELD, MARGIN_RATE_FIELD, INDEX_ID_FIELD,
			INDEX_TENOR_FIELD, INDEX_OFFSET_FIELD, END_DATE_FIELD, RIGHT_OF_SUBSTITUTION_FIELD, RIGHT_OF_REUSE_FIELD,
			CROSS_CURRENCY_COLLATERAL_FIELD, TERMINABLE_ON_DEMAND_FIELD, NOTICE_PERIOD_FIELD };

	public static final Table REPO_TRADE_TABLE = new Table("REPO_TRADE", REPO_TRADE_FIELDS);

	private static final Field[] PARTIAL_TERMINATION_FIELDS = { TRADE_ID_FIELD, DATE_FIELD, REDUCTION_FIELD };

	private static final Table PARTIAL_TERMINATION_TABLE = new Table("PARTIAL_TERMINATION", PARTIAL_TERMINATION_FIELDS);

	public static final Join TRADE_AND_REPO_TRADE_INNER_JOIN = Join.inner(ID_FIELD, REPO_TRADE_ID_FIELD);

	public static final Join PARTIAL_TERMINATION_AND_REPO_TRADE_LEFT_OUTER_JOIN = Join.leftOuter(TRADE_ID_FIELD,
			REPO_TRADE_ID_FIELD);

	private static final String PARTIAL_TERMINATION_SQL_QUERY = TradistaDBUtil
			.buildSelectQuery(PARTIAL_TERMINATION_TABLE);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, REPO_TRADE_TABLE, REPO_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, REPO_TRADE_ID_FIELD, REPO_TRADE_TABLE,
				REPO_TRADE_FIELDS_FOR_UPDATE);
	}

	public static Map<LocalDate, BigDecimal> getPartialTerminations(long repoTradeId) {
		Map<LocalDate, BigDecimal> partialTerminations = null;
		StringBuilder select = new StringBuilder(PARTIAL_TERMINATION_SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(select, TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(select.toString())) {
			stmtGetTradeById.setLong(1, repoTradeId);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (partialTerminations == null) {
						partialTerminations = new HashMap<>();
					}
					partialTerminations.put(results.getDate(DATE_FIELD.getName()).toLocalDate(),
							results.getBigDecimal(REDUCTION_FIELD.getName()));

				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return partialTerminations;
	}

	public static void setPreparedStatementCommonFields(RepoTrade trade, PreparedStatement stmt)
			throws TradistaBusinessException {
		if (stmt == null) {
			throw new TradistaTechnicalException("the prepared statement cannot be null.");
		}

		if (trade == null) {
			throw new TradistaBusinessException("The trade cannot be null.");
		}

		try {
			stmt.setBigDecimal(1, trade.getRepoRate());
			stmt.setBigDecimal(2, trade.getMarginRate());
			if (!trade.isFixedRepoRate()) {
				stmt.setLong(3, trade.getIndex().getId());
				stmt.setString(4, trade.getIndexTenor().name());
			} else {
				stmt.setNull(3, Types.BIGINT);
				stmt.setNull(4, Types.VARCHAR);
			}
			stmt.setBigDecimal(5, trade.getIndexOffset());
			LocalDate endDate = trade.getEndDate();
			if (endDate != null) {
				stmt.setDate(6, java.sql.Date.valueOf(endDate));
			} else {
				stmt.setNull(6, Types.DATE);
			}
			stmt.setBoolean(7, trade.isRightOfSubstitution());
			stmt.setBoolean(8, trade.isRightOfReuse());
			stmt.setBoolean(9, trade.isCrossCurrencyCollateral());
			stmt.setBoolean(10, trade.isTerminableOnDemand());
			stmt.setShort(11, trade.getNoticePeriod());
			stmt.setLong(12, trade.getId());
		}

		catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static void setRepoTradeCommonFields(RepoTrade trade, ResultSet rs) throws TradistaBusinessException {
		if (rs == null) {
			throw new TradistaTechnicalException("the ResultSet cannot be null.");
		}

		if (trade == null) {
			throw new TradistaBusinessException("The trade cannot be null.");
		}

		try {
			trade.setCrossCurrencyCollateral(rs.getBoolean(CROSS_CURRENCY_COLLATERAL_FIELD.getName()));

			java.sql.Date partialTerminationDate = rs.getDate(DATE_FIELD.getName());
			if (partialTerminationDate != null) {
				trade.addParTialTermination(partialTerminationDate.toLocalDate(),
						rs.getBigDecimal(REDUCTION_FIELD.getName()));
			}

			java.sql.Date endDate = rs.getDate(END_DATE_FIELD.getAlias());
			if (endDate != null) {
				trade.setEndDate(endDate.toLocalDate());
			}
			long indexId = rs.getLong(INDEX_ID_FIELD.getName());
			if (indexId != 0) {
				trade.setIndex(IndexSQL.getIndexById(indexId));
				trade.setIndexTenor(Tenor.valueOf(rs.getString(INDEX_TENOR_FIELD.getName())));
			}
			trade.setIndexOffset(rs.getBigDecimal(INDEX_OFFSET_FIELD.getName()));
			trade.setMarginRate(rs.getBigDecimal(MARGIN_RATE_FIELD.getName()));
			trade.setNoticePeriod(rs.getShort(NOTICE_PERIOD_FIELD.getName()));
			trade.setRepoRate(rs.getBigDecimal(REPO_RATE_FIELD.getName()));
			trade.setRightOfReuse(rs.getBoolean(RIGHT_OF_REUSE_FIELD.getName()));
			trade.setRightOfSubstitution(rs.getBoolean(RIGHT_OF_SUBSTITUTION_FIELD.getName()));
			trade.setTerminableOnDemand(rs.getBoolean(TERMINABLE_ON_DEMAND_FIELD.getName()));

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static void savePartialTermination(Connection con, RepoTrade trade) throws TradistaBusinessException {

		if (con == null) {
			throw new TradistaTechnicalException("the connection cannot be null.");
		}

		if (trade == null) {
			throw new TradistaBusinessException("The trade cannot be null.");
		}

		try {

			PreparedStatement stmtDeletePartialTerminations = TradistaDBUtil.buildDeletePreparedStatement(con,
					PARTIAL_TERMINATION_TABLE, TRADE_ID_FIELD);
			PreparedStatement stmtSavePartialTermination = TradistaDBUtil.buildInsertPreparedStatement(con,
					PARTIAL_TERMINATION_TABLE, PARTIAL_TERMINATION_FIELDS);
			if (trade.getId() != 0) {
				stmtDeletePartialTerminations.setLong(1, trade.getId());
				stmtDeletePartialTerminations.executeUpdate();
			}

			// We don't want to save partial terminations for new trades.
			if (trade.getId() != 0) {
				if (trade.getPartialTerminations() != null && !trade.getPartialTerminations().isEmpty()) {
					for (Map.Entry<LocalDate, BigDecimal> partialTerminationEntry : trade.getPartialTerminations()
							.entrySet()) {
						stmtSavePartialTermination.clearParameters();
						stmtSavePartialTermination.setLong(1, trade.getId());
						stmtSavePartialTermination.setDate(2, java.sql.Date.valueOf(partialTerminationEntry.getKey()));
						stmtSavePartialTermination.setBigDecimal(3, partialTerminationEntry.getValue());
						stmtSavePartialTermination.addBatch();
					}
				}
				stmtSavePartialTermination.executeBatch();
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static <X extends RepoTrade> X fillTrade(ResultSet rs, X trade, long tradeId) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		try {
			if (tradeId == 0) {
				return null;
			}
			RepoTradeSQL.setRepoTradeCommonFields(trade, rs);
			// Commmon fields
			TradeSQL.setTradeCommonFields(trade, rs);
		} catch (TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return trade;

	}

}