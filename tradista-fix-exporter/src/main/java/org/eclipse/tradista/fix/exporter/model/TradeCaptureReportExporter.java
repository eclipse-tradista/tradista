package org.eclipse.tradista.fix.exporter.model;

import java.time.format.DateTimeFormatter;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.messaging.EventListener;
import org.eclipse.tradista.core.exporter.model.OutgoingMessageManager;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.trade.incomingmessage.TradeExporter;
import org.eclipse.tradista.core.trade.messaging.TradeEvent;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.fix.common.TradistaFixConstants;
import org.eclipse.tradista.fix.exporter.util.TradistaFixExporterUtil;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;

import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.field.Account;
import quickfix.field.LastQty;
import quickfix.field.NoSides;
import quickfix.field.PartyID;
import quickfix.field.PartyRole;
import quickfix.field.SettlCurrency;
import quickfix.field.SettlDate;
import quickfix.field.Side;
import quickfix.field.TradeDate;
import quickfix.fix44.TradeCaptureReport;
import quickfix.fix44.TradeCaptureReport.NoSides.NoPartyIDs;

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

public class TradeCaptureReportExporter extends FixExporter<Trade<?>, TradeCaptureReport>
		implements TradeExporter<TradeCaptureReport>, EventListener<TradeEvent<?>> {

	protected static final String NO_PARTY_IDS_FIELD_IS_MANDATORY = "NoPartyIDs field is mandatory.%n";

	public TradeCaptureReportExporter(String name, String configFileName, LegalEntity po)
			throws TradistaBusinessException {
		super(name, configFileName, po);
	}

	@ServiceActivator(inputChannel = "tradeCaptureReportExporterQueue", poller = @Poller(fixedDelay = "${tradeCaptureReport.poller.delay:5000}", maxMessagesPerPoll = "${tradeCaptureReport.poller.max:20}"), adviceChain = {
			"dlqAdvice", "txAdvice" })
	public void processEvent(TradeEvent<?> event) throws TradistaBusinessException {
		this.exportObject(event.getTrade());
	}

	public TradeCaptureReport createContent(Trade<?> trade) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		TradeCaptureReport tcReport = new TradeCaptureReport();

		TradeCaptureReport.NoSides buy = new TradeCaptureReport.NoSides();
		buy.set(new Side(Side.BUY));
		NoPartyIDs buyParty = new NoPartyIDs();
		buy.addGroup(buyParty);
		tcReport.addGroup(buy);

		TradeCaptureReport.NoSides sell = new TradeCaptureReport.NoSides();
		sell.set(new Side(Side.SELL));
		NoPartyIDs sellParty = new NoPartyIDs();
		sell.addGroup(sellParty);
		tcReport.addGroup(sell);

		OutgoingMessageManager<TradeCaptureReport, Trade<?>> outgoingMessageManager;

		exportTrade(tcReport, trade);

		outgoingMessageManager = getOutgoingMessageManager(trade, errMsg);

		if (outgoingMessageManager != null) {
			// Use the outgoing message manager to create product type specific content.
			outgoingMessageManager.createContent(tcReport, trade);
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		return tcReport;
	}

	@Override
	protected String getProductType(Trade<?> trade) {
		if (trade != null) {
			return trade.getProductType();
		}
		return null;
	}

	@Override
	public TradeCaptureReport exportTradeDate(Trade<?> trade, TradeCaptureReport tcReport) {
		tcReport.setField(new TradeDate(DateTimeFormatter.BASIC_ISO_DATE.format(trade.getTradeDate())));
		return tcReport;
	}

	@Override
	public TradeCaptureReport exportSettlementDate(Trade<?> trade, TradeCaptureReport tcReport) {
		tcReport.setField(new SettlDate(DateTimeFormatter.BASIC_ISO_DATE.format(trade.getSettlementDate())));
		return tcReport;
	}

	@Override
	public TradeCaptureReport exportNotional(Trade<?> trade, TradeCaptureReport tcReport) {
		// For several product types, 'amount' represents the notional
		tcReport.setField(new LastQty(trade.getAmount().doubleValue()));
		return tcReport;
	}

	@Override
	public TradeCaptureReport exportCurrency(Trade<?> trade, TradeCaptureReport tcReport) {
		for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
			sideGroup.setField(new SettlCurrency(trade.getCurrency().getIsoCode()));
		}
		return tcReport;
	}

	@Override
	public TradeCaptureReport exportCounterparty(Trade<?> trade, TradeCaptureReport tcReport)
			throws TradistaBusinessException {
		String mappedCpty = TradistaFixExporterUtil.getFixLegalEntity(getName(), trade.getCounterparty().getShortName(),
				getProcessingOrg().getId());
		PartyID counterpartyId = new PartyID(mappedCpty);
		for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
			for (Group partyGroup : sideGroup.getGroups(quickfix.field.NoPartyIDs.FIELD)) {
				try {
					if (sideGroup.getInt(Side.FIELD) == Integer.valueOf(TradistaFixConstants.BUY_SIDE)) {
						if (trade.isSell()) {
							partyGroup.setField(new PartyRole(TradistaFixConstants.CONTRA_FIRM_PARTY_ROLE));
							partyGroup.setField(counterpartyId);
							return tcReport;
						}
					} else {
						if (trade.isBuy()) {
							partyGroup.setField(new PartyRole(TradistaFixConstants.CONTRA_FIRM_PARTY_ROLE));
							partyGroup.setField(counterpartyId);
							return tcReport;
						}
					}
				} catch (FieldNotFound _) {
					// Not expected here
				}
			}
		}
		return tcReport;
	}

	@Override
	public TradeCaptureReport exportBook(Trade<?> trade, TradeCaptureReport tcReport) throws TradistaBusinessException {
		String mappedBook = TradistaFixExporterUtil.getFixBook(getName(), trade.getBook().getName(),
				getProcessingOrg().getId());
		Account accountId = new Account(mappedBook);
		for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
			for (Group partyGroup : sideGroup.getGroups(quickfix.field.NoPartyIDs.FIELD)) {
				try {
					if (partyGroup.getInt(Side.FIELD) == Integer.valueOf(TradistaFixConstants.BUY_SIDE)) {
						if (trade.isBuy()) {
							partyGroup.setField(accountId);
							return tcReport;
						}
					} else {
						if (trade.isSell()) {
							partyGroup.setField(accountId);
							return tcReport;
						}
					}
				} catch (FieldNotFound _) {
					// Not expected here
				}
			}
		}
		return tcReport;
	}

	@Override
	public TradeCaptureReport exportBuySell(Trade<?> trade, TradeCaptureReport tcReport)
			throws TradistaBusinessException {
		PartyID poId = new PartyID(TradistaFixExporterUtil.getFixLegalEntity(getName(),
				trade.getBook().getProcessingOrg().getShortName(), getProcessingOrg().getId()));
		for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
			for (Group partyGroup : sideGroup.getGroups(quickfix.field.NoPartyIDs.FIELD)) {
				try {
					if (sideGroup.getInt(Side.FIELD) == Integer.valueOf(TradistaFixConstants.BUY_SIDE)) {
						if (trade.isBuy()) {
							partyGroup.setField(new PartyRole(TradistaFixConstants.EXECUTING_FIRM_PARTY_ROLE));
							partyGroup.setField(poId);
							return tcReport;
						}
					} else {
						if (trade.isSell()) {
							partyGroup.setField(new PartyRole(TradistaFixConstants.EXECUTING_FIRM_PARTY_ROLE));
							partyGroup.setField(poId);
							return tcReport;
						}
					}
				} catch (FieldNotFound _) {
					// Not expected here
				}
			}
		}
		return tcReport;
	}

}