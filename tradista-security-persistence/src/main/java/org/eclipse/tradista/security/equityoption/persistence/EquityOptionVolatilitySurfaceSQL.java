package org.eclipse.tradista.security.equityoption.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.AND;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.OR;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.SURFACE_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.VOLATILITY;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.VOLATILITY_SURFACE_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.VALUE;
import static org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL.ALGORITHM_FIELD;
import static org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL.ID_FIELD;
import static org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL.INSTANCE_FIELD;
import static org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL.INTERPOLATOR_FIELD;
import static org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL.NAME_FIELD;
import static org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL.PROCESSING_ORG_ID_FIELD;
import static org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL.QUOTE_DATE_FIELD;
import static org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL.QUOTE_SET_ID_FIELD;
import static org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL.VOLATILITY_SURFACE_QUOTE_TABLE;
import static org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL.SURFACE_ID_FIELD;
import static org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL.VOLATILITY_SURFACE_TABLE;
import static org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL.TYPE_FIELD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.common.persistence.util.UnaryFunctionExpression;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.core.marketdata.persistence.QuoteSQL;
import org.eclipse.tradista.core.marketdata.persistence.QuoteSetSQL;
import org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;

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

public class EquityOptionVolatilitySurfaceSQL {

	private static final String OPTION_EXPIRY = "OPTION_EXPIRY";
	private static final String STRIKE = "STRIKE";

	private static final Field OPTION_EXPIRY_FIELD = new Field(OPTION_EXPIRY);
	private static final Field STRIKE_FIELD = new Field(STRIKE);
	private static final Field VOLATILITY_FIELD = new Field(VOLATILITY);
	private static final Field VOLATILITY_SURFACE_ID_FIELD = new Field(VOLATILITY_SURFACE_ID);

	private static final Field[] POINT_FIELDS = { VOLATILITY_SURFACE_ID_FIELD, OPTION_EXPIRY_FIELD, STRIKE_FIELD,
			VOLATILITY_FIELD };

	private static final Table POINT_TABLE = new Table("EQUITY_OPTION_VOLATILITY_SURFACE_POINT", POINT_FIELDS);

	private static final Field STRIKE_SURFACE_ID_FIELD = new Field(SURFACE_ID);

	private static final Field STRIKE_VALUE_FIELD = new Field(VALUE);

	private static final Field[] STRIKE_FIELDS = { STRIKE_SURFACE_ID_FIELD, STRIKE_VALUE_FIELD };

	private static final Table STRIKE_TABLE = new Table("EQUITY_OPTION_VOLATILITY_SURFACE_STRIKE", STRIKE_FIELDS);

	public static boolean saveEquityOptionVolatilitySurface(String surfaceName) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveEquityVolatilitySurface = TradistaDBUtil.buildInsertPreparedStatement(con,
						VOLATILITY_SURFACE_TABLE, NAME_FIELD, TYPE_FIELD)) {
			stmtSaveEquityVolatilitySurface.setString(1, surfaceName);
			stmtSaveEquityVolatilitySurface.setString(2, EquityOption.EQUITY_OPTION);
			stmtSaveEquityVolatilitySurface.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<EquityOptionVolatilitySurface> getAllEquityOptionVolatilitySurfaces() {
		return getEquityOptionVolatilitySurfacesByPoId(0);
	}

	public static Set<EquityOptionVolatilitySurface> getEquityOptionVolatilitySurfacesByPoId(long poId) {
		Set<EquityOptionVolatilitySurface> equityOptionVolatilitySurfaces = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_SURFACE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, TYPE_FIELD);
		if (poId > 0) {
			sql.append(AND + " (").append(PROCESSING_ORG_ID_FIELD.getName()).append(" = ").append(poId).append(OR)
					.append(PROCESSING_ORG_ID_FIELD.getName()).append(" IS NULL)");
		}
		StringBuilder strikesSql = new StringBuilder(TradistaDBUtil.buildSelectQuery(STRIKE_TABLE));
		TradistaDBUtil.addParameterizedFilter(strikesSql, STRIKE_SURFACE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetSurfaces = con.prepareStatement(sql.toString());
				PreparedStatement stmtGetStrikesBySurfaceId = con.prepareStatement(strikesSql.toString())) {
			stmtGetSurfaces.setString(1, EquityOption.EQUITY_OPTION);
			try (ResultSet results = stmtGetSurfaces.executeQuery()) {
				while (results.next()) {
					if (equityOptionVolatilitySurfaces == null) {
						equityOptionVolatilitySurfaces = new HashSet<>();
					}
					equityOptionVolatilitySurfaces
							.add(buildEquityOptionVolatilitySurface(results, stmtGetStrikesBySurfaceId));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptionVolatilitySurfaces;
	}

	private static EquityOptionVolatilitySurface buildEquityOptionVolatilitySurface(ResultSet results,
			PreparedStatement stmtGetStrikesBySurfaceId) throws SQLException {
		long resPoId = results.getLong(PROCESSING_ORG_ID_FIELD.getName());
		LegalEntity po = null;
		if (resPoId > 0) {
			po = LegalEntitySQL.getLegalEntityById(resPoId);
		}
		EquityOptionVolatilitySurface equityOptionVolatilitySurface = new EquityOptionVolatilitySurface(
				results.getString(NAME_FIELD.getName()), po);
		long id = results.getLong(ID_FIELD.getName());
		equityOptionVolatilitySurface.setId(id);
		equityOptionVolatilitySurface.setAlgorithm(results.getString(ALGORITHM_FIELD.getName()));
		equityOptionVolatilitySurface.setInterpolator(results.getString(INTERPOLATOR_FIELD.getName()));
		equityOptionVolatilitySurface.setInstance(results.getString(INSTANCE_FIELD.getName()));
		java.sql.Date quoteDate = results.getDate(QUOTE_DATE_FIELD.getName());
		if (quoteDate != null) {
			equityOptionVolatilitySurface.setQuoteDate(quoteDate.toLocalDate());
		}
		// Get the points linked to this surface
		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints = getEquityOptionVolatilitySurfacePointsBySurfaceId(
				id);
		equityOptionVolatilitySurface.setPoints(surfacePoints);

		// Get the quotes linked to this surface
		List<Quote> quotes = QuoteSQL.getQuotesBySurfaceId(id);

		equityOptionVolatilitySurface.setQuotes(quotes);

		// Get the strikes linked to this surface
		List<BigDecimal> strikes = new ArrayList<>();

		stmtGetStrikesBySurfaceId.setLong(1, id);
		try (ResultSet strikesResults = stmtGetStrikesBySurfaceId.executeQuery()) {
			while (strikesResults.next()) {
				strikes.add(strikesResults.getBigDecimal(STRIKE_VALUE_FIELD.getName()));
			}
		}

		equityOptionVolatilitySurface.setStrikes(strikes);
		equityOptionVolatilitySurface
				.setQuoteSet(QuoteSetSQL.getQuoteSetById(results.getLong(QUOTE_SET_ID_FIELD.getName())));
		return equityOptionVolatilitySurface;
	}

	public static EquityOptionVolatilitySurface getEquityOptionVolatilitySurfaceByName(String surfaceName) {
		EquityOptionVolatilitySurface equityOptionVolatilitySurface = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_SURFACE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		StringBuilder strikesSql = new StringBuilder(TradistaDBUtil.buildSelectQuery(STRIKE_TABLE));
		TradistaDBUtil.addParameterizedFilter(strikesSql, STRIKE_SURFACE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityVolatilitySurfaceByName = con.prepareStatement(sql.toString());
				PreparedStatement stmtGetStrikesBySurfaceId = con.prepareStatement(strikesSql.toString())) {
			stmtGetEquityVolatilitySurfaceByName.setString(1, surfaceName);
			try (ResultSet results = stmtGetEquityVolatilitySurfaceByName.executeQuery()) {
				while (results.next()) {
					equityOptionVolatilitySurface = buildEquityOptionVolatilitySurface(results,
							stmtGetStrikesBySurfaceId);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptionVolatilitySurface;
	}

	public static EquityOptionVolatilitySurface getEquityOptionVolatilitySurfaceById(long surfaceId) {
		EquityOptionVolatilitySurface equityOptionVolatilitySurface = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_SURFACE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, ID_FIELD);
		StringBuilder strikesSql = new StringBuilder(TradistaDBUtil.buildSelectQuery(STRIKE_TABLE));
		TradistaDBUtil.addParameterizedFilter(strikesSql, STRIKE_SURFACE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityVolatilitySurfaceById = con.prepareStatement(sql.toString());
				PreparedStatement stmtGetStrikesBySurfaceId = con.prepareStatement(strikesSql.toString())) {
			stmtGetEquityVolatilitySurfaceById.setLong(1, surfaceId);
			try (ResultSet results = stmtGetEquityVolatilitySurfaceById.executeQuery()) {
				while (results.next()) {
					equityOptionVolatilitySurface = buildEquityOptionVolatilitySurface(results,
							stmtGetStrikesBySurfaceId);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptionVolatilitySurface;
	}

	public static boolean deleteEquityOptionVolatilitySurface(long surfaceId) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteSurfacePoints = TradistaDBUtil.buildDeletePreparedStatement(con,
						POINT_TABLE, VOLATILITY_SURFACE_ID_FIELD);
				PreparedStatement stmtDeleteStrikes = TradistaDBUtil.buildDeletePreparedStatement(con, STRIKE_TABLE,
						STRIKE_SURFACE_ID_FIELD);
				PreparedStatement stmtDeleteQuotes = TradistaDBUtil.buildDeletePreparedStatement(con,
						VOLATILITY_SURFACE_QUOTE_TABLE, SURFACE_ID_FIELD);
				PreparedStatement stmtDeleteEquityVolatilitySurface = TradistaDBUtil.buildDeletePreparedStatement(con,
						VOLATILITY_SURFACE_TABLE, ID_FIELD)) {
			stmtDeleteSurfacePoints.setLong(1, surfaceId);
			stmtDeleteSurfacePoints.executeUpdate();
			stmtDeleteStrikes.setLong(1, surfaceId);
			stmtDeleteStrikes.executeUpdate();
			stmtDeleteQuotes.setLong(1, surfaceId);
			stmtDeleteQuotes.executeUpdate();
			stmtDeleteEquityVolatilitySurface.setLong(1, surfaceId);
			stmtDeleteEquityVolatilitySurface.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static boolean saveEquityOptionVolatilitySurfacePoints(long id,
			List<SurfacePoint<Long, BigDecimal, BigDecimal>> surfacePoints, Long optionExpiry, BigDecimal strike) {
		boolean bSaved = false;
		StringBuilder deleteSql = new StringBuilder("DELETE FROM EQUITY_OPTION_VOLATILITY_SURFACE_POINT");
		TradistaDBUtil.addFilter(deleteSql, VOLATILITY_SURFACE_ID_FIELD, id);
		TradistaDBUtil.addFilter(deleteSql, OPTION_EXPIRY_FIELD, optionExpiry);
		TradistaDBUtil.addFilter(deleteSql, STRIKE_FIELD, strike);
		try (Connection con = TradistaDB.getConnection();
				Statement stmt = con.createStatement();
				PreparedStatement stmtSaveSurfacePoints = TradistaDBUtil.buildInsertPreparedStatement(con, POINT_TABLE,
						POINT_FIELDS)) {
			stmt.executeUpdate(deleteSql.toString());

			for (SurfacePoint<Long, BigDecimal, BigDecimal> point : surfacePoints) {
				if (point != null && point.getzAxis() != null) {
					stmtSaveSurfacePoints.setLong(1, id);
					stmtSaveSurfacePoints.setLong(2, point.getxAxis());
					stmtSaveSurfacePoints.setBigDecimal(3, point.getyAxis());
					stmtSaveSurfacePoints.setBigDecimal(4, point.getzAxis());
					stmtSaveSurfacePoints.addBatch();
				}
				bSaved = true;
			}
			stmtSaveSurfacePoints.executeBatch();
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static BigDecimal getVolatilityBySurfaceNameOptionExpiryAndStrike(String surfaceName, long optionExpiry,
			double strike) {
		BigDecimal volatility = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_FIELD, POINT_TABLE,
				Join.innerEq(VOLATILITY_SURFACE_TABLE, VOLATILITY_SURFACE_ID_FIELD, ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, OPTION_EXPIRY_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, STRIKE_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetVolatility = con.prepareStatement(sql.toString())) {
			stmtGetVolatility.setString(1, surfaceName);
			stmtGetVolatility.setLong(2, optionExpiry);
			stmtGetVolatility.setDouble(3, strike);
			try (ResultSet results = stmtGetVolatility.executeQuery()) {
				while (results.next()) {
					volatility = results.getBigDecimal(VOLATILITY);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return volatility;
	}

	public static List<SurfacePoint<Number, Number, Number>> getEquityOptionVolatilitySurfacePointsBySurfaceIdOptionExpiryAndStrike(
			long volatilitySurfaceId, long optionExpiry, BigDecimal strike) {
		List<SurfacePoint<Number, Number, Number>> points = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(POINT_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, VOLATILITY_SURFACE_ID_FIELD);
		TradistaDBUtil.addFilter(sql, OPTION_EXPIRY_FIELD, optionExpiry);
		TradistaDBUtil.addFilter(sql, STRIKE_FIELD, strike);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString())) {
			stmt.setLong(1, volatilitySurfaceId);
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					if (points == null) {
						points = new ArrayList<>();
					}
					points.add(new SurfacePoint<>(results.getLong(OPTION_EXPIRY), results.getBigDecimal(STRIKE),
							results.getBigDecimal(VOLATILITY)));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static long saveEquityOptionVolatilitySurface(EquityOptionVolatilitySurface surface) {
		long surfaceId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveEquityVolatilitySurface = (surface.getId() != 0)
						? SurfaceSQL.getUpdatePreparedStatement(con)
						: SurfaceSQL.getInsertPreparedStatement(con);
				PreparedStatement stmtDeleteQuotes = SurfaceSQL.getQuoteDeletePreparedStatement(con);
				PreparedStatement stmtSaveQuotes = SurfaceSQL.getQuoteInsertPreparedStatement(con);
				PreparedStatement stmtDeleteStrikes = TradistaDBUtil.buildDeletePreparedStatement(con, STRIKE_TABLE,
						STRIKE_SURFACE_ID_FIELD);
				PreparedStatement stmtSaveStrikes = TradistaDBUtil.buildInsertPreparedStatement(con, STRIKE_TABLE,
						STRIKE_FIELDS);
				PreparedStatement stmtDeletePoints = TradistaDBUtil.buildDeletePreparedStatement(con, POINT_TABLE,
						VOLATILITY_SURFACE_ID_FIELD);
				PreparedStatement stmtSavePoints = TradistaDBUtil.buildInsertPreparedStatement(con, POINT_TABLE,
						POINT_FIELDS)) {

			if (surface.getId() != 0) {
				stmtSaveEquityVolatilitySurface.setLong(9, surface.getId());
			}

			stmtSaveEquityVolatilitySurface.setString(1, surface.getName());
			stmtSaveEquityVolatilitySurface.setString(2, surface.getAlgorithm());
			stmtSaveEquityVolatilitySurface.setString(3, surface.getInterpolator());
			stmtSaveEquityVolatilitySurface.setString(4, surface.getInstance());
			if (surface.getQuoteDate() == null) {
				stmtSaveEquityVolatilitySurface.setNull(5, Types.DATE);
			} else {
				stmtSaveEquityVolatilitySurface.setDate(5, java.sql.Date.valueOf(surface.getQuoteDate()));
			}
			stmtSaveEquityVolatilitySurface.setString(6, EquityOption.EQUITY_OPTION);
			if (surface.getQuoteSet() == null) {
				stmtSaveEquityVolatilitySurface.setNull(7, Types.BIGINT);
			} else {
				stmtSaveEquityVolatilitySurface.setLong(7, surface.getQuoteSet().getId());
			}

			if (surface.getProcessingOrg() == null) {
				stmtSaveEquityVolatilitySurface.setNull(8, Types.BIGINT);
			} else {
				stmtSaveEquityVolatilitySurface.setLong(8, surface.getProcessingOrg().getId());
			}

			stmtSaveEquityVolatilitySurface.executeUpdate();

			if (surface.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveEquityVolatilitySurface.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						surfaceId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating surface failed, no generated key obtained.");
					}
				}
			} else {
				surfaceId = surface.getId();
			}

			// Quotes
			if (surface.getId() != 0) {
				stmtDeleteQuotes.setLong(1, surfaceId);
				stmtDeleteQuotes.executeUpdate();
			}

			if (surface.getQuotes() != null && !surface.getQuotes().isEmpty()) {
				for (Quote quote : surface.getQuotes()) {
					stmtSaveQuotes.setLong(1, surfaceId);
					stmtSaveQuotes.setLong(2, quote.getId());
					stmtSaveQuotes.addBatch();
				}
				stmtSaveQuotes.executeBatch();
			}

			// Strikes
			if (surface.getId() != 0) {
				stmtDeleteStrikes.setLong(1, surfaceId);
				stmtDeleteStrikes.executeUpdate();
			}

			if (surface.getStrikes() != null && !surface.getStrikes().isEmpty()) {
				for (BigDecimal strike : surface.getStrikes()) {
					stmtSaveStrikes.setLong(1, surfaceId);
					stmtSaveStrikes.setBigDecimal(2, strike);
					stmtSaveStrikes.addBatch();
				}
				stmtSaveStrikes.executeBatch();
			}

			// Points
			if (surface.getId() != 0) {
				stmtDeletePoints.setLong(1, surfaceId);
				stmtDeletePoints.executeUpdate();
			}

			if (surface.getPoints() != null && !surface.getPoints().isEmpty()) {
				for (SurfacePoint<Integer, BigDecimal, BigDecimal> point : surface.getPoints()) {
					if (point != null && point.getzAxis() != null) {
						stmtSavePoints.setLong(1, surfaceId);
						stmtSavePoints.setLong(2, point.getxAxis());
						stmtSavePoints.setBigDecimal(3, point.getyAxis());
						stmtSavePoints.setBigDecimal(4, point.getzAxis());
						stmtSavePoints.addBatch();
					}
				}
				stmtSavePoints.executeBatch();
			}

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		surface.setId(surfaceId);
		return surfaceId;
	}

	public static List<SurfacePoint<Integer, BigDecimal, BigDecimal>> getEquityOptionVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId) {
		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> points = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(POINT_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, VOLATILITY_SURFACE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPoints = con.prepareStatement(sql.toString())) {
			stmtGetPoints.setLong(1, volatilitySurfaceId);
			try (ResultSet results = stmtGetPoints.executeQuery()) {
				while (results.next()) {
					if (points == null) {
						points = new ArrayList<>();
					}
					points.add(new SurfacePoint<>(results.getInt(OPTION_EXPIRY), results.getBigDecimal(STRIKE),
							results.getBigDecimal(VOLATILITY)));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static BigDecimal getVolatility(String volatilitySurfaceName, long optionExpiry) {
		BigDecimal volatility = null;
		StringBuilder sql = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(UnaryFunctionExpression.avg(VOLATILITY_FIELD), POINT_TABLE,
						Join.innerEq(VOLATILITY_SURFACE_TABLE, VOLATILITY_SURFACE_ID_FIELD, ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, OPTION_EXPIRY_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetVolatility = con.prepareStatement(sql.toString())) {
			stmtGetVolatility.setString(1, volatilitySurfaceName);
			stmtGetVolatility.setLong(2, optionExpiry);
			try (ResultSet results = stmtGetVolatility.executeQuery()) {
				while (results.next()) {
					volatility = results.getBigDecimal(1);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return volatility;
	}

}