package org.eclipse.tradista.core.marketdata.bloombergapi;

import java.io.IOException;
import java.util.Map;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventQueue;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

public class BloombergBPipeProvider extends BloombergProvider {

	/**
	 * Server-API specific authentication system
	 * 
	 * @param userProperties The properties embedding the user properties
	 * @return true if the user is authenticated, false otherwise
	 */
	@Override
	protected void authenticateUser(Map<String, String> properties) {

		if (properties == null) {
			// TODO add log
			throw new TradistaTechnicalException(String.format(
					"Problem with your market data properties (properties null). Ensure your market data provider is %s in %s",
					"BloombergBPipe", "configuration.properties"));
		}

		// Create and start 'session'
		try {
			if (!session.openService("//blp/apiauth")) {
				throw new TradistaTechnicalException("Could not open service " + "//blp/apiauth");
			}
			Service apiAuthSvc = session.getService("//blp/apiauth");
			Request authorizationRequest = apiAuthSvc.createAuthorizationRequest();
			authorizationRequest.set("token", properties.get("bloombergToken"));
			identity = session.createIdentity();
			CorrelationID authorizationRequestID = new CorrelationID(98);

			EventQueue authorizationEventQueue = new EventQueue();

			session.sendAuthorizationRequest(authorizationRequest, identity, authorizationEventQueue,
					authorizationRequestID);

			System.out.println("sent Authorization Request using ipAddress");
			// Wait for 'AuthorizationSuccess' message which indicates
			// that 'identity' can be used.

			// Commenting this part as it it not useful to get the event from
			// the EventQueue. It can be taken from the session directly
			/*
			 * Event authorizationEvent = authorizationEventQueue.nextEvent(); for
			 * (MessageIterator messageIterator = authorizationEvent .messageIterator();
			 * messageIterator.hasNext();) { Message message = messageIterator.next(); if
			 * ("AuthorizationFailure".equals(message.messageType())) { throw new
			 * AzurTechnicalException("Authentication to bloomberg failed." ); } if
			 * ("AuthorizationSuccess".equals(message.messageType())) { // TODO Log OK } }
			 */
			boolean continueToLoop = true;
			while (continueToLoop) {
				Event event = session.nextEvent();
				switch (event.eventType().intValue()) {
				case Event.EventType.Constants.RESPONSE:
					if (!BloombergProvider.handleAuthenticationResponseEvent(event)) {
						throw new TradistaTechnicalException("Authentication to bloomberg failed.");
					}
					continueToLoop = false;
					break;
				default:
					handleOtherEvent(event);
					break;
				}
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}