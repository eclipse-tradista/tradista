package org.eclipse.tradista.core.batch.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.batch.jobproperty.JobProperty;
import org.eclipse.tradista.core.batch.model.TradistaJob;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.service.PositionBusinessDelegate;
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

public class PositionCalculationJob extends TradistaJob {

	@JobProperty(name = "PositionDefinition", type = "PositionDefinition")
	private PositionDefinition positionDefinition;

	@JobProperty(name = "ValueDateTime")
	private String valueDateTime;

	@Override
	public void executeTradistaJob(JobExecutionContext execContext)
			throws JobExecutionException, TradistaBusinessException {

		if (isInterrupted) {
			performInterruption(execContext);
		}

		new PositionBusinessDelegate().calculatePosition(positionDefinition.getName(),
				LocalDateTime.parse(valueDateTime, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

		if (isInterrupted) {
			performInterruption(execContext);
		}
	}

	@Override
	public String getName() {
		return "PositionCalculation";
	}

	public void setPositionDefinition(PositionDefinition positionDefinition) {
		this.positionDefinition = positionDefinition;
	}

	public void setValueDateTime(String valueDateTime) {
		this.valueDateTime = valueDateTime;
	}

	@Override
	public void checkJobProperties() throws TradistaBusinessException {
		if (positionDefinition == null) {
			throw new TradistaBusinessException("The position definition is mandatory.");
		}
		if (StringUtils.isEmpty(valueDateTime)) {
			throw new TradistaBusinessException("The value date is mandatory.");
		}
		try {
			LocalDateTime.parse(valueDateTime, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		} catch (DateTimeParseException dtpe) {
			throw new TradistaBusinessException(
					String.format("The value date must be a valid date: %s.", dtpe.getMessage()));
		}
	}
}
