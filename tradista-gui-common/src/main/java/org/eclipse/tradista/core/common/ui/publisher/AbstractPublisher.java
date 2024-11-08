package org.eclipse.tradista.core.common.ui.publisher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.ui.subscriber.TradistaSubscriber;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
/**
 * Class following the Observer Design Pattern with some adaptations to handle
 * subscriptions to specific data.
 * 
 * @author OA
 *
 */
public abstract class AbstractPublisher implements TradistaPublisher {

	protected Set<TradistaSubscriber> subscribers;

	private boolean error;

	protected AbstractPublisher() {
		subscribers = Collections.synchronizedSet(new HashSet<TradistaSubscriber>());
	}

	@Override
	public void publish() {
		if (subscribers != null && !subscribers.isEmpty()) {
			for (TradistaSubscriber sub : subscribers) {
				sub.update(this);
			}
		}
	}

	@Override
	public void addSubscriber(TradistaSubscriber subscriber) {
		subscribers.add(subscriber);
	}

	@Override
	public void removeSubscriber(TradistaSubscriber subscriber) {
		subscribers.remove(subscriber);
	}

	@Override
	public boolean isError() {
		return error;
	}

	protected void setError(boolean error) {
		this.error = error;
	}

}