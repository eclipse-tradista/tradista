package org.eclipse.tradista.core.common.messaging.service;

import org.eclipse.tradista.core.common.messaging.MessagingConfigurationService;
import org.eclipse.tradista.core.common.messaging.TradistaEventGateway;
import org.eclipse.tradista.core.common.messaging.TradistaMessagingConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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

@PermitAll
@Startup
@Singleton
public class MessagingConfigurationServiceBean implements MessagingConfigurationService {

	private static AnnotationConfigApplicationContext applicationContext;

	@PostConstruct
	public void init() {
		applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(TradistaMessagingConfiguration.class);
		applicationContext.refresh();
		applicationContext.start();
	}

	@Override
	public TradistaEventGateway getTradistaEventGateway() {
		return applicationContext.getBean(TradistaEventGateway.class);
	}

}