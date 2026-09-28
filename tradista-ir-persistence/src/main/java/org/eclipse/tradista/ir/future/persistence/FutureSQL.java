package org.eclipse.tradista.ir.future.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CREATION_TIME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MATURITY_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.SYMBOL;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.product.persistence.ProductSQL;
import org.eclipse.tradista.ir.future.model.Future;

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

public class FutureSQL {

	private static final Field CREATION_TIME_FIELD = new Field(CREATION_TIME);

	private static final Field FUTURE_ID_FIELD = new Field("FUTURE_ID");
	private static final Field FUTURE_CONTRACT_SPECIFICATION_ID_FIELD = new Field("FUTURE_CONTRACT_SPECIFICATION_ID");
	private static final Field SYMBOL_FIELD = new Field(SYMBOL);
	private static final Field MATURITY_DATE_FIELD = new Field(MATURITY_DATE);

	private static final Field[] FUTURE_FIELDS = { FUTURE_ID_FIELD, FUTURE_CONTRACT_SPECIFICATION_ID_FIELD,
			SYMBOL_FIELD, MATURITY_DATE_FIELD };
	public static final Table FUTURE_TABLE = new Table("FUTURE", FUTURE_FIELDS);

	private static final Field FCS_ID_FIELD = new Field(ID);
	private static final Field FCS_NAME_FIELD = new Field(NAME);
	private static final Field[] FCS_FIELDS = { FCS_ID_FIELD, FCS_NAME_FIELD };
	public static final Table FUTURE_CONTRACT_SPECIFICATION_TABLE = new Table("FUTURE_CONTRACT_SPECIFICATION",
			FCS_FIELDS);

	public static Future getFutureById(long id) {
		Future future = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(FUTURE_TABLE,
				Join.innerEq(ProductSQL.PRODUCT_TABLE, FUTURE_ID_FIELD, ProductSQL.ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sql, FUTURE_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFutureById = con.prepareStatement(sql.toString())) {
			stmtGetFutureById.setLong(1, id);
			try (ResultSet results = stmtGetFutureById.executeQuery()) {
				while (results.next()) {
					if (future == null) {
						future = new Future(results.getString(SYMBOL_FIELD.getName()),
								FutureContractSpecificationSQL.getFutureContractSpecificationById(
										results.getLong(FUTURE_CONTRACT_SPECIFICATION_ID_FIELD.getName())));
					}
					future.setId(results.getLong(FUTURE_ID_FIELD.getName()));
					Timestamp creationTimestamp = results.getTimestamp(CREATION_TIME_FIELD.getName());
					if (creationTimestamp != null) {
						future.setCreationDate(creationTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
					}
					future.setMaturityDate(results.getDate(MATURITY_DATE_FIELD.getName()).toLocalDate());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return future;
	}

	public static Future getFutureByContractSpecificationAndSymbol(String contractSpecification, String symbol) {
		Future future = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(FUTURE_TABLE,
				Join.innerEq(ProductSQL.PRODUCT_TABLE, FUTURE_ID_FIELD, ProductSQL.ID_FIELD),
				Join.innerEq(FUTURE_CONTRACT_SPECIFICATION_TABLE, FUTURE_CONTRACT_SPECIFICATION_ID_FIELD,
						FCS_ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sql, FCS_NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, SYMBOL_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFutureByContractSpecificationAndSymbol = con.prepareStatement(sql.toString())) {
			stmtGetFutureByContractSpecificationAndSymbol.setString(1, contractSpecification);
			stmtGetFutureByContractSpecificationAndSymbol.setString(2, symbol);
			try (ResultSet results = stmtGetFutureByContractSpecificationAndSymbol.executeQuery()) {
				while (results.next()) {
					if (future == null) {
						future = new Future(results.getString(SYMBOL_FIELD.getName()),
								FutureContractSpecificationSQL.getFutureContractSpecificationById(
										results.getLong(FUTURE_CONTRACT_SPECIFICATION_ID_FIELD.getName())));
					}
					future.setId(results.getLong(FUTURE_ID_FIELD.getName()));
					Timestamp creationTimestamp = results.getTimestamp(CREATION_TIME_FIELD.getName());
					if (creationTimestamp != null) {
						future.setCreationDate(creationTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
					}
					future.setMaturityDate(results.getDate(MATURITY_DATE_FIELD.getName()).toLocalDate());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return future;
	}

	public static Set<Future> getAllFutures() {
		Set<Future> futures = null;
		String sql = TradistaDBUtil.buildSelectQuery(FUTURE_TABLE,
				Join.innerEq(ProductSQL.PRODUCT_TABLE, FUTURE_ID_FIELD, ProductSQL.ID_FIELD));

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllFutures = con.prepareStatement(sql);
				ResultSet results = stmtGetAllFutures.executeQuery()) {
			while (results.next()) {
				if (futures == null) {
					futures = new HashSet<>();
				}
				Future future = new Future(results.getString(SYMBOL_FIELD.getName()), FutureContractSpecificationSQL
						.getFutureContractSpecificationById(results.getLong(FUTURE_CONTRACT_SPECIFICATION_ID_FIELD.getName())));
				future.setId(results.getLong(FUTURE_ID_FIELD.getName()));
				Timestamp creationTimestamp = results.getTimestamp(CREATION_TIME_FIELD.getName());
				if (creationTimestamp != null) {
					future.setCreationDate(creationTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
				}
				future.setMaturityDate(results.getDate(MATURITY_DATE_FIELD.getName()).toLocalDate());
				futures.add(future);
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return futures;
	}

	public static long saveFuture(Future future) {
		long productId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveProduct = (future.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, ProductSQL.PRODUCT_TABLE,
								ProductSQL.PRODUCT_FIELDS_FOR_INSERT)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ProductSQL.ID_FIELD,
								ProductSQL.PRODUCT_TABLE, ProductSQL.PRODUCT_FIELDS_FOR_UPDATE);
				PreparedStatement stmtSaveFuture = (future.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, FUTURE_TABLE,
								FUTURE_CONTRACT_SPECIFICATION_ID_FIELD, SYMBOL_FIELD, MATURITY_DATE_FIELD,
								FUTURE_ID_FIELD)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, FUTURE_ID_FIELD, FUTURE_TABLE,
								FUTURE_CONTRACT_SPECIFICATION_ID_FIELD, SYMBOL_FIELD, MATURITY_DATE_FIELD)) {
			if (future.getId() == 0) {
				stmtSaveProduct.setTimestamp(1,
						Timestamp.from(future.getCreationTime() != null ? future.getCreationTime() : Instant.now()));
				stmtSaveProduct.setTimestamp(2,
						Timestamp.from(future.getLastUpdateTime() != null ? future.getLastUpdateTime() : Instant.now()));
				stmtSaveProduct.setLong(3, future.getExchange().getId());
			} else {
				stmtSaveProduct.setTimestamp(1, Timestamp.from(Instant.now()));
				stmtSaveProduct.setLong(2, future.getExchange().getId());
				stmtSaveProduct.setLong(3, future.getId());
			}
			stmtSaveProduct.executeUpdate();

			if (future.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveProduct.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						productId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating product failed, no generated key obtained.");
					}
				}
			} else {
				productId = future.getId();
			}

			stmtSaveFuture.setLong(1, future.getContractSpecification().getId());
			stmtSaveFuture.setString(2, future.getSymbol());
			stmtSaveFuture.setDate(3, Date.valueOf(future.getMaturityDate()));
			stmtSaveFuture.setLong(4, productId);
			stmtSaveFuture.executeUpdate();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		future.setId(productId);
		return productId;
	}

}