package org.eclipse.tradista.core.marketdata.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.AND;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ENTERED_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.FROM;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUOTE_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUOTE_SET_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.SELECT;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TYPE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.WHERE;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
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

	private static final Field ID_FIELD = new Field(ID);
	private static final Field NAME_FIELD = new Field(NAME);
	private static final Field TYPE_FIELD = new Field(TYPE);

	private static final Field QUOTE_ID_FIELD = new Field(QUOTE_ID);
	private static final Field DATE_FIELD = new Field(DATE);
	private static final Field BID_FIELD = new Field("BID");
	private static final Field ASK_FIELD = new Field("ASK");
	private static final Field OPEN_FIELD = new Field("OPEN_");
	private static final Field CLOSE_FIELD = new Field("CLOSE_");
	private static final Field HIGH_FIELD = new Field("HIGH");
	private static final Field LOW_FIELD = new Field("LOW");
	private static final Field LAST_FIELD = new Field("LAST_");
	private static final Field SOURCE_NAME_FIELD = new Field("SOURCE_NAME");
	private static final Field ENTERED_DATE_FIELD = new Field(ENTERED_DATE);
	private static final Field QUOTE_SET_ID_FIELD = new Field(QUOTE_SET_ID);
	private static final Field ID_QUOTE_SET_FIELD = new Field(ID);
	private static final Field NAME_QUOTE_SET_FIELD = new Field(NAME);

	private static final Field[] QUOTE_FIELDS = { NAME_FIELD, TYPE_FIELD, ID_FIELD };
	private static final Field[] QUOTE_FIELDS_FOR_INSERT_OR_UPDATE = { NAME_FIELD, TYPE_FIELD };

	private static final Table QUOTE_TABLE = new Table("QUOTE", QUOTE_FIELDS);

	private static final Field[] QUOTE_VALUE_FIELDS = { QUOTE_ID_FIELD, DATE_FIELD, BID_FIELD, ASK_FIELD, OPEN_FIELD,
			CLOSE_FIELD, HIGH_FIELD, LOW_FIELD, LAST_FIELD, SOURCE_NAME_FIELD, ENTERED_DATE_FIELD, QUOTE_SET_ID_FIELD };

	private static final Table QUOTE_VALUE_TABLE = new Table("QUOTE_VALUE", QUOTE_VALUE_FIELDS);

	private static final Table QUOTE_SET_TABLE = new Table("QUOTE_SET",
			new Field[] { ID_QUOTE_SET_FIELD, NAME_QUOTE_SET_FIELD });

	private static final Join QUOTE_AND_QUOTE_VALUE_JOIN = Join.innerEq(QUOTE_TABLE, ID_FIELD, QUOTE_ID_FIELD);
	private static final Join QUOTE_SET_AND_QUOTE_VALUE_JOIN = Join.innerEq(QUOTE_SET_TABLE, ID_QUOTE_SET_FIELD,
			QUOTE_SET_ID_FIELD);

	private static final String SELECT_QUOTE_VALUE_QUERY = TradistaDBUtil.buildSelectQuery(QUOTE_VALUE_TABLE,
			QUOTE_AND_QUOTE_VALUE_JOIN, QUOTE_SET_AND_QUOTE_VALUE_JOIN);

	private static Quote buildQuote(ResultSet results) throws SQLException {
		long id = results.getLong(ID_FIELD.getName());
		String name = results.getString(NAME_FIELD.getName());
		QuoteType type = QuoteType.valueOf(results.getString(TYPE_FIELD.getName()));
		return new Quote(id, name, type);
	}

	private static QuoteValue buildQuoteValue(ResultSet results, Quote quote, QuoteSet quoteSet) throws SQLException {
		LocalDate date = results.getDate(DATE_FIELD.getName()).toLocalDate();
		BigDecimal bid = results.getBigDecimal(BID_FIELD.getName());
		BigDecimal ask = results.getBigDecimal(ASK_FIELD.getName());
		BigDecimal open = results.getBigDecimal(OPEN_FIELD.getName());
		BigDecimal close = results.getBigDecimal(CLOSE_FIELD.getName());
		BigDecimal high = results.getBigDecimal(HIGH_FIELD.getName());
		BigDecimal low = results.getBigDecimal(LOW_FIELD.getName());
		BigDecimal last = results.getBigDecimal(LAST_FIELD.getName());
		String sourceName = results.getString(SOURCE_NAME_FIELD.getName());
		LocalDate enteredDate = results.getDate(ENTERED_DATE_FIELD.getName()).toLocalDate();
		return new QuoteValue(date, bid, ask, open, close, high, low, last, sourceName, quote, enteredDate, quoteSet);
	}

	public static long saveQuote(Quote quote) {
		boolean isNew = (quote.getId() == 0);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveQuote = isNew
						? TradistaDBUtil.buildInsertPreparedStatement(con, QUOTE_TABLE,
								QUOTE_FIELDS_FOR_INSERT_OR_UPDATE)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, QUOTE_TABLE,
								QUOTE_FIELDS_FOR_INSERT_OR_UPDATE)) {
			stmtSaveQuote.setString(1, quote.getName());
			stmtSaveQuote.setString(2, quote.getType().name());
			if (!isNew) {
				stmtSaveQuote.setLong(3, quote.getId());
			}
			stmtSaveQuote.executeUpdate();

			if (isNew) {
				try (ResultSet generatedKeys = stmtSaveQuote.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						quote.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("Creating quote failed, no generated key obtained.");
					}
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quote.getId();
	}

	public static boolean deleteQuote(String quoteName, QuoteType quoteType) {
		boolean bSaved = false;

		StringBuilder deleteSql = new StringBuilder("DELETE").append(FROM).append(QUOTE_VALUE_TABLE);
		StringBuilder inSelect = new StringBuilder(TradistaDBUtil.buildSelectQuery(ID_FIELD, QUOTE_TABLE));
		TradistaDBUtil.addParameterizedFilter(inSelect, NAME_FIELD);
		if (quoteType != null) {
			TradistaDBUtil.addParameterizedFilter(inSelect, TYPE_FIELD);
		}
		TradistaDBUtil.addQueryFilter(deleteSql, QUOTE_ID_FIELD, inSelect.toString(), false);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteQuoteValues = con.prepareStatement(deleteSql.toString());
				PreparedStatement stmtDeleteQuote = quoteType != null
						? TradistaDBUtil.buildDeletePreparedStatement(con, QUOTE_TABLE, NAME_FIELD, TYPE_FIELD)
						: TradistaDBUtil.buildDeletePreparedStatement(con, QUOTE_TABLE, NAME_FIELD)) {
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
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static List<Quote> getAllQuotes() {
		List<Quote> quotes = null;
		String sql = TradistaDBUtil.buildSelectQuery(QUOTE_TABLE);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllQuotes = con.prepareStatement(sql);
				ResultSet results = stmtGetAllQuotes.executeQuery()) {
			while (results.next()) {
				if (quotes == null) {
					quotes = new ArrayList<>();
				}
				quotes.add(buildQuote(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quotes;
	}

	public static List<QuoteValue> getQuoteValuesByQuoteSetIdQuoteNameAndDate(long quoteSetId, String name,
			LocalDate date) {
		List<QuoteValue> quoteValues = null;
		QuoteSet quoteSet = QuoteSetSQL.getQuoteSetById(quoteSetId);
		StringBuilder sql = new StringBuilder(SELECT_QUOTE_VALUE_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, QUOTE_SET_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, DATE_FIELD);
		if (!name.contains("%")) {
			TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		} else {
			sql.append(sql.indexOf(WHERE) != -1 ? AND : WHERE).append(NAME_FIELD.getFullName()).append(" LIKE ?");
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteValuesByQuoteSetIdQuoteNameAndDate = con
						.prepareStatement(sql.toString())) {
			stmtGetQuoteValuesByQuoteSetIdQuoteNameAndDate.setLong(1, quoteSetId);
			stmtGetQuoteValuesByQuoteSetIdQuoteNameAndDate.setDate(2, java.sql.Date.valueOf(date));
			stmtGetQuoteValuesByQuoteSetIdQuoteNameAndDate.setString(3, name);
			try (ResultSet results = stmtGetQuoteValuesByQuoteSetIdQuoteNameAndDate.executeQuery()) {
				while (results.next()) {
					Quote quote = buildQuote(results);
					if (quoteValues == null) {
						quoteValues = new ArrayList<>();
					}
					quoteValues.add(buildQuoteValue(results, quote, quoteSet));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quoteValues;
	}

	public static QuoteValue getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(long quoteSetId, String name,
			QuoteType quoteType, LocalDate date) {
		QuoteValue quoteValue = null;
		QuoteSet quoteSet = QuoteSetSQL.getQuoteSetById(quoteSetId);
		StringBuilder sql = new StringBuilder(SELECT_QUOTE_VALUE_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, QUOTE_SET_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, DATE_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, TYPE_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate = con
						.prepareStatement(sql.toString())) {
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setLong(1, quoteSetId);
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setString(2, name);
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setDate(3, java.sql.Date.valueOf(date));
			stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.setString(4, quoteType.name());
			try (ResultSet results = stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate.executeQuery()) {
				while (results.next()) {
					Quote quote = buildQuote(results);
					quoteValue = buildQuoteValue(results, quote, quoteSet);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quoteValue;
	}

	public static Set<QuoteValue> getQuoteValueByQuoteSetIdQuoteNameTypeAndDates(long quoteSetId, String name,
			QuoteType quoteType, LocalDate startDate, LocalDate endDate) {
		Set<QuoteValue> quoteValues = null;
		QuoteSet quoteSet = QuoteSetSQL.getQuoteSetById(quoteSetId);
		StringBuilder sql = new StringBuilder(SELECT_QUOTE_VALUE_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, QUOTE_SET_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, TYPE_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, DATE_FIELD, true);
		TradistaDBUtil.addParameterizedFilter(sql, DATE_FIELD, false);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteValueByQuoteSetQuoteNameTypeAndDate = con
						.prepareStatement(sql.toString())) {
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
					Quote quote = buildQuote(results);
					quoteValues.add(buildQuoteValue(results, quote, quoteSet));
				}
			}
		} catch (SQLException sqle) {
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
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(QUOTE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, TYPE_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteByNameAndType = con.prepareStatement(sql.toString())) {
			stmtGetQuoteByNameAndType.setString(1, quoteName);
			stmtGetQuoteByNameAndType.setString(2, quoteType.name());
			try (ResultSet results = stmtGetQuoteByNameAndType.executeQuery()) {
				while (results.next()) {
					quote = buildQuote(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quote;
	}

	public static Quote getQuoteById(long quoteId) {
		Quote quote = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(QUOTE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteById = con.prepareStatement(sql.toString())) {
			stmtGetQuoteById.setLong(1, quoteId);
			try (ResultSet results = stmtGetQuoteById.executeQuery()) {
				while (results.next()) {
					quote = buildQuote(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quote;
	}

	public static List<Quote> getQuotesByCurveId(long curveId) {
		List<Quote> quotes = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(QUOTE_TABLE));
		StringBuilder inSelect = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(CurveSQL.QUOTE_ID_FIELD, CurveSQL.CURVE_QUOTE_TABLE));
		TradistaDBUtil.addParameterizedFilter(inSelect, CurveSQL.CURVE_QUOTE_CURVE_ID_FIELD);
		TradistaDBUtil.addQueryFilter(sql, ID_FIELD, inSelect.toString(), false);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuotesByCurveId = con.prepareStatement(sql.toString())) {
			stmtGetQuotesByCurveId.setLong(1, curveId);
			try (ResultSet results = stmtGetQuotesByCurveId.executeQuery()) {
				while (results.next()) {
					if (quotes == null) {
						quotes = new ArrayList<>();
					}
					quotes.add(buildQuote(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quotes;
	}

	public static List<Quote> getQuotesBySurfaceId(long surfaceId) {
		List<Quote> quotes = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(QUOTE_TABLE));
		StringBuilder inSelect = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(SurfaceSQL.QUOTE_ID_FIELD, SurfaceSQL.VOLATILITY_SURFACE_QUOTE_TABLE));
		TradistaDBUtil.addParameterizedFilter(inSelect, SurfaceSQL.SURFACE_ID_FIELD);
		TradistaDBUtil.addQueryFilter(sql, ID_FIELD, inSelect.toString(), false);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuotesBySurfaceId = con.prepareStatement(sql.toString())) {
			stmtGetQuotesBySurfaceId.setLong(1, surfaceId);
			try (ResultSet results = stmtGetQuotesBySurfaceId.executeQuery()) {
				while (results.next()) {
					if (quotes == null) {
						quotes = new ArrayList<>();
					}
					quotes.add(buildQuote(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quotes;
	}

	public static List<Quote> getQuotesByName(String quoteName) {
		List<Quote> quotes = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(QUOTE_TABLE));
		if (!quoteName.contains("%")) {
			TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		} else {
			sql.append(sql.indexOf(WHERE) != -1 ? AND : WHERE).append(NAME_FIELD.getFullName()).append(" LIKE ?");
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString())) {
			stmt.setString(1, quoteName);
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					if (quotes == null) {
						quotes = new ArrayList<>();
					}
					quotes.add(buildQuote(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quotes;
	}

	public static List<String> getAllQuoteNames() {
		List<String> quoteNames = null;
		String sql = TradistaDBUtil.buildSelectQuery(NAME_FIELD, QUOTE_TABLE).replace(SELECT, " SELECT DISTINCT ");

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllQuoteNames = con.prepareStatement(sql);
				ResultSet results = stmtGetAllQuoteNames.executeQuery()) {
			while (results.next()) {
				if (quoteNames == null) {
					quoteNames = new ArrayList<>();
				}
				quoteNames.add(results.getString(NAME_FIELD.getName()));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quoteNames;
	}

	public static List<QuoteValue> getQuoteValuesByQuoteSetIdQuoteNameTypeAndDate(long quoteSetId, String quoteName,
			QuoteType quoteType, Year year, Month month) {
		List<QuoteValue> quotes = null;
		QuoteSet quoteSet = QuoteSetSQL.getQuoteSetById(quoteSetId);
		LocalDate startDate = LocalDate.of(year.getValue(), month, 1);
		LocalDate endDate = startDate.plus(1, ChronoUnit.MONTHS);

		StringBuilder sql = new StringBuilder(SELECT_QUOTE_VALUE_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, QUOTE_SET_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, DATE_FIELD, true);
		TradistaDBUtil.addParameterizedFilter(sql, DATE_FIELD, false, false);
		if (quoteType != null) {
			TradistaDBUtil.addParameterizedFilter(sql, TYPE_FIELD);
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString())) {
			stmt.setLong(1, quoteSetId);
			stmt.setString(2, quoteName);
			stmt.setDate(3, java.sql.Date.valueOf(startDate));
			stmt.setDate(4, java.sql.Date.valueOf(endDate));
			if (quoteType != null) {
				stmt.setString(5, quoteType.name());
			}
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					if (quotes == null) {
						quotes = new ArrayList<>();
					}
					Quote quote = buildQuote(results);
					quotes.add(buildQuoteValue(results, quote, quoteSet));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quotes;
	}

	private static void setBigDecimalOrNull(PreparedStatement stmt, int index, BigDecimal value) throws SQLException {
		if (value != null) {
			stmt.setBigDecimal(index, value);
		} else {
			stmt.setNull(index, Types.DECIMAL);
		}
	}

	public static boolean saveQuoteValues(long quoteSetId, String quoteName, QuoteType quoteType,
			List<QuoteValue> quoteValues, Year year, Month month) {
		boolean bSaved = true;
		LocalDate startDate = LocalDate.of(year.getValue(), month, 1);
		LocalDate endDate = startDate.plus(1, ChronoUnit.MONTHS);

		StringBuilder deleteSql = new StringBuilder("DELETE").append(FROM).append(QUOTE_VALUE_TABLE);
		StringBuilder inSelect = new StringBuilder(TradistaDBUtil.buildSelectQuery(ID_FIELD, QUOTE_TABLE));
		TradistaDBUtil.addParameterizedFilter(inSelect, NAME_FIELD);
		if (quoteType != null) {
			TradistaDBUtil.addParameterizedFilter(inSelect, TYPE_FIELD);
		}
		TradistaDBUtil.addQueryFilter(deleteSql, QUOTE_ID_FIELD, inSelect.toString(), false);

		TradistaDBUtil.addParameterizedFilter(deleteSql, QUOTE_SET_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(deleteSql, DATE_FIELD, true);
		TradistaDBUtil.addParameterizedFilter(deleteSql, DATE_FIELD, false, false);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDelete = con.prepareStatement(deleteSql.toString());
				PreparedStatement stmtSave = TradistaDBUtil.buildInsertPreparedStatement(con, QUOTE_VALUE_TABLE,
						QUOTE_VALUE_FIELDS)) {
			int i = 1;
			stmtDelete.setString(i++, quoteName);
			if (quoteType != null) {
				stmtDelete.setString(i++, quoteType.name());
			}
			stmtDelete.setLong(i++, quoteSetId);
			stmtDelete.setDate(i++, java.sql.Date.valueOf(startDate));
			stmtDelete.setDate(i++, java.sql.Date.valueOf(endDate));
			stmtDelete.executeUpdate();

			for (QuoteValue quoteValue : quoteValues) {
				if (quoteValue != null && !isEmpty(quoteValue)) {
					stmtSave.clearParameters();
					stmtSave.setLong(1, quoteValue.getQuote().getId());
					stmtSave.setDate(2, java.sql.Date.valueOf(quoteValue.getDate()));
					setBigDecimalOrNull(stmtSave, 3, quoteValue.getBid());
					setBigDecimalOrNull(stmtSave, 4, quoteValue.getAsk());
					setBigDecimalOrNull(stmtSave, 5, quoteValue.getOpen());
					setBigDecimalOrNull(stmtSave, 6, quoteValue.getClose());
					setBigDecimalOrNull(stmtSave, 7, quoteValue.getHigh());
					setBigDecimalOrNull(stmtSave, 8, quoteValue.getLow());
					setBigDecimalOrNull(stmtSave, 9, quoteValue.getLast());
					stmtSave.setString(10, quoteValue.getSourceName());
					stmtSave.setDate(11, java.sql.Date.valueOf(LocalDate.now(ZoneId.systemDefault())));
					stmtSave.setLong(12, quoteSetId);
					stmtSave.addBatch();
				}
			}
			stmtSave.executeBatch();
			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return bSaved;
	}

	public static List<QuoteType> getQuoteTypesByQuoteName(String quoteName) {
		List<QuoteType> quoteTypes = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TYPE_FIELD, QUOTE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteTypesByQuoteName = con.prepareStatement(sql.toString())) {
			stmtGetQuoteTypesByQuoteName.setString(1, quoteName);
			try (ResultSet results = stmtGetQuoteTypesByQuoteName.executeQuery()) {
				while (results.next()) {
					if (quoteTypes == null) {
						quoteTypes = new ArrayList<>();
					}
					quoteTypes.add(QuoteType.valueOf(results.getString(TYPE_FIELD.getName())));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quoteTypes;
	}

	public static void deleteQuoteValues(long quoteSetId) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteQuoteValuesByQuoteSet = TradistaDBUtil.buildDeletePreparedStatement(con,
						QUOTE_VALUE_TABLE, QUOTE_SET_ID_FIELD)) {
			stmtDeleteQuoteValuesByQuoteSet.setLong(1, quoteSetId);
			stmtDeleteQuoteValuesByQuoteSet.executeUpdate();
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static boolean saveQuoteValues(long quoteSetId, List<QuoteValue> quoteValues) {
		boolean bSaved = false;

		Field[] updateFields = { BID_FIELD, ASK_FIELD, OPEN_FIELD, CLOSE_FIELD, HIGH_FIELD, LOW_FIELD, LAST_FIELD,
				ENTERED_DATE_FIELD, SOURCE_NAME_FIELD };

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtUpdate = TradistaDBUtil.buildUpdatePreparedStatement(con,
						new Field[] { QUOTE_ID_FIELD, QUOTE_SET_ID_FIELD, DATE_FIELD }, QUOTE_VALUE_TABLE,
						updateFields);
				PreparedStatement stmtSave = TradistaDBUtil.buildInsertPreparedStatement(con, QUOTE_VALUE_TABLE,
						QUOTE_VALUE_FIELDS)) {
			for (QuoteValue quoteValue : quoteValues) {
				if (quoteValue != null && !isEmpty(quoteValue)) {
					if (getQuoteValueByQuoteIdQuoteSetIdAndDate(quoteValue.getQuote().getId(), quoteSetId,
							quoteValue.getDate()) != null) {
						stmtUpdate.clearParameters();
						setBigDecimalOrNull(stmtUpdate, 1, quoteValue.getBid());
						setBigDecimalOrNull(stmtUpdate, 2, quoteValue.getAsk());
						setBigDecimalOrNull(stmtUpdate, 3, quoteValue.getOpen());
						setBigDecimalOrNull(stmtUpdate, 4, quoteValue.getClose());
						setBigDecimalOrNull(stmtUpdate, 5, quoteValue.getHigh());
						setBigDecimalOrNull(stmtUpdate, 6, quoteValue.getLow());
						setBigDecimalOrNull(stmtUpdate, 7, quoteValue.getLast());
						stmtUpdate.setDate(8, java.sql.Date.valueOf(quoteValue.getEnteredDate()));
						stmtUpdate.setString(9, quoteValue.getSourceName());
						stmtUpdate.setLong(10, quoteValue.getQuote().getId());
						stmtUpdate.setLong(11, quoteSetId);
						stmtUpdate.setDate(12, java.sql.Date.valueOf(quoteValue.getDate()));
						stmtUpdate.addBatch();
					} else {
						stmtSave.clearParameters();
						stmtSave.setLong(1, quoteValue.getQuote().getId());
						stmtSave.setDate(2, java.sql.Date.valueOf(quoteValue.getDate()));
						setBigDecimalOrNull(stmtSave, 3, quoteValue.getBid());
						setBigDecimalOrNull(stmtSave, 4, quoteValue.getAsk());
						setBigDecimalOrNull(stmtSave, 5, quoteValue.getOpen());
						setBigDecimalOrNull(stmtSave, 6, quoteValue.getClose());
						setBigDecimalOrNull(stmtSave, 7, quoteValue.getHigh());
						setBigDecimalOrNull(stmtSave, 8, quoteValue.getLow());
						setBigDecimalOrNull(stmtSave, 9, quoteValue.getLast());
						stmtSave.setString(10, quoteValue.getSourceName());
						stmtSave.setDate(11, java.sql.Date.valueOf(LocalDate.now(ZoneId.systemDefault())));
						stmtSave.setLong(12, quoteSetId);
						stmtSave.addBatch();
					}
				}
				bSaved = true;
			}
			stmtUpdate.executeBatch();
			stmtSave.executeBatch();
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return bSaved;
	}

	private static QuoteValue getQuoteValueByQuoteIdQuoteSetIdAndDate(long quoteId, long quoteSetId, LocalDate date) {
		QuoteValue quoteValue = null;
		QuoteSet quoteSet = QuoteSetSQL.getQuoteSetById(quoteSetId);
		StringBuilder sql = new StringBuilder(SELECT_QUOTE_VALUE_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, QUOTE_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, QUOTE_SET_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, DATE_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteValueByQuoteIdQuoteSetIdAndDate = con.prepareStatement(sql.toString())) {
			stmtGetQuoteValueByQuoteIdQuoteSetIdAndDate.setLong(1, quoteId);
			stmtGetQuoteValueByQuoteIdQuoteSetIdAndDate.setLong(2, quoteSetId);
			stmtGetQuoteValueByQuoteIdQuoteSetIdAndDate.setDate(3, java.sql.Date.valueOf(date));
			try (ResultSet results = stmtGetQuoteValueByQuoteIdQuoteSetIdAndDate.executeQuery()) {
				while (results.next()) {
					Quote quote = buildQuote(results);
					quoteValue = buildQuoteValue(results, quote, quoteSet);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quoteValue;
	}

	public static Set<QuoteValue> getQuoteValuesByQuoteSetIdTypeDateAndQuoteNames(long quoteSetId, QuoteType quoteType,
			LocalDate date, String... quoteNames) {
		Set<QuoteValue> quoteValues = null;
		QuoteSet quoteSet = QuoteSetSQL.getQuoteSetById(quoteSetId);
		StringBuilder sql = new StringBuilder(SELECT_QUOTE_VALUE_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, QUOTE_SET_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, DATE_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, TYPE_FIELD);

		if (quoteNames != null && quoteNames.length > 0) {
			if (quoteNames.length == 1) {
				TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
			} else {
				TradistaDBUtil.addParameterizedInFilter(sql, NAME_FIELD, quoteNames);
			}
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteValuesByQuoteSetTypeDateAndQuoteNames = con
						.prepareStatement(sql.toString())) {
			stmtGetQuoteValuesByQuoteSetTypeDateAndQuoteNames.setLong(1, quoteSetId);
			stmtGetQuoteValuesByQuoteSetTypeDateAndQuoteNames.setDate(2, java.sql.Date.valueOf(date));
			stmtGetQuoteValuesByQuoteSetTypeDateAndQuoteNames.setString(3, quoteType.name());
			if (quoteNames != null && quoteNames.length > 0) {
				int pos = 4;
				for (String name : quoteNames) {
					stmtGetQuoteValuesByQuoteSetTypeDateAndQuoteNames.setString(pos++, name);
				}
			}
			try (ResultSet results = stmtGetQuoteValuesByQuoteSetTypeDateAndQuoteNames.executeQuery()) {
				while (results.next()) {
					if (quoteValues == null) {
						quoteValues = new HashSet<>();
					}
					Quote quote = buildQuote(results);
					QuoteValue quoteValue = buildQuoteValue(results, quote, quoteSet);
					quoteValues.add(quoteValue);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quoteValues;
	}

}