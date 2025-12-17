package org.eclipse.tradista.core.importer.workflow.process;

import org.eclipse.tradista.core.importer.service.ImporterBusinessDelegate;
import org.eclipse.tradista.core.message.workflow.mapping.IncomingMessage;

import finance.tradista.flow.model.Process;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
public class MapIncomingMessage extends Process<IncomingMessage> {

	private static final long serialVersionUID = 8845899160138325978L;

	@Transient
	private transient ImporterBusinessDelegate importerBusinessDelegate;

	public MapIncomingMessage() {
		importerBusinessDelegate = new ImporterBusinessDelegate();
		setTask(msg -> importerBusinessDelegate.mapIncomingMessage(msg.getOriginalMessage()));
	}

}