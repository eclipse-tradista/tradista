package org.eclipse.tradista.fx.fxoption.persistence;

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
import org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;
import org.eclipse.tradista.fx.fxoption.model.FXVolatilitySurface;

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

public class FXVolatilitySurfaceSQL {

	private static final String OPTION_EXPIRY = "OPTION_EXPIRY";
	private static final String STRIKE = "STRIKE";

	private static final Field OPTION_EXPIRY_FIELD = new Field(OPTION_EXPIRY);
	private static final Field STRIKE_FIELD = new Field(STRIKE);
	private static final Field VOLATILITY_FIELD = new Field(VOLATILITY);
	private static final Field VOLATILITY_SURFACE_ID_FIELD = new Field(VOLATILITY_SURFACE_ID);

	private static final Field[] POINT_FIELDS = { VOLATILITY_SURFACE_ID_FIELD, OPTION_EXPIRY_FIELD, STRIKE_FIELD,
			VOLATILITY_FIELD };

	private static final Table POINT_TABLE = new Table("FX_VOLATILITY_SURFACE_POINT", POINT_FIELDS);

	private static final Field DELTA_SURFACE_ID_FIELD = new Field(SURFACE_ID);

	private static final Field DELTA_VALUE_FIELD = new Field(VALUE);

	private static final Field[] DELTA_FIELDS = { DELTA_SURFACE_ID_FIELD, DELTA_VALUE_FIELD };

	private static final Table DELTA_TABLE = new Table("FX_VOLATILITY_SURFACE_DELTA", DELTA_FIELDS);

	public static boolean deleteFXVolatilitySurface(long surfaceId) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteSurfacePoints = TradistaDBUtil.buildDeletePreparedStatement(con,
						POINT_TABLE, VOLATILITY_SURFACE_ID_FIELD);
				PreparedStatement stmtDeleteDeltas = TradistaDBUtil.buildDeletePreparedStatement(con, DELTA_TABLE,
						DELTA_SURFACE_ID_FIELD);
				PreparedStatement stmtDeleteQuotes = TradistaDBUtil.buildDeletePreparedStatement(con,
						VOLATILITY_SURFACE_QUOTE_TABLE, SURFACE_ID_FIELD);
				PreparedStatement stmtDeleteFXVolatilitySurface = TradistaDBUtil.buildDeletePreparedStatement(con,
						VOLATILITY_SURFACE_TABLE, ID_FIELD)) {

			stmtDeleteSurfacePoints.setLong(1, surfaceId);
			stmtDeleteSurfacePoints.executeUpdate();

			stmtDeleteDeltas.setLong(1, surfaceId);
			stmtDeleteDeltas.executeUpdate();

			stmtDeleteQuotes.setLong(1, surfaceId);
			stmtDeleteQuotes.executeUpdate();

			stmtDeleteFXVolatilitySurface.setLong(1, surfaceId);
			stmtDeleteFXVolatilitySurface.executeUpdate();

			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<FXVolatilitySurface> getAllFXVolatilitySurfaces() {
		return getFXVolatilitySurfacesByPoId(0);
	}

	public static Set<FXVolatilitySurface> getFXVolatilitySurfacesByPoId(long poId) {
		Set<FXVolatilitySurface> fxVolatilitySurfaces = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_SURFACE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, TYPE_FIELD);
		if (poId > 0) {
			sql.append(AND + " (").append(PROCESSING_ORG_ID_FIELD.getName()).append(" = ").append(poId).append(OR)
					.append(PROCESSING_ORG_ID_FIELD.getName()).append(" IS NULL)");
		}
		StringBuilder deltasSql = new StringBuilder(TradistaDBUtil.buildSelectQuery(DELTA_TABLE));
		TradistaDBUtil.addParameterizedFilter(deltasSql, DELTA_SURFACE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllFXVolatilitySurfaces = con.prepareStatement(sql.toString());
				PreparedStatement stmtGetDeltasBySurfaceId = con.prepareStatement(deltasSql.toString())) {
			stmtGetAllFXVolatilitySurfaces.setString(1, FXOptionTrade.FX_OPTION);
			try (ResultSet results = stmtGetAllFXVolatilitySurfaces.executeQuery()) {
				while (results.next()) {
					if (fxVolatilitySurfaces == null) {
						fxVolatilitySurfaces = new HashSet<>();
					}
					fxVolatilitySurfaces.add(buildFXVolatilitySurface(results, stmtGetDeltasBySurfaceId));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return fxVolatilitySurfaces;
	}

	private static FXVolatilitySurface buildFXVolatilitySurface(ResultSet results,
			PreparedStatement stmtGetDeltasBySurfaceId) throws SQLException {
		long resPoId = results.getLong(PROCESSING_ORG_ID_FIELD.getName());
		LegalEntity po = null;
		if (resPoId > 0) {
			po = LegalEntitySQL.getLegalEntityById(resPoId);
		}
		FXVolatilitySurface fxVolatilitySurface = new FXVolatilitySurface(results.getString(NAME_FIELD.getName()), po);
		long id = results.getLong(ID_FIELD.getName());
		fxVolatilitySurface.setId(id);
		fxVolatilitySurface.setAlgorithm(results.getString(ALGORITHM_FIELD.getName()));
		fxVolatilitySurface.setInterpolator(results.getString(INTERPOLATOR_FIELD.getName()));
		fxVolatilitySurface.setInstance(results.getString(INSTANCE_FIELD.getName()));

		java.sql.Date quoteDate = results.getDate(QUOTE_DATE_FIELD.getName());
		if (quoteDate != null) {
			fxVolatilitySurface.setQuoteDate(quoteDate.toLocalDate());
		}

		// Get the points linked to this surface
		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints = getFXVolatilitySurfacePointsBySurfaceId(id);

		fxVolatilitySurface.setPoints(surfacePoints);

		// Get the quotes linked to this surface
		List<Quote> quotes = QuoteSQL.getQuotesBySurfaceId(id);

		fxVolatilitySurface.setQuotes(quotes);

		// Get the deltas linked to this surface
		List<BigDecimal> deltas = new ArrayList<>();

		stmtGetDeltasBySurfaceId.setLong(1, id);
		try (ResultSet deltasResults = stmtGetDeltasBySurfaceId.executeQuery()) {
			while (deltasResults.next()) {
				deltas.add(deltasResults.getBigDecimal(DELTA_VALUE_FIELD.getName()));
			}
		}

		fxVolatilitySurface.setDeltas(deltas);
		return fxVolatilitySurface;
	}

	public static FXVolatilitySurface getFXVolatilitySurfaceByName(String surfaceName) {
		FXVolatilitySurface fxVolatilitySurface = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_SURFACE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		StringBuilder deltasSql = new StringBuilder(TradistaDBUtil.buildSelectQuery(DELTA_TABLE));
		TradistaDBUtil.addParameterizedFilter(deltasSql, DELTA_SURFACE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXVolatilitySurfaceByName = con.prepareStatement(sql.toString());
				PreparedStatement stmtGetDeltasBySurfaceId = con.prepareStatement(deltasSql.toString())) {
			stmtGetFXVolatilitySurfaceByName.setString(1, surfaceName);
			try (ResultSet results = stmtGetFXVolatilitySurfaceByName.executeQuery()) {
				while (results.next()) {
					fxVolatilitySurface = buildFXVolatilitySurface(results, stmtGetDeltasBySurfaceId);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return fxVolatilitySurface;
	}

	public static FXVolatilitySurface getFXVolatilitySurfaceById(long surfaceId) {
		FXVolatilitySurface fxVolatilitySurface = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_SURFACE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, ID_FIELD);
		StringBuilder deltasSql = new StringBuilder(TradistaDBUtil.buildSelectQuery(DELTA_TABLE));
		TradistaDBUtil.addParameterizedFilter(deltasSql, DELTA_SURFACE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXVolatilitySurfaceById = con.prepareStatement(sql.toString());
				PreparedStatement stmtGetDeltasBySurfaceId = con.prepareStatement(deltasSql.toString())) {
			stmtGetFXVolatilitySurfaceById.setLong(1, surfaceId);
			try (ResultSet results = stmtGetFXVolatilitySurfaceById.executeQuery()) {
				while (results.next()) {
					fxVolatilitySurface = buildFXVolatilitySurface(results, stmtGetDeltasBySurfaceId);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return fxVolatilitySurface;
	}

	public static boolean saveFXVolatilitySurfacePoints(long id,
			List<SurfacePoint<Long, BigDecimal, BigDecimal>> surfacePoints, Long optionExpiry, BigDecimal strike) {
		boolean bSaved = false;
		StringBuilder deleteSql = new StringBuilder("DELETE FROM FX_VOLATILITY_SURFACE_POINT");
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
					stmtSaveSurfacePoints.clearParameters();
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
			double tenor) {
		BigDecimal volatility = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_FIELD, POINT_TABLE,
				Join.innerEq(VOLATILITY_SURFACE_TABLE, VOLATILITY_SURFACE_ID_FIELD, ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, OPTION_EXPIRY_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, STRIKE_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike = con
						.prepareStatement(sql.toString())) {
			stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike.setString(1, surfaceName);
			stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike.setLong(2, optionExpiry);
			stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike.setDouble(3, tenor);
			try (ResultSet results = stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike.executeQuery()) {
				while (results.next()) {
					volatility = results.getBigDecimal(VOLATILITY);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return volatility;
	}

	public static List<SurfacePoint<Number, Number, Number>> getFXVolatilitySurfacePointsBySurfaceIdOptionExpiryAndStrike(
			long volatilitySurfaceId, long optionExpiry, BigDecimal strike) {
		List<SurfacePoint<Number, Number, Number>> points = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(POINT_FIELDS, POINT_TABLE,
				Join.innerEq(VOLATILITY_SURFACE_TABLE, VOLATILITY_SURFACE_ID_FIELD, ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sql, ID_FIELD);
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

	public static long saveFXVolatilitySurface(FXVolatilitySurface surface) {
		long surfaceId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveFXVolatilitySurface = (surface.getId() != 0)
						? SurfaceSQL.getUpdatePreparedStatement(con)
						: SurfaceSQL.getInsertPreparedStatement(con);
				PreparedStatement stmtDeleteQuotes = SurfaceSQL.getQuoteDeletePreparedStatement(con);
				PreparedStatement stmtSaveQuotes = SurfaceSQL.getQuoteInsertPreparedStatement(con);
				PreparedStatement stmtDeleteDeltas = TradistaDBUtil.buildDeletePreparedStatement(con, DELTA_TABLE,
						DELTA_SURFACE_ID_FIELD);
				PreparedStatement stmtSaveDeltas = TradistaDBUtil.buildInsertPreparedStatement(con, DELTA_TABLE,
						DELTA_FIELDS);
				PreparedStatement stmtDeletePoints = TradistaDBUtil.buildDeletePreparedStatement(con, POINT_TABLE,
						VOLATILITY_SURFACE_ID_FIELD);
				PreparedStatement stmtSavePoints = TradistaDBUtil.buildInsertPreparedStatement(con, POINT_TABLE,
						POINT_FIELDS)) {

			if (surface.getId() != 0) {
				stmtSaveFXVolatilitySurface.setLong(9, surface.getId());
			}
			stmtSaveFXVolatilitySurface.setString(1, surface.getName());
			stmtSaveFXVolatilitySurface.setString(2, surface.getAlgorithm());
			stmtSaveFXVolatilitySurface.setString(3, surface.getInterpolator());
			stmtSaveFXVolatilitySurface.setString(4, surface.getInstance());
			if (surface.getQuoteDate() == null) {
				stmtSaveFXVolatilitySurface.setNull(5, Types.DATE);
			} else {
				stmtSaveFXVolatilitySurface.setDate(5, java.sql.Date.valueOf(surface.getQuoteDate()));
			}
			stmtSaveFXVolatilitySurface.setString(6, FXOptionTrade.FX_OPTION);
			if (surface.getQuoteSet() == null) {
				stmtSaveFXVolatilitySurface.setNull(7, Types.BIGINT);
			} else {
				stmtSaveFXVolatilitySurface.setLong(7, surface.getQuoteSet().getId());
			}
			if (surface.getProcessingOrg() == null) {
				stmtSaveFXVolatilitySurface.setNull(8, Types.BIGINT);
			} else {
				stmtSaveFXVolatilitySurface.setLong(8, surface.getProcessingOrg().getId());
			}

			stmtSaveFXVolatilitySurface.executeUpdate();

			if (surface.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveFXVolatilitySurface.getGeneratedKeys()) {
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

			// Deltas
			if (surface.getId() != 0) {
				stmtDeleteDeltas.setLong(1, surfaceId);
				stmtDeleteDeltas.executeUpdate();
			}

			if (surface.getDeltas() != null && !surface.getDeltas().isEmpty()) {
				for (BigDecimal delta : surface.getDeltas()) {
					stmtSaveDeltas.setLong(1, surfaceId);
					stmtSaveDeltas.setBigDecimal(2, delta);
					stmtSaveDeltas.addBatch();
				}
				stmtSaveDeltas.executeBatch();
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
						stmtSavePoints.setInt(2, point.getxAxis());
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

	public static List<SurfacePoint<Integer, BigDecimal, BigDecimal>> getFXVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId) {
		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> points = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(POINT_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, VOLATILITY_SURFACE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXVolatilitySurfacePointsBySurfaceId = con.prepareStatement(sql.toString())) {
			stmtGetFXVolatilitySurfacePointsBySurfaceId.setLong(1, volatilitySurfaceId);
			try (ResultSet results = stmtGetFXVolatilitySurfacePointsBySurfaceId.executeQuery()) {
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

	public static BigDecimal getVolatilityBySurfaceNameOptionExpiry(String volatilitySurfaceName, int optionExpiry) {
		BigDecimal volatility = null;
		StringBuilder sql = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(UnaryFunctionExpression.avg(VOLATILITY_FIELD), POINT_TABLE,
						Join.innerEq(VOLATILITY_SURFACE_TABLE, VOLATILITY_SURFACE_ID_FIELD, ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, OPTION_EXPIRY_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetVolatilityBySurfaceNameAndOptionExpiry = con
						.prepareStatement(sql.toString())) {
			stmtGetVolatilityBySurfaceNameAndOptionExpiry.setString(1, volatilitySurfaceName);
			stmtGetVolatilityBySurfaceNameAndOptionExpiry.setLong(2, optionExpiry);
			try (ResultSet results = stmtGetVolatilityBySurfaceNameAndOptionExpiry.executeQuery()) {
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