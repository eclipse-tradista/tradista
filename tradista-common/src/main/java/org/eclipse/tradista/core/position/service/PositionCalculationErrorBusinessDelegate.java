package org.eclipse.tradista.core.position.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.position.model.PositionCalculationError;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class PositionCalculationErrorBusinessDelegate {

	private PositionCalculationErrorService positionCalculationErrorService;

	public PositionCalculationErrorBusinessDelegate() {
		positionCalculationErrorService = TradistaServiceLocator.getInstance().getPositionCalculationErrorService();
	}

	public boolean savePositionCalculationErrors(List<PositionCalculationError> errors)
			throws TradistaBusinessException {
		if (errors == null || errors.isEmpty()) {
			throw new TradistaBusinessException("The errors list is null or empty.");
		}
		return SecurityUtil.run(() -> positionCalculationErrorService.savePositionCalculationErrors(errors));
	}

	public void solvePositionCalculationError(Set<Long> solved, LocalDate date) throws TradistaBusinessException {
		if (solved == null || solved.isEmpty()) {
			throw new TradistaBusinessException("The solved position definition ids list is null or empty.");
		}
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null.");
		}
		SecurityUtil.run(() -> positionCalculationErrorService.solvePositionCalculationError(solved, date));
	}

	public void solvePositionCalculationError(long positionDefinitionId, LocalDate date)
			throws TradistaBusinessException {
		if (positionDefinitionId <= 0) {
			throw new TradistaBusinessException("The position definition id must be positive.");
		}
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null.");
		}
		SecurityUtil
				.runEx(() -> positionCalculationErrorService.solvePositionCalculationError(positionDefinitionId, date));
	}

	public List<PositionCalculationError> getPositionCalculationErrors(long positionDefinitionId, Status status,
			long tradeId, long productId, LocalDate valueDateFrom, LocalDate valueDateTo, LocalDate errorDateFrom,
			LocalDate errorDateTo, LocalDate solvingDateFrom, LocalDate solvingDateTo)
			throws TradistaBusinessException {
		StringBuilder errorMsg = new StringBuilder();
		if (valueDateFrom != null && valueDateTo != null) {
			if (valueDateTo.isBefore(valueDateFrom)) {
				errorMsg.append(String.format("'To' value date cannot be before 'From' value date.%n"));
			}
		}
		if (errorDateFrom != null && errorDateTo != null) {
			if (errorDateTo.isBefore(errorDateFrom)) {
				errorMsg.append(String.format("'To' error date cannot be before 'From' error date.%n"));
			}
		}
		if (solvingDateFrom != null && solvingDateTo != null) {
			if (solvingDateTo.isBefore(solvingDateFrom)) {
				errorMsg.append(String.format("'To' solving date cannot be before 'From' solving date.%n"));
			}
		}
		if (errorMsg.length() > 0) {
			throw new TradistaBusinessException(errorMsg.toString());
		}
		return SecurityUtil
				.runEx(() -> positionCalculationErrorService.getPositionCalculationErrors(positionDefinitionId, status,
						tradeId, productId, valueDateFrom, valueDateTo, errorDateFrom, errorDateTo, solvingDateFrom,
						solvingDateTo));
	}

}