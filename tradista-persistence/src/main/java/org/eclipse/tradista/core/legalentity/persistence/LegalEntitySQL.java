package org.eclipse.tradista.core.legalentity.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

public class LegalEntitySQL {

	public static long saveLegalEntity(LegalEntity legalEntity) {
		long legalEntityId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveLegalEntity = (legalEntity.getId() == 0) ? con.prepareStatement(
						"INSERT INTO LEGAL_ENTITY(SHORT_NAME, LONG_NAME, ROLE, DESCRIPTION) VALUES (?, ?, ?, ?) ",
						Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE LEGAL_ENTITY SET SHORT_NAME=?, LONG_NAME=?, ROLE=?, DESCRIPTION=? WHERE ID=?")) {
			if (legalEntity.getId() != 0) {
				stmtSaveLegalEntity.setLong(5, legalEntity.getId());
			}
			stmtSaveLegalEntity.setString(1, legalEntity.getShortName());
			stmtSaveLegalEntity.setString(2, legalEntity.getLongName());
			stmtSaveLegalEntity.setString(3, legalEntity.getRole().name());
			stmtSaveLegalEntity.setString(4, legalEntity.getDescription());
			stmtSaveLegalEntity.executeUpdate();

			if (legalEntity.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveLegalEntity.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						legalEntityId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating legal entity failed, no generated key obtained.");
					}
				}
			} else {
				legalEntityId = legalEntity.getId();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		legalEntity.setId(legalEntityId);
		return legalEntityId;
	}

	public static LegalEntity getLegalEntityById(long id) {
		LegalEntity legalEntity = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetLegalEntityById = con
						.prepareStatement("SELECT * FROM LEGAL_ENTITY WHERE LEGAL_ENTITY.ID = ? ")) {
			stmtGetLegalEntityById.setLong(1, id);
			try (ResultSet results = stmtGetLegalEntityById.executeQuery()) {
				while (results.next()) {
					legalEntity = new LegalEntity(results.getString("short_name"));
					legalEntity.setId(results.getLong("id"));
					legalEntity.setLongName(results.getString("long_name"));
					legalEntity.setDescription(results.getString("description"));
					legalEntity.setRole(LegalEntity.Role.valueOf(results.getString("role")));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return legalEntity;
	}

	public static Set<LegalEntity> getAllLegalEntities() {
		Set<LegalEntity> legalEntities = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllLegalEntities = con.prepareStatement("SELECT * FROM LEGAL_ENTITY");
				ResultSet results = stmtGetAllLegalEntities.executeQuery()) {
			while (results.next()) {
				if (legalEntities == null) {
					legalEntities = new HashSet<LegalEntity>();
				}
				LegalEntity legalEntity = new LegalEntity(results.getString("short_name"));
				legalEntity.setId(results.getInt("id"));
				legalEntity.setLongName(results.getString("long_name"));
				legalEntity.setDescription(results.getString("description"));
				legalEntity.setRole(LegalEntity.Role.valueOf(results.getString("role")));
				legalEntities.add(legalEntity);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return legalEntities;
	}

	public static Set<LegalEntity> getAllProcessingOrgs() {
		Set<LegalEntity> pos = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllLegalEntities = con
						.prepareStatement("SELECT * FROM LEGAL_ENTITY WHERE ROLE = 'PROCESSING_ORG'");
				ResultSet results = stmtGetAllLegalEntities.executeQuery()) {
			while (results.next()) {
				if (pos == null) {
					pos = new HashSet<LegalEntity>();
				}
				LegalEntity legalEntity = new LegalEntity(results.getString("short_name"));
				legalEntity.setId(results.getInt("id"));
				legalEntity.setLongName(results.getString("long_name"));
				legalEntity.setDescription(results.getString("description"));
				legalEntity.setRole(LegalEntity.Role.valueOf(results.getString("role")));
				pos.add(legalEntity);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return pos;
	}

	public static Set<LegalEntity> getAllCounterparties() {
		Set<LegalEntity> pos = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllLegalEntities = con
						.prepareStatement("SELECT * FROM LEGAL_ENTITY WHERE ROLE = 'COUNTERPARTY'");
				ResultSet results = stmtGetAllLegalEntities.executeQuery()) {
			while (results.next()) {
				if (pos == null) {
					pos = new HashSet<LegalEntity>();
				}
				LegalEntity legalEntity = new LegalEntity(results.getString("short_name"));
				legalEntity.setId(results.getInt("id"));
				legalEntity.setLongName(results.getString("long_name"));
				legalEntity.setDescription(results.getString("description"));
				legalEntity.setRole(LegalEntity.Role.valueOf(results.getString("role")));
				pos.add(legalEntity);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return pos;
	}

	public static Set<LegalEntity> getLegalEntitiesByShortNameAndRole(String shortName, LegalEntity.Role role) {
		Set<LegalEntity> legalEntities = null;
		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {
			String query = "SELECT * FROM LEGAL_ENTITY WHERE SHORT_NAME ";
			if (!shortName.contains("%")) {
				query += " = '" + shortName + "'";
			} else {
				query += " LIKE '" + shortName + "'";
			}
			if (role != null) {
				query += " AND ROLE = '" + role.name() + "'";
			}
			try (ResultSet results = stmt.executeQuery(query)) {
				while (results.next()) {
					LegalEntity legalEntity = new LegalEntity(results.getString("short_name"));
					legalEntity.setId(results.getInt("id"));
					legalEntity.setLongName(results.getString("long_name"));
					legalEntity.setDescription(results.getString("description"));
					legalEntity.setRole(LegalEntity.Role.valueOf(results.getString("role")));
					if (legalEntities == null) {
						legalEntities = new HashSet<LegalEntity>();
					}
					legalEntities.add(legalEntity);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return legalEntities;
	}

	public static LegalEntity getLegalEntityByShortName(String shortName) {
		LegalEntity legalEntity = null;
		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {
			String query = "SELECT * FROM LEGAL_ENTITY WHERE SHORT_NAME ";
			query += " = '" + shortName + "'";
			try (ResultSet results = stmt.executeQuery(query)) {
				while (results.next()) {
					legalEntity = new LegalEntity(results.getString("short_name"));
					legalEntity.setId(results.getInt("id"));
					legalEntity.setLongName(results.getString("long_name"));
					legalEntity.setDescription(results.getString("description"));
					legalEntity.setRole(LegalEntity.Role.valueOf(results.getString("role")));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return legalEntity;
	}

	public static LegalEntity getLegalEntityByLongName(String longName) {
		LegalEntity legalEntity = null;
		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {
			String query = "SELECT * FROM LEGAL_ENTITY WHERE LONG_NAME ";
			query += " = '" + longName + "'";
			try (ResultSet results = stmt.executeQuery(query)) {
				while (results.next()) {
					legalEntity = new LegalEntity(results.getString("short_name"));
					legalEntity.setId(results.getInt("id"));
					legalEntity.setLongName(results.getString("long_name"));
					legalEntity.setDescription(results.getString("description"));
					legalEntity.setRole(LegalEntity.Role.valueOf(results.getString("role")));
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return legalEntity;
	}

}