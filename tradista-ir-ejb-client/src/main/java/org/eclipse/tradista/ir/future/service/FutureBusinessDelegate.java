package org.eclipse.tradista.ir.future.service;

import java.time.Month;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.ir.future.model.Future;
import org.eclipse.tradista.ir.future.validator.FutureValidator;

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

public class FutureBusinessDelegate {

	private FutureService futureService;

	private FutureValidator futureValidator;

	public FutureBusinessDelegate() {
		futureService = TradistaServiceLocator.getInstance().getFutureService();
		futureValidator = new FutureValidator();
	}

	public Set<Future> getAllFutures() {
		return SecurityUtil.run(() -> futureService.getAllFutures());
	}

	public Future getFutureById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException(String.format("The id (%s) must be positive.", id));
		}
		return SecurityUtil.run(() -> futureService.getFutureById(id));
	}

	public Future getFutureByContractSpecificationAndSymbol(String contractSpecification, String symbol)
			throws TradistaBusinessException {
		if (StringUtils.isEmpty(contractSpecification)) {
			throw new TradistaBusinessException("The contract specification is mandatory.");
		}
		if (StringUtils.isEmpty(symbol)) {
			throw new TradistaBusinessException("The symbol is mandatory.");
		}
		// Checking symbol format.
		if (!isValidSymbol(symbol)) {
			throw new TradistaBusinessException(String.format("The symbol (%s) is not valid.", symbol));
		}

		return SecurityUtil
				.run(() -> futureService.getFutureByContractSpecificationAndSymbol(contractSpecification, symbol));
	}

	public long saveFuture(Future future) throws TradistaBusinessException {
		futureValidator.validateProduct(future);
		return SecurityUtil.runEx(() -> futureService.saveFuture(future));
	}

	public Month getMonth(String symbolMonth) throws TradistaBusinessException {
		if (StringUtils.isEmpty(symbolMonth)) {
			throw new TradistaBusinessException("The contract specification name is mandatory.");
		}
		if (symbolMonth.length() != 3) {
			throw new TradistaBusinessException("The symbol month's length must be 3 characters.");
		}

		switch (symbolMonth) {
		case ("JAN"): {
			return Month.JANUARY;
		}
		case ("FEB"): {
			return Month.FEBRUARY;
		}
		case ("MAR"): {
			return Month.MARCH;
		}
		case ("APR"): {
			return Month.APRIL;
		}
		case ("MAY"): {
			return Month.MAY;
		}
		case ("JUN"): {
			return Month.JUNE;
		}
		case ("JUL"): {
			return Month.JULY;
		}
		case ("AUG"): {
			return Month.AUGUST;
		}
		case ("SEP"): {
			return Month.SEPTEMBER;
		}
		case ("OCT"): {
			return Month.OCTOBER;
		}
		case ("NOV"): {
			return Month.NOVEMBER;
		}
		case ("DEC"): {
			return Month.DECEMBER;
		}
		}

		throw new TradistaBusinessException(String.format("The symbol month '%s' is not a valid one.", symbolMonth));
	}

	public boolean isValidSymbol(String symbol) {
		if (StringUtils.isEmpty(symbol)) {
			return false;
		}
		if (symbol.length() != 5) {
			return false;
		}

		String month = symbol.substring(0, 3);
		Month m;
		try {
			m = getMonth(month);
		} catch (TradistaBusinessException _) {
			return false;
		}
		if (m == null) {
			return false;
		}
		try {
			Integer.parseInt(symbol.substring(3));
		} catch (NumberFormatException _) {
			return false;
		}
		return true;
	}

}