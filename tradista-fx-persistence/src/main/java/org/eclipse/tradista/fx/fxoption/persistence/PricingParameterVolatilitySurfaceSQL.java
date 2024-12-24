package org.eclipse.tradista.fx.fxoption.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
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

	public static void savePricingParameterModule(Connection con, PricingParameterVolatilitySurfaceModule module,
			long pricingParamId) {
		try (PreparedStatement stmtSavePricingParameterFXVolatilitySurfaces = con.prepareStatement(
				"INSERT INTO PRICING_PARAMETER_FX_VOLATILITY_SURFACE(PRICING_PARAMETER_ID, PRIMARY_CURRENCY_ID, QUOTE_CURRENCY_ID, VOLATILITY_SURFACE_ID) VALUES(?, ?, ?, ?)")) {

			if (pricingParamId != 0) {
				// Then, we delete the data for this pricingParam
				try (PreparedStatement stmtDeletePricingParameterEquityOptionVolatilitySurfaces = con.prepareStatement(
						"DELETE FROM PRICING_PARAMETER_FX_VOLATILITY_SURFACE WHERE PRICING_PARAMETER_ID = ?")) {
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
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static PricingParameterVolatilitySurfaceModule getPricingParameterModuleByPricingParameterId(Connection con,
			long id) {
		PricingParameterVolatilitySurfaceModule module = null;
		Map<CurrencyPair, FXVolatilitySurface> surfaces = new HashMap<CurrencyPair, FXVolatilitySurface>();

		try (PreparedStatement stmtGetPricingParameterFxVolatilitySurfacesByPricingParameterId = con.prepareStatement(
				"SELECT * FROM PRICING_PARAMETER_FX_VOLATILITY_SURFACE WHERE PRICING_PARAMETER_ID = ?")) {
			stmtGetPricingParameterFxVolatilitySurfacesByPricingParameterId.setLong(1, id);
			try (ResultSet results = stmtGetPricingParameterFxVolatilitySurfacesByPricingParameterId.executeQuery()) {
				while (results.next()) {
					if (module == null) {
						module = new PricingParameterVolatilitySurfaceModule();
					}
					surfaces.put(
							new CurrencyPair(CurrencySQL.getCurrencyById(results.getLong("primary_currency_id")),
									CurrencySQL.getCurrencyById(results.getLong("quote_currency_id"))),
							FXVolatilitySurfaceSQL
									.getFXVolatilitySurfaceById(results.getLong("volatility_surface_id")));
					module.setVolatilitySurfaces(surfaces);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return module;
	}

	public static boolean deletePricingParameterModule(Connection con, long id) {
		boolean bSaved = false;

		try (PreparedStatement stmtDeletePricingParameterModule = con.prepareStatement(
				"DELETE FROM PRICING_PARAMETER_FX_VOLATILITY_SURFACE WHERE PRICING_PARAMETER_ID = ?")) {
			stmtDeletePricingParameterModule.setLong(1, id);
			stmtDeletePricingParameterModule.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

}