package org.eclipse.tradista.core.message.validator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.message.model.Message;

public class MessageValidator {

	public void validateMessage(Message message) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (message == null) {
			throw new TradistaBusinessException("the message cannot be null.");
		}
		if (StringUtils.isBlank(message.getType())) {
			errMsg.append(String.format("the message type is mandatory.%n"));
		}
		if (StringUtils.isBlank(message.getObjectType()) && message.getObjectId() > 0) {
			errMsg.append(String.format("the message object type cannot be blank when the object id is positive.%n"));
		}
		if (!StringUtils.isBlank(message.getObjectType()) && message.getObjectId() <= 0) {
			errMsg.append(String.format("the message object id should be positive when the object type is present.%n"));
		}
		if (message.getStatus() == null) {
			errMsg.append("The status is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}
	
}
