package org.eclipse.tradista.core.marketdata.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.persistence.QuoteSQL;
import org.eclipse.tradista.core.marketdata.persistence.QuoteSetSQL;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class QuoteServiceBean implements QuoteService {

	@Override
	public List<Quote> getAllQuotes() {
		return QuoteSQL.getAllQuotes();
	}

	@Override
	public List<String> getAllQuoteNames() {
		return QuoteSQL.getAllQuoteNames();
	}

	@Override
	public QuoteSet getQuoteSetByName(String name) {
		return QuoteSetSQL.getQuoteSetByName(name);
	}

	@Interceptors(QuoteSetFilteringInterceptor.class)
	@Override
	public QuoteSet getQuoteSetById(long id) {
		return QuoteSetSQL.getQuoteSetById(id);
	}

	@Interceptors(QuoteValueFilteringInterceptor.class)
	@Override
	public boolean saveQuoteValues(long quoteSetId, String quoteName, QuoteType quoteType, List<QuoteValue> quoteValues,
			Year year, Month month) {
		return QuoteSQL.saveQuoteValues(quoteSetId, quoteName, quoteType, quoteValues, year, month);
	}

	@Override
	public boolean deleteQuote(String quoteName, QuoteType quoteType) throws TradistaBusinessException {
		// 1. Check if the quote is used by a FeedConfig
		Set<String> feedConfigs = new FeedBusinessDelegate().getFeedConfigsUsingQuote(quoteName, quoteType);
		if (feedConfigs != null && !feedConfigs.isEmpty()) {
			StringBuilder buf = new StringBuilder();
			for (String conf : feedConfigs) {
				buf.append(conf);
				buf.append(" ");
			}
			String msg = "Impossible to delete the quote (" + quoteName + "/" + quoteType
					+ ") because it is used by the following FeedConfig(s): ";
			msg = msg + buf.toString();
			throw new TradistaBusinessException(msg);
		}
		return QuoteSQL.deleteQuote(quoteName, quoteType);
	}

	@Override
	public long saveQuote(Quote quote) throws TradistaBusinessException {
		if (quote.getId() == 0) {
			checkQuoteExistence(quote);
		} else {
			Quote oldQuote = getQuoteById(quote.getId());
			if (!oldQuote.getName().equals(quote.getName()) || !oldQuote.getType().equals(quote.getType())) {
				checkQuoteExistence(quote);
			}
		}
		return QuoteSQL.saveQuote(quote);
	}

	private void checkQuoteExistence(Quote quote) throws TradistaBusinessException {
		if (getQuoteByNameAndType(quote.getName(), quote.getType()) != null) {
			throw new TradistaBusinessException(
					String.format("A quote named %s and with type %s already exists in the system.", quote.getName(),
							quote.getType()));
		}
	}

	@Override
	public Quote getQuoteById(long quoteId) {
		return QuoteSQL.getQuoteById(quoteId);
	}

	@Override
	public List<Quote> getQuotesByCurveId(long curveId) {
		return QuoteSQL.getQuotesByCurveId(curveId);
	}

	@Override
	public List<Quote> getQuotesByName(String quoteName) {
		return QuoteSQL.getQuotesByName(quoteName);
	}

	@Override
	public List<QuoteValue> getQuoteValuesByQuoteSetIdQuoteNameAndDate(long quoteSetId, String quoteName,
			LocalDate value) {
		return QuoteSQL.getQuoteValuesByQuoteSetIdQuoteNameAndDate(quoteSetId, quoteName, value);
	}

	@Override
	public QuoteValue getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(long quoteSetId, String quoteName,
			QuoteType quoteType, LocalDate value) {
		return QuoteSQL.getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(quoteSetId, quoteName, quoteType, value);
	}

	@Override
	public Set<QuoteValue> getQuoteValueByQuoteSetIdQuoteNameTypeAndDates(long quoteSetId, String quoteName,
			QuoteType quoteType, LocalDate startDate, LocalDate endDate) {
		return QuoteSQL.getQuoteValueByQuoteSetIdQuoteNameTypeAndDates(quoteSetId, quoteName, quoteType, startDate,
				endDate);
	}

	@Override
	public Set<QuoteValue> getQuoteValuesByQuoteSetIdTypeDateAndQuoteNames(long quoteSetId, QuoteType quoteType,
			LocalDate date, String... quoteNames) {
		return QuoteSQL.getQuoteValuesByQuoteSetIdTypeDateAndQuoteNames(quoteSetId, quoteType, date, quoteNames);
	}

	@Interceptors(QuoteValueFilteringInterceptor.class)
	@Override
	public List<QuoteValue> getQuoteValuesByQuoteSetIdQuoteNameTypeAndDate(long quoteSetId, String quoteName,
			QuoteType quoteType, Year year, Month month) {
		return QuoteSQL.getQuoteValuesByQuoteSetIdQuoteNameTypeAndDate(quoteSetId, quoteName, quoteType, year, month);
	}

	@Override
	public List<QuoteType> getQuoteTypesByQuoteName(String quoteName) {
		return QuoteSQL.getQuoteTypesByQuoteName(quoteName);
	}

	@Override
	public List<Long> getQuoteIdsByNames(List<String> name) {
		List<Long> quoteIds = new ArrayList<>();
		for (String quoteName : name) {
			List<Quote> quotes = getQuotesByName(quoteName);
			for (Quote quote : quotes) {
				quoteIds.add(quote.getId());
			}
		}
		return quoteIds;
	}

	@Interceptors(QuoteSetFilteringInterceptor.class)
	@Override
	public Set<QuoteSet> getAllQuoteSets() {
		return QuoteSetSQL.getAllQuoteSets();
	}

	@Interceptors(QuoteSetFilteringInterceptor.class)
	@Override
	public long saveQuoteSet(QuoteSet quoteSet) throws TradistaBusinessException {
		if (quoteSet.getId() == 0) {
			checkQuoteSetExistence(quoteSet);
		} else {
			QuoteSet oldQuoteSet = QuoteSetSQL.getQuoteSetById(quoteSet.getId());
			if (!oldQuoteSet.getName().equals(quoteSet.getName())
					|| !oldQuoteSet.getProcessingOrg().equals(quoteSet.getProcessingOrg())) {
				checkQuoteSetExistence(quoteSet);
			}
		}
		return QuoteSetSQL.saveQuoteSet(quoteSet);
	}

	private void checkQuoteSetExistence(QuoteSet quoteSet) throws TradistaBusinessException {
		if (QuoteSetSQL.getQuoteSetByNameAndPo(quoteSet.getName(),
				quoteSet.getProcessingOrg() == null ? 0 : quoteSet.getProcessingOrg().getId()) != null) {
			String errMsg;
			if (quoteSet.getProcessingOrg() == null) {
				errMsg = "A global quote set named %s already exists in the system.";
			} else {
				errMsg = "A quote set named %s already exists in the system for the PO %s.";
			}
			throw new TradistaBusinessException(String.format(errMsg, quoteSet.getName(), quoteSet.getProcessingOrg()));
		}
	}

	@Interceptors(QuoteSetFilteringInterceptor.class)
	@Override
	public void deleteQuoteSet(long quoteSetId) throws TradistaBusinessException {
		Set<String> pricingParametersSetByQuoteSetId = new PricerBusinessDelegate()
				.getPricingParametersSetByQuoteSetId(quoteSetId);
		if (pricingParametersSetByQuoteSetId != null && !pricingParametersSetByQuoteSetId.isEmpty()) {
			throw new TradistaBusinessException(String.format(
					"Impossible to delete the Quote Set '%s', it is used by the following Pricing Parameters Set: %s",
					quoteSetId, Arrays.toString(pricingParametersSetByQuoteSetId.toArray())));
		}

		QuoteSetSQL.deleteQuoteSet(quoteSetId);
	}

	@Override
	public Quote getQuoteByNameAndType(String quoteName, QuoteType quoteType) {
		return QuoteSQL.getQuoteByNameAndType(quoteName, quoteType);
	}

	@Interceptors(QuoteSetFilteringInterceptor.class)
	@Override
	public boolean saveQuoteValues(long quoteSetId, List<QuoteValue> quoteValues) {
		return QuoteSQL.saveQuoteValues(quoteSetId, quoteValues);
	}
}