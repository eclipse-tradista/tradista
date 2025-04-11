package org.eclipse.tradista.core.message.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.message.model.IncomingMessage;
import org.eclipse.tradista.core.message.model.Message;

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

public class MessageSQL {

	public static long saveMessage(Message message) {

		long messageId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveMessage = (message.getId() != 0) ? con.prepareStatement(
						"UPDATE MESSAGE SET INCOMING=?, CREATION_DATE=?, LAST_UPDATE_DATE=?, TYPE=?, OBJECT_ID=?, CONTENT=? WHERE ID = ?")
						: con.prepareStatement(
								"INSERT INTO MESSAGE(INCOMING, CREATION_DATE, LAST_UPDATE_DATE, TYPE, OBJECT_ID, CONTENT) VALUES (?,?,?,?,?,?)",
								Statement.RETURN_GENERATED_KEYS)) {
			if (message.getId() != 0) {
				stmtSaveMessage.setLong(7, message.getId());
			}
			stmtSaveMessage.setBoolean(1, message instanceof IncomingMessage);
			stmtSaveMessage.setTimestamp(2, Timestamp.valueOf(message.getCreationDateTime()));
			stmtSaveMessage.setTimestamp(3, Timestamp.valueOf(message.getLastUpdateDateTime()));
			stmtSaveMessage.setString(4, message.getType());
			if (message.getObjectId() == 0) {
				stmtSaveMessage.setNull(5, java.sql.Types.BIGINT);
			} else {
				stmtSaveMessage.setLong(5, message.getObjectId());
			}
			stmtSaveMessage.executeUpdate();

			if (message.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveMessage.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						messageId = generatedKeys.getLong(1);
						message.setId(messageId);
					} else {
						throw new SQLException("Creating message failed, no generated key obtained.");
					}
				}
			} else {
				messageId = message.getId();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle.getMessage());
		}

		return messageId;
	}

}