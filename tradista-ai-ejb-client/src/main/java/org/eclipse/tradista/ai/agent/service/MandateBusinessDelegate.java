package org.eclipse.tradista.ai.agent.service;

import java.util.Map;

import org.eclipse.tradista.ai.agent.model.Mandate;
import org.eclipse.tradista.ai.agent.model.Mandate.Allocation;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;

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

public class MandateBusinessDelegate {

	private MandateService mandateService;

	public MandateBusinessDelegate() {
		mandateService = TradistaServiceLocator.getInstance().getMandateService();
	}

	public long saveMandate(Mandate mandate) throws TradistaBusinessException {
		if (mandate == null) {
			throw new TradistaBusinessException("The mandate is mandatory.");
		}
		StringBuilder errMsg = new StringBuilder();
		if (mandate.getAcceptedRiskLevel() == null) {
			errMsg.append(String.format("The accepted risk level is mandatory.%n"));
		}
		if (mandate.getBook() == null) {
			errMsg.append(String.format("The book is mandatory.%n"));
		}
		if (mandate.getCurrencyAllocations() == null) {
			errMsg.append(String.format("The currency allocation is mandatory.%n"));
		} else {
			checkAllocation(mandate.getCurrencyAllocations(), errMsg, "currency allocation");
		}
		if (mandate.getEndDate() == null) {
			errMsg.append(String.format("The end date is mandatory.%n"));
		} else if (mandate.getStartDate() == null) {
			errMsg.append(String.format("The start date is mandatory.%n"));
		} else {
			if (!mandate.getEndDate().isAfter(mandate.getStartDate())) {
				errMsg.append(String.format("The end date (%tD) must be after the start date (%tD).%n",
						mandate.getStartDate(), mandate.getEndDate()));
			}
		}
		if (mandate.getInitialCashAmount() == null) {
			errMsg.append(String.format("The initial cash amount is mandatory.%n"));
		}
		if (mandate.getInitialCashCurrency() == null) {
			errMsg.append(String.format("The initial cash currency is mandatory.%n"));
		}
		if (mandate.getName() == null) {
			errMsg.append(String.format("The name is mandatory.%n"));
		}
		if (mandate.getProductTypeAllocations() == null) {
			errMsg.append(String.format("The product type allocation is mandatory.%n"));
		} else {
			checkAllocation(mandate.getProductTypeAllocations(), errMsg, "product type allocation");
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		return SecurityUtil.runEx(() -> mandateService.saveMandate(mandate));

	}

	private void checkAllocation(Map<String, Allocation> allocations, StringBuilder errMsg, String allocationType) {
		if (allocations.isEmpty()) {
			errMsg.append(String.format("The %s is mandatory.%n", allocationType));
		}
		short globalMinAllocation = 0;
		for (Map.Entry<String, Mandate.Allocation> alloc : allocations.entrySet()) {
			if (alloc.getValue() == null) {
				errMsg.append(
						String.format("The %s allocation is specified but the allocation is null.%n", alloc.getKey()));
			} else {
				if (alloc.getValue().getMinAllocation() > 100 || alloc.getValue().getMinAllocation() < 0) {
					errMsg.append(String.format("The %s min allocation must be between 0 and 100, it is %d.%n",
							alloc.getKey(), alloc.getValue().getMinAllocation()));
				}
				if (alloc.getValue().getMaxAllocation() > 100 || alloc.getValue().getMaxAllocation() < 0) {
					errMsg.append(String.format("The %s min allocation must be between 0 and 100, it is %d.%n",
							alloc.getKey(), alloc.getValue().getMaxAllocation()));
				}
				if (alloc.getValue().getMinAllocation() > alloc.getValue().getMaxAllocation()) {
					errMsg.append(String.format(
							"The %s min allocation (%d) must be lower than its max allocation (%d).%n", alloc.getKey(),
							alloc.getValue().getMinAllocation(), alloc.getValue().getMaxAllocation()));
				}
				globalMinAllocation += alloc.getValue().getMinAllocation();
			}
		}
		if (globalMinAllocation > 100) {
			errMsg.append(String.format(
					"Regarding the %s, the sum of the min allocations cannot be greater than 100, it is %d.%n",
					allocationType, globalMinAllocation));
		}

	}

	public Mandate getMandateById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException(String.format("The id (%s) must be positive.", id));
		}
		return SecurityUtil.run(() -> mandateService.getMandateById(id));
	}
}
