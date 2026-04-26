package org.eclipse.tradista.ir.irswapoption.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.AND;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.OR;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.VOLATILITY;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.VOLATILITY_SURFACE_ID;
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
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.core.marketdata.persistence.QuoteSQL;
import org.eclipse.tradista.core.marketdata.persistence.QuoteSetSQL;
import org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;

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

public class SwaptionVolatilitySurfaceSQL {

	private static final String SWAPTION_LIFETIME = "SWAPTION_LIFETIME";
	private static final String SWAP_LIFETIME = "SWAP_LIFETIME";

	private static final Field SWAPTION_LIFETIME_FIELD = new Field(SWAPTION_LIFETIME);
	private static final Field SWAP_LIFETIME_FIELD = new Field(SWAP_LIFETIME);
	private static final Field VOLATILITY_FIELD = new Field(VOLATILITY);
	private static final Field VOLATILITY_SURFACE_ID_FIELD = new Field(VOLATILITY_SURFACE_ID);

	private static final Field[] POINT_FIELDS = { VOLATILITY_SURFACE_ID_FIELD, SWAPTION_LIFETIME_FIELD,
			SWAP_LIFETIME_FIELD, VOLATILITY_FIELD };

	private static final Table POINT_TABLE = new Table("SWAPTION_VOLATILITY_SURFACE_POINT", POINT_FIELDS);

	public static boolean saveSwaptionVolatilitySurface(String surfaceName) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveSwaptionVolatilitySurface = TradistaDBUtil.buildInsertPreparedStatement(con,
						VOLATILITY_SURFACE_TABLE, NAME_FIELD, TYPE_FIELD)) {
			stmtSaveSwaptionVolatilitySurface.setString(1, surfaceName);
			stmtSaveSwaptionVolatilitySurface.setString(2, IRSwapOptionTrade.IR_SWAP_OPTION);
			stmtSaveSwaptionVolatilitySurface.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static boolean deleteSwaptionVolatilitySurface(long surfaceId) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteSurfacePoints = TradistaDBUtil.buildDeletePreparedStatement(con,
						POINT_TABLE, VOLATILITY_SURFACE_ID_FIELD);
				PreparedStatement stmtDeleteQuotes = TradistaDBUtil.buildDeletePreparedStatement(con,
						VOLATILITY_SURFACE_QUOTE_TABLE, SURFACE_ID_FIELD);
				PreparedStatement stmtDeleteSwaptionVolatilitySurface = TradistaDBUtil.buildDeletePreparedStatement(con,
						VOLATILITY_SURFACE_TABLE, ID_FIELD);) {
			stmtDeleteSurfacePoints.setLong(1, surfaceId);
			stmtDeleteSurfacePoints.executeUpdate();
			stmtDeleteQuotes.setLong(1, surfaceId);
			stmtDeleteQuotes.executeUpdate();
			stmtDeleteSwaptionVolatilitySurface.setLong(1, surfaceId);
			stmtDeleteSwaptionVolatilitySurface.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<SwaptionVolatilitySurface> getAllSwaptionVolatilitySurfaces() {
		return getSwaptionVolatilitySurfacesByPoId(0);
	}

	public static Set<SwaptionVolatilitySurface> getSwaptionVolatilitySurfacesByPoId(long poId) {
		Set<SwaptionVolatilitySurface> swaptionVolatilitySurfaces = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_SURFACE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, TYPE_FIELD);
		if (poId > 0) {
			sql.append(AND + " (").append(PROCESSING_ORG_ID_FIELD.getName()).append(" = ").append(poId).append(OR)
					.append(PROCESSING_ORG_ID_FIELD.getName()).append(" IS NULL)");
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetSurfaces = con.prepareStatement(sql.toString())) {
			stmtGetSurfaces.setString(1, IRSwapOptionTrade.IR_SWAP_OPTION);
			try (ResultSet results = stmtGetSurfaces.executeQuery()) {
				while (results.next()) {
					if (swaptionVolatilitySurfaces == null) {
						swaptionVolatilitySurfaces = new HashSet<>();
					}
					swaptionVolatilitySurfaces.add(buildSwaptionVolatilitySurface(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return swaptionVolatilitySurfaces;
	}

	private static SwaptionVolatilitySurface buildSwaptionVolatilitySurface(ResultSet results) throws SQLException {
		long resPoId = results.getLong(PROCESSING_ORG_ID_FIELD.getName());
		LegalEntity po = null;
		if (resPoId > 0) {
			po = LegalEntitySQL.getLegalEntityById(resPoId);
		}
		SwaptionVolatilitySurface swaptionVolatilitySurface = new SwaptionVolatilitySurface(
				results.getString(NAME_FIELD.getName()), po);
		long id = results.getLong(ID_FIELD.getName());
		swaptionVolatilitySurface.setId(id);
		swaptionVolatilitySurface.setAlgorithm(results.getString(ALGORITHM_FIELD.getName()));
		swaptionVolatilitySurface.setInterpolator(results.getString(INTERPOLATOR_FIELD.getName()));
		swaptionVolatilitySurface.setInstance(results.getString(INSTANCE_FIELD.getName()));
		java.sql.Date quoteDate = results.getDate(QUOTE_DATE_FIELD.getName());
		if (quoteDate != null) {
			swaptionVolatilitySurface.setQuoteDate(quoteDate.toLocalDate());
		}
		// Get the points linked to this surface
		List<SurfacePoint<Integer, Integer, BigDecimal>> surfacePoints = getSwaptionVolatilitySurfacePointsBySurfaceId(
				id);
		swaptionVolatilitySurface.setPoints(surfacePoints);

		// Get the quotes linked to this surface
		List<Quote> quotes = QuoteSQL.getQuotesBySurfaceId(id);

		swaptionVolatilitySurface.setQuotes(quotes);
		swaptionVolatilitySurface
				.setQuoteSet(QuoteSetSQL.getQuoteSetById(results.getLong(QUOTE_SET_ID_FIELD.getName())));
		return swaptionVolatilitySurface;
	}

	public static SwaptionVolatilitySurface getSwaptionVolatilitySurfaceByName(String surfaceName) {
		SwaptionVolatilitySurface swaptionVolatilitySurface = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_SURFACE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetSwaptionVolatilitySurfaceByName = con.prepareStatement(sql.toString())) {
			stmtGetSwaptionVolatilitySurfaceByName.setString(1, surfaceName);
			try (ResultSet results = stmtGetSwaptionVolatilitySurfaceByName.executeQuery()) {
				while (results.next()) {
					swaptionVolatilitySurface = buildSwaptionVolatilitySurface(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return swaptionVolatilitySurface;
	}

	public static SwaptionVolatilitySurface getSwaptionVolatilitySurfaceById(long surfaceId) {
		SwaptionVolatilitySurface swaptionVolatilitySurface = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_SURFACE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetSwaptionVolatilitySurfaceById = con.prepareStatement(sql.toString())) {
			stmtGetSwaptionVolatilitySurfaceById.setLong(1, surfaceId);
			try (ResultSet results = stmtGetSwaptionVolatilitySurfaceById.executeQuery()) {
				while (results.next()) {
					swaptionVolatilitySurface = buildSwaptionVolatilitySurface(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return swaptionVolatilitySurface;
	}

	public static boolean saveSwaptionVolatilitySurfacePoints(long id,
			List<SurfacePoint<Float, Float, Float>> surfacePoints, Float optionLifeTime, Float swapLifetime) {
		boolean bSaved = false;
		StringBuilder deleteSql = new StringBuilder("DELETE FROM SWAPTION_VOLATILITY_SURFACE_POINT");
		TradistaDBUtil.addFilter(deleteSql, VOLATILITY_SURFACE_ID_FIELD, id);
		TradistaDBUtil.addFilter(deleteSql, SWAPTION_LIFETIME_FIELD, optionLifeTime);
		TradistaDBUtil.addFilter(deleteSql, SWAP_LIFETIME_FIELD, swapLifetime);
		try (Connection con = TradistaDB.getConnection();
				Statement stmt = con.createStatement();
				PreparedStatement stmtSaveSurfacePoints = TradistaDBUtil.buildInsertPreparedStatement(con, POINT_TABLE,
						POINT_FIELDS)) {
			stmt.executeUpdate(deleteSql.toString());

			for (SurfacePoint<Float, Float, Float> point : surfacePoints) {
				if (point != null && point.getzAxis() != null) {
					stmtSaveSurfacePoints.setLong(1, id);
					stmtSaveSurfacePoints.setFloat(2, point.getxAxis());
					stmtSaveSurfacePoints.setFloat(3, point.getyAxis());
					stmtSaveSurfacePoints.setFloat(4, point.getzAxis());
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

	public static BigDecimal getVolatilityBySurfaceNameTimeToMaturityAndTenor(String surfaceName, int timeToMaturity,
			int tenor) {
		BigDecimal volatility = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_FIELD, POINT_TABLE,
				Join.innerEq(VOLATILITY_SURFACE_TABLE, VOLATILITY_SURFACE_ID_FIELD, ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, SWAPTION_LIFETIME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, SWAP_LIFETIME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetVolatility = con.prepareStatement(sql.toString())) {
			stmtGetVolatility.setString(1, surfaceName);
			stmtGetVolatility.setInt(2, timeToMaturity);
			stmtGetVolatility.setInt(3, tenor);
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

	public static List<SurfacePoint<Number, Number, Number>> getSwaptionVolatilitySurfacePointsBySurfaceIdOptionAndSwapLifetimes(
			long currentSwaptionVolatilitySurfaceId, Float optionLifetime, Float swapLifetime) {
		List<SurfacePoint<Number, Number, Number>> points = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(POINT_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, VOLATILITY_SURFACE_ID_FIELD);
		TradistaDBUtil.addFilter(sql, SWAPTION_LIFETIME_FIELD, optionLifetime);
		TradistaDBUtil.addFilter(sql, SWAP_LIFETIME_FIELD, swapLifetime);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString())) {
			stmt.setLong(1, currentSwaptionVolatilitySurfaceId);
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					if (points == null) {
						points = new ArrayList<>();
					}
					points.add(new SurfacePoint<>(results.getFloat(SWAPTION_LIFETIME), results.getFloat(SWAP_LIFETIME),
							results.getFloat(VOLATILITY)));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static long saveSwaptionVolatilitySurface(SwaptionVolatilitySurface surface) {
		long surfaceId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveSwaptionVolatilitySurface = (surface.getId() != 0)
						? SurfaceSQL.getUpdatePreparedStatement(con)
						: SurfaceSQL.getInsertPreparedStatement(con);
				PreparedStatement stmtDeleteQuotes = SurfaceSQL.getQuoteDeletePreparedStatement(con);
				PreparedStatement stmtSaveQuotes = SurfaceSQL.getQuoteInsertPreparedStatement(con);
				PreparedStatement stmtDeletePoints = TradistaDBUtil.buildDeletePreparedStatement(con, POINT_TABLE,
						VOLATILITY_SURFACE_ID_FIELD);
				PreparedStatement stmtSavePoints = TradistaDBUtil.buildInsertPreparedStatement(con, POINT_TABLE,
						POINT_FIELDS)) {

			if (surface.getId() != 0) {
				stmtSaveSwaptionVolatilitySurface.setLong(9, surface.getId());
			}
			stmtSaveSwaptionVolatilitySurface.setString(1, surface.getName());
			stmtSaveSwaptionVolatilitySurface.setString(2, surface.getAlgorithm());
			stmtSaveSwaptionVolatilitySurface.setString(3, surface.getInterpolator());
			stmtSaveSwaptionVolatilitySurface.setString(4, surface.getInstance());
			if (surface.getQuoteDate() == null) {
				stmtSaveSwaptionVolatilitySurface.setNull(5, Types.DATE);
			} else {
				stmtSaveSwaptionVolatilitySurface.setDate(5, java.sql.Date.valueOf(surface.getQuoteDate()));
			}
			stmtSaveSwaptionVolatilitySurface.setString(6, IRSwapOptionTrade.IR_SWAP_OPTION);
			if (surface.getQuoteSet() == null) {
				stmtSaveSwaptionVolatilitySurface.setNull(7, Types.BIGINT);
			} else {
				stmtSaveSwaptionVolatilitySurface.setLong(7, surface.getQuoteSet().getId());
			}
			if (surface.getProcessingOrg() == null) {
				stmtSaveSwaptionVolatilitySurface.setNull(8, Types.BIGINT);
			} else {
				stmtSaveSwaptionVolatilitySurface.setLong(8, surface.getProcessingOrg().getId());
			}

			stmtSaveSwaptionVolatilitySurface.executeUpdate();

			if (surface.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveSwaptionVolatilitySurface.getGeneratedKeys()) {
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

			// Points
			if (surface.getId() != 0) {
				stmtDeletePoints.setLong(1, surfaceId);
				stmtDeletePoints.executeUpdate();
			}

			if (surface.getPoints() != null && !surface.getPoints().isEmpty()) {
				for (SurfacePoint<Integer, Integer, BigDecimal> point : surface.getPoints()) {
					if (point != null && point.getzAxis() != null) {
						stmtSavePoints.setLong(1, surfaceId);
						stmtSavePoints.setLong(2, point.getxAxis());
						stmtSavePoints.setLong(3, point.getyAxis());
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

	public static List<SurfacePoint<Integer, Integer, BigDecimal>> getSwaptionVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId) {
		List<SurfacePoint<Integer, Integer, BigDecimal>> points = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(POINT_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, VOLATILITY_SURFACE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetSwaptionVolatilitySurfacePointsBySurfaceId = con
						.prepareStatement(sql.toString())) {
			stmtGetSwaptionVolatilitySurfacePointsBySurfaceId.setLong(1, volatilitySurfaceId);
			try (ResultSet results = stmtGetSwaptionVolatilitySurfacePointsBySurfaceId.executeQuery()) {
				while (results.next()) {
					if (points == null) {
						points = new ArrayList<>();
					}
					points.add(new SurfacePoint<>(results.getInt(SWAPTION_LIFETIME), results.getInt(SWAP_LIFETIME),
							results.getBigDecimal(VOLATILITY)));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

}