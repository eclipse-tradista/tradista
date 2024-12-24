package org.eclipse.tradista.core.marketdata.ui.publisher;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.ui.publisher.AbstractPublisher;
import org.eclipse.tradista.core.common.ui.subscriber.TradistaSubscriber;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.service.MarketDataBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.MarketDataConfigurationBusinessDelegate;

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

public class MarketDataPublisher extends AbstractPublisher {

	private int refreshFrequency;

	private boolean isStopped = true;

	private Timer timer;

	private TimerTask task;

	private MarketDataBusinessDelegate marketDataBusinessDelegate;

	private static MarketDataPublisher instance;

	private Map<TradistaSubscriber, QuoteSet> quoteSets;

	private Set<QuoteValue> quoteValues;

	private MarketDataPublisher() {
		super();
		quoteSets = Collections.synchronizedMap(new HashMap<TradistaSubscriber, QuoteSet>());
		quoteValues = new HashSet<QuoteValue>();
		// Retrieve Market Data refresh frequency
		refreshFrequency = new MarketDataConfigurationBusinessDelegate().getFrequency();
		marketDataBusinessDelegate = new MarketDataBusinessDelegate();
		timer = new Timer(true);
		task = new TimerTask() {
			@Override
			public void run() {
				// EJB call with a list of quotesets
				Set<QuoteSet> quoteSets = getQuoteSets();
				try {
					if (!subscribers.isEmpty()) {
						boolean wasError = isError();
						boolean updated = false;
						List<QuoteValue> quoteValues = marketDataBusinessDelegate.getMarketData(quoteSets);
						setError(false);
						if (quoteValues != null && !quoteValues.isEmpty()) {
							updateQuoteValues(quoteValues);
							updated = true;
						}
						if (wasError || updated) {
							publish();
						}
					}
				} catch (TradistaBusinessException | TradistaTechnicalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					setError(true);
					publish();
				}
			}

		};

		start();
	}

	public void start() {
		if (isStopped) {
			timer.schedule(task, Calendar.getInstance().getTime(), refreshFrequency * 1000);
			isStopped = false;
		}
	}

	private void updateQuoteValues(List<QuoteValue> quoteValues) {
		this.quoteValues = new HashSet<QuoteValue>(quoteValues);

	}

	public void stop() {
		if (!isStopped) {
			timer.cancel();
			isStopped = true;
		}
	}

	private Set<QuoteSet> getQuoteSets() {
		return new HashSet<QuoteSet>(quoteSets.values());
	}

	public Set<QuoteValue> getQuoteValues() {
		return quoteValues;
	}

	/**
	 * Returns the singleton but if initial instantiation failed, try again.
	 * 
	 * @return the MarketDataPublisher singleton
	 */
	public static synchronized MarketDataPublisher getInstance() {
		if (instance == null) {
			instance = new MarketDataPublisher();
		}
		return instance;
	}

	public void addSubscriber(TradistaSubscriber subscriber, QuoteSet quoteSet) {
		super.addSubscriber(subscriber);
		quoteSets.put(subscriber, quoteSet);
	}

}