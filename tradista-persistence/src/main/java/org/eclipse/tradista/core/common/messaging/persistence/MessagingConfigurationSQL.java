package org.eclipse.tradista.core.common.messaging.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.messaging.model.MessagingEventSubscription;
import org.eclipse.tradista.core.common.messaging.model.MessagingFilter;
import org.eclipse.tradista.core.common.messaging.model.MessagingListener;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Expression;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.springframework.util.CollectionUtils;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public class MessagingConfigurationSQL {

	// Table MESSAGING_LISTENER fields
	private static final Field LISTENER_ID_FIELD = new Field(ID);
	private static final Field LISTENER_NAME_FIELD = new Field(NAME);
	private static final Field LISTENER_QUEUE_NAME_FIELD = new Field("QUEUE_NAME");
	private static final Field LISTENER_POLLER_DELAY_FIELD = new Field("POLLER_DELAY");
	private static final Field LISTENER_POLLER_MAX_MESSAGES_FIELD = new Field("POLLER_MAX_MESSAGES");
	private static final Field LISTENER_IS_ENABLED_FIELD = new Field("IS_ENABLED");

	private static final Field[] LISTENER_FIELDS = { LISTENER_ID_FIELD, LISTENER_NAME_FIELD, LISTENER_QUEUE_NAME_FIELD,
			LISTENER_POLLER_DELAY_FIELD, LISTENER_POLLER_MAX_MESSAGES_FIELD, LISTENER_IS_ENABLED_FIELD };

	private static final Field[] LISTENER_FIELDS_FOR_INSERT_OR_UPDATE = { LISTENER_NAME_FIELD,
			LISTENER_QUEUE_NAME_FIELD, LISTENER_POLLER_DELAY_FIELD, LISTENER_POLLER_MAX_MESSAGES_FIELD,
			LISTENER_IS_ENABLED_FIELD };

	public static final Table MESSAGING_LISTENER_TABLE = new Table("MESSAGING_LISTENER", LISTENER_FIELDS);

	// Table MESSAGING_FILTER fields
	private static final Field FILTER_ID_FIELD = new Field(ID);
	private static final Field FILTER_NAME_FIELD = new Field(NAME);
	private static final Field FILTER_CLASS_NAME_FIELD = new Field("CLASS_NAME");

	private static final Field[] FILTER_FIELDS = { FILTER_ID_FIELD, FILTER_NAME_FIELD, FILTER_CLASS_NAME_FIELD };

	private static final Field[] FILTER_FIELDS_FOR_INSERT_OR_UPDATE = { FILTER_NAME_FIELD, FILTER_CLASS_NAME_FIELD };

	public static final Table MESSAGING_FILTER_TABLE = new Table("MESSAGING_FILTER", FILTER_FIELDS);

	// Table MESSAGING_LISTENER_FILTER fields
	private static final Field LF_LISTENER_ID_FIELD = new Field("LISTENER_ID");
	private static final Field LF_FILTER_ID_FIELD = new Field("FILTER_ID");
	private static final Field LF_EXECUTION_ORDER_FIELD = new Field("EXECUTION_ORDER");

	private static final Field[] LISTENER_FILTER_FIELDS = { LF_LISTENER_ID_FIELD, LF_FILTER_ID_FIELD,
			LF_EXECUTION_ORDER_FIELD };

	public static final Table MESSAGING_LISTENER_FILTER_TABLE = new Table("MESSAGING_LISTENER_FILTER",
			LISTENER_FILTER_FIELDS);

	// Table MESSAGING_EVENT_SUBSCRIPTION fields
	private static final Field ES_EVENT_TYPE_FIELD = new Field("EVENT_TYPE");
	private static final Field ES_LISTENER_ID_FIELD = new Field("LISTENER_ID");

	private static final Field[] EVENT_SUBSCRIPTION_FIELDS = { ES_EVENT_TYPE_FIELD, ES_LISTENER_ID_FIELD };

	public static final Table MESSAGING_EVENT_SUBSCRIPTION_TABLE = new Table("MESSAGING_EVENT_SUBSCRIPTION",
			EVENT_SUBSCRIPTION_FIELDS);

	// Joins
	public static final Join FILTER_JOIN = Join.innerEq(MESSAGING_LISTENER_FILTER_TABLE, FILTER_ID_FIELD,
			LF_FILTER_ID_FIELD);
	public static final Join LISTENER_JOIN = Join.innerEq(MESSAGING_LISTENER_TABLE, LISTENER_ID_FIELD,
			ES_LISTENER_ID_FIELD);
	public static final Join LF_LISTENER_JOIN = Join.innerEq(MESSAGING_LISTENER_TABLE, LISTENER_ID_FIELD,
			LF_LISTENER_ID_FIELD);

	// --- LISTENERS ---

	public static List<MessagingListener> getAllListeners() {
		Set<MessagingListener> listeners = new TreeSet<>();
		String query = TradistaDBUtil.buildSelectQuery(MESSAGING_LISTENER_TABLE);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				MessagingListener listener = buildListener(rs);
				listener.setFilters(getFiltersByListenerId(listener.getId()));
				listeners.add(listener);
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return new ArrayList<>(listeners);
	}

	public static List<String> getAllActiveListenerNames() {
		Set<String> names = new TreeSet<>();
		StringBuilder query = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(LISTENER_NAME_FIELD, MESSAGING_LISTENER_TABLE));
		TradistaDBUtil.addFilter(query, LISTENER_IS_ENABLED_FIELD, true);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(query.toString());
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				names.add(rs.getString(LISTENER_NAME_FIELD.getName()));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return new ArrayList<>(names);
	}

	public static MessagingListener getListenerById(long id) {
		MessagingListener listener = null;
		StringBuilder query = new StringBuilder(TradistaDBUtil.buildSelectQuery(MESSAGING_LISTENER_TABLE));
		TradistaDBUtil.addParameterizedFilter(query, LISTENER_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(query.toString())) {
			stmt.setLong(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					listener = buildListener(rs);
					listener.setFilters(getFiltersByListenerId(listener.getId()));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return listener;
	}

	public static MessagingListener getListenerByName(String name) {
		MessagingListener listener = null;
		StringBuilder query = new StringBuilder(TradistaDBUtil.buildSelectQuery(MESSAGING_LISTENER_TABLE));
		TradistaDBUtil.addParameterizedFilter(query, LISTENER_NAME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(query.toString())) {
			stmt.setString(1, name);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					listener = buildListener(rs);
					listener.setFilters(getFiltersByListenerId(listener.getId()));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return listener;
	}

	public static long saveListener(MessagingListener listener) {
		boolean exists = listener.getId() != 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveListener = (!exists)
						? TradistaDBUtil.buildInsertPreparedStatement(con, MESSAGING_LISTENER_TABLE,
								LISTENER_FIELDS_FOR_INSERT_OR_UPDATE)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, LISTENER_ID_FIELD, MESSAGING_LISTENER_TABLE,
								LISTENER_FIELDS_FOR_INSERT_OR_UPDATE);
				PreparedStatement stmtDeleteFilters = TradistaDBUtil.buildDeletePreparedStatement(con,
						MESSAGING_LISTENER_FILTER_TABLE, LF_LISTENER_ID_FIELD);
				PreparedStatement stmtSaveFilter = TradistaDBUtil.buildInsertPreparedStatement(con,
						MESSAGING_LISTENER_FILTER_TABLE, LISTENER_FILTER_FIELDS)) {

			if (exists) {
				stmtSaveListener.setLong(6, listener.getId());
			}
			stmtSaveListener.setString(1, listener.getName());
			stmtSaveListener.setString(2, listener.getQueueName());
			stmtSaveListener.setLong(3, listener.getPollerDelay());
			stmtSaveListener.setInt(4, listener.getPollerMaxMessages());
			stmtSaveListener.setBoolean(5, listener.isEnabled());
			stmtSaveListener.executeUpdate();

			if (!exists) {
				try (ResultSet generatedKeys = stmtSaveListener.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						listener.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("Creating listener failed, no generated key obtained.");
					}
				}
			} else {
				stmtDeleteFilters.setLong(1, listener.getId());
				stmtDeleteFilters.executeUpdate();
			}

			if (!CollectionUtils.isEmpty(listener.getFilters())) {
				int order = 1;
				for (MessagingFilter f : listener.getFilters()) {
					stmtSaveFilter.clearParameters();
					stmtSaveFilter.setLong(1, listener.getId());
					stmtSaveFilter.setLong(2, f.getId());
					stmtSaveFilter.setInt(3, order++);
					stmtSaveFilter.addBatch();
				}
				stmtSaveFilter.executeBatch();
			}

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return listener.getId();
	}

	public static void deleteListener(long id) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteSubscriptions = TradistaDBUtil.buildDeletePreparedStatement(con,
						MESSAGING_EVENT_SUBSCRIPTION_TABLE, ES_LISTENER_ID_FIELD);
				PreparedStatement stmtDeleteFilters = TradistaDBUtil.buildDeletePreparedStatement(con,
						MESSAGING_LISTENER_FILTER_TABLE, LF_LISTENER_ID_FIELD);
				PreparedStatement stmtDeleteListener = TradistaDBUtil.buildDeletePreparedStatement(con,
						MESSAGING_LISTENER_TABLE, LISTENER_ID_FIELD)) {
			stmtDeleteSubscriptions.setLong(1, id);
			stmtDeleteSubscriptions.executeUpdate();

			stmtDeleteFilters.setLong(1, id);
			stmtDeleteFilters.executeUpdate();

			stmtDeleteListener.setLong(1, id);
			stmtDeleteListener.executeUpdate();
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	// --- FILTERS ---

	public static List<MessagingFilter> getAllFilters() {
		Set<MessagingFilter> filters = new TreeSet<>();
		String query = TradistaDBUtil.buildSelectQuery(MESSAGING_FILTER_TABLE);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				filters.add(buildFilter(rs));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return new ArrayList<>(filters);
	}

	public static MessagingFilter getFilterById(long id) {
		MessagingFilter filter = null;
		StringBuilder query = new StringBuilder(TradistaDBUtil.buildSelectQuery(MESSAGING_FILTER_TABLE));
		TradistaDBUtil.addParameterizedFilter(query, FILTER_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(query.toString())) {
			stmt.setLong(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					filter = buildFilter(rs);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return filter;
	}

	public static MessagingFilter getFilterByName(String name) {
		MessagingFilter filter = null;
		StringBuilder query = new StringBuilder(TradistaDBUtil.buildSelectQuery(MESSAGING_FILTER_TABLE));
		TradistaDBUtil.addParameterizedFilter(query, FILTER_NAME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(query.toString())) {
			stmt.setString(1, name);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					filter = buildFilter(rs);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return filter;
	}

	public static long saveFilter(MessagingFilter filter) {
		boolean exists = filter.getId() != 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveFilter = (!exists)
						? TradistaDBUtil.buildInsertPreparedStatement(con, MESSAGING_FILTER_TABLE,
								FILTER_FIELDS_FOR_INSERT_OR_UPDATE)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, FILTER_ID_FIELD, MESSAGING_FILTER_TABLE,
								FILTER_FIELDS_FOR_INSERT_OR_UPDATE)) {
			if (exists) {
				stmtSaveFilter.setLong(3, filter.getId());
			}
			stmtSaveFilter.setString(1, filter.getName());
			stmtSaveFilter.setString(2, filter.getClassName());
			stmtSaveFilter.executeUpdate();

			if (!exists) {
				try (ResultSet generatedKeys = stmtSaveFilter.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						filter.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("Creating filter failed, no generated key obtained.");
					}
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return filter.getId();
	}

	public static void deleteFilter(long id) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteLF = TradistaDBUtil.buildDeletePreparedStatement(con,
						MESSAGING_LISTENER_FILTER_TABLE, LF_FILTER_ID_FIELD);
				PreparedStatement stmtDeleteFilter = TradistaDBUtil.buildDeletePreparedStatement(con,
						MESSAGING_FILTER_TABLE, FILTER_ID_FIELD)) {
			stmtDeleteLF.setLong(1, id);
			stmtDeleteLF.executeUpdate();

			stmtDeleteFilter.setLong(1, id);
			stmtDeleteFilter.executeUpdate();
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	// --- LISTENER FILTERS ---

	public static List<MessagingFilter> getFiltersByListenerId(long listenerId) {
		List<MessagingFilter> filters = new ArrayList<>();
		StringBuilder query = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(FILTER_FIELDS, MESSAGING_FILTER_TABLE, FILTER_JOIN));
		TradistaDBUtil.addParameterizedFilter(query, LF_LISTENER_ID_FIELD);
		query.append(" ORDER BY ").append(LF_EXECUTION_ORDER_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(query.toString())) {
			stmt.setLong(1, listenerId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					filters.add(buildFilter(rs));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return filters;
	}

	public static List<String> getFilterNamesByListenerQueueName(String queueName) {
		List<String> filterNames = new ArrayList<>();
		StringBuilder query = new StringBuilder(TradistaDBUtil.buildSelectQuery(FILTER_NAME_FIELD,
				MESSAGING_FILTER_TABLE, FILTER_JOIN, LF_LISTENER_JOIN));
		TradistaDBUtil.addParameterizedFilter(query, LISTENER_QUEUE_NAME_FIELD);
		TradistaDBUtil.addFilter(query, LISTENER_IS_ENABLED_FIELD, true);
		query.append(" ORDER BY ").append(LF_EXECUTION_ORDER_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(query.toString())) {
			stmt.setString(1, queueName);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					filterNames.add(rs.getString(FILTER_NAME_FIELD.getName()));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return filterNames;
	}

	// --- SUBSCRIPTIONS / ROUTING ---

	public static List<MessagingEventSubscription> getAllSubscriptions() {
		Set<MessagingEventSubscription> subscriptions = new TreeSet<>();
		String query = TradistaDBUtil
				.buildSelectQuery(
						new Expression[] { ES_EVENT_TYPE_FIELD, LISTENER_ID_FIELD, LISTENER_NAME_FIELD,
								LISTENER_QUEUE_NAME_FIELD, LISTENER_POLLER_DELAY_FIELD,
								LISTENER_POLLER_MAX_MESSAGES_FIELD, LISTENER_IS_ENABLED_FIELD },
						MESSAGING_EVENT_SUBSCRIPTION_TABLE, LISTENER_JOIN);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				String eventType = rs.getString(ES_EVENT_TYPE_FIELD.getName());
				MessagingListener listener = buildListener(rs);
				subscriptions.add(new MessagingEventSubscription(eventType, listener));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return new ArrayList<>(subscriptions);
	}

	public static List<MessagingEventSubscription> getSubscriptionsByEventType(String eventType) {
		Set<MessagingEventSubscription> subscriptions = new TreeSet<>();
		StringBuilder query = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(
						new Expression[] { ES_EVENT_TYPE_FIELD, LISTENER_ID_FIELD, LISTENER_NAME_FIELD,
								LISTENER_QUEUE_NAME_FIELD, LISTENER_POLLER_DELAY_FIELD,
								LISTENER_POLLER_MAX_MESSAGES_FIELD, LISTENER_IS_ENABLED_FIELD },
						MESSAGING_EVENT_SUBSCRIPTION_TABLE, LISTENER_JOIN));
		TradistaDBUtil.addParameterizedFilter(query, ES_EVENT_TYPE_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(query.toString())) {
			stmt.setString(1, eventType);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					MessagingListener listener = buildListener(rs);
					subscriptions.add(new MessagingEventSubscription(eventType, listener));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return new ArrayList<>(subscriptions);
	}

	public static List<String> getQueueNamesByEventType(String eventType) {
		Set<String> queueNames = new TreeSet<>();
		StringBuilder query = new StringBuilder(TradistaDBUtil.buildSelectQuery(LISTENER_QUEUE_NAME_FIELD,
				MESSAGING_EVENT_SUBSCRIPTION_TABLE, LISTENER_JOIN));
		TradistaDBUtil.addParameterizedFilter(query, ES_EVENT_TYPE_FIELD);
		TradistaDBUtil.addFilter(query, LISTENER_IS_ENABLED_FIELD, true);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(query.toString())) {
			stmt.setString(1, eventType);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					queueNames.add(rs.getString(LISTENER_QUEUE_NAME_FIELD.getName()));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return new ArrayList<>(queueNames);
	}

	public static void saveSubscriptions(String eventType, Set<Long> listenerIds) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDelete = TradistaDBUtil.buildDeletePreparedStatement(con,
						MESSAGING_EVENT_SUBSCRIPTION_TABLE, ES_EVENT_TYPE_FIELD);
				PreparedStatement stmtInsert = TradistaDBUtil.buildInsertPreparedStatement(con,
						MESSAGING_EVENT_SUBSCRIPTION_TABLE, EVENT_SUBSCRIPTION_FIELDS)) {
			stmtDelete.setString(1, eventType);
			stmtDelete.executeUpdate();

			if (!CollectionUtils.isEmpty(listenerIds)) {
				for (Long listenerId : listenerIds) {
					stmtInsert.clearParameters();
					stmtInsert.setString(1, eventType);
					stmtInsert.setLong(2, listenerId);
					stmtInsert.addBatch();
				}
				stmtInsert.executeBatch();
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	// --- HELPER BUILDERS ---

	private static MessagingListener buildListener(ResultSet rs) throws SQLException {
		MessagingListener listener = new MessagingListener(rs.getString(LISTENER_NAME_FIELD.getName()),
				rs.getString(LISTENER_QUEUE_NAME_FIELD.getName()), rs.getLong(LISTENER_POLLER_DELAY_FIELD.getName()),
				rs.getInt(LISTENER_POLLER_MAX_MESSAGES_FIELD.getName()),
				rs.getBoolean(LISTENER_IS_ENABLED_FIELD.getName()));
		listener.setId(rs.getLong(LISTENER_ID_FIELD.getName()));
		return listener;
	}

	private static MessagingFilter buildFilter(ResultSet rs) throws SQLException {
		MessagingFilter filter = new MessagingFilter(rs.getString(FILTER_NAME_FIELD.getName()),
				rs.getString(FILTER_CLASS_NAME_FIELD.getName()));
		filter.setId(rs.getLong(FILTER_ID_FIELD.getName()));
		return filter;
	}

}