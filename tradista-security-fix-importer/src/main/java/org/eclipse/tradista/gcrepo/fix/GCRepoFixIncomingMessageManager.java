package org.eclipse.tradista.gcrepo.fix;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.fix.common.TradistaFixUtil;
import org.eclipse.tradista.fix.importer.model.FixImporter;
import org.eclipse.tradista.security.gcrepo.importer.model.GCRepoIncomingMessageManager;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.gcrepo.service.GCRepoTradeBusinessDelegate;
import org.springframework.util.CollectionUtils;

import quickfix.ConfigError;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.field.NoSides;
import quickfix.field.NoStipulations;
import quickfix.field.StipulationType;
import quickfix.field.StipulationValue;
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

public class GCRepoFixIncomingMessageManager implements GCRepoIncomingMessageManager<TradeCaptureReport> {

	private GCRepoTradeBusinessDelegate gcRepoTradeBusinessDelegate;

	public GCRepoFixIncomingMessageManager() {
		gcRepoTradeBusinessDelegate = new GCRepoTradeBusinessDelegate();
	}

	@Override
	public void checkBasket(TradeCaptureReport tcReport, StringBuilder errMsg) {
		if (!tcReport.isSetSecurityID()) {
			errMsg.append(String.format("SecurityID field is mandatory.%n"));
		}
	}

	@Override
	public void checkMarginRate(TradeCaptureReport tcReport, StringBuilder errMsg) {
		if (!tcReport.isSetMarginRatio()) {
			errMsg.append(String.format("MarginRatio field is mandatory.%n"));
		}
	}

	@Override
	public void checkRightOfSubstitution(TradeCaptureReport tcReport, StringBuilder errMsg) {
		checkStipulationTypeAndValue(tcReport, errMsg, "RightOfSubstitution", TradistaFixUtil.YES_NO_REGEX);
	}

	private void checkStipulationTypeAndValue(TradeCaptureReport tcReport, StringBuilder errMsg,
			final String propertyName, Pattern pattern) {
		List<Group> stipulationGroups = tcReport.getGroups(NoStipulations.FIELD);
		if (CollectionUtils.isEmpty(stipulationGroups)) {
			errMsg.append(String.format("There should be at least one NoStipulations group.%n"));
			return;
		}
		for (Group stipulationGroup : tcReport.getGroups(NoStipulations.FIELD)) {
			if (!stipulationGroup.isSetField(StipulationType.FIELD)) {
				errMsg.append(String.format("StipulationType field is mandatory.%n"));
			} else {
				try {
					String code = FixImporter.getSessionSettings().getString(propertyName);
					if (!stipulationGroup.getString(StipulationType.FIELD).equals(code)) {
						errMsg.append(String.format("StipulationType field should be %s.%n", code));
					}
				} catch (ConfigError ce) {
					errMsg.append(String.format("Please configure %s property in the importer configuration file.%n",
							propertyName));
				} catch (FieldNotFound fnfe) {
					// Not expected here.
				}
			}
			TradistaFixUtil.checkFixField(stipulationGroup, StipulationValue.FIELD, "StipulationValue", pattern, true,
					errMsg);
		}
	}

	@Override
	public void checkRightOfReuse(TradeCaptureReport tcReport, StringBuilder errMsg) {
		checkStipulationTypeAndValue(tcReport, errMsg, "RightOfReuse", TradistaFixUtil.YES_NO_REGEX);
	}

	@Override
	public void checkCrossCurrencyCollateral(TradeCaptureReport tcReport, StringBuilder errMsg) {
		// We compare currency and settlCurrency fields to deduce if the trade is a
		// cross currency one.
		// currency field is already checked in checkCurrency method.
		if (tcReport.isSetNoSides()) {
			for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
				if (!((TradeCaptureReport.NoSides) sideGroup).isSetSettlCurrency()) {
					errMsg.append(String.format("SettlCurrency field is mandatory.%n"));
				}
			}
		}

	}

	@Override
	public boolean extractTerminableOnDemand(TradeCaptureReport tcReport, StringBuilder errMsg) {
		int errMsgLength = errMsg.length();
		checkStipulationTypeAndValue(tcReport, errMsg, "TerminableOnDemand", TradistaFixUtil.YES_NO_REGEX);
		return errMsgLength == errMsg.length();
	}

	@Override
	public void checkNoticePeriod(TradeCaptureReport tcReport, StringBuilder errMsg) {
		checkStipulationTypeAndValue(tcReport, errMsg, "NoticePeriod", TradistaFixUtil.NUMBER_OF_DAYS_REGEX);
	}

	@Override
	public GCRepoTrade createObject(TradeCaptureReport externalMessage) {
		return new GCRepoTrade();
	}

	@Override
	public long saveObject(GCRepoTrade trade) throws TradistaBusinessException {
		return gcRepoTradeBusinessDelegate.saveGCRepoTrade(trade, null);
	}

}