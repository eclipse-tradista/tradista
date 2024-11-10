package org.eclipse.tradista.core.marketdata.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.validator.DefaultQuoteValidator;
import org.eclipse.tradista.core.marketdata.validator.QuoteValidator;

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

public class QuoteBusinessDelegate {

	private QuoteService quoteService;

	private static final String QUOTE_NAME_CANNOT_BE_NULL = "The quote name cannot be null.%n";

	private static final String QUOTE_NAME_CANNOT_BE_EMPTY = "The quote name cannot be empty.%n";

	private static final String QUOTE_SET_ID_MUST_BE_POSITIVE = "The quote set id must be positive.%n";

	public QuoteBusinessDelegate() {
		quoteService = TradistaServiceLocator.getInstance().getQuoteService();
	}

	public List<Quote> getAllQuotes() {
		return SecurityUtil.run(() -> quoteService.getAllQuotes());
	}

	public List<String> getAllQuoteNames() {
		return SecurityUtil.run(() -> quoteService.getAllQuoteNames());
	}

	public QuoteSet getQuoteSetByName(String name) throws TradistaBusinessException {
		if (StringUtils.isEmpty(name)) {
			throw new TradistaBusinessException("The quote set name is mandatory.");
		}
		return SecurityUtil.run(() -> quoteService.getQuoteSetByName(name));
	}

	public QuoteSet getQuoteSetById(long quoteSetId) throws TradistaBusinessException {
		if (quoteSetId <= 0) {
			throw new TradistaBusinessException(String.format(QUOTE_SET_ID_MUST_BE_POSITIVE));
		}
		return SecurityUtil.run(() -> quoteService.getQuoteSetById(quoteSetId));
	}

	public boolean saveQuoteValues(long quoteSetId, String quoteName, QuoteType quoteType, List<QuoteValue> quoteValues,
			Year year, Month month) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (quoteSetId <= 0) {
			errMsg.append(String.format(QUOTE_SET_ID_MUST_BE_POSITIVE));
		}
		if (quoteName == null) {
			errMsg.append(String.format(QUOTE_NAME_CANNOT_BE_NULL));
		} else {
			if (quoteName.isEmpty()) {
				errMsg.append(String.format(QUOTE_NAME_CANNOT_BE_EMPTY));
			}
		}
		if (year == null) {
			errMsg.append(String.format("The year cannot be null.%n"));
		}
		if (month == null) {
			errMsg.append(String.format("The month cannot be null.%n"));
		}

		if (quoteValues == null) {
			errMsg.append(String.format("The quote values list cannot be null.%n"));
		} else {
			if (quoteValues.isEmpty()) {
				errMsg.append(String.format("There must be at least one quote value to be saved.%n"));
			}
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		return SecurityUtil
				.run(() -> quoteService.saveQuoteValues(quoteSetId, quoteName, quoteType, quoteValues, year, month));
	}

	public boolean deleteQuote(String quoteName, QuoteType quoteType) throws TradistaBusinessException {
		if (quoteName == null || quoteName.isEmpty()) {
			throw new TradistaBusinessException("The quote name must be specified.");
		}
		return SecurityUtil.runEx(() -> quoteService.deleteQuote(quoteName, quoteType));
	}

	public long saveQuote(Quote quote) throws TradistaBusinessException {
		if (quote == null) {
			throw new TradistaBusinessException("The quote cannot be null");
		}
		if (quote.getType() == null) {
			throw new TradistaBusinessException("The quote type cannot be null");
		}
		validateQuoteName(quote.getName());
		return SecurityUtil.runEx(() -> quoteService.saveQuote(quote));
	}

	public void validateQuoteName(String quoteName) throws TradistaBusinessException {
		new DefaultQuoteValidator().validateQuoteName(quoteName);
		String[] quoteData = quoteName.split("\\.");
		String quoteCategory = quoteData[0];
		if (quoteCategory == null || quoteCategory.isEmpty()) {
			throw new TradistaBusinessException("The quote name must start with a proper quote category.");
		}
		List<Class<QuoteValidator>> validators = TradistaUtil.getAllClassesByType(QuoteValidator.class,
				"org.eclipse.tradista");
		if (validators == null || validators.isEmpty()) {
			throw new TradistaTechnicalException(
					"No QuoteValidator instances were found in the org.eclipse.tradista packages (and sub-packages).");
		}
		QuoteValidator validator = null;
		for (Class<QuoteValidator> valClass : validators) {
			if (valClass.getName().endsWith(quoteCategory + "QuoteValidator")) {
				validator = TradistaUtil.getInstance(valClass);
				break;
			}
		}
		if (validator == null) {
			throw new TradistaBusinessException(
					String.format("The quote category (%s) is not a valid one.", quoteCategory));
		}

		validator.validateQuoteName(quoteName);
	}

	public Quote getQuoteById(long quoteId) {
		return SecurityUtil.run(() -> quoteService.getQuoteById(quoteId));
	}

	public List<Quote> getQuotesByCurveId(long curveId) {
		return SecurityUtil.run(() -> quoteService.getQuotesByCurveId(curveId));
	}

	public List<Quote> getQuotesByName(String quoteName) {
		return SecurityUtil.run(() -> quoteService.getQuotesByName(quoteName));
	}

	public List<QuoteValue> getQuoteValuesByQuoteSetIdQuoteNameAndDate(long quoteSetId, String quoteName,
			LocalDate value) {
		return SecurityUtil
				.run(() -> quoteService.getQuoteValuesByQuoteSetIdQuoteNameAndDate(quoteSetId, quoteName, value));
	}

	public QuoteValue getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(long quoteSetId, String quoteName,
			QuoteType quoteType, LocalDate value) {
		return SecurityUtil.run(() -> quoteService.getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(quoteSetId, quoteName,
				quoteType, value));
	}

	public Set<QuoteValue> getQuoteValueByQuoteSetIdQuoteNameTypeAndDates(long quoteSetId, String quoteName,
			QuoteType quoteType, LocalDate startDate, LocalDate endDate) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (quoteSetId <= 0) {
			errMsg.append(String.format(QUOTE_SET_ID_MUST_BE_POSITIVE));
		}
		if (quoteName == null) {
			errMsg.append(String.format(QUOTE_NAME_CANNOT_BE_NULL));
		} else {
			if (quoteName.isEmpty()) {
				errMsg.append(String.format(QUOTE_NAME_CANNOT_BE_EMPTY));
			}
		}
		if (quoteType == null) {
			errMsg.append(String.format("The quote type cannot be null.%n"));
		}
		if (startDate == null) {
			errMsg.append(String.format("The start date cannot be null.%n"));
		}
		if (endDate == null) {
			errMsg.append(String.format("The end date cannot be null.%n"));
		}
		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			errMsg.append(
					String.format("The start date (%tD) cannot be after the end date (%tD).%n", startDate, endDate));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.run(() -> quoteService.getQuoteValueByQuoteSetIdQuoteNameTypeAndDates(quoteSetId, quoteName,
				quoteType, startDate, endDate));
	}

	public Set<QuoteValue> getQuoteValuesByQuoteSetIdTypeDateAndQuoteNames(long quoteSetId, QuoteType quoteType,
			LocalDate date, String... quoteNames) {
		return SecurityUtil.run(() -> quoteService.getQuoteValuesByQuoteSetIdTypeDateAndQuoteNames(quoteSetId,
				quoteType, date, quoteNames));
	}

	public List<QuoteValue> getQuoteValuesByQuoteSetIdQuoteNameTypeAndDate(long quoteSetId, String quoteName,
			QuoteType quoteType, Year year, Month month) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (quoteSetId <= 0) {
			errMsg.append(String.format(QUOTE_SET_ID_MUST_BE_POSITIVE));
		}
		if (quoteName == null) {
			errMsg.append(String.format(QUOTE_NAME_CANNOT_BE_NULL));
		} else {
			if (quoteName.isEmpty()) {
				errMsg.append(String.format(QUOTE_NAME_CANNOT_BE_EMPTY));
			}
		}
		if (year == null) {
			errMsg.append(String.format("The year cannot be null.%n"));
		}
		if (month == null) {
			errMsg.append(String.format("The month cannot be null.%n"));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		return SecurityUtil.run(() -> quoteService.getQuoteValuesByQuoteSetIdQuoteNameTypeAndDate(quoteSetId, quoteName,
				quoteType, year, month));
	}

	public List<QuoteType> getQuoteTypesByQuoteName(String quoteName) {
		return SecurityUtil.run(() -> quoteService.getQuoteTypesByQuoteName(quoteName));
	}

	public List<Long> getQuoteIdsByNames(List<String> names) {
		return SecurityUtil.run(() -> quoteService.getQuoteIdsByNames(names));
	}

	public Set<QuoteSet> getAllQuoteSets() {
		return SecurityUtil.run(() -> quoteService.getAllQuoteSets());
	}

	public long saveQuoteSet(QuoteSet quoteSet) throws TradistaBusinessException {
		if (quoteSet == null) {
			throw new TradistaBusinessException("The Quote Set cannot be null.");
		}
		if (StringUtils.isEmpty(quoteSet.getName())) {
			throw new TradistaBusinessException("The quote set name must be specified.");
		}
		return SecurityUtil.runEx(() -> quoteService.saveQuoteSet(quoteSet));
	}

	public void deleteQuoteSet(long quoteSetId) throws TradistaBusinessException {
		if (quoteSetId <= 0) {
			throw new TradistaBusinessException(String.format(QUOTE_SET_ID_MUST_BE_POSITIVE));
		}
		SecurityUtil.runEx(() -> quoteService.deleteQuoteSet(quoteSetId));
	}

	public Quote getQuoteByNameAndType(String quoteName, QuoteType quoteType) {
		return SecurityUtil.run(() -> quoteService.getQuoteByNameAndType(quoteName, quoteType));
	}

	public boolean saveQuoteValues(long quoteSetId, List<QuoteValue> quoteValues) throws TradistaBusinessException {
		if (quoteSetId <= 0) {
			throw new TradistaBusinessException("Quote set id (" + quoteSetId + ") must be greater than 0.");
		}
		if (quoteValues == null) {
			throw new TradistaBusinessException("Quote values cannot be null.");
		}
		if (quoteValues.isEmpty()) {
			throw new TradistaBusinessException("Quote values cannot be empty.");
		}
		return SecurityUtil.run(() -> quoteService.saveQuoteValues(quoteSetId, quoteValues));
	}
}