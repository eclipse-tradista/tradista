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

package org.eclipse.tradista.core.message.service;

import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.AccessChecker;
import org.eclipse.tradista.core.message.model.Message;
import org.springframework.util.CollectionUtils;

/**
 * access checker for messages.
 */
public class MessageAccessChecker implements AccessChecker {

	private MessageBusinessDelegate messageBusinessDelegate;

	public MessageAccessChecker() {
		messageBusinessDelegate = new MessageBusinessDelegate();
	}

	@Override
	public void check(Object value, StringBuilder errMsg) throws TradistaBusinessException {
		if (value instanceof Message message) {
			if (message.getId() != 0) {
				List<Message> msgs = messageBusinessDelegate.getMessages(message.getId(), null, null, null, 0, null,
						null, null, null, null, null);
				if (CollectionUtils.isEmpty(msgs)) {
					errMsg.append(String.format("The message %d was not found.%n", message.getId()));
				}
			}
		} else if (value instanceof Long id) {
			if (id != 0) {
				List<Message> msgs = messageBusinessDelegate.getMessages(id, null, null, null, 0, null, null, null,
						null, null, null);
				if (CollectionUtils.isEmpty(msgs)) {
					errMsg.append(String.format("The message %d was not found.%n", id));
				}
			}
		}
	}

}