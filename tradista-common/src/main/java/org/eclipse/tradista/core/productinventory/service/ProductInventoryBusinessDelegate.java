package org.eclipse.tradista.core.productinventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.inventory.model.ProductInventory;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryService;
import org.eclipse.tradista.core.transfer.model.ProductTransfer;

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

public class ProductInventoryBusinessDelegate {

	private ProductInventoryService productInventoryService;

	public ProductInventoryBusinessDelegate() {
		productInventoryService = TradistaServiceLocator.getInstance().getProductInventoryService();
	}

	public void updateProductInventory(ProductTransfer transfer) throws TradistaBusinessException {

		if (transfer == null) {
			throw new TradistaBusinessException("The CashTransfer cannot be null.");
		}

		StringBuffer errMsg = new StringBuffer();

		// TODO Should we validate the transfer here

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		SecurityUtil.runEx(() -> productInventoryService.updateProductInventory(transfer));
	}

	public Set<ProductInventory> getInventoriesBeforeDateByProductAndBookIds(long productId, long bookId,
			LocalDate date) throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null.");
		}

		return SecurityUtil.run(() -> productInventoryService
				.getProductInventoriesBeforeDateByProductAndBookIds(productId, bookId, date));
	}

	public Set<ProductInventory> getOpenPositionsFromInventoryByProductAndBookIds(long productId, long bookId)
			throws TradistaBusinessException {
		return SecurityUtil.run(() -> productInventoryService
				.getOpenPositionsFromProductInventoryByProductAndBookIds(productId, bookId));
	}

	public BigDecimal getQuantityByDateProductAndBookIds(long productId, long bookId, LocalDate date)
			throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date is mandatory.");
		}
		return SecurityUtil
				.run(() -> productInventoryService.getQuantityByDateProductAndBookIds(productId, bookId, date));
	}

	public BigDecimal getAveragePriceByDateProductAndBookIds(long productId, long bookId, LocalDate date)
			throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date is mandatory.");
		}
		return SecurityUtil
				.run(() -> productInventoryService.getAveragePriceByDateProductAndBookIds(productId, bookId, date));
	}

	public Set<ProductInventory> getProductInventories(LocalDate from, LocalDate to, String productType, long productId,
			long bookId, boolean onlyOpenPositions) throws TradistaBusinessException {
		StringBuffer errMsg = new StringBuffer();
		if (onlyOpenPositions && to != null) {
			errMsg.append(String.format("'Only Open positions' and 'To' cannot be selected together.%n"));
		}
		if (from != null && to != null) {
			if (to.isBefore(from)) {
				errMsg.append(String.format("'To' date cannot be before 'From' date.%n"));
			}
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> productInventoryService.getProductInventories(from, to, productType, productId,
				bookId, onlyOpenPositions));
	}

	public void closeExpiredProductsPositions() {
		SecurityUtil.run(() -> productInventoryService.closeExpiredProductsPositions());
	}

}