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
