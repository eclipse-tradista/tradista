package org.eclipse.tradista.fix.exporter.filter;

import org.eclipse.tradista.core.common.messaging.EventFilter;
import org.eclipse.tradista.core.status.constants.StatusConstants;
import org.eclipse.tradista.core.trade.messaging.TradeEvent;

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

public class IsAllocatedTrade implements EventFilter<TradeEvent<?>> {

	@Override
	public boolean test(TradeEvent<?> event) {
		return event.getTrade() != null && event.getTrade().getStatus().getName().equals(StatusConstants.ALLOCATED);
	}
}
