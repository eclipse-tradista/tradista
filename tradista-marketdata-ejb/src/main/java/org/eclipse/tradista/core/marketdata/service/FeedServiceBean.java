package org.eclipse.tradista.core.marketdata.service;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.persistence.FeedConfigSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class FeedServiceBean implements LocalFeedService, FeedService {

	@EJB
	private QuoteService quoteService;

	@Interceptors(FeedConfigFilteringInterceptor.class)
	@Override
	public Set<FeedConfig> getFeedConfigsByName(String name) {
		return FeedConfigSQL.getFeedConfigsByName(name);
	}

	@Interceptors(FeedConfigFilteringInterceptor.class)
	@Override
	public FeedConfig getFeedConfigById(long id) {
		return FeedConfigSQL.getFeedConfigById(id);
	}

	@Override
	public Set<String> getAllFeedConfigNames() {
		return FeedConfigSQL.getAllFeedConfigNames();
	}

	@Interceptors(FeedConfigFilteringInterceptor.class)
	@Override
	public Set<FeedConfig> getAllFeedConfigs() {
		return FeedConfigSQL.getAllFeedConfigs();
	}

	@Interceptors(FeedConfigFilteringInterceptor.class)
	@Override
	public long saveFeedConfig(FeedConfig feedConfig) throws TradistaBusinessException {
		if (feedConfig.getId() == 0) {
			checkFeedConfigExistence(feedConfig);
		} else {
			FeedConfig oldFeedConfig = FeedConfigSQL.getFeedConfigById(feedConfig.getId());
			if (!oldFeedConfig.getName().equals(feedConfig.getName())) {
				checkFeedConfigExistence(feedConfig);
			}
		}
		return FeedConfigSQL.saveFeedConfig(feedConfig);
	}

	private void checkFeedConfigExistence(FeedConfig feedConfig) throws TradistaBusinessException {
		if (FeedConfigSQL.getFeedConfigByNameAndPo(feedConfig.getName(),
				feedConfig.getProcessingOrg() == null ? 0 : feedConfig.getProcessingOrg().getId()) != null) {
			String errMsg;
			if (feedConfig.getProcessingOrg() == null) {
				errMsg = "A global feed config named %s already exists in the system.";
			} else {
				errMsg = "A feed config named %s already exists in the system for the PO %s.";
			}
			throw new TradistaBusinessException(
					String.format(errMsg, feedConfig.getName(), feedConfig.getProcessingOrg()));
		}
	}

	@Interceptors(FeedConfigFilteringInterceptor.class)
	@Override
	public boolean deleteFeedConfig(long id) throws TradistaBusinessException {
		return FeedConfigSQL.deleteFeedConfig(id);
	}

	@Override
	public Set<String> getFeedConfigsUsingQuote(String quoteName, QuoteType quoteType) {
		Set<FeedConfig> feedConfigs = FeedConfigSQL.getAllFeedConfigs();
		Set<String> results = null;
		if (feedConfigs != null) {
			for (FeedConfig conf : feedConfigs) {
				Quote quote = quoteService.getQuoteByNameAndType(quoteName, quoteType);
				if (conf.getMapping().values().contains(quote)) {
					if (results == null) {
						results = new HashSet<String>(feedConfigs.size());
					}
					results.add(conf.getName());
				}
			}
		}
		return results;
	}

}