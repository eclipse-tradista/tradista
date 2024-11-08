package org.eclipse.tradista.ir.future.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.daterule.service.DateRuleBusinessDelegate;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.ir.future.model.FutureContractSpecification;
import org.eclipse.tradista.ir.future.service.FutureContractSpecificationService;

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

public class FutureContractSpecificationBusinessDelegate {

	private FutureContractSpecificationService futureContractSpecificationService;

	private FutureBusinessDelegate futureBusinessDelegate;

	public FutureContractSpecificationBusinessDelegate() {
		futureContractSpecificationService = TradistaServiceLocator.getInstance()
				.getFutureContractSpecificationService();
		futureBusinessDelegate = new FutureBusinessDelegate();
	}

	public Set<FutureContractSpecification> getAllFutureContractSpecifications() {
		return SecurityUtil.run(() -> futureContractSpecificationService.getAllFutureContractSpecifications());
	}

	public FutureContractSpecification getFutureContractSpecificationById(long id) {
		return SecurityUtil.run(() -> futureContractSpecificationService.getFutureContractSpecificationById(id));
	}

	public FutureContractSpecification getFutureContractSpecificationByName(String name) {
		return SecurityUtil.run(() -> futureContractSpecificationService.getFutureContractSpecificationByName(name));
	}

	public LocalDate getMaturityDate(String contractName, int month, int year) throws TradistaBusinessException {
		return SecurityUtil.runEx(() -> futureContractSpecificationService.getMaturityDate(contractName, month, year));
	}

	public boolean isBusinessDay(FutureContractSpecification spec, LocalDate date) throws TradistaBusinessException {
		Exchange specExchange;
		Calendar exchangeCalendar;
		if (spec == null) {
			throw new TradistaBusinessException("The Future Contract Specification cannot be null");
		}
		specExchange = spec.getExchange();
		if (specExchange == null) {
			throw new TradistaBusinessException("The Future Contract Specification exchange cannot be null");
		}

		exchangeCalendar = specExchange.getCalendar();
		if (exchangeCalendar == null) {
			// TODO Add warning log
		}

		return exchangeCalendar.isBusinessDay(date);
	}

	public LocalDate getMaturityDate(String contractSpecificationName, String symbol) throws TradistaBusinessException {
		if (StringUtils.isEmpty(contractSpecificationName)) {
			throw new TradistaBusinessException("The contract specification name is mandatory.");
		}
		if (!futureBusinessDelegate.isValidSymbol(symbol)) {
			throw new TradistaBusinessException(
					String.format("The symbol's year ('%s') is not correct", symbol.substring(3)));
		}
		int month = new FutureBusinessDelegate().getMonth(symbol.substring(0, 3)).getValue();
		int year = 2000;
		try {
			year += Integer.parseInt(symbol.substring(3));
		} catch (NumberFormatException nfe) {
			// Should not appear here, as the symbol is already validated.
		}

		return getMaturityDate(contractSpecificationName, month, year);
	}

	public long saveFutureContractSpecification(FutureContractSpecification fcs) throws TradistaBusinessException {
		validateFutureContractSpecification(fcs);
		return SecurityUtil.runEx(() -> futureContractSpecificationService.saveFutureContractSpecification(fcs));
	}

	public void validateFutureContractSpecification(FutureContractSpecification fcs) throws TradistaBusinessException {
		if (fcs == null) {
			throw new TradistaBusinessException("The Future Contract Specification cannot be null.");
		}

		StringBuilder errMsg = new StringBuilder();

		if (StringUtils.isEmpty(fcs.getName())) {
			errMsg.append("The name is mandatory.\n");
		}
		if (fcs.getNotional() == null) {
			errMsg.append("The notional is mandatory.\n");
		} else {
			if (fcs.getNotional().doubleValue() <= 0) {
				errMsg.append("The notional must be positive.\n");
			}
		}

		if (fcs.getExchange() == null) {
			errMsg.append("The exchange is mandatory.\n");
		}

		if (fcs.getCurrency() == null) {
			errMsg.append("The currency is mandatory.\n");
		}

		if (fcs.getReferenceRateIndex() == null) {
			errMsg.append("The reference rate index is mandatory.\n");
		}

		if (fcs.getReferenceRateIndexTenor() == null) {
			errMsg.append("The reference rate index tenor is mandatory.\n");
		}

		if (fcs.getMaturityDatesDateRule() == null) {
			errMsg.append("The expiry dates date rule is mandatory.\n");
		}

		if (fcs.getDayCountConvention() == null) {
			errMsg.append("The day count convention is mandatory.\n");
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

	public LocalDate getSymbolMaturityDate(FutureContractSpecification futureContractSpecification, String symbol)
			throws TradistaBusinessException {
		if (StringUtils.isEmpty(symbol)) {
			throw new TradistaBusinessException("The future symbol is mandatory.");
		}
		if (!futureBusinessDelegate.isValidSymbol(symbol)) {
			throw new TradistaBusinessException(
					String.format("The future symbol %s must be a valid one (MMMYY).%n", symbol));
		}
		String month = symbol.substring(0, 3);
		int monthNumber = 0;
		int yearNumber = 0;
		for (Month m : Month.values()) {
			if (m.toString().toUpperCase().startsWith(month)) {
				monthNumber = m.getValue();
				break;
			}
		}
		try {
			yearNumber = Integer.parseInt(symbol.substring(3)) + 2000;
		} catch (NumberFormatException nfe) {
		}
		Set<LocalDate> maturityDates = new DateRuleBusinessDelegate().generateDates(
				futureContractSpecification.getMaturityDatesDateRule(), LocalDate.of(yearNumber, monthNumber, 1),
				Period.ofMonths(1));

		if (maturityDates == null || maturityDates.isEmpty()) {
			return null;
		}
		return maturityDates.toArray(new LocalDate[0])[0];
	}

}