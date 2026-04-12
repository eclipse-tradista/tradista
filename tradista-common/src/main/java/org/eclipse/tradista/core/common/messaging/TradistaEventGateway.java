package org.eclipse.tradista.core.common.messaging;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

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

/**
 * Entry point for Eclipse Tradista messaging. This interface is implemented by
 * Spring at runtime.
 */

@MessagingGateway
public interface TradistaEventGateway {

	/**
	 * Sends the event to the Spring Integration input channel. The message will
	 * contain the event object as the payload.
	 */
	@Gateway(requestChannel = "eventInputChannel")
	void publish(Event event);
}