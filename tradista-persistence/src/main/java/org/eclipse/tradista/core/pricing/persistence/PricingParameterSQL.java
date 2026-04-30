package org.eclipse.tradista.core.pricing.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CURRENCY_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.FX_CURVE_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.INDEX_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.INTEREST_RATE_CURVE_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRICER_NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRICING_PARAMETER_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRIMARY_CURRENCY_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PROCESSING_ORG_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRODUCT_TYPE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUOTE_CURRENCY_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUOTE_SET_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.VALUE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.persistence.FXCurveSQL;
import org.eclipse.tradista.core.marketdata.persistence.InterestRateCurveSQL;
import org.eclipse.tradista.core.marketdata.persistence.QuoteSetSQL;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;

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

public class PricingParameterSQL {

	private static final Logger logger = LoggerFactory.getLogger(PricingParameterSQL.class);

	// ---- PRICING_PARAMETER table ----
	private static final Field PP_NAME_FIELD = new Field(NAME);
	private static final Field PP_QUOTE_SET_ID_FIELD = new Field(QUOTE_SET_ID);
	private static final Field PP_PROCESSING_ORG_ID_FIELD = new Field(PROCESSING_ORG_ID);
	private static final Field PP_ID_FIELD = new Field(ID);

	private static final Field[] PP_FIELDS = { PP_NAME_FIELD, PP_QUOTE_SET_ID_FIELD, PP_PROCESSING_ORG_ID_FIELD,
			PP_ID_FIELD };
	private static final Field[] PP_FIELDS_FOR_INSERT = { PP_NAME_FIELD, PP_QUOTE_SET_ID_FIELD,
			PP_PROCESSING_ORG_ID_FIELD };

	public static final Table TABLE = new Table("PRICING_PARAMETER", PP_FIELDS);

	// ---- PRICING_PARAMETER_VALUE table ----
	private static final Field PPV_PRICING_PARAMETER_ID_FIELD = new Field(PRICING_PARAMETER_ID);
	private static final Field PPV_NAME_FIELD = new Field(NAME);
	private static final Field PPV_VALUE_FIELD = new Field(VALUE);

	private static final Field[] PPV_FIELDS = { PPV_PRICING_PARAMETER_ID_FIELD, PPV_NAME_FIELD, PPV_VALUE_FIELD };

	private static final Table PPV_TABLE = new Table("PRICING_PARAMETER_VALUE", PPV_FIELDS);

	// ---- PRICING_PARAMETER_INDEX_CURVE table ----
	private static final Field PPIC_PRICING_PARAMETER_ID_FIELD = new Field(PRICING_PARAMETER_ID);
	private static final Field PPIC_INDEX_ID_FIELD = new Field(INDEX_ID);
	private static final Field PPIC_INTEREST_RATE_CURVE_ID_FIELD = new Field(INTEREST_RATE_CURVE_ID);

	private static final Field[] PPIC_FIELDS = { PPIC_PRICING_PARAMETER_ID_FIELD, PPIC_INDEX_ID_FIELD,
			PPIC_INTEREST_RATE_CURVE_ID_FIELD };

	private static final Table PPIC_TABLE = new Table("PRICING_PARAMETER_INDEX_CURVE", PPIC_FIELDS);

	// ---- PRICING_PARAMETER_DISCOUNT_CURVE table ----
	private static final Field PPDC_PRICING_PARAMETER_ID_FIELD = new Field(PRICING_PARAMETER_ID);
	private static final Field PPDC_CURRENCY_ID_FIELD = new Field(CURRENCY_ID);
	private static final Field PPDC_INTEREST_RATE_CURVE_ID_FIELD = new Field(INTEREST_RATE_CURVE_ID);

	private static final Field[] PPDC_FIELDS = { PPDC_PRICING_PARAMETER_ID_FIELD, PPDC_CURRENCY_ID_FIELD,
			PPDC_INTEREST_RATE_CURVE_ID_FIELD };

	private static final Table PPDC_TABLE = new Table("PRICING_PARAMETER_DISCOUNT_CURVE", PPDC_FIELDS);

	// ---- PRICING_PARAMETER_FX_CURVE table ----
	private static final Field PPFXC_PRICING_PARAMETER_ID_FIELD = new Field(PRICING_PARAMETER_ID);
	private static final Field PPFXC_PRIMARY_CURRENCY_ID_FIELD = new Field(PRIMARY_CURRENCY_ID);
	private static final Field PPFXC_QUOTE_CURRENCY_ID_FIELD = new Field(QUOTE_CURRENCY_ID);
	private static final Field PPFXC_FX_CURVE_ID_FIELD = new Field(FX_CURVE_ID);

	private static final Field[] PPFXC_FIELDS = { PPFXC_PRICING_PARAMETER_ID_FIELD, PPFXC_PRIMARY_CURRENCY_ID_FIELD,
			PPFXC_QUOTE_CURRENCY_ID_FIELD, PPFXC_FX_CURVE_ID_FIELD };

	private static final Table PPFXC_TABLE = new Table("PRICING_PARAMETER_FX_CURVE", PPFXC_FIELDS);

	// ---- PRICING_PARAMETER_CUSTOM_PRICER table ----
	private static final Field PPCP_PRICING_PARAMETER_ID_FIELD = new Field(PRICING_PARAMETER_ID);
	private static final Field PPCP_PRODUCT_TYPE_FIELD = new Field(PRODUCT_TYPE);
	private static final Field PPCP_PRICER_NAME_FIELD = new Field(PRICER_NAME);

	private static final Field[] PPCP_FIELDS = { PPCP_PRICING_PARAMETER_ID_FIELD, PPCP_PRODUCT_TYPE_FIELD,
			PPCP_PRICER_NAME_FIELD };

	private static final Table PPCP_TABLE = new Table("PRICING_PARAMETER_CUSTOM_PRICER", PPCP_FIELDS);

	// ---- Module DAO classes ----
	private static Map<String, Class<?>> daoClasses = new HashMap<>();

	static {
		Class<?> daoClass = null;

		try {
			daoClass = TradistaUtil.getClass(
					"org.eclipse.tradista.security.equityoption.persistence.PricingParameterDividendYieldCurveSQL");
			daoClasses.put("org.eclipse.tradista.security.equityoption.model.PricingParameterDividendYieldCurveModule",
					daoClass);
		} catch (TradistaTechnicalException _) {
			logger.info("PricingParameterDividendYieldCurveSQL module not found, skipping.");
		}
		try {
			daoClass = TradistaUtil
					.getClass("org.eclipse.tradista.fx.common.persistence.PricingParameterUnrealizedPnlCalculationSQL");
			daoClasses.put("org.eclipse.tradista.fx.common.model.PricingParameterUnrealizedPnlCalculationModule",
					daoClass);
		} catch (TradistaTechnicalException _) {
			logger.info("PricingParameterUnrealizedPnlCalculationSQL module not found, skipping.");
		}
		try {
			daoClass = TradistaUtil
					.getClass("org.eclipse.tradista.fx.fxoption.persistence.PricingParameterVolatilitySurfaceSQL");
			daoClasses.put("org.eclipse.tradista.fx.fxoption.model.PricingParameterVolatilitySurfaceModule", daoClass);
		} catch (TradistaTechnicalException _) {
			logger.info("FX Option PricingParameterVolatilitySurfaceSQL module not found, skipping.");
		}
		try {
			daoClass = TradistaUtil
					.getClass("org.eclipse.tradista.ir.irswapoption.persistence.PricingParameterVolatilitySurfaceSQL");
			daoClasses.put("org.eclipse.tradista.ir.irswapoption.model.PricingParameterVolatilitySurfaceModule",
					daoClass);
		} catch (TradistaTechnicalException _) {
			logger.info("IR Swap Option PricingParameterVolatilitySurfaceSQL module not found, skipping.");
		}
		try {
			daoClass = TradistaUtil.getClass(
					"org.eclipse.tradista.security.equityoption.persistence.PricingParameterVolatilitySurfaceSQL");
			daoClasses.put("org.eclipse.tradista.security.equityoption.model.PricingParameterVolatilitySurfaceModule",
					daoClass);
		} catch (TradistaTechnicalException _) {
			logger.info("Equity Option PricingParameterVolatilitySurfaceSQL module not found, skipping.");
		}
	}

	public static boolean deletePricingParameter(long id) {
		boolean bSaved = false;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeletePricingParameterValuesById = TradistaDBUtil
						.buildDeletePreparedStatement(con, PPV_TABLE, PPV_PRICING_PARAMETER_ID_FIELD);
				PreparedStatement stmtDeletePricingParameterIndexCurvesById = TradistaDBUtil
						.buildDeletePreparedStatement(con, PPIC_TABLE, PPIC_PRICING_PARAMETER_ID_FIELD);
				PreparedStatement stmtDeletePricingParameterDiscountCurveById = TradistaDBUtil
						.buildDeletePreparedStatement(con, PPDC_TABLE, PPDC_PRICING_PARAMETER_ID_FIELD);
				PreparedStatement stmtDeletePricingParameterFXCurveById = TradistaDBUtil
						.buildDeletePreparedStatement(con, PPFXC_TABLE, PPFXC_PRICING_PARAMETER_ID_FIELD);
				PreparedStatement stmtDeletePricingParameterCustomPricerById = TradistaDBUtil
						.buildDeletePreparedStatement(con, PPCP_TABLE, PPCP_PRICING_PARAMETER_ID_FIELD);
				PreparedStatement stmtDeletePricingParameter = TradistaDBUtil.buildDeletePreparedStatement(con, TABLE,
						PP_ID_FIELD)) {
			stmtDeletePricingParameterValuesById.setLong(1, id);
			stmtDeletePricingParameterValuesById.executeUpdate();

			stmtDeletePricingParameterIndexCurvesById.setLong(1, id);
			stmtDeletePricingParameterIndexCurvesById.executeUpdate();

			stmtDeletePricingParameterDiscountCurveById.setLong(1, id);
			stmtDeletePricingParameterDiscountCurveById.executeUpdate();

			stmtDeletePricingParameterFXCurveById.setLong(1, id);
			stmtDeletePricingParameterFXCurveById.executeUpdate();

			stmtDeletePricingParameterCustomPricerById.setLong(1, id);
			stmtDeletePricingParameterCustomPricerById.executeUpdate();

			// Module deletion
			for (Class<?> daoClass : daoClasses.values()) {
				try {
					Method method = daoClass.getMethod("deletePricingParameterModule", Connection.class, long.class);
					method.invoke(daoClass, con, id);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					throw new TradistaTechnicalException(e);
				}
			}

			stmtDeletePricingParameter.setLong(1, id);
			stmtDeletePricingParameter.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<PricingParameter> getAllPricingParameters() {
		Set<PricingParameter> pricingParameters = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllPricingParameters = con
						.prepareStatement(TradistaDBUtil.buildSelectQuery(TABLE));
				ResultSet results = stmtGetAllPricingParameters.executeQuery()) {
			while (results.next()) {
				if (pricingParameters == null) {
					pricingParameters = new HashSet<>();
				}
				pricingParameters.add(buildPricingParameter(results, con));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return pricingParameters;
	}

	public static Set<PricingParameter> getPricingParametersByPoId(long poId) {
		Set<PricingParameter> pricingParameters = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PP_PROCESSING_ORG_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPricingParametersByPoId = con.prepareStatement(sql.toString())) {
			stmtGetPricingParametersByPoId.setLong(1, poId);
			try (ResultSet results = stmtGetPricingParametersByPoId.executeQuery()) {
				while (results.next()) {
					if (pricingParameters == null) {
						pricingParameters = new HashSet<>();
					}
					pricingParameters.add(buildPricingParameter(results, con));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return pricingParameters;
	}

	public static PricingParameter getPricingParameterById(long id) {
		PricingParameter pricingParameter = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PP_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPricingParameterById = con.prepareStatement(sql.toString())) {
			stmtGetPricingParameterById.setLong(1, id);
			try (ResultSet results = stmtGetPricingParameterById.executeQuery()) {
				while (results.next()) {
					pricingParameter = buildPricingParameter(results, con);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return pricingParameter;
	}

	public static PricingParameter getPricingParameterByNameAndPoId(String name, long poId) {
		PricingParameter pricingParameter = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PP_NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, PP_PROCESSING_ORG_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPricingParameterByNameAndPoId = con.prepareStatement(sql.toString())) {
			stmtGetPricingParameterByNameAndPoId.setString(1, name);
			stmtGetPricingParameterByNameAndPoId.setLong(2, poId);
			try (ResultSet results = stmtGetPricingParameterByNameAndPoId.executeQuery()) {
				while (results.next()) {
					pricingParameter = buildPricingParameter(results, con);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return pricingParameter;
	}

	public static long savePricingParameter(PricingParameter pricingParam) {
		long pricingParamId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSavePricingParameterValues = TradistaDBUtil.buildInsertPreparedStatement(con,
						PPV_TABLE, PPV_FIELDS);
				PreparedStatement stmtSavePricingParameterIndexCurves = TradistaDBUtil.buildInsertPreparedStatement(con,
						PPIC_TABLE, PPIC_FIELDS);
				PreparedStatement stmtSavePricingParameterDiscountCurves = TradistaDBUtil
						.buildInsertPreparedStatement(con, PPDC_TABLE, PPDC_FIELDS);
				PreparedStatement stmtSavePricingParameterFXCurves = TradistaDBUtil.buildInsertPreparedStatement(con,
						PPFXC_TABLE, PPFXC_FIELDS);
				PreparedStatement stmtSavePricingParameterCustomPricers = TradistaDBUtil
						.buildInsertPreparedStatement(con, PPCP_TABLE, PPCP_FIELDS);
				PreparedStatement stmtSavePricingParameter = (pricingParam.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, TABLE, PP_FIELDS_FOR_INSERT)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, PP_ID_FIELD, TABLE, PP_FIELDS_FOR_INSERT)) {

			stmtSavePricingParameter.setString(1, pricingParam.getName());
			stmtSavePricingParameter.setLong(2, pricingParam.getQuoteSet().getId());
			LegalEntity po = pricingParam.getProcessingOrg();
			if (po == null) {
				stmtSavePricingParameter.setNull(3, Types.BIGINT);
			} else {
				stmtSavePricingParameter.setLong(3, po.getId());
			}
			if (pricingParam.getId() != 0) {
				stmtSavePricingParameter.setLong(4, pricingParam.getId());
			}
			stmtSavePricingParameter.executeUpdate();
			if (pricingParam.getId() == 0) {
				try (ResultSet generatedKeys = stmtSavePricingParameter.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						pricingParamId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating pricing parameter failed, no generated key obtained.");
					}
				}
			} else {
				pricingParamId = pricingParam.getId();
			}

			if (pricingParam.getId() != 0) {
				// Then, we delete the data for this pricingParam
				try (PreparedStatement stmtDeletePricingParameterValues = TradistaDBUtil
						.buildDeletePreparedStatement(con, PPV_TABLE, PPV_PRICING_PARAMETER_ID_FIELD)) {
					stmtDeletePricingParameterValues.setLong(1, pricingParamId);
					stmtDeletePricingParameterValues.executeUpdate();
				}
				try (PreparedStatement stmtDeletePricingParameterIndexCurves = TradistaDBUtil
						.buildDeletePreparedStatement(con, PPIC_TABLE, PPIC_PRICING_PARAMETER_ID_FIELD)) {
					stmtDeletePricingParameterIndexCurves.setLong(1, pricingParamId);
					stmtDeletePricingParameterIndexCurves.executeUpdate();
				}
				try (PreparedStatement stmtDeletePricingParameterDiscountCurves = TradistaDBUtil
						.buildDeletePreparedStatement(con, PPDC_TABLE, PPDC_PRICING_PARAMETER_ID_FIELD)) {
					stmtDeletePricingParameterDiscountCurves.setLong(1, pricingParamId);
					stmtDeletePricingParameterDiscountCurves.executeUpdate();
				}
				try (PreparedStatement stmtDeletePricingParameterFXCurves = TradistaDBUtil
						.buildDeletePreparedStatement(con, PPFXC_TABLE, PPFXC_PRICING_PARAMETER_ID_FIELD)) {
					stmtDeletePricingParameterFXCurves.setLong(1, pricingParamId);
					stmtDeletePricingParameterFXCurves.executeUpdate();
				}
				try (PreparedStatement stmtDeletePricingParameterCustomPricers = TradistaDBUtil
						.buildDeletePreparedStatement(con, PPCP_TABLE, PPCP_PRICING_PARAMETER_ID_FIELD)) {
					stmtDeletePricingParameterCustomPricers.setLong(1, pricingParamId);
					stmtDeletePricingParameterCustomPricers.executeUpdate();
				}
			}

			for (Map.Entry<String, String> entry : pricingParam.getParams().entrySet()) {
				stmtSavePricingParameterValues.clearParameters();
				stmtSavePricingParameterValues.setLong(1, pricingParamId);
				stmtSavePricingParameterValues.setString(2, entry.getKey());
				stmtSavePricingParameterValues.setString(3, entry.getValue());
				stmtSavePricingParameterValues.addBatch();
			}
			stmtSavePricingParameterValues.executeBatch();

			for (Map.Entry<Index, InterestRateCurve> entry : pricingParam.getIndexCurves().entrySet()) {
				stmtSavePricingParameterIndexCurves.clearParameters();
				stmtSavePricingParameterIndexCurves.setLong(1, pricingParamId);
				stmtSavePricingParameterIndexCurves.setLong(2, entry.getKey().getId());
				stmtSavePricingParameterIndexCurves.setLong(3, entry.getValue().getId());
				stmtSavePricingParameterIndexCurves.addBatch();
			}
			stmtSavePricingParameterIndexCurves.executeBatch();

			for (Map.Entry<Currency, InterestRateCurve> entry : pricingParam.getDiscountCurves().entrySet()) {
				stmtSavePricingParameterDiscountCurves.clearParameters();
				stmtSavePricingParameterDiscountCurves.setLong(1, pricingParamId);
				stmtSavePricingParameterDiscountCurves.setLong(2, entry.getKey().getId());
				stmtSavePricingParameterDiscountCurves.setLong(3, entry.getValue().getId());
				stmtSavePricingParameterDiscountCurves.addBatch();
			}
			stmtSavePricingParameterDiscountCurves.executeBatch();

			for (Map.Entry<CurrencyPair, FXCurve> entry : pricingParam.getFxCurves().entrySet()) {
				stmtSavePricingParameterFXCurves.clearParameters();
				stmtSavePricingParameterFXCurves.setLong(1, pricingParamId);
				stmtSavePricingParameterFXCurves.setLong(2, entry.getKey().getPrimaryCurrency().getId());
				stmtSavePricingParameterFXCurves.setLong(3, entry.getKey().getQuoteCurrency().getId());
				stmtSavePricingParameterFXCurves.setLong(4, entry.getValue().getId());
				stmtSavePricingParameterFXCurves.addBatch();
			}
			stmtSavePricingParameterFXCurves.executeBatch();

			for (Map.Entry<String, String> entry : pricingParam.getCustomPricers().entrySet()) {
				stmtSavePricingParameterCustomPricers.clearParameters();
				stmtSavePricingParameterCustomPricers.setLong(1, pricingParamId);
				stmtSavePricingParameterCustomPricers.setString(2, entry.getKey());
				stmtSavePricingParameterCustomPricers.setString(3, entry.getValue());
				stmtSavePricingParameterCustomPricers.addBatch();
			}
			stmtSavePricingParameterCustomPricers.executeBatch();

			if (pricingParam.getModules() != null && !pricingParam.getModules().isEmpty()) {
				for (PricingParameterModule module : pricingParam.getModules()) {
					// Save the module
					PricingParameterSQL.savePricingParameterModule(module, con, pricingParamId);
				}
			}

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return pricingParamId;
	}

	private static void savePricingParameterModule(PricingParameterModule module, Connection con, long pricingParamId) {
		// Get the right DAO
		Class<?> daoClass = daoClasses.get(module.getClass().getName());
		try {
			Method method = daoClass.getMethod("savePricingParameterModule", Connection.class, module.getClass(),
					long.class);
			method.invoke(daoClass, con, module, pricingParamId);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new TradistaTechnicalException(e);
		}
	}

	public static Set<String> getPricingParametersSetByQuoteSetId(long quoteSetId) {
		Set<String> pricingParametersSetNames = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(PP_NAME_FIELD, TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PP_QUOTE_SET_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPricingParametersSetsByQuoteSetId = con.prepareStatement(sql.toString())) {
			stmtGetPricingParametersSetsByQuoteSetId.setLong(1, quoteSetId);
			try (ResultSet results = stmtGetPricingParametersSetsByQuoteSetId.executeQuery()) {
				while (results.next()) {
					if (pricingParametersSetNames == null) {
						pricingParametersSetNames = new HashSet<>();
					}
					pricingParametersSetNames.add(results.getString(PP_NAME_FIELD.getName()));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return pricingParametersSetNames;
	}

	private static PricingParameter buildPricingParameter(ResultSet results, Connection con) throws SQLException {
		LegalEntity processingOrg = null;
		long poId = results.getLong(PP_PROCESSING_ORG_ID_FIELD.getName());
		if (poId > 0) {
			processingOrg = LegalEntitySQL.getLegalEntityById(poId);
		}
		PricingParameter pricingParameter = new PricingParameter(results.getString(PP_NAME_FIELD.getName()),
				processingOrg);
		pricingParameter.setId(results.getInt(PP_ID_FIELD.getName()));
		pricingParameter.setQuoteSet(QuoteSetSQL.getQuoteSetById(results.getLong(PP_QUOTE_SET_ID_FIELD.getName())));

		long ppId = pricingParameter.getId();

		// Load parameter values
		Map<String, String> params = new HashMap<>();
		StringBuilder ppvSql = new StringBuilder(TradistaDBUtil.buildSelectQuery(PPV_TABLE));
		TradistaDBUtil.addParameterizedFilter(ppvSql, PPV_PRICING_PARAMETER_ID_FIELD);
		try (PreparedStatement stmtGetValues = con.prepareStatement(ppvSql.toString())) {
			stmtGetValues.setLong(1, ppId);
			try (ResultSet valuesResults = stmtGetValues.executeQuery()) {
				while (valuesResults.next()) {
					params.put(valuesResults.getString(PPV_NAME_FIELD.getName()),
							valuesResults.getString(PPV_VALUE_FIELD.getName()));
				}
			}
		}
		pricingParameter.setParams(params);

		// Load index curves
		Map<Index, InterestRateCurve> indexCurves = new HashMap<>();
		StringBuilder ppicSql = new StringBuilder(TradistaDBUtil.buildSelectQuery(PPIC_TABLE));
		TradistaDBUtil.addParameterizedFilter(ppicSql, PPIC_PRICING_PARAMETER_ID_FIELD);
		try (PreparedStatement stmtGetIndexCurves = con.prepareStatement(ppicSql.toString())) {
			stmtGetIndexCurves.setLong(1, ppId);
			try (ResultSet indexCurvesResults = stmtGetIndexCurves.executeQuery()) {
				while (indexCurvesResults.next()) {
					Index index = IndexSQL.getIndexById(indexCurvesResults.getLong(PPIC_INDEX_ID_FIELD.getName()));
					InterestRateCurve curve = InterestRateCurveSQL.getInterestRateCurveById(
							indexCurvesResults.getLong(PPIC_INTEREST_RATE_CURVE_ID_FIELD.getName()));
					indexCurves.put(index, curve);
				}
			}
		}
		pricingParameter.setIndexCurves(indexCurves);

		// Load discount curves
		Map<Currency, InterestRateCurve> discountCurves = new HashMap<>();
		StringBuilder ppdcSql = new StringBuilder(TradistaDBUtil.buildSelectQuery(PPDC_TABLE));
		TradistaDBUtil.addParameterizedFilter(ppdcSql, PPDC_PRICING_PARAMETER_ID_FIELD);
		try (PreparedStatement stmtGetDiscountCurves = con.prepareStatement(ppdcSql.toString())) {
			stmtGetDiscountCurves.setLong(1, ppId);
			try (ResultSet discountCurvesResults = stmtGetDiscountCurves.executeQuery()) {
				while (discountCurvesResults.next()) {
					Currency currency = CurrencySQL
							.getCurrencyById(discountCurvesResults.getLong(PPDC_CURRENCY_ID_FIELD.getName()));
					InterestRateCurve curve = InterestRateCurveSQL.getInterestRateCurveById(
							discountCurvesResults.getLong(PPDC_INTEREST_RATE_CURVE_ID_FIELD.getName()));
					discountCurves.put(currency, curve);
				}
			}
		}
		pricingParameter.setDiscountCurves(discountCurves);

		// Load FX curves
		Map<CurrencyPair, FXCurve> fxCurves = new HashMap<>();
		StringBuilder ppfxcSql = new StringBuilder(TradistaDBUtil.buildSelectQuery(PPFXC_TABLE));
		TradistaDBUtil.addParameterizedFilter(ppfxcSql, PPFXC_PRICING_PARAMETER_ID_FIELD);
		try (PreparedStatement stmtGetFXCurves = con.prepareStatement(ppfxcSql.toString())) {
			stmtGetFXCurves.setLong(1, ppId);
			try (ResultSet fxCurvesResults = stmtGetFXCurves.executeQuery()) {
				while (fxCurvesResults.next()) {
					Currency primaryCurrency = CurrencySQL
							.getCurrencyById(fxCurvesResults.getLong(PPFXC_PRIMARY_CURRENCY_ID_FIELD.getName()));
					Currency quoteCurrency = CurrencySQL
							.getCurrencyById(fxCurvesResults.getLong(PPFXC_QUOTE_CURRENCY_ID_FIELD.getName()));
					FXCurve curve = FXCurveSQL
							.getFXCurveById(fxCurvesResults.getLong(PPFXC_FX_CURVE_ID_FIELD.getName()));
					fxCurves.put(new CurrencyPair(primaryCurrency, quoteCurrency), curve);
				}
			}
		}
		pricingParameter.setFxCurves(fxCurves);

		// Load custom pricers
		Map<String, String> customPricers = new HashMap<>();
		StringBuilder ppcpSql = new StringBuilder(TradistaDBUtil.buildSelectQuery(PPCP_TABLE));
		TradistaDBUtil.addParameterizedFilter(ppcpSql, PPCP_PRICING_PARAMETER_ID_FIELD);
		try (PreparedStatement stmtGetCustomPricers = con.prepareStatement(ppcpSql.toString())) {
			stmtGetCustomPricers.setLong(1, ppId);
			try (ResultSet customPricersResults = stmtGetCustomPricers.executeQuery()) {
				while (customPricersResults.next()) {
					customPricers.put(customPricersResults.getString(PPCP_PRODUCT_TYPE_FIELD.getName()),
							customPricersResults.getString(PPCP_PRICER_NAME_FIELD.getName()));
				}
			}
		}
		pricingParameter.setCustomPricers(customPricers);

		// Load modules
		for (Class<?> daoClass : daoClasses.values()) {
			try {
				Method method = daoClass.getMethod("getPricingParameterModuleByPricingParameterId", Connection.class,
						long.class);
				PricingParameterModule module = (PricingParameterModule) method.invoke(daoClass, con,
						pricingParameter.getId());
				if (module != null) {
					pricingParameter.addModule(module);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				throw new TradistaTechnicalException(e);
			}
		}

		return pricingParameter;
	}

}