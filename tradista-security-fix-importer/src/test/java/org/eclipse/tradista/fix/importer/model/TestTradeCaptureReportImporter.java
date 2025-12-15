package org.eclipse.tradista.fix.importer.model;

import quickfix.Application;
import quickfix.ApplicationAdapter;
import quickfix.BooleanField;
import quickfix.DecimalField;
import quickfix.DefaultMessageFactory;
import quickfix.IntField;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.field.Account;
import quickfix.field.Currency;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.MarginRatio;
import quickfix.field.MaturityDate;
import quickfix.field.NoSides;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.PartyID;
import quickfix.field.PartyRole;
import quickfix.field.PreviouslyReported;
import quickfix.field.Product;
import quickfix.field.RepoCollateralSecurityType;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SettlCurrency;
import quickfix.field.SettlDate;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TradeDate;
import quickfix.field.TradeReportID;
import quickfix.field.TransactTime;
import quickfix.fix44.TradeCaptureReport;
import quickfix.fix44.TradeCaptureReportRequest.NoPartyIDs;

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
		tcr.setField(new TradeDate("20251210"));
		tcr.setField(new SettlDate("20251211"));
		tcr.setField(new MaturityDate("20261211"));
		tcr.setField(new PreviouslyReported(false));
		tcr.setField(new SecurityID("XAAA"));
		tcr.setField(new SecurityIDSource(SecurityIDSource.ISIN_NUMBER));

		// GC repo fields
		tcr.setField(new Product(Product.OTHER));
		tcr.setField(new RepoCollateralSecurityType("GC"));
		tcr.setField(new SecurityExchange("DEMO"));
		tcr.setField(new MarginRatio(5.5));
		// Repo Rate
		tcr.setField(new DecimalField(227, 5));

		// Price / Quantity
		tcr.setField(new LastPx(99.85));
		tcr.setField(new LastQty(1000000));

		// Status
		tcr.setField(new ExecType(ExecType.TRADE));
		tcr.setField(new OrdStatus(OrdStatus.FILLED));
		tcr.setField(new TransactTime());

		tcr.setField(new BooleanField(9001, true));
		tcr.setField(new BooleanField(9002, true));
		tcr.setField(new BooleanField(9003, true));
		tcr.setField(new IntField(9004, 5));

		tcr.setInt(NoSides.FIELD, 2);

		TradeCaptureReport.NoSides buy = new TradeCaptureReport.NoSides();
		buy.set(new Side(Side.BUY));
		buy.set(new OrderID("ORDER-001"));
		buy.set(new Currency("USD"));
		buy.set(new SettlCurrency("USD"));
		NoPartyIDs ctpy = new NoPartyIDs();
		ctpy.set(new PartyID("53"));
		ctpy.set(new PartyRole(17));
		buy.addGroup(ctpy);
		tcr.addGroup(buy);

		TradeCaptureReport.NoSides sell = new TradeCaptureReport.NoSides();
		sell.set(new Side(Side.SELL));
		sell.set(new OrderID("ORDER-002"));
		sell.set(new Currency("USD"));
		sell.set(new SettlCurrency("USD"));
		NoPartyIDs po = new NoPartyIDs();
		po.set(new PartyID("52"));
		po.set(new PartyRole(1));
		po.setField(new Account("444"));
		sell.addGroup(po);
		tcr.addGroup(sell);

		Session.sendToTarget(tcr, sessionID);
		System.out.println("Test FIX TradeCaptureReport message sent.");

		// Stop initiator
		Thread.sleep(1000);
		initiator.stop();
		System.out.println("Test terminated");
	}
}