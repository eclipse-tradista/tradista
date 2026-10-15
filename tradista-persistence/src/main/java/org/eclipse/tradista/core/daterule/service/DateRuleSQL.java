package org.eclipse.tradista.core.daterule.service;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CALENDAR_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.calendar.persistence.CalendarSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
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

	private static final String DATE_RULE_ID = "DATE_RULE_ID";
	// ---- DATE_RULE table ----
	public static final Field ID_FIELD = new Field(ID);
	public static final Field NAME_FIELD = new Field(NAME);
	public static final Field DATE_ROLLING_CONVENTION_FIELD = new Field("DATE_ROLLING_CONVENTION");
	public static final Field POSITION_FIELD = new Field("POSITION");
	public static final Field IS_SEQUENCE_FIELD = new Field("IS_SEQUENCE");
	public static final Field WEEK_DAY_FIELD = new Field("WEEK_DAY");
	public static final Field DATE_OFFSET_FIELD = new Field("DATE_OFFSET");

	private static final Field[] DATE_RULE_FIELDS = { ID_FIELD, NAME_FIELD, DATE_ROLLING_CONVENTION_FIELD,
			POSITION_FIELD, IS_SEQUENCE_FIELD, WEEK_DAY_FIELD, DATE_OFFSET_FIELD };

	private static final Field[] DATE_RULE_FIELDS_FOR_INSERT = { NAME_FIELD, DATE_ROLLING_CONVENTION_FIELD,
			POSITION_FIELD, IS_SEQUENCE_FIELD, WEEK_DAY_FIELD, DATE_OFFSET_FIELD };

	private static final Field[] DATE_RULE_FIELDS_FOR_UPDATE = { DATE_ROLLING_CONVENTION_FIELD, POSITION_FIELD,
			IS_SEQUENCE_FIELD, WEEK_DAY_FIELD, DATE_OFFSET_FIELD };

	public static final Table DATE_RULE_TABLE = new Table("DATE_RULE", DATE_RULE_FIELDS);

	// ---- DATE_RULE_CALENDAR table ----
	private static final Field DRC_DATE_RULE_ID_FIELD = new Field(DATE_RULE_ID);
	private static final Field DRC_CALENDAR_ID_FIELD = new Field(CALENDAR_ID);

	private static final Field[] DATE_RULE_CALENDAR_FIELDS = { DRC_DATE_RULE_ID_FIELD, DRC_CALENDAR_ID_FIELD };

	public static final Table DATE_RULE_CALENDAR_TABLE = new Table("DATE_RULE_CALENDAR", DATE_RULE_CALENDAR_FIELDS);

	// ---- DATE_RULE_SUB_DATE_RULE table ----
	private static final Field SDR_DATE_RULE_ID_FIELD = new Field(DATE_RULE_ID);
	private static final Field SDR_SUB_DATE_RULE_ID_FIELD = new Field("SUB_DATE_RULE_ID");
	private static final Field SDR_POSITION_FIELD = new Field("POSITION");
	private static final Field SDR_DURATION_DAY_FIELD = new Field("DURATION_DAY");
	private static final Field SDR_DURATION_MONTH_FIELD = new Field("DURATION_MONTH");
	private static final Field SDR_DURATION_YEAR_FIELD = new Field("DURATION_YEAR");

	private static final Field[] DATE_RULE_SUB_DATE_RULE_FIELDS = { SDR_DURATION_YEAR_FIELD, SDR_DURATION_MONTH_FIELD,
			SDR_DURATION_DAY_FIELD, SDR_POSITION_FIELD, SDR_DATE_RULE_ID_FIELD, SDR_SUB_DATE_RULE_ID_FIELD };

	public static final Table DATE_RULE_SUB_DATE_RULE_TABLE = new Table("DATE_RULE_SUB_DATE_RULE",
			DATE_RULE_SUB_DATE_RULE_FIELDS);

	// ---- MONTH table ----
	private static final Field MONTH_ID_FIELD = new Field(ID);
	private static final Field MONTH_MONTH_FIELD = new Field("MONTH");
	private static final Field MONTH_DATE_RULE_ID_FIELD = new Field(DATE_RULE_ID);

	private static final Field[] MONTH_FIELDS = { MONTH_ID_FIELD, MONTH_MONTH_FIELD, MONTH_DATE_RULE_ID_FIELD };

	private static final Field[] MONTH_FIELDS_FOR_INSERT = { MONTH_MONTH_FIELD, MONTH_DATE_RULE_ID_FIELD };

	public static final Table MONTH_TABLE = new Table("MONTH", MONTH_FIELDS);

	public static DateRule getDateRuleById(long id) {
		DateRule dateRule = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(DATE_RULE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetDateRuleById = con.prepareStatement(sql.toString())) {
			stmtGetDateRuleById.setLong(1, id);
			try (ResultSet results = stmtGetDateRuleById.executeQuery()) {
				while (results.next()) {
					dateRule = buildDateRule(results);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return dateRule;
	}

	private static Set<Calendar> getDateRuleCalendars(long id) {
		Set<Calendar> calendars = null;
		StringBuilder sql = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(DRC_CALENDAR_ID_FIELD, DATE_RULE_CALENDAR_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, DRC_DATE_RULE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCalendarsByDateRuleId = con.prepareStatement(sql.toString())) {
			stmtGetCalendarsByDateRuleId.setLong(1, id);
			try (ResultSet results = stmtGetCalendarsByDateRuleId.executeQuery()) {
				while (results.next()) {
					if (calendars == null) {
						calendars = new HashSet<>();
					}
					calendars.add(CalendarSQL.getCalendarById(results.getLong(DRC_CALENDAR_ID_FIELD.getName())));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return calendars;
	}

	private static List<DateRule.Step> getDateRuleStepsByDateRuleId(long id) {
		List<DateRule.Step> steps = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(DATE_RULE_SUB_DATE_RULE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, SDR_DATE_RULE_ID_FIELD);
		sql.append(" ORDER BY ").append(SDR_POSITION_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetDateRulesPeriodsByDateRuleId = con.prepareStatement(sql.toString())) {
			stmtGetDateRulesPeriodsByDateRuleId.setLong(1, id);
			try (ResultSet results = stmtGetDateRulesPeriodsByDateRuleId.executeQuery()) {
				while (results.next()) {
					if (steps == null) {
						steps = new ArrayList<>();
					}
					DateRule subDateRule = getDateRuleById(results.getLong(SDR_SUB_DATE_RULE_ID_FIELD.getName()));

					Period period = Period.of(results.getInt(SDR_DURATION_YEAR_FIELD.getName()),
							results.getInt(SDR_DURATION_MONTH_FIELD.getName()),
							results.getInt(SDR_DURATION_DAY_FIELD.getName()));

					steps.add(new DateRule.Step(results.getInt(SDR_POSITION_FIELD.getName()), subDateRule, period));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return steps;
	}

	private static Set<Month> getDateRuleMonths(long id) {
		Set<Month> months = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(MONTH_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, MONTH_DATE_RULE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMonthsByDateRuleId = con.prepareStatement(sql.toString())) {
			stmtGetMonthsByDateRuleId.setLong(1, id);
			try (ResultSet results = stmtGetMonthsByDateRuleId.executeQuery()) {
				while (results.next()) {
					if (months == null) {
						months = new HashSet<>();
					}
					months.add(Month.valueOf(results.getString(MONTH_MONTH_FIELD.getName())));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return months;
	}

	public static Set<DateRule> getAllDateRules() {
		Set<DateRule> dateRules = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllDateRules = con
						.prepareStatement(TradistaDBUtil.buildSelectQuery(DATE_RULE_TABLE));
				ResultSet results = stmtGetAllDateRules.executeQuery()) {
			while (results.next()) {
				if (dateRules == null) {
					dateRules = new TreeSet<>();
				}
				dateRules.add(buildDateRule(results));
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return dateRules;
	}

	public static DateRule getDateRuleByName(String name) {
		DateRule dateRule = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(DATE_RULE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetDateRuleByName = con.prepareStatement(sql.toString())) {
			stmtGetDateRuleByName.setString(1, name);
			try (ResultSet results = stmtGetDateRuleByName.executeQuery()) {
				while (results.next()) {
					dateRule = buildDateRule(results);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
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
						? TradistaDBUtil.buildInsertPreparedStatement(con, MONTH_TABLE, MONTH_FIELDS_FOR_INSERT)
						: null;
				PreparedStatement stmtSaveDateRuleByDateRuleId = (dateRule.getSteps() != null)
						? TradistaDBUtil.buildInsertPreparedStatement(con, DATE_RULE_SUB_DATE_RULE_TABLE,
								DATE_RULE_SUB_DATE_RULE_FIELDS)
						: null;
				PreparedStatement stmtSaveCalendarByDateRuleId = (dateRule.getCalendars() != null)
						? TradistaDBUtil.buildInsertPreparedStatement(con, DATE_RULE_CALENDAR_TABLE,
								DATE_RULE_CALENDAR_FIELDS)
						: null;
				PreparedStatement stmtSaveDateRule = (!exists)
						? TradistaDBUtil.buildInsertPreparedStatement(con, DATE_RULE_TABLE, DATE_RULE_FIELDS_FOR_INSERT)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, DATE_RULE_TABLE,
								DATE_RULE_FIELDS_FOR_UPDATE)) {

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
				try (PreparedStatement stmtDeleteMonthsByDateRuleId = TradistaDBUtil.buildDeletePreparedStatement(con,
						MONTH_TABLE, MONTH_DATE_RULE_ID_FIELD);
						PreparedStatement stmtDeleteCalendarsByDateRuleId = TradistaDBUtil
								.buildDeletePreparedStatement(con, DATE_RULE_CALENDAR_TABLE, DRC_DATE_RULE_ID_FIELD);
						PreparedStatement stmtDeleteSubDateRulesByDateRuleId = TradistaDBUtil
								.buildDeletePreparedStatement(con, DATE_RULE_SUB_DATE_RULE_TABLE,
										SDR_DATE_RULE_ID_FIELD)) {
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

			if (dateRule.getSteps() != null) {
				int pos = 1;
				for (DateRule.Step step : dateRule.getSteps()) {
					stmtSaveDateRuleByDateRuleId.clearParameters();
					stmtSaveDateRuleByDateRuleId.setInt(1, step.getDuration().getYears());
					stmtSaveDateRuleByDateRuleId.setInt(2, step.getDuration().getMonths());
					stmtSaveDateRuleByDateRuleId.setInt(3, step.getDuration().getDays());
					stmtSaveDateRuleByDateRuleId.setInt(4, pos);
					stmtSaveDateRuleByDateRuleId.setLong(5, dateRuleId);
					stmtSaveDateRuleByDateRuleId.setLong(6, step.getDateRule().getId());
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
			throw new TradistaTechnicalException(sqle);
		}
		dateRule.setId(dateRuleId);
		return dateRuleId;
	}

	private static DateRule buildDateRule(ResultSet results) throws SQLException, TradistaBusinessException {
		DateRule dateRule = new DateRule(results.getString(NAME_FIELD.getName()));
		dateRule.setId(results.getLong(ID_FIELD.getName()));
		dateRule.setSequence(results.getBoolean(IS_SEQUENCE_FIELD.getName()));
		if (dateRule.isSequence()) {
			dateRule.setSteps(getDateRuleStepsByDateRuleId(dateRule.getId()));
		} else {
			String drc = results.getString(DATE_ROLLING_CONVENTION_FIELD.getName());
			if (drc != null) {
				dateRule.setDateRollingConvention(DateRollingConvention.valueOf(drc));
			}
			dateRule.setPosition(results.getString(POSITION_FIELD.getName()));
			String dayString = results.getString(WEEK_DAY_FIELD.getName());
			if (dayString != null) {
				dateRule.setDay(DayOfWeek.valueOf(dayString));
			}
			dateRule.setDateOffset(results.getShort(DATE_OFFSET_FIELD.getName()));
			dateRule.setMonths(getDateRuleMonths(dateRule.getId()));
			dateRule.setCalendars(getDateRuleCalendars(dateRule.getId()));
		}
		return dateRule;
	}

}