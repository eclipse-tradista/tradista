package org.eclipse.tradista.ai.agent.service;

import java.util.Set;

import org.eclipse.tradista.ai.agent.model.Agent;
import org.eclipse.tradista.ai.agent.persistence.AgentSQL;
import org.eclipse.tradista.ai.reasoning.prm.persistence.FunctionSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class AgentServiceBean implements AgentService {

	@Override
	public long saveAgent(Agent agent) throws TradistaBusinessException {
		if (agent.getId() == 0) {
			checkNameExistence(agent);
			return AgentSQL.saveAgent(agent);
		} else {
			Agent oldAgent = AgentSQL.getAgentById(agent.getId());
			if (!agent.getName().equals(oldAgent.getName())) {
				checkNameExistence(agent);
			}
			return AgentSQL.saveAgent(agent);
		}
	}

	private void checkNameExistence(Agent agent) throws TradistaBusinessException {
		if (FunctionSQL.getFunctionByName(agent.getName()) != null) {
			throw new TradistaBusinessException(String.format("This agent '%s' already exists.", agent.getName()));
		}
	}

	@Override
	public Set<Agent> getAllStartedAgents() {
		return AgentSQL.getAllStartedAgents();
	}

}