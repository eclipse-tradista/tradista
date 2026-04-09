package org.eclipse.tradista.core.exporter.service;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.messaging.TradistaMessagingConfiguration;
import org.eclipse.tradista.core.common.util.TradistaConstants;
import org.eclipse.tradista.core.exporter.model.Exporter;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

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
public class ExporterConfigurationServiceBean implements ExporterConfigurationService {

	@EJB
	private LocalExporterConfigurationService localExporterConfigurationService;

	private static GenericApplicationContext applicationContext;

	private static final String CONFIG_FILE_NAME = "tradista-exporter-context.xml";

	private static final String EXPORTER_CONFIGURATION_BEAN = "exporterConfiguration";

	@PostConstruct
	public void init() {
		applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.registerBean(TradistaMessagingConfiguration.class);

		// We add here the exporter beans in the same context
		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(applicationContext);
		xmlReader.loadBeanDefinitions(new ClassPathResource("/" + TradistaConstants.META_INF + "/" + CONFIG_FILE_NAME));

		applicationContext.refresh();
	}

	@Override
	public SortedSet<String> getModules() {
		return ((ExporterConfiguration) applicationContext.getBean(EXPORTER_CONFIGURATION_BEAN)).getModules();
	}

	@Override
	public SortedSet<String> getAllExporterNames() {
		Set<Exporter<?, ?>> allExporters = localExporterConfigurationService.getAllExporters();
		if (!CollectionUtils.isEmpty(allExporters)) {
			return allExporters.stream().map(i -> i.getName()).collect(Collectors.toCollection(TreeSet::new));
		}
		return null;
	}

}