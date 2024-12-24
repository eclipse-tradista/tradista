package org.eclipse.tradista.ai.reasoning.prm.probability.model;

import org.apache.commons.math3.distribution.NormalDistribution;

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

public class NormalProbabilityDistribution extends ContinuousProbabilityDistribution {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3118439260355422888L;

	public NormalProbabilityDistribution() {
		super();
		realDistributon = new NormalDistribution();
	}

	public NormalProbabilityDistribution(short mean, short standardDeviation) {
		super();
		realDistributon = new NormalDistribution(mean, standardDeviation);
	}

	private short mean;

	private short standardDeviation;

	public short getMean() {
		return mean;
	}

	public void setMean(short mean) {
		this.mean = mean;
	}

	public short getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(short standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

}