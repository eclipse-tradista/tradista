package org.eclipse.tradista.core.exporter.workflow.process;

import org.eclipse.tradista.core.exporter.service.ExporterBusinessDelegate;
import org.eclipse.tradista.core.message.workflow.mapping.OutgoingMessage;

import finance.tradista.flow.model.Process;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

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

@Entity
public class SendOutgoingMessage extends Process<OutgoingMessage> {

	private static final long serialVersionUID = 8845899160138325978L;

	@Transient
	private transient ExporterBusinessDelegate exporterBusinessDelegate;

	public SendOutgoingMessage() {
		exporterBusinessDelegate = new ExporterBusinessDelegate();
		setTask(msg -> exporterBusinessDelegate.sendOutgoingMessage(msg.getOriginalMessage()));
	}

}