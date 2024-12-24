package org.eclipse.tradista.core.calendar.service;

import java.util.Set;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;

import jakarta.ejb.Remote;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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
public interface CalendarService {

	Set<Calendar> getAllCalendars();

	Calendar getCalendarById(long id);

	Calendar getCalendarByCode(String code);

	long saveCalendar(Calendar calendar) throws TradistaBusinessException;

	long[] saveCalendars(Set<Calendar> calendars);

	long[] addHolidays(Set<Calendar> calendars);

	Set<String> getAllCalendarCodes();

}
