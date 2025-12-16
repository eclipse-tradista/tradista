package org.eclipse.tradista.core.mapping.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.AND;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PROCESSING_ORG_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.WHERE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
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

	private static final String INTERFACE_NAME = "INTERFACE_NAME";
	private static final String MAPPING_TYPE = "MAPPING_TYPE";
	private static final String VALUE = "VALUE";
	private static final String MAPPED_VALUE = "MAPPED_VALUE";
	private static final String IS_INCOMING = "IS_INCOMING";

	public static String getMappingValue(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction, String value, long poId) {
		String mappedValue = null;
		String interfaceNameSQL = INTERFACE_NAME + " = ?";
		if (StringUtils.isEmpty(interfaceName)) {
			interfaceNameSQL = INTERFACE_NAME + " IS NULL";
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMappedValueByInterfaceNameMappingTypeDirectionValueAndPoId = con
						.prepareStatement("SELECT * FROM MAPPING, INTERFACE_MAPPING_SET" + WHERE + interfaceNameSQL
								+ AND + "INTERFACE_MAPPING_SET.ID = MAPPING.INTERFACE_MAPPING_SET_ID" + AND
								+ MAPPING_TYPE + "= ?" + AND + VALUE + "= ? " + AND + IS_INCOMING + "= ?" + AND
								+ PROCESSING_ORG_ID + "= ?")) {
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
					mappedValue = results.getString(MAPPED_VALUE);
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
		String interfaceNameSQL = INTERFACE_NAME + " = ?";
		if (StringUtils.isEmpty(interfaceName)) {
			interfaceNameSQL = INTERFACE_NAME + " IS NULL";
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMappedValueByInterfaceNameMappingTypeDirectionMappedValueAndPoId = con
						.prepareStatement("SELECT * FROM MAPPING, INTERFACE_MAPPING_SET" + WHERE + interfaceNameSQL
								+ AND + "INTERFACE_MAPPING_SET.ID = MAPPING.INTERFACE_MAPPING_SET_ID" + AND
								+ MAPPING_TYPE + "= ?" + AND + MAPPED_VALUE + "= ? " + AND + IS_INCOMING + "= ?" + AND
								+ PROCESSING_ORG_ID + "= ?")) {
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
					value = results.getString(VALUE);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return value;
	}

	public static long saveInterfaceMappingSet(InterfaceMappingSet ims) {
		long interfaceMappingSetId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveInterfaceMappingSet = (ims.getId() == 0)
						? con.prepareStatement(
								"INSERT INTO INTERFACE_MAPPING_SET(" + INTERFACE_NAME + "," + MAPPING_TYPE + ","
										+ IS_INCOMING + "," + PROCESSING_ORG_ID + ") VALUES (?, ?, ?, ?)",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE INTERFACE_MAPPING_SET SET " + INTERFACE_NAME + " = ?," + MAPPING_TYPE + " = ?,"
										+ IS_INCOMING + " = ?," + PROCESSING_ORG_ID + " = ?" + WHERE + ID + " = ?");
				PreparedStatement stmtDeleteMapping = con
						.prepareStatement("DELETE FROM MAPPING WHERE INTERFACE_MAPPING_SET_ID = ?");
				PreparedStatement stmtSaveMapping = con.prepareStatement("INSERT INTO MAPPING VALUES (?, ?, ?)");) {
			if (ims.getId() != 0) {
				stmtSaveInterfaceMappingSet.setLong(5, ims.getId());
			}
			stmtSaveInterfaceMappingSet.setString(1, ims.getInterfaceName());
			stmtSaveInterfaceMappingSet.setString(2, ims.getMappingType().name());
			stmtSaveInterfaceMappingSet.setBoolean(3, ims.isIncoming());
			stmtSaveInterfaceMappingSet.setLong(4, ims.getProcessingOrg().getId());

			stmtSaveInterfaceMappingSet.executeUpdate();

			if (ims.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveInterfaceMappingSet.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						interfaceMappingSetId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating interface mapping set failed, no generated key obtained.");
					}
				}
			} else {
				interfaceMappingSetId = ims.getId();
			}
			if (ims.getId() != 0) {
				stmtDeleteMapping.setLong(1, ims.getId());
				stmtDeleteMapping.executeUpdate();
			}
			if (!CollectionUtils.isEmpty(ims.getMappings())) {
				for (Mapping mapping : ims.getMappings()) {
					stmtSaveMapping.clearParameters();
					stmtSaveMapping.setString(1, mapping.getValue());
					stmtSaveMapping.setString(2, mapping.getMappedValue());
					stmtSaveMapping.setLong(3, interfaceMappingSetId);
					stmtSaveMapping.addBatch();
				}
			}

			stmtSaveMapping.executeBatch();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		ims.setId(interfaceMappingSetId);
		return interfaceMappingSetId;
	}

	public static InterfaceMappingSet getInterfaceMappingSet(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction) {
		InterfaceMappingSet ims = null;
		String interfaceNameSQL = INTERFACE_NAME + " = ?";
		if (StringUtils.isEmpty(interfaceName)) {
			interfaceNameSQL = INTERFACE_NAME + " IS NULL";
			interfaceName = null;
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetInterfaceMappingSetByInterfaceNameMappingTypeAndPo = con
						.prepareStatement("SELECT " + ID + "," + INTERFACE_NAME + "," + MAPPING_TYPE + "," + IS_INCOMING
								+ "," + PROCESSING_ORG_ID + "," + VALUE + "," + MAPPED_VALUE
								+ " FROM INTERFACE_MAPPING_SET LEFT OUTER JOIN MAPPING ON INTERFACE_MAPPING_SET.ID = MAPPING.INTERFACE_MAPPING_SET_ID "
								+ WHERE + interfaceNameSQL + AND + MAPPING_TYPE + " = ?" + AND + IS_INCOMING
								+ "= ?");) {
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
							LegalEntitySQL.getLegalEntityById(results.getLong(PROCESSING_ORG_ID)));
					ims.setId(results.getLong(ID));
				}
				if (results.getString(VALUE) != null) {
					ims.addMapping(results.getString(VALUE), results.getString(MAPPED_VALUE));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return ims;
	}

}