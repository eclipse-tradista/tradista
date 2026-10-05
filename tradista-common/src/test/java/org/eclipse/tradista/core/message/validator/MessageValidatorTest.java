package org.eclipse.tradista.core.message.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.message.model.IncomingMessage;
import org.eclipse.tradista.core.status.constants.StatusConstants;
import org.eclipse.tradista.core.workflow.model.Status;
import org.junit.jupiter.api.Test;

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

public class MessageValidatorTest {

	private static final MessageValidator validator = new MessageValidator();
	private static final Status status;

	static {
		status = new Status();
		status.setName(StatusConstants.CREATED);
	}

	private IncomingMessage.Builder createValidMessageBuilder() {
		return IncomingMessage.builder().type("CONFIRMATION").objectType("Trade").objectId(12345L)
				.content("Sample confirmation message").status(status);
	}

	@Test
	public void testValidMessage() {
		IncomingMessage message = createValidMessageBuilder().build();
		assertDoesNotThrow(() -> validator.validateMessage(message));
	}

	@Test
	public void testValidMessageWithoutObjectReference() {
		IncomingMessage message = IncomingMessage.builder().type("SYSTEM").status(status).build();
		assertDoesNotThrow(() -> validator.validateMessage(message));
	}

	@Test
	public void testNullMessage() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateMessage(null));
	}

	@Test
	public void testBlankType() {
		IncomingMessage messageNullType = createValidMessageBuilder().type(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateMessage(messageNullType));

		IncomingMessage messageEmptyType = createValidMessageBuilder().type("   ").build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateMessage(messageEmptyType));
	}

	@Test
	public void testBlankObjectTypeWhenObjectIdIsPositive() {
		IncomingMessage message = createValidMessageBuilder().objectType(null).objectId(100L).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateMessage(message));

		IncomingMessage messageBlank = createValidMessageBuilder().objectType("   ").objectId(100L).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateMessage(messageBlank));
	}

	@Test
	public void testObjectTypePresentWhenObjectIdIsNotPositive() {
		IncomingMessage messageZero = createValidMessageBuilder().objectType("Trade").objectId(0L).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateMessage(messageZero));

		IncomingMessage messageNegative = createValidMessageBuilder().objectType("Trade").objectId(-5L).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateMessage(messageNegative));
	}

	@Test
	public void testNullStatus() {
		IncomingMessage message = createValidMessageBuilder().status(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateMessage(message));
	}

}