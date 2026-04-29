package org.eclipse.tradista.core.position.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.BOOK_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.COUNTERPARTY_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CURRENCY_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.IS_REAL_TIME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRICING_PARAMETER_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PROCESSING_ORG_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRODUCT_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRODUCT_TYPE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
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

	private static final Field NAME_FIELD = new Field(NAME);
	private static final Field PRICING_PARAMETER_ID_FIELD = new Field(PRICING_PARAMETER_ID);
	private static final Field BOOK_ID_FIELD = new Field(BOOK_ID);
	private static final Field PRODUCT_TYPE_FIELD = new Field(PRODUCT_TYPE);
	private static final Field PRODUCT_ID_FIELD = new Field(PRODUCT_ID);
	private static final Field COUNTERPARTY_ID_FIELD = new Field(COUNTERPARTY_ID);
	private static final Field CURRENCY_ID_FIELD = new Field(CURRENCY_ID);
	private static final Field IS_REAL_TIME_FIELD = new Field(IS_REAL_TIME);
	private static final Field PROCESSING_ORG_ID_FIELD = new Field(PROCESSING_ORG_ID);
	private static final Field ID_FIELD = new Field(ID);

	private static final Field[] FIELDS = { NAME_FIELD, PRICING_PARAMETER_ID_FIELD, BOOK_ID_FIELD, PRODUCT_TYPE_FIELD,
			PRODUCT_ID_FIELD, COUNTERPARTY_ID_FIELD, CURRENCY_ID_FIELD, IS_REAL_TIME_FIELD, PROCESSING_ORG_ID_FIELD,
			ID_FIELD };

	private static final Field[] FIELDS_FOR_INSERT = { NAME_FIELD, PRICING_PARAMETER_ID_FIELD, BOOK_ID_FIELD,
			PRODUCT_TYPE_FIELD, PRODUCT_ID_FIELD, COUNTERPARTY_ID_FIELD, CURRENCY_ID_FIELD, IS_REAL_TIME_FIELD,
			PROCESSING_ORG_ID_FIELD };

	private static final Field[] FIELDS_FOR_UPDATE = { PRICING_PARAMETER_ID_FIELD, BOOK_ID_FIELD, PRODUCT_TYPE_FIELD,
			PRODUCT_ID_FIELD, COUNTERPARTY_ID_FIELD, CURRENCY_ID_FIELD, IS_REAL_TIME_FIELD, PROCESSING_ORG_ID_FIELD };

	public static final Table TABLE = new Table("POSITION_DEFINITION", FIELDS);

	public static long savePositionDefinition(PositionDefinition positionDefinition) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSavePositionDefinition = (positionDefinition.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, TABLE, FIELDS_FOR_INSERT)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, TABLE, FIELDS_FOR_UPDATE)) {
			if (positionDefinition.getId() == 0) {
				stmtSavePositionDefinition.setString(1, positionDefinition.getName());
				stmtSavePositionDefinition.setLong(2, positionDefinition.getPricingParameter().getId());
				stmtSavePositionDefinition.setLong(3, positionDefinition.getBook().getId());
				if (positionDefinition.getProductType() != null) {
					stmtSavePositionDefinition.setString(4, positionDefinition.getProductType());
				} else {
					stmtSavePositionDefinition.setNull(4, java.sql.Types.VARCHAR);
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
			} else {
				stmtSavePositionDefinition.setLong(1, positionDefinition.getPricingParameter().getId());
				stmtSavePositionDefinition.setLong(2, positionDefinition.getBook().getId());
				if (positionDefinition.getProductType() != null) {
					stmtSavePositionDefinition.setString(3, positionDefinition.getProductType());
				} else {
					stmtSavePositionDefinition.setNull(3, java.sql.Types.VARCHAR);
				}
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
			}
			stmtSavePositionDefinition.executeUpdate();
			if (positionDefinition.getId() == 0) {
				try (ResultSet generatedKeys = stmtSavePositionDefinition.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						positionDefinition.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("Creating position definition failed, no generated key obtained.");
					}
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinition.getId();
	}

	public static boolean deletePositionDefinition(String positionDefinitionName) {
		boolean deleted = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeletePositionDefinition = TradistaDBUtil.buildDeletePreparedStatement(con, TABLE, NAME_FIELD)) {
			stmtDeletePositionDefinition.setString(1, positionDefinitionName);
			stmtDeletePositionDefinition.executeUpdate();
			deleted = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return deleted;
	}

	public static Set<String> getAllPositionDefinitionNames() {
		Set<String> positionDefinitionNames = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllPositionDefinitionNames = con
						.prepareStatement(TradistaDBUtil.buildSelectQuery(NAME_FIELD, TABLE));
				ResultSet results = stmtGetAllPositionDefinitionNames.executeQuery()) {
			while (results.next()) {
				if (positionDefinitionNames == null) {
					positionDefinitionNames = new HashSet<>();
				}
				positionDefinitionNames.add(results.getString(NAME_FIELD.getName()));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinitionNames;
	}

	public static Set<PositionDefinition> getAllPositionDefinitions() {
		Set<PositionDefinition> positionDefinitions = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllPositionDefinitions = con
						.prepareStatement(TradistaDBUtil.buildSelectQuery(TABLE));
				ResultSet results = stmtGetAllPositionDefinitions.executeQuery()) {
			while (results.next()) {
				if (positionDefinitions == null) {
					positionDefinitions = new HashSet<>();
				}
				positionDefinitions.add(buildPositionDefinition(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinitions;
	}

	public static Set<PositionDefinition> getPositionDefinitionsByPoId(long poId) {
		Set<PositionDefinition> positionDefinitions = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PROCESSING_ORG_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPositionDefinitionsByPoId = con.prepareStatement(sql.toString())) {
			stmtGetPositionDefinitionsByPoId.setLong(1, poId);
			try (ResultSet results = stmtGetPositionDefinitionsByPoId.executeQuery()) {
				while (results.next()) {
					if (positionDefinitions == null) {
						positionDefinitions = new HashSet<>();
					}
					positionDefinitions.add(buildPositionDefinition(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinitions;
	}

	public static Set<PositionDefinition> getPositionDefinitionsByName(String positionDefinitionName) {
		Set<PositionDefinition> positionDefinitions = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPositionDefinitionsByName = con.prepareStatement(sql.toString())) {
			stmtGetPositionDefinitionsByName.setString(1, positionDefinitionName);
			try (ResultSet results = stmtGetPositionDefinitionsByName.executeQuery()) {
				while (results.next()) {
					if (positionDefinitions == null) {
						positionDefinitions = new HashSet<>();
					}
					positionDefinitions.add(buildPositionDefinition(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinitions;
	}

	public static PositionDefinition getPositionDefinitionByName(String positionDefinitionName) {
		PositionDefinition positionDefinition = null;
		Set<PositionDefinition> positionDefinitions = getPositionDefinitionsByName(positionDefinitionName);
		if (positionDefinitions != null && !positionDefinitions.isEmpty()) {
			positionDefinition = positionDefinitions.iterator().next();
		}
		return positionDefinition;
	}

	public static boolean deletePositionDefinition(String name, long poId) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeletePositionDefinition = TradistaDBUtil.buildDeletePreparedStatement(con, TABLE,
						NAME_FIELD, PROCESSING_ORG_ID_FIELD)) {
			stmtDeletePositionDefinition.setString(1, name);
			stmtDeletePositionDefinition.setLong(2, poId);
			return stmtDeletePositionDefinition.executeUpdate() > 0;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static PositionDefinition getPositionDefinitionByNameAndPoId(String name, long poId) {
		PositionDefinition positionDefinition = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		if (poId > 0) {
			TradistaDBUtil.addParameterizedFilter(sql, PROCESSING_ORG_ID_FIELD);
		} else {
			sql.append(" AND " + PROCESSING_ORG_ID_FIELD + " IS NULL");
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPositionDefinitionByNameAndPoId = con.prepareStatement(sql.toString())) {
			stmtGetPositionDefinitionByNameAndPoId.setString(1, name);
			if (poId > 0) {
				stmtGetPositionDefinitionByNameAndPoId.setLong(2, poId);
			}
			try (ResultSet results = stmtGetPositionDefinitionByNameAndPoId.executeQuery()) {
				while (results.next()) {
					positionDefinition = buildPositionDefinition(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinition;
	}

	public static PositionDefinition getPositionDefinitionById(long id) {
		PositionDefinition positionDefinition = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPositionDefinitionById = con.prepareStatement(sql.toString())) {
			stmtGetPositionDefinitionById.setLong(1, id);
			try (ResultSet results = stmtGetPositionDefinitionById.executeQuery()) {
				while (results.next()) {
					positionDefinition = buildPositionDefinition(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinition;
	}

	public static Set<PositionDefinition> getAllRealTimePositionDefinitions() {
		Set<PositionDefinition> positionDefinitions = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, IS_REAL_TIME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllRealTimePositionDefinitions = con.prepareStatement(sql.toString())) {
			stmtGetAllRealTimePositionDefinitions.setBoolean(1, true);
			try (ResultSet results = stmtGetAllRealTimePositionDefinitions.executeQuery()) {
				while (results.next()) {
					if (positionDefinitions == null) {
						positionDefinitions = new HashSet<>();
					}
					positionDefinitions.add(buildPositionDefinition(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinitions;
	}

	public static Set<String> getPositionDefinitionsByPricingParametersSetId(long id) {
		Set<String> positionDefinitionNames = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(NAME_FIELD, TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PRICING_PARAMETER_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPositionDefinitionsByPricingParametersSetName = con
						.prepareStatement(sql.toString())) {
			stmtGetPositionDefinitionsByPricingParametersSetName.setLong(1, id);
			try (ResultSet results = stmtGetPositionDefinitionsByPricingParametersSetName.executeQuery()) {
				while (results.next()) {
					if (positionDefinitionNames == null) {
						positionDefinitionNames = new HashSet<>();
					}
					positionDefinitionNames.add(results.getString(NAME_FIELD.getName()));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return positionDefinitionNames;
	}

	private static PositionDefinition buildPositionDefinition(ResultSet results) throws SQLException {
		long poId = results.getLong(PROCESSING_ORG_ID_FIELD.getName());
		LegalEntity processingOrg = null;
		if (poId > 0) {
			processingOrg = LegalEntitySQL.getLegalEntityById(poId);
		}
		PositionDefinition posDef = new PositionDefinition(results.getString(NAME_FIELD.getName()), processingOrg);
		posDef.setId(results.getLong(ID_FIELD.getName()));
		posDef.setBook(BookSQL.getBookById(results.getLong(BOOK_ID_FIELD.getName())));
		posDef.setCounterparty(LegalEntitySQL.getLegalEntityById(results.getLong(COUNTERPARTY_ID_FIELD.getName())));
		posDef.setCurrency(CurrencySQL.getCurrencyById(results.getLong(CURRENCY_ID_FIELD.getName())));
		posDef.setPricingParameter(
				PricingParameterSQL.getPricingParameterById(results.getLong(PRICING_PARAMETER_ID_FIELD.getName())));
		posDef.setProduct(ProductSQL.getProductById(results.getLong(PRODUCT_ID_FIELD.getName())));
		posDef.setProductType(results.getString(PRODUCT_TYPE_FIELD.getName()));
		posDef.setRealTime(results.getBoolean(IS_REAL_TIME_FIELD.getName()));
		return posDef;
	}

}