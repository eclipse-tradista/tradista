package org.eclipse.tradista.core.importer.service;

import java.util.Set;

import org.eclipse.tradista.core.importer.model.Importer;
import org.eclipse.tradista.core.marketdata.service.ImporterConfigurationService;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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
public class ImporterConfigurationServiceBean implements ImporterConfigurationService {

	private static ApplicationContext applicationContext;

	private static final String CONFIG_FILE_NAME = "tradista-importer-context.xml";

	private static final String IMPORTER_CONFIGURATION_BEAN = "importerConfiguration";

	@PostConstruct
	public void init() {
		applicationContext = new ClassPathXmlApplicationContext("/META-INF/" + CONFIG_FILE_NAME);
	}

	@Override
	public Set<String> getModules() {
		return ((ImporterConfiguration) applicationContext.getBean(IMPORTER_CONFIGURATION_BEAN)).getModules();
	}

	@Override
	public Set<Importer<?>> getImporters() {
		return ((ImporterConfiguration) applicationContext.getBean(IMPORTER_CONFIGURATION_BEAN)).getImporters();
	}

}