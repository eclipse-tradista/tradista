package finance.tradista.ai.reasoning.prm.probability.model;

import finance.tradista.ai.reasoning.prm.model.FunctionCall;
import finance.tradista.core.common.model.TradistaModelUtil;
import finance.tradista.core.common.model.TradistaObject;

/*
 * Copyright 2017 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

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