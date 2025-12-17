package org.eclipse.tradista.security.repo.validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.repo.model.RepoTrade;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public abstract class RepoTradeValidator extends DefaultTradeValidator {

	private static final long serialVersionUID = 8990209164088512400L;

	public StringBuilder validateRepoTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		RepoTrade repoTrade = (RepoTrade) trade;
		StringBuilder errMsg = validateTradeBasics(trade);

		// Other business controls
		if (trade.getAmount() != null && trade.getAmount().doubleValue() <= 0) {
			errMsg.append(String.format("The amount (%s) must be positive.%n", trade.getAmount().doubleValue()));
		}

		if (trade.getSettlementDate() == null) {
			errMsg.append(String.format("The start date is mandatory.%n"));
		} else {
			if (repoTrade.getEndDate() != null) {
				if (repoTrade.getEndDate().isBefore(trade.getSettlementDate())) {
					errMsg.append(String.format("The end date date (%s) cannot be before the start date (%s).%n",
							repoTrade.getEndDate(), trade.getSettlementDate()));
				}
			}
		}

		if (repoTrade.getMarginRate() == null) {
			errMsg.append(String.format("The margin rate is mandatory.%n"));
		} else if (repoTrade.getMarginRate().doubleValue() <= 0) {
			errMsg.append(
					String.format("The margin rate (%s) must be positive.%n", repoTrade.getMarginRate().doubleValue()));
		}

		if (repoTrade.isFixedRepoRate()) {
			if (repoTrade.getRepoRate() == null) {
				errMsg.append(String.format("When the Repo is a fixed rate one, the repo rate is mandatory.%n"));
			} else if (repoTrade.getRepoRate().doubleValue() <= 0) {
				errMsg.append(String.format("When the Repo is a fixed rate one, the repo rate must be positive.%n",
						repoTrade.getRepoRate().doubleValue()));
			}
		}

		if (repoTrade.isTerminableOnDemand()) {
			if (repoTrade.getNoticePeriod() < 0) {
				errMsg.append(String.format("The notice period (%s) must be positive.%n", repoTrade.getNoticePeriod()));
			}
		} else {
			if (repoTrade.getEndDate() == null) {
				errMsg.append(String.format("When the Repo is not terminable on demand, the end date is mandatory.%n"));
			}
		}

		// For the moment, only Repos support Workflows.
		if (repoTrade.getStatus() == null) {
			errMsg.append(String.format("The status is mandatory.%n"));
		}

		// surface checks of collateral to add
		if (repoTrade.getCollateralToAdd() != null && !repoTrade.getCollateralToAdd().isEmpty()) {
			for (Map.Entry<Security, Map<Book, BigDecimal>> entry : repoTrade.getCollateralToAdd().entrySet()) {
				if (entry.getKey() == null) {
					errMsg.append("One of the securities to add as collateral is nul, please check.%n");
					continue;
				}
				if (entry.getValue() == null) {
					errMsg.append(String.format(
							"Origin book and quantity of the security %s to add as collateral is null, please check.%n",
							entry.getKey()));
				} else {
					Map<Book, BigDecimal> bookMap = entry.getValue();
					for (Map.Entry<Book, BigDecimal> bookEntry : bookMap.entrySet()) {
						if (bookEntry.getKey() == null) {
							errMsg.append(String.format(
									"One origin book of the security %s to add as collateral is null, please check.%n",
									entry.getKey()));
							continue;
						}
						if (bookEntry.getValue() == null) {
							errMsg.append(String.format(
									"Quantity of the security %s with origin book %s to add as collateral is null, please check.%n",
									entry.getKey(), bookEntry.getKey()));
						}
					}

				}
			}
		}

		// surface checks of collateral to remove
		if (repoTrade.getCollateralToRemove() != null && !repoTrade.getCollateralToRemove().isEmpty()) {
			for (Map.Entry<Security, Map<Book, BigDecimal>> entry : repoTrade.getCollateralToRemove().entrySet()) {
				if (entry.getKey() == null) {
					errMsg.append("One of the securities to remove as collateral is null, please check.%n");
					continue;
				}
				if (entry.getValue() == null) {
					errMsg.append(String.format(
							"Book and quantity of the security %s to remove as collateral is null, please check.%n",
							entry.getKey()));
				} else {
					Map<Book, BigDecimal> bookMap = entry.getValue();
					for (Map.Entry<Book, BigDecimal> bookEntry : bookMap.entrySet()) {
						if (bookEntry.getKey() == null) {
							errMsg.append(String.format(
									"Book of the security %s to remove as collateral is null, please check.%n",
									entry.getKey()));
							continue;
						}
						if (bookEntry.getValue() == null) {
							errMsg.append(String.format(
									"Quantity of the security %s with book %s to remove as collateral is null, please check.%n",
									entry.getKey(), bookEntry.getKey()));
						}
					}

				}
			}
		}

		// surface checks of partial terminations values
		if (repoTrade.getPartialTerminations() != null && !repoTrade.getPartialTerminations().isEmpty()) {
			for (Map.Entry<LocalDate, BigDecimal> entry : repoTrade.getPartialTerminations().entrySet()) {
				if (entry.getKey() == null) {
					errMsg.append("One of the partial termination dates is null, please check.%n");
					continue;
				} else {
					if (entry.getKey().isBefore(repoTrade.getSettlementDate())
							|| entry.getKey().isAfter(repoTrade.getEndDate())) {
						errMsg.append(String.format(
								"One of the partial termination date (%tD) is not between trade settlement and end dates, please check.%n",
								entry.getKey()));
					}
				}
				if (entry.getValue() == null) {
					errMsg.append(
							String.format("Partial termination reduction amount is null for date %tD, please check.%n",
									entry.getKey()));
				} else {
					if (entry.getValue().signum() <= 0) {
						errMsg.append(String.format(
								"Partial termination amount (%s) for date %tD must be positive, please check.%n",
								entry.getValue(), entry.getKey()));
					}
				}
			}
		}

		return errMsg;

	}

}