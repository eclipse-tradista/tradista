package org.eclipse.tradista.fix.importer.processing;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.fix.importer.model.FixImporter;

import quickfix.ApplicationAdapter;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;

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
		} catch (TradistaBusinessException _) {
			// TODO Add logs
		}
	}

	@Override
	public void toAdmin(Message message, SessionID sessionId) {
		System.out.println("ToAdmin");
		System.out.println(message);
	}

	@Override
	public void toApp(Message message, SessionID sessionId) {
		System.out.println("ToApp");
	}

}
