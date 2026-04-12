package org.eclipse.tradista.core.common.messaging;

import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.trade.messaging.TradeEvent;
import org.springframework.integration.annotation.Router;
import org.springframework.stereotype.Component;

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

@Component
public class TradistaEventRouter {

	// Hard-coded subscriptions for easiness. We'll check later if making it
	// configurable is relevant.
	private static final Map<String, List<String>> SUBSCRIPTIONS = Map.of(TradeEvent.TRADE,
			List.of("tradeCaptureReportExporterQueue"));

	@Router(resolutionRequired = "true")
	public List<String> route(Event event) {
		// We get the queues subscribing to the event type
		return SUBSCRIPTIONS.get(event.getType());
	}
}