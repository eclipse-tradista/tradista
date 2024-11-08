package org.eclipse.tradista.core.calendar.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;

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

public class CalendarSQL {

	public static Calendar getCalendarById(long id) {
		Calendar calendar = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCalendarById = con.prepareStatement("SELECT * FROM CALENDAR WHERE ID = ? ");
				PreparedStatement stmtGetHolidaysByCalendarId = con
						.prepareStatement("SELECT * FROM HOLIDAY WHERE HOLIDAY.CALENDAR_ID = ? ");
				PreparedStatement stmtGetWeekEndByCalendarId = con
						.prepareStatement("SELECT * FROM WEEK_END WHERE WEEK_END.CALENDAR_ID = ? ")) {
			stmtGetCalendarById.setLong(1, id);
			stmtGetHolidaysByCalendarId.setLong(1, id);
			stmtGetWeekEndByCalendarId.setLong(1, id);
			try (ResultSet results = stmtGetCalendarById.executeQuery()) {
				while (results.next()) {
					calendar = new Calendar(results.getString("code"));
					calendar.setId(id);
					calendar.setName(results.getString("name"));
					try (ResultSet holidaysResults = stmtGetHolidaysByCalendarId.executeQuery();
							ResultSet weekEndResults = stmtGetWeekEndByCalendarId.executeQuery()) {
						Set<DayOfWeek> weekEnd = new HashSet<DayOfWeek>();
						Set<LocalDate> nonBusinessDays = new HashSet<LocalDate>();
						while (holidaysResults.next()) {
							nonBusinessDays.add(holidaysResults.getDate("date").toLocalDate());
						}
						while (weekEndResults.next()) {
							weekEnd.add(DayOfWeek.valueOf(weekEndResults.getString("day")));
						}
						if (!weekEnd.isEmpty()) {
							calendar.setWeekEnd(weekEnd);
						}
						if (!nonBusinessDays.isEmpty()) {
							calendar.setHolidays(nonBusinessDays);
						}
					}
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return calendar;
	}

	public static Calendar getCalendarByCode(String code) {
		Calendar calendar = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCalendarByCode = con
						.prepareStatement("SELECT * FROM APP.CALENDAR WHERE APP.CALENDAR.CODE = ? ");
				PreparedStatement stmtGetHolidaysByCalendarId = con
						.prepareStatement("SELECT * FROM HOLIDAY WHERE HOLIDAY.CALENDAR_ID =  ? ");
				PreparedStatement stmtGetWeekEndByCalendarId = con
						.prepareStatement("SELECT * FROM WEEK_END WHERE WEEK_END.CALENDAR_ID = ? ")) {
			stmtGetCalendarByCode.setString(1, code);
			try (ResultSet results = stmtGetCalendarByCode.executeQuery()) {
				while (results.next()) {
					calendar = new Calendar(results.getString("code"));
					calendar.setName(results.getString("name"));
					long id = results.getLong("id");
					calendar.setId(id);
					stmtGetHolidaysByCalendarId.setLong(1, id);
					stmtGetWeekEndByCalendarId.setLong(1, id);
					try (ResultSet holidaysResults = stmtGetHolidaysByCalendarId.executeQuery();
							ResultSet weekEndResults = stmtGetWeekEndByCalendarId.executeQuery()) {
						Set<DayOfWeek> weekEnd = new HashSet<DayOfWeek>();
						Set<LocalDate> nonBusinessDays = new HashSet<LocalDate>();
						while (holidaysResults.next()) {
							nonBusinessDays.add(holidaysResults.getDate("date").toLocalDate());
						}
						while (weekEndResults.next()) {
							weekEnd.add(DayOfWeek.valueOf(weekEndResults.getString("day")));
						}
						if (!weekEnd.isEmpty()) {
							calendar.setWeekEnd(weekEnd);
						}
						if (!nonBusinessDays.isEmpty()) {
							calendar.setHolidays(nonBusinessDays);
						}
					}
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return calendar;
	}

	public static Calendar getCalendarByName(String name) {
		Calendar calendar = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCalendarByName = con
						.prepareStatement("SELECT * FROM CALENDAR WHERE NAME = ? ");
				PreparedStatement stmtGetHolidaysByCalendarId = con
						.prepareStatement("SELECT * FROM HOLIDAY WHERE HOLIDAY.CALENDAR_ID =  ? ");
				PreparedStatement stmtGetWeekEndByCalendarId = con
						.prepareStatement("SELECT * FROM WEEK_END WHERE WEEK_END.CALENDAR_ID = ? ")) {
			stmtGetCalendarByName.setString(1, name);
			try (ResultSet results = stmtGetCalendarByName.executeQuery()) {
				while (results.next()) {
					calendar = new Calendar(results.getString("code"));
					calendar.setName(results.getString("name"));
					long id = results.getLong("id");
					calendar.setId(id);
					stmtGetHolidaysByCalendarId.setLong(1, id);
					stmtGetWeekEndByCalendarId.setLong(1, id);
					try (ResultSet holidaysResults = stmtGetHolidaysByCalendarId.executeQuery();
							ResultSet weekEndResults = stmtGetWeekEndByCalendarId.executeQuery()) {
						Set<DayOfWeek> weekEnd = new HashSet<DayOfWeek>();
						Set<LocalDate> nonBusinessDays = new HashSet<LocalDate>();
						while (holidaysResults.next()) {
							nonBusinessDays.add(holidaysResults.getDate("date").toLocalDate());
						}
						while (weekEndResults.next()) {
							weekEnd.add(DayOfWeek.valueOf(weekEndResults.getString("day")));
						}
						if (!weekEnd.isEmpty()) {
							calendar.setWeekEnd(weekEnd);
						}
						if (!nonBusinessDays.isEmpty()) {
							calendar.setHolidays(nonBusinessDays);
						}
					}
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return calendar;
	}

	public static Set<Calendar> getAllCalendars() {
		Set<Calendar> calendars = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCalendars = con.prepareStatement("SELECT * FROM CALENDAR");
				PreparedStatement stmtGetHolidaysByCalendarId = con
						.prepareStatement("SELECT * FROM HOLIDAY WHERE HOLIDAY.CALENDAR_ID =  ? ");
				PreparedStatement stmtGetWeekEndByCalendarId = con
						.prepareStatement("SELECT * FROM WEEK_END WHERE WEEK_END.CALENDAR_ID = ? ")) {
			try (ResultSet results = stmtGetCalendars.executeQuery()) {
				while (results.next()) {
					if (calendars == null) {
						calendars = new HashSet<Calendar>();
					}
					Calendar calendar = new Calendar(results.getString("code"));
					long id = results.getLong("id");
					calendar.setId(id);
					stmtGetWeekEndByCalendarId.setLong(1, id);
					stmtGetHolidaysByCalendarId.setLong(1, id);
					calendar.setName(results.getString("name"));
					try (ResultSet holidaysResults = stmtGetHolidaysByCalendarId.executeQuery();
							ResultSet weekEndResults = stmtGetWeekEndByCalendarId.executeQuery()) {
						Set<DayOfWeek> weekEnd = new HashSet<DayOfWeek>();
						Set<LocalDate> nonBusinessDays = new HashSet<LocalDate>();
						while (holidaysResults.next()) {
							nonBusinessDays.add(holidaysResults.getDate("date").toLocalDate());
						}
						while (weekEndResults.next()) {
							weekEnd.add(DayOfWeek.valueOf(weekEndResults.getString("day")));
						}
						if (!weekEnd.isEmpty()) {
							calendar.setWeekEnd(weekEnd);
						}
						if (!nonBusinessDays.isEmpty()) {
							calendar.setHolidays(nonBusinessDays);
						}
					}
					calendars.add(calendar);
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return calendars;
	}

	public static Set<String> getAllCalendarCodes() {
		Set<String> codes = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCalendarCodes = con.prepareStatement("SELECT CODE FROM CALENDAR");
				ResultSet results = stmtGetCalendarCodes.executeQuery()) {
			while (results.next()) {
				if (codes == null) {
					codes = new HashSet<String>();
				}
				String code = results.getString("code");
				codes.add(code);
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return codes;
	}

	public static long saveCalendar(Calendar calendar) {
		long calendarId = 0;
		// 1. Check if the calendar already exists
		boolean exists = calendar.getId() != 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveWeekEndByCalendarId = (calendar.getWeekEnd() != null)
						? con.prepareStatement("INSERT INTO WEEK_END(DAY, CALENDAR_ID) VALUES (?, ?) ")
						: null;
				PreparedStatement stmtSaveHolidaysByCalendarId = (calendar.getHolidays() != null)
						? con.prepareStatement("INSERT INTO HOLIDAY(DATE, CALENDAR_ID) VALUES (?, ?) ")
						: null;) {
			// The calendar doesn't exist
			if (!exists) {
				try (PreparedStatement stmtSaveCalendar = con.prepareStatement(
						"INSERT INTO CALENDAR(CODE, NAME) VALUES (?, ?) ", Statement.RETURN_GENERATED_KEYS)) {
					stmtSaveCalendar.setString(1, calendar.getCode());
					stmtSaveCalendar.setString(2, calendar.getName());
					stmtSaveCalendar.executeUpdate();
					try (ResultSet generatedKeys = stmtSaveCalendar.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							calendarId = generatedKeys.getLong(1);
						} else {
							throw new SQLException("Creating calendar failed, no generated key obtained.");
						}
					}
					if (calendar.getWeekEnd() != null) {
						for (DayOfWeek day : calendar.getWeekEnd()) {
							stmtSaveWeekEndByCalendarId.clearParameters();
							stmtSaveWeekEndByCalendarId.setString(1, day.name());
							stmtSaveWeekEndByCalendarId.setLong(2, calendarId);
							stmtSaveWeekEndByCalendarId.addBatch();
						}
						stmtSaveWeekEndByCalendarId.executeBatch();
					}
					if (calendar.getHolidays() != null) {
						for (LocalDate date : calendar.getHolidays()) {
							stmtSaveHolidaysByCalendarId.clearParameters();
							stmtSaveHolidaysByCalendarId.setDate(1, java.sql.Date.valueOf(date));
							stmtSaveHolidaysByCalendarId.setLong(2, calendarId);
							stmtSaveHolidaysByCalendarId.addBatch();
						}
						stmtSaveHolidaysByCalendarId.executeBatch();
					}
				}
			} else {
				// The calendar already exists.
				calendarId = calendar.getId();
				// 1. Delete holidays and week ends linked to this calendar.

				try (PreparedStatement stmtDeleteWeekEndByCalendarId = con
						.prepareStatement("DELETE FROM WEEK_END WHERE CALENDAR_ID = ?");
						PreparedStatement stmtDeleteHolidaysByCalendarId = con
								.prepareStatement("DELETE FROM HOLIDAY WHERE CALENDAR_ID = ?");
						PreparedStatement stmtUpdateCalendarByCalendarId = con
								.prepareStatement("UPDATE CALENDAR SET NAME = ? WHERE ID = ? ")) {
					stmtDeleteWeekEndByCalendarId.setLong(1, calendarId);
					stmtDeleteWeekEndByCalendarId.executeUpdate();
					stmtDeleteHolidaysByCalendarId.setLong(1, calendarId);
					stmtDeleteHolidaysByCalendarId.executeUpdate();
					// 2. Update the calendar
					stmtUpdateCalendarByCalendarId.setString(1, calendar.getName());
					stmtUpdateCalendarByCalendarId.setLong(2, calendarId);
					stmtUpdateCalendarByCalendarId.executeUpdate();
				}

				// 3. Insert week end and holidays new values
				if (calendar.getWeekEnd() != null) {
					for (DayOfWeek day : calendar.getWeekEnd()) {
						stmtSaveWeekEndByCalendarId.clearParameters();
						stmtSaveWeekEndByCalendarId.setString(1, day.name());
						stmtSaveWeekEndByCalendarId.setLong(2, calendarId);
						stmtSaveWeekEndByCalendarId.addBatch();
					}
					stmtSaveWeekEndByCalendarId.executeBatch();
				}

				if (calendar.getHolidays() != null) {
					for (LocalDate date : calendar.getHolidays()) {
						stmtSaveHolidaysByCalendarId.clearParameters();
						stmtSaveHolidaysByCalendarId.setDate(1, java.sql.Date.valueOf(date));
						stmtSaveHolidaysByCalendarId.setLong(2, calendarId);
						stmtSaveHolidaysByCalendarId.addBatch();
					}
					stmtSaveHolidaysByCalendarId.executeBatch();
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		calendar.setId(calendarId);
		return calendarId;
	}

	public static long[] saveCalendars(Set<Calendar> calendars) {
		// First, we retrieve the existing calendars, because we don't want to
		// delete their weekend and name
		// This because Copp Clark data don't contain name and week end, only
		// holidays.
		List<Calendar> calToSave = new ArrayList<Calendar>(calendars.size());
		long[] ids = new long[calendars.size()];
		int i = 0;
		for (Calendar cal : calendars) {
			Calendar existingCalendar = getCalendarByCode(cal.getCode());
			if (existingCalendar != null) {
				if (existingCalendar.getName() != null) {
					cal.setName(existingCalendar.getName());
				}
				if (existingCalendar.getWeekEnd() != null) {
					cal.setWeekEnd(existingCalendar.getWeekEnd());
				}
			}
			calToSave.add(cal);
		}
		for (Calendar cal : calToSave) {
			ids[i] = saveCalendar(cal);
			i++;
		}
		return ids;
	}

	public static long[] addHolidays(Set<Calendar> calendars) {
		// First, we retrieve the existing calendars, because we don't want to
		// delete their weekend and name
		// This because Copp Clark data don't contain name and week end, only
		// holidays.
		List<Calendar> calToSave = new ArrayList<Calendar>(calendars.size());
		long[] ids = new long[calendars.size()];
		int i = 0;
		for (Calendar cal : calendars) {
			Calendar existingCalendar = getCalendarByCode(cal.getCode());
			if (existingCalendar != null) {
				if (existingCalendar.getName() != null) {
					cal.setName(existingCalendar.getName());
				}
				if (existingCalendar.getWeekEnd() != null) {
					cal.setWeekEnd(existingCalendar.getWeekEnd());
				}
				if (existingCalendar.getHolidays() != null) {
					cal.addHolidays(existingCalendar.getHolidays());
				}
			}
			calToSave.add(cal);
		}
		for (Calendar cal : calToSave) {
			ids[i] = saveCalendar(cal);
			i++;
		}
		return ids;
	}

}