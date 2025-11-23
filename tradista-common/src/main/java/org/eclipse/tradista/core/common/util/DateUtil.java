package org.eclipse.tradista.core.common.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.daterollconvention.model.DateRollingConvention;
import org.eclipse.tradista.core.tenor.model.Tenor;

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

public final class DateUtil {

	private DateUtil() {
	}

	/**
	 * Days difference between two dates (Absolute value). As it is the absolute
	 * value, difference(date1, date2) is equal to difference(date2, date1)
	 * 
	 * @param firstDate  a first date to make the comparison
	 * @param secondDate a second date to make the comparison
	 * @return the difference between two dates, expressed in days
	 */
	public static int difference(LocalDate firstDate, LocalDate secondDate) {
		return Math.abs((int) ChronoUnit.DAYS.between(firstDate, secondDate));
	}

	/**
	 * Returns the next business day AFTER the given date, for the selected
	 * calendars. The result date should be a business day for all the selected
	 * calendars.
	 * 
	 * @param date      the date where to start the search
	 * @param calendars the calendars used.
	 * @return the next business day AFTER the given date, for the selected
	 *         calendars.
	 * @throws TradistaBusinessException if the date is null
	 */
	public static LocalDate nextBusinessDay(LocalDate date, Calendar... calendars) throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null");
		}
		while (true) {
			date = date.plusDays(1);
			if (calendars != null && calendars.length > 0) {
				if (DateUtil.isBusinessDay(date, calendars)) {
					return date;
				}
			} else {
				return date;
			}
		}
	}

	/**
	 * Indicates if a date is a business day according to given calendars.
	 * 
	 * @param date      the date to be checked
	 * @param calendars the calendars (optional)
	 * @return true if the date is a business day for ALL the calendars, false
	 *         otherwise
	 * @throws TradistaBusinessException if the date is null
	 */
	public static boolean isBusinessDay(LocalDate date, Calendar... calendars) throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null.");
		}
		if (calendars == null || calendars.length == 0) {
			return true;
		}

		for (Calendar cal : calendars) {
			if (cal != null) {
				if (!cal.isBusinessDay(date)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Returns the next business day BEFORE the given date, for the selected
	 * calendars. The rolled date should be a business day for all the selected
	 * calendars.
	 * 
	 * @param date      the date where to start the search
	 * @param calendars the calendars used. If null, no calendar is used (all days
	 *                  will be considered as Business days).
	 * @return the next business day BEFORE the given date, for the selected
	 *         calendars.
	 * @throws TradistaBusinessException if the date is null
	 */
	public static LocalDate previousBusinessDay(LocalDate date, Calendar... calendars)
			throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null");
		}
		while (true) {
			date = date.plusDays(-1);
			if (calendars != null && calendars.length > 0) {
				if (DateUtil.isBusinessDay(date, calendars)) {
					return date;
				}
			} else {
				return date;
			}
		}
	}

	/**
	 * Adds x business day to the given date, for the selected calendar, and returns
	 * the date.
	 * 
	 * @param date       the date where to start the search
	 * @param calendar   the calendar used.
	 * @param daysNumber the number of days to add.
	 * @return the date being the parameter date plus 'daysNumber' business days.
	 * @throws TradistaBusinessException if the date is null
	 */
	public static LocalDate addBusinessDay(LocalDate date, Calendar calendar, int days)
			throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null");
		}
		LocalDate newDate = date;
		if (days == 0) {
			// Checks how to manage it.
		}
		if (days < 0) {
			while (days < 0) {
				newDate = DateUtil.previousBusinessDay(newDate, calendar);
				days++;
			}
		}
		if (days > 0) {
			while (days > 0) {
				newDate = DateUtil.nextBusinessDay(newDate, calendar);
				days--;
			}
		}
		return newDate;
	}

	public static LocalDate addTenor(LocalDate date, Tenor tenor) throws TradistaBusinessException {

		if (date == null) {
			throw new TradistaBusinessException("The date is mandatory.");
		}

		if (tenor == null) {
			throw new TradistaBusinessException("The tenor is mandatory.");
		}

		if (tenor.equals(Tenor.ONE_MONTH)) {
			date = date.plus(1, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.TWO_MONTHS)) {
			date = date.plus(2, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.THREE_MONTHS)) {
			date = date.plus(3, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.FOUR_MONTHS)) {
			date = date.plus(4, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.FIVE_MONTHS)) {
			date = date.plus(5, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.SIX_MONTHS)) {
			date = date.plus(6, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.ONE_YEAR)) {
			date = date.plus(1, ChronoUnit.YEARS);
		}
		if (tenor.equals(Tenor.EIGHTEEN_MONTHS)) {
			date = date.plus(18, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.TWO_YEARS)) {
			date = date.plus(2, ChronoUnit.YEARS);
		}

		return date;
	}

	public static LocalDate subtractTenor(LocalDate date, Tenor tenor) throws TradistaBusinessException {

		if (date == null) {
			throw new TradistaBusinessException("The date is mandatory.");
		}

		if (tenor == null) {
			throw new TradistaBusinessException("The tenor is mandatory.");
		}

		if (tenor.equals(Tenor.ONE_MONTH)) {
			date = date.minus(1, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.TWO_MONTHS)) {
			date = date.minus(2, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.THREE_MONTHS)) {
			date = date.minus(3, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.FOUR_MONTHS)) {
			date = date.minus(4, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.FIVE_MONTHS)) {
			date = date.minus(5, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.SIX_MONTHS)) {
			date = date.minus(6, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.ONE_YEAR)) {
			date = date.minus(1, ChronoUnit.YEARS);
		}
		if (tenor.equals(Tenor.EIGHTEEN_MONTHS)) {
			date = date.minus(18, ChronoUnit.MONTHS);
		}
		if (tenor.equals(Tenor.TWO_YEARS)) {
			date = date.minus(2, ChronoUnit.YEARS);
		}

		return date;
	}

	/**
	 * Rolls the date according to the convention.
	 * 
	 * @param date      the date to be rolled
	 * @param calendars the calendars to be applied (optional)
	 * @return the date resulted from the rolling
	 * @exception if the date is null.
	 */
	public static LocalDate roll(DateRollingConvention drc, LocalDate date, Calendar... calendars)
			throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null.");
		}

		switch (drc) {
		case ACTUAL:
			return date;
		case FOLLOWING_BUSINESS_DAY: {
			if (!DateUtil.isBusinessDay(date, calendars)) {
				return DateUtil.nextBusinessDay(date, calendars);
			} else {
				return date;
			}
		}
		case MODIFIED_FOLLOWING_BUSINESS_DAY:
			if (!DateUtil.isBusinessDay(date, calendars)) {
				LocalDate rolledDate = DateUtil.nextBusinessDay(date, calendars);
				if (!rolledDate.getMonth().equals(date.getMonth())) {
					return DateUtil.previousBusinessDay(date, calendars);
				} else {
					return rolledDate;
				}
			} else {
				return date;
			}
		case PREVIOUS_BUSINESS_DAY:
			if (!DateUtil.isBusinessDay(date, calendars)) {
				return DateUtil.previousBusinessDay(date, calendars);
			} else {
				return date;
			}
		case MODIFIED_PREVIOUS_BUSINESS_DAY:
			if (!DateUtil.isBusinessDay(date, calendars)) {
				LocalDate rolledDate = DateUtil.previousBusinessDay(date, calendars);
				if (!rolledDate.getMonth().equals(date.getMonth())) {
					return DateUtil.nextBusinessDay(date, calendars);
				} else {
					return rolledDate;
				}
			} else {
				return date;
			}
		case MODIFIED_ROLLING_BUSINESS_DAY:
			if (!DateUtil.isBusinessDay(date, calendars)) {
				return DateUtil.nextBusinessDay(date, calendars);
			} else {
				return date;
			}
		}

		return null;
	}

}