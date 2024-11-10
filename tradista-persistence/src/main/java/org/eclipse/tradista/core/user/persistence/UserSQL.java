package org.eclipse.tradista.core.user.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
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

	public static User getUserById(long id) {
		User user = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetUserById = con.prepareStatement("SELECT * FROM TRADISTA_USER WHERE ID = ? ")) {
			stmtGetUserById.setLong(1, id);
			try (ResultSet results = stmtGetUserById.executeQuery()) {
				while (results.next()) {
					user = new User(results.getString("first_name"), results.getString("surname"),
							LegalEntitySQL.getLegalEntityById(results.getLong("processing_org_id")));
					user.setId(results.getLong("id"));
					user.setLogin(results.getString("login"));
					user.setPassword(results.getString("password"));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return user;
	}

	public static User getUserByLogin(String login) {
		User user = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetUserByLogin = con
						.prepareStatement("SELECT * FROM TRADISTA_USER WHERE LOGIN = ? ")) {
			stmtGetUserByLogin.setString(1, login);
			try (ResultSet results = stmtGetUserByLogin.executeQuery()) {
				while (results.next()) {
					user = new User(results.getString("first_name"), results.getString("surname"),
							LegalEntitySQL.getLegalEntityById(results.getLong("processing_org_id")));
					user.setId(results.getLong("id"));
					user.setLogin(results.getString("login"));
					user.setPassword(results.getString("password"));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return user;
	}

	public static boolean userLoginExists(String login) {
		boolean exists = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtBookExists = con
						.prepareStatement("SELECT 1 FROM TRADISTA_USER WHERE LOGIN = ? ")) {
			stmtBookExists.setString(1, login);
			try (ResultSet results = stmtBookExists.executeQuery()) {
				while (results.next()) {
					exists = true;
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return exists;
	}

	public static Set<User> getAllUsers() {
		Set<User> users = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllUsers = con.prepareStatement("SELECT * FROM TRADISTA_USER");
				ResultSet results = stmtGetAllUsers.executeQuery()) {
			while (results.next()) {
				if (users == null) {
					users = new HashSet<User>();
				}
				User user = new User(results.getString("first_name"), results.getString("surname"),
						LegalEntitySQL.getLegalEntityById(results.getLong("processing_org_id")));
				user.setId(results.getLong("id"));
				user.setLogin(results.getString("login"));
				user.setPassword(results.getString("password"));
				users.add(user);
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return users;
	}

	public static long saveUser(User user) {
		long userId = 0;
		LegalEntity po = user.getProcessingOrg();
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveUser = (user.getId() == 0) ? con.prepareStatement(
						"INSERT INTO TRADISTA_USER(FIRST_NAME, SURNAME, PROCESSING_ORG_ID, LOGIN, PASSWORD) VALUES (?, ?, ?, ?, ?) ",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE TRADISTA_USER SET FIRST_NAME=?, SURNAME=?, PROCESSING_ORG_ID=?, LOGIN=?, PASSWORD=? WHERE ID=?")) {
			if (user.getId() != 0) {
				stmtSaveUser.setLong(6, user.getId());
			}
			stmtSaveUser.setString(1, user.getFirstName());
			stmtSaveUser.setString(2, user.getSurname());
			if (po != null) {
				stmtSaveUser.setLong(3, user.getProcessingOrg().getId());
			} else {
				stmtSaveUser.setNull(3, Types.BIGINT);
			}
			stmtSaveUser.setString(4, user.getLogin());
			stmtSaveUser.setString(5, user.getPassword());
			stmtSaveUser.executeUpdate();
			if (user.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveUser.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						userId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating user failed, no generated key obtained.");
					}
				}
			} else {
				userId = user.getId();
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		user.setId(userId);
		return userId;
	}

	public static Set<User> getUsersBySurname(String surname) {
		Set<User> users = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetUsersBySurname = con
						.prepareStatement("SELECT * FROM TRADISTA_USER WHERE SURNAME = ? ")) {
			stmtGetUsersBySurname.setString(1, surname);
			try (ResultSet results = stmtGetUsersBySurname.executeQuery()) {
				while (results.next()) {
					User user = new User(results.getString("first_name"), results.getString("surname"),
							LegalEntitySQL.getLegalEntityById(results.getLong("processing_org_id")));
					user.setId(results.getLong("id"));
					user.setLogin(results.getString("login"));
					user.setPassword(results.getString("password"));
					if (users == null) {
						users = new HashSet<User>();
					}
					users.add(user);
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return users;
	}

}