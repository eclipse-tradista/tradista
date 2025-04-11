package org.eclipse.tradista.fix.importer.model;

import java.io.InputStream;
import java.util.Optional;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.importer.model.Importer;
import org.eclipse.tradista.core.message.model.Message;

import quickfix.Application;
import quickfix.ApplicationAdapter;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

public class FixImporter extends Importer {

	public static final String FIX = "Fix";

	private String configFileName;

	@Override
	protected void start() {
		InputStream inputStream = this.getClass().getResourceAsStream(configFileName);
		SessionSettings settings;
		SocketAcceptor acceptor;
		try {
			settings = new SessionSettings(inputStream);

			Application application = new ApplicationAdapter();
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
	protected void validateMessage(Object externalMessage) throws TradistaBusinessException {
		// TODO Auto-generated method stub

	}

	@Override
	protected Message createMessage(Object externalMessage) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Optional<? extends TradistaObject> processMessage(Object externalMessage) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	protected void saveObject(TradistaObject tradistaObject) {
		// TODO Auto-generated method stub

	}

}
