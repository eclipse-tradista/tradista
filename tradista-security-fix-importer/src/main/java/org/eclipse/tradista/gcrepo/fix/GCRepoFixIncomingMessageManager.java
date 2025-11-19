package org.eclipse.tradista.gcrepo.fix;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.repo.fix.RepoFixIncomingMessageManager;
import org.eclipse.tradista.security.gcrepo.importer.model.GCRepoIncomingMessageManager;
import org.eclipse.tradista.security.gcrepo.model.GCBasket;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.gcrepo.service.GCRepoTradeBusinessDelegate;

import quickfix.FieldNotFound;
import quickfix.field.SecurityID;
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

public class GCRepoFixIncomingMessageManager extends RepoFixIncomingMessageManager
		implements GCRepoIncomingMessageManager<TradeCaptureReport> {

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
	public GCBasket getBasket(TradeCaptureReport tcReport) {
		GCBasket basket = new GCBasket();
		try {
			basket.setName(tcReport.getString(SecurityID.FIELD));
		} catch (FieldNotFound _) {
			// Not expected here.
		}
		return basket;
	}

	@Override
	public GCRepoTrade createObject(TradeCaptureReport tcReport) {
		GCRepoTrade trade = new GCRepoTrade();
		trade.setGcBasket(getBasket(tcReport));
		return (GCRepoTrade) fillObject(tcReport, trade);
	}

	@Override
	public long saveObject(GCRepoTrade trade) throws TradistaBusinessException {
		return gcRepoTradeBusinessDelegate.saveGCRepoTrade(trade, null);
	}

}