package org.eclipse.tradista.core.marketdata.bloombergapi;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.Provider;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
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

public abstract class BloombergProvider implements Provider {

	private static final Properties properties = new Properties();

	protected Session session;

	protected Identity identity;

	{
		InputStream in = getClass().getResourceAsStream("/bloomberg-api.properties");
		try {
			properties.load(in);
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected abstract void authenticateUser(Map<String, String> properties);

	public void init() {
		SessionOptions sessionOptions = new SessionOptions();
		sessionOptions.setServerHost(properties.getProperty("host")); // default
																		// value
		sessionOptions.setServerPort(Integer.parseInt(properties.getProperty("port"))); // default
																						// value
		session = new Session(sessionOptions);
		try {
			if (!session.start()) {
				System.out.println("Could not start session.");
				// TODO add log
				throw new TradistaTechnicalException("Could not start session.");
			}

			if (!session.openService("//blp/refdata")) {
				System.out.println("Could not open service //blp/refdata");
				// TODO add log
				throw new TradistaTechnicalException("Could not open service //blp/refdata");
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new TradistaTechnicalException(e.getMessage());
		}
	}

	@Override
	public List<QuoteValue> getQuoteValues(FeedConfig feedConfig, Map<String, String> properties) {
		List<QuoteValue> quoteValues = new ArrayList<QuoteValue>();
		authenticateUser(properties);

		CorrelationID requestID = new CorrelationID(1);
		Service refDataSvc = session.getService("//blp/refdata");
		Request request = refDataSvc.createRequest("ReferenceDataRequest");
		for (String data : feedConfig.getMapping().keySet()) {
			request.append("securities", data);
			for (String field : feedConfig.getFieldsMapping().get(data).values()) {
				if (!StringUtils.isEmpty(field)) {
					request.append("fields", field);
				}
			}
		}

		request.set("returnEids", true);
		try {
			session.sendRequest(request, requestID);

			boolean continueToLoop = true;
			while (continueToLoop) {
				Event event;

				event = session.nextEvent();

				switch (event.eventType().intValue()) {
				case Event.EventType.Constants.RESPONSE: // final event
					continueToLoop = false; // fall through
				case Event.EventType.Constants.PARTIAL_RESPONSE:
					handleResponseEvent(event, feedConfig, quoteValues);
					break;
				default:
					handleOtherEvent(event);
					break;
				}
			}

			session.stop();

		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return quoteValues;

	}

	protected static boolean handleAuthenticationResponseEvent(Event event) throws IOException {
		if (hasMessageType(event, "AuthorizationSuccess")) {
			System.err.println("Authorization OK");
			return true;
		} else if (hasMessageType(event, "AuthorizationFailure")) {
			System.err.println("Authorization Problem");

		} else {
			System.err.println("Authorization: Other Problem");

		}
		return false;
	}

	private static boolean hasMessageType(Event event, String messageType) {
		MessageIterator messageIterator = event.messageIterator();
		while (messageIterator.hasNext()) {
			Message message = messageIterator.next();
			if (message.messageType().equals(messageType)) {
				return true;
			}
		}
		return false;
	}

	protected void handleOtherEvent(Event event) {
		System.out.println("EventType=" + event.eventType());
		MessageIterator iter = event.messageIterator();
		while (iter.hasNext()) {
			Message message = iter.next();
			System.out.println("correlationID=" + message.correlationID());
			System.out.println("messageType=" + message.messageType());
			if (Event.EventType.Constants.SESSION_STATUS == event.eventType().intValue()
					&& "SessionTerminated" == message.messageType().toString()) {
				System.out.println("Terminating: " + message.messageType());
				throw new TradistaTechnicalException("Bloomberg session terminated. (Message " + message.messageType());
			}
		}
	}

	private void handleResponseEvent(Event event, FeedConfig feedConfig, List<QuoteValue> quoteValues) {
		System.out.println("EventType =" + event.eventType());
		MessageIterator iter = event.messageIterator();
		while (iter.hasNext()) {
			Message message = iter.next();
			Element referenceDataResponse = message.asElement();
			if (referenceDataResponse.hasElement("responseError")) {
				throw new TradistaTechnicalException("Bloomberg response contains an error.");
			}
			Element securityDataArray = referenceDataResponse.getElement("securityData");
			int numItems = securityDataArray.numValues();
			for (int i = 0; i < numItems; ++i) {
				Element securityData = securityDataArray.getValueAsElement(i);
				String security = securityData.getElementAsString("security");
				/*
				 * Sequence number seems only an informative data indicating the sequence order
				 * of the security info received from Bloomberg
				 */
				// int sequenceNumber = securityData
				// .getElementAsInt32("sequenceNumber");
				if (securityData.hasElement("securityError")) {
					// handle error
					// TODO find a way to handle this kind of non blocking
					// error. the process should continue for other securities so we should not
					// raise an exception
				}
				List<Integer> missingEntitlements = new ArrayList<Integer>();
				Element neededEntitlements = securityData.hasElement("eidData") ? securityData.getElement("eidData")
						: null;
				if (null == neededEntitlements) {
					// ok
					addQuoteValues(security, securityData, feedConfig, quoteValues);

				} else if (identity.hasEntitlements(neededEntitlements, message.service(), missingEntitlements)) {
					// ok
					addQuoteValues(security, securityData, feedConfig, quoteValues);
				} else {
					// ko : some entitlements are entitlements
					// TODO find a way to handle this kind of non blocking
					// error. the process should continue for other securities so we should not
					// raise an exception
				}
			}

		}

	}

	private void addQuoteValues(String security, Element securityData, FeedConfig feedConfig,
			List<QuoteValue> quoteValues) {
		Element fieldData = securityData.getElement("fieldData");
		int numFields = fieldData.numElements();
		QuoteValue quoteValue = new QuoteValue(LocalDate.now(), feedConfig.getMapping().get(security));
		quoteValue.setEnteredDate(LocalDate.now());
		quoteValue.setSourceName(feedConfig.getFeedType().toString());
		for (int j = 0; j < numFields; j++) {
			Element field = fieldData.getValueAsElement(j);
			Map<String, String> fieldMapping = feedConfig.getFieldsMapping().get(security);
			for (Map.Entry<String, String> fld : fieldMapping.entrySet()) {
				if (fld.getValue().equals(field.name().toString())) {
					switch (fld.getKey()) {
					case QuoteValue.ASK: {
						quoteValue.setAsk(BigDecimal.valueOf(field.getValueAsFloat64()));
						break;
					}
					case QuoteValue.BID: {
						quoteValue.setBid(BigDecimal.valueOf(field.getValueAsFloat64()));
						break;
					}
					case QuoteValue.CLOSE: {
						quoteValue.setClose(BigDecimal.valueOf(field.getValueAsFloat64()));
						break;
					}
					case QuoteValue.HIGH: {
						quoteValue.setHigh(BigDecimal.valueOf(field.getValueAsFloat64()));
						break;
					}
					case QuoteValue.LAST: {
						quoteValue.setLast(BigDecimal.valueOf(field.getValueAsFloat64()));
						break;
					}
					case QuoteValue.LOW: {
						quoteValue.setLow(BigDecimal.valueOf(field.getValueAsFloat64()));
						break;
					}
					case QuoteValue.OPEN: {
						quoteValue.setOpen(BigDecimal.valueOf(field.getValueAsFloat64()));
						break;
					}
					}
				}

			}
		}
		quoteValues.add(quoteValue);
	}

}