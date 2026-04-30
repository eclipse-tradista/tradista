package org.eclipse.tradista.fx.common.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.BOOK_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRICING_PARAMETER_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRODUCT_TYPE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.fx.common.model.PricingParameterUnrealizedPnlCalculationModule;
import org.eclipse.tradista.fx.common.model.PricingParameterUnrealizedPnlCalculationModule.BookProductTypePair;
import org.eclipse.tradista.fx.common.model.PricingParameterUnrealizedPnlCalculationModule.UnrealizedPnlCalculation;

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

public class PricingParameterUnrealizedPnlCalculationSQL {

	private static final Field PRICING_PARAMETER_ID_FIELD = new Field(PRICING_PARAMETER_ID);
	private static final Field BOOK_ID_FIELD = new Field(BOOK_ID);
	private static final Field PRODUCT_TYPE_FIELD = new Field(PRODUCT_TYPE);
	private static final Field PNL_CALCULATION_FIELD = new Field("PNL_CALCULATION");

	private static final Field[] FIELDS = { PRICING_PARAMETER_ID_FIELD, BOOK_ID_FIELD, PRODUCT_TYPE_FIELD,
			PNL_CALCULATION_FIELD };
	private static final Table TABLE = new Table("PRICING_PARAMETER_UNREALIZED_PNL_CALCULATION", FIELDS);

	public static void savePricingParameterModule(Connection con, PricingParameterUnrealizedPnlCalculationModule module,
			long pricingParamId) {
		try (PreparedStatement stmtSavePricingParameterUnrealizedPnlCalculations = TradistaDBUtil
				.buildInsertPreparedStatement(con, TABLE, FIELDS)) {

			if (pricingParamId != 0) {
				// Then, we delete the data for this pricingParam
				try (PreparedStatement stmtDeletePricingParameterUnrealizedPnlCalculations = TradistaDBUtil
						.buildDeletePreparedStatement(con, TABLE, PRICING_PARAMETER_ID_FIELD)) {
					stmtDeletePricingParameterUnrealizedPnlCalculations.setLong(1, pricingParamId);
					stmtDeletePricingParameterUnrealizedPnlCalculations.executeUpdate();
				}
			}

			for (Map.Entry<BookProductTypePair, UnrealizedPnlCalculation> entry : module.getUnrealizedPnlCalculations()
					.entrySet()) {
				stmtSavePricingParameterUnrealizedPnlCalculations.clearParameters();
				stmtSavePricingParameterUnrealizedPnlCalculations.setLong(1, pricingParamId);
				Book book = entry.getKey().getBook();
				if (book != null) {
					stmtSavePricingParameterUnrealizedPnlCalculations.setLong(2, book.getId());
				} else {
					stmtSavePricingParameterUnrealizedPnlCalculations.setNull(2, Types.BIGINT);
				}
				stmtSavePricingParameterUnrealizedPnlCalculations.setString(3, entry.getKey().getProductType());
				stmtSavePricingParameterUnrealizedPnlCalculations.setString(4, entry.getValue().name());
				stmtSavePricingParameterUnrealizedPnlCalculations.addBatch();
			}
			stmtSavePricingParameterUnrealizedPnlCalculations.executeBatch();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static PricingParameterUnrealizedPnlCalculationModule getPricingParameterModuleByPricingParameterId(
			Connection con, long id) {
		PricingParameterUnrealizedPnlCalculationModule module = null;
		Map<org.eclipse.tradista.fx.common.model.PricingParameterUnrealizedPnlCalculationModule.BookProductTypePair, UnrealizedPnlCalculation> curves = new HashMap<>();

		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PRICING_PARAMETER_ID_FIELD);
		try (PreparedStatement stmtGetPricingParameterDividendYieldCurvesByPricingParameterId = con.prepareStatement(
				sql.toString())) {
			stmtGetPricingParameterDividendYieldCurvesByPricingParameterId.setLong(1, id);
			try (ResultSet results = stmtGetPricingParameterDividendYieldCurvesByPricingParameterId.executeQuery()) {
				while (results.next()) {
					if (module == null) {
						module = new PricingParameterUnrealizedPnlCalculationModule();
					}
					curves.put(
							new BookProductTypePair(BookSQL.getBookById(results.getLong(BOOK_ID_FIELD.getName())),
									results.getString(PRODUCT_TYPE_FIELD.getName())),
							UnrealizedPnlCalculation.valueOf(results.getString(PNL_CALCULATION_FIELD.getName())));
					module.setUnrealizedPnlCalculations(curves);
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