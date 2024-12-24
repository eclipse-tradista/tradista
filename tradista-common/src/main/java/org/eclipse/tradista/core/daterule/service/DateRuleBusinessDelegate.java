package org.eclipse.tradista.core.daterule.service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.daterule.model.DateRule;

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

	public DateRuleBusinessDelegate() {
		dateRuleService = TradistaServiceLocator.getInstance().getDateRuleService();
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
		validateDateRule(dateRule);
		return SecurityUtil.runEx(() -> dateRuleService.saveDateRule(dateRule));
	}

	private void validateDateRule(DateRule dateRule) throws TradistaBusinessException {
		if (dateRule == null) {
			throw new TradistaBusinessException("The date rule cannot be null.");
		}
		StringBuilder errorMsg = new StringBuilder();
		if (StringUtils.isBlank(dateRule.getName())) {
			errorMsg.append("The name cannot be empty.\n");
		}

		if (dateRule.isSequence()) {
			if (dateRule.getDateRulesPeriods() == null || dateRule.getDateRulesPeriods().isEmpty()) {
				errorMsg.append("The date rule is a sequence but there is no sub date rules.\n");
			} else {
				for (Map.Entry<DateRule, Period> entry : dateRule.getDateRulesPeriods().entrySet()) {
					if (entry.getKey().equals(dateRule)) {
						errorMsg.append("The date rule cannot contain itself as a sub date rule.\n");
					} else {
						if (entry.getKey().isSequence()) {
							errorMsg.append("The sub date rule %s cannot be a sequence.\n");
						} else {
							if (entry.getValue().equals(Period.ZERO)) {
								errorMsg.append("The sub date rule %s cannot run for a duration of 0.\n");
							}
							try {
								validateDateRule(entry.getKey());
							} catch (TradistaBusinessException abe) {
								errorMsg.append(abe.getMessage());
							}
						}
					}
				}
			}
		} else {
			if (dateRule.getDateRollingConvention() == null) {
				errorMsg.append("The date rolling convention cannot be null.\n");
			}
			if (dateRule.getMonths() == null || dateRule.getMonths().isEmpty()) {
				errorMsg.append("There should be at least one month.\n");
			}
			if (StringUtils.isBlank(dateRule.getPosition())) {
				errorMsg.append("The position cannot be empty.\n");
			}
		}

		if (errorMsg.length() > 0) {
			throw new TradistaBusinessException(errorMsg.toString());
		}

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
			} catch (TradistaBusinessException tbe) {
				// Should never happen here.
			}
		} else {
			while (!startDate.isAfter(endDate)) {
				for (Map.Entry<DateRule, Period> entry : dateRule.getDateRulesPeriods().entrySet()) {
					LocalDate drEndDate = startDate.plus(entry.getValue());
					Period p;
					if (!drEndDate.isAfter(endDate)) {
						p = entry.getValue();
					} else {
						p = Period.between(startDate, endDate.plusDays(1));
					}
					dates.addAll(generateDates(entry.getKey(), startDate, p));
					startDate = startDate.plus(p);
				}
			}
		}
		return dates;
	}

}