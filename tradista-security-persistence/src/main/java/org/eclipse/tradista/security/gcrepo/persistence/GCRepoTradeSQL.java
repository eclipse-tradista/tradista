package org.eclipse.tradista.security.gcrepo.persistence;

import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.security.repo.persistence.RepoTradeSQL.PARTIAL_TERMINATION_AND_REPO_TRADE_LEFT_OUTER_JOIN;
import static org.eclipse.tradista.security.repo.persistence.RepoTradeSQL.REPO_TRADE_ID_FIELD;

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
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.repo.persistence.RepoTradeSQL;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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

public class GCRepoTradeSQL {

	private static final String GCREPO_TRADE_ID = "GCREPO_TRADE_ID";

	public static final Field GCREPO_TRADE_ID_FIELD = new Field(GCREPO_TRADE_ID);
	private static final Field GCBASKET_ID_FIELD = new Field("GCBASKET_ID");

	private static final Field[] GCREPO_TRADE_FIELDS = { GCREPO_TRADE_ID_FIELD, GCBASKET_ID_FIELD };

	public static final Table GCREPO_TRADE_TABLE = new Table("GCREPO_TRADE", GCREPO_TRADE_FIELDS);

	private static final Join TRADE_AND_GCREPO_TRADE_INNER_JOIN = Join.inner(ID_FIELD, GCREPO_TRADE_ID_FIELD);

	private static final Join REPO_TRADE_AND_GCREPO_TRADE_INNER_JOIN = Join.inner(REPO_TRADE_ID_FIELD,
			GCREPO_TRADE_ID_FIELD);

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(GCREPO_TRADE_TABLE,
			TRADE_AND_GCREPO_TRADE_INNER_JOIN, REPO_TRADE_AND_GCREPO_TRADE_INNER_JOIN,
			PARTIAL_TERMINATION_AND_REPO_TRADE_LEFT_OUTER_JOIN);

	public static GCRepoTrade getTradeById(long id) {
		GCRepoTrade gcRepoTrade = null;
		// Query joining trade and repo_trade tables
		StringBuilder query = new StringBuilder(SQL_QUERY);
		// Adding a parameterized filter on repo_trade_id field
		TradistaDBUtil.addParameterizedFilter(query, GCREPO_TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (gcRepoTrade == null) {
						gcRepoTrade = new GCRepoTrade();
					}
					TradeSQL.setTradeCommonFields(gcRepoTrade, results);
					RepoTradeSQL.setRepoTradeCommonFields(gcRepoTrade, results);

					gcRepoTrade.setGcBasket(GCBasketSQL.getGCBasketById(results.getLong(GCBASKET_ID_FIELD.getName())));

				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return gcRepoTrade;
	}

	public static long saveGCRepoTrade(GCRepoTrade trade) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveRepoTrade = (trade.getId() == 0) ? RepoTradeSQL.getInsertStatement(con)
						: RepoTradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveGCRepoTrade = (trade.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, GCREPO_TRADE_TABLE, GCREPO_TRADE_FIELDS)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, GCREPO_TRADE_ID_FIELD, GCREPO_TRADE_TABLE,
								GCBASKET_ID_FIELD)) {
			TradeSQL.setPreparedStatementCommonFields(trade, stmtSaveTrade);
			RepoTradeSQL.savePartialTermination(con, trade);

			stmtSaveTrade.executeUpdate();

			if (trade.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveTrade.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						trade.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("Creating trade failed, no generated key obtained.");
					}
				}
				stmtSaveGCRepoTrade.setLong(1, trade.getId());
				stmtSaveGCRepoTrade.setLong(2, trade.getGcBasket().getId());
			} else {
				stmtSaveGCRepoTrade.setLong(1, trade.getGcBasket().getId());
				stmtSaveGCRepoTrade.setLong(2, trade.getId());
			}

			RepoTradeSQL.setPreparedStatementCommonFields(trade, stmtSaveRepoTrade);
			stmtSaveRepoTrade.executeUpdate();
			stmtSaveGCRepoTrade.executeUpdate();

		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return trade.getId();
	}

	public static GCRepoTrade getTrade(ResultSet rs) {

		GCRepoTrade gcRepoTrade = null;
		try {
			gcRepoTrade = RepoTradeSQL.fillTrade(rs, new GCRepoTrade(), rs.getLong(GCREPO_TRADE_ID_FIELD.getName()));
			if (gcRepoTrade != null) {
				gcRepoTrade.setGcBasket(GCBasketSQL.getGCBasketById(rs.getLong(GCBASKET_ID_FIELD.getName())));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return gcRepoTrade;
	}

}