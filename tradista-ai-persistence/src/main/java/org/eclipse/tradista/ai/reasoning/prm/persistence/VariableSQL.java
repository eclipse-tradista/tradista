package org.eclipse.tradista.ai.reasoning.prm.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.tradista.ai.reasoning.prm.model.Variable;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public class VariableSQL {

	public static long saveVariable(Variable variable) {
		long variableId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveValue = (variable.getId() == 0)
						? con.prepareStatement("INSERT INTO VALUE(TYPE, NAME) VALUES (?, ?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement("UPDATE VALUE SET TYPE = ?,  NAME = ? WHERE ID = ? ");
				PreparedStatement stmtSaveVariable = (variable.getId() == 0)
						? con.prepareStatement("INSERT INTO VARIABLE(VARIABLE_ID) VALUES (?) ")
						: null) {
			if (variable.getId() != 0) {
				stmtSaveValue.setLong(3, variable.getId());
			}
			stmtSaveValue.setString(1, variable.getType().getName());
			stmtSaveValue.setString(2, variable.getName());
			stmtSaveValue.executeUpdate();

			if (variable.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveValue.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						variableId = generatedKeys.getLong(1);
						variable.setId(variableId);
					} else {
						throw new SQLException("Creating variable failed, no generated key obtained.");
					}
				}
			} else {
				variableId = variable.getId();
			}

			if (variable.getId() == 0) {
				stmtSaveVariable.setLong(1, variable.getId());
				stmtSaveVariable.executeUpdate();
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return variableId;
	}

}