package org.eclipse.tradista.ai.reasoning.prm.probability.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.eclipse.tradista.ai.reasoning.prm.probability.model.IfExpression;
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

public class IfExpressionSQL {

	public static boolean deleteIfExpressions(Set<IfExpression> ifExpressions) {
		if (ifExpressions != null && ifExpressions.isEmpty()) {
			return true;
		}
		boolean bDeleted = false;

		for (IfExpression expression : ifExpressions) {
			ProbabilityDistributionSQL.deleteProbabilityDistribution(expression.getIfClause());
			ProbabilityDistributionSQL.deleteProbabilityDistribution(expression.getElseClause());
			PredicateSQL.deletePredicate(expression.getCondition());
		}

		bDeleted = true;

		return bDeleted;
	}

	public static long saveIfExpression(IfExpression ifExpression) {
		long ifExpressionId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveIfExpression = (ifExpression.getId() == 0) ? con.prepareStatement(
						"INSERT INTO IF_EXPRESSION(COMPLEX_PROBABILITY_DISTRIBUTION_ID, POSITION, PREDICATE_ID, IF_CLAUSE_ID, ELSE_CLAUSE_ID) VALUES (?, ?, ?, ?, ?) ",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE FUNCTION SET COMPLEX_PROBABILITY_DISTRIBUTION_ID = ?, POSITION = ?,  PREDICATE_ID = ?, IF_CLAUSE_ID = ?, ELSE_CLAUSE_ID = ? WHERE ID = ? ")) {
			if (ifExpression.getId() != 0) {
				stmtSaveIfExpression.setLong(6, ifExpression.getId());
			}
			stmtSaveIfExpression.setLong(1, ifExpression.getComplexProbabilityDistribution().getId());
			stmtSaveIfExpression.setShort(2, ifExpression.getPosition());
			stmtSaveIfExpression.setLong(3, PredicateSQL.savePredicate(ifExpression.getCondition()));
			stmtSaveIfExpression.setLong(4,
					ProbabilityDistributionSQL.saveProbabilityDistribution(ifExpression.getIfClause()));
			stmtSaveIfExpression.setLong(5,
					ProbabilityDistributionSQL.saveProbabilityDistribution(ifExpression.getElseClause()));
			stmtSaveIfExpression.executeUpdate();

			if (ifExpression.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveIfExpression.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						ifExpressionId = generatedKeys.getLong(1);
						ifExpression.setId(ifExpressionId);
					} else {
						throw new SQLException("Creating if expression failed, no generated key obtained.");
					}
				}
			} else {
				ifExpressionId = ifExpression.getId();
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return ifExpressionId;
	}

	public static IfExpression getIfExpression(long id) {
		IfExpression ifExpression = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetIfExpressionById = con
						.prepareStatement("SELECT * FROM IF_EXPRESSION WHERE ID = ?")) {
			stmtGetIfExpressionById.setLong(1, id);
			try (ResultSet results = stmtGetIfExpressionById.executeQuery()) {
				while (results.next()) {
					ifExpression = new IfExpression();
					ifExpression.setId(results.getLong("id"));
					ifExpression.setPosition(results.getShort("position"));
					ifExpression.setCondition(PredicateSQL.getPredicateById(results.getLong("condition_id")));
					ifExpression.setIfClause(
							ProbabilityDistributionSQL.getProbabilityDistributionById(results.getLong("if_clause_id")));
					ifExpression.setElseClause(ProbabilityDistributionSQL
							.getProbabilityDistributionById(results.getLong("else_clause_id")));
				}
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return ifExpression;
	}

}