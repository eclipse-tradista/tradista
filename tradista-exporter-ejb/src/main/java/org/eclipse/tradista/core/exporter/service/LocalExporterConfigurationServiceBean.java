package org.eclipse.tradista.core.exporter.service;

import java.util.Set;

import org.eclipse.tradista.core.common.util.TradistaConstants;
import org.eclipse.tradista.core.exporter.model.Exporter;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.interceptor.Interceptors;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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
public class LocalExporterConfigurationServiceBean implements LocalExporterConfigurationService {

	private static ApplicationContext applicationContext;

	private static final String CONFIG_FILE_NAME = "tradista-exporter-context.xml";

	private static final String EXPORTER_CONFIGURATION_BEAN = "exporterConfiguration";

	@PostConstruct
	public void init() {
		applicationContext = new ClassPathXmlApplicationContext(
				"/" + TradistaConstants.META_INF + "/" + CONFIG_FILE_NAME);
	}

	@Interceptors(ExporterConfigurationAuthorizationFilteringInterceptor.class)
	@Override
	public Set<Exporter<?, ?>> getAllExporters() {
		return ((ExporterConfiguration) applicationContext.getBean(EXPORTER_CONFIGURATION_BEAN)).getExporters();
	}

	@Interceptors(ExporterConfigurationAuthorizationFilteringInterceptor.class)
	@Override
	public Exporter<?, ?> getExporterByName(String name) {
		return getAllExporters().stream().filter(i -> i.getName().equals(name)).findFirst().get();
	}

}