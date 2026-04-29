package org.eclipse.tradista.core.user.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PROCESSING_ORG_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.user.model.User;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

public class UserSQL {

	private static final String FIRST_NAME = "FIRST_NAME";
	private static final String SURNAME = "SURNAME";
	private static final String LOGIN = "LOGIN";
	private static final String PASSWORD = "PASSWORD";

	private static final Field FIRST_NAME_FIELD = new Field(FIRST_NAME);
	private static final Field SURNAME_FIELD = new Field(SURNAME);
	private static final Field PROCESSING_ORG_ID_FIELD = new Field(PROCESSING_ORG_ID);
	private static final Field LOGIN_FIELD = new Field(LOGIN);
	private static final Field PASSWORD_FIELD = new Field(PASSWORD);
	private static final Field ID_FIELD = new Field(ID);

	private static final Field[] FIELDS = { FIRST_NAME_FIELD, SURNAME_FIELD, PROCESSING_ORG_ID_FIELD, LOGIN_FIELD,
			PASSWORD_FIELD, ID_FIELD };
	private static final Field[] FIELDS_FOR_INSERT_OR_UPDATE = { FIRST_NAME_FIELD, SURNAME_FIELD, PROCESSING_ORG_ID_FIELD,
			LOGIN_FIELD, PASSWORD_FIELD };

	public static final Table TABLE = new Table("TRADISTA_USER", FIELDS);

	public static User getUserById(long id) {
		User user = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetUserById = con.prepareStatement(sql.toString())) {
			stmtGetUserById.setLong(1, id);
			try (ResultSet results = stmtGetUserById.executeQuery()) {
				while (results.next()) {
					user = buildUser(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return user;
	}

	public static User getUserByLogin(String login) {
		User user = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, LOGIN_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetUserByLogin = con.prepareStatement(sql.toString())) {
			stmtGetUserByLogin.setString(1, login);
			try (ResultSet results = stmtGetUserByLogin.executeQuery()) {
				while (results.next()) {
					user = buildUser(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return user;
	}

	public static boolean userLoginExists(String login) {
		boolean exists = false;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(ID_FIELD, TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, LOGIN_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtUserExists = con.prepareStatement(sql.toString())) {
			stmtUserExists.setString(1, login);
			try (ResultSet results = stmtUserExists.executeQuery()) {
				if (results.next()) {
					exists = true;
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return exists;
	}

	public static Set<User> getAllUsers() {
		Set<User> users = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllUsers = con.prepareStatement(TradistaDBUtil.buildSelectQuery(TABLE));
				ResultSet results = stmtGetAllUsers.executeQuery()) {
			while (results.next()) {
				if (users == null) {
					users = new HashSet<>();
				}
				users.add(buildUser(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return users;
	}

	public static long saveUser(User user) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveUser = (user.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, TABLE, FIELDS_FOR_INSERT_OR_UPDATE)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, TABLE, FIELDS_FOR_INSERT_OR_UPDATE)) {
			if (user.getId() != 0) {
				stmtSaveUser.setLong(6, user.getId());
			}
			stmtSaveUser.setString(1, user.getFirstName());
			stmtSaveUser.setString(2, user.getSurname());
			if (user.getProcessingOrg() != null) {
				stmtSaveUser.setLong(3, user.getProcessingOrg().getId());
			} else {
				stmtSaveUser.setNull(3, java.sql.Types.BIGINT);
			}
			stmtSaveUser.setString(4, user.getLogin());
			stmtSaveUser.setString(5, user.getPassword());
			stmtSaveUser.executeUpdate();
			if (user.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveUser.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						user.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("Creating user failed, no generated key obtained.");
					}
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return user.getId();
	}

	public static Set<User> getUsersBySurname(String surname) {
		Set<User> users = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, SURNAME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetUsersBySurname = con.prepareStatement(sql.toString())) {
			stmtGetUsersBySurname.setString(1, surname);
			try (ResultSet results = stmtGetUsersBySurname.executeQuery()) {
				while (results.next()) {
					if (users == null) {
						users = new HashSet<>();
					}
					users.add(buildUser(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return users;
	}

	public static Set<User> getUsersByPoId(long poId) {
		Set<User> users = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PROCESSING_ORG_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetUsersByPoId = con.prepareStatement(sql.toString())) {
			stmtGetUsersByPoId.setLong(1, poId);
			try (ResultSet results = stmtGetUsersByPoId.executeQuery()) {
				while (results.next()) {
					if (users == null) {
						users = new HashSet<>();
					}
					users.add(buildUser(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return users;
	}

	public static User getUserByFirstNameSurnameAndPoId(String firstName, String surname, long poId) {
		User user = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, FIRST_NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, SURNAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, PROCESSING_ORG_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString())) {
			stmt.setString(1, firstName);
			stmt.setString(2, surname);
			stmt.setLong(3, poId);
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					user = buildUser(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return user;
	}

	private static User buildUser(ResultSet results) throws SQLException {
		LegalEntity processingOrg = LegalEntitySQL
				.getLegalEntityById(results.getLong(PROCESSING_ORG_ID_FIELD.getName()));
		User user = new User(results.getString(FIRST_NAME_FIELD.getName()), results.getString(SURNAME_FIELD.getName()),
				processingOrg);
		user.setId(results.getLong(ID_FIELD.getName()));
		user.setLogin(results.getString(LOGIN_FIELD.getName()));
		user.setPassword(results.getString(PASSWORD_FIELD.getName()));
		return user;
	}

}