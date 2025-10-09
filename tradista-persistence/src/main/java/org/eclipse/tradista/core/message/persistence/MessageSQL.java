package org.eclipse.tradista.core.message.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.AND;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.IN;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CREATION_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.FROM;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.LAST_UPDATE_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.SELECT;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.STATUS_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TYPE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.WHERE;

import static org.eclipse.tradista.core.workflow.persistence.StatusSQL.STATUS_TABLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.message.model.IncomingMessage;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.workflow.persistence.StatusSQL;
import org.springframework.util.CollectionUtils;

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

	public static final Table MESSAGE_TABLE = new Table("MESSAGE", ID);

	private static final Field ID_FIELD = new Field(ID, MESSAGE_TABLE);
	private static final Field INCOMING_FIELD = new Field("INCOMING", MESSAGE_TABLE);
	private static final Field OBJECT_ID_FIELD = new Field("OBJECT_ID", MESSAGE_TABLE);
	private static final Field OBJECT_TYPE_FIELD = new Field("OBJECT_TYPE", MESSAGE_TABLE);
	private static final Field CONTENT_FIELD = new Field("CONTENT", MESSAGE_TABLE);
	private static final Field TYPE_FIELD = new Field(TYPE, MESSAGE_TABLE);
	public static final Field INTERFACE_NAME_FIELD = new Field("INTERFACE_NAME", MESSAGE_TABLE);
	private static final Field CREATION_DATE_FIELD = new Field(CREATION_DATE, MESSAGE_TABLE);
	private static final Field LAST_UPDATE_DATE_FIELD = new Field(LAST_UPDATE_DATE, MESSAGE_TABLE);
	private static final Field STATUS_ID_FIELD = new Field(STATUS_ID, MESSAGE_TABLE);

	private static final Field[] FIELDS = { ID_FIELD, INCOMING_FIELD, OBJECT_ID_FIELD, OBJECT_TYPE_FIELD, CONTENT_FIELD,
			TYPE_FIELD, INTERFACE_NAME_FIELD, CREATION_DATE_FIELD, LAST_UPDATE_DATE_FIELD, STATUS_ID_FIELD };

	private static final String SELECT_QUERY = TradistaDBUtil.buildSelectQuery(FIELDS, new Table[] { MESSAGE_TABLE });

	public static long saveMessage(Message message) {

		long messageId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveMessage = (message.getId() != 0)
						? con.prepareStatement("UPDATE " + MESSAGE_TABLE + " SET " + INCOMING_FIELD + "=?,"
								+ CREATION_DATE + "=?," + LAST_UPDATE_DATE + "=?," + TYPE + "=?," + INTERFACE_NAME_FIELD
								+ "=?," + OBJECT_ID_FIELD + "=?," + OBJECT_TYPE_FIELD + "=?," + CONTENT_FIELD + "=?,"
								+ STATUS_ID + "=?" + WHERE + ID + "= ?")
						: con.prepareStatement(
								"INSERT INTO " + MESSAGE_TABLE + "(" + INCOMING_FIELD + "," + CREATION_DATE + ","
										+ LAST_UPDATE_DATE + "," + TYPE + "," + INTERFACE_NAME_FIELD + ","
										+ OBJECT_ID_FIELD + "," + OBJECT_TYPE_FIELD + "," + CONTENT_FIELD + ","
										+ STATUS_ID + ") VALUES (?,?,?,?,?,?,?,?,?)",
								Statement.RETURN_GENERATED_KEYS)) {
			if (message.getId() != 0) {
				stmtSaveMessage.setLong(10, message.getId());
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
			stmtSaveMessage.setLong(9, message.getStatus().getId());
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
		StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMessageById = con.prepareStatement(sqlQuery.toString())) {
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
					message.setObjectId(results.getLong(OBJECT_ID_FIELD.name()));
					message.setObjectType(results.getString(OBJECT_TYPE_FIELD.name()));
					message.setInterfaceName(results.getString(INTERFACE_NAME_FIELD.name()));
					message.setContent(results.getString(CONTENT_FIELD.name()));
					message.setStatus(StatusSQL.getStatusById(results.getLong(STATUS_ID)));
					message.setType(results.getString(TYPE_FIELD.name()));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return message;
	}

	public static List<Message> getMessages(long id, Boolean isIncoming, Set<String> types, Set<String> interfaceNames,
			long objectId, Set<String> objectTypes, Set<String> statuses, LocalDateTime creationDateTimeFrom,
			LocalDateTime creationDateTimeTo, LocalDateTime lastUpdateDateTimeFrom,
			LocalDateTime lastUpdateDateTimeTo) {
		List<Message> messages = null;

		try (Connection con = TradistaDB.getConnection(); Statement stmtGetMessages = con.createStatement()) {
			StringBuilder sqlQuery = new StringBuilder(SELECT_QUERY);
			TradistaDBUtil.addFilter(sqlQuery, ID_FIELD, id);
			TradistaDBUtil.addFilter(sqlQuery, INCOMING_FIELD, isIncoming);
			TradistaDBUtil.addFilter(sqlQuery, TYPE_FIELD, types);
			TradistaDBUtil.addFilter(sqlQuery, INTERFACE_NAME_FIELD, interfaceNames);
			TradistaDBUtil.addFilter(sqlQuery, OBJECT_ID_FIELD, objectId);
			TradistaDBUtil.addFilter(sqlQuery, OBJECT_TYPE_FIELD, objectTypes);
			TradistaDBUtil.addFilter(sqlQuery, CREATION_DATE_FIELD, creationDateTimeFrom, true);
			TradistaDBUtil.addFilter(sqlQuery, CREATION_DATE_FIELD, creationDateTimeTo, false);
			TradistaDBUtil.addFilter(sqlQuery, LAST_UPDATE_DATE_FIELD, lastUpdateDateTimeFrom, true);
			TradistaDBUtil.addFilter(sqlQuery, LAST_UPDATE_DATE_FIELD, lastUpdateDateTimeTo, false);

			String statusesSqlQuery = StringUtils.EMPTY;
			if (!CollectionUtils.isEmpty(statuses)) {
				String statusNamesSql = StringUtils.join(statuses, "','");
				if (sqlQuery.indexOf(WHERE) != -1) {
					statusesSqlQuery = AND;
				} else {
					statusesSqlQuery = WHERE;
				}
				statusesSqlQuery += STATUS_ID + IN + " (" + SELECT + ID + FROM + STATUS_TABLE + WHERE + ID + " = "
						+ STATUS_ID + AND + NAME + IN + " ('" + statusNamesSql + "'))";
			}

			sqlQuery.append(statusesSqlQuery);

			try (ResultSet results = stmtGetMessages.executeQuery(sqlQuery.toString())) {
				while (results.next()) {
					if (messages == null) {
						messages = new ArrayList<>();
					}
					Message message = null;
					String dbType = results.getString(TYPE);
					if (dbType.equals("Fix")) {
						message = new IncomingMessage();
					}
					message.setId(results.getLong(ID));
					message.setCreationDateTime(results.getTimestamp(CREATION_DATE).toLocalDateTime());
					message.setLastUpdateDateTime(results.getTimestamp(LAST_UPDATE_DATE).toLocalDateTime());
					message.setObjectId(results.getLong(OBJECT_ID_FIELD.name()));
					message.setObjectType(results.getString(OBJECT_TYPE_FIELD.name()));
					message.setInterfaceName(results.getString(INTERFACE_NAME_FIELD.name()));
					message.setContent(results.getString(CONTENT_FIELD.name()));
					message.setStatus(StatusSQL.getStatusById(results.getLong(STATUS_ID)));
					message.setType(results.getString(TYPE_FIELD.name()));
					messages.add(message);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return messages;
	}

}