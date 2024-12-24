package org.eclipse.tradista.core.index.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.index.model.Index;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

public class IndexSQL {

	public static Set<Index> getAllIndexes() {
		Set<Index> indexes = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllIndexes = con.prepareStatement("SELECT * FROM INDEX");
				ResultSet results = stmtGetAllIndexes.executeQuery()) {
			while (results.next()) {
				if (indexes == null) {
					indexes = new HashSet<Index>();
				}
				Index index = new Index(results.getString("name"));
				index.setId(results.getLong("id"));
				index.setDescription(results.getString("description"));
				index.setPrefixed(results.getBoolean("is_prefixed"));
				indexes.add(index);
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return indexes;
	}

	public static long saveIndex(Index index) {
		long indexId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveIndex = (index.getId() != 0)
						? con.prepareStatement("UPDATE INDEX SET NAME=?, DESCRIPTION=?, IS_PREFIXED=? WHERE ID = ?")
						: con.prepareStatement("INSERT INTO INDEX(NAME, DESCRIPTION, IS_PREFIXED) VALUES (?,?,?)",
								Statement.RETURN_GENERATED_KEYS)) {
			if (index.getId() != 0) {
				stmtSaveIndex.setLong(4, index.getId());
			}
			stmtSaveIndex.setString(1, index.getName());
			stmtSaveIndex.setString(2, index.getDescription());
			stmtSaveIndex.setBoolean(3, index.isPrefixed());
			stmtSaveIndex.executeUpdate();

			if (index.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveIndex.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						indexId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating index failed, no generated key obtained.");
					}
				}
			} else {
				indexId = index.getId();
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		index.setId(indexId);
		return indexId;
	}

	public static Index getIndexByName(String indexName) {
		Index index = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetIndexByName = con.prepareStatement("SELECT * FROM INDEX WHERE NAME = ?")) {
			stmtGetIndexByName.setString(1, indexName);
			try (ResultSet results = stmtGetIndexByName.executeQuery()) {
				while (results.next()) {
					index = new Index(results.getString("name"));
					index.setId(results.getLong("id"));
					index.setDescription(results.getString("description"));
					index.setPrefixed(results.getBoolean("is_prefixed"));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return index;
	}

	public static Index getIndexById(long indexId) {
		Index index = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetIndexById = con.prepareStatement("SELECT * FROM INDEX WHERE ID = ?")) {
			stmtGetIndexById.setLong(1, indexId);
			try (ResultSet results = stmtGetIndexById.executeQuery()) {
				while (results.next()) {
					index = new Index(results.getString("name"));
					index.setId(results.getLong("id"));
					index.setDescription(results.getString("description"));
					index.setPrefixed(results.getBoolean("is_prefixed"));
				}
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return index;
	}

}