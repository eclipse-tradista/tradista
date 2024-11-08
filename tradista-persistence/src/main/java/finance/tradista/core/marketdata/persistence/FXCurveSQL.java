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
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.RatePoint;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

public class FXCurveSQL {

	public static long saveFXCurve(String curveName) {
		long curveId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveCurve = con.prepareStatement("INSERT INTO CURVE(NAME) VALUES(?)",
						Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtSaveFXCurve = con.prepareStatement("INSERT INTO FX_CURVE(ID) VALUES(?)")) {
			stmtSaveCurve.setString(1, curveName);
			stmtSaveCurve.executeUpdate();
			try (ResultSet generatedKeys = stmtSaveFXCurve.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					curveId = generatedKeys.getLong(1);
				} else {
					throw new SQLException("Creation of FX Curve failed, no generated key obtained.");
				}
			}
			stmtSaveFXCurve.setLong(1, curveId);
			stmtSaveFXCurve.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return curveId;
	}

	public static boolean deleteFXCurve(long curveId) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteRatePoints = con
						.prepareStatement("DELETE FROM CURVE_POINT WHERE CURVE_ID = ? ");
				PreparedStatement stmtDeleteQuotesByCurveName = con
						.prepareStatement("DELETE FROM CURVE_QUOTE WHERE CURVE_ID = ? ");
				PreparedStatement stmtDeleteFXCurve = con.prepareStatement("DELETE FROM FX_CURVE WHERE ID = ? ");
				PreparedStatement stmtDeleteCurve = con.prepareStatement("DELETE FROM CURVE WHERE ID = ? ")) {
			stmtDeleteRatePoints.setLong(1, curveId);
			stmtDeleteRatePoints.executeUpdate();
			stmtDeleteQuotesByCurveName.setLong(1, curveId);
			stmtDeleteQuotesByCurveName.executeUpdate();
			stmtDeleteFXCurve.setLong(1, curveId);
			stmtDeleteFXCurve.executeUpdate();
			stmtDeleteCurve.setLong(1, curveId);
			stmtDeleteCurve.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<FXCurve> getAllFXCurves() {
		Set<FXCurve> fxCurves = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllFXCurves = con
						.prepareStatement("SELECT * FROM FX_CURVE, CURVE WHERE FX_CURVE.ID = CURVE.ID");
				ResultSet results = stmtGetAllFXCurves.executeQuery()) {
			while (results.next()) {
				if (fxCurves == null) {
					fxCurves = new HashSet<FXCurve>();
				}
				long poId = results.getLong("processing_org_id");
				LegalEntity processingOrg = null;
				if (poId > 0) {
					processingOrg = LegalEntitySQL.getLegalEntityById(poId);
				}
				FXCurve fxCurve = new FXCurve(results.getString("name"), processingOrg);
				fxCurve.setId(results.getLong("id"));
				fxCurve.setAlgorithm(results.getString("algorithm"));
				fxCurve.setInterpolator(results.getString("interpolator"));
				fxCurve.setInstance(results.getString("instance"));
				long primaryCurrencyId = results.getLong("primary_currency_id");
				long quoteCurrencyId = results.getLong("quote_currency_id");
				long primaryCurrencyIRCurveId = results.getLong("primary_currency_curve_id");
				long quoteCurrencyIRCurveId = results.getLong("quote_currency_curve_id");
				long quoteSetId = results.getLong("quote_set_id");

				java.sql.Date quoteDate = results.getDate("quote_date");

				if (primaryCurrencyId != 0) {
					fxCurve.setPrimaryCurrency(CurrencySQL.getCurrencyById(primaryCurrencyId));
				}

				if (quoteCurrencyId != 0) {
					fxCurve.setQuoteCurrency(CurrencySQL.getCurrencyById(quoteCurrencyId));
				}
				if (primaryCurrencyIRCurveId != 0) {
					fxCurve.setPrimaryCurrencyIRCurve(
							InterestRateCurveSQL.getInterestRateCurveById(primaryCurrencyIRCurveId));
				}
				if (quoteCurrencyIRCurveId != 0) {
					fxCurve.setQuoteCurrencyIRCurve(
							InterestRateCurveSQL.getInterestRateCurveById(quoteCurrencyIRCurveId));
				}
				if (quoteSetId != 0) {
					fxCurve.setQuoteSet(QuoteSetSQL.getQuoteSetById(quoteSetId));
				}
				if (quoteDate != null) {
					fxCurve.setQuoteDate(quoteDate.toLocalDate());
				}
				fxCurves.add(fxCurve);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return fxCurves;
	}

	public static List<RatePoint> getAllFXCurvePointsByCurveIdAndDate(long curveId, Year year, Month month) {
		List<RatePoint> points = null;
		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {

			LocalDate startDate = LocalDate.of(year.getValue(), month, 1);
			LocalDate endDate = startDate.plus(1, ChronoUnit.MONTHS);

			String query = "SELECT DATE DATE, RATE RATE " + "FROM CURVE_POINT WHERE " + "CURVE_ID = " + curveId
					+ " AND DATE >= '" + startDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + "'"
					+ " AND DATE < '" + endDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + "'";

			try (ResultSet results = stmt.executeQuery(query)) {
				while (results.next()) {
					LocalDate date = results.getDate("date").toLocalDate();
					BigDecimal rate = results.getBigDecimal("rate");
					if (points == null) {
						points = new ArrayList<RatePoint>();
					}
					points.add(new RatePoint(date, rate));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static List<RatePoint> getFXCurvePointsByCurveIdAndDates(long curveId, LocalDate min, LocalDate max) {
		List<RatePoint> points = null;
		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {
			String query = "SELECT DATE DATE, RATE RATE "
					+ "FROM CURVE_POINT, FX_CURVE WHERE CURVE_POINT.CURVE_ID = FX_CURVE.ID" + " AND FX_CURVE.ID = "
					+ curveId + " AND DATE >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(min) + "'"
					+ " AND DATE <= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(max) + "'";

			try (ResultSet results = stmt.executeQuery(query)) {
				while (results.next()) {
					LocalDate date = results.getDate("date").toLocalDate();
					BigDecimal rate = results.getBigDecimal("rate");
					if (points == null) {
						points = new ArrayList<RatePoint>();
					}
					points.add(new RatePoint(date, rate));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static boolean saveFXCurvePoints(long id, List<RatePoint> ratePoints, Year year, Month month) {
		boolean bSaved = false;
		// First, we delete the data for this curve and this month
		LocalDate startDate = LocalDate.of(year.getValue(), month, 1);
		LocalDate endDate = startDate.plus(1, ChronoUnit.MONTHS);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteRatePointsByCurveIdYearAndMonth = con
						.prepareStatement("DELETE FROM CURVE_POINT WHERE CURVE_ID = ? AND DATE  >= ? AND DATE < ? ");
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
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return bSaved;

	}

	public static long saveFXCurve(FXCurve curve) {
		long curveId = 0;
		// First, we save the IR Curve definition
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveCurve = (curve.getId() != 0) ? con.prepareStatement(
						"UPDATE CURVE SET NAME=?, ALGORITHM =?, INTERPOLATOR=?, INSTANCE=?, QUOTE_DATE=?, QUOTE_SET_ID=?, PROCESSING_ORG_ID=?  WHERE ID = ?")
						: con.prepareStatement(
								"INSERT INTO CURVE(NAME, ALGORITHM, INTERPOLATOR, INSTANCE, QUOTE_DATE, QUOTE_SET_ID, PROCESSING_ORG_ID) VALUES (?,?,?,?,?,?,?)",
								Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtSaveFXCurve = (curve.getId() != 0) ? con.prepareStatement(
						"UPDATE FX_CURVE SET PRIMARY_CURRENCY_ID=?, QUOTE_CURRENCY_ID=?, PRIMARY_CURRENCY_CURVE_ID=?, QUOTE_CURRENCY_CURVE_ID=? WHERE ID = ?")
						: con.prepareStatement(
								"INSERT INTO FX_CURVE(PRIMARY_CURRENCY_ID, QUOTE_CURRENCY_ID, PRIMARY_CURRENCY_CURVE_ID, QUOTE_CURRENCY_CURVE_ID, ID) VALUES (?,?,?,?,?)")) {
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
						throw new SQLException("Creation of FX Curve failed, no generated key obtained.");
					}
				}
			} else {
				curveId = curve.getId();
			}

			if (curve.getPrimaryCurrency() == null) {
				stmtSaveFXCurve.setNull(1, Types.BIGINT);
			} else {
				stmtSaveFXCurve.setLong(1, curve.getPrimaryCurrency().getId());
			}
			if (curve.getQuoteCurrency() == null) {
				stmtSaveFXCurve.setNull(2, Types.BIGINT);
			} else {
				stmtSaveFXCurve.setLong(2, curve.getQuoteCurrency().getId());
			}
			if (curve.getPrimaryCurrencyIRCurve() == null) {
				stmtSaveFXCurve.setNull(3, Types.BIGINT);
			} else {
				stmtSaveFXCurve.setLong(3, curve.getPrimaryCurrencyIRCurve().getId());
			}
			if (curve.getQuoteCurrencyIRCurve() == null) {
				stmtSaveFXCurve.setNull(4, Types.BIGINT);
			} else {
				stmtSaveFXCurve.setLong(4, curve.getQuoteCurrencyIRCurve().getId());
			}
			stmtSaveFXCurve.setLong(5, curveId);

			stmtSaveFXCurve.executeUpdate();

			if (curve.getId() != 0) {
				// Then, we delete the current curve's quote ids list.
				try (PreparedStatement stmtDeleteQuotesByCurveId = con
						.prepareStatement("DELETE FROM CURVE_QUOTE WHERE CURVE_ID = ? ")) {
					stmtDeleteQuotesByCurveId.setLong(1, curve.getId());
					stmtDeleteQuotesByCurveId.executeUpdate();
				}
			}

			// We insert the new curve's quote ids list
			if (curve.getQuotes() != null && !curve.getQuotes().isEmpty()) {
				try (PreparedStatement stmtSaveRateQuotes = con
						.prepareStatement("INSERT INTO CURVE_QUOTE VALUES(?,?) ")) {
					for (Quote quote : curve.getQuotes()) {
						stmtSaveRateQuotes.clearParameters();
						stmtSaveRateQuotes.setLong(1, curveId);
						stmtSaveRateQuotes.setLong(2, quote.getId());
						stmtSaveRateQuotes.addBatch();
					}
					stmtSaveRateQuotes.executeBatch();
				}
			}

			if (curve.getId() != 0) {
				// Now, we must delete the current rate points
				try (PreparedStatement stmtDeleteRatePointsByCurveId = con
						.prepareStatement("DELETE FROM CURVE_POINT WHERE CURVE_ID = ?")) {
					stmtDeleteRatePointsByCurveId.setLong(1, curve.getId());
					stmtDeleteRatePointsByCurveId.executeUpdate();
				}
			}

			if (curve.getPoints() != null && !curve.getPoints().isEmpty()) {
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

			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		curve.setId(curveId);
		return curveId;
	}

	public static FXCurve getFXCurveByName(String curveName) {
		FXCurve fxCurve = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXCurveByName = con
						.prepareStatement("SELECT * FROM FX_CURVE, CURVE WHERE FX_CURVE.ID = CURVE.ID AND NAME = ?")) {
			stmtGetFXCurveByName.setString(1, curveName);
			try (ResultSet results = stmtGetFXCurveByName.executeQuery()) {
				while (results.next()) {
					long poId = results.getLong("processing_org_id");
					LegalEntity processingOrg = null;
					if (poId > 0) {
						processingOrg = LegalEntitySQL.getLegalEntityById(poId);
					}
					fxCurve = new FXCurve(results.getString("name"), processingOrg);
					fxCurve.setId(results.getLong("id"));
					fxCurve.setAlgorithm(results.getString("algorithm"));
					fxCurve.setInterpolator(results.getString("interpolator"));
					fxCurve.setInstance(results.getString("instance"));
					long primaryCurrencyId = results.getLong("primary_currency_id");
					long quoteCurrencyId = results.getLong("quote_currency_id");
					long primaryCurrencyIRCurveId = results.getLong("primary_currency_curve_id");
					long quoteCurrencyIRCurveId = results.getLong("quote_currency_curve_id");
					long quoteSetId = results.getLong("quote_set_id");
					java.sql.Date quoteDate = results.getDate("quote_date");

					if (primaryCurrencyId != 0) {
						fxCurve.setPrimaryCurrency(CurrencySQL.getCurrencyById(primaryCurrencyId));
					}

					if (quoteCurrencyId != 0) {
						fxCurve.setQuoteCurrency(CurrencySQL.getCurrencyById(quoteCurrencyId));
					}
					if (primaryCurrencyIRCurveId != 0) {
						fxCurve.setPrimaryCurrencyIRCurve(
								InterestRateCurveSQL.getInterestRateCurveById(primaryCurrencyIRCurveId));
					}
					if (quoteCurrencyIRCurveId != 0) {
						fxCurve.setQuoteCurrencyIRCurve(
								InterestRateCurveSQL.getInterestRateCurveById(quoteCurrencyIRCurveId));
					}
					if (quoteSetId != 0) {
						fxCurve.setQuoteSet(QuoteSetSQL.getQuoteSetById(quoteSetId));
					}
					if (quoteDate != null) {
						fxCurve.setQuoteDate(quoteDate.toLocalDate());
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return fxCurve;
	}

	public static FXCurve getFXCurveByNameAndPo(String curveName, long poId) {
		FXCurve fxCurve = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXCurveByName = con.prepareStatement(
						"SELECT * FROM FX_CURVE, CURVE WHERE FX_CURVE.ID = CURVE.ID AND NAME = ? AND PROCESSING_ORG_ID=?")) {
			stmtGetFXCurveByName.setString(1, curveName);
			stmtGetFXCurveByName.setLong(2, poId);
			try (ResultSet results = stmtGetFXCurveByName.executeQuery()) {
				while (results.next()) {
					LegalEntity processingOrg = null;
					if (poId > 0) {
						processingOrg = LegalEntitySQL.getLegalEntityById(poId);
					}
					fxCurve = new FXCurve(results.getString("name"), processingOrg);
					fxCurve.setId(results.getLong("id"));
					fxCurve.setAlgorithm(results.getString("algorithm"));
					fxCurve.setInterpolator(results.getString("interpolator"));
					fxCurve.setInstance(results.getString("instance"));
					long primaryCurrencyId = results.getLong("primary_currency_id");
					long quoteCurrencyId = results.getLong("quote_currency_id");
					long primaryCurrencyIRCurveId = results.getLong("primary_currency_curve_id");
					long quoteCurrencyIRCurveId = results.getLong("quote_currency_curve_id");
					long quoteSetId = results.getLong("quote_set_id");
					java.sql.Date quoteDate = results.getDate("quote_date");

					if (primaryCurrencyId != 0) {
						fxCurve.setPrimaryCurrency(CurrencySQL.getCurrencyById(primaryCurrencyId));
					}

					if (quoteCurrencyId != 0) {
						fxCurve.setQuoteCurrency(CurrencySQL.getCurrencyById(quoteCurrencyId));
					}
					if (primaryCurrencyIRCurveId != 0) {
						fxCurve.setPrimaryCurrencyIRCurve(
								InterestRateCurveSQL.getInterestRateCurveById(primaryCurrencyIRCurveId));
					}
					if (quoteCurrencyIRCurveId != 0) {
						fxCurve.setQuoteCurrencyIRCurve(
								InterestRateCurveSQL.getInterestRateCurveById(quoteCurrencyIRCurveId));
					}
					if (quoteSetId != 0) {
						fxCurve.setQuoteSet(QuoteSetSQL.getQuoteSetById(quoteSetId));
					}
					if (quoteDate != null) {
						fxCurve.setQuoteDate(quoteDate.toLocalDate());
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return fxCurve;
	}

	public static FXCurve getFXCurveById(long curveId) {

		FXCurve fxCurve = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXCurveById = con.prepareStatement(
						"SELECT * FROM FX_CURVE, CURVE WHERE FX_CURVE.ID = CURVE.ID AND CURVE.ID = ?")) {
			stmtGetFXCurveById.setLong(1, curveId);
			try (ResultSet results = stmtGetFXCurveById.executeQuery()) {
				while (results.next()) {
					long poId = results.getLong("processing_org_id");
					LegalEntity processingOrg = null;
					if (poId > 0) {
						processingOrg = LegalEntitySQL.getLegalEntityById(poId);
					}
					fxCurve = new FXCurve(results.getString("name"), processingOrg);
					fxCurve.setId(results.getLong("id"));
					fxCurve.setAlgorithm(results.getString("algorithm"));
					fxCurve.setInterpolator(results.getString("interpolator"));
					fxCurve.setInstance(results.getString("instance"));
					long primaryCurrencyId = results.getLong("primary_currency_id");
					long quoteCurrencyId = results.getLong("quote_currency_id");
					long primaryCurrencyIRCurveId = results.getLong("primary_currency_curve_id");
					long quoteCurrencyIRCurveId = results.getLong("quote_currency_curve_id");
					long quoteSetId = results.getLong("quote_set_id");
					java.sql.Date quoteDate = results.getDate("quote_date");

					if (primaryCurrencyId != 0) {
						fxCurve.setPrimaryCurrency(CurrencySQL.getCurrencyById(primaryCurrencyId));
					}

					if (quoteCurrencyId != 0) {
						fxCurve.setQuoteCurrency(CurrencySQL.getCurrencyById(quoteCurrencyId));
					}
					if (primaryCurrencyIRCurveId != 0) {
						fxCurve.setPrimaryCurrencyIRCurve(
								InterestRateCurveSQL.getInterestRateCurveById(primaryCurrencyIRCurveId));
					}
					if (quoteCurrencyIRCurveId != 0) {
						fxCurve.setQuoteCurrencyIRCurve(
								InterestRateCurveSQL.getInterestRateCurveById(quoteCurrencyIRCurveId));
					}
					if (quoteSetId != 0) {
						fxCurve.setQuoteSet(QuoteSetSQL.getQuoteSetById(quoteSetId));
					}
					if (quoteDate != null) {
						fxCurve.setQuoteDate(quoteDate.toLocalDate());
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return fxCurve;
	}

	public static List<RatePoint> getFXCurvePointsByCurveId(long curveId) {

		List<RatePoint> points = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXCurvePointsByCurveId = con
						.prepareStatement("SELECT * FROM CURVE_POINT WHERE CURVE_ID = ? ")) {
			stmtGetFXCurvePointsByCurveId.setLong(1, curveId);
			try (ResultSet results = stmtGetFXCurvePointsByCurveId.executeQuery()) {
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