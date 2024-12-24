package org.eclipse.tradista.security.repo.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
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

	public static void saveProcessingOrgDefaultsModule(Connection con,
			ProcessingOrgDefaultsCollateralManagementModule module, long poId) {
		try (PreparedStatement stmtSaveProcessingOrgDefaultsCollateralManagement = con.prepareStatement(
				"INSERT INTO PROCESSING_ORG_DEFAULTS_COLLATERAL_MANAGEMENT(PROCESSING_ORG_ID, QUOTE_SET_ID, ALLOCATION_CONFIGURATION_ID) VALUES(?, ?, ?)");
				PreparedStatement stmtDeleteProcessingOrgDefaultsCollateralManagement = con.prepareStatement(
						"DELETE FROM PROCESSING_ORG_DEFAULTS_COLLATERAL_MANAGEMENT WHERE PROCESSING_ORG_ID = ?")) {
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
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static ProcessingOrgDefaultsCollateralManagementModule getProcessingOrgDefaultsModuleByPoId(Connection con,
			long poId) {

		// ProcessingOrgDefaults modules are always returned, even if not persisted yet.
		ProcessingOrgDefaultsCollateralManagementModule module = new ProcessingOrgDefaultsCollateralManagementModule();

		try (PreparedStatement stmtGetProcessingOrgDefaultsCollateralManagementByProcessingOrgDefaultsId = con
				.prepareStatement(
						"SELECT * FROM PROCESSING_ORG_DEFAULTS_COLLATERAL_MANAGEMENT WHERE PROCESSING_ORG_ID = ?")) {
			stmtGetProcessingOrgDefaultsCollateralManagementByProcessingOrgDefaultsId.setLong(1, poId);
			try (ResultSet results = stmtGetProcessingOrgDefaultsCollateralManagementByProcessingOrgDefaultsId
					.executeQuery()) {
				while (results.next()) {
					QuoteSet qs = QuoteSetSQL.getQuoteSetById(results.getLong("quote_set_id"));
					module.setQuoteSet(qs);
					AllocationConfiguration allocConfig = AllocationConfigurationSQL
							.getAllocationConfigurationById(results.getLong("allocation_configuration_id"));
					module.setAllocationConfiguration(allocConfig);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return module;
	}

}