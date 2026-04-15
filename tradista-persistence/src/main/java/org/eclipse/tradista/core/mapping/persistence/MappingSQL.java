package org.eclipse.tradista.core.mapping.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.VALUE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PROCESSING_ORG_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet.Mapping;
import org.eclipse.tradista.core.mapping.model.MappingType;
import org.springframework.util.CollectionUtils;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

public class MappingSQL {

	private static final Field VALUE_FIELD = new Field(VALUE);
	private static final Field MAPPED_VALUE_FIELD = new Field("MAPPED_VALUE");
	private static final Field INTERFACE_MAPPING_SET_ID_FIELD = new Field("INTERFACE_MAPPING_SET_ID");

	private static final Field ID_FIELD = new Field(ID);
	private static final Field INTERFACE_NAME_FIELD = new Field("INTERFACE_NAME");
	private static final Field MAPPING_TYPE_FIELD = new Field("MAPPING_TYPE");
	private static final Field IS_INCOMING_FIELD = new Field("IS_INCOMING");
	private static final Field PROCESSING_ORG_ID_FIELD = new Field(PROCESSING_ORG_ID);

	private static final Field[] MAPPING_FIELDS = { VALUE_FIELD, MAPPED_VALUE_FIELD, INTERFACE_MAPPING_SET_ID_FIELD };

	private static final Field[] INTERFACE_MAPPING_SET_FIELDS = { ID_FIELD, INTERFACE_NAME_FIELD, MAPPING_TYPE_FIELD,
			IS_INCOMING_FIELD, PROCESSING_ORG_ID_FIELD };

	private static final Field[] INTERFACE_MAPPING_SET_FIELDS_FOR_INSERT = { INTERFACE_NAME_FIELD, MAPPING_TYPE_FIELD,
			IS_INCOMING_FIELD, PROCESSING_ORG_ID_FIELD };

	public static final Table MAPPING_TABLE = new Table("MAPPING", MAPPING_FIELDS);

	public static final Table INTERFACE_MAPPING_SET_TABLE = new Table("INTERFACE_MAPPING_SET",
			INTERFACE_MAPPING_SET_FIELDS);

	public static final Join INTERFACE_MAPPING_SET_AND_MAPPING_INNER_JOIN = Join.innerEq(INTERFACE_MAPPING_SET_TABLE,
			ID_FIELD, INTERFACE_MAPPING_SET_ID_FIELD);

	private static final String SELECT_QUERY = TradistaDBUtil.buildSelectQuery(MAPPING_TABLE,
			INTERFACE_MAPPING_SET_AND_MAPPING_INNER_JOIN);

	public static String getMappingValue(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction, String value, long poId) {
		String mappedValue = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);
		if (StringUtils.isEmpty(interfaceName)) {
			TradistaDBUtil.addIsNullFilter(sqlQuery, INTERFACE_NAME_FIELD);
		} else {
			TradistaDBUtil.addParameterizedFilter(sqlQuery, INTERFACE_NAME_FIELD);
		}
		TradistaDBUtil.addParameterizedFilter(sqlQuery, MAPPING_TYPE_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, VALUE_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, IS_INCOMING_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, PROCESSING_ORG_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMappedValueByInterfaceNameMappingTypeDirectionValueAndPoId = con
						.prepareStatement(sqlQuery.toString())) {
			int i = 1;
			if (!StringUtils.isEmpty(interfaceName)) {
				stmtGetMappedValueByInterfaceNameMappingTypeDirectionValueAndPoId.setString(i, interfaceName);
				i++;
			}
			stmtGetMappedValueByInterfaceNameMappingTypeDirectionValueAndPoId.setString(i++, mappingType.name());
			stmtGetMappedValueByInterfaceNameMappingTypeDirectionValueAndPoId.setString(i++, value);
			stmtGetMappedValueByInterfaceNameMappingTypeDirectionValueAndPoId.setBoolean(i++,
					direction.equals(InterfaceMappingSet.Direction.INCOMING));
			stmtGetMappedValueByInterfaceNameMappingTypeDirectionValueAndPoId.setLong(i, poId);
			try (ResultSet results = stmtGetMappedValueByInterfaceNameMappingTypeDirectionValueAndPoId.executeQuery()) {
				while (results.next()) {
					mappedValue = results.getString(MAPPED_VALUE_FIELD.getName());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return mappedValue;
	}

	public static String getOriginalValue(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction, String mappedValue, long poId) {
		String value = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);
		if (StringUtils.isEmpty(interfaceName)) {
			TradistaDBUtil.addIsNullFilter(sqlQuery, INTERFACE_NAME_FIELD);
		} else {
			TradistaDBUtil.addParameterizedFilter(sqlQuery, INTERFACE_NAME_FIELD);
		}
		TradistaDBUtil.addParameterizedFilter(sqlQuery, MAPPING_TYPE_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, MAPPED_VALUE_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, IS_INCOMING_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, PROCESSING_ORG_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMappedValueByInterfaceNameMappingTypeDirectionMappedValueAndPoId = con
						.prepareStatement(sqlQuery.toString())) {
			int i = 1;
			if (!StringUtils.isEmpty(interfaceName)) {
				stmtGetMappedValueByInterfaceNameMappingTypeDirectionMappedValueAndPoId.setString(i, interfaceName);
				i++;
			}
			stmtGetMappedValueByInterfaceNameMappingTypeDirectionMappedValueAndPoId.setString(i++, mappingType.name());
			stmtGetMappedValueByInterfaceNameMappingTypeDirectionMappedValueAndPoId.setString(i++, mappedValue);
			stmtGetMappedValueByInterfaceNameMappingTypeDirectionMappedValueAndPoId.setBoolean(i++,
					direction.equals(InterfaceMappingSet.Direction.INCOMING));
			stmtGetMappedValueByInterfaceNameMappingTypeDirectionMappedValueAndPoId.setLong(i, poId);
			try (ResultSet results = stmtGetMappedValueByInterfaceNameMappingTypeDirectionMappedValueAndPoId
					.executeQuery()) {
				while (results.next()) {
					value = results.getString(VALUE_FIELD.getName());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return value;
	}

	public static long saveInterfaceMappingSet(InterfaceMappingSet ims) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveInterfaceMappingSet = (ims.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, INTERFACE_MAPPING_SET_TABLE,
								INTERFACE_MAPPING_SET_FIELDS_FOR_INSERT)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, INTERFACE_MAPPING_SET_TABLE,
								INTERFACE_MAPPING_SET_FIELDS_FOR_INSERT);
				PreparedStatement stmtDeleteMapping = TradistaDBUtil.buildDeletePreparedStatement(con, MAPPING_TABLE,
						INTERFACE_MAPPING_SET_ID_FIELD);
				PreparedStatement stmtSaveMapping = TradistaDBUtil.buildInsertPreparedStatement(con, MAPPING_TABLE,
						MAPPING_FIELDS)) {
			if (ims.getId() != 0) {
				stmtSaveInterfaceMappingSet.setLong(5, ims.getId());
			}
			stmtSaveInterfaceMappingSet.setString(1, ims.getInterfaceName());
			stmtSaveInterfaceMappingSet.setString(2, ims.getMappingType().name());
			stmtSaveInterfaceMappingSet.setBoolean(3, ims.isIncoming());
			stmtSaveInterfaceMappingSet.setLong(4, ims.getProcessingOrg().getId());

			stmtSaveInterfaceMappingSet.executeUpdate();

			if (ims.getId() != 0) {
				stmtDeleteMapping.setLong(1, ims.getId());
				stmtDeleteMapping.executeUpdate();
			}

			if (ims.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveInterfaceMappingSet.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						ims.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("Creating interface mapping set failed, no generated key obtained.");
					}
				}
			}

			if (!CollectionUtils.isEmpty(ims.getMappings())) {
				for (Mapping mapping : ims.getMappings()) {
					stmtSaveMapping.clearParameters();
					stmtSaveMapping.setString(1, mapping.getValue());
					stmtSaveMapping.setString(2, mapping.getMappedValue());
					stmtSaveMapping.setLong(3, ims.getId());
					stmtSaveMapping.addBatch();
				}
			}

			stmtSaveMapping.executeBatch();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return ims.getId();
	}

	public static InterfaceMappingSet getInterfaceMappingSet(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction) {
		InterfaceMappingSet ims = null;
		final Join mappingSetJoin = Join.leftOuterEq(MAPPING_TABLE, INTERFACE_MAPPING_SET_ID_FIELD,
				ID_FIELD);
		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(
				new Field[] { ID_FIELD, INTERFACE_NAME_FIELD, MAPPING_TYPE_FIELD, IS_INCOMING_FIELD,
						PROCESSING_ORG_ID_FIELD, VALUE_FIELD, MAPPED_VALUE_FIELD },
				INTERFACE_MAPPING_SET_TABLE, mappingSetJoin));
		if (StringUtils.isEmpty(interfaceName)) {
			TradistaDBUtil.addIsNullFilter(sqlQuery, INTERFACE_NAME_FIELD);
		} else {
			TradistaDBUtil.addParameterizedFilter(sqlQuery, INTERFACE_NAME_FIELD);
		}
		TradistaDBUtil.addParameterizedFilter(sqlQuery, MAPPING_TYPE_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, IS_INCOMING_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetInterfaceMappingSetByInterfaceNameMappingTypeAndPo = con
						.prepareStatement(sqlQuery.toString())) {
			int i = 1;
			if (!StringUtils.isEmpty(interfaceName)) {
				stmtGetInterfaceMappingSetByInterfaceNameMappingTypeAndPo.setString(i, interfaceName);
				i++;
			}
			stmtGetInterfaceMappingSetByInterfaceNameMappingTypeAndPo.setString(i++, mappingType.name());
			stmtGetInterfaceMappingSetByInterfaceNameMappingTypeAndPo.setBoolean(i,
					direction.equals(InterfaceMappingSet.Direction.INCOMING));
			ResultSet results = stmtGetInterfaceMappingSetByInterfaceNameMappingTypeAndPo.executeQuery();
			while (results.next()) {
				if (ims == null) {
					ims = new InterfaceMappingSet(interfaceName, mappingType, direction,
							LegalEntitySQL.getLegalEntityById(results.getLong(PROCESSING_ORG_ID_FIELD.getName())));
					ims.setId(results.getLong(ID_FIELD.getName()));
				}
				if (results.getString(VALUE) != null) {
					ims.addMapping(results.getString(VALUE_FIELD.getName()),
							results.getString(MAPPED_VALUE_FIELD.getName()));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return ims;
	}

}