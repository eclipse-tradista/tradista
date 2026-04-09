package org.eclipse.tradista.core.message.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CREATION_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.LAST_UPDATE_TIME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.STATUS_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TYPE;

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

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.message.model.IncomingMessage;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.message.model.Message.ObjectType;
import org.eclipse.tradista.core.message.model.OutgoingMessage;
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

	public static final Field ID_FIELD = new Field(ID);
	private static final Field INCOMING_FIELD = new Field("INCOMING");
	private static final Field OBJECT_ID_FIELD = new Field("OBJECT_ID");
	private static final Field OBJECT_TYPE_FIELD = new Field("OBJECT_TYPE");
	private static final Field CONTENT_FIELD = new Field("CONTENT");
	public static final Field TYPE_FIELD = new Field(TYPE);
	public static final Field INTERFACE_NAME_FIELD = new Field("INTERFACE_NAME");
	private static final Field CREATION_TIME_FIELD = new Field("CREATION_TIME");
	private static final Field LAST_UPDATE_TIME_FIELD = new Field(LAST_UPDATE_TIME);
	private static final Field STATUS_ID_FIELD = new Field(STATUS_ID);

	private static final Field[] MESSAGE_FIELDS = { ID_FIELD, INCOMING_FIELD, OBJECT_ID_FIELD, OBJECT_TYPE_FIELD,
			CONTENT_FIELD, TYPE_FIELD, INTERFACE_NAME_FIELD, CREATION_TIME_FIELD, LAST_UPDATE_TIME_FIELD,
			STATUS_ID_FIELD };

	public static final Table MESSAGE_TABLE = new Table("MESSAGE", ID, MESSAGE_FIELDS);

	private static final String SELECT_QUERY = TradistaDBUtil.buildSelectQuery(MESSAGE_TABLE);

	public static long saveMessage(Message message) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveMessage = (message.getId() != 0)
						? TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, MESSAGE_TABLE, INCOMING_FIELD,
								CREATION_TIME_FIELD, LAST_UPDATE_TIME_FIELD, TYPE_FIELD, INTERFACE_NAME_FIELD,
								OBJECT_ID_FIELD, OBJECT_TYPE_FIELD, CONTENT_FIELD, STATUS_ID_FIELD)
						: TradistaDBUtil.buildInsertPreparedStatement(con, MESSAGE_TABLE, INCOMING_FIELD,
								CREATION_TIME_FIELD, LAST_UPDATE_TIME_FIELD, TYPE_FIELD, INTERFACE_NAME_FIELD,
								OBJECT_ID_FIELD, OBJECT_TYPE_FIELD, CONTENT_FIELD, STATUS_ID_FIELD)) {
			if (message.getId() != 0) {
				stmtSaveMessage.setLong(10, message.getId());
			}
			stmtSaveMessage.setBoolean(1, message instanceof IncomingMessage);
			stmtSaveMessage.setTimestamp(2, Timestamp.from(message.getCreationTime()));
			stmtSaveMessage.setTimestamp(3, Timestamp.from(message.getLastUpdateTime()));
			stmtSaveMessage.setString(4, message.getType());
			stmtSaveMessage.setString(5, message.getInterfaceName());
			if (message.getObjectId() == 0) {
				stmtSaveMessage.setNull(6, java.sql.Types.BIGINT);
			} else {
				stmtSaveMessage.setLong(6, message.getObjectId());
			}
			if (message.getObjectType() == null) {
				stmtSaveMessage.setNull(7, java.sql.Types.VARCHAR);
			} else {
				stmtSaveMessage.setString(7, message.getObjectType().name());
			}
			stmtSaveMessage.setString(8, message.getContent());
			stmtSaveMessage.setLong(9, message.getStatus().getId());
			stmtSaveMessage.executeUpdate();

			if (message.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveMessage.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						message.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("Creating message failed, no generated key obtained.");
					}
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle.getMessage());
		}

		return message.getId();
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
					Message.Builder<? extends Message, ?> builder;
					boolean incoming = results.getBoolean(INCOMING_FIELD.getName());
					if (incoming) {
						builder = new IncomingMessage.Builder();
					} else {
						builder = new OutgoingMessage.Builder();
					}
					String objectTypeStr = results.getString(OBJECT_TYPE_FIELD.getName());
					ObjectType objectType = (objectTypeStr != null) ? ObjectType.valueOf(objectTypeStr) : null;
					builder.id(results.getLong(ID)).objectId(results.getLong(OBJECT_ID_FIELD.getName()))
							.objectType(objectType).type(results.getString(TYPE_FIELD.getName()))
							.content(results.getString(CONTENT_FIELD.getName()))
							.interfaceName(results.getString(INTERFACE_NAME_FIELD.getName()))
							.status(StatusSQL.getStatusById(results.getLong(STATUS_ID)))
							// Crucial : We reinject dates from DB in order to not regenerate "NOW"
							.creationTime(results.getTimestamp(CREATION_DATE).toInstant())
							.lastUpdateTime(results.getTimestamp(LAST_UPDATE_TIME).toInstant());

					message = builder.build();
				}
			}
		} catch (SQLException sqle) {
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
			TradistaDBUtil.addFilter(sqlQuery, CREATION_TIME_FIELD, creationDateTimeFrom, true);
			TradistaDBUtil.addFilter(sqlQuery, CREATION_TIME_FIELD, creationDateTimeTo, false);
			TradistaDBUtil.addFilter(sqlQuery, LAST_UPDATE_TIME_FIELD, lastUpdateDateTimeFrom, true);
			TradistaDBUtil.addFilter(sqlQuery, LAST_UPDATE_TIME_FIELD, lastUpdateDateTimeTo, false);

			if (!CollectionUtils.isEmpty(statuses)) {
				StringBuilder queryFilter = new StringBuilder(
						TradistaDBUtil.buildSelectQuery(new Field[] { StatusSQL.ID_FIELD }, StatusSQL.STATUS_TABLE));
				TradistaDBUtil.addFilter(queryFilter, StatusSQL.NAME_FIELD, statuses);
				TradistaDBUtil.addQueryFilter(sqlQuery, STATUS_ID_FIELD, queryFilter.toString(), false);
			}

			try (ResultSet results = stmtGetMessages.executeQuery(sqlQuery.toString())) {
				while (results.next()) {
					if (messages == null) {
						messages = new ArrayList<>();
					}
					Message.Builder<? extends Message, ?> builder;
					boolean incoming = results.getBoolean(INCOMING_FIELD.getName());
					if (incoming) {
						builder = new IncomingMessage.Builder();
					} else {
						builder = new OutgoingMessage.Builder();
					}
					String objectTypeStr = results.getString(OBJECT_TYPE_FIELD.getName());
					ObjectType objectType = (objectTypeStr != null) ? ObjectType.valueOf(objectTypeStr) : null;
					builder.id(results.getLong(ID_FIELD.getName())).objectId(results.getLong(OBJECT_ID_FIELD.getName()))
							.objectType(objectType).type(results.getString(TYPE_FIELD.getName()))
							.content(results.getString(CONTENT_FIELD.getName()))
							.interfaceName(results.getString(INTERFACE_NAME_FIELD.getName()))
							.status(StatusSQL.getStatusById(results.getLong(STATUS_ID_FIELD.getName())))
							// Crucial : We reinject dates from DB in order to not regenerate "NOW"
							.creationTime(results.getTimestamp(CREATION_TIME_FIELD.getName()).toInstant())
							.lastUpdateTime(results.getTimestamp(LAST_UPDATE_TIME_FIELD.getName()).toInstant());

					messages.add(builder.build());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return messages;
	}

}