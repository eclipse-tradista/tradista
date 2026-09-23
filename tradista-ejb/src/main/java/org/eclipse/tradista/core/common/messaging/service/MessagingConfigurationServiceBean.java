package org.eclipse.tradista.core.common.messaging.service;

import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.messaging.model.MessagingEventSubscription;
import org.eclipse.tradista.core.common.messaging.model.MessagingFilter;
import org.eclipse.tradista.core.common.messaging.model.MessagingListener;
import org.eclipse.tradista.core.common.messaging.persistence.MessagingConfigurationSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

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
@Stateless
public class MessagingConfigurationServiceBean implements MessagingConfigurationService {

	// --- LISTENERS ---

	@Override
	public List<MessagingListener> getAllListeners() {
		return MessagingConfigurationSQL.getAllListeners();
	}

	@Override
	public List<String> getAllActiveListenerNames() {
		return MessagingConfigurationSQL.getAllActiveListenerNames();
	}

	@Override
	public MessagingListener getListenerById(long id) {
		return MessagingConfigurationSQL.getListenerById(id);
	}

	@Override
	public MessagingListener getListenerByName(String name) {
		return MessagingConfigurationSQL.getListenerByName(name);
	}

	@Interceptors(MessagingConfigurationAuthorizationFilteringInterceptor.class)
	@Override
	public long saveListener(MessagingListener listener) throws TradistaBusinessException {
		MessagingListener existing = MessagingConfigurationSQL.getListenerByName(listener.getName());
		if (existing != null && existing.getId() != listener.getId()) {
			throw new TradistaBusinessException(
					String.format("A listener with the name '%s' already exists.", listener.getName()));
		}

		return MessagingConfigurationSQL.saveListener(listener);
	}

	@Interceptors(MessagingConfigurationAuthorizationFilteringInterceptor.class)
	@Override
	public void deleteListener(long id) throws TradistaBusinessException {
		MessagingListener existing = MessagingConfigurationSQL.getListenerById(id);
		if (existing == null) {
			throw new TradistaBusinessException(String.format("Listener with id %d was not found.", id));
		}
		MessagingConfigurationSQL.deleteListener(id);
	}

	// --- FILTERS ---

	@Override
	public List<MessagingFilter> getAllFilters() {
		return MessagingConfigurationSQL.getAllFilters();
	}

	@Override
	public MessagingFilter getFilterById(long id) {
		return MessagingConfigurationSQL.getFilterById(id);
	}

	@Override
	public MessagingFilter getFilterByName(String name) {
		return MessagingConfigurationSQL.getFilterByName(name);
	}

	@Interceptors(MessagingConfigurationAuthorizationFilteringInterceptor.class)
	@Override
	public long saveFilter(MessagingFilter filter) throws TradistaBusinessException {
		MessagingFilter existing = MessagingConfigurationSQL.getFilterByName(filter.getName());
		if (existing != null && existing.getId() != filter.getId()) {
			throw new TradistaBusinessException(
					String.format("A filter with the name '%s' already exists.", filter.getName()));
		}

		return MessagingConfigurationSQL.saveFilter(filter);
	}

	@Interceptors(MessagingConfigurationAuthorizationFilteringInterceptor.class)
	@Override
	public void deleteFilter(long id) throws TradistaBusinessException {
		MessagingFilter existing = MessagingConfigurationSQL.getFilterById(id);
		if (existing == null) {
			throw new TradistaBusinessException(String.format("Filter with id %d was not found.", id));
		}
		MessagingConfigurationSQL.deleteFilter(id);
	}

	// --- LISTENER FILTERS ---

	@Override
	public List<MessagingFilter> getFiltersByListenerId(long listenerId) {
		return MessagingConfigurationSQL.getFiltersByListenerId(listenerId);
	}

	@Override
	public List<String> getFilterNamesByListenerQueueName(String queueName) {
		return MessagingConfigurationSQL.getFilterNamesByListenerQueueName(queueName);
	}

	// --- SUBSCRIPTIONS / ROUTING ---

	@Override
	public List<MessagingEventSubscription> getAllSubscriptions() {
		return MessagingConfigurationSQL.getAllSubscriptions();
	}

	@Override
	public List<MessagingEventSubscription> getSubscriptionsByEventType(String eventType) {
		return MessagingConfigurationSQL.getSubscriptionsByEventType(eventType);
	}

	@Override
	public List<String> getQueueNamesByEventType(String eventType) {
		return MessagingConfigurationSQL.getQueueNamesByEventType(eventType);
	}

	@Interceptors(MessagingConfigurationAuthorizationFilteringInterceptor.class)
	@Override
	public void saveSubscriptions(String eventType, Set<Long> listenerIds) throws TradistaBusinessException {
		MessagingConfigurationSQL.saveSubscriptions(eventType, listenerIds);
	}

}
