package org.eclipse.tradista.core.pricing.persistence;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
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

	private static Map<String, Class<?>> daoClasses = new HashMap<>();

	static {
		Class<?> daoClass = null;

		try {
			daoClass = TradistaUtil.getClass(
					"org.eclipse.tradista.security.equityoption.persistence.PricingParameterDividendYieldCurveSQL");
			daoClasses.put("org.eclipse.tradista.security.equityoption.model.PricingParameterDividendYieldCurveModule",
					daoClass);
		} catch (TradistaTechnicalException tte) {
			// TODO Add log info
		}
		try {
			daoClass = TradistaUtil
					.getClass("org.eclipse.tradista.fx.common.persistence.PricingParameterUnrealizedPnlCalculationSQL");
			daoClasses.put("org.eclipse.tradista.fx.common.model.PricingParameterUnrealizedPnlCalculationModule", daoClass);
		} catch (TradistaTechnicalException tte) {
			// TODO Add log info
		}
		try {
			daoClass = TradistaUtil
					.getClass("org.eclipse.tradista.fx.fxoption.persistence.PricingParameterVolatilitySurfaceSQL");
			daoClasses.put("org.eclipse.tradista.fx.fxoption.model.PricingParameterVolatilitySurfaceModule", daoClass);
		} catch (TradistaTechnicalException tte) {
			// TODO Add log info
		}
		try {
			daoClass = TradistaUtil
					.getClass("org.eclipse.tradista.ir.irswapoption.persistence.PricingParameterVolatilitySurfaceSQL");
			daoClasses.put("org.eclipse.tradista.ir.irswapoption.model.PricingParameterVolatilitySurfaceModule", daoClass);
		} catch (TradistaTechnicalException tte) {
			// TODO Add log info
		}
		try {
			daoClass = TradistaUtil.getClass(
					"org.eclipse.tradista.security.equityoption.persistence.PricingParameterVolatilitySurfaceSQL");
			daoClasses.put("org.eclipse.tradista.security.equityoption.model.PricingParameterVolatilitySurfaceModule",
					daoClass);
		} catch (TradistaTechnicalException tte) {
			// TODO Add log info
		}
	}

	public static boolean deletePricingParameter(long id) {
		boolean bSaved = false;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeletePricingParameterValuesById = con
						.prepareStatement("DELETE FROM PRICING_PARAMETER_VALUE WHERE PRICING_PARAMETER_ID = ?");
				PreparedStatement stmtDeletePricingParameterIndexCurvesById = con
						.prepareStatement("DELETE FROM PRICING_PARAMETER_INDEX_CURVE WHERE PRICING_PARAMETER_ID = ?");
				PreparedStatement stmtDeletePricingParameterDiscountCurveById = con.prepareStatement(
						"DELETE FROM PRICING_PARAMETER_DISCOUNT_CURVE WHERE PRICING_PARAMETER_ID = ?");
				PreparedStatement stmtDeletePricingParameterFXCurveById = con
						.prepareStatement("DELETE FROM PRICING_PARAMETER_FX_CURVE WHERE PRICING_PARAMETER_ID = ?");
				PreparedStatement stmtDeletePricingParameterCustomPricerById = con
						.prepareStatement("DELETE FROM PRICING_PARAMETER_CUSTOM_PRICER WHERE PRICING_PARAMETER_ID = ?");
				PreparedStatement stmtDeletePricingParameter = con
						.prepareStatement("DELETE FROM PRICING_PARAMETER WHERE ID = ?")) {
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
					e.printStackTrace();
					throw new TradistaTechnicalException(e);
				}
			}

			stmtDeletePricingParameter.setLong(1, id);
			stmtDeletePricingParameter.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<PricingParameter> getAllPricingParameters() {
		Set<PricingParameter> pricingParameters = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllPricingParameters = con.prepareStatement("SELECT * FROM PRICING_PARAMETER");
				PreparedStatement stmtGetPricingParameterValueByPricingParameterId = con.prepareStatement(
						"SELECT PRICING_PARAMETER_VALUE.NAME NAME, PRICING_PARAMETER_VALUE.VALUE VALUE "
								+ "FROM PRICING_PARAMETER_VALUE WHERE "
								+ "PRICING_PARAMETER_VALUE.PRICING_PARAMETER_ID = ?");
				PreparedStatement stmtGetPricingParameterIndexCurveById = con
						.prepareStatement("SELECT * FROM PRICING_PARAMETER_INDEX_CURVE WHERE PRICING_PARAMETER_ID = ?");
				PreparedStatement stmtGetPricingParameterDiscountCurveById = con.prepareStatement(
						"SELECT * FROM PRICING_PARAMETER_DISCOUNT_CURVE WHERE PRICING_PARAMETER_ID = ?");
				PreparedStatement stmtGetPricingParameterFXCurveById = con
						.prepareStatement("SELECT * FROM PRICING_PARAMETER_FX_CURVE WHERE PRICING_PARAMETER_ID = ?");
				PreparedStatement stmtGetPricingParameterCustomPricerById = con.prepareStatement(
						"SELECT * FROM PRICING_PARAMETER_CUSTOM_PRICER WHERE PRICING_PARAMETER_ID = ?");
				ResultSet results = stmtGetAllPricingParameters.executeQuery()) {
			while (results.next()) {
				LegalEntity processingOrg = null;
				long poId = results.getLong("processing_org_id");
				if (poId > 0) {
					processingOrg = LegalEntitySQL.getLegalEntityById(poId);
				}
				PricingParameter pricingParameter = new PricingParameter(results.getString("name"), processingOrg);
				Map<String, String> params = new HashMap<>();
				Map<Index, InterestRateCurve> indexCurves = new HashMap<>();
				Map<Currency, InterestRateCurve> discountCurves = new HashMap<>();
				Map<CurrencyPair, FXCurve> fxCurves = new HashMap<>();
				Map<String, String> customPricers = new HashMap<>();
				pricingParameter.setId(results.getInt("id"));
				pricingParameter.setQuoteSet(QuoteSetSQL.getQuoteSetById(results.getLong("quote_set_id")));
				stmtGetPricingParameterValueByPricingParameterId.setLong(1, results.getLong("id"));
				try (ResultSet paramsResults = stmtGetPricingParameterValueByPricingParameterId.executeQuery()) {
					while (paramsResults.next()) {
						String name = paramsResults.getString("name");
						String value = paramsResults.getString("value");
						params.put(name, value);
					}
				}
				pricingParameter.setParams(params);

				stmtGetPricingParameterIndexCurveById.setLong(1, results.getLong("id"));
				try (ResultSet indexCurvesResults = stmtGetPricingParameterIndexCurveById.executeQuery()) {
					while (indexCurvesResults.next()) {
						Index index = IndexSQL.getIndexById(indexCurvesResults.getLong("index_id"));
						InterestRateCurve curve = InterestRateCurveSQL
								.getInterestRateCurveById(indexCurvesResults.getLong("interest_rate_curve_id"));
						indexCurves.put(index, curve);
					}
				}
				pricingParameter.setIndexCurves(indexCurves);

				stmtGetPricingParameterDiscountCurveById.setLong(1, results.getLong("id"));
				try (ResultSet discountCurvesResults = stmtGetPricingParameterDiscountCurveById.executeQuery()) {
					while (discountCurvesResults.next()) {
						Currency currency = CurrencySQL.getCurrencyById(discountCurvesResults.getLong("currency_id"));
						InterestRateCurve curve = InterestRateCurveSQL
								.getInterestRateCurveById(discountCurvesResults.getLong("interest_rate_curve_id"));
						discountCurves.put(currency, curve);
					}
				}
				pricingParameter.setDiscountCurves(discountCurves);

				stmtGetPricingParameterFXCurveById.setLong(1, results.getLong("id"));
				try (ResultSet fxCurvesResults = stmtGetPricingParameterFXCurveById.executeQuery()) {
					while (fxCurvesResults.next()) {
						Currency primaryCurrency = CurrencySQL
								.getCurrencyById(fxCurvesResults.getLong("primary_currency_id"));
						Currency quoteCurrency = CurrencySQL
								.getCurrencyById(fxCurvesResults.getLong("quote_currency_id"));
						FXCurve curve = FXCurveSQL.getFXCurveById(fxCurvesResults.getLong("fx_curve_id"));
						fxCurves.put(new CurrencyPair(primaryCurrency, quoteCurrency), curve);
					}
				}
				pricingParameter.setFxCurves(fxCurves);

				stmtGetPricingParameterCustomPricerById.setLong(1, results.getLong("id"));
				try (ResultSet customPricersResults = stmtGetPricingParameterCustomPricerById.executeQuery()) {
					while (customPricersResults.next()) {
						String productType = customPricersResults.getString("product_type");
						String pricer = customPricersResults.getString("pricer_name");
						customPricers.put(productType, pricer);
					}
				}
				pricingParameter.setCustomPricers(customPricers);

				for (Class<?> daoClass : daoClasses.values()) {
					try {
						Method method = daoClass.getMethod("getPricingParameterModuleByPricingParameterId",
								Connection.class, long.class);
						PricingParameterModule module = (PricingParameterModule) method.invoke(daoClass, con,
								pricingParameter.getId());
						pricingParameter.getModules().add(module);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
						throw new TradistaTechnicalException(e);
					}
				}

				if (pricingParameters == null) {
					pricingParameters = new HashSet<PricingParameter>();
				}
				pricingParameters.add(pricingParameter);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return pricingParameters;
	}

	public static PricingParameter getPricingParameterById(long id) {
		PricingParameter pricingParameter = null;
		Map<String, String> params = new HashMap<>();

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPricingParameterValuesByPricingParameterId = con.prepareStatement(
						"SELECT PRICING_PARAMETER_VALUE.NAME NAME, PRICING_PARAMETER_VALUE.VALUE VALUE "
								+ "FROM PRICING_PARAMETER_VALUE WHERE "
								+ "PRICING_PARAMETER_VALUE.PRICING_PARAMETER_ID = ?");
				PreparedStatement stmtGetPricingParameterById = con
						.prepareStatement("SELECT * FROM PRICING_PARAMETER WHERE ID = ?")) {
			stmtGetPricingParameterValuesByPricingParameterId.setLong(1, id);
			try (ResultSet results = stmtGetPricingParameterValuesByPricingParameterId.executeQuery()) {
				while (results.next()) {
					String name = results.getString("name");
					String value = results.getString("value");
					params.put(name, value);
				}
			}
			stmtGetPricingParameterById.setLong(1, id);
			try (PreparedStatement stmtGetPricingParameterIndexCurveById = con
					.prepareStatement("SELECT * FROM PRICING_PARAMETER_INDEX_CURVE WHERE PRICING_PARAMETER_ID = ?");
					PreparedStatement stmtGetPricingParameterDiscountCurveById = con.prepareStatement(
							"SELECT * FROM PRICING_PARAMETER_DISCOUNT_CURVE WHERE PRICING_PARAMETER_ID = ?");
					PreparedStatement stmtGetPricingParameterFXCurveById = con.prepareStatement(
							"SELECT * FROM PRICING_PARAMETER_FX_CURVE WHERE PRICING_PARAMETER_ID = ?");
					PreparedStatement stmtGetPricingParameterCustomPricerById = con.prepareStatement(
							"SELECT * FROM PRICING_PARAMETER_CUSTOM_PRICER WHERE PRICING_PARAMETER_ID = ?");
					ResultSet results = stmtGetPricingParameterById.executeQuery()) {
				while (results.next()) {
					if (pricingParameter == null) {
						LegalEntity processingOrg = null;
						long poId = results.getLong("processing_org_id");
						if (poId > 0) {
							processingOrg = LegalEntitySQL.getLegalEntityById(poId);
						}
						pricingParameter = new PricingParameter(results.getString("name"), processingOrg);
					}
					Map<Index, InterestRateCurve> indexCurves = new HashMap<>();
					Map<Currency, InterestRateCurve> discountCurves = new HashMap<>();
					Map<CurrencyPair, FXCurve> fxCurves = new HashMap<>();
					Map<String, String> customPricers = new HashMap<>();
					pricingParameter.setId(results.getInt("id"));
					pricingParameter.setQuoteSet(QuoteSetSQL.getQuoteSetById(results.getLong("quote_set_id")));
					stmtGetPricingParameterIndexCurveById.setLong(1, results.getLong("id"));
					try (ResultSet indexCurvesResults = stmtGetPricingParameterIndexCurveById.executeQuery()) {
						while (indexCurvesResults.next()) {
							Index index = IndexSQL.getIndexById(indexCurvesResults.getLong("index_id"));
							InterestRateCurve curve = InterestRateCurveSQL
									.getInterestRateCurveById(indexCurvesResults.getLong("interest_rate_curve_id"));
							indexCurves.put(index, curve);
						}
					}
					pricingParameter.setIndexCurves(indexCurves);

					stmtGetPricingParameterDiscountCurveById.setLong(1, results.getLong("id"));
					try (ResultSet discountCurvesResults = stmtGetPricingParameterDiscountCurveById.executeQuery()) {
						while (discountCurvesResults.next()) {
							Currency currency = CurrencySQL
									.getCurrencyById(discountCurvesResults.getLong("currency_id"));
							InterestRateCurve curve = InterestRateCurveSQL
									.getInterestRateCurveById(discountCurvesResults.getLong("interest_rate_curve_id"));
							discountCurves.put(currency, curve);
						}
					}
					pricingParameter.setDiscountCurves(discountCurves);

					stmtGetPricingParameterFXCurveById.setLong(1, results.getLong("id"));
					try (ResultSet fxCurvesResults = stmtGetPricingParameterFXCurveById.executeQuery()) {
						while (fxCurvesResults.next()) {
							Currency primaryCurrency = CurrencySQL
									.getCurrencyById(fxCurvesResults.getLong("primary_currency_id"));
							Currency quoteCurrency = CurrencySQL
									.getCurrencyById(fxCurvesResults.getLong("quote_currency_id"));
							FXCurve curve = FXCurveSQL.getFXCurveById(fxCurvesResults.getLong("fx_curve_id"));
							fxCurves.put(new CurrencyPair(primaryCurrency, quoteCurrency), curve);
						}
					}
					pricingParameter.setFxCurves(fxCurves);

					stmtGetPricingParameterCustomPricerById.setLong(1, results.getLong("id"));
					try (ResultSet customPricersResults = stmtGetPricingParameterCustomPricerById.executeQuery()) {
						while (customPricersResults.next()) {
							String productType = customPricersResults.getString("product_type");
							String pricer = customPricersResults.getString("pricer_name");
							customPricers.put(productType, pricer);
						}
					}
					pricingParameter.setCustomPricers(customPricers);

					for (Class<?> daoClass : daoClasses.values()) {
						try {
							Method method = daoClass.getMethod("getPricingParameterModuleByPricingParameterId",
									Connection.class, long.class);
							PricingParameterModule module = (PricingParameterModule) method.invoke(daoClass, con,
									pricingParameter.getId());
							pricingParameter.getModules().add(module);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
								| NoSuchMethodException | SecurityException e) {
							e.printStackTrace();
							throw new TradistaTechnicalException(e);
						}
					}

				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		pricingParameter.setParams(params);
		return pricingParameter;
	}

	public static PricingParameter getPricingParameterByNameAndPoId(String name, long poId) {
		PricingParameter pricingParameter = null;
		Map<String, String> params = new HashMap<>();

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPricingParameterByNameAndPoId = con
						.prepareStatement("SELECT * FROM PRICING_PARAMETER WHERE NAME = ? AND PROCESSING_ORG_ID = ?")) {
			stmtGetPricingParameterByNameAndPoId.setString(1, name);
			stmtGetPricingParameterByNameAndPoId.setLong(2, poId);
			try (PreparedStatement stmtGetPricingParameterIndexCurveById = con
					.prepareStatement("SELECT * FROM PRICING_PARAMETER_INDEX_CURVE WHERE PRICING_PARAMETER_ID = ?");
					PreparedStatement stmtGetPricingParameterDiscountCurveById = con.prepareStatement(
							"SELECT * FROM PRICING_PARAMETER_DISCOUNT_CURVE WHERE PRICING_PARAMETER_ID = ?");
					PreparedStatement stmtGetPricingParameterFXCurveById = con.prepareStatement(
							"SELECT * FROM PRICING_PARAMETER_FX_CURVE WHERE PRICING_PARAMETER_ID = ?");
					PreparedStatement stmtGetPricingParameterCustomPricerById = con.prepareStatement(
							"SELECT * FROM PRICING_PARAMETER_CUSTOM_PRICER WHERE PRICING_PARAMETER_ID = ?");
					ResultSet results = stmtGetPricingParameterByNameAndPoId.executeQuery()) {
				while (results.next()) {
					LegalEntity processingOrg = null;
					if (poId > 0) {
						processingOrg = LegalEntitySQL.getLegalEntityById(poId);
					}
					pricingParameter = new PricingParameter(results.getString("name"), processingOrg);
					Map<Index, InterestRateCurve> indexCurves = new HashMap<>();
					Map<Currency, InterestRateCurve> discountCurves = new HashMap<>();
					Map<CurrencyPair, FXCurve> fxCurves = new HashMap<>();
					Map<String, String> customPricers = new HashMap<>();
					pricingParameter.setId(results.getInt("id"));
					pricingParameter.setQuoteSet(QuoteSetSQL.getQuoteSetById(results.getLong("quote_set_id")));
					stmtGetPricingParameterIndexCurveById.setLong(1, results.getLong("id"));
					try (ResultSet indexCurvesResults = stmtGetPricingParameterIndexCurveById.executeQuery()) {
						while (indexCurvesResults.next()) {
							Index index = IndexSQL.getIndexById(indexCurvesResults.getLong("index_id"));
							InterestRateCurve curve = InterestRateCurveSQL
									.getInterestRateCurveById(indexCurvesResults.getLong("interest_rate_curve_id"));
							indexCurves.put(index, curve);
						}
					}
					pricingParameter.setIndexCurves(indexCurves);

					stmtGetPricingParameterDiscountCurveById.setLong(1, results.getLong("id"));
					try (ResultSet discountCurvesResults = stmtGetPricingParameterDiscountCurveById.executeQuery()) {
						while (discountCurvesResults.next()) {
							Currency currency = CurrencySQL
									.getCurrencyById(discountCurvesResults.getLong("currency_id"));
							InterestRateCurve curve = InterestRateCurveSQL
									.getInterestRateCurveById(discountCurvesResults.getLong("interest_rate_curve_id"));
							discountCurves.put(currency, curve);
						}
					}
					pricingParameter.setDiscountCurves(discountCurves);

					stmtGetPricingParameterFXCurveById.setLong(1, results.getLong("id"));
					try (ResultSet fxCurvesResults = stmtGetPricingParameterFXCurveById.executeQuery()) {
						while (fxCurvesResults.next()) {
							Currency primaryCurrency = CurrencySQL
									.getCurrencyById(fxCurvesResults.getLong("primary_currency_id"));
							Currency quoteCurrency = CurrencySQL
									.getCurrencyById(fxCurvesResults.getLong("quote_currency_id"));
							FXCurve curve = FXCurveSQL.getFXCurveById(fxCurvesResults.getLong("fx_curve_id"));
							fxCurves.put(new CurrencyPair(primaryCurrency, quoteCurrency), curve);
						}
					}
					pricingParameter.setFxCurves(fxCurves);

					stmtGetPricingParameterCustomPricerById.setLong(1, results.getLong("id"));
					try (ResultSet customPricersResults = stmtGetPricingParameterCustomPricerById.executeQuery()) {
						while (customPricersResults.next()) {
							String productType = customPricersResults.getString("product_type");
							String pricer = customPricersResults.getString("pricer_name");
							customPricers.put(productType, pricer);
						}
					}
					pricingParameter.setCustomPricers(customPricers);

					for (Class<?> daoClass : daoClasses.values()) {
						try {
							Method method = daoClass.getMethod("getPricingParameterModuleByPricingParameterId",
									Connection.class, long.class);
							PricingParameterModule module = (PricingParameterModule) method.invoke(daoClass, con,
									pricingParameter.getId());
							pricingParameter.getModules().add(module);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
								| NoSuchMethodException | SecurityException e) {
							e.printStackTrace();
							throw new TradistaTechnicalException(e);
						}
					}

				}
			}
			if (pricingParameter != null) {
				try (PreparedStatement stmtGetPricingParameterValuesByPricingParameterId = con.prepareStatement(
						"SELECT PRICING_PARAMETER_VALUE.NAME NAME, PRICING_PARAMETER_VALUE.VALUE VALUE "
								+ "FROM PRICING_PARAMETER_VALUE WHERE "
								+ "PRICING_PARAMETER_VALUE.PRICING_PARAMETER_ID = ?")) {
					stmtGetPricingParameterValuesByPricingParameterId.setLong(1, pricingParameter.getId());
					try (ResultSet results = stmtGetPricingParameterValuesByPricingParameterId.executeQuery()) {
						while (results.next()) {
							String paramName = results.getString("name");
							String paramValue = results.getString("value");
							params.put(paramName, paramValue);
						}
					}
					pricingParameter.setParams(params);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return pricingParameter;
	}

	public static long savePricingParameter(PricingParameter pricingParam) {
		long pricingParamId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSavePricingParameterValues = con.prepareStatement(
						"INSERT INTO PRICING_PARAMETER_VALUE(PRICING_PARAMETER_ID, NAME, VALUE) VALUES(?, ?, ?)");
				PreparedStatement stmtSavePricingParameterIndexCurves = con.prepareStatement(
						"INSERT INTO PRICING_PARAMETER_INDEX_CURVE(PRICING_PARAMETER_ID, INDEX_ID, INTEREST_RATE_CURVE_ID) VALUES(?, ?, ?)");
				PreparedStatement stmtSavePricingParameterDiscountCurves = con.prepareStatement(
						"INSERT INTO PRICING_PARAMETER_DISCOUNT_CURVE(PRICING_PARAMETER_ID, CURRENCY_ID, INTEREST_RATE_CURVE_ID) VALUES(?, ?, ?)");
				PreparedStatement stmtSavePricingParameterFXCurves = con.prepareStatement(
						"INSERT INTO PRICING_PARAMETER_FX_CURVE(PRICING_PARAMETER_ID, PRIMARY_CURRENCY_ID, QUOTE_CURRENCY_ID, FX_CURVE_ID) VALUES(?, ?, ?, ?)");
				PreparedStatement stmtSavePricingParameterCustomPricers = con.prepareStatement(
						"INSERT INTO PRICING_PARAMETER_CUSTOM_PRICER(PRICING_PARAMETER_ID, PRODUCT_TYPE, PRICER_NAME) VALUES(?, ?, ?)");
				PreparedStatement stmtSavePricingParameter = (pricingParam.getId() == 0)
						? con.prepareStatement(
								"INSERT INTO PRICING_PARAMETER(NAME, QUOTE_SET_ID, PROCESSING_ORG_ID) VALUES(?, ?, ?)",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE PRICING_PARAMETER SET NAME=?, QUOTE_SET_ID = ?, PROCESSING_ORG_ID=? WHERE ID=?")) {

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
				try (PreparedStatement stmtDeletePricingParameterValues = con
						.prepareStatement("DELETE FROM PRICING_PARAMETER_VALUE WHERE PRICING_PARAMETER_ID = ?")) {
					stmtDeletePricingParameterValues.setLong(1, pricingParamId);
					stmtDeletePricingParameterValues.executeUpdate();
				}
				try (PreparedStatement stmtDeletePricingParameterIndexCurves = con
						.prepareStatement("DELETE FROM PRICING_PARAMETER_INDEX_CURVE WHERE PRICING_PARAMETER_ID = ?")) {
					stmtDeletePricingParameterIndexCurves.setLong(1, pricingParamId);
					stmtDeletePricingParameterIndexCurves.executeUpdate();
				}
				try (PreparedStatement stmtDeletePricingParameterDiscountCurves = con.prepareStatement(
						"DELETE FROM PRICING_PARAMETER_DISCOUNT_CURVE WHERE PRICING_PARAMETER_ID = ?")) {
					stmtDeletePricingParameterDiscountCurves.setLong(1, pricingParamId);
					stmtDeletePricingParameterDiscountCurves.executeUpdate();
				}
				try (PreparedStatement stmtDeletePricingParameterFXCurves = con
						.prepareStatement("DELETE FROM PRICING_PARAMETER_FX_CURVE WHERE PRICING_PARAMETER_ID = ?")) {
					stmtDeletePricingParameterFXCurves.setLong(1, pricingParamId);
					stmtDeletePricingParameterFXCurves.executeUpdate();
				}
				try (PreparedStatement stmtDeletePricingParameterCustomPricers = con.prepareStatement(
						"DELETE FROM PRICING_PARAMETER_CUSTOM_PRICER WHERE PRICING_PARAMETER_ID = ?")) {
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
					PricingParameterSQL.savePricingParameterModule(module, con, pricingParam.getId());
				}
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
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
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}
	}

	public static Set<String> getPricingParametersSetByQuoteSetId(long quoteSetId) {
		Set<String> pricingParametersSetNames = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetPricingParametersSetsByQuoteSetName = con
						.prepareStatement("SELECT * FROM PRICING_PARAMETER WHERE QUOTE_SET_ID = ?")) {
			stmtGetPricingParametersSetsByQuoteSetName.setLong(1, quoteSetId);
			try (ResultSet results = stmtGetPricingParametersSetsByQuoteSetName.executeQuery()) {
				while (results.next()) {
					if (pricingParametersSetNames == null) {
						pricingParametersSetNames = new HashSet<>();
					}
					pricingParametersSetNames.add(results.getString("name"));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return pricingParametersSetNames;
	}

}