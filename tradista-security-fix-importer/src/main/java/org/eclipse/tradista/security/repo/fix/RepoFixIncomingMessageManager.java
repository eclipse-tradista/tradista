package org.eclipse.tradista.security.repo.fix;

import java.math.BigDecimal;
import java.util.Objects;

import org.eclipse.tradista.fix.importer.util.TradistaFixImporterUtil;

import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.field.MarginRatio;
import quickfix.field.NoSides;
import quickfix.field.SettlCurrency;
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

	protected static final String TERMINABLE_ON_DEMAND = "TerminableOnDemand";

	protected static final String NOTICE_PERIOD = "NoticePeriod";

	protected static final int RIGHT_OF_SUBSTITUTION_TAG = 9001;

	protected static final int RIGHT_OF_REUSE_TAG = 9002;

	protected static final int TERMINABLE_ON_DEMAND_TAG = 9003;

	protected static final int NOTICE_PERIOD_TAG = 9004;

	protected static final int REPO_RATE_TAG = 227;

	public void checkMarginRate(TradeCaptureReport tcReport, StringBuilder errMsg) {
		if (!tcReport.isSetMarginRatio()) {
			errMsg.append(String.format("MarginRatio field is mandatory.%n"));
		}
	}

	public void checkRightOfSubstitution(TradeCaptureReport tcReport, StringBuilder errMsg) {
		// check previously done by QuickFixJ
	}

	public void checkRightOfReuse(TradeCaptureReport tcReport, StringBuilder errMsg) {
		// check previously done by QuickFixJ
	}

	public void checkCrossCurrencyCollateral(TradeCaptureReport tcReport, StringBuilder errMsg) {
		// We compare currency and settlCurrency fields to deduce if the trade is a
		// cross currency one.
		// currency field is already checked in checkCurrency method.
		if (tcReport.isSetNoSides()) {
			for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
				if (!sideGroup.isSetField(SettlCurrency.FIELD)) {
					errMsg.append(String.format("SettlCurrency field is mandatory.%n"));
				}
			}
		}

	}

	public void checkNoticePeriod(TradeCaptureReport tcReport, StringBuilder errMsg) {
		// check previously done by QuickFixJ
	}

	public void checkTerminableOnDemand(TradeCaptureReport tcReport, StringBuilder errMsg) {
		// check previously done by QuickFixJ
	}

	public void checkRepoRate(TradeCaptureReport tcReport, StringBuilder errMsg) {
		TradistaFixImporterUtil.checkFixAmount(tcReport, REPO_RATE_TAG, "Repo Rate", true, errMsg);
	}

	public BigDecimal getMarginRate(TradeCaptureReport tcReport) {
		return getTagValue(tcReport, MarginRatio.FIELD, BigDecimal.class);
	}

	public boolean isTerminableOnDemand(TradeCaptureReport tcReport) {
		return getTagValue(tcReport, TERMINABLE_ON_DEMAND_TAG, Boolean.class);
	}

	public boolean hasRightOfSubstitution(TradeCaptureReport tcReport) {
		return getTagValue(tcReport, RIGHT_OF_SUBSTITUTION_TAG, Boolean.class);
	}

	public boolean hasRightOfReuse(TradeCaptureReport tcReport) {
		return getTagValue(tcReport, RIGHT_OF_REUSE_TAG, Boolean.class);
	}

	public short getNoticePeriod(TradeCaptureReport tcReport) {
		return getTagValue(tcReport, NOTICE_PERIOD_TAG, Integer.class).shortValue();
	}

	public BigDecimal getRepoRate(TradeCaptureReport tcReport) {
		return getTagValue(tcReport, REPO_RATE_TAG, BigDecimal.class);
	}

	public boolean allowsCrossCurrencyCollateral(TradeCaptureReport tcReport) {
		String currency = null;
		String settleCurrency = null;
		try {
			// We compare currency and settlCurrency fields to deduce if the trade is a
			// cross currency one.
			// currency field is already checked in checkCurrency method.
			if (tcReport.isSetNoSides()) {
				for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
					settleCurrency = sideGroup.getString(SettlCurrency.FIELD);
					currency = sideGroup.getString(quickfix.field.Currency.FIELD);
				}
			}
			return Objects.equals(currency, settleCurrency);
		} catch (FieldNotFound _) {
			// Not expected here.
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private <X> X getTagValue(TradeCaptureReport tcReport, int tag, Class<X> type) {
		try {
			switch (type.getSimpleName()) {
			case "Boolean":
				return (X) Boolean.valueOf(tcReport.getBoolean(tag));
			case "Integer":
				return (X) Integer.valueOf(tcReport.getInt(tag));
			case "BigDecimal":
				return (X) tcReport.getDecimal(tag);
			default:
				throw new IllegalArgumentException(String.format("This type (%s) is not managed", type));
			}
		} catch (FieldNotFound _) {
			// Not expected here.
		}
		return null;
	}

}