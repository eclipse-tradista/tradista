package org.eclipse.tradista.security.equityoption.persistence;

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

	public static boolean saveEquityOptionVolatilitySurface(String surfaceName) {
		boolean bSaved = false;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveEquityVolatilitySurface = con
						.prepareStatement("INSERT INTO VOLATILITY_SURFACE(NAME, TYPE) VALUES(?, 'EquityOption') ")) {
			stmtSaveEquityVolatilitySurface.setString(1, surfaceName);
			stmtSaveEquityVolatilitySurface.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static EquityOptionVolatilitySurface getEquityOptionVolatilitySurfaceByName(String surfaceName) {
		EquityOptionVolatilitySurface equityOptionVolatilitySurface = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityVolatilitySurfaceByName = con
						.prepareStatement("SELECT * FROM VOLATILITY_SURFACE WHERE NAME = ?")) {
			stmtGetEquityVolatilitySurfaceByName.setString(1, surfaceName);
			try (ResultSet results = stmtGetEquityVolatilitySurfaceByName.executeQuery()) {
				while (results.next()) {
					long poId = results.getLong("processing_org_id");
					LegalEntity po = null;
					if (poId > 0) {
						po = LegalEntitySQL.getLegalEntityById(poId);
					}
					equityOptionVolatilitySurface = new EquityOptionVolatilitySurface(results.getString("name"), po);
					long id = results.getLong("id");
					equityOptionVolatilitySurface.setId(id);
					equityOptionVolatilitySurface.setAlgorithm(results.getString("algorithm"));
					equityOptionVolatilitySurface.setInterpolator(results.getString("interpolator"));
					equityOptionVolatilitySurface.setInstance(results.getString("instance"));
					java.sql.Date quoteDate = results.getDate("quote_date");
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
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptionVolatilitySurface;
	}

	public static EquityOptionVolatilitySurface getEquityOptionVolatilitySurfaceById(long surfaceId) {
		EquityOptionVolatilitySurface equityOptionVolatilitySurface = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityVolatilitySurfaceById = con
						.prepareStatement("SELECT * FROM VOLATILITY_SURFACE WHERE ID = ?")) {
			stmtGetEquityVolatilitySurfaceById.setLong(1, surfaceId);
			try (ResultSet results = stmtGetEquityVolatilitySurfaceById.executeQuery()) {
				while (results.next()) {
					long poId = results.getLong("processing_org_id");
					LegalEntity po = null;
					if (poId > 0) {
						po = LegalEntitySQL.getLegalEntityById(poId);
					}
					equityOptionVolatilitySurface = new EquityOptionVolatilitySurface(results.getString("name"), po);
					long id = results.getLong("id");
					equityOptionVolatilitySurface.setId(id);
					equityOptionVolatilitySurface.setAlgorithm(results.getString("algorithm"));
					equityOptionVolatilitySurface.setInterpolator(results.getString("interpolator"));
					equityOptionVolatilitySurface.setInstance(results.getString("instance"));
					java.sql.Date quoteDate = results.getDate("quote_date");
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
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptionVolatilitySurface;
	}

	public static boolean deleteEquityOptionVolatilitySurface(long surfaceId) {
		boolean bSaved = false;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteSurfacePoints = con.prepareStatement(
						"DELETE FROM EQUITY_OPTION_VOLATILITY_SURFACE_POINT WHERE VOLATILITY_SURFACE_ID = ?");
				PreparedStatement stmtDeleteStrikesBySurfaceName = con
						.prepareStatement("DELETE FROM EQUITY_OPTION_VOLATILITY_SURFACE_STRIKE WHERE SURFACE_ID = ?");
				PreparedStatement stmtDeleteQuotesBySurfaceName = con
						.prepareStatement("DELETE FROM VOLATILITY_SURFACE_QUOTE WHERE SURFACE_ID = ?");
				PreparedStatement stmtDeleteEquityVolatilitySurface = con
						.prepareStatement("DELETE FROM VOLATILITY_SURFACE WHERE ID = ?")) {

			stmtDeleteSurfacePoints.setLong(1, surfaceId);
			stmtDeleteSurfacePoints.executeUpdate();

			stmtDeleteStrikesBySurfaceName.setLong(1, surfaceId);
			stmtDeleteStrikesBySurfaceName.executeUpdate();

			stmtDeleteQuotesBySurfaceName.setLong(1, surfaceId);
			stmtDeleteQuotesBySurfaceName.executeUpdate();

			stmtDeleteEquityVolatilitySurface.setLong(1, surfaceId);
			stmtDeleteEquityVolatilitySurface.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<EquityOptionVolatilitySurface> getAllEquityOptionVolatilitySurfaces() {
		Set<EquityOptionVolatilitySurface> equityOptionVolatilitySurfaces = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllEquityVolatilitySurfaces = con
						.prepareStatement("SELECT * FROM VOLATILITY_SURFACE WHERE TYPE = 'EquityOption'");
				ResultSet results = stmtGetAllEquityVolatilitySurfaces.executeQuery();
				PreparedStatement stmtGetStrikesBySurfaceId = con.prepareStatement(
						"SELECT * FROM EQUITY_OPTION_VOLATILITY_SURFACE_STRIKE WHERE SURFACE_ID = ?")) {
			while (results.next()) {
				long poId = results.getLong("processing_org_id");
				LegalEntity po = null;
				if (poId > 0) {
					po = LegalEntitySQL.getLegalEntityById(poId);
				}
				EquityOptionVolatilitySurface equityOptionVolatilitySurface = new EquityOptionVolatilitySurface(
						results.getString("name"), po);
				long id = results.getLong("id");
				equityOptionVolatilitySurface.setId(id);
				equityOptionVolatilitySurface.setAlgorithm(results.getString("algorithm"));
				equityOptionVolatilitySurface.setInterpolator(results.getString("interpolator"));
				equityOptionVolatilitySurface.setInstance(results.getString("instance"));
				java.sql.Date quoteDate = results.getDate("quote_date");
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

				// Get the deltas linked to this surface
				List<BigDecimal> strikes = new ArrayList<BigDecimal>();
				ResultSet strikesResults = null;

				stmtGetStrikesBySurfaceId.setLong(1, id);
				strikesResults = stmtGetStrikesBySurfaceId.executeQuery();

				while (strikesResults.next()) {
					strikes.add(strikesResults.getBigDecimal("value"));
				}

				equityOptionVolatilitySurface.setStrikes(strikes);
				if (equityOptionVolatilitySurfaces == null) {
					equityOptionVolatilitySurfaces = new HashSet<EquityOptionVolatilitySurface>();
				}
				equityOptionVolatilitySurfaces.add(equityOptionVolatilitySurface);
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptionVolatilitySurfaces;
	}

	public static boolean saveEquityOptionVolatilitySurfacePoints(long id,
			List<SurfacePoint<Long, BigDecimal, BigDecimal>> surfacePoints, Long optionExpiry, BigDecimal strike) {
		boolean bSaved = false;

		try (Connection con = TradistaDB.getConnection();
				Statement stmt = con.createStatement();
				PreparedStatement stmtSaveSurfacePoints = con
						.prepareStatement("INSERT INTO EQUITY_OPTION_VOLATILITY_SURFACE_POINT VALUES(?,?,?,?) ")) {

			// First, we delete the data for this surface, option and swap
			// lifetimes

			String delete = "DELETE FROM EQUITY_OPTION_VOLATILITY_SURFACE_POINT" + " WHERE SURFACE_ID =  " + id;
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
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return bSaved;
	}

	public static BigDecimal getVolatilityBySurfaceNameOptionExpiryAndStrike(String surfaceName, long optionExpiry,
			double strike) {
		BigDecimal volatility = null;

		try (Connection con = TradistaDB.getConnection();
				Statement stmt = con.createStatement();
				PreparedStatement stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike = con.prepareStatement(
						"SELECT VOLATILITY FROM VOLATILITY_SURFACE, EQUITY_OPTION_VOLATILITY_SURFACE_POINT"
								+ " WHERE VOLATILITY_SURFACE.ID = EQUITY_OPTION_VOLATILITY_SURFACE_POINT.VOLATILITY_SURFACE_ID"
								+ " AND NAME = ?" + " AND OPTION_EXPIRY = ?" + " AND STRIKE = ?")) {
			stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike.setString(1, surfaceName);
			stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike.setLong(2, optionExpiry);
			stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike.setDouble(3, strike);
			try (ResultSet results = stmtGetVolatilityBySurfaceNameOptionExpiryAndStrike.executeQuery()) {

				while (results.next()) {
					volatility = results.getBigDecimal("volatility");

				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return volatility;
	}

	public static List<SurfacePoint<Number, Number, Number>> getEquityOptionVolatilitySurfacePointsBySurfaceIdOptionExpiryAndStrike(
			long volatilitySurfaceId, long optionExpiry, BigDecimal strike) {
		List<SurfacePoint<Number, Number, Number>> points = null;

		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {

			String query = "SELECT EQUITY_OPTION_VOLATILITY_SURFACE_POINT.OPTION_EXPIRY, EQUITY_OPTION_VOLATILITY_SURFACE_POINT.STRIKE, EQUITY_OPTION_VOLATILITY_SURFACE_POINT.VOLATILITY "
					+ "FROM EQUITY_OPTION_VOLATILITY_SURFACE_POINT, SURFACE WHERE "
					+ "EQUITY_OPTION_VOLATILITY_SURFACE_POINT.SURFACE_ID = SURFACE.ID";
			if (optionExpiry != 0) {
				query += " AND OPTION_EXPIRY = " + optionExpiry;
			}

			if (strike != null) {
				query += " AND STRIKE = " + strike;
			}
			try (ResultSet results = stmt.executeQuery(query)) {
				while (results.next()) {
					Long optionLt = results.getLong("option_expiry");
					BigDecimal strikeRes = results.getBigDecimal("strike");
					BigDecimal volatility = results.getBigDecimal("volatility");
					if (points == null) {
						points = new ArrayList<SurfacePoint<Number, Number, Number>>();
					}
					points.add(new SurfacePoint<Number, Number, Number>(optionLt, strikeRes, volatility));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static long saveEquityOptionVolatilitySurface(EquityOptionVolatilitySurface surface) {
		long surfaceId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveEquityVolatilitySurface = (surface.getId() != 0) ? con.prepareStatement(
						"UPDATE VOLATILITY_SURFACE SET NAME=?, ALGORITHM =?, INTERPOLATOR=?, INSTANCE=?, QUOTE_DATE=?, TYPE=?, QUOTE_SET_ID=?, PROCESSING_ORG_ID=? WHERE ID = ?")
						: con.prepareStatement(
								"INSERT INTO VOLATILITY_SURFACE(NAME, ALGORITHM, INTERPOLATOR, INSTANCE, QUOTE_DATE, TYPE, QUOTE_SET_ID, PROCESSING_ORG_ID) VALUES (?,?,?,?,?,?,?,?)",
								Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtSaveRateQuotes = con
						.prepareStatement("INSERT INTO VOLATILITY_SURFACE_QUOTE VALUES(?,?) ");
				PreparedStatement stmtSaveStrikes = con.prepareStatement(
						"INSERT INTO EQUITY_OPTION_VOLATILITY_SURFACE_STRIKE(SURFACE_ID, VALUE) VALUES(?,?) ");
				PreparedStatement stmtSaveSurfacePoints = con
						.prepareStatement("INSERT INTO EQUITY_OPTION_VOLATILITY_SURFACE_POINT VALUES(?,?,?,?) ")) {
			// First, we save the Swaption Volatility Surface definition

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

			if (surface.getId() != 0) {
				// Then, we delete the current curve's quote ids list.
				try (PreparedStatement stmtDeleteQuotesBySurfaceId = con
						.prepareStatement("DELETE FROM VOLATILITY_SURFACE_QUOTE WHERE SURFACE_ID = ? ")) {
					stmtDeleteQuotesBySurfaceId.setLong(1, surface.getId());
					stmtDeleteQuotesBySurfaceId.executeUpdate();
				}
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
				try (PreparedStatement stmtDeleteStrikesBySurfaceId = con.prepareStatement(
						"DELETE FROM EQUITY_OPTION_VOLATILITY_SURFACE_STRIKE WHERE SURFACE_ID = ? ")) {
					stmtDeleteStrikesBySurfaceId.setLong(1, surface.getId());
					stmtDeleteStrikesBySurfaceId.executeUpdate();
				}
			}

			// We insert the new surface's deltas list
			for (BigDecimal strike : surface.getStrikes()) {
				stmtSaveStrikes.clearParameters();
				stmtSaveStrikes.setLong(1, surfaceId);
				stmtSaveStrikes.setBigDecimal(2, strike);
				stmtSaveStrikes.addBatch();
			}
			stmtSaveStrikes.executeBatch();

			if (surface.getId() != 0) {
				try (PreparedStatement stmtDeletePointsBySurfaceId = con.prepareStatement(
						"DELETE FROM EQUITY_OPTION_VOLATILITY_SURFACE_POINT WHERE VOLATILITY_SURFACE_ID = ?")) {
					// Then, we delete the data for this surface
					stmtDeletePointsBySurfaceId.setLong(1, surface.getId());
					stmtDeletePointsBySurfaceId.executeUpdate();
				}
			}

			for (SurfacePoint<Integer, BigDecimal, BigDecimal> point : surface.getPoints()) {
				if (point != null && point.getzAxis() != null) {
					stmtSaveSurfacePoints.clearParameters();
					stmtSaveSurfacePoints.setLong(1, surfaceId);
					stmtSaveSurfacePoints.setLong(2, point.getxAxis());
					stmtSaveSurfacePoints.setBigDecimal(3, point.getyAxis());
					stmtSaveSurfacePoints.setBigDecimal(4, point.getzAxis());
					stmtSaveSurfacePoints.addBatch();
				}
			}

			stmtSaveSurfacePoints.executeBatch();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		surface.setId(surfaceId);
		return surfaceId;
	}

	public static List<SurfacePoint<Integer, BigDecimal, BigDecimal>> getEquityOptionVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId) {
		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> points = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityOptionVolatilitySurfacePointsBySurfaceId = con.prepareStatement(
						"SELECT EQUITY_OPTION_VOLATILITY_SURFACE_POINT.OPTION_EXPIRY, EQUITY_OPTION_VOLATILITY_SURFACE_POINT.STRIKE, EQUITY_OPTION_VOLATILITY_SURFACE_POINT.VOLATILITY "
								+ "FROM EQUITY_OPTION_VOLATILITY_SURFACE_POINT, VOLATILITY_SURFACE WHERE "
								+ "EQUITY_OPTION_VOLATILITY_SURFACE_POINT.VOLATILITY_SURFACE_ID = VOLATILITY_SURFACE.ID"
								+ " AND VOLATILITY_SURFACE.ID = ?")) {
			stmtGetEquityOptionVolatilitySurfacePointsBySurfaceId.setLong(1, volatilitySurfaceId);
			try (ResultSet results = stmtGetEquityOptionVolatilitySurfacePointsBySurfaceId.executeQuery()) {
				while (results.next()) {
					Integer optionExpiry = results.getInt("option_expiry");
					BigDecimal strikeRes = results.getBigDecimal("strike");
					BigDecimal volatility = results.getBigDecimal("volatility");
					if (points == null) {
						points = new ArrayList<SurfacePoint<Integer, BigDecimal, BigDecimal>>();
					}
					points.add(new SurfacePoint<Integer, BigDecimal, BigDecimal>(optionExpiry, strikeRes, volatility));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static BigDecimal getVolatility(String volatilitySurfaceName, long optionExpiry) {
		BigDecimal volatility = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetVolatilityBySurfaceNameAndOptionExpiry = con.prepareStatement(
						"SELECT AVG(VOLATILITY) FROM VOLATILITY_SURFACE, EQUITY_OPTION_VOLATILITY_SURFACE_POINT"
								+ " WHERE VOLATILITY_SURFACE.ID = EQUITY_OPTION_VOLATILITY_SURFACE_POINT.VOLATILITY_SURFACE_ID"
								+ " AND NAME = ? AND OPTION_EXPIRY = ?")) {
			stmtGetVolatilityBySurfaceNameAndOptionExpiry.setString(1, volatilitySurfaceName);
			stmtGetVolatilityBySurfaceNameAndOptionExpiry.setLong(2, optionExpiry);
			try (ResultSet results = stmtGetVolatilityBySurfaceNameAndOptionExpiry.executeQuery()) {
				while (results.next()) {
					volatility = results.getBigDecimal("volatility");
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return volatility;
	}

}