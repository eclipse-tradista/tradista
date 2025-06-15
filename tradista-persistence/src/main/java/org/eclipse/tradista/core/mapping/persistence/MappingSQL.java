package org.eclipse.tradista.core.mapping.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.mapping.model.MappingType;

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

	public static String getMappingValue(String importerName, MappingType mappingType, String value) {
		String mappedValue = null;
		String importerNameSQL = "IMPORTER_NAME = ?";
		if (StringUtils.isEmpty(importerName)) {
			importerNameSQL = "IMPORTER_NAME IS NULL";
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMappedValueByImporterNameMappingTypeAndValue = con.prepareStatement(
						"SELECT * FROM MAPPING WHERE " + importerNameSQL + ", MAPPING_TYPE = ? AND VALUE = ? ")) {
			int i = 1;
			if (!StringUtils.isEmpty(importerName)) {
				stmtGetMappedValueByImporterNameMappingTypeAndValue.setString(i, importerName);
				i++;
			}
			stmtGetMappedValueByImporterNameMappingTypeAndValue.setString(i++, mappingType.name());
			stmtGetMappedValueByImporterNameMappingTypeAndValue.setString(i, value);
			try (ResultSet results = stmtGetMappedValueByImporterNameMappingTypeAndValue.executeQuery()) {
				while (results.next()) {
					mappedValue = results.getString("mapped_value");
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return mappedValue;
	}
	
	public static String getOriginalValue(String importerName, MappingType mappingType, String mappedValue) {
		String value = null;
		String importerNameSQL = "IMPORTER_NAME = ?";
		if (StringUtils.isEmpty(importerName)) {
			importerNameSQL = "IMPORTER_NAME IS NULL";
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMappedValueByImporterNameMappingTypeAndMappedValue = con.prepareStatement(
						"SELECT * FROM MAPPING WHERE " + importerNameSQL + ", MAPPING_TYPE = ? AND MAPPED_VALUE = ? ")) {
			int i = 1;
			if (!StringUtils.isEmpty(importerName)) {
				stmtGetMappedValueByImporterNameMappingTypeAndMappedValue.setString(i, importerName);
				i++;
			}
			stmtGetMappedValueByImporterNameMappingTypeAndMappedValue.setString(i++, mappingType.name());
			stmtGetMappedValueByImporterNameMappingTypeAndMappedValue.setString(i, mappedValue);
			try (ResultSet results = stmtGetMappedValueByImporterNameMappingTypeAndMappedValue.executeQuery()) {
				while (results.next()) {
					value = results.getString("value");
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return value;
	}

}