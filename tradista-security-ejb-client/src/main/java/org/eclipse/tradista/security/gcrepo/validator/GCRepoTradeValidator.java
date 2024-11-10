package org.eclipse.tradista.security.gcrepo.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.repo.service.RepoTradeValidator;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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

public class GCRepoTradeValidator extends RepoTradeValidator {

	private static final long serialVersionUID = 8195327334102296257L;

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		GCRepoTrade gcRepoTrade = (GCRepoTrade) trade;
		StringBuilder errMsg = validateRepoTrade(trade);

		if (gcRepoTrade.getGcBasket() == null) {
			errMsg.append(String.format("The GC basket is mandatory.%n"));
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}