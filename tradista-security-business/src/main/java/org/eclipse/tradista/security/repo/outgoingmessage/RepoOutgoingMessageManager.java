package org.eclipse.tradista.security.repo.outgoingmessage;

import org.eclipse.tradista.core.exporter.model.OutgoingMessageManager;
import org.eclipse.tradista.security.repo.model.RepoTrade;

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
 * 
 * @param <X>
 * @param <X>
 ********************************************************************************/

public interface RepoOutgoingMessageManager<X, Y extends RepoTrade> extends OutgoingMessageManager<X, Y> {

	@Override
	public default X createContent(X externalMessage, Y trade) {
		externalMessage = createMarginRate(externalMessage, trade);
		externalMessage = createRightOfSubstitution(externalMessage, trade);
		externalMessage = createRightOfReuse(externalMessage, trade);
		externalMessage = createCrossCurrencyCollateral(externalMessage, trade);
		externalMessage = createTerminableOnDemand(externalMessage, trade);
		externalMessage = createRepoRate(externalMessage, trade);
		if (trade.isTerminableOnDemand()) {
			externalMessage = createNoticePeriod(externalMessage, trade);
		}
		return externalMessage;
	}

	X createMarginRate(X externalMessage, Y trade);

	X createRightOfSubstitution(X externalMessage, Y trade);

	X createRightOfReuse(X externalMessage, Y trade);

	X createCrossCurrencyCollateral(X externalMessage, Y trade);

	X createTerminableOnDemand(X externalMessage, Y trade);

	X createRepoRate(X externalMessage, Y trade);

	X createNoticePeriod(X externalMessage, Y trade);

}