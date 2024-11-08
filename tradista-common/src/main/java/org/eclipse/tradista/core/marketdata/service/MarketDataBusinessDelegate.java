package org.eclipse.tradista.core.marketdata.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.common.util.TradistaProperties;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.requestpreparator.RequestPreparator;
import org.eclipse.tradista.core.marketdata.requestpreparator.RequestPreparatorFactory;
import org.eclipse.tradista.core.marketdata.service.MarketDataService;

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

public class MarketDataBusinessDelegate {

	private MarketDataService service;

	public MarketDataBusinessDelegate() {
		service = TradistaServiceLocator.getInstance().getMarketDataService();
	}

	public List<QuoteValue> getMarketData(Set<QuoteSet> quoteSets) throws TradistaBusinessException {
		if (quoteSets == null) {
			throw new TradistaBusinessException("The set of QuoteSets cannot be null");
		}

		if (quoteSets.isEmpty()) {
			throw new TradistaBusinessException("The set of QuoteSets cannot be empty");
		}
		Map<String, String> props = null;
		RequestPreparator requestPreparator = RequestPreparatorFactory
				.createRequestPreparator(TradistaProperties.getMarketDataProvider());
		if (requestPreparator != null) {
			props = requestPreparator.prepareRequest();
		}

		final Map<String, String> fProps = props;

		return SecurityUtil.runEx(() -> service.getMarketData(quoteSets, fProps));
	}

	public void getMarketData(Set<QuoteSet> quoteSets, FeedConfig feedConfig) throws TradistaBusinessException {

		StringBuilder errMsg = new StringBuilder();
		if (feedConfig == null) {
			errMsg.append("The feed config cannot be null.");
		}

		if (quoteSets == null) {
			errMsg.append("The set of QuoteSets cannot be null");
		} else {
			if (quoteSets.isEmpty()) {
				errMsg.append("The set of QuoteSets cannot be empty");
			}
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		Map<String, String> props = null;
		RequestPreparator requestPreparator = RequestPreparatorFactory
				.createRequestPreparator(feedConfig.getFeedType().toString());
		if (requestPreparator != null) {
			props = requestPreparator.prepareRequest();
		}
		final Map<String, String> fProps = props;

		SecurityUtil.runEx(() -> service.getMarketData(quoteSets, fProps, feedConfig.getId()));
	}

}