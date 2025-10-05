package org.eclipse.tradista.core.workflow.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
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

	public static final Table STATUS_TABLE = new Table("STATUS", ID);
	private static final Table WORKFLOW_TABLE = new Table("WORKFLOW", ID);

	private static final Field ID_FIELD = new Field(ID, STATUS_TABLE);
	private static final Field WORKFLOW_ID_FIELD = new Field("WORKFLOW_ID", STATUS_TABLE);
	private static final Field NAME_FIELD = new Field(NAME, STATUS_TABLE);
	private static final Field WORKFLOW_NAME_FIELD = new Field(NAME, WORKFLOW_TABLE, "WORKFLOW_NAME");
	private static final Field ID_FIELD_WORKFLOW = new Field(ID, WORKFLOW_TABLE);

	private static final Field[] FIELDS = { ID_FIELD, WORKFLOW_ID_FIELD, NAME_FIELD, WORKFLOW_NAME_FIELD,
			ID_FIELD_WORKFLOW };

	private static final Table[] TABLES = { STATUS_TABLE, WORKFLOW_TABLE };

	private static final Join JOIN = new Join(new Field[] { WORKFLOW_ID_FIELD, ID_FIELD_WORKFLOW });

	private static final String SELECT_QUERY = TradistaDBUtil.buildSelectQuery(FIELDS, TABLES, JOIN);

	public static Status getStatusById(long id) {
		Status status = null;
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);
		sqlQuery = TradistaDBUtil.addParameterizedFilter(sqlQuery, ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetStatusById = con.prepareStatement(sqlQuery.toString())) {
			stmtGetStatusById.setLong(1, id);
			try (ResultSet results = stmtGetStatusById.executeQuery()) {
				while (results.next()) {
					status = new Status();
					status.setId(results.getLong(ID));
					status.setWorkflowName(results.getString(WORKFLOW_NAME_FIELD.getNameOrAlias()));
					status.setName(results.getString(NAME));
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