package org.eclipse.tradista.core.calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

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

public class Calendar extends TradistaObject {

	private static final long serialVersionUID = 8619460945695027871L;

	@Id
	private String code;
	private String name;

	public Calendar(String code) {
		super();
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private Set<DayOfWeek> weekEnd;

	private Set<LocalDate> holidays;

	public Set<DayOfWeek> getWeekEnd() {
		if (weekEnd == null) {
			return null;
		}
		return new HashSet<>(weekEnd);
	}

	public void setWeekEnd(Set<DayOfWeek> weekEnd) {
		this.weekEnd = weekEnd;
	}

	public Set<LocalDate> getHolidays() {
		if (holidays == null) {
			return null;
		}
		return new HashSet<>(holidays);
	}

	public void setHolidays(Set<LocalDate> holidays) {
		this.holidays = holidays;
	}

	public String toString() {
		return code;
	}

	public boolean isBusinessDay(LocalDate date) throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null");
		}
		// Check if it is a weekend
		if (weekEnd != null) {
			if (weekEnd.contains(date.getDayOfWeek())) {
				return false;
			}
		}
		// Check if it is a holiday
		if (holidays != null) {
			if (holidays.contains(date)) {
				return false;
			}
		}

		return true;
	}

	public void addHolidays(Set<LocalDate> holidays) {
		if (this.holidays == null) {
			this.holidays = holidays;
		} else {
			this.holidays.addAll(holidays);
		}
	}

}