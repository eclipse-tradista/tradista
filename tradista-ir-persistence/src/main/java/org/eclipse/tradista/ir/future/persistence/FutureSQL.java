package org.eclipse.tradista.ir.future.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
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

	public static Future getFutureById(long id) {

		Future future = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFutureById = con.prepareStatement(
						"SELECT * FROM FUTURE, PRODUCT WHERE FUTURE_ID = ? AND FUTURE.FUTURE_ID = PRODUCT.ID")) {
			stmtGetFutureById.setLong(1, id);
			try (ResultSet results = stmtGetFutureById.executeQuery()) {
				while (results.next()) {
					if (future == null) {
						future = new Future(results.getString("symbol"),
								FutureContractSpecificationSQL.getFutureContractSpecificationById(
										results.getLong("future_contract_specification_id")));
					}
					future.setId(results.getLong("future_id"));
					future.setCreationDate(results.getDate("creation_date").toLocalDate());
					future.setMaturityDate(results.getDate("maturity_date").toLocalDate());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return future;
	}

	public static Future getFutureByContractSpecificationAndSymbol(String contractSpecification, String symbol) {

		Future future = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFutureByContractSpecificationAndSymbol = con.prepareStatement(
						"SELECT * FROM FUTURE, PRODUCT, FUTURE_CONTRACT_SPECIFICATION WHERE NAME = ? AND SYMBOL = ? AND FUTURE.FUTURE_ID = PRODUCT.ID AND FUTURE.FUTURE_CONTRACT_SPECIFICATION_ID = FUTURE_CONTRACT_SPECIFICATION.ID")) {
			stmtGetFutureByContractSpecificationAndSymbol.setString(1, contractSpecification);
			stmtGetFutureByContractSpecificationAndSymbol.setString(2, symbol);
			try (ResultSet results = stmtGetFutureByContractSpecificationAndSymbol.executeQuery()) {
				while (results.next()) {
					if (future == null) {
						future = new Future(results.getString("symbol"),
								FutureContractSpecificationSQL.getFutureContractSpecificationById(
										results.getLong("future_contract_specification_id")));
					}
					future.setId(results.getLong("future_id"));
					future.setCreationDate(results.getDate("creation_date").toLocalDate());
					future.setMaturityDate(results.getDate("maturity_date").toLocalDate());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return future;
	}

	public static Set<Future> getAllFutures() {

		Set<Future> futures = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllFutures = con
						.prepareStatement("SELECT * FROM FUTURE, PRODUCT WHERE FUTURE.FUTURE_ID = PRODUCT.ID");
				ResultSet results = stmtGetAllFutures.executeQuery()) {
			while (results.next()) {
				if (futures == null) {
					futures = new HashSet<>();
				}
				Future future = new Future(results.getString("symbol"), FutureContractSpecificationSQL
						.getFutureContractSpecificationById(results.getLong("future_contract_specification_id")));
				future.setId(results.getLong("future_id"));
				future.setCreationDate(results.getDate("creation_date").toLocalDate());
				future.setMaturityDate(results.getDate("maturity_date").toLocalDate());
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
						? con.prepareStatement("INSERT INTO PRODUCT(CREATION_DATE, EXCHANGE_ID) VALUES (?, ?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement("UPDATE PRODUCT SET CREATION_DATE=?, EXCHANGE_ID=? WHERE ID=? ");
				PreparedStatement stmtSaveFuture = (future.getId() == 0) ? con.prepareStatement(
						"INSERT INTO FUTURE(FUTURE_CONTRACT_SPECIFICATION_ID, SYMBOL, MATURITY_DATE, FUTURE_ID) VALUES (?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE FUTURE SET FUTURE_CONTRACT_SPECIFICATION_ID=?, SYMBOL=?, MATURITY_DATE=? WHERE FUTURE_ID=?")) {
			if (future.getId() != 0) {
				stmtSaveProduct.setLong(3, future.getId());
			}
			stmtSaveProduct.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
			stmtSaveProduct.setLong(2, future.getExchange().getId());
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
			stmtSaveFuture.setDate(3, java.sql.Date.valueOf(future.getMaturityDate()));
			stmtSaveFuture.setLong(4, productId);
			stmtSaveFuture.executeUpdate();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		future.setId(productId);
		return productId;

	}

}