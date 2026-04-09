package org.eclipse.tradista.core.exporter.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.message.model.OutgoingMessage;
import org.eclipse.tradista.core.message.validator.MessageValidator;

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

public class ExporterBusinessDelegate {

	private ExporterService exporterService;

	private MessageValidator messageValidator;

	public ExporterBusinessDelegate() {
		exporterService = TradistaServiceLocator.getInstance().getExporterService();
		messageValidator = new MessageValidator();
	}

	public OutgoingMessage generateOutgoingMessage(OutgoingMessage msg) throws TradistaBusinessException {
		messageValidator.validateMessage(msg);
		return SecurityUtil.runEx(() -> exporterService.generateOutgoingMessage(msg));
	}

	public void sendOutgoingMessage(OutgoingMessage msg) throws TradistaBusinessException {
		messageValidator.validateMessage(msg);
		SecurityUtil.runEx(() -> exporterService.sendOutgoingMessage(msg));
	}
}