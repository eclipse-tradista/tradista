package org.eclipse.tradista.core.trade.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;

import jakarta.ejb.Remote;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

@Remote
public interface TradeService {

	List<Trade<? extends Product>> getTradesByCreationDate(LocalDate creationDate);

	List<Trade<? extends Product>> getTradesByDates(LocalDate startCreationDate, LocalDate endCreationDate,
			LocalDate startTradeDate, LocalDate endTradeDate);

	Trade<? extends Product> getTradeById(long id);

	Set<Trade<? extends Product>> getTrades(PositionDefinition posDef);

	Trade<?> getTradeById(long tradeId, boolean includeUnderlying);
}
