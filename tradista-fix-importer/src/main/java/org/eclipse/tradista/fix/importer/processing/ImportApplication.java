package org.eclipse.tradista.fix.importer.processing;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaExceptionHandlerInterceptor;
import org.eclipse.tradista.fix.importer.model.FixImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ApplicationAdapter;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.MsgType;

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

public class ImportApplication<X extends quickfix.fix44.Message> extends ApplicationAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ImportApplication.class);

	private FixImporter<X> fixImporter;

	public ImportApplication(FixImporter<X> fixImporter) {
		this.fixImporter = fixImporter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fromApp(Message message, SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		try {
			fixImporter.importMessage((X) message);
		} catch (TradistaBusinessException tbe) {
			logger.error("Unexpected TradistaBusinessException during import", tbe);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void toAdmin(Message message, SessionID sessionId) {
		try {
			if (message.getHeader().getString(MsgType.FIELD).equals(MsgType.REJECT)) {
				try {
					fixImporter.handleError(fixImporter.createMessage((X) message),
							"There was a structure issue in the message. Please consult Importer App logs.");
				} catch (TradistaBusinessException _) {
					// Nothing more to do. In case of exception, it has already been logged.
				}
			}
		} catch (FieldNotFound _) {
			// Not expected here.
		}
	}

}