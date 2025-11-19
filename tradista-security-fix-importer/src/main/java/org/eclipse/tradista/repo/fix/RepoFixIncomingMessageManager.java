package org.eclipse.tradista.repo.fix;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.fix.importer.model.FixImporter;
import org.eclipse.tradista.fix.importer.util.TradistaFixImporterUtil;
import org.eclipse.tradista.security.repo.model.RepoTrade;
import org.springframework.util.CollectionUtils;

import quickfix.ConfigError;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.field.MarginRatio;
import quickfix.field.NoSides;
import quickfix.field.NoStipulations;
import quickfix.field.SettlCurrency;
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

public class RepoFixIncomingMessageManager {

	protected static final String RIGHT_OF_SUBSTITUTION = "RightOfSubstitution";

	protected static final String RIGHT_OF_REUSE = "RightOfReuse";

	protected static final String NOTICE_PERIOD = "NoticePeriod";

	protected static final String TERMINABLE_ON_DEMAND = "TerminableOnDemand";

	public void checkMarginRate(TradeCaptureReport tcReport, StringBuilder errMsg) {
		if (!tcReport.isSetMarginRatio()) {
			errMsg.append(String.format("MarginRatio field is mandatory.%n"));
		}
	}

	public void checkRightOfSubstitution(TradeCaptureReport tcReport, StringBuilder errMsg) {
		checkStipulationTypeAndValue(tcReport, errMsg, RIGHT_OF_SUBSTITUTION, TradistaFixImporterUtil.YES_NO_REGEX);
	}

	public void checkRightOfReuse(TradeCaptureReport tcReport, StringBuilder errMsg) {
		checkStipulationTypeAndValue(tcReport, errMsg, RIGHT_OF_REUSE, TradistaFixImporterUtil.YES_NO_REGEX);
	}

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

	public boolean extractTerminableOnDemand(TradeCaptureReport tcReport, StringBuilder errMsg) {
		int errMsgLength = errMsg.length();
		checkStipulationTypeAndValue(tcReport, errMsg, TERMINABLE_ON_DEMAND, TradistaFixImporterUtil.YES_NO_REGEX);
		if (errMsgLength == errMsg.length()) {
			return getTerminableOnDemand(tcReport);
		} else
			return false;
	}

	public void checkNoticePeriod(TradeCaptureReport tcReport, StringBuilder errMsg) {
		checkStipulationTypeAndValue(tcReport, errMsg, NOTICE_PERIOD, TradistaFixImporterUtil.NUMBER_OF_DAYS_REGEX);

	}

	public BigDecimal getMarginRate(TradeCaptureReport tcReport) {
		try {
			return tcReport.getDecimal(MarginRatio.FIELD);
		} catch (FieldNotFound _) {
			// Not expected here.
		}
		return null;
	}

	public boolean getTerminableOnDemand(TradeCaptureReport tcReport) {
		return getStipulationVaue(tcReport, TERMINABLE_ON_DEMAND, Boolean.class);
	}

	public boolean getRightOfSubstitution(TradeCaptureReport tcReport) {
		return getStipulationVaue(tcReport, RIGHT_OF_SUBSTITUTION, Boolean.class);
	}

	public boolean getRightOfReuse(TradeCaptureReport tcReport) {
		return getStipulationVaue(tcReport, RIGHT_OF_REUSE, Boolean.class);
	}

	public short getNoticePeriod(TradeCaptureReport tcReport) {
		return getStipulationVaue(tcReport, NOTICE_PERIOD, Integer.class).shortValue();
	}

	public boolean getCrossCurrencyCollateral(TradeCaptureReport tcReport) {
		Currency currency = null;
		Currency settleCurrency = null;
		try {
			// We compare currency and settlCurrency fields to deduce if the trade is a
			// cross currency one.
			// currency field is already checked in checkCurrency method.
			if (tcReport.isSetNoSides()) {
				for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
					settleCurrency = new Currency(sideGroup.getString(SettlCurrency.FIELD));
					currency = new Currency(sideGroup.getString(quickfix.field.Currency.FIELD));
				}
			}
			return Objects.equals(currency, settleCurrency);
		} catch (FieldNotFound _) {
			// Not expected here.
		}
		return false;
	}

	private <X> X getStipulationVaue(TradeCaptureReport tcReport, String value, Class<X> type) {
		for (Group stipulationGroup : tcReport.getGroups(NoStipulations.FIELD)) {
			try {
				if (stipulationGroup.getString(StipulationType.FIELD).equals(value)) {
					switch (type.getSimpleName()) {
					case "Boolean" -> stipulationGroup.getBoolean(StipulationValue.FIELD);
					case "Integer" -> stipulationGroup.getInt(StipulationValue.FIELD);
					default -> throw new IllegalArgumentException(String.format("This type (%s) is not managed", type));
					}
				}
			} catch (FieldNotFound _) {
				// Not expected here.
			}
		}
		return null;
	}

	private void checkStipulationTypeAndValue(TradeCaptureReport tcReport, StringBuilder errMsg,
			final String propertyName, Pattern pattern) {
		String code = null;
		boolean blockingError = false;
		try {
			code = FixImporter.getSessionSettings().getString(propertyName);
		} catch (ConfigError _) {
			errMsg.append(
					String.format("Please configure %s property in the importer configuration file.%n", propertyName));
			blockingError = true;
		}
		List<Group> stipulationGroups = tcReport.getGroups(NoStipulations.FIELD);
		if (CollectionUtils.isEmpty(stipulationGroups)) {
			errMsg.append(String.format("There should be at least one NoStipulations group.%n"));
			blockingError = true;
		}
		if (blockingError) {
			return;
		}
		for (Group stipulationGroup : tcReport.getGroups(NoStipulations.FIELD)) {
			if (!stipulationGroup.isSetField(StipulationType.FIELD)) {
				errMsg.append(String.format("StipulationType field is mandatory.%n"));
			} else {
				try {
					if (stipulationGroup.getString(StipulationValue.FIELD).equals(code)) {
						TradistaFixImporterUtil.checkFixField(stipulationGroup, StipulationValue.FIELD,
								"StipulationValue", pattern, true, errMsg);
						return;
					}
				} catch (FieldNotFound _) {
					// Not expected here.
				}
			}
			TradistaFixImporterUtil.checkFixField(stipulationGroup, StipulationValue.FIELD, "StipulationValue", pattern,
					true, errMsg);
		}
		errMsg.append(String.format("StipulationValue %s field doesn't exist.%n", code));
	}

	public RepoTrade fillObject(TradeCaptureReport tcReport, RepoTrade trade) {
		trade.setMarginRate(getMarginRate(tcReport));
		trade.setRightOfSubstitution(getRightOfSubstitution(tcReport));
		trade.setRightOfReuse(getRightOfReuse(tcReport));
		trade.setCrossCurrencyCollateral(getCrossCurrencyCollateral(tcReport));
		if (getTerminableOnDemand(tcReport)) {
			trade.setNoticePeriod(getNoticePeriod(tcReport));
		}
		return trade;
	}

}