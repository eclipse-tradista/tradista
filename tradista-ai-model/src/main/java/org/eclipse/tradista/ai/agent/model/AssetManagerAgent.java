package org.eclipse.tradista.ai.agent.model;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;

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

public class AssetManagerAgent extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 497961030733496375L;

	private Mandate mandate;

	private PricingParameter pricingParameter;

	public AssetManagerAgent(String name) {
		super(name);
	}

	public Mandate getMandate() {
		return TradistaModelUtil.clone(mandate);
	}

	public void setMandate(Mandate mandate) {
		this.mandate = mandate;
	}

	public PricingParameter getPricingParameter() {
		return TradistaModelUtil.clone(pricingParameter);
	}

	public void setPricingParameter(PricingParameter pricingParameter) {
		this.pricingParameter = pricingParameter;
	}

	@Override
	public AssetManagerAgent clone() {
		AssetManagerAgent agent = (AssetManagerAgent) super.clone();
		agent.mandate = TradistaModelUtil.clone(mandate);
		agent.pricingParameter = TradistaModelUtil.clone(pricingParameter);
		return agent;
	}

}