package org.eclipse.tradista.core.marketdata.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Set;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;

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
public interface QuoteService {

	List<Quote> getAllQuotes();

	List<String> getAllQuoteNames();

	Set<QuoteSet> getAllQuoteSets();

	Quote getQuoteById(long quoteId);

	List<QuoteValue> getQuoteValuesByQuoteSetIdQuoteNameTypeAndDate(long quoteSetId, String quoteName,
			QuoteType quoteType, Year year, Month month);

	/**
	 * Delete a quote with given name and type. If the type is not specified, all
	 * the quotes with this name are deleted.
	 * 
	 * @param quoteName the name of the quote to delete
	 * @param quoteType the type of the quote to delete
	 * @return true if the quote was successfully deleted, false otherwise
	 * @throws TradistaBusinessException if a quote is already used by a feed
	 *                                   config, deletion is impossible.
	 */
	boolean deleteQuote(String quoteName, QuoteType quoteType) throws TradistaBusinessException;

	long saveQuote(Quote quote) throws TradistaBusinessException;

	List<Quote> getQuotesByCurveId(long curveId);

	List<Quote> getQuotesByName(String quoteName);

	/**
	 * Returns the Quote value linked to a quote name, type and a date.
	 * 
	 * @param quoteSetId the quote set id
	 * @param quoteName  the quote name
	 * @param quoteType  the quote type
	 * @param quoteDate  the quote date
	 * @return the Quote value linked to a quote name, type and a date.
	 */
	QuoteValue getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(long quoteSetId, String quoteName, QuoteType quoteTye,
			LocalDate quoteDate);

	/**
	 * Returns the Quote valued linked to a quote name and type in an interval of
	 * time.
	 * 
	 * @param quoteSetId the quote set id
	 * @param quoteName  the quote name
	 * @param quoteType  the quote type
	 * @param startDate  the start date
	 * @param endDate    the end date
	 * @return the Quote value linked to a quote name and type in an interval of
	 *         time.
	 */
	Set<QuoteValue> getQuoteValueByQuoteSetIdQuoteNameTypeAndDates(long quoteSetId, String quoteName,
			QuoteType quoteTye, LocalDate startDate, LocalDate endDate);

	boolean saveQuoteValues(long quoteSetId, String quoteName, QuoteType quoteType, List<QuoteValue> quoteValues,
			Year year, Month month);

	List<QuoteType> getQuoteTypesByQuoteName(String quoteName);

	List<Long> getQuoteIdsByNames(List<String> name);

	long saveQuoteSet(QuoteSet quoteSet) throws TradistaBusinessException;

	void deleteQuoteSet(long quoteSetId) throws TradistaBusinessException;

	Quote getQuoteByNameAndType(String quoteName, QuoteType quoteType);

	boolean saveQuoteValues(long quoteSetId, List<QuoteValue> quoteValues);

	QuoteSet getQuoteSetByName(String name);

	QuoteSet getQuoteSetById(long id);

	Set<QuoteValue> getQuoteValuesByQuoteSetIdTypeDateAndQuoteNames(long quoteSetId, QuoteType quoteType,
			LocalDate value, String... quoteNames);

	/**
	 * Returns the Quote values linked to a quote set, quote name and a date.
	 * Supports the Like operator.
	 * 
	 * @param quoteSetId the quote set id
	 * @param quoteName  the quote name. If it contains a '%', the link operator
	 *                   will be used
	 * @param quoteDate  the quote date
	 * @return the Quote values linked to a quote name and a date.
	 */
	List<QuoteValue> getQuoteValuesByQuoteSetIdQuoteNameAndDate(long quoteSetId, String quoteName, LocalDate quoteDate);

}