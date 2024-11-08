package org.eclipse.tradista.core.batch.job;

import java.util.Set;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.eclipse.tradista.core.batch.jobproperty.JobProperty;
import org.eclipse.tradista.core.batch.model.TradistaJob;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.service.FeedBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.MarketDataBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;

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

public class MarketDataJob extends TradistaJob {

	@JobProperty(name = "QuoteSets", type = "QuoteSetSet")
	private Set<QuoteSet> quoteSets;

	@JobProperty(name = "FeedConfig", type = "FeedConfig")
	private FeedConfig feedConfig;

	@Override
	public void executeTradistaJob(JobExecutionContext execContext)
			throws JobExecutionException, TradistaBusinessException {

		if (isInterrupted) {
			performInterruption(execContext);
		}

		new MarketDataBusinessDelegate().getMarketData(quoteSets, feedConfig);

		if (isInterrupted) {
			performInterruption(execContext);
		}
	}

	@Override
	public String getName() {
		return "MarketData";
	}

	public void setFeedConfig(FeedConfig feedConfig) {
		this.feedConfig = feedConfig;
	}

	public void setQuoteSets(Set<QuoteSet> quoteSets) {
		this.quoteSets = quoteSets;
	}

	@Override
	public void checkJobProperties() throws TradistaBusinessException {
		if (quoteSets == null || quoteSets.isEmpty()) {
			throw new TradistaBusinessException("The quoteSets are mandatory.");
		}
		if (feedConfig == null) {
			throw new TradistaBusinessException("The feedConfig is mandatory.");
		}
		for (QuoteSet qs : quoteSets) {
			if (new QuoteBusinessDelegate().getQuoteSetByName(qs.getName()) == null) {
				throw new TradistaBusinessException(String.format("The quoteSet '%s' does not exist.", qs));
			}
		}
		if (new FeedBusinessDelegate().getFeedConfigById(feedConfig.getId()) == null) {
			throw new TradistaBusinessException(String.format("The feed config '%s' does not exist.", feedConfig));
		}
	}
}