package org.eclipse.tradista.core.marketdata.ui.publisher;

import java.util.Set;

import org.eclipse.tradista.core.common.ui.publisher.PublisherRequest;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class MarketDataPublisherRequest implements PublisherRequest {

	private static final long serialVersionUID = -5635333331539746621L;

	private String quoteSetName;

	private Set<String> quoteNames;

	public String getQuoteSetName() {
		return quoteSetName;
	}

	public void setQuoteSetName(String quoteSetName) {
		this.quoteSetName = quoteSetName;
	}

	public Set<String> getQuoteNames() {
		return quoteNames;
	}

	public void setQuoteNames(Set<String> quoteNames) {
		this.quoteNames = quoteNames;
	}

}