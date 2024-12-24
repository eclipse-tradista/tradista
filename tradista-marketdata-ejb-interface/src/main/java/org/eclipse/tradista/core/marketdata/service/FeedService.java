package org.eclipse.tradista.core.marketdata.service;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
import org.eclipse.tradista.core.marketdata.model.QuoteType;

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
public interface FeedService {

	Set<FeedConfig> getFeedConfigsByName(String name);

	FeedConfig getFeedConfigById(long id);

	Set<String> getAllFeedConfigNames();

	Set<FeedConfig> getAllFeedConfigs();

	long saveFeedConfig(FeedConfig feedConfig) throws TradistaBusinessException;

	boolean deleteFeedConfig(long id) throws TradistaBusinessException;

	Set<String> getFeedConfigsUsingQuote(String quoteName, QuoteType quoteType);

}
