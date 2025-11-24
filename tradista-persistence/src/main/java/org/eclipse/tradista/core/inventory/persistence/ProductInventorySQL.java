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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.inventory.model.ProductInventory;
import org.eclipse.tradista.core.product.persistence.ProductSQL;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

public class ProductInventorySQL {

	public static ProductInventory getLastProductInventoryBeforeDateByProductAndBookIds(long productId, long bookId,
			LocalDate date) {
		ProductInventory inventory = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetLastInventoryBeforeDateByProductAndBookIds = con.prepareStatement(
						"SELECT * FROM PRODUCT_INVENTORY WHERE PRODUCT_ID = ? AND BOOK_ID = ? AND FROM_DATE = (SELECT MAX(FROM_DATE) FROM PRODUCT_INVENTORY WHERE PRODUCT_ID = ? AND BOOK_ID = ? AND FROM_DATE <= ? AND (TO_DATE IS NULL OR TO_DATE >= ?))")) {
			stmtGetLastInventoryBeforeDateByProductAndBookIds.setLong(1, productId);
			stmtGetLastInventoryBeforeDateByProductAndBookIds.setLong(2, bookId);
			stmtGetLastInventoryBeforeDateByProductAndBookIds.setLong(3, productId);
			stmtGetLastInventoryBeforeDateByProductAndBookIds.setLong(4, bookId);
			stmtGetLastInventoryBeforeDateByProductAndBookIds.setDate(5, Date.valueOf(date));
			stmtGetLastInventoryBeforeDateByProductAndBookIds.setDate(6, Date.valueOf(date));
			try (ResultSet results = stmtGetLastInventoryBeforeDateByProductAndBookIds.executeQuery()) {
				while (results.next()) {
					if (inventory == null) {
						inventory = new ProductInventory(BookSQL.getBookById(results.getLong("book_id")),
								results.getDate("from_date").toLocalDate(),
								ProductSQL.getProductById(results.getLong("product_id")));
					}
					inventory.setId(results.getLong("id"));
					inventory.setQuantity(results.getBigDecimal("quantity"));
					inventory.setAveragePrice(results.getBigDecimal("average_price"));
					Date to = results.getDate("to_date");
					if (to != null) {
						inventory.setTo(to.toLocalDate());
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return inventory;
	}

	public static ProductInventory getFirstProductInventoryAfterDateByProductAndBookIds(long productId, long bookId,
			LocalDate date) {
		ProductInventory inventory = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFirstInventoryAfterDateByProductAndBookIds = con.prepareStatement(
						"SELECT * FROM PRODUCT_INVENTORY WHERE PRODUCT_ID = ? AND BOOK_ID = ? AND FROM_DATE = (SELECT MIN(FROM_DATE) FROM PRODUCT_INVENTORY WHERE PRODUCT_ID = ? AND BOOK_ID = ? AND FROM_DATE >= ?)")) {
			stmtGetFirstInventoryAfterDateByProductAndBookIds.setLong(1, productId);
			stmtGetFirstInventoryAfterDateByProductAndBookIds.setLong(2, bookId);
			stmtGetFirstInventoryAfterDateByProductAndBookIds.setLong(3, productId);
			stmtGetFirstInventoryAfterDateByProductAndBookIds.setLong(4, bookId);
			stmtGetFirstInventoryAfterDateByProductAndBookIds.setDate(5, Date.valueOf(date));
			try (ResultSet results = stmtGetFirstInventoryAfterDateByProductAndBookIds.executeQuery()) {
				while (results.next()) {
					if (inventory == null) {
						inventory = new ProductInventory(BookSQL.getBookById(results.getLong("book_id")),
								results.getDate("from_date").toLocalDate(),
								ProductSQL.getProductById(results.getLong("product_id")));
					}
					inventory.setId(results.getLong("id"));
					inventory.setQuantity(results.getBigDecimal("quantity"));
					inventory.setAveragePrice(results.getBigDecimal("average_price"));
					Date to = results.getDate("to_date");
					if (to != null) {
						inventory.setTo(to.toLocalDate());
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return inventory;
	}

	public static void save(Set<ProductInventory> inventories) {

		if (inventories == null || inventories.isEmpty()) {
			return;
		}

		ConfigurationBusinessDelegate cbs = new ConfigurationBusinessDelegate();

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveInventory = con.prepareStatement(
						"INSERT INTO PRODUCT_INVENTORY(PRODUCT_ID, BOOK_ID, FROM_DATE, TO_DATE, QUANTITY, AVERAGE_PRICE) VALUES(?, ?, ?, ?, ?, ?)");
				PreparedStatement stmtUpdateInventory = con.prepareStatement(
						"UPDATE PRODUCT_INVENTORY SET PRODUCT_ID=?, BOOK_ID=?, FROM_DATE=?, TO_DATE=?, QUANTITY=?, AVERAGE_PRICE=? WHERE ID=?")) {
			for (ProductInventory inventory : inventories) {

				// 1. Check if the position already exists

				boolean exists = inventory.getId() != 0;

				if (!exists) {

					// 3. If the inventory doesn't exist, we save it

					stmtSaveInventory.setLong(1, inventory.getProduct().getId());
					stmtSaveInventory.setLong(2, inventory.getBook().getId());
					stmtSaveInventory.setDate(3, Date.valueOf(inventory.getFrom()));
					if (inventory.getTo() != null) {
						stmtSaveInventory.setDate(4, Date.valueOf(inventory.getTo()));
					} else {
						stmtSaveInventory.setNull(4, java.sql.Types.DATE);
					}
					// Derby does not support decimal with
					// a precision greater than 31.
					stmtSaveInventory.setBigDecimal(5,
							inventory.getQuantity().setScale(cbs.getScale(), cbs.getRoundingMode()));
					stmtSaveInventory.setBigDecimal(6,
							inventory.getAveragePrice().setScale(cbs.getScale(), cbs.getRoundingMode()));
					stmtSaveInventory.addBatch();

				} else {
					// The inventory exists, so we update it
					stmtUpdateInventory.setLong(1, inventory.getProduct().getId());
					stmtUpdateInventory.setLong(2, inventory.getBook().getId());
					stmtUpdateInventory.setDate(3, Date.valueOf(inventory.getFrom()));
					if (inventory.getTo() != null) {
						stmtUpdateInventory.setDate(4, Date.valueOf(inventory.getTo()));
					} else {
						stmtUpdateInventory.setNull(4, java.sql.Types.DATE);
					}
					stmtUpdateInventory.setBigDecimal(5,
							inventory.getQuantity().setScale(cbs.getScale(), cbs.getRoundingMode()));
					stmtUpdateInventory.setBigDecimal(6,
							inventory.getAveragePrice().setScale(cbs.getScale(), cbs.getRoundingMode()));
					stmtUpdateInventory.setLong(7, inventory.getId());
					stmtUpdateInventory.addBatch();
				}

			}

			stmtSaveInventory.executeBatch();
			stmtUpdateInventory.executeBatch();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

	}

	public static Set<ProductInventory> getProductInventoriesBeforeDateByProductAndBookIds(long productId, long bookId,
			LocalDate date) {
		Set<ProductInventory> inventories = null;
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetInventoriesByProductBookIdsAndDate = con.createStatement()) {
			String query = "SELECT * FROM PRODUCT_INVENTORY WHERE FROM_DATE <= '"
					+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date) + "'";
			if (productId > 0) {
				query += " AND PRODUCT_ID = " + productId;
			}
			if (bookId > 0) {
				query += " AND BOOK_ID = " + bookId;
			}
			try (ResultSet results = stmtGetInventoriesByProductBookIdsAndDate.executeQuery(query)) {
				while (results.next()) {
					if (inventories == null) {
						inventories = new TreeSet<ProductInventory>();
					}
					ProductInventory inventory = new ProductInventory(BookSQL.getBookById(results.getLong("book_id")),
							results.getDate("from_date").toLocalDate(),
							ProductSQL.getProductById(results.getLong("product_id")));
					inventory.setId(results.getLong("id"));
					inventory.setQuantity(results.getBigDecimal("quantity"));
					inventory.setAveragePrice(results.getBigDecimal("average_price"));
					Date to = results.getDate("to_date");
					if (to != null) {
						inventory.setTo(to.toLocalDate());
					}
					inventories.add(inventory);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return inventories;
	}

	public static Set<ProductInventory> getOpenPositionsFromProductInventoryByProductAndBookIds(long productId,
			long bookId) {
		Set<ProductInventory> inventories = null;
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetOpenPositionFromInventoryByProductAndBookIds = con.createStatement()) {
			String query = "SELECT * FROM PRODUCT_INVENTORY WHERE TO_DATE IS NULL";
			if (productId > 0) {
				query += " AND PRODUCT_ID = " + productId;
			}
			if (bookId > 0) {
				query += " AND BOOK_ID = " + bookId;
			}
			try (ResultSet results = stmtGetOpenPositionFromInventoryByProductAndBookIds.executeQuery(query)) {
				while (results.next()) {
					if (inventories == null) {
						inventories = new TreeSet<ProductInventory>();
					}
					ProductInventory inventory = new ProductInventory(BookSQL.getBookById(results.getLong("book_id")),
							results.getDate("from_date").toLocalDate(),
							ProductSQL.getProductById(results.getLong("product_id")));
					inventory.setId(results.getLong("id"));
					inventory.setQuantity(results.getBigDecimal("quantity"));
					inventory.setAveragePrice(results.getBigDecimal("average_price"));
					Date to = results.getDate("to_date");
					if (to != null) {
						inventory.setTo(to.toLocalDate());
					}
					inventories.add(inventory);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return inventories;
	}

	public static BigDecimal getQuantityByDateProductAndBookIds(long productId, long bookId, LocalDate date) {
		BigDecimal quantity = BigDecimal.ZERO;
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetQuantityByDateProductAndBookIds = con.createStatement()) {
			String query = "SELECT QUANTITY FROM PRODUCT_INVENTORY WHERE";

			if (productId > 0) {
				query += " PRODUCT_ID=" + productId + " AND";
			}

			if (bookId > 0) {
				query += " BOOK_ID=" + bookId + " AND";
			}

			query += " (TO_DATE IS NULL OR TO_DATE >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date)
					+ "') ";
			query += "AND FROM_DATE = (SELECT MAX(FROM_DATE) FROM PRODUCT_INVENTORY WHERE FROM_DATE <= '"
					+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date) + "' ";

			if (productId > 0) {
				query += " AND PRODUCT_ID=" + productId;
			}

			if (bookId > 0) {
				query += " AND BOOK_ID=" + bookId + ")";
			}
			try (ResultSet results = stmtGetQuantityByDateProductAndBookIds.executeQuery(query)) {
				while (results.next()) {
					quantity = results.getBigDecimal("quantity");
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return quantity;
	}

	public static BigDecimal getAveragePriceByDateProductAndBookIds(long productId, long bookId, LocalDate date) {
		BigDecimal averagePrice = null;
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetAveragePriceByDateProductAndBookIds = con.createStatement()) {
			String query = "SELECT AVERAGE_PRICE FROM PRODUCT_INVENTORY WHERE";

			if (productId > 0) {
				query += " PRODUCT_ID=" + productId + " AND";
			}

			if (bookId > 0) {
				query += " BOOK_ID=" + bookId + " AND";
			}

			query += " (TO_DATE IS NULL OR TO_DATE >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date)
					+ "') ";
			query += "AND FROM_DATE = (SELECT MAX(FROM_DATE) FROM PRODUCT_INVENTORY WHERE FROM_DATE <= '"
					+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(date) + "' ";

			if (productId > 0) {
				query += " AND PRODUCT_ID=" + productId;
			}

			if (bookId > 0) {
				query += " AND BOOK_ID=" + bookId;
			}
			query += " )";
			try (ResultSet results = stmtGetAveragePriceByDateProductAndBookIds.executeQuery(query)) {
				while (results.next()) {
					averagePrice = results.getBigDecimal("average_price");
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return averagePrice;
	}

	public static void remove(Set<Long> inventoryIds) {
		if (inventoryIds == null || inventoryIds.isEmpty()) {
			return;
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteInventory = con
						.prepareStatement("DELETE FROM PRODUCT_INVENTORY WHERE ID=?")) {
			for (long id : inventoryIds) {
				stmtDeleteInventory.setLong(1, id);
				stmtDeleteInventory.addBatch();
			}

			stmtDeleteInventory.executeBatch();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static Set<ProductInventory> getProductInventoriesByProductAndBookIds(long productId, long bookId) {
		Set<ProductInventory> inventories = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetInventoriesByProductAndBookIds = con
						.prepareStatement("SELECT * FROM PRODUCT_INVENTORY WHERE PRODUCT_ID = ? AND BOOK_ID = ?")) {
			stmtGetInventoriesByProductAndBookIds.setLong(1, productId);
			stmtGetInventoriesByProductAndBookIds.setLong(2, bookId);
			try (ResultSet results = stmtGetInventoriesByProductAndBookIds.executeQuery()) {
				while (results.next()) {
					if (inventories == null) {
						inventories = new HashSet<ProductInventory>();
					}
					ProductInventory inventory = new ProductInventory(BookSQL.getBookById(results.getLong("book_id")),
							results.getDate("from_date").toLocalDate(),
							ProductSQL.getProductById(results.getLong("product_id")));
					inventory.setId(results.getLong("id"));
					inventory.setQuantity(results.getBigDecimal("quantity"));
					inventory.setAveragePrice(results.getBigDecimal("average_price"));
					Date to = results.getDate("to_date");
					if (to != null) {
						inventory.setTo(to.toLocalDate());
					}
					inventories.add(inventory);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return inventories;
	}

	public static Set<ProductInventory> getProductInventories(LocalDate from, LocalDate to, String productType,
			long productId, long bookId, boolean onlyOpenPositions) {
		Set<ProductInventory> inventories = null;
		try (Connection con = TradistaDB.getConnection(); Statement stmtGetInventories = con.createStatement()) {
			String query = "SELECT * FROM PRODUCT_INVENTORY ";

			if (productId > 0) {
				query += " WHERE PRODUCT_ID=" + productId;
			} else if (!StringUtils.isEmpty(productType)) {
				query += " WHERE PRODUCT_ID IN (SELECT PRODUCT_ID FROM " + ProductSQL.getProductTableByType(productType)
						+ ")";
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

			try (ResultSet results = stmtGetInventories.executeQuery(query)) {
				while (results.next()) {
					if (inventories == null) {
						inventories = new TreeSet<>();
					}
					ProductInventory inventory = new ProductInventory(BookSQL.getBookById(results.getLong("book_id")),
							results.getDate("from_date").toLocalDate(),
							ProductSQL.getProductById(results.getLong("product_id")));
					inventory.setId(results.getLong("id"));
					inventory.setQuantity(results.getBigDecimal("quantity"));
					inventory.setAveragePrice(results.getBigDecimal("average_price"));
					Date toResult = results.getDate("to_date");
					if (toResult != null) {
						inventory.setTo(toResult.toLocalDate());
					}
					inventories.add(inventory);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return inventories;
	}

}