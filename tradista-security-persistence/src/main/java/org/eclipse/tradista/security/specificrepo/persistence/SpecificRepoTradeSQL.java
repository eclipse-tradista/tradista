package org.eclipse.tradista.security.specificrepo.persistence;

import static org.eclipse.tradista.core.trade.persistence.TradeSQL.PRODUCT_ID_FIELD;
import static org.eclipse.tradista.security.gcrepo.persistence.GCRepoTradeSQL.GCREPO_TRADE_ID_FIELD;
import static org.eclipse.tradista.security.gcrepo.persistence.GCRepoTradeSQL.GCREPO_TRADE_TABLE;
import static org.eclipse.tradista.security.repo.persistence.RepoTradeSQL.TRADE_AND_REPO_TRADE_INNER_JOIN;
import static org.eclipse.tradista.security.repo.persistence.RepoTradeSQL.PARTIAL_TERMINATION_AND_REPO_TRADE_LEFT_OUTER_JOIN;
import static org.eclipse.tradista.security.repo.persistence.RepoTradeSQL.REPO_TRADE_ID_FIELD;
import static org.eclipse.tradista.security.repo.persistence.RepoTradeSQL.REPO_TRADE_TABLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.security.bond.persistence.BondSQL;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.equity.persistence.EquitySQL;
import org.eclipse.tradista.security.repo.persistence.RepoTradeSQL;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public class SpecificRepoTradeSQL {

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(REPO_TRADE_TABLE,
			TRADE_AND_REPO_TRADE_INNER_JOIN, PARTIAL_TERMINATION_AND_REPO_TRADE_LEFT_OUTER_JOIN);

	public static SpecificRepoTrade getTradeById(long id) {
		SpecificRepoTrade specificRepoTrade = null;
		// Query inner joining trade and repo trade tables then left outer joining
		// partial termination table
		StringBuilder query = new StringBuilder(SQL_QUERY);
		// Adding a parameterized filter on repo_trade_id field
		TradistaDBUtil.addParameterizedFilter(query, REPO_TRADE_ID_FIELD);
		// Excluding trades referenced in the gc_repo table
		TradistaDBUtil.addQueryFilter(query, REPO_TRADE_ID_FIELD, TradistaDBUtil
				.buildSelectQuery(new Field[] { GCREPO_TRADE_ID_FIELD }, GCREPO_TRADE_TABLE, (Join[]) null), true);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (specificRepoTrade == null) {
						specificRepoTrade = new SpecificRepoTrade();
					}
					TradeSQL.setTradeCommonFields(specificRepoTrade, results);
					RepoTradeSQL.setRepoTradeCommonFields(specificRepoTrade, results);
					long securityId = results.getLong(PRODUCT_ID_FIELD.getName());
					Security security = BondSQL.getBondById(securityId);
					if (security == null) {
						security = EquitySQL.getEquityById(securityId);
					}
					specificRepoTrade.setSecurity(security);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return specificRepoTrade;
	}

	public static long saveSpecificRepoTrade(SpecificRepoTrade trade) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveRepoTrade = (trade.getId() == 0) ? RepoTradeSQL.getInsertStatement(con)
						: RepoTradeSQL.getUpdateStatement(con);) {

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
			}
			RepoTradeSQL.setPreparedStatementCommonFields(trade, stmtSaveRepoTrade);
			stmtSaveRepoTrade.executeUpdate();

		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return trade.getId();
	}

	public static SpecificRepoTrade getTrade(ResultSet rs) {

		SpecificRepoTrade specificRepoTrade = null;
		try {
			specificRepoTrade = RepoTradeSQL.fillTrade(rs, new SpecificRepoTrade(),
					rs.getLong(REPO_TRADE_ID_FIELD.getName()));
			if (specificRepoTrade != null) {
				long securityId = rs.getLong(PRODUCT_ID_FIELD.getName());
				Security security = BondSQL.getBondById(securityId);
				if (security == null) {
					security = EquitySQL.getEquityById(securityId);
				}
				specificRepoTrade.setSecurity(security);
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return specificRepoTrade;
	}

}