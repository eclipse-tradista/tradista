package org.eclipse.tradista.security.specificrepo.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.security.bond.persistence.BondSQL;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.equity.persistence.EquitySQL;
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

	public static SpecificRepoTrade getTradeById(long id) {
		SpecificRepoTrade specificRepoTrade = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(
						"SELECT * FROM SPECIFICREPO_TRADE INNER JOIN TRADE ON TRADE.ID = SPECIFICREPO_TRADE.SPECIFICREPO_TRADE_ID INNER JOIN REPO_TRADE ON REPO_TRADE.REPO_TRADE_ID = SPECIFICREPO_TRADE.SPECIFICREPO_TRADE_ID"
								+ " LEFT OUTER JOIN PARTIAL_TERMINATION ON PARTIAL_TERMINATION.TRADE_ID = SPECIFICREPO_TRADE.SPECIFICREPO_TRADE_ID"
								+ " WHERE SPECIFICREPO_TRADE.SPECIFICREPO_TRADE_ID = ?")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (specificRepoTrade == null) {
						specificRepoTrade = new SpecificRepoTrade();
					}
					java.sql.Date partialTerminationDate = results.getDate("date");
					if (partialTerminationDate != null) {
						specificRepoTrade.addParTialTermination(partialTerminationDate.toLocalDate(),
								results.getBigDecimal("reduction"));
					}
					TradeSQL.setTradeCommonFields(specificRepoTrade, results);
					specificRepoTrade.setCrossCurrencyCollateral(results.getBoolean("cross_currency_collateral"));
					long securityId = results.getLong("security_id");
					Security security = BondSQL.getBondById(securityId);
					if (security == null) {
						security = EquitySQL.getEquityById(securityId);
					}
					specificRepoTrade.setSecurity(security);
					java.sql.Date endDate = results.getDate("end_date");
					if (endDate != null) {
						specificRepoTrade.setEndDate(endDate.toLocalDate());
					}
					long indexId = results.getLong("index_id");
					if (indexId != 0) {
						specificRepoTrade.setIndex(IndexSQL.getIndexById(indexId));
						specificRepoTrade.setIndexTenor(Tenor.valueOf(results.getString("index_tenor")));
					}
					specificRepoTrade.setIndexOffset(results.getBigDecimal("index_offset"));
					specificRepoTrade.setMarginRate(results.getBigDecimal("margin_rate"));
					specificRepoTrade.setNoticePeriod(results.getShort("notice_period"));
					specificRepoTrade.setRepoRate(results.getBigDecimal("repo_rate"));
					specificRepoTrade.setRightOfReuse(results.getBoolean("right_of_reuse"));
					specificRepoTrade.setRightOfSubstitution(results.getBoolean("right_of_substitution"));
					specificRepoTrade.setTerminableOnDemand(results.getBoolean("terminable_on_demand"));
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}
		return specificRepoTrade;
	}

	public static long saveSpecificRepoTrade(SpecificRepoTrade trade) {
		long tradeId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, PRODUCT_ID, COUNTERPARTY_ID, AMOUNT, BOOK_ID, SETTLEMENT_DATE, CURRENCY_ID, STATUS_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, AMOUNT=?, BOOK_ID=?, SETTLEMENT_DATE=?, CURRENCY_ID=?, STATUS_ID=? WHERE ID = ?");
				PreparedStatement stmtSaveRepoTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO REPO_TRADE(CROSS_CURRENCY_COLLATERAL, END_DATE, INDEX_ID, INDEX_TENOR, INDEX_OFFSET, MARGIN_RATE, NOTICE_PERIOD, REPO_RATE, RIGHT_OF_REUSE, RIGHT_OF_SUBSTITUTION, TERMINABLE_ON_DEMAND, REPO_TRADE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE REPO_TRADE SET CROSS_CURRENCY_COLLATERAL = ?, END_DATE = ?, INDEX_ID = ?, INDEX_TENOR = ?, INDEX_OFFSET = ?, MARGIN_RATE = ?, NOTICE_PERIOD = ?, REPO_RATE = ?, RIGHT_OF_REUSE = ?, RIGHT_OF_SUBSTITUTION = ?, TERMINABLE_ON_DEMAND = ? WHERE REPO_TRADE_ID = ?");
				PreparedStatement stmtSaveSpecificRepoTrade = (trade.getId() == 0)
						? con.prepareStatement(
								"INSERT INTO SPECIFICREPO_TRADE(SECURITY_ID, SPECIFICREPO_TRADE_ID) VALUES (?, ?) ")
						: con.prepareStatement(
								"UPDATE SPECIFICREPO_TRADE SET SECURITY_ID = ? WHERE SPECIFICREPO_TRADE_ID = ?");
				PreparedStatement stmtDeletePartialTerminations = con
						.prepareStatement("DELETE FROM PARTIAL_TERMINATION WHERE TRADE_ID = ?");
				PreparedStatement stmtSavePartialTermination = con.prepareStatement(
						"INSERT INTO PARTIAL_TERMINATION(TRADE_ID, DATE, REDUCTION) VALUES(?, ?, ?)")) {
			boolean isBuy = trade.isBuy();
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(10, java.sql.Date.valueOf(LocalDate.now()));
			} else {
				stmtSaveTrade.setLong(10, trade.getId());
				stmtDeletePartialTerminations.setLong(1, trade.getId());
				stmtDeletePartialTerminations.executeUpdate();
			}
			stmtSaveTrade.setBoolean(1, isBuy);
			stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			stmtSaveTrade.setNull(3, java.sql.Types.BIGINT);
			stmtSaveTrade.setLong(4, trade.getCounterparty().getId());
			stmtSaveTrade.setBigDecimal(5, trade.getAmount());
			stmtSaveTrade.setLong(6, trade.getBook().getId());
			stmtSaveTrade.setDate(7, java.sql.Date.valueOf(trade.getSettlementDate()));
			stmtSaveTrade.setLong(8, trade.getCurrency().getId());
			stmtSaveTrade.setLong(9, trade.getStatus().getId());
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
			stmtSaveRepoTrade.setBoolean(1, trade.isCrossCurrencyCollateral());
			LocalDate endDate = trade.getEndDate();
			if (endDate != null) {
				stmtSaveRepoTrade.setDate(2, java.sql.Date.valueOf(endDate));
			} else {
				stmtSaveRepoTrade.setNull(2, Types.DATE);
			}
			if (!trade.isFixedRepoRate()) {
				stmtSaveRepoTrade.setLong(3, trade.getIndex().getId());
				stmtSaveRepoTrade.setString(4, trade.getIndexTenor().name());
			} else {
				stmtSaveRepoTrade.setNull(3, Types.BIGINT);
				stmtSaveRepoTrade.setNull(4, Types.VARCHAR);
			}
			stmtSaveRepoTrade.setBigDecimal(5, trade.getIndexOffset());
			stmtSaveRepoTrade.setBigDecimal(6, trade.getMarginRate());
			stmtSaveRepoTrade.setShort(7, trade.getNoticePeriod());
			stmtSaveRepoTrade.setBigDecimal(8, trade.getRepoRate());
			stmtSaveRepoTrade.setBoolean(9, trade.isRightOfReuse());
			stmtSaveRepoTrade.setBoolean(10, trade.isRightOfSubstitution());
			stmtSaveRepoTrade.setBoolean(11, trade.isTerminableOnDemand());
			stmtSaveRepoTrade.setLong(12, tradeId);
			stmtSaveRepoTrade.executeUpdate();

			stmtSaveSpecificRepoTrade.setLong(1, trade.getSecurity().getId());
			stmtSaveSpecificRepoTrade.setLong(2, tradeId);
			stmtSaveSpecificRepoTrade.executeUpdate();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}

	public static SpecificRepoTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		SpecificRepoTrade specificRepoTrade = null;
		try {
			if (rs.getLong("specificrepo_trade_id") == 0) {
				return null;
			}
			specificRepoTrade = new SpecificRepoTrade();
			specificRepoTrade.setCrossCurrencyCollateral(rs.getBoolean("cross_currency_collateral"));
			long securityId = rs.getLong("security_id");
			Security security = BondSQL.getBondById(securityId);
			if (security == null) {
				security = EquitySQL.getEquityById(securityId);
			}
			specificRepoTrade.setSecurity(security);
			java.sql.Date endDate = rs.getDate("specificrepo_end_date");
			if (endDate != null) {
				specificRepoTrade.setEndDate(endDate.toLocalDate());
			}
			long indexId = rs.getLong("index_id");
			if (indexId != 0) {
				specificRepoTrade.setIndex(IndexSQL.getIndexById(indexId));
				specificRepoTrade.setIndexTenor(Tenor.valueOf(rs.getString("index_tenor")));
			}
			specificRepoTrade.setIndexOffset(rs.getBigDecimal("index_offset"));
			specificRepoTrade.setMarginRate(rs.getBigDecimal("margin_rate"));
			specificRepoTrade.setNoticePeriod(rs.getShort("notice_period"));
			specificRepoTrade.setRepoRate(rs.getBigDecimal("repo_rate"));
			specificRepoTrade.setRightOfReuse(rs.getBoolean("right_of_reuse"));
			specificRepoTrade.setRightOfSubstitution(rs.getBoolean("right_of_substitution"));
			specificRepoTrade.setTerminableOnDemand(rs.getBoolean("terminable_on_demand"));
			specificRepoTrade.setPartialTerminations(
					SpecificRepoTradeSQL.getPartialTerminations(rs.getLong("specificrepo_trade_id")));

			// Commmon fields
			TradeSQL.setTradeCommonFields(specificRepoTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return specificRepoTrade;
	}

	private static Map<LocalDate, BigDecimal> getPartialTerminations(long specificRepoTradeId) {
		Map<LocalDate, BigDecimal> partialTerminations = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con
						.prepareStatement("SELECT * FROM PARTIAL_TERMINATION WHERE TRADE_ID = ?")) {
			stmtGetTradeById.setLong(1, specificRepoTradeId);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (partialTerminations == null) {
						partialTerminations = new HashMap<>();
					}
					partialTerminations.put(results.getDate("date").toLocalDate(), results.getBigDecimal("reduction"));

				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return partialTerminations;
	}

}