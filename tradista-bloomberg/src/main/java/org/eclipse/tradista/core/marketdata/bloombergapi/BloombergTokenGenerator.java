package org.eclipse.tradista.core.marketdata.bloombergapi;

import java.io.IOException;
import java.util.Properties;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventQueue;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;

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

public class BloombergTokenGenerator {

	private static final Name TOKEN_SUCCESS = Name.getName("TokenGenerationSuccess");
	private static final Name TOKEN_FAILURE = Name.getName("TokenGenerationFailure");

	/**
	 * Generates token. It is needed in B-Pipe mode. The token generation is done on
	 * client side.
	 * 
	 * @param userProperties
	 * @return the generated token
	 */
	public String generateToken(Properties userProperties) {
		Session session;
		String token = null;
		SessionOptions sessionOptions = new SessionOptions();
		sessionOptions.setServerHost(userProperties.getProperty("host")); // default
																			// value
		sessionOptions.setServerPort(Integer.parseInt(userProperties.getProperty("port"))); // default value
		switch (userProperties.getProperty("bloombergAuthenticationMode")) {
		case "USER": {
			sessionOptions.setAuthenticationOptions(getUserModeAuthenticationOptions(userProperties));
			break;
		}
		case "APPLICATION": {
			sessionOptions.setAuthenticationOptions(getApplicationModeAuthenticationOptions(userProperties));
			break;
		}
		case "USER_AND_APPLICATION": {
			sessionOptions.setAuthenticationOptions(getUserAndApplicationModeAuthenticationOptions(userProperties));
			break;
		}
		}
		session = new Session(sessionOptions);
		try {
			if (!session.start()) {
				System.out.println("Could not start session.");
				// TO DO thrower une exception technique
			}

			CorrelationID tokenGenerationId = new CorrelationID(99);
			EventQueue tokenEventQueue = new EventQueue();
			session.generateToken(tokenGenerationId, tokenEventQueue);

			Event tokenEvent = tokenEventQueue.nextEvent(); // blocking
			for (MessageIterator messageIterator = tokenEvent.messageIterator(); messageIterator.hasNext();) {
				Message message = messageIterator.next();
				if (TOKEN_FAILURE.equals(message.messageType())) {
					// Failed to obtain token
					// TODO throw a technical exception
					return null;
				}
				if (TOKEN_SUCCESS.equals(message.messageType())) {
					token = message.getElementAsString("token");
					break;
				}
			}

		} catch (IOException | InterruptedException e) {
			// TODO throw a technical exception
		}

		return token;
	}

	private String getUserAndApplicationModeAuthenticationOptions(Properties userProperties) {
		return "AuthenticationMode=USER_AND_APPLICATION;ApplicationAuthenticationType=APPNAME_AND_KEY;ApplicationName="
				+ userProperties.getProperty("BloombergApplicationName") + ";AuthenticationType=OS_LOGON";
	}

	private String getApplicationModeAuthenticationOptions(Properties userProperties) {
		return "AuthenticationMode=APPLICATION_ONLY;ApplicationAuthenticationType=APPNAME_AND_KEY;ApplicationName="
				+ userProperties.getProperty("BloombergApplicationName");
	}

	private String getUserModeAuthenticationOptions(Properties userProperties) {
		if (userProperties.getProperty("bloombergAuthenticationType").equals("OS_LOGON")) {
			return "AuthenticationType=OS_LOGON";
		} else {
			return "AuthenticationType=DIRECTORY_SERVICE;DirSvcProperty=mail";
		}
	}

}
