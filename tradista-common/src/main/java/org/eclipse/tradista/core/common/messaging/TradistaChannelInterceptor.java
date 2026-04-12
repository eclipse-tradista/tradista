package org.eclipse.tradista.core.common.messaging;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.integration.support.context.NamedComponent;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
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

@Component("tradistaChannelInterceptor")
@GlobalChannelInterceptor(patterns = "*Queue") // Applied to all event consumers
public class TradistaChannelInterceptor implements ChannelInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(TradistaChannelInterceptor.class);

	@Autowired
	private ApplicationContext context;

	@SuppressWarnings("unchecked")
	@Override
	public Message<?> postReceive(Message<?> message, MessageChannel channel) {

		// Get the channel name
		String listenerName = (channel instanceof NamedComponent namedComponent) ? namedComponent.getComponentName()
				: channel.toString();

		// Hardcoded for now but it should be taken from a configuration in db
		List<String> activeFilters = List.of("isAllocatedTrade");

		if (activeFilters == null || activeFilters.isEmpty()) {
			return message; // No filter configured
		}

		Event event = (Event) message.getPayload();

		// Execute each filter
		for (String filterName : activeFilters) {
			EventFilter<Event> filter = context.getBean(filterName, EventFilter.class);
			if (!filter.test(event)) {
				logger.info("Message rejected by filter {} for listener {}", filterName, listenerName);
				// Message removed by the poller
				return null;
			}
		}

		return message; // The message is validated by all filters
	}

}