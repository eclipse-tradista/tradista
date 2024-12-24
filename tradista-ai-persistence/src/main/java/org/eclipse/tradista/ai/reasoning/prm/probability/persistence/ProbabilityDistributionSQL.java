package org.eclipse.tradista.ai.reasoning.prm.probability.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.tradista.ai.reasoning.prm.probability.model.ComplexProbabilityDistribution;
import org.eclipse.tradista.ai.reasoning.prm.probability.model.ContinuousProbabilityDistribution;
import org.eclipse.tradista.ai.reasoning.prm.probability.model.NormalProbabilityDistribution;
import org.eclipse.tradista.ai.reasoning.prm.probability.model.ProbabilityDistribution;
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

public class ProbabilityDistributionSQL {

	public static long saveProbabilityDistribution(ProbabilityDistribution probabilityDistribution) {

		long probabilityDistributionId;
		if (probabilityDistribution instanceof ComplexProbabilityDistribution) {
			probabilityDistributionId = ComplexProbabilityDistributionSQL
					.saveComplexProbabilityDistribution((ComplexProbabilityDistribution) probabilityDistribution);
		} else {
			// TODO So far, only continuous probability distribution are managed
			// Question: do we keep the 'instanceof' mechanism ?
			probabilityDistributionId = NormalProbabilityDistributionSQL
					.saveNormalProbabilityDistribution((ContinuousProbabilityDistribution) probabilityDistribution);
		}
		return probabilityDistributionId;
	}

	public static boolean deleteProbabilityDistribution(ProbabilityDistribution probabilityDistribution) {

		boolean bDeleted = false;
		if (probabilityDistribution instanceof ComplexProbabilityDistribution) {
			bDeleted = ComplexProbabilityDistributionSQL
					.deleteComplexProbabilityDistribution((ComplexProbabilityDistribution) probabilityDistribution);
		} else {
			// TODO So far, only continuous probability distribution are managed
			// Question: do we keep the 'instanceof' mechanism ?
			bDeleted = NormalProbabilityDistributionSQL
					.deleteNormalProbabilityDistribution(probabilityDistribution.getId());
		}
		return bDeleted;
	}

	public static ProbabilityDistribution getProbabilityDistributionById(long id) {
		ProbabilityDistribution probabilityDistribution = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetProbabilityDistributionById = con.prepareStatement(
						"SELECT * FROM PROBABILITY_DISTRIBUTION WHERE ID = ? LEFT OUTER JOIN COMPLEX_PROBABILITY_DISTRIBUTION ON COMPLEX_PROBABILITY_DISTRIBUTION_ID = ID LEFT OUTER JOIN CONTINUOUS_PROBABILITY_DISTRIBUTION ON CONTINUOUS_PROBABILITY_DISTRIBUTION_ID = ID")) {
			stmtGetProbabilityDistributionById.setLong(1, id);
			try (ResultSet results = stmtGetProbabilityDistributionById.executeQuery()) {

				while (results.next()) {
					if (results.getLong("complex_probability_distribution_id") != 0) {
						probabilityDistribution = new ComplexProbabilityDistribution();
						probabilityDistribution = ComplexProbabilityDistributionSQL
								.getComplexProbabilityDistributionById(
										results.getLong("complex_probability_distribution_id"));
					} else {
						probabilityDistribution = new NormalProbabilityDistribution();
						probabilityDistribution = NormalProbabilityDistributionSQL
								.getNormalProbabilityDistributionById(results.getLong("id"));
					}

				}
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return probabilityDistribution;
	}

}