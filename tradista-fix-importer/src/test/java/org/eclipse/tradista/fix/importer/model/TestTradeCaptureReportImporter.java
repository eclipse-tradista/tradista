package org.eclipse.tradista.fix.importer.model;

import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.TradeCaptureReport;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

public class TestTradeCaptureReportImporter {

	public static void main(String[] args) throws Exception {

		SessionID sessionID = new SessionID("FIX.4.4", "TESTTARGET", "TESTSEND");
		SessionSettings settings = new SessionSettings();

		settings.setString(sessionID, "ConnectionType", "initiator");
		settings.setString(sessionID, "StartTime", "00:00:00");
		settings.setString(sessionID, "EndTime", "23:59:59");
		settings.setString(sessionID, "HeartBtInt", "30");
		settings.setString(sessionID, "SocketConnectHost", "127.0.0.1");
		settings.setString(sessionID, "SocketConnectPort", "10000");
		settings.setString(sessionID, "BeginString", "FIX.4.4");
		settings.setString(sessionID, "ResetOnLogon", "Y");
		settings.setString(sessionID, "SenderCompID", "TESTTARGET");
		settings.setString(sessionID, "TargetCompID", "TESTSEND");

		// Dummy application
		Application app = new ApplicationAdapter();

		// Message store + log
		MessageStoreFactory storeFactory = new MemoryStoreFactory();
		LogFactory logFactory = new ScreenLogFactory(false, false, false);

		// Initiator creation
		SocketInitiator initiator = new SocketInitiator(app, storeFactory, settings, logFactory,
				new DefaultMessageFactory());

		// Start the initiator
		initiator.start();

		// Wait for login
		int attempts = 0;
		while (!Session.lookupSession(sessionID).isLoggedOn() && attempts < 20) {
			Thread.sleep(500);
			attempts++;
		}

		if (!Session.lookupSession(sessionID).isLoggedOn()) {
			System.err.println("Connection to Tradista importer not possible.");
			initiator.stop();
			return;
		}

		System.out.println("Connected to Tradista importer.");

		System.out.println("Connected, sending the test FIX TradeCaptureReport message...");
		// ------------------------------
		// Building the TradeCaptureReport message
		// ------------------------------
		TradeCaptureReport tcr = new TradeCaptureReport();

		tcr.set(new TradeReportID("TRD-TEST-001"));
		tcr.set(new Symbol("CH0001234567"));
		tcr.setField(new SecurityID("CH0001234567"));
		tcr.setField(new SecurityIDSource(SecurityIDSource.ISIN_NUMBER));

		// GC repo fields
		tcr.setField(new Product(Product.OTHER));
		tcr.setField(new RepoCollateralSecurityType("GC"));
		tcr.setField(new SecurityExchange("XSWX"));

		// Price / Quantity
		tcr.setField(new LastPx(99.85));
		tcr.setField(new LastQty(1000000));

		// Status
		tcr.setField(new ExecType(ExecType.FILL));
		tcr.setField(new OrdStatus(OrdStatus.FILLED));
		tcr.setField(new TransactTime());

		Session.sendToTarget(tcr, sessionID);
		System.out.println("Test FIX TradeCaptureReport message sent.");

		// Stop initiator
		Thread.sleep(1000);
		initiator.stop();
		System.out.println("Test terminated");
	}
}