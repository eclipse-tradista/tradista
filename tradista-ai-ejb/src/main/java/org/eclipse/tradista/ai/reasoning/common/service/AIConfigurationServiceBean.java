package org.eclipse.tradista.ai.reasoning.common.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.tradista.ai.reasoning.common.util.TradistaAIProperties;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

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
public class AIConfigurationServiceBean implements LocalConfigurationService {

	@PostConstruct
	public void init() {
		Properties properties = new Properties();
		InputStream in = TradistaAIProperties.class.getResourceAsStream("/META-INF/solver.properties");
		try {
			properties.load(in);
			in.close();
		} catch (IOException ioe) {
			// should not happen here.
		}
		try {
			TradistaAIProperties.load(properties);
		} catch (TradistaBusinessException abe) {
			// should not happen here.
		}
	}

	@Override
	public String getSolverPath() {
		return TradistaAIProperties.getSolverPath();
	}
}
