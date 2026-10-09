package org.eclipse.tradista.core.daterule.service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.daterule.model.DateRule;
import org.eclipse.tradista.core.daterule.validator.DateRuleValidator;

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

public class DateRuleBusinessDelegate {

	private DateRuleService dateRuleService;

	private DateRuleValidator validator;

	public DateRuleBusinessDelegate() {
		dateRuleService = TradistaServiceLocator.getInstance().getDateRuleService();
		validator = new DateRuleValidator();
	}

	public Set<DateRule> getAllDateRules() {
		return SecurityUtil.run(() -> dateRuleService.getAllDateRules());
	}

	public DateRule getDateRuleById(long id) {
		return SecurityUtil.run(() -> dateRuleService.getDateRuleById(id));
	}

	public DateRule getDateRuleByName(String name) {
		return SecurityUtil.run(() -> dateRuleService.getDateRuleByName(name));
	}

	public long saveDateRule(DateRule dateRule) throws TradistaBusinessException {
		validator.validateDateRule(dateRule);
		return SecurityUtil.runEx(() -> dateRuleService.saveDateRule(dateRule));
	}

	public Set<LocalDate> generateDates(DateRule dateRule, LocalDate startDate, Period period) {
		Set<LocalDate> dates = new TreeSet<LocalDate>();
		LocalDate endDate = startDate.plus(period);
		if (!dateRule.isSequence()) {
			try {
				while (!startDate.isAfter(endDate)) {
					if (dateRule.getMonths().contains(startDate.getMonth())) {
						if (dateRule.getDay() == null) {
							if (dateRule.getPosition().equals("Last")) {
								if (startDate.equals(startDate.withDayOfMonth(startDate.lengthOfMonth()))) {
									LocalDate toBeCheckedDate = startDate;
									if (dateRule.getDateOffset() != 0) {
										toBeCheckedDate = toBeCheckedDate.plusDays(dateRule.getDateOffset());
									}
									Calendar[] cals = null;
									if (dateRule.getCalendars() != null) {
										cals = dateRule.getCalendars().toArray(new Calendar[0]);
									}
									if (DateUtil.isBusinessDay(toBeCheckedDate, cals)) {
										dates.add(toBeCheckedDate);
									} else {
										dates.add(DateUtil.roll(dateRule.getDateRollingConvention(), toBeCheckedDate,
												cals));
									}
								}
							} else {
								int pos = Integer.parseInt(dateRule.getPosition()
										.subSequence(0, dateRule.getPosition().length() - 2).toString());
								if (startDate.equals(startDate.withDayOfMonth(pos))) {
									LocalDate toBeCheckedDate = startDate;
									if (dateRule.getDateOffset() != 0) {
										toBeCheckedDate = toBeCheckedDate.plusDays(dateRule.getDateOffset());
									}
									Calendar[] cals = null;
									if (dateRule.getCalendars() != null) {
										cals = dateRule.getCalendars().toArray(new Calendar[0]);
									}
									if (DateUtil.isBusinessDay(toBeCheckedDate, cals)) {
										dates.add(toBeCheckedDate);
									} else {
										dates.add(DateUtil.roll(dateRule.getDateRollingConvention(), toBeCheckedDate,
												cals));
									}
								}
							}
						} else {
							if (dateRule.getPosition().equals("Last")) {
								TemporalAdjuster adj = TemporalAdjusters.lastInMonth(dateRule.getDay());
								if (startDate.equals(startDate.with(adj))) {
									LocalDate toBeCheckedDate = startDate;
									if (dateRule.getDateOffset() != 0) {
										toBeCheckedDate = toBeCheckedDate.plusDays(dateRule.getDateOffset());
									}
									Calendar[] cals = null;
									if (dateRule.getCalendars() != null) {
										cals = dateRule.getCalendars().toArray(new Calendar[0]);
									}
									if (DateUtil.isBusinessDay(toBeCheckedDate, cals)) {
										dates.add(toBeCheckedDate);
									} else {
										dates.add(DateUtil.roll(dateRule.getDateRollingConvention(), toBeCheckedDate,
												cals));
									}
								}
							} else {
								int pos = Integer.parseInt(dateRule.getPosition()
										.subSequence(0, dateRule.getPosition().length() - 2).toString());
								TemporalAdjuster adj = TemporalAdjusters.dayOfWeekInMonth(pos, dateRule.getDay());
								if (startDate.equals(startDate.with(adj))) {
									LocalDate toBeCheckedDate = startDate;
									if (dateRule.getDateOffset() != 0) {
										toBeCheckedDate = toBeCheckedDate.plusDays(dateRule.getDateOffset());
									}
									Calendar[] cals = null;
									if (dateRule.getCalendars() != null) {
										cals = dateRule.getCalendars().toArray(new Calendar[0]);
									}
									if (DateUtil.isBusinessDay(toBeCheckedDate, cals)) {
										dates.add(toBeCheckedDate);
									} else {
										dates.add(DateUtil.roll(dateRule.getDateRollingConvention(), toBeCheckedDate,
												cals));
									}
								}
							}
						}
					}
					startDate = startDate.plusDays(1);
				}
			} catch (TradistaBusinessException _) {
				// Should never happen here.
			}
		} else {
			while (!startDate.isAfter(endDate)) {
				for (DateRule.Step step : dateRule.getSteps()) {
					LocalDate drEndDate = startDate.plus(step.getDuration());
					Period p;
					if (!drEndDate.isAfter(endDate)) {
						p = step.getDuration();
					} else {
						p = Period.between(startDate, endDate.plusDays(1));
					}
					dates.addAll(generateDates(step.getDateRule(), startDate, p));
					startDate = startDate.plus(p);
				}
			}
		}
		return dates;
	}

}