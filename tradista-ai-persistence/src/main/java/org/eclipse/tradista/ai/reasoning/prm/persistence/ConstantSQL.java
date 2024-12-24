package org.eclipse.tradista.ai.reasoning.prm.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.tradista.ai.reasoning.prm.model.Constant;
import org.eclipse.tradista.ai.reasoning.prm.model.Type;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public class ConstantSQL {

	public static long saveConstant(Constant constant) {
		long constantId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveValue = (constant.getId() == 0)
						? con.prepareStatement("INSERT INTO VALUE(TYPE, NAME) VALUES (?, ?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement("UPDATE VALUE SET TYPE = ?,  NAME = ? WHERE ID = ? ");
				PreparedStatement stmtSaveConstant = (constant.getId() == 0)
						? con.prepareStatement("INSERT INTO CONSTANT(CONSTANT_ID) VALUES (?) ")
						: null) {
			stmtSaveValue.setString(1, constant.getType().getName());
			stmtSaveValue.setString(2, constant.getName());
			if (constant.getId() != 0) {
				stmtSaveValue.setLong(3, constant.getId());
			}
			stmtSaveValue.executeUpdate();

			if (constant.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveValue.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						constantId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating constant failed, no generated key obtained.");
					}
				}
			} else {
				constantId = constant.getId();
			}

			if (constant.getId() == 0) {
				stmtSaveConstant.setLong(1, constant.getId());
				stmtSaveConstant.executeUpdate();
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		constant.setId(constantId);
		return constantId;
	}

	public static Constant getConstantByNameAndType(String name, Type type) {
		Constant constant = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetConstantByNameAndType = con.prepareStatement(
						"SELECT ID, NAME, TYPE FROM VALUE, CONSTANT WHERE ID = CONSTANT_ID AND NAME = ? AND TYPE = ?")) {
			stmtGetConstantByNameAndType.setString(1, name);
			stmtGetConstantByNameAndType.setString(2, type.getName());
			try (ResultSet results = stmtGetConstantByNameAndType.executeQuery()) {
				while (results.next()) {
					constant = new Constant(new Type(results.getString("type")), results.getString("name"));
					constant.setId(results.getLong("id"));
				}
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return constant;
	}

	public static Constant getConstantById(long id) {
		Constant constant = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetConstantById = con.prepareStatement(
						"SELECT ID, NAME, TYPE FROM VALUE, CONSTANT WHERE ID = CONSTANT_ID AND NAME = ? AND TYPE = ?")) {
			stmtGetConstantById.setLong(1, id);
			try (ResultSet results = stmtGetConstantById.executeQuery()) {
				while (results.next()) {
					constant = new Constant(new Type(results.getString("type")), results.getString("name"));
					constant.setId(results.getLong("id"));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return constant;
	}

}