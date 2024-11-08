package org.eclipse.tradista.core.daterule.model;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.Period;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.daterollconvention.model.DateRollingConvention;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public class DateRule extends TradistaObject implements Comparable<DateRule> {

	private static final long serialVersionUID = -6578402702449743205L;

	public static String[] DAY_POSITIONS = { "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th",
			"11th", "12th", "13th", "14th", "15th", "16th", "17th", "18th", "19th", "20th", "21st", "22nd", "23th",
			"24th", "25th", "26th", "27th", "28th", "29th", "30th", "31st", "Last" };

	public static String[] WEEK_DAY_POSITIONS = { "1st", "2nd", "3rd", "4th", "5th", "Last" };

	@Id
	private String name;

	private DateRollingConvention drc;

	private Set<Month> months;

	private DayOfWeek day;

	private String position;

	private boolean isSequence;

	private Map<DateRule, Period> dateRulesPeriods;

	private Set<Calendar> calendars;

	private short dateOffset;

	public DateRule(String name) {
		super();
		this.name = name;
		months = new HashSet<Month>();
		dateRulesPeriods = new LinkedHashMap<DateRule, Period>();
	}

	public String getName() {
		return name;
	}

	public DateRollingConvention getDateRollingConvention() {
		return drc;
	}

	public void setDateRollingConvention(DateRollingConvention drc) {
		this.drc = drc;
	}

	public Set<Month> getMonths() {
		if (months == null) {
			return null;
		}
		return new HashSet<>(months);
	}

	public void setMonths(Set<Month> months) {
		this.months = months;
	}

	public boolean isWeekDay() {
		return (day == null);
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) throws TradistaBusinessException {
		if (isWeekDay()) {
			if (!ArrayUtils.contains(WEEK_DAY_POSITIONS, position)) {
				throw new TradistaBusinessException(String.format(
						"The position (%s) should be a valid week day position: %s", position, WEEK_DAY_POSITIONS));
			}
		} else {
			if (!ArrayUtils.contains(DAY_POSITIONS, position)) {
				throw new TradistaBusinessException(
						String.format("The position (%s) should be a valid day position: %s", position, DAY_POSITIONS));
			}
		}
		this.position = position;
	}

	public boolean isSequence() {
		return isSequence;
	}

	public void setSequence(boolean isSequence) {
		this.isSequence = isSequence;
	}

	@SuppressWarnings("unchecked")
	public Map<DateRule, Period> getDateRulesPeriods() {
		return (Map<DateRule, Period>) TradistaModelUtil.deepCopy(dateRulesPeriods);
	}

	public void setDateRulesPeriods(Map<DateRule, Period> rulesPeriods) {
		this.dateRulesPeriods = rulesPeriods;
	}

	public DayOfWeek getDay() {
		return day;
	}

	public void setDay(DayOfWeek day) {
		this.day = day;
	}

	@SuppressWarnings("unchecked")
	public Set<Calendar> getCalendars() {
		return (Set<Calendar>) TradistaModelUtil.deepCopy(calendars);
	}

	public void setCalendars(Set<Calendar> calendars) {
		this.calendars = calendars;
	}

	public short getDateOffset() {
		return dateOffset;
	}

	public void setDateOffset(short dateOffset) {
		this.dateOffset = dateOffset;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(DateRule dr) {
		return name.compareTo(dr.getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public DateRule clone() {
		DateRule dateRule = (DateRule) super.clone();
		dateRule.dateRulesPeriods = (Map<DateRule, Period>) TradistaModelUtil.deepCopy(dateRulesPeriods);
		dateRule.calendars = (Set<Calendar>) TradistaModelUtil.deepCopy(calendars);
		return dateRule;
	}

}