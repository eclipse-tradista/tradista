package org.eclipse.tradista.core.common.messaging.service;

import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.messaging.model.MessagingEventSubscription;
import org.eclipse.tradista.core.common.messaging.model.MessagingFilter;
import org.eclipse.tradista.core.common.messaging.model.MessagingListener;

import jakarta.ejb.Remote;

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

@Remote
public interface MessagingConfigurationService {

	// Listeners
	List<MessagingListener> getAllListeners();

	List<String> getAllActiveListenerNames();

	MessagingListener getListenerById(long id);

	MessagingListener getListenerByName(String name);

	long saveListener(MessagingListener listener) throws TradistaBusinessException;

	void deleteListener(long id) throws TradistaBusinessException;

	// Filters
	List<MessagingFilter> getAllFilters();

	MessagingFilter getFilterById(long id);

	MessagingFilter getFilterByName(String name);

	long saveFilter(MessagingFilter filter) throws TradistaBusinessException;

	void deleteFilter(long id) throws TradistaBusinessException;

	List<MessagingFilter> getFiltersByListenerId(long listenerId);

	List<String> getFilterNamesByListenerQueueName(String queueName);

	// Subscriptions & Routing
	List<MessagingEventSubscription> getAllSubscriptions();

	List<MessagingEventSubscription> getSubscriptionsByEventType(String eventType);

	List<String> getQueueNamesByEventType(String eventType);

	void saveSubscriptions(String eventType, Set<Long> listenerIds) throws TradistaBusinessException;

}
