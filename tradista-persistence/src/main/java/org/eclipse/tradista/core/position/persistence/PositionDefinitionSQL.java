package org.eclipse.tradista.core.position.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.pricing.persistence.PricingParameterSQL;
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

public class PositionDefinitionSQL {

	public static long savePositionDefinition(PositionDefinition positionDefinition) {
		// 1. Check if the position definition already exists
		boolean exists = positionDefinition.getId() != 0;
		long positionDefinitionId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSavePositionDefinition = (!exists) ? con.prepareStatement(
						"INSERT INTO POSITION_DEFINITION(NAME, PRICING_PARAMETER_ID, BOOK_ID, PRODUCT_TYPE, PRODUCT_ID, COUNTERPARTY_ID, CURRENCY_ID, IS_REAL_TIME, PROCESSING_ORG_ID) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE POSITION_DEFINITION SET PRICING_PARAMETER_ID=?, BOOK_ID=?, PRODUCT_TYPE=?, PRODUCT_ID=?, COUNTERPARTY_ID=?, CURRENCY_ID=?, IS_REAL_TIME=?, PROCESSING_ORG_ID=? WHERE ID=?")) {
			if (!exists) {
				// 2. If the position definition doesn't exist, save it
				stmtSavePositionDefinition.setString(1, positionDefinition.getName());
				stmtSavePositionDefinition.setLong(2, positionDefinition.getPricingParameter().getId());
				stmtSavePositionDefinition.setLong(3, positionDefinition.getBook().getId());
				if (positionDefinition.getProductType() != null) {
					stmtSavePositionDefinition.setString(4, positionDefinition.getProductType());
				} else {
					stmtSavePositionDefinition.setNull(4, java.sql.Types.BIGINT);
				}
				if (positionDefinition.getProduct() != null) {
					stmtSavePositionDefinition.setLong(5, positionDefinition.getProduct().getId());
				} else {
					stmtSavePositionDefinition.setNull(5, java.sql.Types.BIGINT);
				}
				if (positionDefinition.getCounterparty() != null) {
					stmtSavePositionDefinition.setLong(6, positionDefinition.getCounterparty().getId());
				} else {
					stmtSavePositionDefinition.setNull(6, java.sql.Types.BIGINT);
				}
				stmtSavePositionDefinition.setLong(7, positionDefinition.getCurrency().getId());
				stmtSavePositionDefinition.setBoolean(8, positionDefinition.isRealTime());
				if (positionDefinition.getProcessingOrg() == null) {
					stmtSavePositionDefinition.setNull(9, Types.BIGINT);
				} else {
					stmtSavePositionDefinition.setLong(9, positionDefinition.getProcessingOrg().getId());
				}
				stmtSavePositionDefinition.executeUpdate();
				try (ResultSet generatedKeys = stmtSavePositionDefinition.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						positionDefinitionId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating position definition failed, no generated key obtained.");
					}
				}
			} else {
				// The position definition exists, so we update it
				stmtSavePositionDefinition.setLong(1, positionDefinition.getPricingParameter().getId());
				stmtSavePositionDefinition.setLong(2, positionDefinition.getBook().getId());
				stmtSavePositionDefinition.setString(3, positionDefinition.getProductType());
				if (positionDefinition.getProduct() != null) {
					stmtSavePositionDefinition.setLong(4, positionDefinition.getProduct().getId());
				} else {
					stmtSavePositionDefinition.setNull(4, java.sql.Types.BIGINT);
				}
				if (positionDefinition.getCounterparty() != null) {
					stmtSavePositionDefinition.setLong(5, positionDefinition.getCounterparty().getId());
				} else {
					stmtSavePositionDefinition.setNull(5, java.sql.Types.BIGINT);
				}
				stmtSavePositionDefinition.setLong(6, positionDefinition.getCurrency().getId());
				stmtSavePositionDefinition.setBoolean(7, positionDefinition.isRealTime());
				if (positionDefinition.getProcessingOrg() == null) {
					stmtSavePositionDefinition.setNull(8, Types.BIGINT);
				} else {
					stmtSavePositionDefinition.setLong(8, positionDefinition.getProcessingOrg().getId());
				}
				stmtSavePositionDefinition.setLong(9, positionDefinition.getId());
				stmtSavePositionDefinition.executeUpdate();
				positionDefinitionId = positionDefinition.getId();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		positionDefinition.setId(positionDefinitionId);
		return positionDefinitionId;
	}

	public static boolean deletePositionDefinition(String positionDefinitionName) {
		boolean deleted = false;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeletePositionDefinition = con
						.prepareStatement("DELETE FROM POSITION_DEFINITION WHERE NAME = ? ")) {
			stmtDeletePositionDefinition.setString(1, positionDefinitionName);
			stmtDeletePositionDefinition.executeUpdate();
			deleted = true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return deleted;
	}

	public static Set<String> getAllPositionDefinitionNames() {
		Set<String> positionDefinitionNames = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllPositionDefinitionNames = con
						.prepareStatement("SELECT NAME FROM POSITION_DEFINITION");
				ResultSet results = stmtGetAllPositionDefinitionNames.executeQuery()) {
			while (results.next()) {
				if (positionDefinitionNames == null) {
					positionDefinitionNames = new HashSet<String>();
				}
				positionDefinitionNames.add(results.getString("name"));
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinitionNames;
	}

	public static Set<PositionDefinition> getAllPositionDefinitions() {
		Set<PositionDefinition> positionDefinitions = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllPositionDefinitionNames = con
						.prepareStatement("SELECT * FROM POSITION_DEFINITION");
				ResultSet results = stmtGetAllPositionDefinitionNames.executeQuery()) {
			while (results.next()) {
				if (positionDefinitions == null) {
					positionDefinitions = new HashSet<PositionDefinition>();
				}
				long poId = results.getLong("processing_org_id");
				LegalEntity processingOrg = null;
				if (poId > 0) {
					processingOrg = LegalEntitySQL.getLegalEntityById(poId);
				}
				PositionDefinition posDef = new PositionDefinition(results.getString("name"), processingOrg);
				posDef.setId(results.getLong("id"));
				posDef.setBook(BookSQL.getBookById(results.getLong("book_id")));
				posDef.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
				posDef.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
				posDef.setPricingParameter(
						PricingParameterSQL.getPricingParameterById(results.getLong("pricing_parameter_id")));
				posDef.setProduct(ProductSQL.getProductById(results.getLong("product_id")));
				posDef.setProductType(results.getString("product_type"));
				posDef.setRealTime(results.getBoolean("is_real_time"));
				positionDefinitions.add(posDef);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinitions;
	}

	public static PositionDefinition getPositionDefinitionByName(String positionDefinitionName) {
		PositionDefinition positionDefinition = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPositionDefinitionByName = con
						.prepareStatement("SELECT * FROM POSITION_DEFINITION WHERE NAME = ?")) {
			stmtGetPositionDefinitionByName.setString(1, positionDefinitionName);
			try (ResultSet results = stmtGetPositionDefinitionByName.executeQuery()) {
				while (results.next()) {
					if (positionDefinition == null) {
						long poId = results.getLong("processing_org_id");
						LegalEntity processingOrg = null;
						if (poId > 0) {
							processingOrg = LegalEntitySQL.getLegalEntityById(poId);
						}
						positionDefinition = new PositionDefinition(results.getString("name"), processingOrg);
						positionDefinition.setId(results.getLong("id"));
						positionDefinition.setBook(BookSQL.getBookById(results.getLong("book_id")));
						positionDefinition
								.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
						positionDefinition.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
						positionDefinition.setPricingParameter(
								PricingParameterSQL.getPricingParameterById(results.getLong("pricing_parameter_id")));
						positionDefinition.setProduct(ProductSQL.getProductById(results.getLong("product_id")));
						positionDefinition.setProductType(results.getString("product_type"));
						positionDefinition.setRealTime(results.getBoolean("is_real_time"));
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return positionDefinition;
	}

	public static PositionDefinition getPositionDefinitionById(long id) {
		PositionDefinition positionDefinition = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPositionDefinitionById = con
						.prepareStatement("SELECT * FROM POSITION_DEFINITION WHERE ID = ?")) {
			stmtGetPositionDefinitionById.setLong(1, id);
			try (ResultSet results = stmtGetPositionDefinitionById.executeQuery()) {
				while (results.next()) {
					if (positionDefinition == null) {
						long poId = results.getLong("processing_org_id");
						LegalEntity processingOrg = null;
						if (poId > 0) {
							processingOrg = LegalEntitySQL.getLegalEntityById(poId);
						}
						positionDefinition = new PositionDefinition(results.getString("name"), processingOrg);
						positionDefinition.setId(results.getLong("id"));
						positionDefinition.setBook(BookSQL.getBookById(results.getLong("book_id")));
						positionDefinition
								.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
						positionDefinition.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
						positionDefinition.setPricingParameter(
								PricingParameterSQL.getPricingParameterById(results.getLong("pricing_parameter_id")));
						positionDefinition.setProduct(ProductSQL.getProductById(results.getLong("product_id")));
						positionDefinition.setProductType(results.getString("product_type"));
						positionDefinition.setRealTime(results.getBoolean("is_real_time"));
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return positionDefinition;
	}

	public static Set<PositionDefinition> getAllRealTimePositionDefinitions() {
		Set<PositionDefinition> positionDefinitions = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllRealTimePositionDefinition = con
						.prepareStatement("SELECT * FROM POSITION_DEFINITION WHERE IS_REAL_TIME = TRUE");
				ResultSet results = stmtGetAllRealTimePositionDefinition.executeQuery()) {
			while (results.next()) {
				if (positionDefinitions == null) {
					positionDefinitions = new HashSet<PositionDefinition>();
				}
				long poId = results.getLong("processing_org_id");
				LegalEntity processingOrg = null;
				if (poId > 0) {
					processingOrg = LegalEntitySQL.getLegalEntityById(poId);
				}
				PositionDefinition posDef = new PositionDefinition(results.getString("name"), processingOrg);
				posDef.setId(results.getLong("id"));
				posDef.setBook(BookSQL.getBookById(results.getLong("book_id")));
				posDef.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong("counterparty_id")));
				posDef.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
				posDef.setPricingParameter(
						PricingParameterSQL.getPricingParameterById(results.getLong("pricing_parameter_id")));
				posDef.setProduct(ProductSQL.getProductById(results.getLong("product_id")));
				posDef.setProductType(results.getString("product_type"));
				posDef.setRealTime(results.getBoolean("is_real_time"));
				positionDefinitions.add(posDef);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinitions;
	}

	public static Set<String> getPositionDefinitionsByPricingParametersSetId(long id) {
		Set<String> positionDefinitionNames = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPositionDefinitionsByPricingParametersSetName = con
						.prepareStatement("SELECT * FROM POSITION_DEFINITION WHERE PRICING_PARAMETER_ID = ?")) {
			stmtGetPositionDefinitionsByPricingParametersSetName.setLong(1, id);
			try (ResultSet results = stmtGetPositionDefinitionsByPricingParametersSetName.executeQuery()) {
				while (results.next()) {
					if (positionDefinitionNames == null) {
						positionDefinitionNames = new HashSet<String>();
					}
					positionDefinitionNames.add(results.getString("name"));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinitionNames;
	}

}