package org.eclipse.tradista.core.marketdata.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;

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

public class QuoteSQL {

	public static long saveQuote(Quote quote) {
		long quoteId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveQuote = (quote.getId() == 0)
						? con.prepareStatement("INSERT INTO QUOTE(NAME, TYPE) VALUES(?, ?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement("UPDATE QUOTE SET NAME=?, TYPE=? WHERE ID=?")) {
			if (quote.getId() != 0) {
				stmtSaveQuote.setLong(3, quote.getId());
			}
			stmtSaveQuote.setString(1, quote.getName());
			stmtSaveQuote.setString(2, quote.getType().name());
			stmtSaveQuote.executeUpdate();

			if (quote.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveQuote.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						quoteId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating quote failed, no generated key obtained.");
					}
				}
			} else {
				quoteId = quote.getId();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		quote.setId(quoteId);
		return quoteId;
	}

	public static boolean deleteQuote(String quoteName, QuoteType quoteType) {
		boolean bSaved = false;
		String quoteValueQuery = "DELETE FROM QUOTE_VALUE WHERE QUOTE_ID IN (SELECT ID FROM QUOTE WHERE NAME = ?";
		String quoteQuery = "DELETE FROM QUOTE WHERE NAME = ?";

		if (quoteType != null) {
			quoteValueQuery += " AND TYPE = ?)";
		} else {
			quoteValueQuery += ")";
		}
		if (quoteType != null) {
			quoteQuery += " AND TYPE = ?";
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteQuoteValues = con.prepareStatement(quoteValueQuery);
				PreparedStatement stmtDeleteQuote = con.prepareStatement(quoteQuery)) {
			stmtDeleteQuoteValues.setString(1, quoteName);
			if (quoteType != null) {
				stmtDeleteQuoteValues.setString(2, quoteType.name());
			}
			stmtDeleteQuoteValues.executeUpdate();

			stmtDeleteQuote.setString(1, quoteName);
			if (quoteType != null) {
				stmtDeleteQuote.setString(2, quoteType.name());
			}
			stmtDeleteQuote.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static List<Quote> getAllQuotes() {
		List<Quote> quotes = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllQuotes = con
						.prepareStatement("SELECT QUOTE.ID ID, QUOTE.NAME NAME, QUOTE.TYPE TYPE FROM QUOTE");
				ResultSet results = stmtGetAllQuotes.executeQuery()) {
			while (results.next()) {
				long quoteId = results.getLong("id");
				String quoteName = results.getString("name");
				QuoteType quoteType = QuoteType.valueOf(results.getString("type"));
				Quote quote = new Quote(quoteId, quoteName, quoteType);
				if (quotes == null) {
					quotes = new ArrayList<>();
				}
				quotes.add(quote);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quotes;
	}

	public static List<QuoteValue> getQuoteValuesByQuoteSetIdQuoteNameAndDate(long quoteSetId, String name,
			LocalDate date) {
		List<QuoteValue> quoteValues = null;
		String query = "SELECT QUOTE.ID ID, QUOTE_VALUE.DATE DATE, QUOTE_VALUE.BID BID, QUOTE_VALUE.ASK ASK, "
				+ "QUOTE_VALUE.OPEN_ OPEN_, QUOTE_VALUE.CLOSE_ CLOSE_, QUOTE_VALUE.HIGH HIGH, "
				+ "QUOTE_VALUE.LOW LOW, QUOTE_VALUE.LAST_ LAST_, QUOTE_VALUE.ENTERED_DATE ENTERED_DATE, "
				+ "QUOTE_VALUE.SOURCE_NAME SOURCE_NAME, QUOTE.TYPE TYPE, QUOTE.NAME NAME, "
				+ "QUOTE_SET.ID QUOTE_SET_ID "
				+ "FROM QUOTE, QUOTE_VALUE, QUOTE_SET WHERE QUOTE.ID = QUOTE_VALUE.QUOTE_ID"
				+ " AND QUOTE_VALUE.QUOTE_SET_ID = QUOTE_SET.ID AND QUOTE_SET.ID = ?" + " AND DATE = ? AND QUOTE.NAME";
		QuoteSet quoteSet = QuoteSetSQL.getQuoteSetById(quoteSetId);
		if (!name.contains("%")) {
			query += " = ?";
		} else {
			query += " LIKE ?";
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteValuesByQuoteSetIdQuoteNameAndDate = con.prepareStatement(query)) {
			stmtGetQuoteValuesByQuoteSetIdQuoteNameAndDate.setLong(1, quoteSetId);
			stmtGetQuoteValuesByQuoteSetIdQuoteNameAndDate.setDate(2, java.sql.Date.valueOf(date));
			stmtGetQuoteValuesByQuoteSetIdQuoteNameAndDate.setString(3, name);
			try (ResultSet results = stmtGetQuoteValuesByQuoteSetIdQuoteNameAndDate.executeQuery()) {
				while (results.next()) {
					LocalDate quoteDate = results.getDate("date").toLocalDate();
					BigDecimal bid = results.getBigDecimal("bid");
					BigDecimal ask = results.getBigDecimal("ask");
					BigDecimal open = results.getBigDecimal("open_");
					BigDecimal close = results.getBigDecimal("close_");
					BigDecimal high = results.getBigDecimal("high");
					BigDecimal low = results.getBigDecimal("low");
					BigDecimal last = results.getBigDecimal("last_");
					LocalDate enteredDate = results.getDate("entered_date").toLocalDate();
					String sourceName = results.getString("source_name");
					Quote quote = QuoteSQL.getQuoteById(results.getLong("id"));
					if (quoteValues == null) {
						quoteValues = new ArrayList<QuoteValue>();
					}
					quoteValues.add(new QuoteValue(quoteDate, bid, ask, open, close, high, low, last, sourceName, quote,
							enteredDate, quoteSet));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quoteValues;
	}

	public static QuoteValue getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(long quoteSetId, String name,
			QuoteType quoteType, LocalDate date) {
		QuoteValue quoteValue = null;
		QuoteSet quoteSet = QuoteSetSQL.getQuoteSetById(quoteSetId);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate = con
						.prepareStatement("SELECT QUOTE.ID ID, QUOTE_VALUE.DATE DATE, QUOTE_VALUE.BID BID, "
								+ "QUOTE_VALUE.ASK ASK, " + "QUOTE_VALUE.OPEN_ OPEN_, " + "QUOTE_VALUE.CLOSE_ CLOSE_, "
								+ "QUOTE_VALUE.HIGH HIGH, " + "QUOTE_VALUE.LOW LOW, " + "QUOTE_VALUE.LAST_ LAST_, "
								+ "QUOTE_VALUE.ENTERED_DATE ENTERED_DATE, " + "QUOTE_VALUE.SOURCE_NAME SOURCE_NAME, "
								+ "QUOTE.TYPE TYPE, " + "QUOTE.NAME NAME, " + "QUOTE_SET.ID QUOTE_SET_ID "
								+ "FROM QUOTE, QUOTE_VALUE, QUOTE_SET WHERE QUOTE.ID = QUOTE_VALUE.QUOTE_ID"
								+ " AND QUOTE_VALUE.QUOTE_SET_ID = QUOTE_SET.ID" + " AND QUOTE_SET.ID = ?"
								+ " AND QUOTE.NAME = ? AND DATE = ? AND TYPE=?")) {
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setLong(1, quoteSetId);
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setString(2, name);
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setDate(3, java.sql.Date.valueOf(date));
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setString(4, quoteType.name());
			try (ResultSet results = stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.executeQuery()) {
				while (results.next()) {
					LocalDate quoteDate = results.getDate("date").toLocalDate();
					BigDecimal bid = results.getBigDecimal("bid");
					BigDecimal ask = results.getBigDecimal("ask");
					BigDecimal open = results.getBigDecimal("open_");
					BigDecimal close = results.getBigDecimal("close_");
					BigDecimal high = results.getBigDecimal("high");
					BigDecimal low = results.getBigDecimal("low");
					BigDecimal last = results.getBigDecimal("last_");
					LocalDate enteredDate = results.getDate("entered_date").toLocalDate();
					String sourceName = results.getString("source_name");
					Quote quote = QuoteSQL.getQuoteById(results.getLong("id"));
					quoteValue = new QuoteValue(quoteDate, bid, ask, open, close, high, low, last, sourceName, quote,
							enteredDate, quoteSet);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quoteValue;
	}

	public static Set<QuoteValue> getQuoteValueByQuoteSetIdQuoteNameTypeAndDates(long quoteSetId, String name,
			QuoteType quoteType, LocalDate startDate, LocalDate endDate) {
		Set<QuoteValue> quoteValues = null;
		QuoteSet quoteSet = QuoteSetSQL.getQuoteSetById(quoteSetId);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate = con
						.prepareStatement("SELECT QUOTE.ID ID, QUOTE_VALUE.DATE DATE, QUOTE_VALUE.BID BID, "
								+ "QUOTE_VALUE.ASK ASK, " + "QUOTE_VALUE.OPEN_ OPEN_, " + "QUOTE_VALUE.CLOSE_ CLOSE_, "
								+ "QUOTE_VALUE.HIGH HIGH, " + "QUOTE_VALUE.LOW LOW, " + "QUOTE_VALUE.LAST_ LAST_, "
								+ "QUOTE_VALUE.ENTERED_DATE ENTERED_DATE, " + "QUOTE_VALUE.SOURCE_NAME SOURCE_NAME, "
								+ "QUOTE.TYPE TYPE, " + "QUOTE.NAME NAME, " + "QUOTE_SET.ID QUOTE_SET_ID "
								+ "FROM QUOTE, QUOTE_VALUE, QUOTE_SET WHERE QUOTE.ID = QUOTE_VALUE.QUOTE_ID"
								+ " AND QUOTE_VALUE.QUOTE_SET_ID = QUOTE_SET.ID" + " AND QUOTE_SET.ID = ?"
								+ " AND QUOTE.NAME = ? AND TYPE=? AND DATE BETWEEN ? AND ?")) {
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setLong(1, quoteSetId);
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setString(2, name);
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setString(3, quoteType.name());
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setDate(4, java.sql.Date.valueOf(startDate));
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setDate(5, java.sql.Date.valueOf(endDate));
			try (ResultSet results = stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.executeQuery()) {
				while (results.next()) {
					if (quoteValues == null) {
						quoteValues = new TreeSet<>();
					}
					LocalDate quoteDate = results.getDate("date").toLocalDate();
					BigDecimal bid = results.getBigDecimal("bid");
					BigDecimal ask = results.getBigDecimal("ask");
					BigDecimal open = results.getBigDecimal("open_");
					BigDecimal close = results.getBigDecimal("close_");
					BigDecimal high = results.getBigDecimal("high");
					BigDecimal low = results.getBigDecimal("low");
					BigDecimal last = results.getBigDecimal("last_");
					LocalDate enteredDate = results.getDate("entered_date").toLocalDate();
					String sourceName = results.getString("source_name");
					Quote quote = QuoteSQL.getQuoteById(results.getLong("id"));
					quoteValues.add(new QuoteValue(quoteDate, bid, ask, open, close, high, low, last, sourceName, quote,
							enteredDate, quoteSet));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quoteValues;
	}

	private static boolean isEmpty(QuoteValue quoteValue) {
		if (quoteValue == null) {
			return true;
		}

		if (quoteValue.getBid() != null) {
			return false;
		}

		if (quoteValue.getAsk() != null) {
			return false;
		}

		if (quoteValue.getOpen() != null) {
			return false;
		}

		if (quoteValue.getClose() != null) {
			return false;
		}

		if (quoteValue.getHigh() != null) {
			return false;
		}

		if (quoteValue.getLow() != null) {
			return false;
		}

		if (quoteValue.getLast() != null) {
			return false;
		}

		return true;
	}

	public static Quote getQuoteByNameAndType(String quoteName, QuoteType quoteType) {
		Quote quote = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteByNameAndType = con.prepareStatement(
						"SELECT QUOTE.ID ID, QUOTE.NAME NAME, QUOTE.TYPE TYPE FROM QUOTE WHERE QUOTE.NAME = ? AND QUOTE.TYPE = ?")) {
			stmtGetQuoteByNameAndType.setString(1, quoteName);
			stmtGetQuoteByNameAndType.setString(2, quoteType.name());
			try (ResultSet results = stmtGetQuoteByNameAndType.executeQuery()) {
				while (results.next()) {
					long quoteId = results.getLong("id");
					quote = new Quote(quoteId, quoteName, quoteType);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quote;
	}

	public static Quote getQuoteById(long quoteId) {
		Quote quote = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteById = con.prepareStatement(
						"SELECT QUOTE.ID ID, QUOTE.NAME NAME, QUOTE.TYPE TYPE FROM QUOTE WHERE QUOTE.ID = ?")) {
			stmtGetQuoteById.setLong(1, quoteId);
			try (ResultSet results = stmtGetQuoteById.executeQuery()) {
				while (results.next()) {
					String quoteName = results.getString("name");
					QuoteType quoteType = QuoteType.valueOf(results.getString("type"));
					quote = new Quote(quoteId, quoteName, quoteType);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quote;
	}

	public static List<Quote> getQuotesByCurveId(long curveId) {
		List<Quote> quotes = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuotesByCurveId = con
						.prepareStatement("SELECT * FROM QUOTE WHERE ID IN (SELECT QUOTE_ID FROM CURVE_QUOTE "
								+ " WHERE CURVE_ID = ?) ")) {
			stmtGetQuotesByCurveId.setLong(1, curveId);
			try (ResultSet results = stmtGetQuotesByCurveId.executeQuery()) {
				while (results.next()) {
					long id = results.getLong("id");
					String name = results.getString("name");
					QuoteType type = QuoteType.valueOf(results.getString("type"));
					if (quotes == null) {
						quotes = new ArrayList<>();
					}
					quotes.add(new Quote(id, name, type));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quotes;
	}

	public static List<Quote> getQuotesBySurfaceId(long surfaceId) {
		List<Quote> quotes = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuotesBySurfaceId = con.prepareStatement(
						"SELECT * FROM QUOTE WHERE ID IN (SELECT QUOTE_ID FROM VOLATILITY_SURFACE_QUOTE "
								+ " WHERE SURFACE_ID = ?) ")) {
			stmtGetQuotesBySurfaceId.setLong(1, surfaceId);
			try (ResultSet results = stmtGetQuotesBySurfaceId.executeQuery()) {
				while (results.next()) {
					long id = results.getLong("id");
					String name = results.getString("name");
					QuoteType type = QuoteType.valueOf(results.getString("type"));
					if (quotes == null) {
						quotes = new ArrayList<>();
					}
					quotes.add(new Quote(id, name, type));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quotes;
	}

	public static List<Quote> getQuotesByName(String quoteName) {
		List<Quote> quotes = null;

		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {
			String query = "SELECT * FROM QUOTE WHERE NAME ";
			if (quoteName.contains("%")) {
				query += "LIKE";
			} else {
				query += "=";
			}
			query += " '" + quoteName + "'";
			try (ResultSet results = stmt.executeQuery(query)) {
				while (results.next()) {
					long id = results.getLong("id");
					String name = results.getString("name");
					QuoteType type = QuoteType.valueOf(results.getString("type"));
					if (quotes == null) {
						quotes = new ArrayList<>();
					}
					quotes.add(new Quote(id, name, type));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quotes;
	}

	public static List<String> getAllQuoteNames() {
		List<String> quoteNames = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllQuoteNames = con.prepareStatement("SELECT DISTINCT NAME FROM QUOTE");
				ResultSet results = stmtGetAllQuoteNames.executeQuery()) {
			while (results.next()) {
				if (quoteNames == null) {
					quoteNames = new ArrayList<>();
				}
				quoteNames.add(results.getString("name"));
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quoteNames;
	}

	public static List<QuoteValue> getQuoteValuesByQuoteSetIdQuoteNameTypeAndDate(long quoteSetId, String quoteName,
			QuoteType quoteType, Year year, Month month) {
		List<QuoteValue> quotes = null;
		QuoteSet quoteSet = QuoteSetSQL.getQuoteSetById(quoteSetId);
		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {
			LocalDate startDate = LocalDate.of(year.getValue(), month, 1);
			LocalDate endDate = startDate.plus(1, ChronoUnit.MONTHS);
			String query = "SELECT * FROM QUOTE_VALUE, QUOTE, QUOTE_SET WHERE "
					+ "QUOTE_VALUE.QUOTE_ID = QUOTE.ID AND QUOTE_VALUE.QUOTE_SET_ID = QUOTE_SET.ID AND QUOTE.NAME ='"
					+ quoteName + "' AND QUOTE_SET.ID = " + quoteSetId + " AND DATE >= '"
					+ startDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + "'" + " AND DATE < '"
					+ endDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + "'";
			if (quoteType != null) {
				query += " AND TYPE = '" + quoteType.name() + "'";
			}
			try (ResultSet results = stmt.executeQuery(query)) {
				while (results.next()) {
					LocalDate date = results.getDate("date").toLocalDate();
					BigDecimal bid = results.getBigDecimal("bid");
					BigDecimal ask = results.getBigDecimal("ask");
					BigDecimal open = results.getBigDecimal("open_");
					BigDecimal close = results.getBigDecimal("close_");
					BigDecimal high = results.getBigDecimal("high");
					BigDecimal low = results.getBigDecimal("low");
					BigDecimal last = results.getBigDecimal("last_");
					LocalDate enteredDate = results.getDate("entered_date").toLocalDate();
					String sourceName = results.getString("source_name");
					long quoteId = results.getLong("quote_id");
					QuoteType resQuoteType = QuoteType.valueOf(results.getString("type"));
					Quote quote = new Quote(quoteId, quoteName, resQuoteType);
					QuoteValue qv = new QuoteValue(date, bid, ask, open, close, high, low, last, sourceName, quote,
							enteredDate, quoteSet);
					if (quotes == null) {
						quotes = new ArrayList<>();
					}
					quotes.add(qv);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quotes;
	}

	public static boolean saveQuoteValues(long quoteSetId, String quoteName, QuoteType quoteType,
			List<QuoteValue> quoteValues, Year year, Month month) {
		boolean bSaved = true;
		// First, we delete the data for this curve and this month
		LocalDate startDate = LocalDate.of(year.getValue(), month, 1);
		LocalDate endDate = startDate.plus(1, ChronoUnit.MONTHS);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteQuoteValuesByQuoteSetQuoteNameYearAndMonth = con.prepareStatement(
						"DELETE FROM QUOTE_VALUE WHERE QUOTE_ID IN (SELECT ID FROM QUOTE WHERE NAME = ? AND TYPE = ?)"
								+ " AND QUOTE_SET_ID = ?" + " AND DATE  >= ? AND DATE < ? ");
				PreparedStatement stmtSaveQuoteValues = con
						.prepareStatement("INSERT INTO QUOTE_VALUE VALUES(?,?,?,?,?,?,?,?,?,?,?,?) ")) {
			stmtDeleteQuoteValuesByQuoteSetQuoteNameYearAndMonth.setString(1, quoteName);
			if (quoteType != null) {
				stmtDeleteQuoteValuesByQuoteSetQuoteNameYearAndMonth.setString(2, quoteType.name());
			} else {
				stmtDeleteQuoteValuesByQuoteSetQuoteNameYearAndMonth.setNull(2, Types.VARCHAR);
			}
			stmtDeleteQuoteValuesByQuoteSetQuoteNameYearAndMonth.setLong(3, quoteSetId);
			stmtDeleteQuoteValuesByQuoteSetQuoteNameYearAndMonth.setDate(4, java.sql.Date.valueOf(startDate));
			stmtDeleteQuoteValuesByQuoteSetQuoteNameYearAndMonth.setDate(5, java.sql.Date.valueOf(endDate));
			stmtDeleteQuoteValuesByQuoteSetQuoteNameYearAndMonth.executeUpdate();
			for (QuoteValue quoteValue : quoteValues) {
				if (quoteValue != null && !isEmpty(quoteValue)) {
					stmtSaveQuoteValues.clearParameters();
					stmtSaveQuoteValues.setLong(1, quoteValue.getQuote().getId());
					stmtSaveQuoteValues.setDate(2, java.sql.Date.valueOf(quoteValue.getDate()));
					stmtSaveQuoteValues.setBigDecimal(3, quoteValue.getBid());
					stmtSaveQuoteValues.setBigDecimal(4, quoteValue.getAsk());
					stmtSaveQuoteValues.setBigDecimal(5, quoteValue.getOpen());
					stmtSaveQuoteValues.setBigDecimal(6, quoteValue.getClose());
					stmtSaveQuoteValues.setBigDecimal(7, quoteValue.getHigh());
					stmtSaveQuoteValues.setBigDecimal(8, quoteValue.getLow());
					stmtSaveQuoteValues.setBigDecimal(9, quoteValue.getLast());
					stmtSaveQuoteValues.setString(10, quoteValue.getSourceName());
					stmtSaveQuoteValues.setDate(11, java.sql.Date.valueOf(LocalDate.now()));
					stmtSaveQuoteValues.setLong(12, quoteSetId);
					stmtSaveQuoteValues.addBatch();
				}
			}
			stmtSaveQuoteValues.executeBatch();
			bSaved = true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return bSaved;
	}

	public static List<QuoteType> getQuoteTypesByQuoteName(String quoteName) {
		List<QuoteType> quoteTypes = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteTypesByQuoteName = con
						.prepareStatement("SELECT TYPE FROM QUOTE WHERE NAME = ?")) {
			stmtGetQuoteTypesByQuoteName.setString(1, quoteName);
			try (ResultSet results = stmtGetQuoteTypesByQuoteName.executeQuery()) {
				while (results.next()) {
					if (quoteTypes == null) {
						quoteTypes = new ArrayList<>();
					}
					quoteTypes.add(QuoteType.valueOf(results.getString("type")));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quoteTypes;
	}

	public static void deleteQuoteValues(long quoteSetId) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteQuoteValuesByQuoteSet = con
						.prepareStatement("DELETE FROM QUOTE_VALUE WHERE QUOTE_SET_ID = ?")) {
			stmtDeleteQuoteValuesByQuoteSet.setLong(1, quoteSetId);
			stmtDeleteQuoteValuesByQuoteSet.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static boolean saveQuoteValues(long quoteSetId, List<QuoteValue> quoteValues) {
		boolean bSaved = false;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtUpdateQuoteValue = con.prepareStatement("UPDATE QUOTE_VALUE SET BID=?, "
						+ "ASK=?, OPEN_=?, CLOSE_=?, HIGH=?, LOW=?, LAST_=?, ENTERED_DATE=?, SOURCE_NAME=? "
						+ " WHERE QUOTE_ID = ? AND QUOTE_SET_ID = ? AND DATE = ?");
				PreparedStatement stmtSaveQuoteValue = con
						.prepareStatement("INSERT INTO QUOTE_VALUE VALUES(?,?,?,?,?,?,?,?,?,?,?,?) ")) {
			for (QuoteValue quoteValue : quoteValues) {
				if (quoteValue != null && !isEmpty(quoteValue)) {
					if (getQuoteValueByQuoteIdQuoteSetIdAndDate(quoteValue.getQuote().getId(), quoteSetId,
							quoteValue.getDate()) != null) {
						stmtUpdateQuoteValue.clearParameters();
						stmtUpdateQuoteValue.setBigDecimal(1, quoteValue.getBid());
						stmtUpdateQuoteValue.setBigDecimal(2, quoteValue.getAsk());
						stmtUpdateQuoteValue.setBigDecimal(3, quoteValue.getOpen());
						stmtUpdateQuoteValue.setBigDecimal(4, quoteValue.getClose());
						stmtUpdateQuoteValue.setBigDecimal(5, quoteValue.getHigh());
						stmtUpdateQuoteValue.setBigDecimal(6, quoteValue.getLow());
						stmtUpdateQuoteValue.setBigDecimal(7, quoteValue.getLast());
						stmtUpdateQuoteValue.setDate(8, java.sql.Date.valueOf(quoteValue.getEnteredDate()));
						stmtUpdateQuoteValue.setString(9, quoteValue.getSourceName());
						stmtUpdateQuoteValue.setLong(10, quoteValue.getQuote().getId());
						stmtUpdateQuoteValue.setLong(11, quoteSetId);
						stmtUpdateQuoteValue.setDate(12, java.sql.Date.valueOf(quoteValue.getDate()));
						stmtUpdateQuoteValue.addBatch();
					} else {
						stmtSaveQuoteValue.clearParameters();
						stmtSaveQuoteValue.setLong(1, quoteValue.getQuote().getId());
						stmtSaveQuoteValue.setDate(2, java.sql.Date.valueOf(quoteValue.getDate()));
						stmtSaveQuoteValue.setBigDecimal(3, quoteValue.getBid());
						stmtSaveQuoteValue.setBigDecimal(4, quoteValue.getAsk());
						stmtSaveQuoteValue.setBigDecimal(5, quoteValue.getOpen());
						stmtSaveQuoteValue.setBigDecimal(6, quoteValue.getClose());
						stmtSaveQuoteValue.setBigDecimal(7, quoteValue.getHigh());
						stmtSaveQuoteValue.setBigDecimal(8, quoteValue.getLow());
						stmtSaveQuoteValue.setBigDecimal(9, quoteValue.getLast());
						stmtSaveQuoteValue.setString(10, quoteValue.getSourceName());
						stmtSaveQuoteValue.setDate(11, java.sql.Date.valueOf(LocalDate.now()));
						stmtSaveQuoteValue.setLong(12, quoteSetId);
						stmtSaveQuoteValue.addBatch();
					}
				}
				bSaved = true;
			}
			stmtUpdateQuoteValue.executeBatch();
			stmtSaveQuoteValue.executeBatch();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return bSaved;
	}

	private static QuoteValue getQuoteValueByQuoteIdQuoteSetIdAndDate(long quoteId, long quoteSetId, LocalDate date) {
		QuoteValue quoteValue = null;
		QuoteSet quoteSet = QuoteSetSQL.getQuoteSetById(quoteSetId);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteValueByQuoteIdQuoteSetIdAndDate = con
						.prepareStatement("SELECT QUOTE_VALUE.DATE DATE, QUOTE_VALUE.BID BID, QUOTE_VALUE.ASK ASK, "
								+ "QUOTE_VALUE.OPEN_ OPEN_, QUOTE_VALUE.CLOSE_ CLOSE_, QUOTE_VALUE.HIGH HIGH, "
								+ "QUOTE_VALUE.LOW LOW, QUOTE_VALUE.LAST_ LAST_, "
								+ "QUOTE_VALUE.ENTERED_DATE ENTERED_DATE, QUOTE_VALUE.SOURCE_NAME SOURCE_NAME, "
								+ "QUOTE.TYPE TYPE, QUOTE.NAME NAME, QUOTE_SET.NAME QUOTE_SET_NAME, "
								+ "QUOTE_VALUE.QUOTE_SET_ID QUOTE_SET_ID "
								+ "FROM QUOTE, QUOTE_VALUE, QUOTE_SET WHERE QUOTE_VALUE.QUOTE_SET_ID = QUOTE_SET.ID AND QUOTE.ID = QUOTE_VALUE.QUOTE_ID AND QUOTE.ID = ? AND QUOTE_SET_ID = ? AND DATE = ?")) {

			stmtGetQuoteValueByQuoteIdQuoteSetIdAndDate.setLong(1, quoteId);
			stmtGetQuoteValueByQuoteIdQuoteSetIdAndDate.setLong(2, quoteSetId);
			stmtGetQuoteValueByQuoteIdQuoteSetIdAndDate.setDate(3, java.sql.Date.valueOf(date));
			try (ResultSet results = stmtGetQuoteValueByQuoteIdQuoteSetIdAndDate.executeQuery()) {
				while (results.next()) {
					LocalDate quoteDate = results.getDate("date").toLocalDate();
					BigDecimal bid = results.getBigDecimal("bid");
					BigDecimal ask = results.getBigDecimal("ask");
					BigDecimal open = results.getBigDecimal("open_");
					BigDecimal close = results.getBigDecimal("close_");
					BigDecimal high = results.getBigDecimal("high");
					BigDecimal low = results.getBigDecimal("low");
					BigDecimal last = results.getBigDecimal("last_");
					LocalDate enteredDate = results.getDate("entered_date").toLocalDate();
					String sourceName = results.getString("source_name");
					String quoteName = results.getString("name");
					QuoteType quoteType = QuoteType.valueOf(results.getString("type"));
					Quote quote = new Quote(quoteId, quoteName, quoteType);
					quoteValue = new QuoteValue(quoteDate, bid, ask, open, close, high, low, last, sourceName, quote,
							enteredDate, quoteSet);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quoteValue;
	}

	public static Set<QuoteValue> getQuoteValuesByQuoteSetIdTypeDateAndQuoteNames(long quoteSetId, QuoteType quoteType,
			LocalDate date, String... quoteNames) {
		Set<QuoteValue> quoteValues = null;
		QuoteSet quoteSet = QuoteSetSQL.getQuoteSetById(quoteSetId);
		StringBuilder query = new StringBuilder("SELECT QUOTE.ID ID, QUOTE_VALUE.DATE DATE, QUOTE_VALUE.BID BID, "
				+ "QUOTE_VALUE.ASK ASK, QUOTE_VALUE.OPEN_ OPEN_, QUOTE_VALUE.CLOSE_ CLOSE_, "
				+ "QUOTE_VALUE.HIGH HIGH, QUOTE_VALUE.LOW LOW, QUOTE_VALUE.LAST_ LAST_, "
				+ "QUOTE_VALUE.ENTERED_DATE ENTERED_DATE, QUOTE_VALUE.SOURCE_NAME SOURCE_NAME, "
				+ "QUOTE.TYPE TYPE, QUOTE.NAME NAME, QUOTE_SET.ID QUOTE_SET_ID "
				+ "FROM QUOTE, QUOTE_VALUE, QUOTE_SET WHERE QUOTE.ID = QUOTE_VALUE.QUOTE_ID"
				+ " AND QUOTE_VALUE.QUOTE_SET_ID = QUOTE_SET.ID AND QUOTE_SET.ID = ?" + " AND DATE = ? AND TYPE=? ");

		if (quoteNames.length == 1) {
			query.append("AND QUOTE.NAME = ?");
		} else if (quoteNames.length > 1) {
			query.append("AND QUOTE.NAME IN (");
			for (int i = 1; i <= quoteNames.length; i++) {
				query.append("?,");
			}
			query.delete(query.length() - 1, query.length());
			query.append(")");
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteValuesByQuoteSetTypeDateAndQuoteNames = con
						.prepareStatement(query.toString())) {
			stmtGetQuoteValuesByQuoteSetTypeDateAndQuoteNames.setLong(1, quoteSetId);
			stmtGetQuoteValuesByQuoteSetTypeDateAndQuoteNames.setDate(2, java.sql.Date.valueOf(date));
			stmtGetQuoteValuesByQuoteSetTypeDateAndQuoteNames.setString(3, quoteType.name());
			int pos = 4;
			for (String name : quoteNames) {
				stmtGetQuoteValuesByQuoteSetTypeDateAndQuoteNames.setString(pos, name);
				pos++;
			}
			try (ResultSet results = stmtGetQuoteValuesByQuoteSetTypeDateAndQuoteNames.executeQuery()) {
				while (results.next()) {
					if (quoteValues == null) {
						quoteValues = new HashSet<>();
					}
					LocalDate quoteDate = results.getDate("date").toLocalDate();
					BigDecimal bid = results.getBigDecimal("bid");
					BigDecimal ask = results.getBigDecimal("ask");
					BigDecimal open = results.getBigDecimal("open_");
					BigDecimal close = results.getBigDecimal("close_");
					BigDecimal high = results.getBigDecimal("high");
					BigDecimal low = results.getBigDecimal("low");
					BigDecimal last = results.getBigDecimal("last_");
					LocalDate enteredDate = results.getDate("entered_date").toLocalDate();
					String sourceName = results.getString("source_name");
					Quote quote = QuoteSQL.getQuoteById(results.getLong("id"));
					QuoteValue quoteValue = new QuoteValue(quoteDate, bid, ask, open, close, high, low, last,
							sourceName, quote, enteredDate, quoteSet);
					quoteValues.add(quoteValue);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quoteValues;
	}

}