package org.eclipse.tradista.core.batch.job;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.batch.jobproperty.JobProperty;
import org.eclipse.tradista.core.batch.model.TradistaJob;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.error.service.ErrorBusinessDelegate;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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

public class CleanErrorJob extends TradistaJob {

	@JobProperty(name = "ErrorType", type = "ErrorType")
	private String errorType;

	@JobProperty(name = "ErrorDateFrom", type = "Date")
	private LocalDate errorDateFrom;

	@JobProperty(name = "ErrorDateTo", type = "Date")
	private LocalDate errorDateTo;

	@JobProperty(name = "Status", type = "ErrorStatus")
	private String status;

	@Override
	public void executeTradistaJob(JobExecutionContext execContext)
			throws JobExecutionException, TradistaBusinessException {

		if (isInterrupted) {
			performInterruption(execContext);
		}

		Status statusEnumValue = null;

		if (!StringUtils.isEmpty(status)) {
			statusEnumValue = Status.getStatus(status);
		}

		if (isInterrupted) {
			performInterruption(execContext);
		}

		new ErrorBusinessDelegate().deleteErrors(errorType, statusEnumValue, errorDateFrom, errorDateTo);

		if (isInterrupted) {
			performInterruption(execContext);
		}
	}

	@Override
	public String getName() {
		return "CleanError";
	}

	@Override
	public void checkJobProperties() throws TradistaBusinessException {
		if (!StringUtils.isEmpty(status)) {
			if (!status.equals(org.eclipse.tradista.core.error.model.Error.Status.SOLVED.toString())
					&& !status.equals(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED.toString())
					&& !status.equals("ALL")) {
				throw new TradistaBusinessException(String.format("The status must be %s, %s or %s",
						org.eclipse.tradista.core.error.model.Error.Status.SOLVED,
						org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED, "ALL"));
			}
		}
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	public void setErrorDateFrom(LocalDate errorDateFrom) {
		this.errorDateFrom = errorDateFrom;
	}

	public void setErrorDateTo(LocalDate errorDateTo) {
		this.errorDateTo = errorDateTo;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}