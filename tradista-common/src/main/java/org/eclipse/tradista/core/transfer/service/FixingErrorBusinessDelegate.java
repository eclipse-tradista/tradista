package org.eclipse.tradista.core.transfer.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.transfer.model.FixingError;

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

public class FixingErrorBusinessDelegate {

	private FixingErrorService fixingErrorService;

	public FixingErrorBusinessDelegate() {
		fixingErrorService = TradistaServiceLocator.getInstance().getFixingErrorService();
	}

	public boolean saveFixingErrors(List<FixingError> errors) throws TradistaBusinessException {
		if (errors == null || errors.isEmpty()) {
			throw new TradistaBusinessException("The errors list is null or empty.");
		}
		return SecurityUtil.run(() -> fixingErrorService.saveFixingErrors(errors));
	}

	public void solveFixingError(Set<Long> solved, LocalDate date) throws TradistaBusinessException {
		if (solved == null || solved.isEmpty()) {
			throw new TradistaBusinessException("The solved transfer ids list is null or empty.");
		}
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null.");
		}
		SecurityUtil.run(() -> fixingErrorService.solveFixingError(solved, date));
	}

	public void solveFixingError(long transferId, LocalDate date) throws TradistaBusinessException {
		if (transferId <= 0) {
			throw new TradistaBusinessException("The transfer id must be positive.");
		}
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null.");
		}
		SecurityUtil.runEx(() -> fixingErrorService.solveFixingError(transferId, date));
	}

	public List<FixingError> getFixingErrors(long transferId, Status status, LocalDate errorDateFrom,
			LocalDate errorDateTo, LocalDate solvingDateFrom, LocalDate solvingDateTo)
			throws TradistaBusinessException {
		StringBuilder errorMsg = new StringBuilder();
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
		return SecurityUtil.runEx(() -> fixingErrorService.getFixingErrors(transferId, status, errorDateFrom,
				errorDateTo, solvingDateFrom, solvingDateTo));
	}

}