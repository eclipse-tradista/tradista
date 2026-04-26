package org.eclipse.tradista.core.marketdata.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PROCESSING_ORG_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
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

	private static final Field QUOTE_SET_ID_FIELD = new Field(ID);

	private static final Field QUOTE_SET_NAME_FIELD = new Field(NAME);

	private static final Field QUOTE_SET_PROCESSING_ORG_ID_FIELD = new Field(PROCESSING_ORG_ID);

	private static final Field[] QUOTE_SET_FIELDS = { QUOTE_SET_ID_FIELD, QUOTE_SET_NAME_FIELD,
			QUOTE_SET_PROCESSING_ORG_ID_FIELD };

	private static final Table QUOTE_SET_TABLE = new Table("QUOTE_SET", QUOTE_SET_FIELDS);

	public static long saveQuoteSet(QuoteSet quoteSet) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveQuoteSet = (quoteSet.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, QUOTE_SET_TABLE, QUOTE_SET_NAME_FIELD,
								QUOTE_SET_PROCESSING_ORG_ID_FIELD)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, QUOTE_SET_ID_FIELD, QUOTE_SET_TABLE,
								QUOTE_SET_NAME_FIELD, QUOTE_SET_PROCESSING_ORG_ID_FIELD)) {
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
						quoteSet.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("QuoteSet creation failed, no generated key obtained.");
					}
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quoteSet.getId();
	}

	public static boolean deleteQuoteSet(long quoteSetId) {
		boolean bSaved = false;
		QuoteSQL.deleteQuoteValues(quoteSetId);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteQuoteSet = TradistaDBUtil.buildDeletePreparedStatement(con, QUOTE_SET_TABLE,
						QUOTE_SET_ID_FIELD)) {
			stmtDeleteQuoteSet.setLong(1, quoteSetId);
			stmtDeleteQuoteSet.executeUpdate();
			bSaved = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bSaved;
	}

	public static Set<QuoteSet> getAllQuoteSets() {
		return getQuoteSets(null, null, null);
	}

	public static Set<QuoteSet> getQuoteSetsByPoId(long poId) {
		return getQuoteSets(null, poId, null);
	}

	public static QuoteSet getQuoteSetByName(String name) {
		Set<QuoteSet> quoteSets = getQuoteSets(name, null, null);
		return (quoteSets == null || quoteSets.isEmpty()) ? null : quoteSets.iterator().next();
	}

	public static QuoteSet getQuoteSetByNameAndPo(String name, long poId) {
		Set<QuoteSet> quoteSets = getQuoteSets(name, poId, null);
		return (quoteSets == null || quoteSets.isEmpty()) ? null : quoteSets.iterator().next();
	}

	public static QuoteSet getQuoteSetById(long id) {
		Set<QuoteSet> quoteSets = getQuoteSets(null, null, id);
		return (quoteSets == null || quoteSets.isEmpty()) ? null : quoteSets.iterator().next();
	}

	private static Set<QuoteSet> getQuoteSets(String name, Long poId, Long id) {
		Set<QuoteSet> quoteSets = null;
		StringBuilder query = new StringBuilder(TradistaDBUtil.buildSelectQuery(QUOTE_SET_TABLE));
		if (name != null) {
			TradistaDBUtil.addParameterizedFilter(query, QUOTE_SET_NAME_FIELD);
		}
		if (poId != null) {
			TradistaDBUtil.addParameterizedFilter(query, QUOTE_SET_PROCESSING_ORG_ID_FIELD);
		}
		if (id != null) {
			TradistaDBUtil.addParameterizedFilter(query, QUOTE_SET_ID_FIELD);
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetQuoteSets = con.prepareStatement(query.toString())) {
			int i = 1;
			if (name != null) {
				stmtGetQuoteSets.setString(i++, name);
			}
			if (poId != null) {
				stmtGetQuoteSets.setLong(i++, poId);
			}
			if (id != null) {
				stmtGetQuoteSets.setLong(i++, id);
			}
			try (ResultSet results = stmtGetQuoteSets.executeQuery()) {
				while (results.next()) {
					if (quoteSets == null) {
						quoteSets = new HashSet<>();
					}
					long resId = results.getLong(QUOTE_SET_ID_FIELD.getName());
					String resName = results.getString(QUOTE_SET_NAME_FIELD.getName());
					long resPoId = results.getLong(QUOTE_SET_PROCESSING_ORG_ID_FIELD.getName());
					LegalEntity po = null;
					if (resPoId > 0) {
						po = LegalEntitySQL.getLegalEntityById(resPoId);
					}
					quoteSets.add(new QuoteSet(resId, resName, po));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return quoteSets;
	}

}