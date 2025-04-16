package org.eclipse.tradista.core.message.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.message.model.ImportError;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

public class ImportErrorSQL {

	public static long saveImportError(ImportError error) {
		long errorId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveError = con.prepareStatement(
						"INSERT INTO ERROR(TYPE, MESSAGE, STATUS, ERROR_DATE) VALUES(?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtSaveImportError = con.prepareStatement("INSERT INTO IMPORT_ERROR VALUES(?, ?)");
				PreparedStatement stmtUpdateError = con
						.prepareStatement("UPDATE ERROR SET TYPE=?, MESSAGE=?, STATUS=?, ERROR_DATE=? WHERE ID=?");
				PreparedStatement stmtUpdateImportError = con
						.prepareStatement("UPDATE IMPORT_ERROR SET MESSAGE_ID = ? WHERE ERROR_ID=?")) {
			if (error.getId() == 0) {
				stmtSaveError.setString(1, error.getType());
				stmtSaveError.setString(2, error.getErrorMessage());
				stmtSaveError.setString(3, error.getStatus().name());
				stmtSaveError.setTimestamp(4, java.sql.Timestamp.valueOf(error.getErrorDate()));
				stmtSaveError.executeUpdate();
				if (error.getId() == 0) {
					try (ResultSet generatedKeys = stmtSaveError.getGeneratedKeys()) {
						if (generatedKeys.next()) {
							errorId = generatedKeys.getLong(1);
						} else {
							throw new SQLException("Creating error failed, no generated key obtained.");
						}
					}
				} else {
					errorId = error.getId();
				}
				stmtSaveImportError.setLong(1, error.getId());
				stmtSaveImportError.setLong(2, error.getMessage().getId());
				stmtSaveImportError.executeUpdate();
			} else {
				stmtUpdateImportError.setLong(1, error.getMessage().getId());
				stmtUpdateImportError.setLong(2, error.getId());
				stmtUpdateImportError.executeUpdate();

				stmtUpdateError.setString(1, error.getType());
				stmtUpdateError.setString(2, error.getErrorMessage());
				stmtUpdateError.setString(3, error.getStatus().name());
				stmtUpdateError.setTimestamp(4, java.sql.Timestamp.valueOf(error.getErrorDate()));
				stmtUpdateError.setLong(5, error.getId());
				stmtUpdateError.executeUpdate();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		error.setId(errorId);
		return errorId;
	}

}