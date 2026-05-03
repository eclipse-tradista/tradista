package org.eclipse.tradista.security.equityoption.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.INTEREST_RATE_CURVE_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRICING_PARAMETER_ID;

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
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.persistence.InterestRateCurveSQL;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.persistence.EquitySQL;
import org.eclipse.tradista.security.equityoption.model.PricingParameterDividendYieldCurveModule;

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

public class PricingParameterDividendYieldCurveSQL {

	private static final Field PRICING_PARAMETER_ID_FIELD = new Field(PRICING_PARAMETER_ID);
	private static final Field EQUITY_ID_FIELD = new Field("EQUITY_ID");
	private static final Field INTEREST_RATE_CURVE_ID_FIELD = new Field(INTEREST_RATE_CURVE_ID);

	private static final Field[] FIELDS = { PRICING_PARAMETER_ID_FIELD, EQUITY_ID_FIELD, INTEREST_RATE_CURVE_ID_FIELD };
	private static final Table TABLE = new Table("PRICING_PARAMETER_DIVIDEND_YIELD_CURVE", FIELDS);

	public static void savePricingParameterModule(Connection con, PricingParameterDividendYieldCurveModule module,
			long pricingParamId) {
		try (PreparedStatement stmtSavePricingParameterDividendYieldCurves = TradistaDBUtil
				.buildInsertPreparedStatement(con, TABLE, FIELDS)) {

			if (pricingParamId != 0) {
				// Then, we delete the data for this pricingParam
				try (PreparedStatement stmtDeletePricingParameterDividendYieldCurves = TradistaDBUtil
						.buildDeletePreparedStatement(con, TABLE, PRICING_PARAMETER_ID_FIELD)) {
					stmtDeletePricingParameterDividendYieldCurves.setLong(1, pricingParamId);
					stmtDeletePricingParameterDividendYieldCurves.executeUpdate();
				}
			}
			for (Map.Entry<Equity, InterestRateCurve> entry : module.getDividendYieldCurves().entrySet()) {
				stmtSavePricingParameterDividendYieldCurves.clearParameters();
				stmtSavePricingParameterDividendYieldCurves.setLong(1, pricingParamId);
				stmtSavePricingParameterDividendYieldCurves.setLong(2, entry.getKey().getId());
				stmtSavePricingParameterDividendYieldCurves.setLong(3, entry.getValue().getId());
				stmtSavePricingParameterDividendYieldCurves.addBatch();
			}
			stmtSavePricingParameterDividendYieldCurves.executeBatch();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static PricingParameterDividendYieldCurveModule getPricingParameterModuleByPricingParameterId(Connection con,
			long id) {
		PricingParameterDividendYieldCurveModule module = null;
		Map<Equity, InterestRateCurve> curves = new HashMap<>();

		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PRICING_PARAMETER_ID_FIELD);
		try (PreparedStatement stmtGetPricingParameterDividendYieldCurvesByPricingParameterId = con.prepareStatement(
				sql.toString())) {
			stmtGetPricingParameterDividendYieldCurvesByPricingParameterId.setLong(1, id);
			try (ResultSet results = stmtGetPricingParameterDividendYieldCurvesByPricingParameterId.executeQuery()) {
				while (results.next()) {
					if (module == null) {
						module = new PricingParameterDividendYieldCurveModule();
					}
					curves.put(EquitySQL.getEquityById(results.getLong(EQUITY_ID_FIELD.getName())),
							InterestRateCurveSQL
									.getInterestRateCurveById(results.getLong(INTEREST_RATE_CURVE_ID_FIELD.getName())));
					module.setDividendYieldCurves(curves);
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