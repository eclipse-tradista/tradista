package org.eclipse.tradista.core.marketdata.service;

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

public class MarketDataProperties {

	private String feedConfigName;

	private String modules;

	private int frequency;

	public void setFeedConfigName(String feedConfigName) {
		this.feedConfigName = feedConfigName;
	}

	public String getFeedConfigName() {
		return feedConfigName;
	}

	public void setModules(String modules) {
		this.modules = modules;
	}

	public String getModules() {
		return modules;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getFrequency() {
		return frequency;
	}
}