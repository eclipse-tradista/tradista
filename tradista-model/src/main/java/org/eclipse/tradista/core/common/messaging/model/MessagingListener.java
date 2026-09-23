package org.eclipse.tradista.core.common.messaging.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;

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

public class MessagingListener extends TradistaObject implements Comparable<MessagingListener> {

	private static final long serialVersionUID = 1L;

	@Id
	private String name;

	private String queueName;

	private long pollerDelay;

	private int pollerMaxMessages;

	private boolean enabled;

	private List<MessagingFilter> filters = new ArrayList<>();

	public MessagingListener(String name) {
		this.name = name;
		this.queueName = name != null ? name + "Queue" : null;
		this.pollerDelay = 5000;
		this.pollerMaxMessages = 20;
		this.enabled = true;
	}

	public MessagingListener(String name, String queueName, long pollerDelay, int pollerMaxMessages, boolean enabled) {
		this.name = name;
		this.queueName = (queueName != null && !queueName.isBlank()) ? queueName : (name != null ? name + "Queue" : null);
		this.pollerDelay = pollerDelay;
		this.pollerMaxMessages = pollerMaxMessages;
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQueueName() {
		return (queueName != null && !queueName.isBlank()) ? queueName : (name != null ? name + "Queue" : null);
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public long getPollerDelay() {
		return pollerDelay;
	}

	public void setPollerDelay(long pollerDelay) {
		this.pollerDelay = pollerDelay;
	}

	public int getPollerMaxMessages() {
		return pollerMaxMessages;
	}

	public void setPollerMaxMessages(int pollerMaxMessages) {
		this.pollerMaxMessages = pollerMaxMessages;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@SuppressWarnings("unchecked")
	public List<MessagingFilter> getFilters() {
		return (List<MessagingFilter>) TradistaModelUtil.deepCopy(filters);
	}

	public void setFilters(List<MessagingFilter> filters) {
		this.filters = filters != null ? filters : new ArrayList<>();
	}

	@Override
	public int compareTo(MessagingListener o) {
		if (o == null) {
			return 1;
		}
		if (this.name == null) {
			return o.name == null ? 0 : -1;
		}
		return this.name.compareTo(o.name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MessagingListener clone() {
		MessagingListener messagingListener = (MessagingListener) super.clone();
		messagingListener.filters = (List<MessagingFilter>) TradistaModelUtil.deepCopy(filters);
		return messagingListener;
	}

}
