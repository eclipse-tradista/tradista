package org.eclipse.tradista.security.repo.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PROCESSING_ORG_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUOTE_SET_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.persistence.QuoteSetSQL;
import org.eclipse.tradista.security.gcrepo.persistence.AllocationConfigurationSQL;
import org.eclipse.tradista.security.repo.model.AllocationConfiguration;
import org.eclipse.tradista.security.repo.model.ProcessingOrgDefaultsCollateralManagementModule;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public class ProcessingOrgDefaultsCollateralManagementSQL {

	private static final Field PROCESSING_ORG_ID_FIELD = new Field(PROCESSING_ORG_ID);
	private static final Field QUOTE_SET_ID_FIELD = new Field(QUOTE_SET_ID);
	private static final Field ALLOCATION_CONFIGURATION_ID_FIELD = new Field("ALLOCATION_CONFIGURATION_ID");

	private static final Field[] FIELDS = { PROCESSING_ORG_ID_FIELD, QUOTE_SET_ID_FIELD,
			ALLOCATION_CONFIGURATION_ID_FIELD };
	private static final Table TABLE = new Table("PROCESSING_ORG_DEFAULTS_COLLATERAL_MANAGEMENT", FIELDS);

	public static void saveProcessingOrgDefaultsModule(Connection con,
			ProcessingOrgDefaultsCollateralManagementModule module, long poId) {
		try (PreparedStatement stmtSaveProcessingOrgDefaultsCollateralManagement = TradistaDBUtil
				.buildInsertPreparedStatement(con, TABLE, FIELDS);
				PreparedStatement stmtDeleteProcessingOrgDefaultsCollateralManagement = TradistaDBUtil
						.buildDeletePreparedStatement(con, TABLE, PROCESSING_ORG_ID_FIELD)) {
			QuoteSet qs = module.getQuoteSet();
			AllocationConfiguration allocConfig = module.getAllocationConfiguration();
			stmtDeleteProcessingOrgDefaultsCollateralManagement.setLong(1, poId);
			stmtDeleteProcessingOrgDefaultsCollateralManagement.executeUpdate();
			stmtSaveProcessingOrgDefaultsCollateralManagement.setLong(1, poId);
			if (qs != null) {
				stmtSaveProcessingOrgDefaultsCollateralManagement.setLong(2, qs.getId());
			} else {
				stmtSaveProcessingOrgDefaultsCollateralManagement.setNull(2, Types.BIGINT);
			}
			if (allocConfig != null) {
				stmtSaveProcessingOrgDefaultsCollateralManagement.setLong(3, allocConfig.getId());
			} else {
				stmtSaveProcessingOrgDefaultsCollateralManagement.setNull(3, Types.BIGINT);
			}
			stmtSaveProcessingOrgDefaultsCollateralManagement.executeUpdate();
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static ProcessingOrgDefaultsCollateralManagementModule getProcessingOrgDefaultsModuleByPoId(Connection con,
			long poId) {

		// ProcessingOrgDefaults modules are always returned, even if not persisted yet.
		ProcessingOrgDefaultsCollateralManagementModule module = new ProcessingOrgDefaultsCollateralManagementModule();

		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PROCESSING_ORG_ID_FIELD);

		try (PreparedStatement stmtGetProcessingOrgDefaultsCollateralManagementByProcessingOrgDefaultsId = con
				.prepareStatement(sql.toString())) {
			stmtGetProcessingOrgDefaultsCollateralManagementByProcessingOrgDefaultsId.setLong(1, poId);
			try (ResultSet results = stmtGetProcessingOrgDefaultsCollateralManagementByProcessingOrgDefaultsId
					.executeQuery()) {
				while (results.next()) {
					QuoteSet qs = QuoteSetSQL.getQuoteSetById(results.getLong(QUOTE_SET_ID_FIELD.getName()));
					module.setQuoteSet(qs);
					AllocationConfiguration allocConfig = AllocationConfigurationSQL
							.getAllocationConfigurationById(results.getLong(ALLOCATION_CONFIGURATION_ID_FIELD.getName()));
					module.setAllocationConfiguration(allocConfig);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return module;
	}

}