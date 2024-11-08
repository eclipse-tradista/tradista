package org.eclipse.tradista.ai.reasoning.prm.probability.model;

import org.eclipse.tradista.ai.reasoning.prm.model.FunctionCall;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;

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

public class ProbabilityLaw extends TradistaObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6117351919886016790L;

	private FunctionCall function;

	private ProbabilityDistribution probabilityDistribution;

	public FunctionCall getFunction() {
		return TradistaModelUtil.clone(function);
	}

	public ProbabilityDistribution getProbabilityDistribution() {
		return TradistaModelUtil.clone(probabilityDistribution);
	}

	public void setFunction(FunctionCall function) {
		this.function = function;
	}

	@Override
	public ProbabilityLaw clone() {
		ProbabilityLaw probabilityLaw = (ProbabilityLaw) super.clone();
		probabilityLaw.function = TradistaModelUtil.clone(function);
		probabilityLaw.probabilityDistribution = TradistaModelUtil.clone(probabilityDistribution);
		return probabilityLaw;
	}

}