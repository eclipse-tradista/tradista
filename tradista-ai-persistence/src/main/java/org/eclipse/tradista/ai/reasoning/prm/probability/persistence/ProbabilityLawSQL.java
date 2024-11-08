package org.eclipse.tradista.ai.reasoning.prm.probability.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.tradista.ai.reasoning.prm.persistence.FunctionCallSQL;
import org.eclipse.tradista.ai.reasoning.prm.probability.model.ProbabilityLaw;
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

public class ProbabilityLawSQL {

	public static long saveProbabilityLaw(ProbabilityLaw probabilityLaw) {
		long probabilityLawId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveProbabilityLaw = (probabilityLaw.getId() == 0) ? con.prepareStatement(
						"INSERT INTO PROBABILITY_LAW(FUNCTION_CALL_ID, PROBABILITY_DISTRIBUTION_ID) VALUES (?, ?) ",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE PROBABILITY_LAW SET FUNCTION_CALL_ID = ?, PROBABILITY_DISTRIBUTION_ID = ? WHERE ID = ? ")) {
			if (probabilityLaw.getId() == 0) {
				FunctionCallSQL.saveFunctionCall(probabilityLaw.getFunction());
				ProbabilityDistributionSQL.saveProbabilityDistribution(probabilityLaw.getProbabilityDistribution());
			} else {
				FunctionCallSQL.deleteFunctionCall(probabilityLaw.getFunction().getId());
				ProbabilityDistributionSQL.saveProbabilityDistribution(probabilityLaw.getProbabilityDistribution());
			}
			if (probabilityLaw.getId() != 0) {
				stmtSaveProbabilityLaw.setLong(3, probabilityLaw.getId());
			}
			stmtSaveProbabilityLaw.setLong(1, probabilityLaw.getFunction().getId());
			stmtSaveProbabilityLaw.setLong(2, probabilityLaw.getProbabilityDistribution().getId());
			stmtSaveProbabilityLaw.executeUpdate();

			if (probabilityLaw.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveProbabilityLaw.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						probabilityLawId = generatedKeys.getLong(1);
						probabilityLaw.setId(probabilityLawId);
					} else {
						throw new SQLException("Creating probability law failed, no generated key obtained.");
					}
				}
			} else {
				probabilityLawId = probabilityLaw.getId();
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return probabilityLawId;
	}

	public static ProbabilityLaw getProbabilityLawByProbabilityDistributionId(long ProbabilityDistributionId) {
		ProbabilityLaw pLaw = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetProbabilityLawByProbabilityDistributionId = con
						.prepareStatement("SELECT * FROM PROBABILITY_LAW WHERE PROBABILITY_DISTRIBUTION_ID = ?")) {
			stmtGetProbabilityLawByProbabilityDistributionId.setLong(1, ProbabilityDistributionId);
			try (ResultSet results = stmtGetProbabilityLawByProbabilityDistributionId.executeQuery()) {
				while (results.next()) {
					pLaw = new ProbabilityLaw();
					pLaw.setId(results.getLong("id"));
					pLaw.setFunction(FunctionCallSQL.getFunctionCallById(results.getLong("function_call_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return pLaw;
	}

	public static ProbabilityLaw getProbabilityLawByFunctionName(String name) {
		ProbabilityLaw pLaw = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetProbabilityLawByByFunctionName = con.prepareStatement(
						"SELECT * FROM PROBABILITY_LAW, FUNCTION_CALL, FUNCTION WHERE FUNCTION_CALL_ID = FUNCTION_CALL.ID AND FUNCTION_ID = FUNCTION.ID AND NAME = ?")) {
			stmtGetProbabilityLawByByFunctionName.setString(1, name);
			try (ResultSet results = stmtGetProbabilityLawByByFunctionName.executeQuery()) {

				while (results.next()) {
					pLaw = new ProbabilityLaw();
					pLaw.setId(results.getLong("id"));
					pLaw.setFunction(FunctionCallSQL.getFunctionCallById(results.getLong("function_call_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return pLaw;
	}

	public static ProbabilityLaw getProbabilityLawById(long id) {
		ProbabilityLaw pLaw = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetProbabilityLawByProbabilityDistributionId = con
						.prepareStatement("SELECT * FROM PROBABILITY_LAW WHERE ID = ?")) {
			stmtGetProbabilityLawByProbabilityDistributionId.setLong(1, id);
			try (ResultSet results = stmtGetProbabilityLawByProbabilityDistributionId.executeQuery()) {
				while (results.next()) {
					pLaw = new ProbabilityLaw();
					pLaw.setId(results.getLong("id"));
					pLaw.setFunction(FunctionCallSQL.getFunctionCallById(results.getLong("function_call_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return pLaw;
	}

}