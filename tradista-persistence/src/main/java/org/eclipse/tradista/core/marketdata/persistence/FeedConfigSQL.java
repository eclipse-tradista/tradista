package org.eclipse.tradista.core.marketdata.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
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

	public static boolean deleteFeedConfig(long id) {
		boolean deleted = false;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteFeedMappingValues = con
						.prepareStatement("DELETE FROM FEED_MAPPING_VALUE WHERE FEED_CONFIG_ID = ?");
				PreparedStatement stmtDeleteFeedConfig = con.prepareStatement("DELETE FROM FEED_CONFIG WHERE ID = ?")) {
			stmtDeleteFeedMappingValues.setLong(1, id);
			stmtDeleteFeedMappingValues.executeUpdate();
			stmtDeleteFeedConfig.setLong(1, id);
			stmtDeleteFeedConfig.executeUpdate();
			deleted = true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return deleted;
	}

	public static Set<FeedConfig> getFeedConfigsByName(String feedConfigName) {
		Set<FeedConfig> feedConfigs = null;
		Map<String, Quote> mapping = new HashMap<String, Quote>();
		Map<String, Map<String, String>> fieldsMapping = new HashMap<String, Map<String, String>>();
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFeedConfigByName = con.prepareStatement(
						"SELECT * FROM FEED_CONFIG LEFT OUTER JOIN FEED_MAPPING_VALUE ON FEED_CONFIG.ID = FEED_MAPPING_VALUE.FEED_CONFIG_ID WHERE NAME = ? ORDER BY FEED_CONFIG.ID")) {
			stmtGetFeedConfigByName.setString(1, feedConfigName);
			try (ResultSet results = stmtGetFeedConfigByName.executeQuery()) {
				long feedConfigId = 0;
				FeedConfig feedConfig = null;
				while (results.next()) {
					if (feedConfigs == null) {
						feedConfigs = new HashSet<FeedConfig>();
					}
					if (results.getLong("id") != feedConfigId) {
						long poId = results.getLong("processing_org_id");
						LegalEntity processingOrg = null;
						if (poId > 0) {
							processingOrg = LegalEntitySQL.getLegalEntityById(poId);
						}
						if (feedConfig != null) {
							feedConfig.setMapping(mapping);
							feedConfig.setFieldsMapping(fieldsMapping);
							feedConfigs.add(feedConfig);
							mapping = new HashMap<String, Quote>();
							fieldsMapping = new HashMap<String, Map<String, String>>();
						}
						feedConfigId = results.getLong("id");
						feedConfig = new FeedConfig(results.getString("name"), processingOrg);
						feedConfig.setId(feedConfigId);
						feedConfig.setFeedType(FeedType.valueOf(results.getString("feed_type")));
					}

					String fieldName = results.getString("feed_quote_name");
					if (fieldName != null) {
						Map<String, String> fieldsValues = new HashMap<String, String>();
						fieldsValues.put(QuoteValue.ASK, results.getString("feed_ask_field"));
						fieldsValues.put(QuoteValue.BID, results.getString("feed_bid_field"));
						fieldsValues.put(QuoteValue.CLOSE, results.getString("feed_close_field"));
						fieldsValues.put(QuoteValue.HIGH, results.getString("feed_high_field"));
						fieldsValues.put(QuoteValue.LAST, results.getString("feed_last_field"));
						fieldsValues.put(QuoteValue.LOW, results.getString("feed_low_field"));
						fieldsValues.put(QuoteValue.OPEN, results.getString("feed_open_field"));
						fieldsMapping.put(fieldName, fieldsValues);
						Quote quote = QuoteSQL.getQuoteById(results.getLong("quote_id"));
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

	public static FeedConfig getFeedConfigByNameAndPo(String feedConfigName, long poId) {
		FeedConfig feedConfig = null;
		Map<String, Quote> mapping = new HashMap<String, Quote>();
		Map<String, Map<String, String>> fieldsMapping = new HashMap<String, Map<String, String>>();
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFeedConfigByName = con.prepareStatement(
						"SELECT * FROM FEED_CONFIG LEFT OUTER JOIN FEED_MAPPING_VALUE ON FEED_CONFIG.ID = FEED_MAPPING_VALUE.FEED_CONFIG_ID WHERE NAME = ? AND PROCESSING_ORG_ID = ?")) {
			stmtGetFeedConfigByName.setString(1, feedConfigName);
			stmtGetFeedConfigByName.setLong(2, poId);
			try (ResultSet results = stmtGetFeedConfigByName.executeQuery()) {
				while (results.next()) {
					if (feedConfig == null) {
						LegalEntity processingOrg = null;
						if (poId > 0) {
							processingOrg = LegalEntitySQL.getLegalEntityById(poId);
						}
						feedConfig = new FeedConfig(results.getString("name"), processingOrg);
						feedConfig.setId(results.getLong("id"));
						feedConfig.setFeedType(FeedType.valueOf(results.getString("feed_type")));
					}
					String fieldName = results.getString("feed_quote_name");
					if (fieldName != null) {
						Map<String, String> fieldsValues = new HashMap<String, String>();
						fieldsValues.put(QuoteValue.ASK, results.getString("feed_ask_field"));
						fieldsValues.put(QuoteValue.BID, results.getString("feed_bid_field"));
						fieldsValues.put(QuoteValue.CLOSE, results.getString("feed_close_field"));
						fieldsValues.put(QuoteValue.HIGH, results.getString("feed_high_field"));
						fieldsValues.put(QuoteValue.LAST, results.getString("feed_last_field"));
						fieldsValues.put(QuoteValue.LOW, results.getString("feed_low_field"));
						fieldsValues.put(QuoteValue.OPEN, results.getString("feed_open_field"));
						fieldsMapping.put(fieldName, fieldsValues);
						Quote quote = QuoteSQL.getQuoteById(results.getLong("quote_id"));
						mapping.put(fieldName, quote);
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		if (feedConfig != null) {
			feedConfig.setFieldsMapping(fieldsMapping);
			feedConfig.setMapping(mapping);
		}
		return feedConfig;
	}

	public static FeedConfig getFeedConfigById(long feedConfigId) {
		FeedConfig feedConfig = null;
		Map<String, Quote> mapping = new HashMap<String, Quote>();
		Map<String, Map<String, String>> fieldsMapping = new HashMap<String, Map<String, String>>();
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFeedConfigById = con.prepareStatement(
						"SELECT * FROM FEED_CONFIG LEFT OUTER JOIN FEED_MAPPING_VALUE ON FEED_CONFIG.ID = FEED_MAPPING_VALUE.FEED_CONFIG_ID WHERE FEED_CONFIG.ID = ?")) {
			stmtGetFeedConfigById.setLong(1, feedConfigId);
			try (ResultSet results = stmtGetFeedConfigById.executeQuery()) {
				while (results.next()) {
					if (feedConfig == null) {
						long poId = results.getLong("processing_org_id");
						LegalEntity processingOrg = null;
						if (poId > 0) {
							processingOrg = LegalEntitySQL.getLegalEntityById(poId);
						}
						feedConfig = new FeedConfig(results.getString("name"), processingOrg);
						feedConfig.setId(results.getLong("id"));
						feedConfig.setFeedType(FeedType.valueOf(results.getString("feed_type")));
					}
					String fieldName = results.getString("feed_quote_name");
					if (fieldName != null) {
						Map<String, String> fieldsValues = new HashMap<String, String>();
						fieldsValues.put(QuoteValue.ASK, results.getString("feed_ask_field"));
						fieldsValues.put(QuoteValue.BID, results.getString("feed_bid_field"));
						fieldsValues.put(QuoteValue.CLOSE, results.getString("feed_close_field"));
						fieldsValues.put(QuoteValue.HIGH, results.getString("feed_high_field"));
						fieldsValues.put(QuoteValue.LAST, results.getString("feed_last_field"));
						fieldsValues.put(QuoteValue.LOW, results.getString("feed_low_field"));
						fieldsValues.put(QuoteValue.OPEN, results.getString("feed_open_field"));
						fieldsMapping.put(fieldName, fieldsValues);
						Quote quote = QuoteSQL.getQuoteById(results.getLong("quote_id"));
						mapping.put(fieldName, quote);
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		if (feedConfig != null) {
			feedConfig.setFieldsMapping(fieldsMapping);
			feedConfig.setMapping(mapping);
		}
		return feedConfig;
	}

	public static Set<FeedConfig> getAllFeedConfigs() {
		Set<FeedConfig> feedConfigs = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetFeedConfigs = con.prepareStatement(
						"SELECT * FROM FEED_CONFIG LEFT OUTER JOIN FEED_MAPPING_VALUE ON FEED_CONFIG.ID = FEED_MAPPING_VALUE.FEED_CONFIG_ID ORDER BY FEED_CONFIG.ID");
				ResultSet results = stmtGetFeedConfigs.executeQuery()) {
			FeedConfig feedConfig = null;
			long feedConfigId = 0;
			Map<String, Quote> mapping = new HashMap<String, Quote>();
			Map<String, Map<String, String>> fieldsMapping = new HashMap<String, Map<String, String>>();

			while (results.next()) {
				if (feedConfigs == null) {
					feedConfigs = new HashSet<FeedConfig>();
				}
				if (results.getLong("id") != feedConfigId) {
					if (feedConfig != null) {
						feedConfig.setMapping(mapping);
						feedConfig.setFieldsMapping(fieldsMapping);
						feedConfigs.add(feedConfig);
						mapping = new HashMap<String, Quote>();
						fieldsMapping = new HashMap<String, Map<String, String>>();
					}
					long poId = results.getLong("processing_org_id");
					LegalEntity processingOrg = null;
					if (poId > 0) {
						processingOrg = LegalEntitySQL.getLegalEntityById(poId);
					}
					feedConfigId = results.getLong("id");
					feedConfig = new FeedConfig(results.getString("name"), processingOrg);
					feedConfig.setId(feedConfigId);
					feedConfig.setFeedType(FeedType.valueOf(results.getString("feed_type")));
				}

				String fieldName = results.getString("feed_quote_name");
				if (fieldName != null) {
					Map<String, String> fieldsValues = new HashMap<String, String>();
					fieldsValues.put(QuoteValue.ASK, results.getString("feed_ask_field"));
					fieldsValues.put(QuoteValue.BID, results.getString("feed_bid_field"));
					fieldsValues.put(QuoteValue.CLOSE, results.getString("feed_close_field"));
					fieldsValues.put(QuoteValue.HIGH, results.getString("feed_high_field"));
					fieldsValues.put(QuoteValue.LAST, results.getString("feed_last_field"));
					fieldsValues.put(QuoteValue.LOW, results.getString("feed_low_field"));
					fieldsValues.put(QuoteValue.OPEN, results.getString("feed_open_field"));
					fieldsMapping.put(fieldName, fieldsValues);
					Quote quote = QuoteSQL.getQuoteById(results.getLong("quote_id"));
					mapping.put(fieldName, quote);
				}
			}
			if (feedConfig != null) {
				feedConfig.setMapping(mapping);
				feedConfig.setFieldsMapping(fieldsMapping);
				feedConfigs.add(feedConfig);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}

		return feedConfigs;
	}

	public static Set<String> getAllFeedConfigNames() {
		Set<String> feedConfigNames = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllFeedConfigNames = con
						.prepareStatement("SELECT DISTINCT NAME FROM FEED_CONFIG");
				ResultSet results = stmtGetAllFeedConfigNames.executeQuery()) {
			while (results.next()) {
				if (feedConfigNames == null) {
					feedConfigNames = new HashSet<String>();
				}
				feedConfigNames.add(results.getString("name"));
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
						? con.prepareStatement(
								"INSERT INTO FEED_CONFIG(NAME, FEED_TYPE, PROCESSING_ORG_ID) VALUES(?, ?, ?)",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE FEED_CONFIG SET NAME = ?, FEED_TYPE = ?, PROCESSING_ORG_ID = ? WHERE ID = ?");
				PreparedStatement stmtDeleteFeedMappingValues = con
						.prepareStatement("DELETE FROM FEED_MAPPING_VALUE WHERE FEED_CONFIG_ID = ?");
				PreparedStatement stmtSaveFeedMappingValue = con
						.prepareStatement("INSERT INTO FEED_MAPPING_VALUE VALUES(?,?,?,?,?,?,?,?,?,?) ")) {

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

			stmtDeleteFeedMappingValues.setLong(1, feedConfig.getId());
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
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		feedConfig.setId(feedConfigId);
		return feedConfigId;
	}

}