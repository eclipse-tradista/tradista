package org.eclipse.tradista.core.calendar.service;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;

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

public class CalendarBusinessDelegate {

	private CalendarService calendarService;

	public CalendarBusinessDelegate() {
		calendarService = TradistaServiceLocator.getInstance().getCalendarService();
	}

	public Set<Calendar> getAllCalendars() {
		return SecurityUtil.run(() -> calendarService.getAllCalendars());
	}

	public Set<String> getAllCalendarCodes() {
		return SecurityUtil.run(() -> calendarService.getAllCalendarCodes());
	}

	public Calendar getCalendarById(long id) {
		return SecurityUtil.run(() -> calendarService.getCalendarById(id));
	}

	public Calendar getCalendarByCode(String code) {
		return SecurityUtil.run(() -> calendarService.getCalendarByCode(code));
	}

	public long saveCalendar(Calendar calendar) throws TradistaBusinessException {
		if (StringUtils.isBlank(calendar.getCode())) {
			throw new TradistaBusinessException("The code cannot be empty.");
		}
		return SecurityUtil.runEx(() -> calendarService.saveCalendar(calendar));
	}

	public long[] saveCalendars(Set<Calendar> calendars) {
		return SecurityUtil.run(() -> calendarService.saveCalendars(calendars));
	}

	public long[] addHolidays(Set<Calendar> calendars) {
		return SecurityUtil.run(() -> calendarService.addHolidays(calendars));
	}

}
