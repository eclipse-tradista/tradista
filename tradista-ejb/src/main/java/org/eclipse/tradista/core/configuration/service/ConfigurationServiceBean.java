package org.eclipse.tradista.core.configuration.service;

import java.math.RoundingMode;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.eclipse.tradista.core.common.service.CustomProperties;
import org.eclipse.tradista.core.common.util.MathProperties;
import org.eclipse.tradista.core.configuration.model.UIConfiguration;
import org.eclipse.tradista.core.configuration.persistence.UIConfigurationSQL;
import org.eclipse.tradista.core.configuration.service.ConfigurationPreFilteringInterceptor;
import org.eclipse.tradista.core.configuration.service.ConfigurationService;
import org.eclipse.tradista.core.user.model.User;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.interceptor.Interceptors;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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
public class ConfigurationServiceBean implements LocalConfigurationService, ConfigurationService {

	private static ApplicationContext applicationContext;

	@PostConstruct
	public void init() {
		applicationContext = new ClassPathXmlApplicationContext("/META-INF/tradista-context.xml");
	}

	@Override
	public Scheduler getScheduler() {
		Scheduler scheduler;
		// Instanciating the scheduler
		scheduler = (Scheduler) applicationContext.getBean("TradistaScheduler");

		return scheduler;
	}

	@SuppressWarnings("static-access")
	@Override
	public short getScale() {
		return ((MathProperties) applicationContext.getBean("mathProperties")).getScale();
	}

	@SuppressWarnings("static-access")
	@Override
	public RoundingMode getRoundingMode() {
		return ((MathProperties) applicationContext.getBean("mathProperties")).getRoundingMode();
	}

	@SuppressWarnings("static-access")
	@Override
	public String getCustomPackage() {
		return ((CustomProperties) applicationContext.getBean("customProperties")).getCustomPackage();
	}

	@Interceptors(ConfigurationPreFilteringInterceptor.class)
	@Override
	public UIConfiguration getUIConfiguration(User user) {
		UIConfiguration uiConfiguration = UIConfigurationSQL.getUIConfiguration(user);
		// if the UI Configuration is not customized, we load a default one.
		if (uiConfiguration == null) {
			uiConfiguration = new UIConfiguration(user);
		}
		return uiConfiguration;
	}

	@Interceptors(ConfigurationPreFilteringInterceptor.class)
	@Override
	public void saveUIConfiguration(UIConfiguration configuration) {
		UIConfigurationSQL.saveUIConfiguration(configuration);
	}

}