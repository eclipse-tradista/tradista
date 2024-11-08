package org.eclipse.tradista.ir.future.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;

import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.calendar.service.CalendarBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.exchange.service.ExchangeBusinessDelegate;
import org.eclipse.tradista.ir.future.model.FutureContractSpecification;
import org.eclipse.tradista.ir.future.persistence.FutureContractSpecificationSQL;
import org.eclipse.tradista.ir.future.service.FutureContractSpecificationService;

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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class FutureContractSpecificationServiceBean implements FutureContractSpecificationService {

	@Override
	public Set<FutureContractSpecification> getAllFutureContractSpecifications() {
		return FutureContractSpecificationSQL.getAllFutureContractSpecifications();
	}

	@Override
	public FutureContractSpecification getFutureContractSpecificationById(long id) {
		return FutureContractSpecificationSQL.getFutureContractSpecificationById(id);
	}

	@Override
	public FutureContractSpecification getFutureContractSpecificationByName(String name) {
		return FutureContractSpecificationSQL.getFutureContractSpecificationByName(name);
	}

	@Override
	public LocalDate getMaturityDate(String contractName, int month, int year) throws TradistaBusinessException {
		if (contractName == null || contractName.isEmpty()) {
			throw new TradistaBusinessException("Contract name cannot be null or empty.");
		}
		LocalDate settlementDate = LocalDate.of(year, month, 1);
		settlementDate = settlementDate.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY))
				.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY)).with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
		Calendar settlementCalendar = null;
		if (contractName.equals("CME EURODOLLAR 3M")) {
			Exchange londonExchange = new ExchangeBusinessDelegate().getExchangeByCode("LONDON");
			if (londonExchange == null) {
				throw new TradistaBusinessException(
						"Cannot find London Exchange definition. Please contact your Tradista administrator");
			}
			settlementCalendar = londonExchange.getCalendar();
		} else if (contractName.equals("ICE EURIBOR 3M")) {
			settlementCalendar = new CalendarBusinessDelegate().getCalendarByCode("ICE FUTURE EUROPE");
		} else if (contractName.equals("EUREX EURIBOR 3M")) {
			settlementCalendar = new CalendarBusinessDelegate().getCalendarByCode("EUREX FUTURE");
		}

		// Subtract 2 Business days
		settlementDate = DateUtil.addBusinessDay(settlementDate, settlementCalendar, -2);
		return settlementDate;
	}

	@Override
	@Interceptors(FutureProductScopeFilteringInterceptor.class)
	public long saveFutureContractSpecification(FutureContractSpecification fcs) throws TradistaBusinessException {
		if (fcs.getId() == 0) {
			checkFutureContractSpecification(fcs);
			return FutureContractSpecificationSQL.saveFutureContractSpecification(fcs);
		} else {
			FutureContractSpecification oldFcs = FutureContractSpecificationSQL
					.getFutureContractSpecificationById(fcs.getId());
			if (!oldFcs.getName().equals(fcs.getName())) {
				checkFutureContractSpecification(fcs);
			}
			return FutureContractSpecificationSQL.saveFutureContractSpecification(fcs);
		}
	}

	private void checkFutureContractSpecification(FutureContractSpecification fcs) throws TradistaBusinessException {
		if (getFutureContractSpecificationByName(fcs.getName()) != null) {
			throw new TradistaBusinessException(
					String.format("This future contract specification with name %s already.", fcs.getName()));
		}
	}

}