package org.eclipse.tradista.ai.agent.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.ai.agent.model.AssetManagerAgent;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
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

	public static long saveAssetManagerAgent(AssetManagerAgent agent) {
		long agentId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveAgent = (agent.getId() != 0)
						? con.prepareStatement(
								"UPDATE AGENT SET NAME = ?, ONLY_INFORMATIVE = ?, STARTED = ? WHERE ID = ? ")
						: con.prepareStatement("INSERT INTO AGENT(NAME, ONLY_INFORMATIVE, STARTED) VALUES (?, ?, ?) ",
								Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtSaveAssetManagerAgent = (agent.getId() != 0) ? con.prepareStatement(
						"UPDATE ASSET_MANAGER_AGENT SET MANDATE_ID = ?, PRICING_PARAMETER_ID = ? WHERE AGENT_ID = ? ")
						: con.prepareStatement(
								"INSERT INTO ASSET_MANAGER_AGENT(MANDATE_ID, PRICING_PARAMETER_ID, AGENT_ID) VALUES (?, ?, ?)")) {
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
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		agent.setId(agentId);
		return agentId;
	}

	public static AssetManagerAgent getAssetManagerAgentByName(String name) {
		AssetManagerAgent agent = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAssetManagerAgentByName = con.prepareStatement(
						"SELECT * FROM AGENT, ASSET_MANAGER_AGENT WHERE ID = AGENT_ID AND NAME = ?")) {

			stmtGetAssetManagerAgentByName.setString(1, name);
			try (ResultSet results = stmtGetAssetManagerAgentByName.executeQuery()) {

				while (results.next()) {
					agent = new AssetManagerAgent(results.getString("name"));
					agent.setId(results.getLong("id"));
					agent.setOnlyInformative(results.getBoolean("only_informative"));
					agent.setStarted(results.getBoolean("started"));
					agent.setMandate(MandateSQL.getMandateById(results.getLong("mandate_id")));
					agent.setPricingParameter(new PricerBusinessDelegate()
							.getPricingParameterById(results.getLong("pricing_parameter_id")));
				}
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return agent;
	}

	public static Set<AssetManagerAgent> getAllStartedAssetManagerAgents() {
		Set<AssetManagerAgent> agents = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllStartedAssetManagerAgents = con.prepareStatement(
						"SELECT * FROM AGENT, ASSET_MANAGER_AGENT WHERE ID = AGENT_ID AND STARTED = TRUE");
				ResultSet results = stmtGetAllStartedAssetManagerAgents.executeQuery()) {
			PricerBusinessDelegate pricerBusinessDelegate = new PricerBusinessDelegate();
			while (results.next()) {
				if (agents == null) {
					agents = new HashSet<AssetManagerAgent>();
				}
				AssetManagerAgent agent = new AssetManagerAgent(results.getString("name"));
				agent.setId(results.getLong("id"));
				agent.setOnlyInformative(results.getBoolean("only_informative"));
				agent.setStarted(results.getBoolean("started"));
				agent.setMandate(MandateSQL.getMandateById(results.getLong("mandate_id")));
				agent.setPricingParameter(
						pricerBusinessDelegate.getPricingParameterById(results.getLong("pricing_parameter_id")));
				agents.add(agent);
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return agents;
	}

}