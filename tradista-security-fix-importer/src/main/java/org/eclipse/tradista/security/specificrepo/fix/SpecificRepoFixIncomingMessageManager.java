package org.eclipse.tradista.security.specificrepo.fix;

import org.eclipse.tradista.security.repo.fix.RepoFixIncomingMessageManager;
import org.eclipse.tradista.security.specificrepo.incomingmessage.SpecificRepoIncomingMessageManager;

import quickfix.FieldNotFound;
import quickfix.field.SecurityExchange;
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

public class SpecificRepoFixIncomingMessageManager extends RepoFixIncomingMessageManager
		implements SpecificRepoIncomingMessageManager<TradeCaptureReport> {

	@Override
	public void checkIsin(TradeCaptureReport tcReport, StringBuilder errMsg) {
		if (!tcReport.isSetSecurityID()) {
			errMsg.append(String.format("SecurityID field is mandatory.%n"));
		}
	}

	@Override
	public void checkExchangeCode(TradeCaptureReport tcReport, StringBuilder errMsg) {
		if (!tcReport.isSetSecurityExchange()) {
			errMsg.append(String.format("SecurityExchange field is mandatory.%n"));
		}
	}

	@Override
	public String getExchangeCode(TradeCaptureReport tcReport) {
		String exchangeCode = null;
		try {
			exchangeCode = tcReport.getString(SecurityExchange.FIELD);
		} catch (FieldNotFound _) {
			// Not expected here.
		}
		return exchangeCode;
	}

	@Override
	public String getIsin(TradeCaptureReport tcReport) {
		String isin = null;
		try {
			isin = tcReport.getString(SecurityID.FIELD);
		} catch (FieldNotFound _) {
			// Not expected here.
		}
		return isin;
	}

}