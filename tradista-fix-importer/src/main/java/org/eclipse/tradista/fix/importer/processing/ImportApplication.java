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
import quickfix.fix44.TradeCaptureReport;

public class ImportApplication<X extends Message> extends ApplicationAdapter {

	private FixImporter fixImporter;

	public ImportApplication(FixImporter fixImporter) {
		this.fixImporter = fixImporter;
	}

	@Override
	public void fromApp(Message message, SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		try {
			fixImporter.importMessage((X) message);
		} catch (TradistaBusinessException tbe) {
			// TODO Add logs
		}
	}

}
