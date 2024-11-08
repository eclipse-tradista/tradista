package org.eclipse.tradista.ir.irswapoption.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.ir.irswapoption.model.PricingParameterVolatilitySurfaceModule;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;

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
		try (PreparedStatement stmtSavePricingParameterSwaptionVolatilitySurfaces = con.prepareStatement(
				"INSERT INTO PRICING_PARAMETER_SWAPTION_VOLATILITY_SURFACE(PRICING_PARAMETER_ID, INDEX_ID, VOLATILITY_SURFACE_ID) VALUES(?, ?, ?)")) {

			if (pricingParamId != 0) {
				// Then, we delete the data for this pricingParam
				try (PreparedStatement stmtDeletePricingParameterEquityOptionVolatilitySurfaces = con.prepareStatement(
						"DELETE FROM PRICING_PARAMETER_SWAPTION_VOLATILITY_SURFACE WHERE PRICING_PARAMETER_ID = ?")) {
					stmtDeletePricingParameterEquityOptionVolatilitySurfaces.setLong(1, pricingParamId);
					stmtDeletePricingParameterEquityOptionVolatilitySurfaces.executeUpdate();
				}
			}
			for (Map.Entry<Index, SwaptionVolatilitySurface> entry : module.getVolatilitySurfaces().entrySet()) {
				stmtSavePricingParameterSwaptionVolatilitySurfaces.clearParameters();
				stmtSavePricingParameterSwaptionVolatilitySurfaces.setLong(1, pricingParamId);
				stmtSavePricingParameterSwaptionVolatilitySurfaces.setLong(2, entry.getKey().getId());
				stmtSavePricingParameterSwaptionVolatilitySurfaces.setLong(3, entry.getValue().getId());
				stmtSavePricingParameterSwaptionVolatilitySurfaces.addBatch();
			}
			stmtSavePricingParameterSwaptionVolatilitySurfaces.executeBatch();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static PricingParameterVolatilitySurfaceModule getPricingParameterModuleByPricingParameterId(Connection con,
			long id) {
		PricingParameterVolatilitySurfaceModule module = null;
		Map<Index, SwaptionVolatilitySurface> surfaces = new HashMap<Index, SwaptionVolatilitySurface>();

		try (PreparedStatement stmtGetPricingParameterSwaptionVolatilitySurfacesByPricingParameterId = con
				.prepareStatement(
						"SELECT * FROM PRICING_PARAMETER_SWAPTION_VOLATILITY_SURFACE WHERE PRICING_PARAMETER_ID = ?")) {
			stmtGetPricingParameterSwaptionVolatilitySurfacesByPricingParameterId.setLong(1, id);
			try (ResultSet results = stmtGetPricingParameterSwaptionVolatilitySurfacesByPricingParameterId
					.executeQuery()) {
				while (results.next()) {
					if (module == null) {
						module = new PricingParameterVolatilitySurfaceModule();
					}
					surfaces.put(IndexSQL.getIndexById(results.getLong("index_id")), SwaptionVolatilitySurfaceSQL
							.getSwaptionVolatilitySurfaceById(results.getLong("volatility_surface_id")));
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
				"DELETE FROM PRICING_PARAMETER_SWAPTION_VOLATILITY_SURFACE WHERE PRICING_PARAMETER_ID = ?")) {
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