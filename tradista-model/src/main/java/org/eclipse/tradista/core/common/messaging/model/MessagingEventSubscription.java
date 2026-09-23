package org.eclipse.tradista.core.common.messaging.model;

import org.eclipse.tradista.core.common.model.Id;
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

public class MessagingEventSubscription extends TradistaObject implements Comparable<MessagingEventSubscription> {

	private static final long serialVersionUID = 1L;

	@Id
	private String eventType;

	@Id
	private MessagingListener listener;

	public MessagingEventSubscription(String eventType, MessagingListener listener) {
		this.eventType = eventType;
		this.listener = listener;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public MessagingListener getListener() {
		return listener;
	}

	public void setListener(MessagingListener listener) {
		this.listener = listener;
	}

	@Override
	public int compareTo(MessagingEventSubscription o) {
		if (o == null) {
			return 1;
		}
		int res = 0;
		if (this.eventType != null && o.eventType != null) {
			res = this.eventType.compareTo(o.eventType);
		}
		if (res != 0) {
			return res;
		}
		if (this.listener != null && o.listener != null) {
			return this.listener.compareTo(o.listener);
		}
		return 0;
	}

	@Override
	public MessagingEventSubscription clone() {
		MessagingEventSubscription subscription = (MessagingEventSubscription) super.clone();
		if (listener != null) {
			subscription.listener = listener.clone();
		}
		return subscription;
	}

}
