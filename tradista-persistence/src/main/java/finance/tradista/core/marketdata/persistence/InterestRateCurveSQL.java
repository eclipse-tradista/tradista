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
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.model.ZeroCouponCurve;

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

public class InterestRateCurveSQL {

	public static long saveInterestRateCurve(String curveName, String curveType) {
		long curveId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveCurve = con.prepareStatement("INSERT INTO CURVE(NAME) VALUES(?)",
						Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtSaveInterestRateCurve = con
						.prepareStatement("INSERT INTO INTEREST_RATE_CURVE(ID, TYPE) VALUES(?, ?)")) {
			stmtSaveCurve.setString(1, curveName);
			// stmtSaveRateCurve.setString(2, curveType);
			stmtSaveCurve.executeUpdate();
			try (ResultSet generatedKeys = stmtSaveCurve.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					curveId = generatedKeys.getLong(1);
				} else {
					throw new SQLException("Creating Curve failed, no generated key obtained.");
				}
			}
			stmtSaveInterestRateCurve.setLong(1, curveId);
			stmtSaveInterestRateCurve.setString(2, curveType);
			stmtSaveInterestRateCurve.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return curveId;
	}

	public static boolean deleteInterestRateCurve(long curveId) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteRatePoints = con
						.prepareStatement("DELETE FROM CURVE_POINT WHERE CURVE_ID = ? ");
				PreparedStatement stmtDeleteQuotesByCurveName = con
						.prepareStatement("DELETE FROM CURVE_QUOTE WHERE CURVE_ID = ? ");
				PreparedStatement stmtDeleteInterestRateCurve = con
						.prepareStatement("DELETE FROM INTEREST_RATE_CURVE WHERE ID = ? ");
				PreparedStatement stmtDeleteCurve = con.prepareStatement("DELETE FROM CURVE WHERE ID = ? ")) {
			stmtDeleteRatePoints.setLong(1, curveId);
			stmtDeleteRatePoints.executeUpdate();
			stmtDeleteQuotesByCurveName.setLong(1, curveId);
			stmtDeleteQuotesByCurveName.executeUpdate();
			stmtDeleteInterestRateCurve.setLong(1, curveId);
			stmtDeleteInterestRateCurve.executeUpdate();
			stmtDeleteCurve.setLong(1, curveId);
			stmtDeleteCurve.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<InterestRateCurve> getAllInterestRateCurves() {
		Set<InterestRateCurve> interestRateCurves = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllInterestRateCurves = con.prepareStatement(
						"SELECT * FROM CURVE, INTEREST_RATE_CURVE WHERE CURVE.ID = INTEREST_RATE_CURVE.ID");
				ResultSet results = stmtGetAllInterestRateCurves.executeQuery()) {
			while (results.next()) {
				if (interestRateCurves == null) {
					interestRateCurves = new HashSet<InterestRateCurve>();
				}
				boolean isZeroCoupon = results.getString("type") != null
						&& results.getString("type").equals("ZeroCouponCurve");

				InterestRateCurve interestRateCurve;
				LegalEntity processingOrg = null;
				long poId = results.getLong("processing_org_id");
				if (poId > 0) {
					processingOrg = LegalEntitySQL.getLegalEntityById(poId);
				}
				if (isZeroCoupon) {
					interestRateCurve = new ZeroCouponCurve(results.getString("name"), processingOrg);
				} else {
					interestRateCurve = new InterestRateCurve(results.getString("name"), processingOrg);
				}

				interestRateCurve.setId(results.getLong("id"));
				interestRateCurve.setAlgorithm(results.getString("algorithm"));
				interestRateCurve.setInterpolator(results.getString("interpolator"));
				interestRateCurve.setInstance(results.getString("instance"));
				java.sql.Date quoteDate = results.getDate("quote_date");
				if (quoteDate != null) {
					interestRateCurve.setQuoteDate(quoteDate.toLocalDate());
				}
				interestRateCurves.add(interestRateCurve);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return interestRateCurves;
	}

	public static Set<ZeroCouponCurve> getAllZeroCouponCurves() {
		Set<ZeroCouponCurve> zeroCouponCurves = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllZeroCouponCurves = con.prepareStatement(
						"SELECT * FROM CURVE, INTEREST_RATE_CURVE WHERE CURVE.ID = INTEREST_RATE_CURVE.ID AND TYPE = 'ZeroCouponCurve'");
				ResultSet results = stmtGetAllZeroCouponCurves.executeQuery()) {
			while (results.next()) {
				if (zeroCouponCurves == null) {
					zeroCouponCurves = new HashSet<ZeroCouponCurve>();
				}
				LegalEntity processingOrg = null;
				long poId = results.getLong("processing_org_id");
				if (poId > 0) {
					processingOrg = LegalEntitySQL.getLegalEntityById(poId);
				}
				ZeroCouponCurve zeroCouponCurve = new ZeroCouponCurve(results.getString("name"), processingOrg);
				zeroCouponCurve.setId(results.getLong("id"));
				zeroCouponCurve.setAlgorithm(results.getString("algorithm"));
				zeroCouponCurve.setInterpolator(results.getString("interpolator"));
				zeroCouponCurve.setInstance(results.getString("instance"));
				java.sql.Date quoteDate = results.getDate("quote_date");
				if (quoteDate != null) {
					zeroCouponCurve.setQuoteDate(quoteDate.toLocalDate());
				}
				zeroCouponCurves.add(zeroCouponCurve);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return zeroCouponCurves;
	}

	public static List<RatePoint> getAllInterestRateCurvePointsByCurveIdAndDate(long curveId, Year year, Month month) {
		List<RatePoint> points = null;
		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {

			LocalDate startDate = LocalDate.of(year.getValue(), month, 1);
			LocalDate endDate = startDate.plus(1, ChronoUnit.MONTHS);

			String query = "SELECT DATE, RATE " + "FROM CURVE_POINT WHERE CURVE_ID = " + curveId + " AND DATE >= '"
					+ startDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + "'" + " AND DATE < '"
					+ endDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + "'";

			try (ResultSet results = stmt.executeQuery(query)) {

				while (results.next()) {
					if (points == null) {
						points = new ArrayList<RatePoint>();
					}
					LocalDate date = results.getDate("date").toLocalDate();
					BigDecimal rate = results.getBigDecimal("rate");
					points.add(new RatePoint(date, rate));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static List<RatePoint> getInterestRateCurvePointsByCurveIdAndDates(long curveId, LocalDate min,
			LocalDate max) {
		List<RatePoint> points = null;
		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {
			String query = "SELECT DATE, RATE " + "FROM CURVE_POINT, INTEREST_RATE_CURVE WHERE "
					+ "CURVE_POINT.CURVE_ID = INTEREST_RATE_CURVE.ID AND INTEREST_RATE_CURVE.ID = " + curveId + ""
					+ " AND DATE >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(min) + "'" + " AND DATE <= '"
					+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(max) + "'";
			try (ResultSet results = stmt.executeQuery(query)) {
				while (results.next()) {
					if (points == null) {
						points = new ArrayList<RatePoint>();
					}
					LocalDate date = results.getDate("date").toLocalDate();
					BigDecimal rate = results.getBigDecimal("rate");
					points.add(new RatePoint(date, rate));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static boolean saveInterestRateCurvePoints(long id, List<RatePoint> ratePoints, Year year, Month month) {
		boolean bSaved = false;
		// First, we delete the data for this curve and this month

		LocalDate startDate = LocalDate.of(year.getValue(), month, 1);
		LocalDate endDate = startDate.plus(1, ChronoUnit.MONTHS);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteRatePointsByCurveIdYearAndMonth = con.prepareStatement(
						"DELETE FROM CURVE_POINT WHERE CURVE_ID = ? " + " AND DATE  >= ? AND DATE < ? ");
				PreparedStatement stmtSaveRatePoints = con.prepareStatement("INSERT INTO CURVE_POINT VALUES(?,?,?) ")) {

			stmtDeleteRatePointsByCurveIdYearAndMonth.setLong(1, id);
			stmtDeleteRatePointsByCurveIdYearAndMonth.setDate(2, java.sql.Date.valueOf(startDate));
			stmtDeleteRatePointsByCurveIdYearAndMonth.setDate(3, java.sql.Date.valueOf(endDate));

			stmtDeleteRatePointsByCurveIdYearAndMonth.executeUpdate();
			for (RatePoint point : ratePoints) {
				if (point != null && point.getRate() != null) {
					stmtSaveRatePoints.clearParameters();
					stmtSaveRatePoints.setLong(1, id);
					stmtSaveRatePoints.setDate(2, java.sql.Date.valueOf(point.getDate()));
					stmtSaveRatePoints.setBigDecimal(3, point.getRate());
					stmtSaveRatePoints.addBatch();
				}
				bSaved = true;
			}
			stmtSaveRatePoints.executeBatch();
		}

		catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return bSaved;

	}

	public static long saveInterestRateCurve(InterestRateCurve curve) {
		long curveId = 0;
		// First, we save the IR Curve definition
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveCurve = (curve.getId() != 0) ? con.prepareStatement(
						"UPDATE CURVE SET NAME=?, ALGORITHM =?, INTERPOLATOR=?, INSTANCE=?, QUOTE_DATE=?, QUOTE_SET_ID=?, PROCESSING_ORG_ID=? WHERE ID = ?")
						: con.prepareStatement(
								"INSERT INTO CURVE(NAME, ALGORITHM, INTERPOLATOR, INSTANCE, QUOTE_DATE, QUOTE_SET_ID, PROCESSING_ORG_ID) VALUES (?,?,?,?,?,?,?)",
								Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtSaveInterestRateCurve = (curve.getId() != 0)
						? con.prepareStatement("UPDATE INTEREST_RATE_CURVE SET TYPE=? WHERE ID = ?")
						: con.prepareStatement("INSERT INTO INTEREST_RATE_CURVE(TYPE, ID) VALUES (?, ?)",
								Statement.RETURN_GENERATED_KEYS)) {
			if (curve.getId() != 0) {
				stmtSaveCurve.setLong(8, curve.getId());
			}
			stmtSaveCurve.setString(1, curve.getName());
			stmtSaveCurve.setString(2, curve.getAlgorithm());
			stmtSaveCurve.setString(3, curve.getInterpolator());
			stmtSaveCurve.setString(4, curve.getInstance());
			if (curve.getQuoteDate() != null) {
				stmtSaveCurve.setDate(5, java.sql.Date.valueOf(curve.getQuoteDate()));
			} else {
				stmtSaveCurve.setNull(5, Types.DATE);
			}
			if (curve.getQuoteSet() == null) {
				stmtSaveCurve.setNull(6, Types.BIGINT);
			} else {
				stmtSaveCurve.setLong(6, curve.getQuoteSet().getId());
			}

			if (curve.getProcessingOrg() == null) {
				stmtSaveCurve.setNull(7, Types.BIGINT);
			} else {
				stmtSaveCurve.setLong(7, curve.getProcessingOrg().getId());
			}

			stmtSaveCurve.executeUpdate();

			if (curve.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveCurve.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						curveId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating surface failed, no generated key obtained.");
					}
				}
			} else {
				curveId = curve.getId();
			}

			stmtSaveInterestRateCurve.setString(1, curve.getType());
			stmtSaveInterestRateCurve.setLong(2, curveId);
			stmtSaveInterestRateCurve.executeUpdate();

			if (curve.getId() != 0) {
				// Then, we delete the current curve's quote ids list.
				try (PreparedStatement stmtDeleteQuotesByCurveId = con
						.prepareStatement("DELETE FROM CURVE_QUOTE WHERE CURVE_ID = ? ")) {
					stmtDeleteQuotesByCurveId.setLong(1, curve.getId());
					stmtDeleteQuotesByCurveId.executeUpdate();
				}
			}

			// We insert the new curve's quote ids list

			try (PreparedStatement stmtSaveRateQuotes = con.prepareStatement("INSERT INTO CURVE_QUOTE VALUES(?,?) ")) {
				if (curve.getQuotes() != null && curve.getQuotes().isEmpty()) {
					for (Quote quote : curve.getQuotes()) {
						stmtSaveRateQuotes.clearParameters();
						stmtSaveRateQuotes.setLong(1, curveId);
						stmtSaveRateQuotes.setLong(2, quote.getId());
						stmtSaveRateQuotes.addBatch();
					}
				}
				stmtSaveRateQuotes.executeBatch();
			}

			if (curve.getId() != 0) {
				// Now, we must delete the current rate points

				try (PreparedStatement stmtDeleteRatePointsByCurveId = con
						.prepareStatement("DELETE FROM CURVE_POINT WHERE CURVE_ID = ?")) {
					stmtDeleteRatePointsByCurveId.setLong(1, curve.getId());
					stmtDeleteRatePointsByCurveId.executeUpdate();
				}
			}

			try (PreparedStatement stmtSaveRatePoints = con
					.prepareStatement("INSERT INTO CURVE_POINT VALUES(?,?,?) ")) {
				for (Map.Entry<LocalDate, BigDecimal> point : curve.getPoints().entrySet()) {
					if (point != null && point.getValue() != null) {
						stmtSaveRatePoints.clearParameters();
						stmtSaveRatePoints.setLong(1, curveId);
						stmtSaveRatePoints.setDate(2, java.sql.Date.valueOf(point.getKey()));
						stmtSaveRatePoints.setBigDecimal(3, point.getValue());
						stmtSaveRatePoints.addBatch();
					}
				}
				stmtSaveRatePoints.executeBatch();
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		curve.setId(curveId);
		return curveId;
	}

	public static InterestRateCurve getInterestRateCurveByName(String curveName) {
		InterestRateCurve interestRateCurve = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetInterestRateCurveByName = con.prepareStatement(
						"SELECT * FROM INTEREST_RATE_CURVE, CURVE WHERE INTEREST_RATE_CURVE.ID = CURVE.ID AND CURVE.NAME = ?")) {
			stmtGetInterestRateCurveByName.setString(1, curveName);
			try (ResultSet results = stmtGetInterestRateCurveByName.executeQuery()) {
				while (results.next()) {
					boolean isZeroCoupon = results.getString("type") != null
							&& results.getString("type").equals("ZeroCouponCurve");
					LegalEntity processingOrg = null;
					long poId = results.getLong("processing_org_id");
					if (poId > 0) {
						processingOrg = LegalEntitySQL.getLegalEntityById(poId);
					}
					if (isZeroCoupon) {
						interestRateCurve = new ZeroCouponCurve(results.getString("name"), processingOrg);
					} else {
						interestRateCurve = new InterestRateCurve(results.getString("name"), processingOrg);
					}
					interestRateCurve.setId(results.getLong("id"));
					interestRateCurve.setAlgorithm(results.getString("algorithm"));
					interestRateCurve.setInterpolator(results.getString("interpolator"));
					interestRateCurve.setInstance(results.getString("instance"));
					java.sql.Date quoteDate = results.getDate("quote_date");
					if (quoteDate != null) {
						interestRateCurve.setQuoteDate(quoteDate.toLocalDate());
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return interestRateCurve;
	}

	public static InterestRateCurve getInterestRateCurveByNameAndPo(String curveName, long poId) {
		InterestRateCurve interestRateCurve = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetInterestRateCurveByName = con.prepareStatement(
						"SELECT * FROM INTEREST_RATE_CURVE, CURVE WHERE INTEREST_RATE_CURVE.ID = CURVE.ID AND NAME = ? AND PROCESSING_ORG_ID = ?")) {
			stmtGetInterestRateCurveByName.setString(1, curveName);
			stmtGetInterestRateCurveByName.setLong(2, poId);
			try (ResultSet results = stmtGetInterestRateCurveByName.executeQuery()) {
				while (results.next()) {
					boolean isZeroCoupon = results.getString("type") != null
							&& results.getString("type").equals("ZeroCouponCurve");
					LegalEntity processingOrg = null;
					if (poId > 0) {
						processingOrg = LegalEntitySQL.getLegalEntityById(poId);
					}
					if (isZeroCoupon) {
						interestRateCurve = new ZeroCouponCurve(results.getString("name"), processingOrg);
					} else {
						interestRateCurve = new InterestRateCurve(results.getString("name"), processingOrg);
					}
					interestRateCurve.setId(results.getLong("id"));
					interestRateCurve.setAlgorithm(results.getString("algorithm"));
					interestRateCurve.setInterpolator(results.getString("interpolator"));
					interestRateCurve.setInstance(results.getString("instance"));
					java.sql.Date quoteDate = results.getDate("quote_date");
					if (quoteDate != null) {
						interestRateCurve.setQuoteDate(quoteDate.toLocalDate());
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return interestRateCurve;
	}

	public static InterestRateCurve getInterestRateCurveById(long curveId) {
		InterestRateCurve interestRateCurve = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetInterestRateCurveById = con.prepareStatement(
						"SELECT * FROM INTEREST_RATE_CURVE, CURVE WHERE INTEREST_RATE_CURVE.ID = CURVE.ID AND INTEREST_RATE_CURVE.ID = ?")) {
			stmtGetInterestRateCurveById.setLong(1, curveId);
			try (ResultSet results = stmtGetInterestRateCurveById.executeQuery()) {
				while (results.next()) {
					boolean isZeroCoupon = results.getString("type") != null
							&& results.getString("type").equals("ZeroCouponCurve");
					LegalEntity processingOrg = null;
					long poId = results.getLong("processing_org_id");
					if (poId > 0) {
						processingOrg = LegalEntitySQL.getLegalEntityById(poId);
					}
					if (isZeroCoupon) {
						interestRateCurve = new ZeroCouponCurve(results.getString("name"), processingOrg);
					} else {
						interestRateCurve = new InterestRateCurve(results.getString("name"), processingOrg);
					}
					interestRateCurve.setId(results.getLong("id"));
					interestRateCurve.setAlgorithm(results.getString("algorithm"));
					interestRateCurve.setInterpolator(results.getString("interpolator"));
					interestRateCurve.setInstance(results.getString("instance"));
					java.sql.Date quoteDate = results.getDate("quote_date");
					if (quoteDate != null) {
						interestRateCurve.setQuoteDate(quoteDate.toLocalDate());
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return interestRateCurve;
	}

	public static List<RatePoint> getInterestRateCurvePointsByCurveId(long curveId) {

		List<RatePoint> points = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetInterestRateCurvePointsByCurveId = con
						.prepareStatement("SELECT * FROM CURVE_POINT WHERE CURVE_ID = ? ")) {
			stmtGetInterestRateCurvePointsByCurveId.setLong(1, curveId);
			try (ResultSet results = stmtGetInterestRateCurvePointsByCurveId.executeQuery()) {
				while (results.next()) {
					if (points == null) {
						points = new ArrayList<RatePoint>();
					}
					LocalDate date = results.getDate("date").toLocalDate();
					BigDecimal rate = results.getBigDecimal("rate");
					points.add(new RatePoint(date, rate));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

}