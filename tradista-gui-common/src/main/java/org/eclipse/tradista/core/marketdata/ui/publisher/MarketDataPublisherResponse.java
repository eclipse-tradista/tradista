package org.eclipse.tradista.core.marketdata.ui.publisher;

import java.util.Set;

import org.eclipse.tradista.core.common.ui.publisher.PublisherResponse;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;

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

public class MarketDataPublisherResponse implements PublisherResponse {

	private static final long serialVersionUID = 6941087610419061135L;

	private Set<QuoteValue> quoteValues;

	public Set<QuoteValue> getQuoteValues() {
		return quoteValues;
	}

	public void setQuoteValues(Set<QuoteValue> quoteValues) {
		this.quoteValues = quoteValues;
	}

}