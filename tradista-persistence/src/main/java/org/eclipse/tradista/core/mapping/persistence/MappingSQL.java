package org.eclipse.tradista.core.mapping.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;

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

	public static String getMappingValue(String interfaceName, String mappingType, String value) {
		String mappedValue = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMappedValueByInterfaceNameMappingTypeAndValue = con.prepareStatement(
						"SELECT * FROM MAPPING WHERE INTERFACE_NAME = ?, MAPPING_TYPE = ? AND VALUE = ? ")) {
			stmtGetMappedValueByInterfaceNameMappingTypeAndValue.setString(1, interfaceName);
			stmtGetMappedValueByInterfaceNameMappingTypeAndValue.setString(2, mappingType);
			stmtGetMappedValueByInterfaceNameMappingTypeAndValue.setString(3, value);
			try (ResultSet results = stmtGetMappedValueByInterfaceNameMappingTypeAndValue.executeQuery()) {
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

}