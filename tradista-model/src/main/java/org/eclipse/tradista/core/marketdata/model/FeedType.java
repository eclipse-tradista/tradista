package org.eclipse.tradista.core.marketdata.model;

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

public enum FeedType {

	BLOOMBERG_BPIPE, BLOOMBERG_SERVER_API, QUANDL;

	private static final String BLOOMBERG_BPIPE_NAME = "BloombergBPipe";

	private static final String BLOOMBERG_SERVER_API_NAME = "BloombergServerAPI";

	private static final String QUANDL_NAME = "Quandl";

	public String toString() {
		switch (this) {
		case BLOOMBERG_BPIPE:
			return BLOOMBERG_BPIPE_NAME;
		case BLOOMBERG_SERVER_API:
			return BLOOMBERG_SERVER_API_NAME;
		case QUANDL:
			return QUANDL_NAME;
		}
		return super.toString();
	}

	/**
	 * Gets a QuoteType from a display name. Display names are used in GUIs. A
	 * display name of a QuoteType is the result of hits toString() method.
	 * 
	 * @param type
	 * @return
	 */
	public static FeedType getFeedType(String displayName) {
		switch (displayName) {
		case BLOOMBERG_BPIPE_NAME:
			return BLOOMBERG_BPIPE;
		case BLOOMBERG_SERVER_API_NAME:
			return BLOOMBERG_SERVER_API;
		case QUANDL_NAME:
			return QUANDL;
		}
		return null;
	}

}