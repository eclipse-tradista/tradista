package org.eclipse.tradista.security.gcrepo.persistence;

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
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;

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

	public static GCRepoTrade getTradeById(long id) {
		GCRepoTrade gcRepoTrade = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(
						"SELECT * FROM GCREPO_TRADE INNER JOIN TRADE ON TRADE.ID = GCREPO_TRADE.GCREPO_TRADE_ID INNER JOIN REPO_TRADE ON REPO_TRADE.REPO_TRADE_ID = GCREPO_TRADE.GCREPO_TRADE_ID"
								+ " LEFT OUTER JOIN PARTIAL_TERMINATION ON PARTIAL_TERMINATION.TRADE_ID = GCREPO_TRADE.GCREPO_TRADE_ID"
								+ " WHERE GCREPO_TRADE.GCREPO_TRADE_ID = ?")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (gcRepoTrade == null) {
						gcRepoTrade = new GCRepoTrade();
					}
					java.sql.Date partialTerminationDate = results.getDate("date");
					if (partialTerminationDate != null) {
						gcRepoTrade.addParTialTermination(partialTerminationDate.toLocalDate(),
								results.getBigDecimal("reduction"));
					}
					TradeSQL.setTradeCommonFields(gcRepoTrade, results);
					gcRepoTrade.setCrossCurrencyCollateral(results.getBoolean("cross_currency_collateral"));
					gcRepoTrade.setGcBasket(GCBasketSQL.getGCBasketById(results.getLong("gcbasket_id")));
					java.sql.Date endDate = results.getDate("end_date");
					if (endDate != null) {
						gcRepoTrade.setEndDate(endDate.toLocalDate());
					}
					long indexId = results.getLong("index_id");
					if (indexId != 0) {
						gcRepoTrade.setIndex(IndexSQL.getIndexById(indexId));
						gcRepoTrade.setIndexTenor(Tenor.valueOf(results.getString("index_tenor")));
					}
					gcRepoTrade.setIndexOffset(results.getBigDecimal("index_offset"));
					gcRepoTrade.setMarginRate(results.getBigDecimal("margin_rate"));
					gcRepoTrade.setNoticePeriod(results.getShort("notice_period"));
					gcRepoTrade.setRepoRate(results.getBigDecimal("repo_rate"));
					gcRepoTrade.setRightOfReuse(results.getBoolean("right_of_reuse"));
					gcRepoTrade.setRightOfSubstitution(results.getBoolean("right_of_substitution"));
					gcRepoTrade.setTerminableOnDemand(results.getBoolean("terminable_on_demand"));
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}
		return gcRepoTrade;
	}

	public static long saveGCRepoTrade(GCRepoTrade trade) {
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
				PreparedStatement stmtSaveGCRepoTrade = (trade.getId() == 0)
						? con.prepareStatement("INSERT INTO GCREPO_TRADE(GCBASKET_ID, GCREPO_TRADE_ID) VALUES (?, ?) ")
						: con.prepareStatement("UPDATE GCREPO_TRADE SET GCBASKET_ID = ? WHERE GCREPO_TRADE_ID = ?");
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

			stmtSaveGCRepoTrade.setLong(1, trade.getGcBasket().getId());
			stmtSaveGCRepoTrade.setLong(2, tradeId);
			stmtSaveGCRepoTrade.executeUpdate();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}

	public static GCRepoTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		GCRepoTrade gcRepoTrade = null;
		try {
			if (rs.getLong("gcrepo_trade_id") == 0) {
				return null;
			}
			gcRepoTrade = new GCRepoTrade();
			gcRepoTrade.setCrossCurrencyCollateral(rs.getBoolean("cross_currency_collateral"));
			gcRepoTrade.setGcBasket(GCBasketSQL.getGCBasketById(rs.getLong("gcbasket_id")));
			java.sql.Date endDate = rs.getDate("gcrepo_end_date");
			if (endDate != null) {
				gcRepoTrade.setEndDate(endDate.toLocalDate());
			}
			long indexId = rs.getLong("index_id");
			if (indexId != 0) {
				gcRepoTrade.setIndex(IndexSQL.getIndexById(indexId));
				gcRepoTrade.setIndexTenor(Tenor.valueOf(rs.getString("index_tenor")));
			}
			gcRepoTrade.setIndexOffset(rs.getBigDecimal("index_offset"));
			gcRepoTrade.setMarginRate(rs.getBigDecimal("margin_rate"));
			gcRepoTrade.setNoticePeriod(rs.getShort("notice_period"));
			gcRepoTrade.setRepoRate(rs.getBigDecimal("repo_rate"));
			gcRepoTrade.setRightOfReuse(rs.getBoolean("right_of_reuse"));
			gcRepoTrade.setRightOfSubstitution(rs.getBoolean("right_of_substitution"));
			gcRepoTrade.setTerminableOnDemand(rs.getBoolean("terminable_on_demand"));
			gcRepoTrade.setPartialTerminations(GCRepoTradeSQL.getPartialTerminations(rs.getLong("gcrepo_trade_id")));

			// Commmon fields
			TradeSQL.setTradeCommonFields(gcRepoTrade, rs);
		} catch (SQLException | TradistaBusinessException e) {
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return gcRepoTrade;
	}

	private static Map<LocalDate, BigDecimal> getPartialTerminations(long gcRepoTradeId) {
		Map<LocalDate, BigDecimal> partialTerminations = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con
						.prepareStatement("SELECT * FROM PARTIAL_TERMINATION WHERE TRADE_ID = ?")) {
			stmtGetTradeById.setLong(1, gcRepoTradeId);
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