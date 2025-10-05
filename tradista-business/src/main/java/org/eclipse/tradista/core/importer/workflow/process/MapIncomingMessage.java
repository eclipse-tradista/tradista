package org.eclipse.tradista.core.importer.workflow.process;

import java.time.LocalDateTime;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.importer.model.Importer;
import org.eclipse.tradista.core.importer.service.ImporterConfigurationBusinessDelegate;
import org.eclipse.tradista.core.message.model.ImportError;
import org.eclipse.tradista.core.message.workflow.mapping.IncomingMessage;

import finance.tradista.flow.model.Process;
import jakarta.persistence.Entity;

@Entity
public class MapIncomingMessage extends Process<IncomingMessage> {

	private static final long serialVersionUID = 8845899160138325978L;

	private ImporterConfigurationBusinessDelegate importerConfigurationBusinessDelegate;

	@SuppressWarnings("unchecked")
	public MapIncomingMessage() {
		importerConfigurationBusinessDelegate = new ImporterConfigurationBusinessDelegate();
		setTask(msg -> {
			// Load the importer
			Importer<Object> importer = (Importer<Object>) importerConfigurationBusinessDelegate
					.getImporterByName(msg.geInterfaceName());
			// Build the message object from a string
			Object msgObject = importer.buildMessage(msg.geContent());
			// Apply the mapping
			try {
				importer.processMessage(msgObject);
				// If ok, solve the existing mapping error if any
			} catch (TradistaBusinessException tbe) {
				// Update the existing mapping error if any, otherwise create a new one.
//				ImportError importError = new ImportError();
//				importError.setErrorDate(LocalDateTime.now());
//				importError.setErrorMessage(tbe.getMessage());
//				importError.setStatus(Status.UNSOLVED);
//				importError.setMessage(msg);
//				importErrorBusinessDelegate.saveImportError(importError);
			}

		});
	}

}