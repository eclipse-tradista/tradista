package org.eclipse.tradista.ai.reasoning.prm.service;

import org.jboss.ejb3.annotation.SecurityDomain;

import org.eclipse.tradista.ai.reasoning.prm.probability.model.ProbabilityLaw;
import org.eclipse.tradista.ai.reasoning.prm.probability.persistence.ProbabilityLawSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class ProbabilityServiceBean implements ProbabilityService {

	@Override
	public long saveProbabilityLaw(ProbabilityLaw probabilityLaw) throws TradistaBusinessException {
		if (probabilityLaw.getId() == 0) {
			checkFunctionName(probabilityLaw);
			return ProbabilityLawSQL.saveProbabilityLaw(probabilityLaw);
		} else {
			ProbabilityLaw oldProbabilityLaw = ProbabilityLawSQL.getProbabilityLawById(probabilityLaw.getId());
			if (!probabilityLaw.getFunction().getFunction().getName()
					.equals(oldProbabilityLaw.getFunction().getFunction().getName())) {
				checkFunctionName(probabilityLaw);
			}
			return ProbabilityLawSQL.saveProbabilityLaw(probabilityLaw);
		}
	}

	private void checkFunctionName(ProbabilityLaw probabilityLaw) throws TradistaBusinessException {
		if (ProbabilityLawSQL
				.getProbabilityLawByFunctionName(probabilityLaw.getFunction().getFunction().getName()) != null) {
			throw new TradistaBusinessException(
					String.format("The probability law with function '%s' already exists in the model.",
							probabilityLaw.getFunction().getFunction().getName()));
		}
	}

}