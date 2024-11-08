package org.eclipse.tradista.core.trade.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.core.trade.service.TradeAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.trade.service.TradeService;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class TradeServiceBean implements TradeService {

	@Override
	public List<Trade<? extends Product>> getTradesByCreationDate(LocalDate creationDate) {
		return TradeSQL.getTradesByCreationDate(creationDate);
	}

	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	@Override
	public List<Trade<? extends Product>> getTradesByDates(LocalDate startCreationDate, LocalDate endCreationDate,
			LocalDate startTradeDate, LocalDate endTradeDate) {
		return TradeSQL.getTradesByDates(startCreationDate, endCreationDate, startTradeDate, endTradeDate);
	}

	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	@Override
	public Trade<? extends Product> getTradeById(long id) {
		return TradeSQL.getTradeById(id, false);
	}

	@Interceptors(TradeAuthorizationFilteringInterceptor.class)
	@Override
	public Trade<? extends Product> getTradeById(long id, boolean includeUnderlying) {
		return TradeSQL.getTradeById(id, includeUnderlying);
	}

	@Override
	public Set<Trade<? extends Product>> getTrades(PositionDefinition posDef) {
		return TradeSQL.getTrades(posDef);
	}

}