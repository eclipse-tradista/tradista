package org.eclipse.tradista.core.message.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.SELECT;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.FROM;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.WHERE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CREATION_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.LAST_UPDATE_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TYPE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
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

	private static final String INCOMING = "INCOMING";

	private static final String OBJECT_ID = "OBJECT_ID";

	private static final String OBJECT_TYPE = "OBJECT_TYPE";

	private static final String CONTENT = "CONTENT";

	public static final String MESSAGE = "MESSAGE";

	public static final String INTERFACE_NAME = "INTERFACE_NAME";

	public static long saveMessage(Message message) {

		long messageId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveMessage = (message.getId() != 0)
						? con.prepareStatement("UPDATE " + MESSAGE + " SET " + INCOMING + "=?," + CREATION_DATE + "=?,"
								+ LAST_UPDATE_DATE + "=?," + TYPE + "=?," + INTERFACE_NAME + "=?," + OBJECT_ID + "=?,"
								+ OBJECT_TYPE + "=?," + CONTENT + "=?" + WHERE + ID + "= ?")
						: con.prepareStatement("INSERT INTO " + MESSAGE + "(" + INCOMING + "," + CREATION_DATE + ","
								+ LAST_UPDATE_DATE + "," + TYPE + "," + INTERFACE_NAME + "," + OBJECT_ID + ","
								+ OBJECT_TYPE + "," + CONTENT + ") VALUES (?,?,?,?,?,?,?,?)",
								Statement.RETURN_GENERATED_KEYS)) {
			if (message.getId() != 0) {
				stmtSaveMessage.setLong(9, message.getId());
			}
			stmtSaveMessage.setBoolean(1, message instanceof IncomingMessage);
			stmtSaveMessage.setTimestamp(2, Timestamp.valueOf(message.getCreationDateTime()));
			stmtSaveMessage.setTimestamp(3, Timestamp.valueOf(message.getLastUpdateDateTime()));
			stmtSaveMessage.setString(4, message.getType());
			stmtSaveMessage.setString(5, message.getInterfaceName());
			if (message.getObjectId() == 0) {
				stmtSaveMessage.setNull(6, java.sql.Types.BIGINT);
			} else {
				stmtSaveMessage.setLong(6, message.getObjectId());
			}
			if (StringUtils.isBlank(message.getObjectType())) {
				stmtSaveMessage.setNull(7, java.sql.Types.VARCHAR);
			} else {
				stmtSaveMessage.setString(7, message.getObjectType());
			}
			stmtSaveMessage.setString(8, message.getContent());
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

	public static Message getMessageById(long id) {
		Message message = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMessageById = con.prepareStatement(SELECT + ID + "," + INCOMING + ","
						+ CREATION_DATE + "," + LAST_UPDATE_DATE + "," + TYPE + "," + INTERFACE_NAME + "," + OBJECT_ID
						+ "," + OBJECT_TYPE + "," + CONTENT + FROM + MESSAGE + WHERE + ID + " = ?")) {
			stmtGetMessageById.setLong(1, id);
			try (ResultSet results = stmtGetMessageById.executeQuery()) {
				while (results.next()) {
					String type = results.getString(TYPE);
					if (type.equals("Fix")) {
						message = new IncomingMessage();
					}
					message.setId(results.getLong(ID));
					message.setCreationDateTime(results.getTimestamp(CREATION_DATE).toLocalDateTime());
					message.setLastUpdateDateTime(results.getTimestamp(LAST_UPDATE_DATE).toLocalDateTime());
					message.setObjectId(results.getLong(OBJECT_ID));
					message.setObjectType(results.getString(OBJECT_TYPE));
					message.setInterfaceName(results.getString(INTERFACE_NAME));
					message.setContent(results.getString(CONTENT));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return message;
	}

}