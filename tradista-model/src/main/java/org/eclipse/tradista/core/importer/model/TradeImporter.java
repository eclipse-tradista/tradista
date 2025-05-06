package org.eclipse.tradista.core.importer.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

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

public interface TradeImporter<X> {

	default void validateTradeMessage(X externalMessage, StringBuilder errMsg) {
		checkTradeDate(externalMessage, errMsg);
		checkSettlementDate(externalMessage, errMsg);
		checkMaturityDate(externalMessage, errMsg);
		checkNotional(externalMessage, errMsg);
		checkPrice(externalMessage, errMsg);
		checkCurrency(externalMessage, errMsg);
		checkCounterparty(externalMessage, errMsg);
	}

	void checkTradeDate(X externalMessage, StringBuilder errMsg);

	void checkSettlementDate(X externalMessage, StringBuilder errMsg);

	void checkMaturityDate(X externalMessage, StringBuilder errMsg);

	void checkNotional(X externalMessage, StringBuilder errMsg);

	void checkPrice(X externalMessage, StringBuilder errMsg);

	void checkCurrency(X externalMessage, StringBuilder errMsg);

	void checkCounterparty(X externalMessage, StringBuilder errMsg);

	LocalDate getTradeDate(X externalMessage);

	LocalDate getSettlementDate(X externalMessage);

	BigDecimal getNotional(X externalMessage);

	Currency getCurrency(X externalMessage) throws TradistaBusinessException;

	LegalEntity getCounterparty(X externalMessage) throws TradistaBusinessException;

}