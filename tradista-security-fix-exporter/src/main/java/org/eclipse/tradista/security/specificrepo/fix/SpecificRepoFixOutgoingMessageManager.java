package org.eclipse.tradista.security.specificrepo.fix;

import org.eclipse.tradista.security.repo.fix.RepoFixOutgoingMessageManager;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;
import org.eclipse.tradista.security.specificrepo.outgoingmessage.SpecificRepoOutgoingMessageManager;

import quickfix.Group;
import quickfix.field.Currency;
import quickfix.field.NoSides;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.fix44.TradeCaptureReport;

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

public class SpecificRepoFixOutgoingMessageManager extends RepoFixOutgoingMessageManager<SpecificRepoTrade>
		implements SpecificRepoOutgoingMessageManager<TradeCaptureReport> {

	@Override
	public TradeCaptureReport createSecurity(TradeCaptureReport tcReport, SpecificRepoTrade trade) {
		tcReport.setField(new SecurityID(trade.getSecurity().getIsin()));
		tcReport.setField(new SecurityIDSource(SecurityIDSource.ISIN_NUMBER));
		return tcReport;
	}

	@Override
	public TradeCaptureReport createCrossCurrencyCollateral(TradeCaptureReport tcReport, SpecificRepoTrade trade) {
		org.eclipse.tradista.core.currency.model.Currency currency = null;
		if (trade.isCrossCurrencyCollateral()) {
			currency = trade.getSecurity().getCurrency();
		} else {
			currency = trade.getCurrency();
		}
		if (tcReport.isSetNoSides()) {
			for (Group sideGroup : tcReport.getGroups(NoSides.FIELD)) {
				sideGroup.setString(Currency.FIELD, currency.getIsoCode());
			}
		}
		return tcReport;
	}

}