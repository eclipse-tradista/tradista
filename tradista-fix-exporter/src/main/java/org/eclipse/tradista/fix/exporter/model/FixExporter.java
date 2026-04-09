package org.eclipse.tradista.fix.exporter.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.exporter.TradistaExporter;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.fix.common.TradistaFixUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.ApplicationAdapter;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.fix44.Message;

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

public abstract class FixExporter<X extends TradistaObject, Y extends Message> extends TradistaExporter<X, Y> {

	public static final String FIX = "Fix";

	private String configFileName;

	private static SessionSettings settings;

	private static Session session;

	private static Application app = new ApplicationAdapter();

	private static final Logger logger = LoggerFactory.getLogger(FixExporter.class);

	protected FixExporter(String name, String configFileName, LegalEntity po) {
		setName(name);
		this.configFileName = configFileName;
		setProcessingOrg(po);
	}

	@Override
	protected void start() {
		try (InputStream inputStream = new FileInputStream(configFileName)) {
			settings = new SessionSettings(inputStream);
			FileStoreFactory storeFactory = new FileStoreFactory(settings);
			LogFactory logFactory = new ScreenLogFactory(settings);

			// Initiator creation
			SocketInitiator initiator = new SocketInitiator(app, storeFactory, settings, logFactory,
					new DefaultMessageFactory());

			// Start the initiator
			initiator.start();
		}

		catch (ConfigError | IOException e) {
			logger.error(e.getMessage());
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
	public void sendMessage(Y content) throws TradistaBusinessException {
		try {
			Session.sendToTarget(content, getSession().getSessionID());
		} catch (SessionNotFound se) {
			throw new TradistaTechnicalException(se);
		}
		logger.debug("FIX TradeCaptureReport message sent: {}", content);
	}

	private static Session getSession() {
		if (session == null) {
			session = Session.lookupSession(settings.sectionIterator().next());
		}
		return session;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Y buildMessage(String externalMessage) {
		return (Y) TradistaFixUtil.buildMessage(getSession(), externalMessage);
	}

}