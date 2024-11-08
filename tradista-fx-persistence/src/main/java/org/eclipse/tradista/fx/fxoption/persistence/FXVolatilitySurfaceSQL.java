package org.eclipse.tradista.fx.fxoption.persistence;

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
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.core.marketdata.persistence.QuoteSQL;
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

	public static boolean deleteFXVolatilitySurface(long surfaceId) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteSurfacePoints = con
						.prepareStatement("DELETE FROM FX_VOLATILITY_SURFACE_POINT WHERE VOLATILITY_SURFACE_ID = ?");
				PreparedStatement stmtDeleteDeltasBySurfaceName = con
						.prepareStatement("DELETE FROM FX_VOLATILITY_SURFACE_DELTA WHERE SURFACE_ID = ?");
				PreparedStatement stmtDeleteQuotesBySurfaceName = con
						.prepareStatement("DELETE FROM VOLATILITY_SURFACE_QUOTE WHERE SURFACE_ID = ?");
				PreparedStatement stmtDeleteFXVolatilitySurface = con
						.prepareStatement("DELETE FROM VOLATILITY_SURFACE WHERE ID = ?")) {

			stmtDeleteSurfacePoints.setLong(1, surfaceId);
			stmtDeleteSurfacePoints.executeUpdate();

			stmtDeleteDeltasBySurfaceName.setLong(1, surfaceId);
			stmtDeleteDeltasBySurfaceName.executeUpdate();

			stmtDeleteQuotesBySurfaceName.setLong(1, surfaceId);
			stmtDeleteQuotesBySurfaceName.executeUpdate();

			stmtDeleteFXVolatilitySurface.setLong(1, surfaceId);
			stmtDeleteFXVolatilitySurface.executeUpdate();

			bSaved = true;
		} catch (SQLException sqle) {
			bSaved = false;
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<FXVolatilitySurface> getAllFXVolatilitySurfaces() {
		Set<FXVolatilitySurface> fxVolatilitySurfaces = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllFXVolatilitySurfaces = con
						.prepareStatement("SELECT * FROM VOLATILITY_SURFACE WHERE TYPE = 'FXOption'");
				PreparedStatement stmtGetDeltasBySurfaceId = con
						.prepareStatement("SELECT * FROM FX_VOLATILITY_SURFACE_DELTA WHERE SURFACE_ID = ?");
				ResultSet results = stmtGetAllFXVolatilitySurfaces.executeQuery();) {
			while (results.next()) {
				if (fxVolatilitySurfaces == null) {
					fxVolatilitySurfaces = new HashSet<FXVolatilitySurface>();
				}
				long poId = results.getLong("processing_org_id");
				LegalEntity po = null;
				if (poId > 0) {
					po = LegalEntitySQL.getLegalEntityById(poId);
				}
				FXVolatilitySurface fxVolatilitySurface = new FXVolatilitySurface(results.getString("name"), po);
				long id = results.getLong("id");
				fxVolatilitySurface.setId(id);
				fxVolatilitySurface.setAlgorithm(results.getString("algorithm"));
				fxVolatilitySurface.setInterpolator(results.getString("interpolator"));
				fxVolatilitySurface.setInstance(results.getString("instance"));

				java.sql.Date quoteDate = results.getDate("quote_date");
				if (quoteDate != null) {
					fxVolatilitySurface.setQuoteDate(quoteDate.toLocalDate());
				}

				// Get the points linked to this surface
				List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints = getFXVolatilitySurfacePointsBySurfaceId(
						id);

				fxVolatilitySurface.setPoints(surfacePoints);

				// Get the quotes linked to this surface
				List<Quote> quotes = QuoteSQL.getQuotesBySurfaceId(id);

				fxVolatilitySurface.setQuotes(quotes);

				// Get the deltas linked to this surface
				List<BigDecimal> deltas = new ArrayList<BigDecimal>();

				stmtGetDeltasBySurfaceId.setLong(1, id);
				try (ResultSet deltasResults = stmtGetDeltasBySurfaceId.executeQuery()) {
					while (deltasResults.next()) {
						deltas.add(deltasResults.getBigDecimal("value"));
					}
				}

				fxVolatilitySurface.setDeltas(deltas);

				fxVolatilitySurfaces.add(fxVolatilitySurface);
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return fxVolatilitySurfaces;
	}

	public static FXVolatilitySurface getFXVolatilitySurfaceByName(String surfaceName) {

		FXVolatilitySurface fxVolatilitySurface = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXVolatilitySurfaceByName = con
						.prepareStatement("SELECT * FROM VOLATILITY_SURFACE WHERE NAME = ?");
				PreparedStatement stmtGetDeltasBySurfaceId = con
						.prepareStatement("SELECT * FROM FX_VOLATILITY_SURFACE_DELTA WHERE SURFACE_ID = ?")) {

			stmtGetFXVolatilitySurfaceByName.setString(1, surfaceName);
			try (ResultSet results = stmtGetFXVolatilitySurfaceByName.executeQuery()) {

				while (results.next()) {
					long poId = results.getLong("processing_org_id");
					LegalEntity po = null;
					if (poId > 0) {
						po = LegalEntitySQL.getLegalEntityById(poId);
					}
					fxVolatilitySurface = new FXVolatilitySurface(results.getString("name"), po);
					long id = results.getLong("id");
					fxVolatilitySurface.setId(id);
					fxVolatilitySurface.setAlgorithm(results.getString("algorithm"));
					fxVolatilitySurface.setInterpolator(results.getString("interpolator"));
					fxVolatilitySurface.setInstance(results.getString("instance"));
					java.sql.Date quoteDate = results.getDate("quote_date");
					if (quoteDate != null) {
						fxVolatilitySurface.setQuoteDate(quoteDate.toLocalDate());
					}

					// Get the points linked to this surface
					List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints = getFXVolatilitySurfacePointsBySurfaceId(
							id);

					fxVolatilitySurface.setPoints(surfacePoints);

					// Get the quotes linked to this surface
					List<Quote> quotes = QuoteSQL.getQuotesBySurfaceId(id);

					fxVolatilitySurface.setQuotes(quotes);

					// Get the deltas linked to this surface
					List<BigDecimal> deltas = new ArrayList<BigDecimal>();

					stmtGetDeltasBySurfaceId.setLong(1, id);
					try (ResultSet deltasResults = stmtGetDeltasBySurfaceId.executeQuery()) {

						while (deltasResults.next()) {
							deltas.add(deltasResults.getBigDecimal("value"));
						}
					}

					fxVolatilitySurface.setDeltas(deltas);
				}
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return fxVolatilitySurface;
	}

	public static FXVolatilitySurface getFXVolatilitySurfaceById(long surfaceId) {

		FXVolatilitySurface fxVolatilitySurface = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXVolatilitySurfaceById = con
						.prepareStatement("SELECT * FROM VOLATILITY_SURFACE WHERE ID = ?");
				PreparedStatement stmtGetDeltasBySurfaceId = con
						.prepareStatement("SELECT * FROM FX_VOLATILITY_SURFACE_DELTA WHERE SURFACE_ID = ?")) {

			stmtGetFXVolatilitySurfaceById.setLong(1, surfaceId);
			try (ResultSet results = stmtGetFXVolatilitySurfaceById.executeQuery()) {

				while (results.next()) {
					long poId = results.getLong("processing_org_id");
					LegalEntity po = null;
					if (poId > 0) {
						po = LegalEntitySQL.getLegalEntityById(poId);
					}
					fxVolatilitySurface = new FXVolatilitySurface(results.getString("name"), po);
					long id = results.getLong("id");
					fxVolatilitySurface.setId(id);
					fxVolatilitySurface.setAlgorithm(results.getString("algorithm"));
					fxVolatilitySurface.setInterpolator(results.getString("interpolator"));
					fxVolatilitySurface.setInstance(results.getString("instance"));
					java.sql.Date quoteDate = results.getDate("quote_date");
					if (quoteDate != null) {
						fxVolatilitySurface.setQuoteDate(quoteDate.toLocalDate());
					}

					// Get the points linked to this surface
					List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints = getFXVolatilitySurfacePointsBySurfaceId(
							id);

					fxVolatilitySurface.setPoints(surfacePoints);

					// Get the quotes linked to this surface
					List<Quote> quotes = QuoteSQL.getQuotesBySurfaceId(id);

					fxVolatilitySurface.setQuotes(quotes);

					// Get the deltas linked to this surface
					List<BigDecimal> deltas = new ArrayList<BigDecimal>();

					stmtGetDeltasBySurfaceId.setLong(1, id);
					try (ResultSet deltasResults = stmtGetDeltasBySurfaceId.executeQuery()) {

						while (deltasResults.next()) {
							deltas.add(deltasResults.getBigDecimal("value"));
						}
					}

					fxVolatilitySurface.setDeltas(deltas);
				}
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return fxVolatilitySurface;
	}

	public static boolean saveFXVolatilitySurfacePoints(long id,
			List<SurfacePoint<Long, BigDecimal, BigDecimal>> surfacePoints, Long optionExpiry, BigDecimal strike) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				Statement stmt = con.createStatement();
				PreparedStatement stmtSaveSurfacePoints = con
						.prepareStatement("INSERT INTO FX_VOLATILITY_SURFACE_POINT VALUES(?,?,?,?) ")) {

			// First, we delete the data for this surface, option and swap
			// lifetimes

			String delete = "DELETE FROM FX_VOLATILITY_SURFACE_POINT WHERE SURFACE_ID =  " + id;
			if (optionExpiry != null) {
				delete += " AND OPTION_EXPIRY = " + optionExpiry;
			}
			if (strike != null) {
				delete += " AND STRIKE = " + strike;
			}

			stmt.executeUpdate(delete);

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
			bSaved = false;
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return bSaved;

	}

	public static BigDecimal getVolatilityBySurfaceNameOptionExpiryAndStrike(String surfaceName, long optionExpiry,
			double tenor) {

		BigDecimal volatility = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike = con
						.prepareStatement("SELECT VOLATILITY FROM VOLATILITY_SURFACE, FX_VOLATILITY_SURFACE_POINT"
								+ " WHERE VOLATILITY_SURFACE.ID = FX_VOLATILITY_SURFACE_POINT.VOLATILITY_SURFACE_ID"
								+ " AND NAME = ? AND OPTION_EXPIRY = ? AND STRIKE = ?")) {
			stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike.setString(1, surfaceName);
			stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike.setLong(2, optionExpiry);
			stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike.setDouble(3, tenor);
			try (ResultSet results = stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike.executeQuery()) {
				while (results.next()) {
					volatility = results.getBigDecimal("volatility");
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return volatility;
	}

	public static List<SurfacePoint<Number, Number, Number>> getFXVolatilitySurfacePointsBySurfaceIdOptionExpiryAndStrike(
			long volatilitySurfaceId, long optionExpiry, BigDecimal strike) {

		List<SurfacePoint<Number, Number, Number>> points = null;
		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {
			String query = "SELECT FX_VOLATILITY_SURFACE_POINT.OPTION_EXPIRY, FX_VOLATILITY_SURFACE_POINT.STRIKE, FX_VOLATILITY_SURFACE_POINT.VOLATILITY "
					+ "FROM FX_VOLATILITY_SURFACE_POINT, SURFACE WHERE "
					+ "FX_VOLATILITY_SURFACE_POINT.SURFACE_ID = SURFACE.ID";

			if (optionExpiry != 0) {
				query += " AND OPTION_EXPIRY = " + optionExpiry;
			}

			if (strike != null) {
				query += " AND STRIKE = " + strike;
			}

			try (ResultSet results = stmt.executeQuery(query)) {

				while (results.next()) {
					if (points == null) {
						points = new ArrayList<SurfacePoint<Number, Number, Number>>();
					}
					Long optionLt = results.getLong("option_expiry");
					BigDecimal strikeRes = results.getBigDecimal("strike");
					BigDecimal volatility = results.getBigDecimal("volatility");
					points.add(new SurfacePoint<Number, Number, Number>(optionLt, strikeRes, volatility));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static long saveFXVolatilitySurface(FXVolatilitySurface surface) {
		long surfaceId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveFXVolatilitySurface = (surface.getId() != 0) ? con.prepareStatement(
						"UPDATE VOLATILITY_SURFACE SET NAME=?, ALGORITHM =?, INTERPOLATOR=?, INSTANCE=?, QUOTE_DATE=?, TYPE=?, QUOTE_SET_ID=?, PROCESSING_ORG_ID=? WHERE ID = ?")
						: con.prepareStatement(
								"INSERT INTO VOLATILITY_SURFACE(NAME,ALGORITHM,INTERPOLATOR,INSTANCE,QUOTE_DATE,TYPE,QUOTE_SET_ID,PROCESSING_ORG_ID) VALUES (?,?, ?, ?, ?, ?, ?, ?)",
								Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtDeleteQuotesBySurfaceId = (surface.getId() != 0)
						? con.prepareStatement("DELETE FROM VOLATILITY_SURFACE_QUOTE WHERE SURFACE_ID = ? ")
						: null;
				PreparedStatement stmtSaveRateQuotes = (surface.getQuotes() != null && !surface.getQuotes().isEmpty())
						? con.prepareStatement("INSERT INTO VOLATILITY_SURFACE_QUOTE VALUES(?,?) ")
						: null;
				PreparedStatement stmtDeleteDeltasBySurfaceId = (surface.getId() != 0)
						? con.prepareStatement("DELETE FROM FX_VOLATILITY_SURFACE_DELTA WHERE SURFACE_ID = ? ")
						: null;
				PreparedStatement stmtSaveDeltas = con
						.prepareStatement("INSERT INTO FX_VOLATILITY_SURFACE_DELTA(SURFACE_ID, VALUE) VALUES(?,?) ");
				PreparedStatement stmtDeleteSurfacePoints = (surface.getId() != 0) ? con.prepareStatement(
						"DELETE FROM FX_VOLATILITY_SURFACE_POINT WHERE VOLATILITY_SURFACE_ID =  ?") : null;
				PreparedStatement stmtSaveSurfacePoints = con
						.prepareStatement("INSERT INTO FX_VOLATILITY_SURFACE_POINT VALUES(?,?,?,?) ")) {

			// First, we save the Swaption Volatility Surface definition

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

			if (surface.getId() != 0) {
				// Then, we delete the current curve's quote ids list.
				stmtDeleteQuotesBySurfaceId.setLong(1, surface.getId());
				stmtDeleteQuotesBySurfaceId.executeUpdate();
			}

			// We insert the new curve's quote ids list

			if (surface.getQuotes() != null && !surface.getQuotes().isEmpty()) {
				for (Quote quote : surface.getQuotes()) {
					stmtSaveRateQuotes.clearParameters();
					stmtSaveRateQuotes.setLong(1, surfaceId);
					stmtSaveRateQuotes.setLong(2, quote.getId());
					stmtSaveRateQuotes.addBatch();
				}
				stmtSaveRateQuotes.executeBatch();
			}

			if (surface.getId() != 0) {
				// Then, we delete the current surface's deltas list
				stmtDeleteDeltasBySurfaceId.setLong(1, surface.getId());
				stmtDeleteDeltasBySurfaceId.executeUpdate();
			}

			// We insert the new surface's deltas list
			for (BigDecimal delta : surface.getDeltas()) {
				stmtSaveDeltas.clearParameters();
				stmtSaveDeltas.setLong(1, surfaceId);
				stmtSaveDeltas.setBigDecimal(2, delta);
				stmtSaveDeltas.addBatch();
			}
			stmtSaveDeltas.executeBatch();

			// Then, we delete the data for this surface

			if (surface.getId() != 0) {
				stmtDeleteSurfacePoints.setLong(1, surface.getId());
				stmtDeleteSurfacePoints.execute();
			}

			for (SurfacePoint<Integer, BigDecimal, BigDecimal> point : surface.getPoints()) {

				if (point != null && point.getzAxis() != null) {
					stmtSaveSurfacePoints.clearParameters();
					stmtSaveSurfacePoints.setLong(1, surfaceId);
					stmtSaveSurfacePoints.setInt(2, point.getxAxis());
					stmtSaveSurfacePoints.setBigDecimal(3, point.getyAxis());
					stmtSaveSurfacePoints.setBigDecimal(4, point.getzAxis());
					stmtSaveSurfacePoints.addBatch();
				}
			}

			stmtSaveSurfacePoints.executeBatch();
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		surface.setId(surfaceId);
		return surfaceId;
	}

	public static List<SurfacePoint<Integer, BigDecimal, BigDecimal>> getFXVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId) {

		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> points = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(
						"SELECT FX_VOLATILITY_SURFACE_POINT.OPTION_EXPIRY, FX_VOLATILITY_SURFACE_POINT.STRIKE, FX_VOLATILITY_SURFACE_POINT.VOLATILITY "
								+ "FROM FX_VOLATILITY_SURFACE_POINT, VOLATILITY_SURFACE WHERE "
								+ "FX_VOLATILITY_SURFACE_POINT.VOLATILITY_SURFACE_ID = VOLATILITY_SURFACE.ID"
								+ " AND VOLATILITY_SURFACE.ID = ?");) {
			stmt.setLong(1, volatilitySurfaceId);
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					if (points == null) {
						points = new ArrayList<SurfacePoint<Integer, BigDecimal, BigDecimal>>();
					}
					Integer optionLt = results.getInt("option_expiry");
					BigDecimal strikeRes = results.getBigDecimal("strike");
					BigDecimal volatility = results.getBigDecimal("volatility");
					points.add(new SurfacePoint<Integer, BigDecimal, BigDecimal>(optionLt, strikeRes, volatility));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static BigDecimal getVolatilityBySurfaceNameOptionExpiry(String volatilitySurfaceName, int optionExpiry) {

		BigDecimal volatility = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetVolatilityBySurfaceNameAndOptionExpiry = con.prepareStatement(
						"SELECT AVG(VOLATILITY) VOLATILITY FROM VOLATILITY_SURFACE, FX_VOLATILITY_SURFACE_POINT"
								+ " WHERE VOLATILITY_SURFACE.ID = FX_VOLATILITY_SURFACE_POINT.VOLATILITY_SURFACE_ID"
								+ " AND NAME = ? AND OPTION_EXPIRY = ?")) {
			stmtGetVolatilityBySurfaceNameAndOptionExpiry.setString(1, volatilitySurfaceName);
			stmtGetVolatilityBySurfaceNameAndOptionExpiry.setLong(2, optionExpiry);
			try (ResultSet results = stmtGetVolatilityBySurfaceNameAndOptionExpiry.executeQuery()) {
				while (results.next()) {
					volatility = results.getBigDecimal("volatility");
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return volatility;
	}

}