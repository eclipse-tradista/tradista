package org.eclipse.tradista.core.exchange.service;

import java.util.Set;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.exchange.persistence.ExchangeSQL;
import org.eclipse.tradista.core.exchange.service.ExchangeService;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

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
public class ExchangeServiceBean implements ExchangeService {

	@Override
	public Set<Exchange> getAllExchanges() {
		return ExchangeSQL.getAllExchanges();
	}

	@Override
	public Exchange getExchangeById(long id) {
		return ExchangeSQL.getExchangeById(id);
	}

	@Override
	public Exchange getExchangeByCode(String code) {
		return ExchangeSQL.getExchangeByCode(code);
	}

	@Override
	public Exchange getExchangeByName(String name) {
		return ExchangeSQL.getExchangeByName(name);
	}

	@Override
	public long saveExchange(Exchange exchange) throws TradistaBusinessException {
		if (exchange.getId() == 0) {
			checkCodeExistence(exchange);
			checkNameExistence(exchange);
		} else {
			Exchange oldExchange = ExchangeSQL.getExchangeById(exchange.getId());
			if (!oldExchange.getCode().equals(exchange.getCode())) {
				checkCodeExistence(exchange);
			}
			if (!oldExchange.getName().equals(exchange.getName())) {
				checkNameExistence(exchange);
			}
		}
		return ExchangeSQL.saveExchange(exchange);
	}

	private void checkCodeExistence(Exchange exchange) throws TradistaBusinessException {
		if (getExchangeByCode(exchange.getCode()) != null) {
			throw new TradistaBusinessException(
					String.format("An exchange with the code '%s' already exists in the system.", exchange.getCode()));
		}
	}

	private void checkNameExistence(Exchange exchange) throws TradistaBusinessException {
		if (getExchangeByName(exchange.getName()) != null) {
			throw new TradistaBusinessException(
					String.format("An exchange with the name '%s' already exists in the system.", exchange.getName()));
		}
	}
}
