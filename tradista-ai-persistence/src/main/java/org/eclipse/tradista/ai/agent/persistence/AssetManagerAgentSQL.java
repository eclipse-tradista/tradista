package org.eclipse.tradista.ai.agent.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRICING_PARAMETER_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.ai.agent.model.AssetManagerAgent;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;

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

public class AssetManagerAgentSQL {

	private static final Field ID_FIELD = new Field(ID);
	private static final Field NAME_FIELD = new Field(NAME);
	private static final Field ONLY_INFORMATIVE_FIELD = new Field("ONLY_INFORMATIVE");
	private static final Field STARTED_FIELD = new Field("STARTED");
	private static final Field[] AGENT_FIELDS = { ID_FIELD, NAME_FIELD, ONLY_INFORMATIVE_FIELD, STARTED_FIELD };
	private static final Table AGENT_TABLE = new Table("AGENT", AGENT_FIELDS);

	private static final Field AGENT_ID_FIELD = new Field("AGENT_ID");
	private static final Field MANDATE_ID_FIELD = new Field("MANDATE_ID");
	private static final Field PRICING_PARAMETER_ID_FIELD = new Field(PRICING_PARAMETER_ID);
	private static final Field[] ASSET_MANAGER_AGENT_FIELDS = { AGENT_ID_FIELD, MANDATE_ID_FIELD,
			PRICING_PARAMETER_ID_FIELD };
	private static final Table ASSET_MANAGER_AGENT_TABLE = new Table("ASSET_MANAGER_AGENT", ASSET_MANAGER_AGENT_FIELDS);

	public static long saveAssetManagerAgent(AssetManagerAgent agent) {
		long agentId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveAgent = (agent.getId() != 0)
						? TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, AGENT_TABLE, NAME_FIELD,
								ONLY_INFORMATIVE_FIELD, STARTED_FIELD)
						: TradistaDBUtil.buildInsertPreparedStatement(con, AGENT_TABLE, NAME_FIELD,
								ONLY_INFORMATIVE_FIELD, STARTED_FIELD);
				PreparedStatement stmtSaveAssetManagerAgent = (agent.getId() != 0)
						? TradistaDBUtil.buildUpdatePreparedStatement(con, AGENT_ID_FIELD, ASSET_MANAGER_AGENT_TABLE,
								MANDATE_ID_FIELD, PRICING_PARAMETER_ID_FIELD)
						: TradistaDBUtil.buildInsertPreparedStatement(con, ASSET_MANAGER_AGENT_TABLE, MANDATE_ID_FIELD,
								PRICING_PARAMETER_ID_FIELD, AGENT_ID_FIELD)) {
			if (agent.getId() != 0) {
				stmtSaveAgent.setLong(4, agent.getId());
			}
			stmtSaveAgent.setString(1, agent.getName());
			stmtSaveAgent.setBoolean(2, agent.isOnlyInformative());
			stmtSaveAgent.setBoolean(3, agent.isStarted());
			stmtSaveAgent.executeUpdate();

			if (agent.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveAgent.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						agentId = generatedKeys.getLong(1);
						agent.setId(agentId);
					} else {
						throw new SQLException("Creating agent failed, no generated key obtained.");
					}
				}
			} else {
				agentId = agent.getId();
			}
			stmtSaveAssetManagerAgent.setLong(1, agent.getMandate().getId());
			stmtSaveAssetManagerAgent.setLong(2, agent.getPricingParameter().getId());
			stmtSaveAssetManagerAgent.setLong(3, agent.getId());
			stmtSaveAssetManagerAgent.executeUpdate();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		agent.setId(agentId);
		return agentId;
	}

	public static AssetManagerAgent getAssetManagerAgentByName(String name) {
		AssetManagerAgent agent = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(AGENT_TABLE,
				Join.innerEq(ASSET_MANAGER_AGENT_TABLE, ID_FIELD, AGENT_ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAssetManagerAgentByName = con.prepareStatement(sql.toString())) {

			stmtGetAssetManagerAgentByName.setString(1, name);
			try (ResultSet results = stmtGetAssetManagerAgentByName.executeQuery()) {

				while (results.next()) {
					agent = new AssetManagerAgent(results.getString(NAME_FIELD.getName()));
					agent.setId(results.getLong(ID_FIELD.getName()));
					agent.setOnlyInformative(results.getBoolean(ONLY_INFORMATIVE_FIELD.getName()));
					agent.setStarted(results.getBoolean(STARTED_FIELD.getName()));
					agent.setMandate(MandateSQL.getMandateById(results.getLong(MANDATE_ID_FIELD.getName())));
					try {
						agent.setPricingParameter(new PricerBusinessDelegate()
								.getPricingParameterById(results.getLong(PRICING_PARAMETER_ID_FIELD.getName())));
					} catch (TradistaBusinessException _) {
						// Not expected here
					}
				}
			}

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return agent;
	}

	public static Set<AssetManagerAgent> getAllStartedAssetManagerAgents() {
		Set<AssetManagerAgent> agents = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(AGENT_TABLE,
				Join.innerEq(ASSET_MANAGER_AGENT_TABLE, ID_FIELD, AGENT_ID_FIELD)));
		TradistaDBUtil.addFilter(sql, STARTED_FIELD, Boolean.TRUE);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllStartedAssetManagerAgents = con.prepareStatement(sql.toString());
				ResultSet results = stmtGetAllStartedAssetManagerAgents.executeQuery()) {
			PricerBusinessDelegate pricerBusinessDelegate = new PricerBusinessDelegate();
			while (results.next()) {
				if (agents == null) {
					agents = new HashSet<>();
				}
				AssetManagerAgent agent = new AssetManagerAgent(results.getString(NAME_FIELD.getName()));
				agent.setId(results.getLong(ID_FIELD.getName()));
				agent.setOnlyInformative(results.getBoolean(ONLY_INFORMATIVE_FIELD.getName()));
				agent.setStarted(results.getBoolean(STARTED_FIELD.getName()));
				agent.setMandate(MandateSQL.getMandateById(results.getLong(MANDATE_ID_FIELD.getName())));
				try {
					agent.setPricingParameter(
							pricerBusinessDelegate.getPricingParameterById(results.getLong(PRICING_PARAMETER_ID_FIELD.getName())));
				} catch (TradistaBusinessException _) {
					// Not expected here
				}
				agents.add(agent);
			}

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return agents;
	}

}