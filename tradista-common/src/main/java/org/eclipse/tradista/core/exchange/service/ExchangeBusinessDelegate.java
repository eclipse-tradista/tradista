package org.eclipse.tradista.core.exchange.service;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.exchange.model.Exchange;

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

public class ExchangeBusinessDelegate {

	private ExchangeService exchangeService;

	public ExchangeBusinessDelegate() {
		exchangeService = TradistaServiceLocator.getInstance().getExchangeService();
	}

	public Set<Exchange> getAllExchanges() {
		return SecurityUtil.run(() -> exchangeService.getAllExchanges());
	}

	public Exchange getExchangeById(long id) {
		return SecurityUtil.run(() -> exchangeService.getExchangeById(id));
	}

	public Exchange getExchangeByCode(String code) {
		return SecurityUtil.run(() -> exchangeService.getExchangeByCode(code));
	}

	public long saveExchange(Exchange exchange) throws TradistaBusinessException {
		if (StringUtils.isBlank(exchange.getCode())) {
			throw new TradistaBusinessException("The code cannot be empty.");
		}
		return SecurityUtil.runEx(() -> exchangeService.saveExchange(exchange));
	}

}
