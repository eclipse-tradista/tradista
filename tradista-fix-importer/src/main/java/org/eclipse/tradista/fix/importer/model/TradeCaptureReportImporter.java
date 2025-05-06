package org.eclipse.tradista.fix.importer.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.importer.model.IncomingMessageManager;
import org.eclipse.tradista.core.importer.model.TradeImporter;
import org.eclipse.tradista.core.importer.util.TradistaImporterUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.fix.common.TradistaFixUtil;

import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.IncorrectTagValue;
import quickfix.MessageCracker.Handler;
import quickfix.SessionID;
import quickfix.field.ExecType;
import quickfix.field.LastQty;
import quickfix.field.NoSides;
import quickfix.field.OrdStatus;
import quickfix.field.SettlDate;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TradeDate;
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

public class TradeCaptureReportImporter extends FixImporter<TradeCaptureReport>
		implements TradeImporter<TradeCaptureReport> {

	@SuppressWarnings("unchecked")
	@Handler
	public void onTradeCaptureReport(TradeCaptureReport tcReport, SessionID sessionID) {
		StringBuilder errMsg = new StringBuilder();
		String productType;

		checkExecTypeAndOrderStatusConsistency(extractExecType(tcReport, errMsg), extractOrderStatus(tcReport, errMsg),
				errMsg);

		checkSymbol(tcReport, errMsg);

		checkNoSides(tcReport, errMsg);

		validateTradeMessage(tcReport, errMsg);

		// Product type specific checks
		// 1. Get product type from message
		productType = getProductType(tcReport);
		// 2. Get the incoming message manager for this product type
		if (productType != null) {
			try {
				IncomingMessageManager<TradeCaptureReport, Trade<?>> incomingMessageManager = (IncomingMessageManager<TradeCaptureReport, Trade<?>>) TradistaImporterUtil
						.getIncomingMessageManager(productType, getType());
				// 3. Use the incoming message manager to check the message.
				incomingMessageManager.checkMessage(tcReport, errMsg);
			} catch (TradistaBusinessException tbe) {
				errMsg.append(String.format(
						"Incoming Message Manager could not be found for product type %s and message type %s",
						productType, getType()));
			}
		}
	}

	public void checkMaturityDate(TradeCaptureReport tcReport, StringBuilder errMsg) {
		if (!tcReport.isSetMaturityDate()) {
			errMsg.append(String.format("Maturity date field is mandatory.%n"));
		}
	}

	public void checkTradeDate(TradeCaptureReport tcReport, StringBuilder errMsg) {
		TradistaFixUtil.checkFixDate(tcReport, TradeDate.FIELD, "TradeDate", true, errMsg);
	}

	public void checkSettlementDate(TradeCaptureReport tcReport, StringBuilder errMsg) {
		TradistaFixUtil.checkFixDate(tcReport, SettlDate.FIELD, "SettlDate", true, errMsg);
	}

	public void checkNotional(TradeCaptureReport tcReport, StringBuilder errMsg) {
		TradistaFixUtil.checkFixAmount(tcReport, LastQty.FIELD, "LastQty", true, errMsg);
	}

	public void checkPrice(TradeCaptureReport tcReport, StringBuilder errMsg) {
		if (!tcReport.isSetLastPx()) {
			errMsg.append(String.format("LastPy field is mandatory.%n"));
		}
	}

	private void checkSymbol(TradeCaptureReport tcReport, StringBuilder errMsg) {
		if (!tcReport.isSetSymbol()) {
			errMsg.append(String.format("Symbol field is mandatory.%n"));
		}
	}

	/**
	 * Managed execution types are new trades, update and cancellation. 'F' : Trade
	 * (normal execution), '0' : New (execution confirmation) ,'H' : Trade Cancel,
	 * 'G' : Trade Update
	 * 
	 * @param tcReport the fix message (TradeCaptureReport type)
	 * @param errMsg   the error message
	 * @return the execType if a valid value was found, null otherwise
	 * 
	 */
	private ExecType extractExecType(TradeCaptureReport tcReport, StringBuilder errMsg) {
		ExecType execType = null;
		try {
			execType = tcReport.getExecType();

			if (execType.getValue() != 'F' && execType.getValue() != '0' && execType.getValue() != 'H'
					&& execType.getValue() != 'G') {
				throw new IncorrectTagValue(execType.getField());
			}
		} catch (FieldNotFound | IncorrectTagValue e) {
			errMsg.append(e);
		}
		return execType;
	}

	/**
	 * Managed order status are filled (2) or canceled (4).
	 * 
	 * @param tcReport the fix message (TradeCaptureReport type)
	 * @param errMsg   the error message
	 * @return the ordStatus if a valid value was found, null otherwise
	 */
	private OrdStatus extractOrderStatus(TradeCaptureReport tcReport, StringBuilder errMsg) {

		OrdStatus ordStatus = null;
		try {
			ordStatus = tcReport.getOrdStatus();

			if (ordStatus.getValue() != '2' && ordStatus.getValue() != '4') {
				throw new IncorrectTagValue(ordStatus.getField());
			}
		} catch (FieldNotFound | IncorrectTagValue e) {
			errMsg.append(e);
		}
		return ordStatus;
	}

	/**
	 * No sides expected value is 2. The currency must be found in the Nosides
	 * group, as long as the party id.
	 * 
	 * @param tcReport the fix message (TradeCaptureReport type)
	 * @param errMsg   the error message
	 */
	private void checkNoSides(TradeCaptureReport tcReport, StringBuilder errMsg) {
		try {
			NoSides noSides = null;
			final char TWO = '2';
			if (!tcReport.isSetNoSides()) {
				errMsg.append(String.format("NoSides field is mandatory.%n"));
				return;
			}

			noSides = tcReport.getNoSides();
			if (noSides.getValue() != TWO) {
				errMsg.append(String.format("NoSides field should be %c.%n", TWO));
			}
		} catch (FieldNotFound fnfe) {
			// Not expected here
		}
	}

	/**
	 * Ensure execType and ordStatus have consistent values.
	 * 
	 * @param execType  the execution type
	 * @param ordStatus the order status
	 * @param errMsg    the error message
	 */
	private void checkExecTypeAndOrderStatusConsistency(ExecType execType, OrdStatus ordStatus, StringBuilder errMsg) {
		if (execType != null && ordStatus != null) {
			try {
				switch (execType.getValue()) {
				case 'F', '0', 'G':
					if (ordStatus.getValue() != '2') {
						throw new IncorrectTagValue(ordStatus.getField(), String.valueOf(ordStatus.getValue()), String
								.format("When the ExecType is %c, OrdStatus should be %c", execType.getValue(), '2'));
					}
					break;
				case 'H':
					if (ordStatus.getValue() != '4') {
						throw new IncorrectTagValue(ordStatus.getField(), String.valueOf(ordStatus.getValue()), String
								.format("When the ExecType is %c, OrdStatus should be %c", execType.getValue(), '4'));
					}
					break;
				}
			} catch (IncorrectTagValue ite) {
				errMsg.append(ite);
			}
		}
	}

	@Override
	protected String getProductType(TradeCaptureReport tcReport) {
		Symbol symbol = null;
		if (tcReport.isSetSymbol()) {
			try {
				symbol = tcReport.getSymbol();
			} catch (FieldNotFound fnfe) {
				// Not expected here.
			}
		}
		if (symbol != null && symbol.getValue().contains("GC")) {
			return "GCRepo";
		}
		return null;
	}

	@Override
	public void checkCurrency(TradeCaptureReport tcReport, StringBuilder errMsg) {
		if (tcReport.isSetNoSides()) {
			for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
				if (!((TradeCaptureReport.NoSides) sideGroup).isSetCurrency()) {
					errMsg.append(String.format("Currency field is mandatory.%n"));
				}
			}
		}
	}

	@Override
	public void checkCounterparty(TradeCaptureReport tcReport, StringBuilder errMsg) {
		if (tcReport.isSetNoSides()) {
			for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
				if (!((TradeCaptureReport.NoSides) sideGroup).isSetSide()) {
					errMsg.append(String.format("Side field is mandatory.%n"));
				} else {
					for (Group partyGroup : tcReport.getGroups(Side.FIELD)) {
						if (!((TradeCaptureReport.NoSides.NoPartyIDs) partyGroup).isSetPartyID()) {
							errMsg.append(String.format("PartyID field is mandatory.%n"));
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Optional<? extends TradistaObject> processMessage(TradeCaptureReport tcReport)
			throws TradistaBusinessException {
		String productType;
		Trade<?> trade = null;
		// Create the right trade type, based on the product type.
		// 1. Get product type from message
		productType = getProductType(tcReport);
		// 2. Get the incoming message manager for this product type
		if (productType != null) {
			try {
				IncomingMessageManager<TradeCaptureReport, Trade<?>> incomingMessageManager = (IncomingMessageManager<TradeCaptureReport, Trade<?>>) TradistaImporterUtil
						.getIncomingMessageManager(productType, getType());
				// 3. Use the incoming message manager to create the trade object.
				trade = incomingMessageManager.createObject(tcReport);
			} catch (TradistaBusinessException tbe) {
				// Not expected here.
			}
		}
		trade.setTradeDate(getTradeDate(tcReport));
		trade.setSettlementDate(getSettlementDate(tcReport));
		// For several product types, 'amount' represents the notional
		trade.setAmount(getNotional(tcReport));
		trade.setCurrency(getCurrency(tcReport));
		return Optional.empty();
	}

	@Override
	public LocalDate getTradeDate(TradeCaptureReport tcReport) {
		return TradistaFixUtil.parseFixDate(tcReport, TradeDate.FIELD);
	}

	@Override
	public LocalDate getSettlementDate(TradeCaptureReport tcReport) {
		return TradistaFixUtil.parseFixDate(tcReport, SettlDate.FIELD);
	}

	@Override
	public BigDecimal getNotional(TradeCaptureReport tcReport) {
		return TradistaFixUtil.parseFixAmount(tcReport, LastQty.FIELD);
	}

	@Override
	public Currency getCurrency(TradeCaptureReport tcReport) throws TradistaBusinessException {
		if (tcReport.isSetNoSides()) {
			for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
				return TradistaFixUtil.parseFixCurrency(sideGroup, quickfix.field.Currency.FIELD);
			}
		}
		return null;
	}

	@Override
	public LegalEntity getCounterparty(TradeCaptureReport tcReport) throws TradistaBusinessException {
		if (tcReport.isSetNoSides()) {
			for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
				for (Group partyGroup : tcReport.getGroups(Side.FIELD)) {
					return TradistaFixUtil.parseFixLegalEntity(partyGroup, quickfix.field.PartyID.FIELD);
				}
			}
		}
		return null;
	}

}