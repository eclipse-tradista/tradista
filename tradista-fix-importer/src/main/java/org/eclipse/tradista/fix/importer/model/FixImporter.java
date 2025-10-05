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
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.importer.TradistaImporter;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.fix.importer.processing.ImportApplication;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldNotFound;
import quickfix.FileStoreFactory;
import quickfix.IncorrectTagValue;
import quickfix.InvalidMessage;
import quickfix.LogFactory;
import quickfix.MessageCracker;
import quickfix.MessageUtils;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.UnsupportedMessageType;
import quickfix.fix44.Message;

public abstract class FixImporter<X extends Message> extends TradistaImporter<X> {

	private static final long serialVersionUID = 2843562569558766619L;

	public static final String FIX = "Fix";

	private String configFileName;

	private MessageCracker messageCracker;

	private static SessionSettings settings;

	private static Session session;

	protected FixImporter(String name, String configFileName, LegalEntity po) {
		setName(name);
		this.configFileName = configFileName;
		setProcessingOrg(po);
	}

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
		} catch (ConfigError _) {
			// TODO Add error logs
		}
	}

	@Override
	public String getType() {
		return FIX;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	@Override
	protected void validateMessage(X fixMessage) throws TradistaBusinessException {
		try {
			messageCracker.crack(fixMessage, null);
		} catch (UnsupportedMessageType | FieldNotFound | IncorrectTagValue e) {
			throw new TradistaBusinessException(e);
		}
	}

	public static SessionSettings getSessionSettings() {
		return settings;
	}

	private static Session getSession() {
		if (session == null) {
			session = Session.lookupSession(settings.sectionIterator().next());
		}
		return session;
	}

	@SuppressWarnings("unchecked")
	@Override
	public X buildMessage(String externalMessage) {
		try {
			return (X) MessageUtils.parse(getSession(), externalMessage);
		} catch (InvalidMessage ie) {
			throw new TradistaTechnicalException(ie.getMessage());
		}
	}

}