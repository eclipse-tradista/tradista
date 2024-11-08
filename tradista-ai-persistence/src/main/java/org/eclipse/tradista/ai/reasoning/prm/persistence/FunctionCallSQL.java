package org.eclipse.tradista.ai.reasoning.prm.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.tradista.ai.reasoning.prm.model.Constant;
import org.eclipse.tradista.ai.reasoning.prm.model.FunctionCall;
import org.eclipse.tradista.ai.reasoning.prm.model.Type;
import org.eclipse.tradista.ai.reasoning.prm.model.Value;
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

public class FunctionCallSQL {

	public static long saveFunctionCall(FunctionCall functionCall) {
		long functionCallId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveFunctionCall = (functionCall.getId() == 0)
						? con.prepareStatement("INSERT INTO FUNCTION_CALL(FUNCTION_ID) VALUES (?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement("UPDATE FUNCTION SET FUNCTION_ID = ? WHERE ID = ? ");
				PreparedStatement stmtDeleteFunctionCallValue = (functionCall.getId() != 0)
						? con.prepareStatement("DELETE FROM FUNCTION_CALL_VALUE WHERE FUNCTION_ID = ?")
						: null;
				PreparedStatement stmtSaveFunctionCallValue = (functionCall.getParameters() != null
						&& !functionCall.getParameters().isEmpty()) ? con.prepareStatement(
								"INSERT INTO FUNCTION_CALL_VALUE(FUNCTION_CALL_ID, VALUE_ID, POSITION) VALUES (?, ?, ?)")
								: null;) {
			if (functionCall.getId() != 0) {
				stmtSaveFunctionCall.setLong(2, functionCall.getId());
			}
			stmtSaveFunctionCall.setLong(1, functionCall.getFunction().getId());
			stmtSaveFunctionCall.executeUpdate();

			if (functionCall.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveFunctionCall.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						functionCallId = generatedKeys.getLong(1);
						functionCall.setId(functionCallId);
					} else {
						throw new SQLException("Creating function call failed, no generated key obtained.");
					}
				}
			} else {
				functionCallId = functionCall.getId();
			}

			if (functionCall.getId() != 0) {
				stmtDeleteFunctionCallValue.setLong(1, functionCall.getId());
				stmtDeleteFunctionCallValue.executeUpdate();
			}

			if (functionCall.getParameters() != null && !functionCall.getParameters().isEmpty()) {
				int position = 1;
				for (Value value : functionCall.getParameters()) {
					stmtSaveFunctionCallValue.setLong(1, functionCallId);
					stmtSaveFunctionCallValue.setString(2, value.getName());
					stmtSaveFunctionCallValue.setLong(3, position);
					stmtSaveFunctionCallValue.addBatch();
					position++;
				}
				stmtSaveFunctionCallValue.executeBatch();
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return functionCallId;
	}

	public static boolean deleteFunctionCall(long functionCallId) {
		boolean bDeleted = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteFunctionCallValue = con
						.prepareStatement("DELETE FROM FUNCTION_CALL_VALUE WHERE FUNCTION_CALL_ID = ?) ");
				PreparedStatement stmtDeleteFunctionCall = con
						.prepareStatement("DELETE FROM FUNCTION_CALL WHERE FUNCTION_CALL_ID = ?) ")) {
			stmtDeleteFunctionCallValue.setLong(1, functionCallId);
			stmtDeleteFunctionCallValue.executeUpdate();

			stmtDeleteFunctionCall.setLong(1, functionCallId);
			stmtDeleteFunctionCall.executeUpdate();

			bDeleted = true;
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			bDeleted = false;
			throw new TradistaTechnicalException(sqle);
		}
		return bDeleted;
	}

	public static FunctionCall getFunctionCallById(long id) {
		FunctionCall functionCall = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFunctionCallById = con
						.prepareStatement("SELECT * FROM FUNCTION_CALL WHERE ID = ?");
				PreparedStatement stmtGetFunctionCallValuesByFunctionCallId = con.prepareStatement(
						"SELECT * FROM FUNCTION_CALL_VALUE WHERE FUNCTION_CALL_ID = ?  LEFT OUTER JOIN VARIABLE ON VALUE_ID = VARIABLE_ID LEFT OUTER JOIN CONSTANT ON VALUE_ID = CONSTANT_ID ORDER BY POSITION")) {
			stmtGetFunctionCallById.setLong(1, id);
			try (ResultSet results = stmtGetFunctionCallById.executeQuery()) {
				while (results.next()) {
					functionCall = new FunctionCall();
					functionCall.setId(results.getLong("id"));
					functionCall.setFunction(FunctionSQL.getFunctionById(results.getLong("function_id")));
				}
			}

			stmtGetFunctionCallValuesByFunctionCallId.setLong(1, id);
			try (ResultSet results = stmtGetFunctionCallValuesByFunctionCallId.executeQuery()) {
				Set<Value> parameters = new LinkedHashSet<Value>();
				while (results.next()) {
					Value value;
					if (results.getLong("variable_id") != 0) {
						value = new Variable(new Type(results.getString("type")), results.getString("name"));
						value.setId(results.getLong("variable_id"));
					} else {
						value = new Constant(new Type(results.getString("type")), results.getString("name"));
						value.setId(results.getLong("constant_id"));
					}
					parameters.add(value);
				}
				functionCall.setParameters(parameters);
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return functionCall;
	}

}