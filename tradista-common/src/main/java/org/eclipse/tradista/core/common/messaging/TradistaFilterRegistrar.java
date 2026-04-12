package org.eclipse.tradista.core.common.messaging;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Component;

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

@Component
public class TradistaFilterRegistrar implements BeanFactoryPostProcessor {

	private static final Logger logger = LoggerFactory.getLogger(TradistaFilterRegistrar.class);

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

		// Hard coded for the moment. Will be replaced by filter declarations
		// persisted in DB (mapping BeanName -> FullyQualifiedClassName)
		Map<String, String> filters = Map.of("isAllocatedTrade",
				"org.eclipse.tradista.fix.exporter.filter.IsAllocatedTrade");

		for (Map.Entry<String, String> entry : filters.entrySet()) {
			String beanName = entry.getKey().trim();
			String className = entry.getValue().trim();

			try {
				// We load the class
				Class<?> filterClass = Class.forName(className);

				// We create the bean definition
				registry.registerBeanDefinition(beanName,
						BeanDefinitionBuilder.genericBeanDefinition(filterClass).getBeanDefinition());

			} catch (ClassNotFoundException _) {
				// Normal behavior in multi-EARs environment, the class is part of another
				// module
				// We silently ignore the exception and continue with the next filter.
				logger.debug("Filter '{}' skipped: class {} is not present in this EAR's classpath.", beanName,
						className);
			}
		}
	}
}