package org.eclipse.tradista.core.common.messaging;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.integration.history.MessageHistory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.ErrorMessage;
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
public class TradistaDLQService {

	private final ApplicationContext context;
	private static final Logger logger = LoggerFactory.getLogger(TradistaDLQService.class);

	protected static final String TRADISTA_DLQ = "tradistaDLQ";

	public TradistaDLQService(ApplicationContext context) {
		this.context = context;
	}

	@org.springframework.context.event.EventListener(ContextRefreshedEvent.class)
	public void onApplicationStart() {
		logger.info("Checking Tradista DLQ...");
		try {
			this.requeueAllFromDlq();
		} catch (Exception e) {
			logger.error("Error when trying to requeue events from DLQ : {}", e.getMessage());
		}
	}

	public void requeueAllFromDlq() {
		// We retrieve all queues from Spring context
		Map<String, PollableChannel> pollableChannels = context.getBeansOfType(PollableChannel.class);
		PollableChannel dlq = pollableChannels.get(TRADISTA_DLQ);

		if (dlq == null) {
			logger.warn("{} was not found", TRADISTA_DLQ);
			return;
		}

		int count = 0;
		Message<?> message;

		while ((message = dlq.receive(0)) != null) {
			if (message instanceof ErrorMessage errorMessage
					&& errorMessage.getPayload() instanceof MessagingException ex) {
				Message<?> originalMessage = ex.getFailedMessage();
				if (originalMessage != null) {
					MessageHistory history = MessageHistory.read(originalMessage);

					if (history != null && !history.isEmpty()) {
						String originName = history.get(0).getProperty(MessageHistory.NAME_PROPERTY);

						if (originName != null && pollableChannels.containsKey(originName)) {
							try {
								MessageChannel target = pollableChannels.get(originName);
								target.send(MessageBuilder.fromMessage(originalMessage).build());
								count++;
							} catch (Exception e) {
								logger.warn("Error when resending event to queue {} : {} ", originName, e.getMessage());
							}
						} else {
							logger.warn("The event in DLQ is coming from an unknown queue : {}", originName);
						}
					}
				}
			}
		}

		if (count > 0) {
			logger.info("Requeueing from DLQ is terminated :  {} events were reinjected", count);
		}
	}

}