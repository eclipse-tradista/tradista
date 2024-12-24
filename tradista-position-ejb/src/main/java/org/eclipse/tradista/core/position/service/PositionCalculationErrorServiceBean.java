package org.eclipse.tradista.core.position.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.position.model.PositionCalculationError;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.persistence.PositionCalculationErrorSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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
public class PositionCalculationErrorServiceBean
		implements LocalPositionCalculationErrorService, PositionCalculationErrorService {

	@Override
	public boolean savePositionCalculationErrors(List<PositionCalculationError> errors) {
		return PositionCalculationErrorSQL.savePositionCalculationErrors(errors);
	}

	@Override
	public void solvePositionCalculationError(Set<Long> solved, LocalDate date) {
		PositionCalculationErrorSQL.solvePositionCalculationError(solved, date);
	}

	/**
	 * no existence controls on parameters here for performance reasons. Existence
	 * controls are important for saving methods but for getXXX methods like this
	 * one, not sure it is a good idea.
	 */
	@Interceptors(PositionCalculationErrorFilteringInterceptor.class)
	@Override
	public List<PositionCalculationError> getPositionCalculationErrors(long positionDefinitionId, Status status,
			long tradeId, long productId, LocalDate valueDateFrom, LocalDate valueDateTo, LocalDate errorDateFrom,
			LocalDate errorDateTo, LocalDate solvingDateFrom, LocalDate solvingDateTo)
			throws TradistaBusinessException {
		return PositionCalculationErrorSQL.getPositionCalculationErrors(positionDefinitionId, status, tradeId,
				productId, valueDateFrom, valueDateTo, errorDateFrom, errorDateTo, solvingDateFrom, solvingDateTo);
	}

	@Override
	public void solvePositionCalculationError(long positionDefinitionId, LocalDate date)
			throws TradistaBusinessException {
		PositionDefinition posDef = new PositionDefinitionBusinessDelegate()
				.getPositionDefinitionById(positionDefinitionId);
		if (posDef == null) {
			throw new TradistaBusinessException(String
					.format("The position definition with id %s does not exist in the system.", positionDefinitionId));
		}
		PositionCalculationErrorSQL.solvePositionCalculationError(positionDefinitionId, date);
	}

}
