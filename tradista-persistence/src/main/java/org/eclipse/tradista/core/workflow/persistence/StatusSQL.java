package org.eclipse.tradista.core.workflow.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.workflow.model.Status;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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

public class StatusSQL {

	public static Status getStatusById(long id) {
		Status status = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetStatusById = con.prepareStatement(
						"SELECT S.ID ID, S.WORKFLOW_ID, S.NAME NAME, W.NAME WORKFLOW_NAME, W.ID FROM STATUS S, WORKFLOW W WHERE S.WORKFLOW_ID = W.ID AND S.ID = ? ")) {
			stmtGetStatusById.setLong(1, id);
			try (ResultSet results = stmtGetStatusById.executeQuery()) {
				while (results.next()) {
					status = new Status();
					status.setId(results.getLong("id"));
					status.setWorkflowName(results.getString("workflow_name"));
					status.setName(results.getString("name"));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return status;
	}

}