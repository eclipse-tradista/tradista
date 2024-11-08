package org.eclipse.tradista.ai.reasoning.prm.probability.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.tradista.ai.reasoning.prm.probability.model.ComplexProbabilityDistribution;
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

public class ComplexProbabilityDistributionSQL {

	public static long saveComplexProbabilityDistribution(ComplexProbabilityDistribution probabilityDistribution) {
		long probabilityDistributionId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveComplexProbabilityDistribution = (probabilityDistribution.getId() == 0)
						? con.prepareStatement("INSERT INTO COMPLEX_PROBABILITY_DISTRIBUTION",
								Statement.RETURN_GENERATED_KEYS)
						: null) {
			if (probabilityDistribution.getId() == 0) {
				stmtSaveComplexProbabilityDistribution.executeUpdate();
			} else {
				// Load the existing probability distribution, so we remove all its if
				// expressions.
				ComplexProbabilityDistribution existingComplexProbabilityDistribution = ComplexProbabilityDistributionSQL
						.getComplexProbabilityDistributionById(probabilityDistribution.getId());
				IfExpressionSQL.deleteIfExpressions(existingComplexProbabilityDistribution.getIfExpressions());
			}

			if (probabilityDistribution.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveComplexProbabilityDistribution.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						probabilityDistributionId = generatedKeys.getLong(1);
						probabilityDistribution.setId(probabilityDistributionId);
					} else {
						throw new SQLException(
								"Creating complex probability distribution failed, no generated key obtained.");
					}
				}
			} else {
				probabilityDistributionId = probabilityDistribution.getId();
			}

			if (probabilityDistribution.getIfExpressions() != null
					&& !probabilityDistribution.getIfExpressions().isEmpty()) {
				for (IfExpression ifExpression : probabilityDistribution.getIfExpressions()) {
					IfExpressionSQL.saveIfExpression(ifExpression);
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return probabilityDistributionId;
	}

	public static ComplexProbabilityDistribution getComplexProbabilityDistributionById(
			long complexProbabilityDistributionId) {
		ComplexProbabilityDistribution probabilityDistribution = new ComplexProbabilityDistribution();
		probabilityDistribution.setId(complexProbabilityDistributionId);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetIfExpressionsByProbabilityDistributionId = con.prepareStatement(
						"SELECT ID FROM IF_EXPRESSION WHERE COMPLEX_PROBABILITY_DISTRIBUTION_ID = ? ORDER BY POSITION")) {
			Set<IfExpression> ifExpressions = new LinkedHashSet<IfExpression>();
			stmtGetIfExpressionsByProbabilityDistributionId.setLong(1, complexProbabilityDistributionId);
			try (ResultSet results = stmtGetIfExpressionsByProbabilityDistributionId.executeQuery()) {
				while (results.next()) {
					IfExpression ifExpression = IfExpressionSQL.getIfExpression(results.getLong("id"));
					ifExpressions.add(ifExpression);
				}
			}
			probabilityDistribution.setIfExpressions(ifExpressions);
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return probabilityDistribution;
	}

	public static boolean deleteComplexProbabilityDistribution(
			ComplexProbabilityDistribution complexProbabilityDistribution) {
		boolean bDeleted = IfExpressionSQL.deleteIfExpressions(complexProbabilityDistribution.getIfExpressions());
		if (bDeleted) {
			try (Connection con = TradistaDB.getConnection();
					PreparedStatement stmtDeleteComplexProbabilityDistribution = con
							.prepareStatement("DELETE FROM COMPLEX_PROBABILITY_DISTRIBUTION WHERE ID = ?) ")) {
				stmtDeleteComplexProbabilityDistribution.setLong(1, complexProbabilityDistribution.getId());
				stmtDeleteComplexProbabilityDistribution.executeUpdate();
				bDeleted = true;
			} catch (SQLException sqle) {
				// TODO Manage logs
				sqle.printStackTrace();
				throw new TradistaTechnicalException(sqle);
			}
		}
		return bDeleted;
	}

}