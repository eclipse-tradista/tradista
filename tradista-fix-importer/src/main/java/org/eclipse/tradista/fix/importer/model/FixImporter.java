package org.eclipse.tradista.fix.importer.model;

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

import java.io.InputStream;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.importer.TradistaImporter;
import org.eclipse.tradista.fix.importer.processing.ImportApplication;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldNotFound;
import quickfix.FileStoreFactory;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.MessageCracker;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.UnsupportedMessageType;
import quickfix.fix44.Message;

public abstract class FixImporter<X extends Message> extends TradistaImporter<X> {

	public static final String FIX = "Fix";

	private String configFileName;

	private MessageCracker messageCracker;

	private static SessionSettings settings;

	@Override
	protected void start() {
		messageCracker = new MessageCracker(this);
		InputStream inputStream = this.getClass().getResourceAsStream(configFileName);
		SocketAcceptor acceptor;
		try {
			settings = new SessionSettings(inputStream);
			ImportApplication<Message> application = new ImportApplication<>(this);
			FileStoreFactory storeFactory = new FileStoreFactory(settings);
			LogFactory logFactory = new ScreenLogFactory(settings);
			acceptor = new SocketAcceptor(application, storeFactory, settings, logFactory, new DefaultMessageFactory());
			acceptor.start();
		} catch (ConfigError ce) {
			// TODO Add error logs
			return;
		}
	}

	@Override
	public String getType() {
		return FIX;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	@Override
	protected void validateMessage(X fixMessage) throws TradistaBusinessException {
		try {
			messageCracker.crack(fixMessage, null);
		} catch (UnsupportedMessageType | FieldNotFound | IncorrectTagValue e) {
			throw new TradistaBusinessException(e);
		}
	}

	@Override
	protected void saveObject(TradistaObject tradistaObject) throws TradistaBusinessException {
		// TODO Auto-generated method stub

	}

	public static SessionSettings getSessionSettings() {
		return settings;
	}

}