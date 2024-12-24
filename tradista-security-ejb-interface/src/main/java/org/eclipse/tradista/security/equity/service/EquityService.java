package org.eclipse.tradista.security.equity.service;

import java.time.LocalDate;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.equity.model.Equity;

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
public interface EquityService {

	long saveEquity(Equity equity) throws TradistaBusinessException;

	Set<Equity> getEquitiesByCreationDate(LocalDate date);

	Set<Equity> getEquitiesByDates(LocalDate minCreationDate, LocalDate maxCreationDate, LocalDate minActiveDate,
			LocalDate maxActiveDate);

	Set<Equity> getAllEquities();

	Equity getEquityById(long id);

	Set<Equity> getEquitiesByIsin(String isin);

	Equity getEquityByIsinAndExchangeCode(String isin, String exchangeCode);

}