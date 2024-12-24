package org.eclipse.tradista.core.marketdata.service;

import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteType;

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

public class FeedBusinessDelegate {

	private FeedService feedService;

	public FeedBusinessDelegate() {
		feedService = TradistaServiceLocator.getInstance().getFeedService();
	}

	public Set<FeedConfig> getFeedConfigsByName(String name) throws TradistaBusinessException {
		if (name == null) {
			throw new TradistaBusinessException("The feed config name cannot be null.");
		} else {
			if (name.isEmpty()) {
				throw new TradistaBusinessException("The feed config name cannot be empty.");
			}
		}
		return SecurityUtil.run(() -> feedService.getFeedConfigsByName(name));
	}

	public Set<String> getAllFeedConfigNames() {
		return SecurityUtil.run(() -> feedService.getAllFeedConfigNames());
	}

	public Set<FeedConfig> getAllFeedConfigs() {
		return SecurityUtil.run(() -> feedService.getAllFeedConfigs());
	}

	public long saveFeedConfig(FeedConfig feedConfig) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (feedConfig == null) {
			throw new TradistaBusinessException("The feed config cannot be null");
		}
		if (feedConfig.getName() == null || feedConfig.getName().isEmpty()) {
			errMsg.append(String.format("The name is mandatory.%n"));
		}
		if (feedConfig.getFeedType() == null) {
			errMsg.append(String.format("The feed type is mandatory.%n"));
		}
		if (feedConfig.getMapping().values() != null && !feedConfig.getMapping().values().isEmpty()) {
			QuoteBusinessDelegate quoteBusinessDelegate = new QuoteBusinessDelegate();
			for (Map.Entry<String, Quote> entry : feedConfig.getMapping().entrySet()) {
				if (entry.getValue() == null) {
					errMsg.append(
							String.format("This field name: %s cannot be linked to a null quote.%n", entry.getKey()));
				} else {
					try {
						quoteBusinessDelegate.validateQuoteName(entry.getValue().getName());
					} catch (TradistaBusinessException abe) {
						errMsg.append(String.format("%s.%n", abe.getMessage()));
					}
					if (entry.getValue().getType() == null) {
						errMsg.append(
								String.format("This quote: %s must have a quote type.%n", entry.getValue().getName()));
					}
				}
			}
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> feedService.saveFeedConfig(feedConfig));
	}

	public boolean deleteFeedConfig(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The id must be positive.");
		}
		return SecurityUtil.runEx(() -> feedService.deleteFeedConfig(id));
	}

	public Set<String> getFeedConfigsUsingQuote(String quoteName, QuoteType quoteType) {
		return SecurityUtil.run(() -> feedService.getFeedConfigsUsingQuote(quoteName, quoteType));
	}

	public FeedConfig getFeedConfigById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The feed config id must be positive.");
		}
		return SecurityUtil.run(() -> feedService.getFeedConfigById(id));
	}

}