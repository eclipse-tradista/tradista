package org.eclipse.tradista.security.equityoption.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRICING_PARAMETER_ID;
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
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.persistence.EquitySQL;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;
import org.eclipse.tradista.security.equityoption.model.PricingParameterVolatilitySurfaceModule;

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
	private static final Field EQUITY_ID_FIELD = new Field("EQUITY_ID");
	private static final Field VOLATILITY_SURFACE_ID_FIELD = new Field(VOLATILITY_SURFACE_ID);

	private static final Field[] FIELDS = { PRICING_PARAMETER_ID_FIELD, EQUITY_ID_FIELD, VOLATILITY_SURFACE_ID_FIELD };
	private static final Table TABLE = new Table("PRICING_PARAMETER_EQUITY_OPTION_VOLATILITY_SURFACE", FIELDS);

	public static void savePricingParameterModule(Connection con, PricingParameterVolatilitySurfaceModule module,
			long pricingParamId) {
		try (PreparedStatement stmtSavePricingParameterEquityOptionVolatilitySurfaces = TradistaDBUtil
				.buildInsertPreparedStatement(con, TABLE, FIELDS)) {

			if (pricingParamId != 0) {
				// Then, we delete the data for this pricingParam
				try (PreparedStatement stmtDeletePricingParameterEquityOptionVolatilitySurfaces = TradistaDBUtil
						.buildDeletePreparedStatement(con, TABLE, PRICING_PARAMETER_ID_FIELD)) {
					stmtDeletePricingParameterEquityOptionVolatilitySurfaces.setLong(1, pricingParamId);
					stmtDeletePricingParameterEquityOptionVolatilitySurfaces.executeUpdate();
				}
			}
			for (Map.Entry<Equity, EquityOptionVolatilitySurface> entry : module.getVolatilitySurfaces().entrySet()) {
				stmtSavePricingParameterEquityOptionVolatilitySurfaces.clearParameters();
				stmtSavePricingParameterEquityOptionVolatilitySurfaces.setLong(1, pricingParamId);
				stmtSavePricingParameterEquityOptionVolatilitySurfaces.setLong(2, entry.getKey().getId());
				stmtSavePricingParameterEquityOptionVolatilitySurfaces.setLong(3, entry.getValue().getId());
				stmtSavePricingParameterEquityOptionVolatilitySurfaces.addBatch();
			}
			stmtSavePricingParameterEquityOptionVolatilitySurfaces.executeBatch();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static PricingParameterVolatilitySurfaceModule getPricingParameterModuleByPricingParameterId(Connection con,
			long id) {
		PricingParameterVolatilitySurfaceModule module = null;
		Map<Equity, EquityOptionVolatilitySurface> surfaces = new HashMap<>();

		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PRICING_PARAMETER_ID_FIELD);
		try (PreparedStatement stmtGetPricingParameterEquityOptionVolatilitySurfacesByPricingParameterId = con
				.prepareStatement(sql.toString())) {
			stmtGetPricingParameterEquityOptionVolatilitySurfacesByPricingParameterId.setLong(1, id);
			try (ResultSet results = stmtGetPricingParameterEquityOptionVolatilitySurfacesByPricingParameterId
					.executeQuery()) {
				while (results.next()) {
					if (module == null) {
						module = new PricingParameterVolatilitySurfaceModule();
					}
					surfaces.put(EquitySQL.getEquityById(results.getLong(EQUITY_ID_FIELD.getName())),
							EquityOptionVolatilitySurfaceSQL
									.getEquityOptionVolatilitySurfaceById(
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