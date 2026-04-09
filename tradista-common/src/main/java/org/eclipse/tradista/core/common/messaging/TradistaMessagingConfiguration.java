package org.eclipse.tradista.core.common.messaging;

import static org.eclipse.tradista.core.common.util.TradistaConstants.DATASOURCE_JNDI_URL;

import java.time.Duration;
import java.util.Collections;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableMessageHistory;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.DerbyChannelMessageStoreQueryProvider;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.transaction.TransactionInterceptorBuilder;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.jta.JtaTransactionManager;

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

@Configuration
@EnableIntegration
@EnableMessageHistory("*Queue")
@ComponentScan(basePackages = "org.eclipse.tradista.core.common.messaging")
@IntegrationComponentScan(basePackages = "org.eclipse.tradista.core.common.messaging")
public class TradistaMessagingConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(TradistaMessagingConfiguration.class);

	/**
	 * The input channel where the Gateway sends events.
	 */
	@Bean
	public MessageChannel eventInputChannel() {
		return new DirectChannel();
	}

	/**
	 * The DLQ global to Tradista.
	 */
	@Bean
	public MessageChannel tradistaDLQ(JdbcChannelMessageStore messageStore) {
		QueueChannel tradistaDLQ = MessageChannels.queue(messageStore, TradistaDLQService.TRADISTA_DLQ).getObject();

		// This interceptor logs the exception before persistence of the error message
		tradistaDLQ.addInterceptor(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				if (message.getPayload() instanceof MessagingException) {
					MessagingException ex = (MessagingException) message.getPayload();
					logger.error("Exception sent to Tradista DLQ: {}",
							ex.getCause() != null ? ex.getCause() : "Unknown exception");
				}
				return message; // the message is returned, so it can continue to DB
			}
		});
		return tradistaDLQ;
	}

	@Bean
	public JdbcChannelMessageStore messageStore(DataSource dataSource) {
		JdbcChannelMessageStore store = new JdbcChannelMessageStore(dataSource);
		store.setChannelMessageStoreQueryProvider(new DerbyChannelMessageStoreQueryProvider());
		return store;
	}

	/**
	 * The heart of the messaging system. It receives the event from the input
	 * channel and sends it to the Router.
	 */
	@Bean
	public IntegrationFlow eventFlow(TradistaEventRouter eventRouter) {
		return IntegrationFlow.from(eventInputChannel()).route(eventRouter).get();
	}

	@Bean
	public TransactionInterceptor txAdvice(PlatformTransactionManager transactionManager) {
		RuleBasedTransactionAttribute rule = new RuleBasedTransactionAttribute();
		rule.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
		return new TransactionInterceptorBuilder().transactionManager(transactionManager).isolation(Isolation.DEFAULT)
				.transactionAttribute(rule).propagation(Propagation.REQUIRED).build();
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		// Spring will automatically get the Transaction Manager defined in the
		// Application Server.
		return new JtaTransactionManager();
	}

	@Bean
	public DataSource dataSource() {
		JndiDataSourceLookup lookup = new JndiDataSourceLookup();
		return lookup.getDataSource(DATASOURCE_JNDI_URL);
	}

	@Bean
	public ExpressionEvaluatingRequestHandlerAdvice dlqAdvice() {
		ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();
		// Global Tradista DLQ channel
		advice.setFailureChannelName(TradistaDLQService.TRADISTA_DLQ);
		advice.setTrapException(true);
		return advice;
	}

	@Bean
	public PollerMetadata defaultPoller(PlatformTransactionManager transactionManager,
			@Value("${tradista.poller.default.delay:5000}") long pollerDelayMs,
			@Value("${tradista.poller.default.max:10}") int maxMessagesPerPoll) {

		return Pollers.fixedDelay(Duration.ofMillis(pollerDelayMs)) // delay between polls
				.maxMessagesPerPoll(maxMessagesPerPoll).getObject(); // max messages per poll
	}

}