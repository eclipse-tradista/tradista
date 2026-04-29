package org.eclipse.tradista.core.marketdata.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;

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
import org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
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

	public static final String PRIMARY_CURRENCY_CURVE_ID = "PRIMARY_CURRENCY_CURVE_ID";
	public static final String QUOTE_CURRENCY_CURVE_ID = "QUOTE_CURRENCY_CURVE_ID";

	private static final Field PRIMARY_CURRENCY_ID_FIELD = new Field(TradistaDBConstants.PRIMARY_CURRENCY_ID);
	private static final Field QUOTE_CURRENCY_ID_FIELD = new Field(TradistaDBConstants.QUOTE_CURRENCY_ID);
	private static final Field PRIMARY_CURRENCY_CURVE_ID_FIELD = new Field(PRIMARY_CURRENCY_CURVE_ID);
	private static final Field QUOTE_CURRENCY_CURVE_ID_FIELD = new Field(QUOTE_CURRENCY_CURVE_ID);
	private static final Field ID_FIELD = new Field(ID);

	private static final Field[] FXC_FIELDS = { PRIMARY_CURRENCY_ID_FIELD, QUOTE_CURRENCY_ID_FIELD,
			PRIMARY_CURRENCY_CURVE_ID_FIELD, QUOTE_CURRENCY_CURVE_ID_FIELD, ID_FIELD };

	private static final Field[] FXC_FIELDS_FOR_INSERT = { PRIMARY_CURRENCY_ID_FIELD, QUOTE_CURRENCY_ID_FIELD,
			PRIMARY_CURRENCY_CURVE_ID_FIELD, QUOTE_CURRENCY_CURVE_ID_FIELD };

	public static final Table FX_CURVE_TABLE = new Table("FX_CURVE", FXC_FIELDS);

	public static long saveFXCurve(String curveName) {
		long curveId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveCurve = TradistaDBUtil.buildInsertPreparedStatement(con, CurveSQL.CURVE_TABLE,
						CurveSQL.NAME_FIELD);
				PreparedStatement stmtSaveFXCurve = TradistaDBUtil.buildInsertPreparedStatement(con, FX_CURVE_TABLE,
						ID_FIELD)) {
			stmtSaveCurve.setString(1, curveName);
			stmtSaveCurve.executeUpdate();
			try (ResultSet generatedKeys = stmtSaveCurve.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					curveId = generatedKeys.getLong(1);
				} else {
					throw new SQLException("Creation of FX Curve failed, no generated key obtained.");
				}
			}
			stmtSaveFXCurve.setLong(1, curveId);
			stmtSaveFXCurve.executeUpdate();
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return curveId;
	}

	public static boolean deleteFXCurve(long curveId) {
		boolean bSaved = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteRatePoints = CurveSQL.getDeleteCurvePointsByCurveIdPreparedStatement(con);
				PreparedStatement stmtDeleteQuotesByCurveId = CurveSQL.getDeleteCurveQuotesByCurveIdPreparedStatement(con);
				PreparedStatement stmtDeleteFXCurve = TradistaDBUtil.buildDeletePreparedStatement(con, FX_CURVE_TABLE,
						ID_FIELD);
				PreparedStatement stmtDeleteCurve = TradistaDBUtil.buildDeletePreparedStatement(con,
						CurveSQL.CURVE_TABLE, CurveSQL.ID_FIELD)) {
			stmtDeleteRatePoints.setLong(1, curveId);
			stmtDeleteRatePoints.executeUpdate();
			stmtDeleteQuotesByCurveId.setLong(1, curveId);
			stmtDeleteQuotesByCurveId.executeUpdate();
			stmtDeleteFXCurve.setLong(1, curveId);
			stmtDeleteFXCurve.executeUpdate();
			stmtDeleteCurve.setLong(1, curveId);
			stmtDeleteCurve.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<FXCurve> getAllFXCurves() {
		Set<FXCurve> fxCurves = null;
		org.eclipse.tradista.core.common.persistence.util.Join join = org.eclipse.tradista.core.common.persistence.util.Join
				.innerEq(FX_CURVE_TABLE, CurveSQL.ID_FIELD, ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllFXCurves = con
						.prepareStatement(TradistaDBUtil.buildSelectQuery(CurveSQL.CURVE_TABLE, join));
				ResultSet results = stmtGetAllFXCurves.executeQuery()) {
			while (results.next()) {
				if (fxCurves == null) {
					fxCurves = new HashSet<FXCurve>();
				}
				fxCurves.add(buildFXCurve(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return fxCurves;
	}

	public static Set<FXCurve> getFXCurvesByPoId(long poId) {
		Set<FXCurve> fxCurves = null;
		org.eclipse.tradista.core.common.persistence.util.Join join = org.eclipse.tradista.core.common.persistence.util.Join
				.innerEq(FX_CURVE_TABLE, CurveSQL.ID_FIELD, ID_FIELD);
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(CurveSQL.CURVE_TABLE, join));
		TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.PROCESSING_ORG_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXCurvesByPoId = con.prepareStatement(sql.toString())) {
			stmtGetFXCurvesByPoId.setLong(1, poId);
			try (ResultSet results = stmtGetFXCurvesByPoId.executeQuery()) {
				while (results.next()) {
					if (fxCurves == null) {
						fxCurves = new HashSet<>();
					}
					fxCurves.add(buildFXCurve(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return fxCurves;
	}

	public static List<RatePoint> getAllFXCurvePointsByCurveIdAndDate(long curveId, Year year, Month month) {
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

	public static List<RatePoint> getFXCurvePointsByCurveIdAndDates(long curveId, LocalDate min, LocalDate max) {
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
						points = new ArrayList<RatePoint>();
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

	public static boolean saveFXCurvePoints(long id, List<RatePoint> ratePoints, Year year, Month month) {
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

	public static long saveFXCurve(FXCurve curve) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveCurve = (curve.getId() == 0) ? CurveSQL.getInsertCurvePreparedStatement(con)
						: CurveSQL.getUpdateCurvePreparedStatement(con);
				PreparedStatement stmtSaveFXCurve = (curve.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, FX_CURVE_TABLE, FXC_FIELDS)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, FX_CURVE_TABLE,
								FXC_FIELDS_FOR_INSERT)) {

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
						throw new SQLException("Creating FX curve failed, no generated key obtained.");
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
			stmtSaveFXCurve.setLong(5, curve.getId());
			stmtSaveFXCurve.executeUpdate();

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

	public static FXCurve getFXCurveByName(String curveName) {
		FXCurve fxCurve = null;
		org.eclipse.tradista.core.common.persistence.util.Join join = org.eclipse.tradista.core.common.persistence.util.Join
				.innerEq(FX_CURVE_TABLE, CurveSQL.ID_FIELD, ID_FIELD);
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(CurveSQL.CURVE_TABLE, join));
		TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.NAME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXCurveByName = con.prepareStatement(sql.toString())) {
			stmtGetFXCurveByName.setString(1, curveName);
			try (ResultSet results = stmtGetFXCurveByName.executeQuery()) {
				while (results.next()) {
					fxCurve = buildFXCurve(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return fxCurve;
	}

	public static FXCurve getFXCurveByNameAndPo(String curveName, long poId) {
		FXCurve fxCurve = null;
		org.eclipse.tradista.core.common.persistence.util.Join join = org.eclipse.tradista.core.common.persistence.util.Join
				.innerEq(FX_CURVE_TABLE, CurveSQL.ID_FIELD, ID_FIELD);
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(CurveSQL.CURVE_TABLE, join));
		TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.NAME_FIELD);
		if (poId > 0) {
			TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.PROCESSING_ORG_ID_FIELD);
		} else {
			TradistaDBUtil.addIsNullFilter(sql, CurveSQL.PROCESSING_ORG_ID_FIELD);
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXCurveByNameAndPo = con.prepareStatement(sql.toString())) {
			stmtGetFXCurveByNameAndPo.setString(1, curveName);
			if (poId > 0) {
				stmtGetFXCurveByNameAndPo.setLong(2, poId);
			}
			try (ResultSet results = stmtGetFXCurveByNameAndPo.executeQuery()) {
				while (results.next()) {
					fxCurve = buildFXCurve(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return fxCurve;
	}

	public static FXCurve getFXCurveById(long curveId) {
		FXCurve fxCurve = null;
		org.eclipse.tradista.core.common.persistence.util.Join join = org.eclipse.tradista.core.common.persistence.util.Join
				.innerEq(FX_CURVE_TABLE, CurveSQL.ID_FIELD, ID_FIELD);
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(CurveSQL.CURVE_TABLE, join));
		TradistaDBUtil.addParameterizedFilter(sql, CurveSQL.ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFXCurveById = con.prepareStatement(sql.toString())) {
			stmtGetFXCurveById.setLong(1, curveId);
			try (ResultSet results = stmtGetFXCurveById.executeQuery()) {
				while (results.next()) {
					fxCurve = buildFXCurve(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return fxCurve;
	}

	public static List<RatePoint> getFXCurvePointsByCurveId(long curveId) {
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

	private static FXCurve buildFXCurve(ResultSet results) throws SQLException {
		long poId = results.getLong(CurveSQL.PROCESSING_ORG_ID_FIELD.getName());
		LegalEntity processingOrg = null;
		if (poId > 0) {
			processingOrg = LegalEntitySQL.getLegalEntityById(poId);
		}
		FXCurve fxCurve = new FXCurve(results.getString(CurveSQL.NAME_FIELD.getName()), processingOrg);
		fxCurve.setId(results.getLong(CurveSQL.ID_FIELD.getName()));
		fxCurve.setAlgorithm(results.getString(CurveSQL.ALGORITHM_FIELD.getName()));
		fxCurve.setInterpolator(results.getString(CurveSQL.INTERPOLATOR_FIELD.getName()));
		fxCurve.setInstance(results.getString(CurveSQL.INSTANCE_FIELD.getName()));
		long primaryCurrencyId = results.getLong(PRIMARY_CURRENCY_ID_FIELD.getName());
		long quoteCurrencyId = results.getLong(QUOTE_CURRENCY_ID_FIELD.getName());
		long primaryCurrencyIRCurveId = results.getLong(PRIMARY_CURRENCY_CURVE_ID_FIELD.getName());
		long quoteCurrencyIRCurveId = results.getLong(QUOTE_CURRENCY_CURVE_ID_FIELD.getName());
		long quoteSetId = results.getLong(CurveSQL.QUOTE_SET_ID_FIELD.getName());

		java.sql.Date quoteDate = results.getDate(CurveSQL.QUOTE_DATE_FIELD.getName());

		if (primaryCurrencyId != 0) {
			fxCurve.setPrimaryCurrency(CurrencySQL.getCurrencyById(primaryCurrencyId));
		}

		if (quoteCurrencyId != 0) {
			fxCurve.setQuoteCurrency(CurrencySQL.getCurrencyById(quoteCurrencyId));
		}
		if (primaryCurrencyIRCurveId != 0) {
			fxCurve.setPrimaryCurrencyIRCurve(InterestRateCurveSQL.getInterestRateCurveById(primaryCurrencyIRCurveId));
		}
		if (quoteCurrencyIRCurveId != 0) {
			fxCurve.setQuoteCurrencyIRCurve(InterestRateCurveSQL.getInterestRateCurveById(quoteCurrencyIRCurveId));
		}
		if (quoteSetId != 0) {
			fxCurve.setQuoteSet(QuoteSetSQL.getQuoteSetById(quoteSetId));
		}
		if (quoteDate != null) {
			fxCurve.setQuoteDate(quoteDate.toLocalDate());
		}
		return fxCurve;
	}
}