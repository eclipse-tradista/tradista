package org.eclipse.tradista.ir.irswapoption.persistence;

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
import org.eclipse.tradista.core.marketdata.persistence.QuoteSetSQL;
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

	public static boolean saveSwaptionVolatilitySurface(String surfaceName) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveSwaptionVolatilitySurface = con
						.prepareStatement("INSERT INTO VOLATILITY_SURFACE(NAME, TYPE) VALUES(?, 'IRSwapOption') ")) {
			stmtSaveSwaptionVolatilitySurface.setString(1, surfaceName);
			stmtSaveSwaptionVolatilitySurface.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static boolean deleteSwaptionVolatilitySurface(long surfaceId) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteSurfacePoints = con.prepareStatement(
						"DELETE FROM SWAPTION_VOLATILITY_SURFACE_POINT WHERE VOLATILITY_SURFACE_ID = ?");
				PreparedStatement stmtDeleteQuotesBySurfaceName = con
						.prepareStatement("DELETE FROM VOLATILITY_SURFACE_QUOTE WHERE SURFACE_ID = ?");
				PreparedStatement stmtDeleteSwaptionVolatilitySurface = con
						.prepareStatement("DELETE FROM VOLATILITY_SURFACE WHERE ID = ? ");) {
			stmtDeleteSurfacePoints.setLong(1, surfaceId);
			stmtDeleteSurfacePoints.executeUpdate();
			stmtDeleteQuotesBySurfaceName.setLong(1, surfaceId);
			stmtDeleteQuotesBySurfaceName.executeUpdate();
			stmtDeleteSwaptionVolatilitySurface.setLong(1, surfaceId);
			stmtDeleteSwaptionVolatilitySurface.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<SwaptionVolatilitySurface> getAllSwaptionVolatilitySurfaces() {
		Set<SwaptionVolatilitySurface> swaptionVolatilitySurfaces = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllSwaptionVolatilitySurfaces = con
						.prepareStatement("SELECT * FROM VOLATILITY_SURFACE WHERE TYPE = 'IRSwapOption'");
				ResultSet results = stmtGetAllSwaptionVolatilitySurfaces.executeQuery()) {
			while (results.next()) {
				if (swaptionVolatilitySurfaces == null) {
					swaptionVolatilitySurfaces = new HashSet<SwaptionVolatilitySurface>();
				}
				long poId = results.getLong("processing_org_id");
				LegalEntity po = null;
				if (poId > 0) {
					po = LegalEntitySQL.getLegalEntityById(poId);
				}
				SwaptionVolatilitySurface swaptionVolatilitySurface = new SwaptionVolatilitySurface(
						results.getString("name"), po);
				long id = results.getLong("id");
				swaptionVolatilitySurface.setId(id);
				swaptionVolatilitySurface.setAlgorithm(results.getString("algorithm"));
				swaptionVolatilitySurface.setInterpolator(results.getString("interpolator"));
				swaptionVolatilitySurface.setInstance(results.getString("instance"));
				java.sql.Date quoteDate = results.getDate("quote_date");
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
				swaptionVolatilitySurface.setQuoteSet(QuoteSetSQL.getQuoteSetById(results.getLong("quote_set_id")));
				swaptionVolatilitySurfaces.add(swaptionVolatilitySurface);
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return swaptionVolatilitySurfaces;
	}

	public static SwaptionVolatilitySurface getSwaptionVolatilitySurfaceByName(String surfaceName) {
		SwaptionVolatilitySurface swaptionVolatilitySurface = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetSwaptionVolatilitySurfaceByName = con
						.prepareStatement("SELECT * FROM VOLATILITY_SURFACE WHERE NAME = ?")) {
			stmtGetSwaptionVolatilitySurfaceByName.setString(1, surfaceName);
			try (ResultSet results = stmtGetSwaptionVolatilitySurfaceByName.executeQuery()) {
				while (results.next()) {
					long poId = results.getLong("processing_org_id");
					LegalEntity po = null;
					if (poId > 0) {
						po = LegalEntitySQL.getLegalEntityById(poId);
					}
					swaptionVolatilitySurface = new SwaptionVolatilitySurface(results.getString("name"), po);
					long id = results.getLong("id");
					swaptionVolatilitySurface.setId(id);
					swaptionVolatilitySurface.setAlgorithm(results.getString("algorithm"));
					swaptionVolatilitySurface.setInterpolator(results.getString("interpolator"));
					swaptionVolatilitySurface.setInstance(results.getString("instance"));
					java.sql.Date quoteDate = results.getDate("quote_date");
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
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return swaptionVolatilitySurface;
	}

	public static SwaptionVolatilitySurface getSwaptionVolatilitySurfaceById(long surfaceId) {
		SwaptionVolatilitySurface swaptionVolatilitySurface = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetSwaptionVolatilitySurfaceById = con
						.prepareStatement("SELECT * FROM VOLATILITY_SURFACE WHERE ID = ?")) {
			stmtGetSwaptionVolatilitySurfaceById.setLong(1, surfaceId);
			try (ResultSet results = stmtGetSwaptionVolatilitySurfaceById.executeQuery()) {
				while (results.next()) {
					long poId = results.getLong("processing_org_id");
					LegalEntity po = null;
					if (poId > 0) {
						po = LegalEntitySQL.getLegalEntityById(poId);
					}
					swaptionVolatilitySurface = new SwaptionVolatilitySurface(results.getString("name"), po);
					long id = results.getLong("id");
					swaptionVolatilitySurface.setId(id);
					swaptionVolatilitySurface.setAlgorithm(results.getString("algorithm"));
					swaptionVolatilitySurface.setInterpolator(results.getString("interpolator"));
					swaptionVolatilitySurface.setInstance(results.getString("instance"));
					java.sql.Date quoteDate = results.getDate("quote_date");
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
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return swaptionVolatilitySurface;
	}

	public static boolean saveSwaptionVolatilitySurfacePoints(long id,
			List<SurfacePoint<Float, Float, Float>> surfacePoints, Float optionLifeTime, Float swapLifetime) {
		boolean bSaved = false;
		String delete = "DELETE FROM SWAPTION_VOLATILITY_SURFACE_POINT WHERE VOLATILITY_SURFACE_ID =  " + id;
		if (optionLifeTime != null) {
			delete += " AND SWAPTION_LIFETIME = " + optionLifeTime;
		}
		if (swapLifetime != null) {
			delete += " AND SWAP_LIFETIME = " + swapLifetime;
		}
		try (Connection con = TradistaDB.getConnection();
				Statement stmt = con.createStatement();
				PreparedStatement stmtSaveSurfacePoints = con
						.prepareStatement("INSERT INTO SWAPTION_VOLATILITY_SURFACE_POINT VALUES(?,?,?,?) ")) {

			// First, we delete the data for this surface, option and swap
			// lifetimes
			stmt.executeUpdate(delete);

			for (SurfacePoint<Float, Float, Float> point : surfacePoints) {

				if (point != null && point.getzAxis() != null) {
					stmtSaveSurfacePoints.clearParameters();
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
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;

	}

	public static BigDecimal getVolatilityBySurfaceNameTimeToMaturityAndTenor(String surfaceName, int timeToMaturity,
			int tenor) {

		BigDecimal volatility = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetVolatilityBySurfaceNameSwaptionAndSwapLifetimes = con
						.prepareStatement("SELECT VOLATILITY FROM VOLATILITY_SURFACE, SWAPTION_VOLATILITY_SURFACE_POINT"
								+ " WHERE VOLATILITY_SURFACE.ID = SWAPTION_VOLATILITY_SURFACE_POINT.VOLATILITY_SURFACE_ID"
								+ " AND NAME = ? AND SWAPTION_LIFETIME = ? AND SWAP_LIFETIME = ?")) {
			stmtGetVolatilityBySurfaceNameSwaptionAndSwapLifetimes.setString(1, surfaceName);
			stmtGetVolatilityBySurfaceNameSwaptionAndSwapLifetimes.setInt(2, timeToMaturity);
			stmtGetVolatilityBySurfaceNameSwaptionAndSwapLifetimes.setInt(3, tenor);
			try (ResultSet results = stmtGetVolatilityBySurfaceNameSwaptionAndSwapLifetimes.executeQuery()) {
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

	public static List<SurfacePoint<Number, Number, Number>> getSwaptionVolatilitySurfacePointsBySurfaceIdOptionAndSwapLifetimes(
			long currentSwaptionVolatilitySurfaceId, Float optionLifetime, Float swapLifetime) {
		List<SurfacePoint<Number, Number, Number>> points = null;
		String query = "SELECT SWAPTION_VOLATILITY_SURFACE_POINT.SWAPTION_LIFETIME, SWAPTION_VOLATILITY_SURFACE_POINT.SWAP_LIFETIME, SWAPTION_VOLATILITY_SURFACE_POINT.VOLATILITY "
				+ "FROM SWAPTION_VOLATILITY_SURFACE_POINT, VOLATILITY_SURFACE WHERE "
				+ "SWAPTION_VOLATILITY_SURFACE_POINT.VOLATILITY_SURFACE_ID = VOLATILITY_SURFACE.ID";
		if (optionLifetime != null) {
			query += " AND SWAPTION_LIFETIME = " + optionLifetime;
		}
		if (swapLifetime != null) {
			query += " AND SWAP_LIFETIME = " + swapLifetime;
		}
		try (Connection con = TradistaDB.getConnection();
				Statement stmt = con.createStatement();
				ResultSet results = stmt.executeQuery(query)) {
			while (results.next()) {
				if (points == null) {
					points = new ArrayList<SurfacePoint<Number, Number, Number>>();
				}
				Float optionLt = results.getFloat("swaption_lifetime");
				Float swapLt = results.getFloat("swap_lifetime");
				Float volatility = results.getFloat("volatility");
				points.add(new SurfacePoint<Number, Number, Number>(optionLt, swapLt, volatility));
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static long saveSwaptionVolatilitySurface(SwaptionVolatilitySurface surface) {
		long surfaceId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveSwaptionVolatilitySurface = (surface.getId() != 0) ? con.prepareStatement(
						"UPDATE VOLATILITY_SURFACE SET NAME = ?, ALGORITHM =?, INTERPOLATOR=?, INSTANCE=?, TYPE=?, QUOTE_DATE=?, QUOTE_SET_ID=?, PROCESSING_ORG_ID=? WHERE ID = ?")
						: con.prepareStatement(
								"INSERT INTO VOLATILITY_SURFACE(NAME, ALGORITHM, INTERPOLATOR, INSTANCE, TYPE, QUOTE_DATE, QUOTE_SET_ID, PROCESSING_ORG_ID) VALUES (?,?,?,?,?,?,?,?)",
								Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtSaveSurfacePoints = con
						.prepareStatement("INSERT INTO SWAPTION_VOLATILITY_SURFACE_POINT VALUES(?,?,?,?) ")) {
			// First, we save the Swaption Volatility Surface definition
			if (surface.getId() != 0) {
				stmtSaveSwaptionVolatilitySurface.setLong(9, surface.getId());
			}
			stmtSaveSwaptionVolatilitySurface.setString(1, surface.getName());
			stmtSaveSwaptionVolatilitySurface.setString(2, surface.getAlgorithm());
			stmtSaveSwaptionVolatilitySurface.setString(3, surface.getInterpolator());
			stmtSaveSwaptionVolatilitySurface.setString(4, surface.getInstance());
			stmtSaveSwaptionVolatilitySurface.setString(5, IRSwapOptionTrade.IR_SWAP_OPTION);
			if (surface.getQuoteDate() == null) {
				stmtSaveSwaptionVolatilitySurface.setNull(6, Types.DATE);
			} else {
				stmtSaveSwaptionVolatilitySurface.setDate(6, java.sql.Date.valueOf(surface.getQuoteDate()));
			}
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

			if (surface.getId() != 0) {
				// Then, we delete the current curve's quote ids list.
				try (PreparedStatement stmtDeleteQuotesBySurfaceId = con
						.prepareStatement("DELETE FROM VOLATILITY_SURFACE_QUOTE WHERE SURFACE_ID = ? ")) {
					stmtDeleteQuotesBySurfaceId.setLong(1, surface.getId());
					stmtDeleteQuotesBySurfaceId.executeUpdate();
				}
			}

			if (surface.getQuotes() != null && !surface.getQuotes().isEmpty()) {
				// We insert the new curve's quote ids list
				try (PreparedStatement stmtSaveRateQuotes = con
						.prepareStatement("INSERT INTO VOLATILITY_SURFACE_QUOTE VALUES(?,?) ")) {
					for (Quote quote : surface.getQuotes()) {
						stmtSaveRateQuotes.clearParameters();
						stmtSaveRateQuotes.setLong(1, surfaceId);
						stmtSaveRateQuotes.setLong(2, quote.getId());
						stmtSaveRateQuotes.addBatch();
					}
					stmtSaveRateQuotes.executeBatch();
				}
			}

			if (surface.getId() != 0) {
				// Then, we delete the data for this surface
				try (PreparedStatement stmtDeleteSurfacePoints = con.prepareStatement(
						"DELETE FROM SWAPTION_VOLATILITY_SURFACE_POINT WHERE VOLATILITY_SURFACE_ID =  ?")) {
					stmtDeleteSurfacePoints.setLong(1, surface.getId());
					stmtDeleteSurfacePoints.executeUpdate();
				}
			}

			if (surface.getPoints() != null && !surface.getPoints().isEmpty()) {
				for (SurfacePoint<Integer, Integer, BigDecimal> point : surface.getPoints()) {
					if (point != null && point.getzAxis() != null) {
						stmtSaveSurfacePoints.clearParameters();
						stmtSaveSurfacePoints.setLong(1, surfaceId);
						stmtSaveSurfacePoints.setLong(2, point.getxAxis());
						stmtSaveSurfacePoints.setLong(3, point.getyAxis());
						stmtSaveSurfacePoints.setBigDecimal(4, point.getzAxis());
						stmtSaveSurfacePoints.addBatch();
					}
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

	public static List<SurfacePoint<Integer, Integer, BigDecimal>> getSwaptionVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId) {

		List<SurfacePoint<Integer, Integer, BigDecimal>> points = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetSwaptionVolatilitySurfacePointsBySurfaceId = con.prepareStatement(
						"SELECT SWAPTION_VOLATILITY_SURFACE_POINT.SWAPTION_LIFETIME, SWAPTION_VOLATILITY_SURFACE_POINT.SWAP_LIFETIME, SWAPTION_VOLATILITY_SURFACE_POINT.VOLATILITY "
								+ "FROM SWAPTION_VOLATILITY_SURFACE_POINT, VOLATILITY_SURFACE WHERE "
								+ "SWAPTION_VOLATILITY_SURFACE_POINT.VOLATILITY_SURFACE_ID = VOLATILITY_SURFACE.ID"
								+ " AND VOLATILITY_SURFACE.ID = ?")) {
			stmtGetSwaptionVolatilitySurfacePointsBySurfaceId.setLong(1, volatilitySurfaceId);
			try (ResultSet results = stmtGetSwaptionVolatilitySurfacePointsBySurfaceId.executeQuery()) {
				while (results.next()) {
					if (points == null) {
						points = new ArrayList<SurfacePoint<Integer, Integer, BigDecimal>>();
					}
					Integer optionLt = results.getInt("swaption_lifetime");
					Integer swapLt = results.getInt("swap_lifetime");
					BigDecimal volatility = results.getBigDecimal("volatility");
					points.add(new SurfacePoint<Integer, Integer, BigDecimal>(optionLt, swapLt, volatility));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

}