package org.eclipse.tradista.core.position.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.position.model.PositionCalculationError;

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

@Remote
public interface PositionCalculationErrorService {

	boolean savePositionCalculationErrors(List<PositionCalculationError> errors);

	void solvePositionCalculationError(Set<Long> solved, LocalDate date);

	void solvePositionCalculationError(long positionDefinitionId, LocalDate date) throws TradistaBusinessException;

	List<PositionCalculationError> getPositionCalculationErrors(long positionDefinitionId, Status status, long tradeId,
			long productId, LocalDate valueDateFrom, LocalDate valueDateTo, LocalDate errorDateFrom,
			LocalDate errorDateTo, LocalDate solvingDateFrom, LocalDate solvingDateTo) throws TradistaBusinessException;

}
