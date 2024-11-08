package org.eclipse.tradista.core.marketdata.service;

import java.util.Set;
import java.util.TreeSet;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.Provider;

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
@Startup
@Singleton
public class MarketDataConfigurationServiceBean implements MarketDataConfigurationService, LocalConfigurationService {

	private static ApplicationContext applicationContext;

	public static final String CONFIG_FILE_NAME = "tradista-marketdata-context.xml";

	private FeedConfig feedConfig;

	private String feedConfigName;

	@EJB
	private LocalFeedService feedService;

	@PostConstruct
	public void init() {
		applicationContext = new ClassPathXmlApplicationContext("/META-INF/tradista-marketdata-context.xml");
		StringBuilder errMsg = new StringBuilder();
		// check provider
		try {
			getProvider();
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			errMsg.append(String.format("Provider cannot be created: %s.%n", te.getMessage()));
		}
		// check frequency
		int frequency = getFrequency();
		if (frequency <= 0) {
			errMsg.append(String.format("The frequency is incorrect: %s, it must be positive.", frequency));
		}
		if (errMsg.length() > 0) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
	}

	public Provider getProvider() throws TradistaBusinessException {
		if (feedConfig == null) {
			Set<FeedConfig> feedConfigs = feedService.getFeedConfigsByName(getFeedConfigName());
			if (feedConfigs == null) {
				throw new TradistaBusinessException(
						String.format("The Feed Config '%s' does not exist in the system.", getFeedConfigName()));
			} else {
				feedConfig = feedConfigs.toArray(new FeedConfig[] {})[0];
			}
		}
		String beanName = feedConfig.getFeedType() + "Provider";
		if (!applicationContext.containsBean(beanName)) {
			throw new TradistaTechnicalException(
					String.format("The %s bean is not defined in %s.", beanName, CONFIG_FILE_NAME));
		}
		return (Provider) applicationContext.getBean(feedConfig.getFeedType() + "Provider");
	}

	public Provider getProviderByFeedType(String feedType) {
		return (Provider) applicationContext.getBean(feedType + "Provider");
	}

	public String getFeedConfigName() {
		if (feedConfigName == null) {
			feedConfigName = ((MarketDataProperties) applicationContext.getBean("marketDataProperties"))
					.getFeedConfigName();
		}
		return feedConfigName;
	}

	@Override
	public int getFrequency() {
		return ((MarketDataProperties) applicationContext.getBean("marketDataProperties")).getFrequency();
	}

	@Override
	public Set<String> getModules() {
		Set<String> modules = null;
		String mods = ((MarketDataProperties) applicationContext.getBean("marketDataProperties")).getModules();
		if (mods != null && !mods.isEmpty()) {
			String[] modsArray = mods.split(",");
			if (modsArray.length > 0) {
				modules = new TreeSet<String>();
				for (String m : modsArray) {
					modules.add(m.trim());
				}
			}
		}
		return modules;
	}

}