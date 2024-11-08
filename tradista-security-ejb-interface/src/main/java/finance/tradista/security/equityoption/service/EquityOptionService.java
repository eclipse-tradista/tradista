package org.eclipse.tradista.security.equityoption.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.security.equityoption.model.EquityOption;

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
public interface EquityOptionService {

	long saveEquityOption(EquityOption equityOption) throws TradistaBusinessException;

	Set<EquityOption> getEquityOptionsByCreationDate(LocalDate date);

	Set<EquityOption> getEquityOptionsByCreationDate(LocalDate minDate, LocalDate maxDate);

	Set<EquityOption> getAllEquityOptions();

	EquityOption getEquityOptionById(long id);

	EquityOption getEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName(String code,
			OptionTrade.Type type, BigDecimal strike, LocalDate maturityDate, String contractSpecificationName);

	Set<EquityOption> getEquityOptionsByCode(String code);

}