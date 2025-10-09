package org.eclipse.tradista.core.importer.workflow.process;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.importer.model.Importer;
import org.eclipse.tradista.core.importer.service.ImporterConfigurationBusinessDelegate;
import org.eclipse.tradista.core.message.model.ImportError;
import org.eclipse.tradista.core.message.service.ImportErrorBusinessDelegate;
import org.eclipse.tradista.core.message.workflow.mapping.IncomingMessage;

import finance.tradista.flow.model.Process;
import jakarta.persistence.Entity;

@Entity
public class MapIncomingMessage extends Process<IncomingMessage> {

	private static final long serialVersionUID = 8845899160138325978L;

	private ImporterConfigurationBusinessDelegate importerConfigurationBusinessDelegate;

	private ImportErrorBusinessDelegate importErrorBusinessDelegate;

	@SuppressWarnings("unchecked")
	public MapIncomingMessage() {
		importerConfigurationBusinessDelegate = new ImporterConfigurationBusinessDelegate();
		importErrorBusinessDelegate = new ImportErrorBusinessDelegate();
		setTask(msg -> {
			// Load the importer
			Importer<Object> importer = (Importer<Object>) importerConfigurationBusinessDelegate
					.getImporterByName(msg.geInterfaceName());
			// Build the message object from a string
			Object msgObject = importer.buildMessage(msg.geContent());
			ImportError existingMappingError = null;
			// Apply the mapping
			try {
				existingMappingError = importErrorBusinessDelegate.getImportError(msg.getId(),
						ImportError.ImportErrorType.MAPPING);
				importer.processMessage(msgObject, msg.getOriginalMessage());
				// If ok, solve the existing mapping error if any
				if (existingMappingError != null) {
					existingMappingError.solve();
				}
			} catch (TradistaBusinessException tbe) {
				// Update the existing mapping error if any, otherwise create a new one.
				if (existingMappingError != null) {
					existingMappingError.update(tbe.getMessage());
				}
			}
			if (existingMappingError != null) {
				importErrorBusinessDelegate.saveImportError(existingMappingError);
			}
		});
	}

}