package org.eclipse.tradista.ai.reasoning.prm.probability.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.tradista.ai.reasoning.prm.model.Constant;
import org.eclipse.tradista.ai.reasoning.prm.model.Type;
import org.eclipse.tradista.ai.reasoning.prm.model.Value;
import org.eclipse.tradista.ai.reasoning.prm.model.Variable;
import org.eclipse.tradista.ai.reasoning.prm.persistence.FunctionCallSQL;
import org.eclipse.tradista.ai.reasoning.prm.probability.model.Operator;
import org.eclipse.tradista.ai.reasoning.prm.probability.model.Predicate;
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

public class PredicateSQL {

	public static long savePredicate(Predicate predicate) {
		long predicateId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSavePredicate = (predicate.getId() == 0) ? con.prepareStatement(
						"INSERT INTO PREDICATE(FUNCTION_CALL_ID, OPERATOR, VALUE_ID, RESULT) VALUES (?, ?, ?, ?) ",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE PREDICATE SET FUNCTION_CALL_ID = ?,  OPERATOR = ?, VALUE_ID = ?, RESULT = ? WHERE ID = ? ")) {
			if (predicate.getId() != 0) {
				stmtSavePredicate.setLong(5, predicate.getId());
			}
			FunctionCallSQL.saveFunctionCall(predicate.getFunctionCall());
			stmtSavePredicate.setLong(1, predicate.getFunctionCall().getId());
			stmtSavePredicate.setString(2, predicate.getOperator().name());
			stmtSavePredicate.setLong(3, predicate.getValue().getId());
			stmtSavePredicate.setBoolean(4, predicate.isResult());
			stmtSavePredicate.executeUpdate();

			if (predicate.getId() == 0) {
				try (ResultSet generatedKeys = stmtSavePredicate.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						predicateId = generatedKeys.getLong(1);
						predicate.setId(predicateId);
					} else {
						throw new SQLException("Creating predicate failed, no generated key obtained.");
					}
				}
			} else {
				predicateId = predicate.getId();
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return predicateId;
	}

	public static boolean deletePredicate(Predicate predicate) {
		boolean bDeleted = FunctionCallSQL.deleteFunctionCall(predicate.getFunctionCall().getId());

		if (bDeleted) {
			try (Connection con = TradistaDB.getConnection();
					PreparedStatement stmtDeletePredicate = con
							.prepareStatement("DELETE FROM PREDICATE WHERE PREDICATE_ID = ?) ")) {

				stmtDeletePredicate.setLong(1, predicate.getId());
				stmtDeletePredicate.executeUpdate();

				bDeleted = true;
			} catch (SQLException sqle) {
				// TODO Manage logs
				sqle.printStackTrace();
				throw new TradistaTechnicalException(sqle);
			}
		}
		return bDeleted;
	}

	public static Predicate getPredicateById(long id) {
		Predicate predicate = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPredicateById = con.prepareStatement(
						"SELECT * FROM PREDICATE WHERE ID = ? LEFT OUTER JOIN VARIABLE ON VALUE_ID = VARIABLE_ID LEFT OUTER JOIN CONSTANT ON VALUE_ID = CONSTANT_ID")) {
			stmtGetPredicateById.setLong(1, id);
			try (ResultSet results = stmtGetPredicateById.executeQuery()) {

				while (results.next()) {
					predicate = new Predicate();
					predicate.setId(results.getLong("id"));
					predicate.setFunctionCall(FunctionCallSQL.getFunctionCallById(results.getLong("function_call_id")));
					predicate.setOperator(Operator.valueOf(results.getString("operator")));
					predicate.setResult(results.getBoolean("result"));
					Value value = null;
					if (results.getLong("variable_id") != 0) {
						value = new Variable(new Type(results.getString("type")), results.getString("name"));
						value.setId(results.getLong("variable_id"));
					} else {
						value = new Constant(new Type(results.getString("type")), results.getString("name"));
						value.setId(results.getLong("constant_id"));
					}
					predicate.setValue(value);
				}
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return predicate;
	}
}