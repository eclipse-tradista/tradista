package org.eclipse.tradista.security.equityoption.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
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

	public static void savePricingParameterModule(Connection con, PricingParameterDividendYieldCurveModule module,
			long pricingParamId) {
		try (PreparedStatement stmtSavePricingParameterDividendYieldCurves = con.prepareStatement(
				"INSERT INTO PRICING_PARAMETER_DIVIDEND_YIELD_CURVE(PRICING_PARAMETER_ID, EQUITY_ID, INTEREST_RATE_CURVE_ID) VALUES(?, ?, ?)")) {

			if (pricingParamId != 0) {
				// Then, we delete the data for this pricingParam
				try (PreparedStatement stmtDeletePricingParameterDividendYieldCurves = con.prepareStatement(
						"DELETE FROM PRICING_PARAMETER_DIVIDEND_YIELD_CURVE WHERE PRICING_PARAMETER_ID = ?")) {
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
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static PricingParameterDividendYieldCurveModule getPricingParameterModuleByPricingParameterId(Connection con,
			long id) {
		PricingParameterDividendYieldCurveModule module = null;
		Map<Equity, InterestRateCurve> curves = new HashMap<Equity, InterestRateCurve>();

		try (PreparedStatement stmtGetPricingParameterDividendYieldCurvesByPricingParameterId = con.prepareStatement(
				"SELECT * FROM PRICING_PARAMETER_DIVIDEND_YIELD_CURVE WHERE PRICING_PARAMETER_ID = ?")) {
			stmtGetPricingParameterDividendYieldCurvesByPricingParameterId.setLong(1, id);
			try (ResultSet results = stmtGetPricingParameterDividendYieldCurvesByPricingParameterId.executeQuery()) {
				while (results.next()) {
					if (module == null) {
						module = new PricingParameterDividendYieldCurveModule();
					}
					curves.put(EquitySQL.getEquityById(results.getLong("equity_id")),
							InterestRateCurveSQL.getInterestRateCurveById(results.getLong("interest_rate_curve_id")));
					module.setDividendYieldCurves(curves);
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
				"DELETE FROM PRICING_PARAMETER_DIVIDEND_YIELD_CURVE WHERE PRICING_PARAMETER_ID = ?")) {
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