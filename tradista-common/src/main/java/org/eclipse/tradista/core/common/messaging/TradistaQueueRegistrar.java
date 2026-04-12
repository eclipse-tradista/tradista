package org.eclipse.tradista.core.common.messaging;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.integration.dsl.MessageChannels;
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
public class TradistaQueueRegistrar implements BeanFactoryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

		// Hard coded for the moment. Will be replaced by listener declarations
		// persisted in DB.
		List<String> listeners = List.of("tradeCaptureReportExporter");

		for (String listener : listeners) {
			String beanName = listener.trim() + "Queue";

			registry.registerBeanDefinition(beanName,
					BeanDefinitionBuilder.genericBeanDefinition(MessageChannels.class).setFactoryMethod("queue")
							.addConstructorArgReference("messageStore").addConstructorArgValue(beanName)
							.getBeanDefinition());
		}
	}
}