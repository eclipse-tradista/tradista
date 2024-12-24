package org.eclipse.tradista.core.common.util;

import java.util.Properties;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;

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

public final class TradistaProperties {

	private TradistaProperties() {
	}

	private static String marketDataProvider;

	private static String tradistaAppServer;

	private static String tradistaAppPort;

	private static String tradistaAppProtocol;

	public static void load(Properties prop) throws TradistaBusinessException {
		if (prop == null) {
			throw new TradistaBusinessException("The properties cannot be null.");
		}
		marketDataProvider = prop.getProperty("marketDataProvider");
		tradistaAppServer = prop.getProperty("tradistaAppServer");
		tradistaAppPort = prop.getProperty("tradistaAppPort");
		tradistaAppProtocol = prop.getProperty("tradistaAppProtocol");
	}

	public static String getMarketDataProvider() {
		return marketDataProvider;
	}

	public static String getTradistaAppServer() {
		return tradistaAppServer;
	}

	public static String getTradistaAppPort() {
		return tradistaAppPort;
	}

	public static String getTradistaAppProtocol() {
		return tradistaAppProtocol;
	}

}