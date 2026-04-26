package org.eclipse.tradista.core.marketdata.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.FROM;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.SELECT;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.FeedType;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;

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

public class FeedConfigSQL {

	public static final Field FEED_CONFIG_ID_FIELD = new Field("ID");

	public static final Field FEED_CONFIG_NAME_FIELD = new Field("NAME");

	public static final Field FEED_CONFIG_FEED_TYPE_FIELD = new Field("FEED_TYPE");

	public static final Field FEED_CONFIG_PROCESSING_ORG_ID_FIELD = new Field("PROCESSING_ORG_ID");

	private static final Field[] FEED_CONFIG_FIELDS = { FEED_CONFIG_ID_FIELD, FEED_CONFIG_NAME_FIELD,
			FEED_CONFIG_FEED_TYPE_FIELD, FEED_CONFIG_PROCESSING_ORG_ID_FIELD };

	public static final Table FEED_CONFIG_TABLE = new Table("FEED_CONFIG", FEED_CONFIG_FIELDS);

	public static final Field FEED_MAPPING_VALUE_FEED_CONFIG_ID_FIELD = new Field("FEED_CONFIG_ID");

	public static final Field FEED_MAPPING_VALUE_QUOTE_ID_FIELD = new Field("QUOTE_ID");

	public static final Field FEED_MAPPING_VALUE_FEED_QUOTE_NAME_FIELD = new Field("FEED_QUOTE_NAME");

	public static final Field FEED_MAPPING_VALUE_FEED_BID_FIELD = new Field("FEED_BID_FIELD");

	public static final Field FEED_MAPPING_VALUE_FEED_ASK_FIELD = new Field("FEED_ASK_FIELD");

	public static final Field FEED_MAPPING_VALUE_FEED_OPEN_FIELD = new Field("FEED_OPEN_FIELD");

	public static final Field FEED_MAPPING_VALUE_FEED_CLOSE_FIELD = new Field("FEED_CLOSE_FIELD");

	public static final Field FEED_MAPPING_VALUE_FEED_HIGH_FIELD = new Field("FEED_HIGH_FIELD");

	public static final Field FEED_MAPPING_VALUE_FEED_LOW_FIELD = new Field("FEED_LOW_FIELD");

	public static final Field FEED_MAPPING_VALUE_FEED_LAST_FIELD = new Field("FEED_LAST_FIELD");

	private static final Field[] FEED_MAPPING_VALUE_FIELDS = { FEED_MAPPING_VALUE_FEED_CONFIG_ID_FIELD,
			FEED_MAPPING_VALUE_QUOTE_ID_FIELD, FEED_MAPPING_VALUE_FEED_QUOTE_NAME_FIELD,
			FEED_MAPPING_VALUE_FEED_BID_FIELD, FEED_MAPPING_VALUE_FEED_ASK_FIELD, FEED_MAPPING_VALUE_FEED_OPEN_FIELD,
			FEED_MAPPING_VALUE_FEED_CLOSE_FIELD, FEED_MAPPING_VALUE_FEED_HIGH_FIELD, FEED_MAPPING_VALUE_FEED_LOW_FIELD,
			FEED_MAPPING_VALUE_FEED_LAST_FIELD };

	public static final Table FEED_MAPPING_VALUE_TABLE = new Table("FEED_MAPPING_VALUE", FEED_MAPPING_VALUE_FIELDS);

	public static boolean deleteFeedConfig(long id) {
		boolean deleted = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteFeedMappingValues = TradistaDBUtil.buildDeletePreparedStatement(con,
						FEED_MAPPING_VALUE_TABLE, FEED_MAPPING_VALUE_FEED_CONFIG_ID_FIELD);
				PreparedStatement stmtDeleteFeedConfig = TradistaDBUtil.buildDeletePreparedStatement(con,
						FEED_CONFIG_TABLE, FEED_CONFIG_ID_FIELD)) {
			stmtDeleteFeedMappingValues.setLong(1, id);
			stmtDeleteFeedMappingValues.executeUpdate();
			stmtDeleteFeedConfig.setLong(1, id);
			stmtDeleteFeedConfig.executeUpdate();
			deleted = true;
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return deleted;
	}

	public static Set<FeedConfig> getFeedConfigsByName(String feedConfigName) {
		return getFeedConfigs(feedConfigName, null, null);
	}

	public static FeedConfig getFeedConfigByNameAndPo(String feedConfigName, long poId) {
		Set<FeedConfig> feedConfigs = getFeedConfigs(feedConfigName, poId, null);
		return (feedConfigs == null || feedConfigs.isEmpty()) ? null : feedConfigs.iterator().next();
	}

	public static FeedConfig getFeedConfigById(long feedConfigId) {
		Set<FeedConfig> feedConfigs = getFeedConfigs(null, null, feedConfigId);
		return (feedConfigs == null || feedConfigs.isEmpty()) ? null : feedConfigs.iterator().next();
	}

	public static Set<FeedConfig> getAllFeedConfigs() {
		return getFeedConfigs(null, null, null);
	}

	public static Set<FeedConfig> getFeedConfigsByPoId(long poId) {
		return getFeedConfigs(null, poId, null);
	}

	private static Set<FeedConfig> getFeedConfigs(String name, Long poId, Long id) {
		Set<FeedConfig> feedConfigs = null;
		StringBuilder query = new StringBuilder(TradistaDBUtil.buildSelectQuery(FEED_CONFIG_TABLE, Join
				.leftOuterEq(FEED_MAPPING_VALUE_TABLE, FEED_CONFIG_ID_FIELD, FEED_MAPPING_VALUE_FEED_CONFIG_ID_FIELD)));
		if (name != null) {
			TradistaDBUtil.addParameterizedFilter(query, FEED_CONFIG_NAME_FIELD);
		}
		if (poId != null) {
			TradistaDBUtil.addParameterizedFilter(query, FEED_CONFIG_PROCESSING_ORG_ID_FIELD);
		}
		if (id != null) {
			TradistaDBUtil.addParameterizedFilter(query, FEED_CONFIG_ID_FIELD);
		}

		query.append(" ORDER BY " + FEED_CONFIG_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFeedConfigs = con.prepareStatement(query.toString())) {
			int i = 1;
			if (name != null) {
				stmtGetFeedConfigs.setString(i++, name);
			}
			if (poId != null) {
				stmtGetFeedConfigs.setLong(i++, poId);
			}
			if (id != null) {
				stmtGetFeedConfigs.setLong(i++, id);
			}
			try (ResultSet results = stmtGetFeedConfigs.executeQuery()) {
				FeedConfig feedConfig = null;
				long feedConfigId = 0;
				Map<String, Quote> mapping = new HashMap<>();
				Map<String, Map<String, String>> fieldsMapping = new HashMap<>();

				while (results.next()) {
					if (feedConfigs == null) {
						feedConfigs = new HashSet<>();
					}
					if (results.getLong(FEED_CONFIG_ID_FIELD.getName()) != feedConfigId) {
						if (feedConfig != null) {
							feedConfig.setMapping(mapping);
							feedConfig.setFieldsMapping(fieldsMapping);
							feedConfigs.add(feedConfig);
							mapping = new HashMap<>();
							fieldsMapping = new HashMap<>();
						}
						long resPoId = results.getLong(FEED_CONFIG_PROCESSING_ORG_ID_FIELD.getName());
						LegalEntity processingOrg = null;
						if (resPoId > 0) {
							processingOrg = LegalEntitySQL.getLegalEntityById(resPoId);
						}
						feedConfigId = results.getLong(FEED_CONFIG_ID_FIELD.getName());
						feedConfig = new FeedConfig(results.getString(FEED_CONFIG_NAME_FIELD.getName()), processingOrg);
						feedConfig.setId(feedConfigId);
						feedConfig.setFeedType(
								FeedType.valueOf(results.getString(FEED_CONFIG_FEED_TYPE_FIELD.getName())));
					}

					String fieldName = results.getString(FEED_MAPPING_VALUE_FEED_QUOTE_NAME_FIELD.getName());
					if (fieldName != null) {
						Map<String, String> fieldsValues = new HashMap<>();
						fieldsValues.put(QuoteValue.ASK,
								results.getString(FEED_MAPPING_VALUE_FEED_ASK_FIELD.getName()));
						fieldsValues.put(QuoteValue.BID,
								results.getString(FEED_MAPPING_VALUE_FEED_BID_FIELD.getName()));
						fieldsValues.put(QuoteValue.CLOSE,
								results.getString(FEED_MAPPING_VALUE_FEED_CLOSE_FIELD.getName()));
						fieldsValues.put(QuoteValue.HIGH,
								results.getString(FEED_MAPPING_VALUE_FEED_HIGH_FIELD.getName()));
						fieldsValues.put(QuoteValue.LAST,
								results.getString(FEED_MAPPING_VALUE_FEED_LAST_FIELD.getName()));
						fieldsValues.put(QuoteValue.LOW,
								results.getString(FEED_MAPPING_VALUE_FEED_LOW_FIELD.getName()));
						fieldsValues.put(QuoteValue.OPEN,
								results.getString(FEED_MAPPING_VALUE_FEED_OPEN_FIELD.getName()));
						fieldsMapping.put(fieldName, fieldsValues);
						Quote quote = QuoteSQL
								.getQuoteById(results.getLong(FEED_MAPPING_VALUE_QUOTE_ID_FIELD.getName()));
						mapping.put(fieldName, quote);
					}
				}
				if (feedConfig != null) {
					feedConfig.setMapping(mapping);
					feedConfig.setFieldsMapping(fieldsMapping);
					feedConfigs.add(feedConfig);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return feedConfigs;
	}

	public static Set<String> getAllFeedConfigNames() {
		Set<String> feedConfigNames = null;
		String query = SELECT + " DISTINCT " + FEED_CONFIG_NAME_FIELD + FROM + FEED_CONFIG_TABLE;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllFeedConfigNames = con.prepareStatement(query);
				ResultSet results = stmtGetAllFeedConfigNames.executeQuery()) {
			while (results.next()) {
				if (feedConfigNames == null) {
					feedConfigNames = new HashSet<>();
				}
				feedConfigNames.add(results.getString(FEED_CONFIG_NAME_FIELD.getName()));
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return feedConfigNames;
	}

	public static long saveFeedConfig(FeedConfig feedConfig) {

		// 1. Check if the feedconfig already exists
		boolean exists = feedConfig.getId() != 0;
		long feedConfigId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveFeedConfig = (!exists)
						? TradistaDBUtil.buildInsertPreparedStatement(con, FEED_CONFIG_TABLE, FEED_CONFIG_NAME_FIELD,
								FEED_CONFIG_FEED_TYPE_FIELD, FEED_CONFIG_PROCESSING_ORG_ID_FIELD)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, FEED_CONFIG_ID_FIELD, FEED_CONFIG_TABLE,
								FEED_CONFIG_NAME_FIELD, FEED_CONFIG_FEED_TYPE_FIELD,
								FEED_CONFIG_PROCESSING_ORG_ID_FIELD);
				PreparedStatement stmtDeleteFeedMappingValues = TradistaDBUtil.buildDeletePreparedStatement(con,
						FEED_MAPPING_VALUE_TABLE, FEED_MAPPING_VALUE_FEED_CONFIG_ID_FIELD);
				PreparedStatement stmtSaveFeedMappingValue = TradistaDBUtil.buildInsertPreparedStatement(con,
						FEED_MAPPING_VALUE_TABLE, FEED_MAPPING_VALUE_FEED_CONFIG_ID_FIELD,
						FEED_MAPPING_VALUE_QUOTE_ID_FIELD, FEED_MAPPING_VALUE_FEED_QUOTE_NAME_FIELD,
						FEED_MAPPING_VALUE_FEED_BID_FIELD, FEED_MAPPING_VALUE_FEED_ASK_FIELD,
						FEED_MAPPING_VALUE_FEED_OPEN_FIELD, FEED_MAPPING_VALUE_FEED_CLOSE_FIELD,
						FEED_MAPPING_VALUE_FEED_HIGH_FIELD, FEED_MAPPING_VALUE_FEED_LOW_FIELD,
						FEED_MAPPING_VALUE_FEED_LAST_FIELD)) {

			stmtSaveFeedConfig.setString(1, feedConfig.getName());
			stmtSaveFeedConfig.setString(2, feedConfig.getFeedType().name());
			if (feedConfig.getProcessingOrg() == null) {
				stmtSaveFeedConfig.setNull(3, Types.BIGINT);
			} else {
				stmtSaveFeedConfig.setLong(3, feedConfig.getProcessingOrg().getId());
			}
			if (exists) {
				stmtSaveFeedConfig.setLong(4, feedConfig.getId());
			}
			stmtSaveFeedConfig.executeUpdate();
			if (!exists) {
				// 3. If the feedConfig doesn't exist save it
				try (ResultSet generatedKeys = stmtSaveFeedConfig.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						feedConfigId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating feed config failed, no generated key obtained.");
					}
				}
			} else {
				// The feedConfig exists, so we update it
				feedConfigId = feedConfig.getId();
			}

			// 2. Drop all feed mapping values related to this feed config

			stmtDeleteFeedMappingValues.setLong(1, feedConfigId);
			stmtDeleteFeedMappingValues.executeUpdate();

			// 3. Recreate the new ones
			for (String providerField : feedConfig.getFieldsMapping().keySet()) {
				Quote quote = feedConfig.getMapping().get(providerField);
				long quoteId;
				Quote dbQuote = QuoteSQL.getQuoteByNameAndType(quote.getName(), quote.getType());
				if (dbQuote != null) {
					quoteId = dbQuote.getId();
				} else {
					// if the quote doesn't exist, create it
					quoteId = QuoteSQL.saveQuote(new Quote(quote.getName(), quote.getType()));
				}
				stmtSaveFeedMappingValue.clearParameters();
				stmtSaveFeedMappingValue.setLong(1, feedConfigId);
				stmtSaveFeedMappingValue.setLong(2, quoteId);
				stmtSaveFeedMappingValue.setString(3, providerField);
				for (Map.Entry<String, String> entry : feedConfig.getFieldsMapping().get(providerField).entrySet()) {

					switch (entry.getKey()) {
					case QuoteValue.ASK: {
						stmtSaveFeedMappingValue.setString(5, entry.getValue());
						break;
					}
					case QuoteValue.BID: {
						stmtSaveFeedMappingValue.setString(4, entry.getValue());
						break;
					}
					case QuoteValue.CLOSE: {
						stmtSaveFeedMappingValue.setString(7, entry.getValue());
						break;
					}
					case QuoteValue.HIGH: {
						stmtSaveFeedMappingValue.setString(8, entry.getValue());
						break;
					}
					case QuoteValue.LAST: {
						stmtSaveFeedMappingValue.setString(10, entry.getValue());
						break;
					}
					case QuoteValue.LOW: {
						stmtSaveFeedMappingValue.setString(9, entry.getValue());
						break;
					}
					case QuoteValue.OPEN: {
						stmtSaveFeedMappingValue.setString(6, entry.getValue());
						break;
					}
					}
				}
				stmtSaveFeedMappingValue.addBatch();

			}
			stmtSaveFeedMappingValue.executeBatch();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		feedConfig.setId(feedConfigId);
		return feedConfigId;
	}

}