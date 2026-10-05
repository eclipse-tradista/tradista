package org.eclipse.tradista.fx.test.util;

import java.lang.reflect.Field;

import org.eclipse.tradista.core.calendar.model.BlankCalendar;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.fx.fx.service.AbstractFXTradeBusinessDelegate;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public final class FXTestUtil {

	private FXTestUtil() {
	}

	/**
	 * Injects an in-memory stub for the static {@code fxExchange} field in
	 * {@link AbstractFXTradeBusinessDelegate} via reflection.
	 * <p>
	 * This prevents unit tests from performing remote EJB / database lookups via
	 * {@code ExchangeBusinessDelegate} when resolving the "FX" exchange and its
	 * calendar. The configured stub exchange uses a {@link BlankCalendar} so that
	 * all trade dates are treated as business days by default without requiring a
	 * running application server.
	 */
	public static void setupFXExchange() {
		try {
			Exchange exchange = new Exchange("FX");
			exchange.setCalendar(BlankCalendar.getInstance());
			Field fxExchangeField = AbstractFXTradeBusinessDelegate.class.getDeclaredField("fxExchange");
			fxExchangeField.setAccessible(true);
			fxExchangeField.set(null, exchange);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set up test FX Exchange", e);
		}
	}
}