package org.eclipse.tradista.core.exporter.service;

import java.time.LocalDateTime;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.exporter.model.Exporter;
import org.eclipse.tradista.core.message.model.ExportError;
import org.eclipse.tradista.core.message.model.ExportError.ExportErrorType;
import org.eclipse.tradista.core.message.model.OutgoingMessage;
import org.eclipse.tradista.core.message.service.ExportErrorBusinessDelegate;
import org.eclipse.tradista.core.message.service.MessageAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.message.util.MessageUtil;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class ExporterServiceBean implements ExporterService {

	@EJB
	private LocalExporterConfigurationService localExporterConfigurationService;

	private ExportErrorBusinessDelegate exportErrorBusinessDelegate;

	@Interceptors(MessageAuthorizationFilteringInterceptor.class)
	@SuppressWarnings("unchecked")
	@Override
	public OutgoingMessage generateOutgoingMessage(OutgoingMessage msg) throws TradistaBusinessException {
		// Get the exporter
		Exporter<TradistaObject, ?> exporter = (Exporter<TradistaObject, ?>) localExporterConfigurationService
				.getExporterByName(msg.getInterfaceName());
		// Get the export error linked to the message, if any
		ExportError existingGenerationError = exportErrorBusinessDelegate.getExportError(msg.getId(),
				ExportError.ExportErrorType.GENERATION);
		try {
			// Load the object to export
			TradistaObject object = MessageUtil.loadObject(msg.getObjectId(), msg.getObjectType());
			// Generate the content
			Object content = exporter.createContent(object);
			// Set the content to the message
			OutgoingMessage.Builder builder = msg.toBuilder().content(content.toString());
			// If ok, solve the existing generation error if any
			if (existingGenerationError != null) {
				existingGenerationError.solve();
				exportErrorBusinessDelegate.saveExportError(existingGenerationError);
			}
			return builder.build();
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			// Update the existing generation error if any, otherwise create a new one
			if (existingGenerationError != null) {
				existingGenerationError.update(te.getMessage());
				exportErrorBusinessDelegate.saveExportError(existingGenerationError);
			} else {
				ExportError exportError = new ExportError();
				ExportErrorType errorType = ExportErrorType.GENERATION;
				exportError.setErrorDate(LocalDateTime.now());
				exportError.setErrorMessage(te.getMessage());
				exportError.setStatus(Status.UNSOLVED);
				exportError.setMessage(msg);
				exportError.setExportErrorType(errorType);
				exportErrorBusinessDelegate.saveExportError(exportError);
			}
			throw te;
		}
	}

	@Interceptors(MessageAuthorizationFilteringInterceptor.class)
	@SuppressWarnings("unchecked")
	@Override
	public void sendOutgoingMessage(OutgoingMessage msg) throws TradistaBusinessException {
		// Get the exporter
		Exporter<TradistaObject, Object> exporter = (Exporter<TradistaObject, Object>) localExporterConfigurationService
				.getExporterByName(msg.getInterfaceName());
		// Get the export error linked to the message, if any
		ExportError existingSendingError = exportErrorBusinessDelegate.getExportError(msg.getId(),
				ExportError.ExportErrorType.SENDING);

		try {
			// Send the message content
			exporter.sendMessage(exporter.buildMessage(msg.getContent()));
			// If ok, solve the existing sending error if any
			if (existingSendingError != null) {
				existingSendingError.solve();
				exportErrorBusinessDelegate.saveExportError(existingSendingError);
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			// Update the existing sending error if any
			if (existingSendingError != null) {
				existingSendingError.update(te.getMessage());
				exportErrorBusinessDelegate.saveExportError(existingSendingError);
			} else {
				ExportError exportError = new ExportError();
				ExportErrorType errorType = ExportErrorType.SENDING;
				exportError.setErrorDate(LocalDateTime.now());
				exportError.setErrorMessage(te.getMessage());
				exportError.setStatus(Status.UNSOLVED);
				exportError.setMessage(msg);
				exportError.setExportErrorType(errorType);
				exportErrorBusinessDelegate.saveExportError(exportError);
			}
			throw te;
		}
	}

	@PostConstruct
	private void init() {
		exportErrorBusinessDelegate = new ExportErrorBusinessDelegate();
	}

}