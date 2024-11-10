package org.eclipse.tradista.security.equityoption.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.persistence.EquitySQL;
import org.eclipse.tradista.security.equity.persistence.EquityTradeSQL;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;

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

public class EquityOptionTradeSQL {

	public static EquityOptionTrade getTradeById(long id) {
		EquityOptionTrade equityOptionTrade = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(
						"SELECT TRADE.*, VANILLA_OPTION_TRADE.*, EQUITY_TRADE.*,  UND_TRADE.PRODUCT_ID UND_PRODUCT_ID, UND_TRADE.AMOUNT UND_AMOUNT, UND_TRADE.SETTLEMENT_DATE UND_SETTLEMENT_DATE, UND_TRADE.TRADE_DATE UND_TRADE_DATE, EQUITY_TRADE.QUANTITY UND_EQUITY_QUANTITY FROM TRADE, TRADE UND_TRADE, VANILLA_OPTION_TRADE, EQUITY_TRADE WHERE "
								+ "TRADE.ID = VANILLA_OPTION_TRADE_ID AND VANILLA_OPTION_TRADE_ID = ? AND UNDERLYING_TRADE_ID = EQUITY_TRADE_ID AND EQUITY_TRADE_ID = UND_TRADE.ID")) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (equityOptionTrade == null) {
						equityOptionTrade = new EquityOptionTrade();
					}

					equityOptionTrade.setStyle(getStyle(results.getString("style")));
					equityOptionTrade.setType(OptionTrade.Type.valueOf(results.getString("type")));
					equityOptionTrade.setAmount(results.getBigDecimal("amount"));
					equityOptionTrade.setStrike(results.getBigDecimal("strike"));
					equityOptionTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					long productId = results.getLong("product_id");
					if (productId != 0) {
						equityOptionTrade.setEquityOption(EquityOptionSQL.getEquityOptionById(productId));
					}
					equityOptionTrade.setSettlementType(
							OptionTrade.SettlementType.valueOf(results.getString("settlement_type")));
					equityOptionTrade.setSettlementDateOffset(results.getInt("settlement_date_offset"));
					equityOptionTrade.setMaturityDate(results.getDate("maturity_date").toLocalDate());
					equityOptionTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					equityOptionTrade.setBuySell(results.getBoolean("buy_sell"));
					equityOptionTrade
							.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					equityOptionTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					equityOptionTrade.setId(results.getLong("VANILLA_OPTION_TRADE_ID"));
					equityOptionTrade.setTradeDate(results.getDate("trade_date").toLocalDate());
					Date exerciseDate = results.getDate("exercise_date");
					if (exerciseDate != null) {
						equityOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
					}
					equityOptionTrade.setQuantity(results.getBigDecimal("quantity"));
					equityOptionTrade.setSettlementDate(results.getDate("settlement_date").toLocalDate());

					// Building the underlying
					EquityTrade underlying = new EquityTrade();
					underlying.setId(results.getLong("UNDERLYING_TRADE_ID"));
					underlying.setProduct(EquitySQL.getEquityById(results.getLong("UND_PRODUCT_ID")));
					underlying.setAmount(results.getBigDecimal("UND_AMOUNT"));
					underlying.setBook(BookSQL.getBookById(results.getLong("book_id")));
					underlying.setBuySell(results.getBoolean("buy_sell"));
					underlying.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					underlying.setCreationDate(results.getDate("creation_date").toLocalDate());
					underlying.setQuantity(results.getBigDecimal("und_equity_quantity"));
					Date undSettleDate = results.getDate("und_settlement_date");
					if (undSettleDate != null) {
						underlying.setSettlementDate(undSettleDate.toLocalDate());
					}
					Date undTradeDate = results.getDate("und_trade_date");
					if (undTradeDate != null) {
						underlying.setTradeDate(undTradeDate.toLocalDate());
					}

					equityOptionTrade.setUnderlying(underlying);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptionTrade;
	}

	private static VanillaOptionTrade.Style getStyle(String name) {
		if (name.equals("EUROPEAN")) {
			return VanillaOptionTrade.Style.EUROPEAN;
		} else
			return VanillaOptionTrade.Style.AMERICAN;
	}

	public static long saveEquityOptionTrade(EquityOptionTrade trade) {
		long tradeId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADE(BUY_SELL, TRADE_DATE, SETTLEMENT_DATE, PRODUCT_ID, COUNTERPARTY_ID, AMOUNT, CURRENCY_ID, BOOK_ID, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADE SET BUY_SELL=?, TRADE_DATE=?, SETTLEMENT_DATE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, AMOUNT=?, CURRENCY_ID=?, BOOK_ID=? WHERE ID=?");
				PreparedStatement stmtSaveEquityOptionTrade = (trade.getId() == 0) ? con.prepareStatement(
						"INSERT INTO VANILLA_OPTION_TRADE(STYLE, TYPE, STRIKE, MATURITY_DATE, EXERCISE_DATE, UNDERLYING_TRADE_ID, SETTLEMENT_TYPE, SETTLEMENT_DATE_OFFSET, QUANTITY, VANILLA_OPTION_TRADE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE VANILLA_OPTION_TRADE SET STYLE = ?, TYPE = ?, STRIKE = ?, MATURITY_DATE = ?, EXERCISE_DATE = ?, UNDERLYING_TRADE_ID = ?, SETTLEMENT_TYPE = ?, SETTLEMENT_DATE_OFFSET = ?, QUANTITY = ? WHERE VANILLA_OPTION_TRADE_ID = ?")) {
			boolean isBuy = trade.isBuy();
			if (trade.getId() == 0) {
				stmtSaveTrade.setDate(9, java.sql.Date.valueOf(LocalDate.now()));
			} else {
				stmtSaveTrade.setLong(9, trade.getId());
			}
			stmtSaveTrade.setBoolean(1, isBuy);
			stmtSaveTrade.setDate(2, java.sql.Date.valueOf(trade.getTradeDate()));
			stmtSaveTrade.setDate(3, java.sql.Date.valueOf(trade.getSettlementDate()));
			if (trade.getProduct() == null) {
				stmtSaveTrade.setNull(4, java.sql.Types.BIGINT);
			} else {
				stmtSaveTrade.setLong(4, trade.getProductId());
			}
			stmtSaveTrade.setLong(5, trade.getCounterparty().getId());
			stmtSaveTrade.setBigDecimal(6, trade.getAmount());
			stmtSaveTrade.setLong(7, trade.getCurrency().getId());
			stmtSaveTrade.setLong(8, trade.getBook().getId());
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

			// Underlying saving
			long underlyingId = EquityTradeSQL.saveEquityTrade(trade.getUnderlying());
			stmtSaveEquityOptionTrade.setString(1, trade.getStyle().name());
			stmtSaveEquityOptionTrade.setString(2, trade.getType().name());
			stmtSaveEquityOptionTrade.setBigDecimal(3, trade.getStrike());
			stmtSaveEquityOptionTrade.setDate(4, java.sql.Date.valueOf(trade.getMaturityDate()));
			if (trade.getExerciseDate() != null) {
				stmtSaveEquityOptionTrade.setDate(5, java.sql.Date.valueOf(trade.getExerciseDate()));
			} else {
				stmtSaveEquityOptionTrade.setNull(5, Types.DATE);
			}
			stmtSaveEquityOptionTrade.setLong(6, underlyingId);
			stmtSaveEquityOptionTrade.setString(7, trade.getSettlementType().name());
			stmtSaveEquityOptionTrade.setInt(8, trade.getSettlementDateOffset());
			stmtSaveEquityOptionTrade.setBigDecimal(9, trade.getQuantity());
			stmtSaveEquityOptionTrade.setLong(10, tradeId);
			stmtSaveEquityOptionTrade.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		trade.setId(tradeId);
		return tradeId;
	}

	public static EquityOptionTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		EquityOptionTrade equityOptionTrade = null;
		try {
			if ((rs.getLong("vanilla_option_trade_id") == 0) || (rs.getLong("underlying_equity_trade_id") == 0)) {
				return null;
			}
			equityOptionTrade = new EquityOptionTrade();
			equityOptionTrade.setStyle(getStyle(rs.getString("style")));
			equityOptionTrade.setType(OptionTrade.Type.valueOf(rs.getString("type")));
			equityOptionTrade.setStrike(rs.getBigDecimal("strike"));
			long productId = rs.getLong("product_id");
			if (productId != 0) {
				equityOptionTrade.setEquityOption(EquityOptionSQL.getEquityOptionById(productId));
			}
			equityOptionTrade.setSettlementType(OptionTrade.SettlementType.valueOf(rs.getString("settlement_type")));
			equityOptionTrade.setSettlementDateOffset(rs.getInt("settlement_date_offset"));
			equityOptionTrade.setMaturityDate(rs.getDate("option_maturity_date").toLocalDate());
			Date exerciseDate = rs.getDate("exercise_date");
			if (exerciseDate != null) {
				equityOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
			}
			equityOptionTrade.setQuantity(rs.getBigDecimal("option_quantity"));

			// Commmon fields
			TradeSQL.setTradeCommonFields(equityOptionTrade, rs);

			// Building the underlying
			EquityTrade underlying = new EquityTrade();
			underlying.setId(rs.getLong("UNDERLYING_EQUITY_TRADE_ID"));
			underlying.setProduct(EquitySQL.getEquityById(rs.getLong("UND_EQUITY_PRODUCT_ID")));
			underlying.setAmount(rs.getBigDecimal("UND_EQUITY_AMOUNT"));
			underlying.setBook(BookSQL.getBookById(rs.getLong("book_id")));
			underlying.setBuySell(rs.getBoolean("UND_EQUITY_buy_sell"));
			underlying.setCounterparty(LegalEntitySQL.getLegalEntityById(rs.getLong("UND_EQUITY_counterparty_id")));
			underlying.setCreationDate(rs.getDate("UND_EQUITY_creation_date").toLocalDate());
			underlying.setQuantity(rs.getBigDecimal("UNDERLYING_EQUITY_QUANTITY"));
			Date undSettleDate = rs.getDate("und_equity_settlement_date");
			if (undSettleDate != null) {
				underlying.setSettlementDate(undSettleDate.toLocalDate());
			}
			Date undTradeDate = rs.getDate("und_equity_trade_date");
			if (undTradeDate != null) {
				underlying.setTradeDate(undTradeDate.toLocalDate());
			}

			equityOptionTrade.setUnderlying(underlying);

		} catch (SQLException | TradistaBusinessException e) {
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}

		return equityOptionTrade;
	}

	public static List<EquityOptionTrade> getEquityOptionTradesBeforeTradeDateByEquityOptionAndBookIds(
			LocalDate tradeDate, long equityOptionId, long bookId) {
		List<EquityOptionTrade> equityOptionTrades = null;

		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetTradesBeforeTradeDateByEquityOptionAndBookIds = con.createStatement()) {
			String query = "SELECT TRADE.*, VANILLA_OPTION_TRADE.*, EQUITY_TRADE.*,  UND_TRADE.PRODUCT_ID UND_PRODUCT_ID, UND_TRADE.AMOUNT UND_AMOUNT, UND_TRADE.SETTLEMENT_DATE UND_SETTLEMENT_DATE, UND_TRADE.TRADE_DATE UND_TRADE_DATE, EQUITY_TRADE.QUANTITY UND_EQUITY_QUANTITY FROM TRADE, TRADE UND_TRADE, VANILLA_OPTION_TRADE, EQUITY_TRADE WHERE "
					+ "TRADE.ID = VANILLA_OPTION_TRADE_ID AND UNDERLYING_TRADE_ID = EQUITY_TRADE_ID AND EQUITY_TRADE_ID = UND_TRADE.ID AND TRADE.TRADE_DATE <= '"
					+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(tradeDate) + "'";
			if (equityOptionId > 0) {
				query += " AND UND_TRADE.PRODUCT_ID = " + equityOptionId;
			}
			if (bookId > 0) {
				query += " AND TRADE.BOOK_ID = " + bookId;
			}
			try (ResultSet results = stmtGetTradesBeforeTradeDateByEquityOptionAndBookIds.executeQuery(query)) {
				while (results.next()) {
					if (equityOptionTrades == null) {
						equityOptionTrades = new ArrayList<EquityOptionTrade>();
					}

					EquityOptionTrade equityOptionTrade = new EquityOptionTrade();
					equityOptionTrade.setStyle(getStyle(results.getString("style")));
					equityOptionTrade.setType(OptionTrade.Type.valueOf(results.getString("type")));
					equityOptionTrade.setAmount(results.getBigDecimal("amount"));
					equityOptionTrade.setStrike(results.getBigDecimal("strike"));
					equityOptionTrade.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					long productId = results.getLong("product_id");
					if (productId != 0) {
						equityOptionTrade.setEquityOption(EquityOptionSQL.getEquityOptionById(productId));
					}
					equityOptionTrade.setSettlementType(
							OptionTrade.SettlementType.valueOf(results.getString("settlement_type")));
					equityOptionTrade.setSettlementDateOffset(results.getInt("settlement_date_offset"));
					equityOptionTrade.setMaturityDate(results.getDate("maturity_date").toLocalDate());
					equityOptionTrade.setBook(BookSQL.getBookById(results.getLong("book_id")));
					equityOptionTrade.setBuySell(results.getBoolean("buy_sell"));
					equityOptionTrade
							.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					equityOptionTrade.setCreationDate(results.getDate("creation_date").toLocalDate());
					equityOptionTrade.setId(results.getLong("VANILLA_OPTION_TRADE_ID"));
					equityOptionTrade.setTradeDate(results.getDate("trade_date").toLocalDate());
					equityOptionTrade.setSettlementDate(results.getDate("settlement_date").toLocalDate());
					Date exerciseDate = results.getDate("exercise_date");
					if (exerciseDate != null) {
						equityOptionTrade.setExerciseDate(exerciseDate.toLocalDate());
					}
					equityOptionTrade.setQuantity(results.getBigDecimal("quantity"));

					// Building the underlying
					EquityTrade underlying = new EquityTrade();
					underlying.setId(results.getLong("UNDERLYING_TRADE_ID"));
					underlying.setProduct(EquitySQL.getEquityById(results.getLong("UND_PRODUCT_ID")));
					underlying.setAmount(results.getBigDecimal("UND_AMOUNT"));
					underlying.setBook(BookSQL.getBookById(results.getLong("book_id")));
					underlying.setBuySell(results.getBoolean("buy_sell"));
					underlying.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
					underlying.setCreationDate(results.getDate("creation_date").toLocalDate());
					underlying.setQuantity(results.getBigDecimal("und_equity_quantity"));
					Date undSettleDate = results.getDate("und_settlement_date");
					if (undSettleDate != null) {
						underlying.setSettlementDate(undSettleDate.toLocalDate());
					}
					Date undTradeDate = results.getDate("und_trade_date");
					if (undTradeDate != null) {
						underlying.setTradeDate(undTradeDate.toLocalDate());
					}

					equityOptionTrade.setUnderlying(underlying);

					equityOptionTrades.add(equityOptionTrade);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptionTrades;
	}

}