package org.eclipse.tradista.core.marketdata.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;

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

public class SurfaceSQL {

	public static boolean surfaceExists(VolatilitySurface<?, ?, ?> surface, String type) {
		boolean exists = false;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetSurfaceByName = con.prepareStatement(
						"SELECT 1 FROM VOLATILITY_SURFACE WHERE NAME = ? AND PROCESSING_ORG_ID = ? AND TYPE = ?")) {
			stmtGetSurfaceByName.setString(1, surface.getName());
			stmtGetSurfaceByName.setLong(2, surface.getProcessingOrg().getId());
			stmtGetSurfaceByName.setString(3, type);
			try (ResultSet results = stmtGetSurfaceByName.executeQuery()) {
				while (results.next()) {
					exists = true;
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return exists;
	}

}