package org.eclipse.tradista.core.common.persistence.util;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

public final class TradistaDBConstants {

	public static final String SELECT = "SELECT ";
	public static final String FROM = " FROM ";
	public static final String WHERE = " WHERE ";
	public static final String AND = " AND ";
	public static final String OR = " OR ";
	public static final String IN = " IN ";
	public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	public static final String MM_DD_YYYY = "MM/dd/yyyy";

	// Common field names.
	public static final String ID = "ID";
	public static final String TYPE = "TYPE";
	public static final String VALUE = "VALUE";
	public static final String STATUS = "STATUS";
	public static final String NAME = "NAME";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String STATUS_ID = "STATUS_ID";
	public static final String PROCESSING_ORG_ID = "PROCESSING_ORG_ID";
	public static final String BOOK_ID = "BOOK_ID";
	public static final String CREATION_DATE = "CREATION_DATE";
	public static final String LAST_UPDATE_TIME = "LAST_UPDATE_TIME";
	public static final String ERROR_ID = "ERROR_ID";
	public static final String DATE = "DATE";
	public static final String END_DATE = "END_DATE";
	public static final String INDEX_ID = "INDEX_ID";
	public static final String INDEX_TENOR = "INDEX_TENOR";
	public static final String INDEX_OFFSET = "INDEX_OFFSET";
	public static final String NOTICE_PERIOD = "NOTICE_PERIOD";
	public static final String AMOUNT = "AMOUNT";
	public static final String QUANTITY = "QUANTITY";
	public static final String CURRENCY_ID = "CURRENCY_ID";
	public static final String TRADE_ID = "TRADE_ID";
	public static final String PRODUCT_ID = "PRODUCT_ID";
	public static final String CREATION_DATETIME = "CREATION_DATETIME";
	public static final String PRICING_PARAMETER_ID = "PRICING_PARAMETER_ID";
	public static final String PRODUCT_TYPE = "PRODUCT_TYPE";
	public static final String COUNTERPARTY_ID = "COUNTERPARTY_ID";
	public static final String IS_REAL_TIME = "IS_REAL_TIME";
	public static final String QUOTE_SET_ID = "QUOTE_SET_ID";
	public static final String INTEREST_RATE_CURVE_ID = "INTEREST_RATE_CURVE_ID";
	public static final String PRIMARY_CURRENCY_ID = "PRIMARY_CURRENCY_ID";
	public static final String QUOTE_CURRENCY_ID = "QUOTE_CURRENCY_ID";
	public static final String FX_CURVE_ID = "FX_CURVE_ID";
	public static final String PRICER_NAME = "PRICER_NAME";
	public static final String QUOTE_ID = "QUOTE_ID";
	public static final String VOLATILITY = "VOLATILITY";
	public static final String VOLATILITY_SURFACE_ID = "VOLATILITY_SURFACE_ID";
	public static final String SURFACE_ID = "SURFACE_ID";

	private TradistaDBConstants() {
	}

}