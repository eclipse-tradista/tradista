package org.eclipse.tradista.core.common.persistence.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;

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

public final class TradistaDB {

	private static DataSource ds;

	private TradistaDB() {
	}

	static {
		try {
			InitialContext context = new InitialContext();
			ds = (DataSource) context.lookup("java:/TradistaDataSource");
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		Connection con = null;
		try {
			// Should use a connection pool as we are using
			// org.apache.derby.jdbc.ClientConnectionPoolDataSource
			con = ds.getConnection();
		} catch (SQLException sqle) {
			// TODO put error log
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return con;
	}

}