package org.eclipse.tradista.commercial.commercial.workflow.mapping;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.workflow.model.mapping.StatusMapper;

import finance.tradista.flow.model.Status;
import finance.tradista.flow.model.Workflow;
import finance.tradista.flow.model.WorkflowObject;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public class CommercialTrade implements WorkflowObject {

	private org.eclipse.tradista.commercial.commercial.model.CommercialTrade trade;

	private Workflow<CommercialTrade> workflow;

	public CommercialTrade(Workflow<CommercialTrade> workflow) {
		this.workflow = workflow;
	}

	public org.eclipse.tradista.commercial.commercial.model.CommercialTrade getTrade() {
		return trade;
	}

	public void setTrade(org.eclipse.tradista.commercial.commercial.model.CommercialTrade trade) {
		this.trade = trade;
	}

	@Override
	public Status<CommercialTrade> getStatus() {
		return StatusMapper.map(trade.getStatus(), workflow);
	}

	@Override
	public void setStatus(Status status) {
		trade.setStatus(StatusMapper.map(status));
	}

	@Override
	public String getWorkflow() {
		return workflow != null ? workflow.getName() : null;
	}

	@Override
	public CommercialTrade clone() {
		CommercialTrade cloned = new CommercialTrade(workflow);
		cloned.setTrade(
				(org.eclipse.tradista.commercial.commercial.model.CommercialTrade) TradistaModelUtil.clone(trade));
		return cloned;
	}

}