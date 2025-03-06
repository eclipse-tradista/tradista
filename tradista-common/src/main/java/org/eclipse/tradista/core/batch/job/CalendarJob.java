package org.eclipse.tradista.core.batch.job;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.batch.jobproperty.JobProperty;
import org.eclipse.tradista.core.batch.model.TradistaJob;
import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.calendar.service.CalendarBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ParserUtil;
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

public class CalendarJob extends TradistaJob {

	@JobProperty(name = "FilePath")
	private String filePath;

	@JobProperty(name = "ReplaceCalendar", type = "Boolean")
	private boolean replaceCalendar;

	@JobProperty(name = "FieldSeparator")
	private String fieldSeparator;

	@SuppressWarnings({ "unchecked" })
	@Override
	public void executeTradistaJob(JobExecutionContext execContext)
			throws JobExecutionException, TradistaBusinessException {

		CalendarBusinessDelegate calendarBusinessDelegate;
		Map<String, String> config = null;
		if (fieldSeparator != null && !fieldSeparator.isEmpty()) {
			config = new HashMap<>();
			config.put("fieldSeparator", fieldSeparator);
		}

		if (isInterrupted) {
			performInterruption(execContext);
		}

		// 1. Get the file
		File file = new File(filePath);

		if (isInterrupted) {
			performInterruption(execContext);
		}

		// 2. Parse the file
		List<Calendar> calendars = (List<Calendar>) ParserUtil.parse(file, "Calendar", config);

		if (isInterrupted) {
			performInterruption(execContext);
		}

		// 3. Transform the list of Calendars into a Set
		Set<Calendar> calSet = toSet(calendars);

		if (isInterrupted) {
			performInterruption(execContext);
		}

		calendarBusinessDelegate = new CalendarBusinessDelegate();

		// 4. Drop the existing calendar (only if replaceCalendar was selected)
		if (replaceCalendar) {
			calendarBusinessDelegate.saveCalendars(calSet);
			return;
		}

		calendarBusinessDelegate.addHolidays(calSet);

	}

	private Set<Calendar> toSet(List<Calendar> calendars) {
		Set<Calendar> calSet = new HashSet<Calendar>();
		Map<String, Calendar> calMap = new HashMap<String, Calendar>();
		for (Calendar cal : calendars) {
			if (calMap.containsKey(cal.getCode())) {
				Calendar tmpCal = calMap.remove(cal.getCode());
				tmpCal.addHolidays(cal.getHolidays());
				calMap.put(cal.getCode(), tmpCal);
			} else {
				calMap.put(cal.getCode(), cal);
			}
		}

		// Now the map contains all the calendars, we will transform it as a set
		for (Calendar cal : calMap.values()) {
			calSet.add(cal);
		}

		return calSet;
	}

	public void setReplaceCalendar(boolean replaceCalendar) {
		this.replaceCalendar = replaceCalendar;
	}

	public void setFieldSeparator(String fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
	}

	@Override
	public String getName() {
		return "Calendar";
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public void checkJobProperties() throws TradistaBusinessException {
		if (StringUtils.isEmpty(fieldSeparator)) {
			throw new TradistaBusinessException("The field separator is mandatory.");
		}
		if (StringUtils.isEmpty(filePath)) {
			throw new TradistaBusinessException("The file path is mandatory.");
		}
		File file = new File(filePath);
		if (!file.exists()) {
			throw new TradistaBusinessException("The file path must exist.");
		}
	}

}
