package org.eclipse.tradista.core.common.messaging.service;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.messaging.model.MessagingEventSubscription;
import org.eclipse.tradista.core.common.messaging.model.MessagingFilter;
import org.eclipse.tradista.core.common.messaging.model.MessagingListener;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;

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

public class MessagingConfigurationBusinessDelegate {

	private MessagingConfigurationService messagingConfigurationService;

	public MessagingConfigurationBusinessDelegate() {
		messagingConfigurationService = TradistaServiceLocator.getInstance().getMessagingConfigurationService();
	}

	// --- LISTENERS ---

	public List<MessagingListener> getAllListeners() {
		return SecurityUtil.run(() -> messagingConfigurationService.getAllListeners());
	}

	public List<String> getAllActiveListenerNames() {
		return SecurityUtil.run(() -> messagingConfigurationService.getAllActiveListenerNames());
	}

	public MessagingListener getListenerById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException(String.format("The id (%d) must be positive.", id));
		}
		return SecurityUtil.run(() -> messagingConfigurationService.getListenerById(id));
	}

	public MessagingListener getListenerByName(String name) throws TradistaBusinessException {
		if (StringUtils.isBlank(name)) {
			throw new TradistaBusinessException("The listener name is mandatory.");
		}
		return SecurityUtil.run(() -> messagingConfigurationService.getListenerByName(name));
	}

	public long saveListener(MessagingListener listener) throws TradistaBusinessException {
		if (listener == null) {
			throw new TradistaBusinessException("The listener cannot be null.");
		}
		if (StringUtils.isBlank(listener.getName())) {
			throw new TradistaBusinessException("The listener name is mandatory.");
		}
		listener.setQueueName(listener.getName().trim() + "Queue");
		if (listener.getPollerDelay() <= 0) {
			throw new TradistaBusinessException("The poller delay must be strictly positive.");
		}
		if (listener.getPollerMaxMessages() <= 0) {
			throw new TradistaBusinessException("The poller max messages must be strictly positive.");
		}
		return SecurityUtil.runEx(() -> messagingConfigurationService.saveListener(listener));
	}

	public void deleteListener(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException(String.format("The id (%d) must be positive.", id));
		}
		SecurityUtil.runEx(() -> messagingConfigurationService.deleteListener(id));
	}

	// --- FILTERS ---

	public List<MessagingFilter> getAllFilters() {
		return SecurityUtil.run(() -> messagingConfigurationService.getAllFilters());
	}

	public MessagingFilter getFilterById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException(String.format("The id (%d) must be positive.", id));
		}
		return SecurityUtil.run(() -> messagingConfigurationService.getFilterById(id));
	}

	public MessagingFilter getFilterByName(String name) throws TradistaBusinessException {
		if (StringUtils.isBlank(name)) {
			throw new TradistaBusinessException("The filter name is mandatory.");
		}
		return SecurityUtil.run(() -> messagingConfigurationService.getFilterByName(name));
	}

	public long saveFilter(MessagingFilter filter) throws TradistaBusinessException {
		if (filter == null) {
			throw new TradistaBusinessException("The filter cannot be null.");
		}
		if (StringUtils.isBlank(filter.getName())) {
			throw new TradistaBusinessException("The filter name is mandatory.");
		}
		if (StringUtils.isBlank(filter.getClassName())) {
			throw new TradistaBusinessException("The filter class name is mandatory.");
		}
		return SecurityUtil.runEx(() -> messagingConfigurationService.saveFilter(filter));
	}

	public void deleteFilter(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException(String.format("The id (%d) must be positive.", id));
		}
		SecurityUtil.runEx(() -> messagingConfigurationService.deleteFilter(id));
	}

	public List<MessagingFilter> getFiltersByListenerId(long listenerId) throws TradistaBusinessException {
		if (listenerId <= 0) {
			throw new TradistaBusinessException(String.format("The listener id (%d) must be positive.", listenerId));
		}
		return SecurityUtil.run(() -> messagingConfigurationService.getFiltersByListenerId(listenerId));
	}

	public List<String> getFilterNamesByListenerQueueName(String queueName) {
		return SecurityUtil.run(() -> messagingConfigurationService.getFilterNamesByListenerQueueName(queueName));
	}

	// --- SUBSCRIPTIONS / ROUTING ---

	public List<MessagingEventSubscription> getAllSubscriptions() {
		return SecurityUtil.run(() -> messagingConfigurationService.getAllSubscriptions());
	}

	public List<MessagingEventSubscription> getSubscriptionsByEventType(String eventType)
			throws TradistaBusinessException {
		if (StringUtils.isBlank(eventType)) {
			throw new TradistaBusinessException("The event type is mandatory.");
		}
		return SecurityUtil.run(() -> messagingConfigurationService.getSubscriptionsByEventType(eventType));
	}

	public List<String> getQueueNamesByEventType(String eventType) {
		return SecurityUtil.run(() -> messagingConfigurationService.getQueueNamesByEventType(eventType));
	}

	public void saveSubscriptions(String eventType, Set<Long> listenerIds) throws TradistaBusinessException {
		if (StringUtils.isBlank(eventType)) {
			throw new TradistaBusinessException("The event type is mandatory.");
		}
		SecurityUtil.runEx(() -> messagingConfigurationService.saveSubscriptions(eventType, listenerIds));
	}

}
