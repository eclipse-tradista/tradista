package org.eclipse.tradista.core.batch.job;

import java.time.LocalDate;

import org.eclipse.tradista.core.batch.jobproperty.JobProperty;
import org.eclipse.tradista.core.batch.model.TradistaJob;
import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.dailypnl.model.DailyPnl;
import org.eclipse.tradista.core.dailypnl.service.DailyPnlBusinessDelegate;
import org.eclipse.tradista.core.position.model.PositionDefinition;
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

public class DailyPnlCalculationJob extends TradistaJob {

	@JobProperty(name = "PositionDefinition", type = "PositionDefinition")
	private PositionDefinition positionDefinition;

	@JobProperty(name = "ValueDate", type = "Date")
	private LocalDate valueDate;

	@JobProperty(name = "Calendar", type = "Calendar")
	private Calendar calendar;

	@Override
	public void executeTradistaJob(JobExecutionContext execContext)
			throws JobExecutionException, TradistaBusinessException {

		if (isInterrupted) {
			performInterruption(execContext);
		}

		DailyPnlBusinessDelegate dailyPnlBusinessDelegate = new DailyPnlBusinessDelegate();

		DailyPnl dailyPnl = dailyPnlBusinessDelegate.calculateDailyPnl(positionDefinition.getName(), calendar.getCode(),
				valueDate);

		dailyPnlBusinessDelegate.saveDailyPnl(dailyPnl);

		if (isInterrupted) {
			performInterruption(execContext);
		}
	}

	@Override
	public String getName() {
		return "DailyPnlCalculation";
	}

	public void setPositionDefinition(PositionDefinition positionDefinition) {
		this.positionDefinition = positionDefinition;
	}

	public void setValueDate(LocalDate valueDate) {
		this.valueDate = valueDate;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	@Override
	public void checkJobProperties() throws TradistaBusinessException {
		if (positionDefinition == null) {
			throw new TradistaBusinessException("The position definition is mandatory.");
		}
		if (valueDate == null) {
			throw new TradistaBusinessException("The value date is mandatory.");
		}
		if (calendar == null) {
			throw new TradistaBusinessException("The calendar is mandatory.");
		}
	}
}