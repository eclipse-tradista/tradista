package org.eclipse.tradista.core.message.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.message.model.Message;

import jakarta.ejb.Remote;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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
public interface MessageService {

	long saveMessage(Message message);

	List<Message> getMessages(long id, Boolean isIncoming, Set<String> types, Set<String> interfaceNames, long objectId,
			Set<String> objectTypes, Set<String> statuses, LocalDateTime creationDateTimeFrom,
			LocalDateTime creationDateTimeTo, LocalDateTime lastUpdateDateTimeFrom, LocalDateTime lastUpdateDateTimeTo);

	Set<String> getAllMessageTypes();

	Set<String> getAllObjectTypes();
}