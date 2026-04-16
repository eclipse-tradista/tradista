package org.eclipse.tradista.core.inventory.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.BOOK_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRODUCT_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUANTITY;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.AND;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.WHERE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MM_DD_YYYY;

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
import org.eclipse.tradista.core.common.persistence.util.Expression;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.common.persistence.util.UnaryFunctionExpression;
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

	private static final Field ID_FIELD = new Field(ID);
	private static final Field PRODUCT_ID_FIELD = new Field(PRODUCT_ID);
	private static final Field BOOK_ID_FIELD = new Field(BOOK_ID);
	private static final Field FROM_DATE_FIELD = new Field("FROM_DATE");
	private static final Field TO_DATE_FIELD = new Field("TO_DATE");
	private static final Field QUANTITY_FIELD = new Field(QUANTITY);
	private static final Field AVERAGE_PRICE_FIELD = new Field("AVERAGE_PRICE");

	private static final Field[] PRODUCT_INVENTORY_FIELDS = { ID_FIELD, PRODUCT_ID_FIELD, BOOK_ID_FIELD,
			FROM_DATE_FIELD, TO_DATE_FIELD, QUANTITY_FIELD, AVERAGE_PRICE_FIELD };

	private static final Field[] PRODUCT_INVENTORY_FIELDS_FOR_INSERT_OR_UPDATE = { PRODUCT_ID_FIELD, BOOK_ID_FIELD,
			FROM_DATE_FIELD, TO_DATE_FIELD, QUANTITY_FIELD, AVERAGE_PRICE_FIELD };

	public static final Table PRODUCT_INVENTORY_TABLE = new Table("PRODUCT_INVENTORY", PRODUCT_INVENTORY_FIELDS);

	private static final String SELECT_QUERY = TradistaDBUtil.buildSelectQuery(PRODUCT_INVENTORY_TABLE);

	public static ProductInventory getLastProductInventoryBeforeDateByProductAndBookIds(long productId, long bookId,
			LocalDate date) {
		ProductInventory inventory = null;
		StringBuilder queryFilter = new StringBuilder(TradistaDBUtil.buildSelectQuery(
				UnaryFunctionExpression.max(FROM_DATE_FIELD), PRODUCT_INVENTORY_TABLE));
		TradistaDBUtil.addParameterizedFilter(queryFilter, PRODUCT_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(queryFilter, BOOK_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(queryFilter, FROM_DATE_FIELD, false);
		// The framework doesn't manage "OR" yet, adding it manually
		queryFilter.append("AND (TO_DATE IS NULL OR TO_DATE >= ?)");
		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(PRODUCT_INVENTORY_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlQuery, PRODUCT_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, BOOK_ID_FIELD);
		TradistaDBUtil.addQueryFilter(sqlQuery, FROM_DATE_FIELD, queryFilter.toString(), false);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetLastInventoryBeforeDateByProductAndBookIds = con
						.prepareStatement(sqlQuery.toString())) {
			stmtGetLastInventoryBeforeDateByProductAndBookIds.setLong(1, productId);
			stmtGetLastInventoryBeforeDateByProductAndBookIds.setLong(2, bookId);
			stmtGetLastInventoryBeforeDateByProductAndBookIds.setLong(3, productId);
			stmtGetLastInventoryBeforeDateByProductAndBookIds.setLong(4, bookId);
			stmtGetLastInventoryBeforeDateByProductAndBookIds.setDate(5, Date.valueOf(date));
			stmtGetLastInventoryBeforeDateByProductAndBookIds.setDate(6, Date.valueOf(date));
			try (ResultSet results = stmtGetLastInventoryBeforeDateByProductAndBookIds.executeQuery()) {
				while (results.next()) {
					buildProductInventory(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return inventory;
	}

	public static ProductInventory getFirstProductInventoryAfterDateByProductAndBookIds(long productId, long bookId,
			LocalDate date) {
		ProductInventory inventory = null;
		StringBuilder queryFilter = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(UnaryFunctionExpression.min(FROM_DATE_FIELD), PRODUCT_INVENTORY_TABLE));
		TradistaDBUtil.addParameterizedFilter(queryFilter, PRODUCT_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(queryFilter, BOOK_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(queryFilter, FROM_DATE_FIELD, true);
		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(PRODUCT_INVENTORY_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlQuery, PRODUCT_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, BOOK_ID_FIELD);
		TradistaDBUtil.addQueryFilter(sqlQuery, FROM_DATE_FIELD, queryFilter.toString(), false);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFirstInventoryAfterDateByProductAndBookIds = con
						.prepareStatement(sqlQuery.toString())) {
			stmtGetFirstInventoryAfterDateByProductAndBookIds.setLong(1, productId);
			stmtGetFirstInventoryAfterDateByProductAndBookIds.setLong(2, bookId);
			stmtGetFirstInventoryAfterDateByProductAndBookIds.setLong(3, productId);
			stmtGetFirstInventoryAfterDateByProductAndBookIds.setLong(4, bookId);
			stmtGetFirstInventoryAfterDateByProductAndBookIds.setDate(5, Date.valueOf(date));
			try (ResultSet results = stmtGetFirstInventoryAfterDateByProductAndBookIds.executeQuery()) {
				while (results.next()) {
					inventory = buildProductInventory(results);
				}
			}
		} catch (SQLException sqle) {
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
				PreparedStatement stmtSaveInventory = TradistaDBUtil.buildInsertPreparedStatement(con,
						PRODUCT_INVENTORY_TABLE, PRODUCT_INVENTORY_FIELDS_FOR_INSERT_OR_UPDATE);
				PreparedStatement stmtUpdateInventory = TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD,
						PRODUCT_INVENTORY_TABLE, PRODUCT_INVENTORY_FIELDS_FOR_INSERT_OR_UPDATE)) {
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
			throw new TradistaTechnicalException(sqle);
		}

	}

	public static Set<ProductInventory> getProductInventoriesBeforeDateByProductAndBookIds(long productId, long bookId,
			LocalDate date) {
		Set<ProductInventory> inventories = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);
		TradistaDBUtil.addFilter(sqlQuery, FROM_DATE_FIELD, date, false);
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetInventoriesByProductBookIdsAndDate = con.createStatement()) {

			if (productId > 0) {
				TradistaDBUtil.addFilter(sqlQuery, PRODUCT_ID_FIELD, productId);
			}
			if (bookId > 0) {
				TradistaDBUtil.addFilter(sqlQuery, BOOK_ID_FIELD, bookId);
			}
			try (ResultSet results = stmtGetInventoriesByProductBookIdsAndDate.executeQuery(sqlQuery.toString())) {
				while (results.next()) {
					if (inventories == null) {
						inventories = new TreeSet<>();
					}
					inventories.add(buildProductInventory(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return inventories;
	}

	public static Set<ProductInventory> getOpenPositionsFromProductInventoryByProductAndBookIds(long productId,
			long bookId) {
		Set<ProductInventory> inventories = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);
		TradistaDBUtil.addIsNullFilter(sqlQuery, TO_DATE_FIELD);
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetOpenPositionFromInventoryByProductAndBookIds = con.createStatement()) {
			if (productId > 0) {
				TradistaDBUtil.addFilter(sqlQuery, PRODUCT_ID_FIELD, productId);
			}
			if (bookId > 0) {
				TradistaDBUtil.addFilter(sqlQuery, BOOK_ID_FIELD, bookId);
			}
			try (ResultSet results = stmtGetOpenPositionFromInventoryByProductAndBookIds
					.executeQuery(sqlQuery.toString())) {
				while (results.next()) {
					if (inventories == null) {
						inventories = new TreeSet<>();
					}
					inventories.add(buildProductInventory(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return inventories;
	}

	public static BigDecimal getQuantityByDateProductAndBookIds(long productId, long bookId, LocalDate date) {
		BigDecimal quantity = BigDecimal.ZERO;
		StringBuilder sqlQuery = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(QUANTITY_FIELD, PRODUCT_INVENTORY_TABLE));
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetQuantityByDateProductAndBookIds = con.createStatement()) {
			if (productId > 0) {
				TradistaDBUtil.addFilter(sqlQuery, PRODUCT_ID_FIELD, productId);
			}

			if (bookId > 0) {
				TradistaDBUtil.addFilter(sqlQuery, BOOK_ID_FIELD, bookId);
			}

			// The framework doesn't manage "OR" yet, adding it manually
			sqlQuery.append(AND + " (TO_DATE IS NULL OR TO_DATE >= '"
					+ DateTimeFormatter.ofPattern(MM_DD_YYYY).format(date) + "') ");

			StringBuilder queryFilter = new StringBuilder(TradistaDBUtil.buildSelectQuery(
					new Expression[] { UnaryFunctionExpression.max(FROM_DATE_FIELD) }, PRODUCT_INVENTORY_TABLE));
			TradistaDBUtil.addFilter(queryFilter, FROM_DATE_FIELD, date, false);

			if (productId > 0) {
				TradistaDBUtil.addFilter(queryFilter, PRODUCT_ID_FIELD, productId);
			}

			if (bookId > 0) {
				TradistaDBUtil.addFilter(queryFilter, BOOK_ID_FIELD, bookId);
			}

			TradistaDBUtil.addQueryFilter(sqlQuery, FROM_DATE_FIELD, queryFilter.toString(), false);
			try (ResultSet results = stmtGetQuantityByDateProductAndBookIds.executeQuery(sqlQuery.toString())) {
				while (results.next()) {
					quantity = results.getBigDecimal(QUANTITY_FIELD.getName());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return quantity;
	}

	public static BigDecimal getAveragePriceByDateProductAndBookIds(long productId, long bookId, LocalDate date) {
		BigDecimal averagePrice = null;
		StringBuilder sqlQuery = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(AVERAGE_PRICE_FIELD, PRODUCT_INVENTORY_TABLE));
		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetAveragePriceByDateProductAndBookIds = con.createStatement()) {

			if (productId > 0) {
				TradistaDBUtil.addFilter(sqlQuery, PRODUCT_ID_FIELD, productId);
			}

			if (bookId > 0) {
				TradistaDBUtil.addFilter(sqlQuery, BOOK_ID_FIELD, bookId);
			}

			// The framework doesn't manage "OR" yet, adding it manually
			sqlQuery.append(AND + " (TO_DATE IS NULL OR TO_DATE >= '"
					+ DateTimeFormatter.ofPattern(MM_DD_YYYY).format(date) + "') ");

			StringBuilder queryFilter = new StringBuilder(TradistaDBUtil.buildSelectQuery(
					new Expression[] { UnaryFunctionExpression.max(FROM_DATE_FIELD) }, PRODUCT_INVENTORY_TABLE));
			TradistaDBUtil.addFilter(queryFilter, FROM_DATE_FIELD, date, false);

			if (productId > 0) {
				TradistaDBUtil.addFilter(queryFilter, PRODUCT_ID_FIELD, productId);
			}

			if (bookId > 0) {
				TradistaDBUtil.addFilter(queryFilter, BOOK_ID_FIELD, bookId);
			}
			TradistaDBUtil.addQueryFilter(sqlQuery, FROM_DATE_FIELD, queryFilter.toString(), false);
			try (ResultSet results = stmtGetAveragePriceByDateProductAndBookIds.executeQuery(sqlQuery.toString())) {
				while (results.next()) {
					averagePrice = results.getBigDecimal(AVERAGE_PRICE_FIELD.getName());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return averagePrice;
	}

	public static void remove(Set<Long> inventoryIds) {
		if (inventoryIds == null || inventoryIds.isEmpty()) {
			return;
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteInventory = TradistaDBUtil.buildDeletePreparedStatement(con,
						PRODUCT_INVENTORY_TABLE, ID_FIELD)) {
			for (long id : inventoryIds) {
				stmtDeleteInventory.setLong(1, id);
				stmtDeleteInventory.addBatch();
			}

			stmtDeleteInventory.executeBatch();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static Set<ProductInventory> getProductInventoriesByProductAndBookIds(long productId, long bookId) {
		Set<ProductInventory> inventories = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, PRODUCT_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, BOOK_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetInventoriesByProductAndBookIds = con.prepareStatement(sqlQuery.toString())) {
			stmtGetInventoriesByProductAndBookIds.setLong(1, productId);
			stmtGetInventoriesByProductAndBookIds.setLong(2, bookId);
			try (ResultSet results = stmtGetInventoriesByProductAndBookIds.executeQuery()) {
				while (results.next()) {
					if (inventories == null) {
						inventories = new HashSet<>();
					}
					inventories.add(buildProductInventory(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return inventories;
	}

	public static Set<ProductInventory> getProductInventories(LocalDate from, LocalDate to, String productType,
			long productId, long bookId, boolean onlyOpenPositions) {
		Set<ProductInventory> inventories = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);
		try (Connection con = TradistaDB.getConnection(); Statement stmtGetInventories = con.createStatement()) {
			if (productId > 0) {
				TradistaDBUtil.addFilter(sqlQuery, PRODUCT_ID_FIELD, productId);
			} else if (!StringUtils.isEmpty(productType)) {
				// Added manually as the framework doesn't manage dynamic table name
				sqlQuery.append(WHERE + "PRODUCT_ID IN (SELECT PRODUCT_ID FROM "
						+ ProductSQL.getProductTableByType(productType) + ")");
			}

			if (bookId > 0) {
				TradistaDBUtil.addFilter(sqlQuery, BOOK_ID_FIELD, bookId);
			}

			if (from != null) {
				// Added manually as the framework doesn't manage 'OR'
				if (sqlQuery.indexOf(WHERE) != -1) {
					sqlQuery.append(AND);
				} else {
					sqlQuery.append(WHERE);
				}
				sqlQuery.append(" (TO_DATE >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(from) + "'"
						+ " OR TO_DATE IS NULL)");
			}

			if (onlyOpenPositions) {
				TradistaDBUtil.addIsNullFilter(sqlQuery, TO_DATE_FIELD);
			} else {
				if (to != null) {
					TradistaDBUtil.addFilter(sqlQuery, FROM_DATE_FIELD, to, false);
				}
			}

			try (ResultSet results = stmtGetInventories.executeQuery(sqlQuery.toString())) {
				while (results.next()) {
					if (inventories == null) {
						inventories = new TreeSet<>();
					}
					inventories.add(buildProductInventory(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return inventories;
	}

	private static ProductInventory buildProductInventory(ResultSet results) throws SQLException {
		ProductInventory inventory = new ProductInventory(BookSQL.getBookById(results.getLong(BOOK_ID_FIELD.getName())),
				results.getDate(FROM_DATE_FIELD.getName()).toLocalDate(),
				ProductSQL.getProductById(results.getLong(PRODUCT_ID_FIELD.getName())));
		inventory.setId(results.getLong(ID_FIELD.getName()));
		inventory.setQuantity(results.getBigDecimal(QUANTITY_FIELD.getName()));
		inventory.setAveragePrice(results.getBigDecimal(AVERAGE_PRICE_FIELD.getName()));
		Date toResult = results.getDate(TO_DATE_FIELD.getName());
		if (toResult != null) {
			inventory.setTo(toResult.toLocalDate());
		}
		return inventory;
	}

}