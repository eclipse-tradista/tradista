package org.eclipse.tradista.security.bond.service;

import java.time.LocalDate;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.bond.model.Bond;

import jakarta.ejb.Remote;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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
public interface BondService {

	long saveBond(Bond bond) throws TradistaBusinessException;

	Set<Bond> getBondsByCreationDate(LocalDate date);

	Set<Bond> getBondsByDates(LocalDate minCreationDate, LocalDate maxCreationDate, LocalDate minMaturityDate,
			LocalDate maxMaturityDate);

	Set<Bond> getAllBonds();

	Bond getBondById(long id);

	Set<Bond> getBondsByIsin(String isin);

	Bond getBondByIsinAndExchangeCode(String isin, String exchangeCode);

}
