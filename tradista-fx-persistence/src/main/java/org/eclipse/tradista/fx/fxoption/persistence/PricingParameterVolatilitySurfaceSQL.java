package org.eclipse.tradista.fx.fxoption.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRICING_PARAMETER_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRIMARY_CURRENCY_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUOTE_CURRENCY_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.VOLATILITY_SURFACE_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.fx.fxoption.model.FXVolatilitySurface;
import org.eclipse.tradista.fx.fxoption.model.PricingParameterVolatilitySurfaceModule;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

public class PricingParameterVolatilitySurfaceSQL {

	private static final Field PRICING_PARAMETER_ID_FIELD = new Field(PRICING_PARAMETER_ID);
	private static final Field PRIMARY_CURRENCY_ID_FIELD = new Field(PRIMARY_CURRENCY_ID);
	private static final Field QUOTE_CURRENCY_ID_FIELD = new Field(QUOTE_CURRENCY_ID);
	private static final Field VOLATILITY_SURFACE_ID_FIELD = new Field(VOLATILITY_SURFACE_ID);

	private static final Field[] FIELDS = { PRICING_PARAMETER_ID_FIELD, PRIMARY_CURRENCY_ID_FIELD,
			QUOTE_CURRENCY_ID_FIELD, VOLATILITY_SURFACE_ID_FIELD };
	private static final Table TABLE = new Table("PRICING_PARAMETER_FX_VOLATILITY_SURFACE", FIELDS);

	public static void savePricingParameterModule(Connection con, PricingParameterVolatilitySurfaceModule module,
			long pricingParamId) {
		try (PreparedStatement stmtSavePricingParameterFXVolatilitySurfaces = TradistaDBUtil
				.buildInsertPreparedStatement(con, TABLE, FIELDS)) {

			if (pricingParamId != 0) {
				// Then, we delete the data for this pricingParam
				try (PreparedStatement stmtDeletePricingParameterEquityOptionVolatilitySurfaces = TradistaDBUtil
						.buildDeletePreparedStatement(con, TABLE, PRICING_PARAMETER_ID_FIELD)) {
					stmtDeletePricingParameterEquityOptionVolatilitySurfaces.setLong(1, pricingParamId);
					stmtDeletePricingParameterEquityOptionVolatilitySurfaces.executeUpdate();
				}
			}
			for (Map.Entry<CurrencyPair, FXVolatilitySurface> entry : module.getVolatilitySurfaces().entrySet()) {
				stmtSavePricingParameterFXVolatilitySurfaces.clearParameters();
				stmtSavePricingParameterFXVolatilitySurfaces.setLong(1, pricingParamId);
				stmtSavePricingParameterFXVolatilitySurfaces.setLong(2, entry.getKey().getPrimaryCurrency().getId());
				stmtSavePricingParameterFXVolatilitySurfaces.setLong(3, entry.getKey().getQuoteCurrency().getId());
				stmtSavePricingParameterFXVolatilitySurfaces.setLong(4, entry.getValue().getId());
				stmtSavePricingParameterFXVolatilitySurfaces.addBatch();
			}
			stmtSavePricingParameterFXVolatilitySurfaces.executeBatch();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static PricingParameterVolatilitySurfaceModule getPricingParameterModuleByPricingParameterId(Connection con,
			long id) {
		PricingParameterVolatilitySurfaceModule module = null;
		Map<CurrencyPair, FXVolatilitySurface> surfaces = new HashMap<>();

		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PRICING_PARAMETER_ID_FIELD);
		try (PreparedStatement stmtGetPricingParameterFxVolatilitySurfacesByPricingParameterId = con.prepareStatement(
				sql.toString())) {
			stmtGetPricingParameterFxVolatilitySurfacesByPricingParameterId.setLong(1, id);
			try (ResultSet results = stmtGetPricingParameterFxVolatilitySurfacesByPricingParameterId.executeQuery()) {
				while (results.next()) {
					if (module == null) {
						module = new PricingParameterVolatilitySurfaceModule();
					}
					surfaces.put(
							new CurrencyPair(
									CurrencySQL.getCurrencyById(results.getLong(PRIMARY_CURRENCY_ID_FIELD.getName())),
									CurrencySQL.getCurrencyById(results.getLong(QUOTE_CURRENCY_ID_FIELD.getName()))),
							FXVolatilitySurfaceSQL
									.getFXVolatilitySurfaceById(
											results.getLong(VOLATILITY_SURFACE_ID_FIELD.getName())));
					module.setVolatilitySurfaces(surfaces);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return module;
	}

	public static boolean deletePricingParameterModule(Connection con, long id) {
		boolean bSaved = false;

		try (PreparedStatement stmtDeletePricingParameterModule = TradistaDBUtil
				.buildDeletePreparedStatement(con, TABLE, PRICING_PARAMETER_ID_FIELD)) {
			stmtDeletePricingParameterModule.setLong(1, id);
			stmtDeletePricingParameterModule.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

}