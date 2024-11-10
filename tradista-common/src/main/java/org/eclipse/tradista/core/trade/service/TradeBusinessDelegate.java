package org.eclipse.tradista.core.trade.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class TradeBusinessDelegate {

	private TradeService tradeService;

	public TradeBusinessDelegate() {
		tradeService = TradistaServiceLocator.getInstance().getTradeService();
	}

	public List<Trade<? extends Product>> getTradesByCreationDate(LocalDate creationDate) {
		return SecurityUtil.run(() -> tradeService.getTradesByCreationDate(creationDate));
	}

	public List<Trade<? extends Product>> getTradesByDates(LocalDate startCreationDate, LocalDate endCreationDate,
			LocalDate startTradeDate, LocalDate endTradeDate) throws TradistaBusinessException {
		StringBuilder errorMsg = new StringBuilder();
		if (startCreationDate != null && endCreationDate != null) {
			if (endCreationDate.isBefore(startCreationDate)) {
				errorMsg.append(String.format("'To' creation date cannot be before 'From' creation date.%n"));
			}
		}
		if (startTradeDate != null && endTradeDate != null) {
			if (endTradeDate.isBefore(startTradeDate)) {
				errorMsg.append("'To' trade date cannot be before 'From' trade date.");
			}
		}
		if (errorMsg.length() > 0) {
			throw new TradistaBusinessException(errorMsg.toString());
		}
		return SecurityUtil.run(
				() -> tradeService.getTradesByDates(startCreationDate, endCreationDate, startTradeDate, endTradeDate));
	}

	public Trade<? extends Product> getTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> tradeService.getTradeById(id));
	}

	public Set<Trade<? extends Product>> getTrades(PositionDefinition posDef) {
		return SecurityUtil.run(() -> tradeService.getTrades(posDef));
	}

	public Trade<?> getTradeById(long tradeId, boolean includeUnderlying) throws TradistaBusinessException {
		if (tradeId <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> tradeService.getTradeById(tradeId, includeUnderlying));
	}

}