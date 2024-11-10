package org.eclipse.tradista.core.daterule.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.Period;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.calendar.persistence.CalendarSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.daterollconvention.model.DateRollingConvention;
import org.eclipse.tradista.core.daterule.model.DateRule;

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

public class DateRuleSQL {

	public static DateRule getDateRuleById(long id) {

		DateRule dateRule = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetDateRuleById = con.prepareStatement("SELECT * FROM DATE_RULE WHERE ID = ? ")) {
			stmtGetDateRuleById.setLong(1, id);
			try (ResultSet results = stmtGetDateRuleById.executeQuery()) {
				while (results.next()) {
					dateRule = new DateRule(results.getString("name"));
					dateRule.setId(results.getLong("id"));
					dateRule.setSequence(results.getBoolean("is_sequence"));
					if (dateRule.isSequence()) {
						dateRule.setDateRulesPeriods(getDateRulesPeriodsByDateRuleId(dateRule.getId()));
					} else {
						dateRule.setDateRollingConvention(
								DateRollingConvention.valueOf(results.getString("date_rolling_convention")));
						dateRule.setPosition(results.getString("position"));
						String dayString = results.getString("week_day");
						if (dayString != null) {
							dateRule.setDay(DayOfWeek.valueOf(dayString));
						}
						dateRule.setDateOffset(results.getShort("date_offset"));
						dateRule.setMonths(getDateRuleMonths(dateRule.getId()));
						dateRule.setCalendars(getDateRuleCalendars(dateRule.getId()));
					}
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}
		return dateRule;
	}

	private static Set<Calendar> getDateRuleCalendars(long id) {
		Set<Calendar> calendars = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCalendarsByDateRuleId = con
						.prepareStatement("SELECT CALENDAR_ID FROM DATE_RULE_CALENDAR WHERE DATE_RULE_ID = ? ")) {
			stmtGetCalendarsByDateRuleId.setLong(1, id);
			try (ResultSet results = stmtGetCalendarsByDateRuleId.executeQuery()) {
				while (results.next()) {
					if (calendars == null) {
						calendars = new HashSet<Calendar>();
					}
					calendars.add(CalendarSQL.getCalendarById(results.getLong("calendar_id")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return calendars;
	}

	private static Map<DateRule, Period> getDateRulesPeriodsByDateRuleId(long id) {
		Map<DateRule, Period> dateRulesPeriods = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetDateRulesPeriodsByDateRuleId = con.prepareStatement(
						"SELECT * FROM DATE_RULE_SUB_DATE_RULE WHERE DATE_RULE_ID = ? ORDER BY POSITION")) {
			stmtGetDateRulesPeriodsByDateRuleId.setLong(1, id);
			try (ResultSet results = stmtGetDateRulesPeriodsByDateRuleId.executeQuery()) {
				while (results.next()) {
					if (dateRulesPeriods == null) {
						dateRulesPeriods = new LinkedHashMap<DateRule, Period>();
					}
					DateRule dateRule = getDateRuleById(results.getLong("sub_date_rule_id"));

					Period period = Period.of(results.getInt("duration_year"), results.getInt("duration_month"),
							results.getInt("duration_day"));

					dateRulesPeriods.put(dateRule, period);
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return dateRulesPeriods;
	}

	private static Set<Month> getDateRuleMonths(long id) {

		Set<Month> months = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMonthsByDateRuleId = con
						.prepareStatement("SELECT * FROM MONTH WHERE DATE_RULE_ID = ? ")) {
			stmtGetMonthsByDateRuleId.setLong(1, id);
			try (ResultSet results = stmtGetMonthsByDateRuleId.executeQuery()) {
				while (results.next()) {
					if (months == null) {
						months = new HashSet<Month>();
					}
					months.add(Month.valueOf(results.getString("month")));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return months;
	}

	public static Set<DateRule> getAllDateRules() {

		Set<DateRule> dateRules = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllDateRules = con.prepareStatement("SELECT * FROM DATE_RULE");
				ResultSet results = stmtGetAllDateRules.executeQuery()) {
			while (results.next()) {
				if (dateRules == null) {
					dateRules = new TreeSet<DateRule>();
				}
				DateRule dateRule = new DateRule(results.getString("name"));
				dateRule.setId(results.getLong("id"));
				dateRule.setSequence(results.getBoolean("is_sequence"));
				if (dateRule.isSequence()) {
					dateRule.setDateRulesPeriods(getDateRulesPeriodsByDateRuleId(dateRule.getId()));
				} else {
					dateRule.setDateRollingConvention(
							DateRollingConvention.valueOf(results.getString("date_rolling_convention")));
					dateRule.setPosition(results.getString("position"));
					String dayString = results.getString("week_day");
					if (dayString != null) {
						dateRule.setDay(DayOfWeek.valueOf(dayString));
					}
					dateRule.setDateOffset(results.getShort("date_offset"));
					dateRule.setMonths(getDateRuleMonths(dateRule.getId()));
					dateRule.setCalendars(getDateRuleCalendars(dateRule.getId()));
				}
				dateRules.add(dateRule);
			}
		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}
		return dateRules;
	}

	public static DateRule getDateRuleByName(String name) {
		DateRule dateRule = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetDateRuleByName = con
						.prepareStatement("SELECT * FROM DATE_RULE WHERE NAME = ? ")) {
			stmtGetDateRuleByName.setString(1, name);
			try (ResultSet results = stmtGetDateRuleByName.executeQuery()) {
				while (results.next()) {
					dateRule = new DateRule(results.getString("name"));
					dateRule.setId(results.getLong("id"));
					dateRule.setSequence(results.getBoolean("is_sequence"));
					if (dateRule.isSequence()) {
						dateRule.setDateRulesPeriods(getDateRulesPeriodsByDateRuleId(dateRule.getId()));
					} else {
						dateRule.setDateRollingConvention(
								DateRollingConvention.valueOf(results.getString("date_rolling_convention")));
						dateRule.setPosition(results.getString("position"));
						String dayString = results.getString("week_day");
						if (dayString != null) {
							dateRule.setDay(DayOfWeek.valueOf(dayString));
						}
						dateRule.setDateOffset(results.getShort("date_offset"));
						dateRule.setMonths(getDateRuleMonths(dateRule.getId()));
						dateRule.setCalendars(getDateRuleCalendars(dateRule.getId()));
					}
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			// TODO Manage logs
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}
		return dateRule;

	}

	public static long saveDateRule(DateRule dateRule) {
		long dateRuleId = 0;

		// 1. Check if the date rule already exists

		boolean exists = dateRule.getId() != 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveMonthByDateRuleId = (dateRule.getMonths() != null)
						? con.prepareStatement("INSERT INTO MONTH(MONTH, DATE_RULE_ID) VALUES (?, ?) ")
						: null;
				PreparedStatement stmtSaveDateRuleByDateRuleId = (dateRule.getDateRulesPeriods() != null)
						? con.prepareStatement(
								"INSERT INTO DATE_RULE_SUB_DATE_RULE(DURATION_YEAR, DURATION_MONTH, DURATION_DAY, POSITION, DATE_RULE_ID, SUB_DATE_RULE_ID) VALUES (?, ?, ?, ?, ?, ?) ")
						: null;
				PreparedStatement stmtSaveCalendarByDateRuleId = (dateRule.getCalendars() != null) ? con
						.prepareStatement("INSERT INTO DATE_RULE_CALENDAR(DATE_RULE_ID, CALENDAR_ID) VALUES (?, ?) ")
						: null;
				PreparedStatement stmtSaveDateRule = (!exists) ? con.prepareStatement(
						"INSERT INTO DATE_RULE(NAME, DATE_ROLLING_CONVENTION, POSITION, IS_SEQUENCE, WEEK_DAY, DATE_OFFSET) VALUES (?, ?, ?, ?, ?, ?) ",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE DATE_RULE SET DATE_ROLLING_CONVENTION = ?, POSITION = ?, IS_SEQUENCE = ?, WEEK_DAY = ?, DATE_OFFSET = ? WHERE ID = ?")) {

			// The date rule doesn't exist
			if (!exists) {
				stmtSaveDateRule.setString(1, dateRule.getName());
				DateRollingConvention drc = dateRule.getDateRollingConvention();
				if (drc != null) {
					stmtSaveDateRule.setString(2, dateRule.getDateRollingConvention().name());
				} else {
					stmtSaveDateRule.setNull(2, Types.VARCHAR);
				}
				String position = dateRule.getPosition();
				if (position != null) {
					stmtSaveDateRule.setString(3, dateRule.getPosition());
				} else {
					stmtSaveDateRule.setNull(3, Types.VARCHAR);
				}
				stmtSaveDateRule.setBoolean(4, dateRule.isSequence());
				DayOfWeek day = dateRule.getDay();
				if (day != null) {
					stmtSaveDateRule.setString(5, day.toString());
				} else {
					stmtSaveDateRule.setNull(5, Types.VARCHAR);
				}
				stmtSaveDateRule.setShort(6, dateRule.getDateOffset());
				stmtSaveDateRule.executeUpdate();
				try (ResultSet generatedKeys = stmtSaveDateRule.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						dateRuleId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating date rule failed, no generated key obtained.");
					}
				}
			} else {
				// The date rule already exists.
				dateRuleId = dateRule.getId();
				// 1. Delete sub date rules and months linked to this date rule.
				try (PreparedStatement stmtDeleteMonthsByDateRuleId = con
						.prepareStatement("DELETE FROM MONTH WHERE DATE_RULE_ID = ?");
						PreparedStatement stmtDeleteSubDateRulesByDateRuleId = con
								.prepareStatement("DELETE FROM DATE_RULE_SUB_DATE_RULE WHERE DATE_RULE_ID = ?");
						PreparedStatement stmtDeleteCalendarsByDateRuleId = con
								.prepareStatement("DELETE FROM DATE_RULE_CALENDAR WHERE DATE_RULE_ID = ?")) {
					stmtDeleteMonthsByDateRuleId.setLong(1, dateRuleId);
					stmtDeleteMonthsByDateRuleId.executeUpdate();
					stmtDeleteCalendarsByDateRuleId.setLong(1, dateRuleId);
					stmtDeleteCalendarsByDateRuleId.executeUpdate();
					stmtDeleteSubDateRulesByDateRuleId.setLong(1, dateRuleId);
					stmtDeleteSubDateRulesByDateRuleId.executeUpdate();

					// 2. Update the date rule
					DateRollingConvention drc = dateRule.getDateRollingConvention();
					if (drc != null) {
						stmtSaveDateRule.setString(1, dateRule.getDateRollingConvention().name());
					} else {
						stmtSaveDateRule.setNull(1, Types.VARCHAR);
					}
					String position = dateRule.getPosition();
					if (position != null) {
						stmtSaveDateRule.setString(2, dateRule.getPosition());
					} else {
						stmtSaveDateRule.setNull(2, Types.VARCHAR);
					}
					stmtSaveDateRule.setBoolean(3, dateRule.isSequence());
					DayOfWeek day = dateRule.getDay();
					if (day != null) {
						stmtSaveDateRule.setString(4, day.toString());
					} else {
						stmtSaveDateRule.setNull(4, Types.VARCHAR);
					}
					stmtSaveDateRule.setShort(5, dateRule.getDateOffset());
					stmtSaveDateRule.setLong(6, dateRuleId);
					stmtSaveDateRule.executeUpdate();
				}
			}

			// 3. Insert month, sub date rules new values
			if (dateRule.getMonths() != null) {
				for (Month month : dateRule.getMonths()) {
					stmtSaveMonthByDateRuleId.clearParameters();
					stmtSaveMonthByDateRuleId.setString(1, month.name());
					stmtSaveMonthByDateRuleId.setLong(2, dateRuleId);
					stmtSaveMonthByDateRuleId.addBatch();
				}
				stmtSaveMonthByDateRuleId.executeBatch();
			}

			if (dateRule.getDateRulesPeriods() != null) {
				int pos = 1;
				for (Map.Entry<DateRule, Period> dateRulePeriod : dateRule.getDateRulesPeriods().entrySet()) {
					stmtSaveDateRuleByDateRuleId.clearParameters();
					stmtSaveDateRuleByDateRuleId.setInt(1, dateRulePeriod.getValue().getYears());
					stmtSaveDateRuleByDateRuleId.setInt(2, dateRulePeriod.getValue().getMonths());
					stmtSaveDateRuleByDateRuleId.setInt(3, dateRulePeriod.getValue().getDays());
					stmtSaveDateRuleByDateRuleId.setInt(4, pos);
					stmtSaveDateRuleByDateRuleId.setLong(5, dateRuleId);
					stmtSaveDateRuleByDateRuleId.setLong(6, dateRulePeriod.getKey().getId());
					stmtSaveDateRuleByDateRuleId.addBatch();
					pos++;
				}
				stmtSaveDateRuleByDateRuleId.executeBatch();
			}

			if (dateRule.getCalendars() != null) {
				for (Calendar cal : dateRule.getCalendars()) {
					stmtSaveCalendarByDateRuleId.clearParameters();
					stmtSaveCalendarByDateRuleId.setLong(1, dateRuleId);
					stmtSaveCalendarByDateRuleId.setLong(2, cal.getId());
					stmtSaveCalendarByDateRuleId.addBatch();
				}
				stmtSaveCalendarByDateRuleId.executeBatch();
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		dateRule.setId(dateRuleId);
		return dateRuleId;
	}

}