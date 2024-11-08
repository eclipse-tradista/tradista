package org.eclipse.tradista.ai.agent.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.ai.agent.model.Agent;
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

public class AgentSQL {

	public static long saveAgent(Agent agent) {
		long agentId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveAgent = (agent.getId() != 0)
						? con.prepareStatement(
								"UPDATE AGENT SET NAME = ?, ONLY_INFORMATIVE = ?, STARTED = ? WHERE ID = ? ")
						: con.prepareStatement("INSERT INTO AGENT(NAME, ONLY_INFORMATIVE, STARTED) VALUES (?, ?, ?) ",
								Statement.RETURN_GENERATED_KEYS)) {
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

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		agent.setId(agentId);
		return agentId;
	}

	public static Agent getAgentById(long id) {
		Agent agent = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAgentById = con.prepareStatement("SELECT * FROM AGENT WHERE ID = ?")) {
			stmtGetAgentById.setLong(1, id);
			try (ResultSet results = stmtGetAgentById.executeQuery()) {

				while (results.next()) {
					agent = new Agent(results.getString("name"));
					agent.setId(results.getLong("id"));
					agent.setOnlyInformative(results.getBoolean("only_informative"));
					agent.setStarted(results.getBoolean("started"));
				}
			}

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return agent;
	}

	public static Agent getAgentByName(String name) {
		Agent agent = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAgentByName = con.prepareStatement("SELECT * FROM AGENT WHERE NAME = ?")) {
			stmtGetAgentByName.setString(1, name);
			try (ResultSet results = stmtGetAgentByName.executeQuery()) {

				while (results.next()) {
					agent = new Agent(results.getString("name"));
					agent.setId(results.getLong("id"));
					agent.setOnlyInformative(results.getBoolean("only_informative"));
					agent.setStarted(results.getBoolean("started"));
				}
			}

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return agent;
	}

	public static Set<Agent> getAllStartedAgents() {
		Set<Agent> agents = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllStartedAgents = con
						.prepareStatement("SELECT * FROM AGENT WHERE STARTED = TRUE");
				ResultSet results = stmtGetAllStartedAgents.executeQuery()) {
			while (results.next()) {
				if (agents == null) {
					agents = new HashSet<Agent>();
				}
				Agent agent = new Agent(results.getString("name"));
				agent.setId(results.getLong("id"));
				agent.setOnlyInformative(results.getBoolean("only_informative"));
				agent.setStarted(results.getBoolean("started"));
				agents.add(agent);
			}

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return agents;
	}

}