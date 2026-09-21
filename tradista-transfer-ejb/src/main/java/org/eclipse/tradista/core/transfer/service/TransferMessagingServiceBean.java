package org.eclipse.tradista.core.transfer.service;

import static org.eclipse.tradista.core.common.util.TradistaConstants.META_INF;

import org.eclipse.tradista.core.common.messaging.Event;
import org.eclipse.tradista.core.common.messaging.TradistaEventGateway;
import org.eclipse.tradista.core.common.messaging.TradistaMessagingConfiguration;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
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
public class TransferMessagingServiceBean implements LocalTransferMessagingService {

	private static GenericApplicationContext applicationContext;

	public static final String CONFIG_FILE_NAME = "tradista-transfer-context.xml";

	@PostConstruct
	public void init() {
		applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.registerBean(TradistaMessagingConfiguration.class);

		// We add here the exporter beans in the same context
		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(applicationContext);
		xmlReader.loadBeanDefinitions(new ClassPathResource("/" + META_INF + "/" + CONFIG_FILE_NAME));

		applicationContext.refresh();
	}

	@Override
	public void publishEvent(Event event) {
		applicationContext.getBean(TradistaEventGateway.class).publish(event);
	}

}