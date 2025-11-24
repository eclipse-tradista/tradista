package org.eclipse.tradista.core.inventory.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.inventory.model.CashInventory;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class CashInventorySQL {

	public static CashInventory getLastCashInventoryBeforeDateByCurrencyAndBookIds(long currencyId, long bookId,
			LocalDate date) {
		CashInventory cashInventory = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetLastCashInventoryBeforeDateByCurrencyAndBookIds = con.prepareStatement(
						"SELECT * FROM CASH_INVENTORY WHERE CURRENCY_ID = ? AND BOOK_ID = ? AND FROM_DATE = (SELECT MAX(FROM_DATE) FROM CASH_INVENTORY WHERE CURRENCY_ID = ? AND BOOK_ID = ? AND FROM_DATE <= ?) AND (TO_DATE IS NULL OR TO_DATE >= ?)")) {
			stmtGetLastCashInventoryBeforeDateByCurrencyAndBookIds.setLong(1, currencyId);
			stmtGetLastCashInventoryBeforeDateByCurrencyAndBookIds.setLong(2, bookId);
			stmtGetLastCashInventoryBeforeDateByCurrencyAndBookIds.setLong(3, currencyId);
			stmtGetLastCashInventoryBeforeDateByCurrencyAndBookIds.setLong(4, bookId);
			stmtGetLastCashInventoryBeforeDateByCurrencyAndBookIds.setDate(5, Date.valueOf(date));
			stmtGetLastCashInventoryBeforeDateByCurrencyAndBookIds.setDate(6, Date.valueOf(date));
			try (ResultSet results = stmtGetLastCashInventoryBeforeDateByCurrencyAndBookIds.executeQuery()) {
				while (results.next()) {
					if (cashInventory == null) {
						cashInventory = new CashInventory(CurrencySQL.getCurrencyById(results.getLong("currency_id")),
								results.getDate("from_date").toLocalDate(),
								BookSQL.getBookById(results.getLong("book_id")));
					}
					cashInventory.setId(results.getLong("id"));
					cashInventory.setAmount(results.getBigDecimal("amount"));
					Date to = results.getDate("to_date");
					if (to != null) {
						cashInventory.setTo(to.toLocalDate());
					}
				}

			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return cashInventory;

	}

	public static CashInventory getFirstCashInventoryAfterDateByCurrencyAndBookIds(long currencyId, long bookId,
			LocalDate date) {
		CashInventory cashInventory = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFirstCashInventoryAfterDateByCurrencyAndBookIds = con.prepareStatement(
						"SELECT * FROM CASH_INVENTORY WHERE CURRENCY_ID = ? AND BOOK_ID = ? AND FROM_DATE = (SELECT MIN(FROM_DATE) FROM CASH_INVENTORY WHERE CURRENCY_ID = ? AND BOOK_ID = ? AND FROM_DATE >= ?)")) {
			stmtGetFirstCashInventoryAfterDateByCurrencyAndBookIds.setLong(1, currencyId);
			stmtGetFirstCashInventoryAfterDateByCurrencyAndBookIds.setLong(2, bookId);
			stmtGetFirstCashInventoryAfterDateByCurrencyAndBookIds.setLong(3, currencyId);
			stmtGetFirstCashInventoryAfterDateByCurrencyAndBookIds.setLong(4, bookId);
			stmtGetFirstCashInventoryAfterDateByCurrencyAndBookIds.setDate(5, Date.valueOf(date));
			try (ResultSet results = stmtGetFirstCashInventoryAfterDateByCurrencyAndBookIds.executeQuery()) {
				while (results.next()) {
					if (cashInventory == null) {
						cashInventory = new CashInventory(CurrencySQL.getCurrencyById(results.getLong("currency_id")),
								results.getDate("from_date").toLocalDate(),
								BookSQL.getBookById(results.getLong("book_id")));
					}
					cashInventory.setId(results.getLong("id"));
					cashInventory.setAmount(results.getBigDecimal("amount"));
					Date to = results.getDate("to_date");
					if (to != null) {
						cashInventory.setTo(to.toLocalDate());
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return cashInventory;
	}

	public static void save(Set<CashInventory> cashInventories) {

		if (cashInventories == null || cashInventories.isEmpty()) {
			return;
		}

		ConfigurationBusinessDelegate cbs = new ConfigurationBusinessDelegate();

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveCashInventory = con.prepareStatement(
						"INSERT INTO CASH_INVENTORY(CURRENCY_ID, BOOK_ID, FROM_DATE, TO_DATE, AMOUNT) VALUES(?, ?, ?, ?, ?)");
				PreparedStatement stmtUpdateCashInventory = con.prepareStatement(
						"UPDATE CASH_INVENTORY SET CURRENCY_ID=?, BOOK_ID=?, FROM_DATE=?, TO_DATE=?, AMOUNT=? WHERE ID=?")) {

			for (CashInventory cashInventory : cashInventories) {

				// 1. Check if the position already exists

				boolean exists = cashInventory.getId() != 0;

				if (!exists) {

					// 3. If the inventory doesn't exist, we save it

					stmtSaveCashInventory.setLong(1, cashInventory.getCurrency().getId());
					stmtSaveCashInventory.setLong(2, cashInventory.getBook().getId());
					stmtSaveCashInventory.setDate(3, Date.valueOf(cashInventory.getFrom()));
					if (cashInventory.getTo() != null) {
						stmtSaveCashInventory.setDate(4, Date.valueOf(cashInventory.getTo()));
					} else {
						stmtSaveCashInventory.setNull(4, java.sql.Types.DATE);
					}
					// Derby does not support decimal with
					// a precision greater than 31.
					stmtSaveCashInventory.setBigDecimal(5,
							cashInventory.getAmount().setScale(cbs.getScale(), cbs.getRoundingMode()));
					stmtSaveCashInventory.addBatch();

				} else {
					// The inventory exists, so we update it
					stmtUpdateCashInventory.setLong(1, cashInventory.getCurrency().getId());
					stmtUpdateCashInventory.setLong(2, cashInventory.getBook().getId());
					stmtUpdateCashInventory.setDate(3, Date.valueOf(cashInventory.getFrom()));
					if (cashInventory.getTo() != null) {
						stmtUpdateCashInventory.setDate(4, Date.valueOf(cashInventory.getTo()));
					} else {
						stmtUpdateCashInventory.setNull(4, java.sql.Types.DATE);
					}
					stmtUpdateCashInventory.setBigDecimal(5,
							cashInventory.getAmount().setScale(cbs.getScale(), cbs.getRoundingMode()));
					stmtUpdateCashInventory.setLong(6, cashInventory.getId());
					stmtUpdateCashInventory.addBatch();
				}

			}

			stmtSaveCashInventory.executeBatch();
			stmtUpdateCashInventory.executeBatch();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

	}

	public static Set<CashInventory> getCashInventoriesBeforeDateByCurrencyAndBookIds(long currencyId, long bookId,
			LocalDate date) {
		Set<CashInventory> cashInventories = null;
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetCashInventoriesByCurrencyBookIdsAndDate = con.createStatement()) {
			String query = "SELECT * FROM CASH_INVENTORY WHERE FROM_DATE <= '"
					+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date) + "'";
			if (currencyId > 0) {
				query += " AND CURRENCY_ID = " + currencyId;
			}
			if (bookId > 0) {
				query += " AND BOOK_ID = " + bookId;
			}
			try (ResultSet results = stmtGetCashInventoriesByCurrencyBookIdsAndDate.executeQuery(query)) {
				while (results.next()) {
					if (cashInventories == null) {
						cashInventories = new TreeSet<CashInventory>();
					}
					CashInventory cashInventory = new CashInventory(
							CurrencySQL.getCurrencyById(results.getLong("currency_id")),
							results.getDate("from_date").toLocalDate(),
							BookSQL.getBookById(results.getLong("book_id")));
					cashInventory.setId(results.getLong("id"));
					cashInventory.setAmount(results.getBigDecimal("amount"));
					Date to = results.getDate("to_date");
					if (to != null) {
						cashInventory.setTo(to.toLocalDate());
					}
					cashInventories.add(cashInventory);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return cashInventories;
	}

	public static Set<CashInventory> getOpenPositionsFromCashInventoryByCurrencyAndBookIds(long currencyId,
			long bookId) {
		Set<CashInventory> cashInventories = null;
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetOpenPositionFromCashInventoryByCurrencyAndBookIds = con.createStatement()) {
			String query = "SELECT * FROM CASH_INVENTORY WHERE TO_DATE IS NULL";
			if (currencyId > 0) {
				query += " AND CURRENCY_ID = " + currencyId;
			}
			if (bookId > 0) {
				query += " AND BOOK_ID = " + bookId;
			}
			try (ResultSet results = stmtGetOpenPositionFromCashInventoryByCurrencyAndBookIds.executeQuery(query)) {
				while (results.next()) {
					if (cashInventories == null) {
						cashInventories = new TreeSet<CashInventory>();
					}
					CashInventory cashInventory = new CashInventory(
							CurrencySQL.getCurrencyById(results.getLong("currency_id")),
							results.getDate("from_date").toLocalDate(),
							BookSQL.getBookById(results.getLong("book_id")));
					cashInventory.setId(results.getLong("id"));
					cashInventory.setAmount(results.getBigDecimal("amount"));
					Date to = results.getDate("to_date");
					if (to != null) {
						cashInventory.setTo(to.toLocalDate());
					}
					cashInventories.add(cashInventory);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return cashInventories;
	}

	public static BigDecimal getAmountByDateCurrencyAndBookIds(long currencyId, long bookId, LocalDate date) {
		BigDecimal amount = BigDecimal.ZERO;
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetAmountByDateCurrencyAndBookIds = con.createStatement()) {
			String query = "SELECT AMOUNT FROM CASH_INVENTORY WHERE";

			if (currencyId > 0) {
				query += " CURRENCY_ID=" + currencyId + " AND";
			}

			if (bookId > 0) {
				query += " BOOK_ID=" + bookId + " AND";
			}

			query += " (TO_DATE IS NULL OR TO_DATE >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date)
					+ "') ";
			query += "AND FROM_DATE = (SELECT MAX(FROM_DATE) FROM CASH_INVENTORY WHERE FROM_DATE <= '"
					+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date) + "' ";

			if (currencyId > 0) {
				query += " AND CURRENCY_ID=" + currencyId;
			}

			if (bookId > 0) {
				query += " AND BOOK_ID=" + bookId + ")";
			}
			try (ResultSet results = stmtGetAmountByDateCurrencyAndBookIds.executeQuery(query)) {
				while (results.next()) {
					amount = results.getBigDecimal("amount");
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return amount;
	}

	public static void remove(Set<Long> cashInventoryIds) {
		if (cashInventoryIds == null || cashInventoryIds.isEmpty()) {
			return;
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteCashInventory = con
						.prepareStatement("DELETE FROM CASH_INVENTORY WHERE ID=?")) {

			for (long id : cashInventoryIds) {
				stmtDeleteCashInventory.setLong(1, id);
				stmtDeleteCashInventory.addBatch();
			}

			stmtDeleteCashInventory.executeBatch();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static Set<CashInventory> getCashInventoriesByCurrencyAndBookIds(long currencyId, long bookId) {

		Set<CashInventory> cashInventories = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCashInventoriesByCurrencyAndBookIds = con
						.prepareStatement("SELECT * FROM CASH_INVENTORY WHERE CURRENCY_ID = ? AND BOOK_ID = ?")) {

			stmtGetCashInventoriesByCurrencyAndBookIds.setLong(1, currencyId);
			stmtGetCashInventoriesByCurrencyAndBookIds.setLong(2, bookId);
			try (ResultSet results = stmtGetCashInventoriesByCurrencyAndBookIds.executeQuery()) {
				while (results.next()) {
					if (cashInventories == null) {
						cashInventories = new HashSet<CashInventory>();
					}
					CashInventory cashInventory = new CashInventory(
							CurrencySQL.getCurrencyById(results.getLong("currency_id")),
							results.getDate("from_date").toLocalDate(),
							BookSQL.getBookById(results.getLong("book_id")));
					cashInventory.setId(results.getLong("id"));
					cashInventory.setAmount(results.getBigDecimal("amount"));
					Date to = results.getDate("to_date");
					if (to != null) {
						cashInventory.setTo(to.toLocalDate());
					}
					cashInventories.add(cashInventory);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return cashInventories;
	}

	public static Set<CashInventory> getCashInventories(LocalDate from, LocalDate to, long currencyId, long bookId,
			boolean onlyOpenPositions) {

		Set<CashInventory> cashInventories = null;
		try (Connection con = TradistaDB.getConnection(); Statement stmtGetCashInventories = con.createStatement()) {
			String query = "SELECT * FROM CASH_INVENTORY ";

			if (currencyId > 0) {
				query += " WHERE CURRENCY_ID=" + currencyId;
			}

			if (bookId > 0) {
				if (query.contains("WHERE")) {
					query += " AND ";
				} else {
					query += " WHERE ";
				}
				query += " BOOK_ID=" + bookId;
			}

			if (from != null) {
				if (query.contains("WHERE")) {
					query += " AND ";
				} else {
					query += " WHERE ";
				}
				query += " (TO_DATE >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(from) + "'"
						+ " OR TO_DATE IS NULL)";
			}

			if (onlyOpenPositions) {
				if (query.contains("WHERE")) {
					query += " AND ";
				} else {
					query += " WHERE ";
				}
				query += " TO_DATE IS NULL";
			} else {
				if (to != null) {
					if (query.contains("WHERE")) {
						query += " AND ";
					} else {
						query += " WHERE ";
					}
					query += " (FROM_DATE <= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(to) + "')";
				}
			}

			try (ResultSet results = stmtGetCashInventories.executeQuery(query)) {
				while (results.next()) {
					if (cashInventories == null) {
						cashInventories = new TreeSet<CashInventory>();
					}
					CashInventory cashInventory = new CashInventory(
							CurrencySQL.getCurrencyById(results.getLong("currency_id")),
							results.getDate("from_date").toLocalDate(),
							BookSQL.getBookById(results.getLong("book_id")));
					cashInventory.setId(results.getLong("id"));
					cashInventory.setAmount(results.getBigDecimal("amount"));
					Date toResult = results.getDate("to_date");
					if (toResult != null) {
						cashInventory.setTo(toResult.toLocalDate());
					}
					cashInventories.add(cashInventory);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return cashInventories;
	}

}