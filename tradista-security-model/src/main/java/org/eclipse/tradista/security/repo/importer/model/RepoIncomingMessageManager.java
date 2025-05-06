package org.eclipse.tradista.security.repo.importer.model;

import org.eclipse.tradista.core.importer.model.IncomingMessageManager;
import org.eclipse.tradista.security.repo.model.RepoTrade;

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
 * 
 * @param <X>
 * @param <X>
 ********************************************************************************/

public interface RepoIncomingMessageManager<X, Y extends RepoTrade> extends IncomingMessageManager<X, Y> {

	@Override
	public default void checkMessage(X externalMessage, StringBuilder errMsg) {
		checkMarginRate(externalMessage, errMsg);
		checkRightOfSubstitution(externalMessage, errMsg);
		checkRightOfReuse(externalMessage, errMsg);
		checkCrossCurrencyCollateral(externalMessage, errMsg);
		if (extractTerminableOnDemand(externalMessage, errMsg)) {
			checkNoticePeriod(externalMessage, errMsg);
		}
	}

	void checkMarginRate(X externalMessage, StringBuilder errMsg);

	void checkRightOfSubstitution(X externalMessage, StringBuilder errMsg);

	void checkRightOfReuse(X externalMessage, StringBuilder errMsg);

	void checkCrossCurrencyCollateral(X externalMessage, StringBuilder errMsg);

	boolean extractTerminableOnDemand(X externalMessage, StringBuilder errMsg);

	void checkNoticePeriod(X externalMessage, StringBuilder errMsg);

}