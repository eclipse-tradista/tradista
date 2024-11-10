package org.eclipse.tradista.core.marketdata.persistence;

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
import org.eclipse.tradista.core.marketdata.model.QuoteSet;

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

public class QuoteSetSQL {

	public static long saveQuoteSet(QuoteSet quoteSet) {
		long quoteSetId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveQuoteSet = (quoteSet.getId() == 0)
						? con.prepareStatement("INSERT INTO QUOTE_SET(NAME, PROCESSING_ORG_ID) VALUES(?, ?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement("UPDATE QUOTE_SET SET NAME=?, PROCESSING_ORG_ID=? WHERE ID=?")) {
			if (quoteSet.getId() != 0) {
				stmtSaveQuoteSet.setLong(3, quoteSet.getId());
			}
			stmtSaveQuoteSet.setString(1, quoteSet.getName());
			LegalEntity po = quoteSet.getProcessingOrg();
			if (po == null) {
				stmtSaveQuoteSet.setNull(2, Types.BIGINT);
			} else {
				stmtSaveQuoteSet.setLong(2, po.getId());
			}
			stmtSaveQuoteSet.executeUpdate();

			if (quoteSet.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveQuoteSet.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						quoteSetId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("QuoteSet creation failed, no generated key obtained.");
					}
				}
			} else {
				quoteSetId = quoteSet.getId();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		quoteSet.setId(quoteSetId);
		return quoteSetId;
	}

	public static boolean deleteQuoteSet(long quoteSetId) {
		boolean bSaved = false;
		QuoteSQL.deleteQuoteValues(quoteSetId);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteQuoteSet = con.prepareStatement("DELETE FROM QUOTE_SET WHERE ID = ? ")) {
			stmtDeleteQuoteSet.setLong(1, quoteSetId);
			stmtDeleteQuoteSet.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<QuoteSet> getAllQuoteSets() {
		Set<QuoteSet> quoteSets = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllQuoteSets = con.prepareStatement("SELECT * FROM QUOTE_SET");
				ResultSet results = stmtGetAllQuoteSets.executeQuery()) {
			while (results.next()) {
				long quoteSetId = results.getLong("id");
				String quoteSetName = results.getString("name");
				long poId = results.getLong("processing_org_id");
				LegalEntity po = null;
				if (poId > 0) {
					po = LegalEntitySQL.getLegalEntityById(results.getLong("processing_org_id"));
				}
				QuoteSet quoteSet = new QuoteSet(quoteSetId, quoteSetName, po);
				if (quoteSets == null) {
					quoteSets = new HashSet<QuoteSet>();
				}
				quoteSets.add(quoteSet);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quoteSets;
	}

	public static QuoteSet getQuoteSetByName(String quoteSetName) {
		QuoteSet quoteSet = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteSetByName = con
						.prepareStatement("SELECT * FROM QUOTE_SET WHERE NAME = ?")) {
			stmtGetQuoteSetByName.setString(1, quoteSetName);
			try (ResultSet results = stmtGetQuoteSetByName.executeQuery()) {
				while (results.next()) {
					long quoteSetId = results.getLong("id");
					long poId = results.getLong("processing_org_id");
					LegalEntity po = null;
					if (poId > 0) {
						po = LegalEntitySQL.getLegalEntityById(results.getLong("processing_org_id"));
					}
					quoteSet = new QuoteSet(quoteSetId, quoteSetName, po);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quoteSet;
	}

	public static QuoteSet getQuoteSetByNameAndPo(String quoteSetName, long poId) {
		QuoteSet quoteSet = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteSetByName = con
						.prepareStatement("SELECT * FROM QUOTE_SET WHERE NAME = ? AND PROCESSING_ORG_ID=?")) {
			stmtGetQuoteSetByName.setString(1, quoteSetName);
			stmtGetQuoteSetByName.setLong(2, poId);
			try (ResultSet results = stmtGetQuoteSetByName.executeQuery()) {
				while (results.next()) {
					long quoteSetId = results.getLong("id");
					LegalEntity po = null;
					if (poId > 0) {
						po = LegalEntitySQL.getLegalEntityById(poId);
					}
					quoteSet = new QuoteSet(quoteSetId, quoteSetName, po);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quoteSet;
	}

	public static QuoteSet getQuoteSetById(long quoteSetId) {

		QuoteSet quoteSet = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteSetById = con.prepareStatement("SELECT * FROM QUOTE_SET WHERE ID = ?")) {
			stmtGetQuoteSetById.setLong(1, quoteSetId);
			try (ResultSet results = stmtGetQuoteSetById.executeQuery()) {
				while (results.next()) {
					String quoteSetName = results.getString("name");
					long poId = results.getLong("processing_org_id");
					LegalEntity po = null;
					if (poId > 0) {
						po = LegalEntitySQL.getLegalEntityById(results.getLong("processing_org_id"));
					}
					quoteSet = new QuoteSet(quoteSetId, quoteSetName, po);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return quoteSet;
	}

}