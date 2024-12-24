package org.eclipse.tradista.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.service.MarketDataBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;

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

public class BloombergTestClient {

	private static Properties userProperties = new Properties();

	{
		InputStream in = getClass().getResourceAsStream("/user.properties");
		try {
			userProperties.load(in);
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		Set<QuoteSet> quoteSets = new HashSet<QuoteSet>();
		try {
			QuoteSet qs = new QuoteBusinessDelegate().getQuoteSetByName("Test");
			quoteSets.add(qs);
			new MarketDataBusinessDelegate().getMarketData(quoteSets);
		} catch (TradistaBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
