package org.eclipse.tradista.core.importer.workflow.process;

import org.eclipse.tradista.core.importer.service.ImporterBusinessDelegate;
import org.eclipse.tradista.core.message.workflow.mapping.IncomingMessage;

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
public class ValidateMessage extends Process<IncomingMessage> {

	private static final long serialVersionUID = 405172602649208130L;

	@Transient
	private transient ImporterBusinessDelegate importerBusinessDelegate;

	public ValidateMessage() {
		importerBusinessDelegate = new ImporterBusinessDelegate();
		setTask(msg -> importerBusinessDelegate.validateMessage(msg.getOriginalMessage()));
	}

}