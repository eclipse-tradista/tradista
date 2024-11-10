package org.eclipse.tradista.security.gcrepo.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.security.bond.persistence.BondSQL;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.equity.persistence.EquitySQL;
import org.eclipse.tradista.security.gcrepo.model.GCBasket;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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

public class GCBasketSQL {

	public static long saveGCBasket(GCBasket gcBasket) {
		long gcBasketId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveGCBasket = (gcBasket.getId() == 0)
						? con.prepareStatement("INSERT INTO GCBASKET(NAME) VALUES (?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement("UPDATE GCBASKET SET NAME=? WHERE ID=? ");
				PreparedStatement stmtDeleteGCBasketSecurity = con
						.prepareStatement("DELETE FROM GCBASKET_SECURITY WHERE GCBASKET_ID = ? ");
				PreparedStatement stmtSaveGCBasketSecurity = con
						.prepareStatement("INSERT INTO GCBASKET_SECURITY VALUES (?, ?) ");) {
			if (gcBasket.getId() != 0) {
				stmtSaveGCBasket.setLong(2, gcBasket.getId());
			}
			stmtSaveGCBasket.setString(1, gcBasket.getName());
			stmtSaveGCBasket.executeUpdate();

			if (gcBasket.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveGCBasket.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						gcBasketId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating gc basket failed, no generated key obtained.");
					}
				}
			} else {
				gcBasketId = gcBasket.getId();
			}
			if (gcBasket.getId() != 0) {
				stmtDeleteGCBasketSecurity.setLong(1, gcBasket.getId());
				stmtDeleteGCBasketSecurity.executeUpdate();
			}
			if (gcBasket.getSecurities() != null) {
				for (Security security : gcBasket.getSecurities()) {
					stmtSaveGCBasketSecurity.clearParameters();
					stmtSaveGCBasketSecurity.setLong(1, gcBasketId);
					stmtSaveGCBasketSecurity.setLong(2, security.getId());
					stmtSaveGCBasketSecurity.addBatch();
				}
			}

			stmtSaveGCBasketSecurity.executeBatch();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		gcBasket.setId(gcBasketId);
		return gcBasketId;
	}

	public static Set<GCBasket> getAllGCBaskets() {
		Set<GCBasket> gcBaskets = null;
		Map<Long, GCBasket> gcBasketsMap = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllGCBaskets = con.prepareStatement(
						"SELECT GCBASKET.NAME, GCBASKET.ID, GCBASKET_SECURITY.SECURITY_ID FROM GCBASKET LEFT OUTER JOIN GCBASKET_SECURITY ON GCBASKET.ID = GCBASKET_SECURITY.GCBASKET_ID");
				ResultSet results = stmtGetAllGCBaskets.executeQuery()) {
			while (results.next()) {
				if (gcBasketsMap == null) {
					gcBasketsMap = new HashMap<>();
				}
				long basketId = results.getLong(2);

				GCBasket gcBasket;
				if (gcBasketsMap.containsKey(basketId)) {
					gcBasket = gcBasketsMap.get(basketId);
				} else {
					gcBasket = new GCBasket();
					gcBasket.setName(results.getString(1));
					gcBasket.setId(results.getLong(2));
				}
				Long securityId = results.getLong(3);
				if (securityId != 0) {
					Set<Security> securities;
					if (gcBasket.getSecurities() == null) {
						securities = new HashSet<>();
					} else {
						securities = gcBasket.getSecurities();
					}

					Security security = BondSQL.getBondById(securityId);
					if (security == null) {
						security = EquitySQL.getEquityById(securityId);
					}
					securities.add(security);
					gcBasket.setSecurities(securities);
				}
				gcBasketsMap.put(basketId, gcBasket);

			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		if (gcBasketsMap != null) {
			gcBaskets = new HashSet<>(gcBasketsMap.values());
		}
		return gcBaskets;
	}

	public static GCBasket getGCBasketById(long id) {
		GCBasket gcBasket = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetGCBasketById = con.prepareStatement(
						"SELECT GCBASKET.NAME, GCBASKET.ID, GCBASKET_SECURITY.SECURITY_ID FROM GCBASKET LEFT OUTER JOIN GCBASKET_SECURITY ON GCBASKET.ID = GCBASKET_SECURITY.GCBASKET_ID WHERE GCBASKET.ID = ?");) {
			stmtGetGCBasketById.setLong(1, id);
			ResultSet results = stmtGetGCBasketById.executeQuery();
			while (results.next()) {
				if (gcBasket == null) {
					gcBasket = new GCBasket();
					gcBasket.setName(results.getString(1));
					gcBasket.setId(results.getLong(2));
				}
				Long securityId = results.getLong(3);
				if (securityId != 0) {
					Set<Security> securities;
					if (gcBasket.getSecurities() == null) {
						securities = new HashSet<>();
					} else {
						securities = gcBasket.getSecurities();
					}

					Security security = BondSQL.getBondById(securityId);
					if (security == null) {
						security = EquitySQL.getEquityById(securityId);
					}
					securities.add(security);
					gcBasket.setSecurities(securities);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return gcBasket;
	}

	public static GCBasket getGCBasketByName(String name) {
		GCBasket gcBasket = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetGCBasketByName = con.prepareStatement(
						"SELECT GCBASKET.NAME, GCBASKET.ID, GCBASKET_SECURITY.SECURITY_ID FROM GCBASKET LEFT OUTER JOIN GCBASKET_SECURITY ON GCBASKET.ID = GCBASKET_SECURITY.GCBASKET_ID WHERE GCBASKET.NAME = ?");) {
			stmtGetGCBasketByName.setString(1, name);
			ResultSet results = stmtGetGCBasketByName.executeQuery();
			while (results.next()) {
				if (gcBasket == null) {
					gcBasket = new GCBasket();
					gcBasket.setName(results.getString(1));
					gcBasket.setId(results.getLong(2));
				}
				Long securityId = results.getLong(3);
				if (securityId != 0) {
					Set<Security> securities;
					if (gcBasket.getSecurities() == null) {
						securities = new HashSet<>();
					} else {
						securities = gcBasket.getSecurities();
					}

					Security security = BondSQL.getBondById(securityId);
					if (security == null) {
						security = EquitySQL.getEquityById(securityId);
					}
					securities.add(security);
					gcBasket.setSecurities(securities);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return gcBasket;
	}

}