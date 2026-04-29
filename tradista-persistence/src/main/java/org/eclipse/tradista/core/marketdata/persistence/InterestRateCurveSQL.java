package org.eclipse.tradista.core.marketdata.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TYPE;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
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

	private static final Field TYPE_FIELD = new Field(TYPE);
	private static final Field ID_FIELD = new Field(ID);

	private static final Field[] IRC_FIELDS = { TYPE_FIELD, ID_FIELD };

	public static final Table INTEREST_RATE_CURVE_TABLE = new Table("INTEREST_RATE_CURVE", IRC_FIELDS);

	public static long saveInterestRateCurve(String curveName, String curveType) {
		long curveId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveCurve = TradistaDBUtil.buildInsertPreparedStatement(con, CurveSQL.CURVE_TABLE,
						CurveSQL.NAME_FIELD);
				PreparedStatement stmtSaveInterestRateCurve = TradistaDBUtil.buildInsertPreparedStatement(con,
						INTEREST_RATE_CURVE_TABLE, IRC_FIELDS)) {
			stmtSaveCurve.setString(1, curveName);
			stmtSaveCurve.executeUpdate();
			try (ResultSet generatedKeys = stmtSaveCurve.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					curveId = generatedKeys.getLong(1);
				} else {
					throw new SQLException("Creating Curve failed, no generated key obtained.");
				}
			}
			stmtSaveInterestRateCurve.setString(1, curveType);
			stmtSaveInterestRateCurve.setLong(2, curveId);
			stmtSaveInterestRateCurve.executeUpdate();
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return curveId;
	}

	public static boolean deleteInterestRateCurve(long curveId) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteRatePoints = CurveSQL.getDeleteCurvePointsByCurveIdPreparedStatement(con);
				PreparedStatement stmtDeleteQuotesByCurveId = CurveSQL.getDeleteCurveQuotesByCurveIdPreparedStatement(con);
				PreparedStatement stmtDeleteInterestRateCurve = TradistaDBUtil.buildDeletePreparedStatement(con,
						INTEREST_RATE_CURVE_TABLE, ID_FIELD);
				PreparedStatement stmtDeleteCurve = TradistaDBUtil.buildDeletePreparedStatement(con,
						CurveSQL.CURVE_TABLE, CurveSQL.ID_FIELD)) {
			stmtDeleteRatePoints.setLong(1, curveId);
			stmtDeleteRatePoints.executeUpdate();
			stmtDeleteQuotesByCurveId.setLong(1, curveId);
			stmtDeleteQuotesByCurveId.executeUpdate();
			stmtDeleteInterestRateCurve.setLong(1, curveId);
			stmtDeleteInterestRateCurve.executeUpdate();
			stmtDeleteCurve.setLong(1, curveId);
			stmtDeleteCurve.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<InterestRateCurve> getAllInterestRateCurves() {
		Set<InterestRateCurve> interestRateCurves = null;
		org.eclipse.tradista.core.common.persistence.util.Join join = org.eclipse.tradista.core.common.persistence.util.Join
				.innerEq(INTEREST_RATE_CURVE_TABLE, CurveSQL.ID_FIELD, ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllInterestRateCurves = con
						.prepareStatement(TradistaDBUtil.buildSelectQuery(CurveSQL.CURVE_TABLE, join));
				ResultSet results = stmtGetAllInterestRateCurves.executeQuery()) {
			while (results.next()) {
				if (interestRateCurves == null) {
					interestRateCurves = new HashSet<>();
				}
				interestRateCurves.add(buildInterestRateCurve(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return interestRateCurves;
	}

	public static Set<InterestRateCurve> getInterestRateCurvesByPoId(long poId) {
		Set<InterestRateCurve> interestRateCurves = null;
		org.eclipse.tradista.core.common.persistence.util.Join join = org.eclipse.tradista.core.common.persistence.util.Join
				.innerEq(INTEREST_RATE_CURVE_TABLE, CurveSQL.ID_FIELD, ID_FIELD);
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(CurveSQL.CURVE_TABLE, join));
		TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.PROCESSING_ORG_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetInterestRateCurvesByPoId = con.prepareStatement(sql.toString())) {
			stmtGetInterestRateCurvesByPoId.setLong(1, poId);
			try (ResultSet results = stmtGetInterestRateCurvesByPoId.executeQuery()) {
				while (results.next()) {
					if (interestRateCurves == null) {
						interestRateCurves = new HashSet<>();
					}
					interestRateCurves.add(buildInterestRateCurve(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return interestRateCurves;
	}

	public static Set<ZeroCouponCurve> getAllZeroCouponCurves() {
		Set<ZeroCouponCurve> zeroCouponCurves = null;
		org.eclipse.tradista.core.common.persistence.util.Join join = org.eclipse.tradista.core.common.persistence.util.Join
				.innerEq(INTEREST_RATE_CURVE_TABLE, CurveSQL.ID_FIELD, ID_FIELD);
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(CurveSQL.CURVE_TABLE, join));
		TradistaDBUtil.addParameterizedFilter(sql, TYPE_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllZeroCouponCurves = con.prepareStatement(sql.toString())) {
			stmtGetAllZeroCouponCurves.setString(1, ZeroCouponCurve.ZERO_COUPON_CURVE);
			try (ResultSet results = stmtGetAllZeroCouponCurves.executeQuery()) {
				while (results.next()) {
					if (zeroCouponCurves == null) {
						zeroCouponCurves = new HashSet<>();
					}
					zeroCouponCurves.add(buildZeroCouponCurve(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return zeroCouponCurves;
	}

	public static Set<ZeroCouponCurve> getZeroCouponCurvesByPoId(long poId) {
		Set<ZeroCouponCurve> zeroCouponCurves = null;
		org.eclipse.tradista.core.common.persistence.util.Join join = org.eclipse.tradista.core.common.persistence.util.Join
				.innerEq(INTEREST_RATE_CURVE_TABLE, CurveSQL.ID_FIELD, ID_FIELD);
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(CurveSQL.CURVE_TABLE, join));
		TradistaDBUtil.addParameterizedFilter(sql, TYPE_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.PROCESSING_ORG_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetZeroCouponCurvesByPoId = con.prepareStatement(sql.toString())) {
			stmtGetZeroCouponCurvesByPoId.setString(1, ZeroCouponCurve.ZERO_COUPON_CURVE);
			stmtGetZeroCouponCurvesByPoId.setLong(2, poId);
			try (ResultSet results = stmtGetZeroCouponCurvesByPoId.executeQuery()) {
				while (results.next()) {
					if (zeroCouponCurves == null) {
						zeroCouponCurves = new HashSet<>();
					}
					zeroCouponCurves.add(buildZeroCouponCurve(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return zeroCouponCurves;
	}

	public static List<RatePoint> getAllInterestRateCurvePointsByCurveIdAndDate(long curveId, Year year, Month month) {
		List<RatePoint> points = null;
		LocalDate startDate = LocalDate.of(year.getValue(), month, 1);
		LocalDate endDate = startDate.plus(1, ChronoUnit.MONTHS);

		StringBuilder sql = new StringBuilder(CurveSQL.getCurvePointSelectQuery());
		TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.CURVE_POINT_CURVE_ID_FIELD);
		TradistaDBUtil.addFilter(sql, CurveSQL.DATE_FIELD, startDate, true);
		TradistaDBUtil.addFilter(sql, CurveSQL.DATE_FIELD, endDate, false, false);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString())) {
			stmt.setLong(1, curveId);

			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					if (points == null) {
						points = new ArrayList<>();
					}
					LocalDate date = results.getDate(CurveSQL.DATE_FIELD.getName()).toLocalDate();
					BigDecimal rate = results.getBigDecimal(CurveSQL.RATE_FIELD.getName());
					points.add(new RatePoint(date, rate));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static List<RatePoint> getInterestRateCurvePointsByCurveIdAndDates(long curveId, LocalDate min,
			LocalDate max) {
		List<RatePoint> points = null;
		StringBuilder sql = new StringBuilder(CurveSQL.getCurvePointSelectQuery());
		TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.CURVE_POINT_CURVE_ID_FIELD);
		TradistaDBUtil.addFilter(sql, CurveSQL.DATE_FIELD, min, true);
		TradistaDBUtil.addFilter(sql, CurveSQL.DATE_FIELD, max, false);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString())) {
			stmt.setLong(1, curveId);
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					if (points == null) {
						points = new ArrayList<>();
					}
					LocalDate date = results.getDate(CurveSQL.DATE_FIELD.getName()).toLocalDate();
					BigDecimal rate = results.getBigDecimal(CurveSQL.RATE_FIELD.getName());
					points.add(new RatePoint(date, rate));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	public static boolean saveInterestRateCurvePoints(long id, List<RatePoint> ratePoints, Year year, Month month) {
		boolean bSaved = false;
		LocalDate startDate = LocalDate.of(year.getValue(), month, 1);
		LocalDate endDate = startDate.plus(1, ChronoUnit.MONTHS);

		StringBuilder deleteSql = new StringBuilder("DELETE FROM " + CurveSQL.CURVE_POINT_TABLE);
		TradistaDBUtil.addParameterizedFilter(deleteSql, CurveSQL.CURVE_POINT_CURVE_ID_FIELD);
		TradistaDBUtil.addFilter(deleteSql, CurveSQL.DATE_FIELD, startDate, true);
		TradistaDBUtil.addFilter(deleteSql, CurveSQL.DATE_FIELD, endDate, false, false);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteRatePointsByCurveIdYearAndMonth = con
						.prepareStatement(deleteSql.toString());
				PreparedStatement stmtSaveRatePoints = CurveSQL.getInsertCurvePointPreparedStatement(con)) {

			stmtDeleteRatePointsByCurveIdYearAndMonth.setLong(1, id);
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
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static long saveInterestRateCurve(InterestRateCurve curve) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveCurve = (curve.getId() == 0) ? CurveSQL.getInsertCurvePreparedStatement(con)
						: CurveSQL.getUpdateCurvePreparedStatement(con);
				PreparedStatement stmtSaveInterestRateCurve = (curve.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, INTEREST_RATE_CURVE_TABLE, IRC_FIELDS)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, INTEREST_RATE_CURVE_TABLE,
								TYPE_FIELD)) {

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
			if (curve.getId() != 0) {
				stmtSaveCurve.setLong(8, curve.getId());
			}
			stmtSaveCurve.executeUpdate();

			if (curve.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveCurve.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						curve.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("Creating curve failed, no generated key obtained.");
					}
				}
			} else {
				// We delete the current curve's quote ids list.
				try (PreparedStatement stmtDeleteQuotesByCurveId = CurveSQL.getDeleteCurveQuotesByCurveIdPreparedStatement(con)) {
					stmtDeleteQuotesByCurveId.setLong(1, curve.getId());
					stmtDeleteQuotesByCurveId.executeUpdate();
				}
				// Now, we must delete the current rate points
				try (PreparedStatement stmtDeleteRatePointsByCurveId = CurveSQL.getDeleteCurvePointsByCurveIdPreparedStatement(con)) {
					stmtDeleteRatePointsByCurveId.setLong(1, curve.getId());
					stmtDeleteRatePointsByCurveId.executeUpdate();
				}
			}

			stmtSaveInterestRateCurve.setString(1, curve.getType());
			stmtSaveInterestRateCurve.setLong(2, curve.getId());
			stmtSaveInterestRateCurve.executeUpdate();

			// We insert the new curve's quote ids list
			try (PreparedStatement stmtSaveRateQuotes = CurveSQL.getInsertCurveQuotePreparedStatement(con)) {
				if (curve.getQuotes() != null && !curve.getQuotes().isEmpty()) {
					for (Quote quote : curve.getQuotes()) {
						stmtSaveRateQuotes.clearParameters();
						stmtSaveRateQuotes.setLong(1, curve.getId());
						stmtSaveRateQuotes.setLong(2, quote.getId());
						stmtSaveRateQuotes.addBatch();
					}
				}
				stmtSaveRateQuotes.executeBatch();
			}
			try (PreparedStatement stmtSaveRatePoints = CurveSQL.getInsertCurvePointPreparedStatement(con)) {
				for (Map.Entry<LocalDate, BigDecimal> point : curve.getPoints().entrySet()) {
					if (point != null && point.getValue() != null) {
						stmtSaveRatePoints.clearParameters();
						stmtSaveRatePoints.setLong(1, curve.getId());
						stmtSaveRatePoints.setDate(2, java.sql.Date.valueOf(point.getKey()));
						stmtSaveRatePoints.setBigDecimal(3, point.getValue());
						stmtSaveRatePoints.addBatch();
					}
				}
				stmtSaveRatePoints.executeBatch();
			}

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return curve.getId();
	}

	public static InterestRateCurve getInterestRateCurveByName(String curveName) {
		InterestRateCurve interestRateCurve = null;
		org.eclipse.tradista.core.common.persistence.util.Join join = org.eclipse.tradista.core.common.persistence.util.Join
				.innerEq(INTEREST_RATE_CURVE_TABLE, CurveSQL.ID_FIELD, ID_FIELD);
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(CurveSQL.CURVE_TABLE, join));
		TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.NAME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetInterestRateCurveByName = con.prepareStatement(sql.toString())) {
			stmtGetInterestRateCurveByName.setString(1, curveName);
			try (ResultSet results = stmtGetInterestRateCurveByName.executeQuery()) {
				while (results.next()) {
					interestRateCurve = buildInterestRateCurve(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return interestRateCurve;
	}

	public static InterestRateCurve getInterestRateCurveByNameAndPo(String curveName, long poId) {
		InterestRateCurve interestRateCurve = null;
		org.eclipse.tradista.core.common.persistence.util.Join join = org.eclipse.tradista.core.common.persistence.util.Join
				.innerEq(INTEREST_RATE_CURVE_TABLE, CurveSQL.ID_FIELD, ID_FIELD);
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(CurveSQL.CURVE_TABLE, join));
		TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.NAME_FIELD);
		if (poId > 0) {
			TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.PROCESSING_ORG_ID_FIELD);
		} else {
			TradistaDBUtil.addIsNullFilter(sql, CurveSQL.PROCESSING_ORG_ID_FIELD);
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetInterestRateCurveByNameAndPo = con.prepareStatement(sql.toString())) {
			stmtGetInterestRateCurveByNameAndPo.setString(1, curveName);
			if (poId > 0) {
				stmtGetInterestRateCurveByNameAndPo.setLong(2, poId);
			}
			try (ResultSet results = stmtGetInterestRateCurveByNameAndPo.executeQuery()) {
				while (results.next()) {
					interestRateCurve = buildInterestRateCurve(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return interestRateCurve;
	}

	public static InterestRateCurve getInterestRateCurveById(long curveId) {
		InterestRateCurve interestRateCurve = null;
		org.eclipse.tradista.core.common.persistence.util.Join join = org.eclipse.tradista.core.common.persistence.util.Join
				.innerEq(INTEREST_RATE_CURVE_TABLE, CurveSQL.ID_FIELD, ID_FIELD);
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(CurveSQL.CURVE_TABLE, join));
		TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetInterestRateCurveById = con.prepareStatement(sql.toString())) {
			stmtGetInterestRateCurveById.setLong(1, curveId);
			try (ResultSet results = stmtGetInterestRateCurveById.executeQuery()) {
				while (results.next()) {
					interestRateCurve = buildInterestRateCurve(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return interestRateCurve;
	}

	public static List<RatePoint> getInterestRateCurvePointsByCurveId(long curveId) {
		List<RatePoint> points = null;
		StringBuilder sql = new StringBuilder(CurveSQL.getCurvePointSelectQuery());
		TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.CURVE_POINT_CURVE_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString())) {
			stmt.setLong(1, curveId);
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					if (points == null) {
						points = new ArrayList<>();
					}
					LocalDate date = results.getDate(CurveSQL.DATE_FIELD.getName()).toLocalDate();
					BigDecimal rate = results.getBigDecimal(CurveSQL.RATE_FIELD.getName());
					points.add(new RatePoint(date, rate));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return points;
	}

	private static InterestRateCurve buildInterestRateCurve(ResultSet results) throws SQLException {
		boolean isZeroCoupon = results.getString(TYPE_FIELD.getName()) != null
				&& results.getString(TYPE_FIELD.getName()).equals("ZeroCouponCurve");
		InterestRateCurve interestRateCurve;
		LegalEntity processingOrg = null;
		long poId = results.getLong(CurveSQL.PROCESSING_ORG_ID_FIELD.getName());
		if (poId > 0) {
			processingOrg = LegalEntitySQL.getLegalEntityById(poId);
		}
		if (isZeroCoupon) {
			interestRateCurve = new ZeroCouponCurve(results.getString(CurveSQL.NAME_FIELD.getName()), processingOrg);
		} else {
			interestRateCurve = new InterestRateCurve(results.getString(CurveSQL.NAME_FIELD.getName()), processingOrg);
		}

		interestRateCurve.setId(results.getLong(CurveSQL.ID_FIELD.getName()));
		interestRateCurve.setAlgorithm(results.getString(CurveSQL.ALGORITHM_FIELD.getName()));
		interestRateCurve.setInterpolator(results.getString(CurveSQL.INTERPOLATOR_FIELD.getName()));
		interestRateCurve.setInstance(results.getString(CurveSQL.INSTANCE_FIELD.getName()));
		java.sql.Date quoteDate = results.getDate(CurveSQL.QUOTE_DATE_FIELD.getName());
		if (quoteDate != null) {
			interestRateCurve.setQuoteDate(quoteDate.toLocalDate());
		}
		return interestRateCurve;
	}

	private static ZeroCouponCurve buildZeroCouponCurve(ResultSet results) throws SQLException {
		LegalEntity processingOrg = null;
		long poId = results.getLong(CurveSQL.PROCESSING_ORG_ID_FIELD.getName());
		if (poId > 0) {
			processingOrg = LegalEntitySQL.getLegalEntityById(poId);
		}
		ZeroCouponCurve zeroCouponCurve = new ZeroCouponCurve(results.getString(CurveSQL.NAME_FIELD.getName()),
				processingOrg);
		zeroCouponCurve.setId(results.getLong(CurveSQL.ID_FIELD.getName()));
		zeroCouponCurve.setAlgorithm(results.getString(CurveSQL.ALGORITHM_FIELD.getName()));
		zeroCouponCurve.setInterpolator(results.getString(CurveSQL.INTERPOLATOR_FIELD.getName()));
		zeroCouponCurve.setInstance(results.getString(CurveSQL.INSTANCE_FIELD.getName()));
		java.sql.Date quoteDate = results.getDate(CurveSQL.QUOTE_DATE_FIELD.getName());
		if (quoteDate != null) {
			zeroCouponCurve.setQuoteDate(quoteDate.toLocalDate());
		}
		return zeroCouponCurve;
	}
}