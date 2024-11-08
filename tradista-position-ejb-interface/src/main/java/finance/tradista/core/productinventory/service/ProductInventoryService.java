package org.eclipse.tradista.core.productinventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.inventory.model.ProductInventory;
import org.eclipse.tradista.core.transfer.model.ProductTransfer;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

@Remote
public interface ProductInventoryService {

	void updateProductInventory(ProductTransfer transfer) throws TradistaBusinessException;

	Set<ProductInventory> getProductInventoriesBeforeDateByProductAndBookIds(long productId, long bookId,
			LocalDate date);

	Set<ProductInventory> getOpenPositionsFromProductInventoryByProductAndBookIds(long productId, long bookId);

	BigDecimal getQuantityByDateProductAndBookIds(long productId, long bookId, LocalDate date);

	BigDecimal getAveragePriceByDateProductAndBookIds(long productId, long bookId, LocalDate date);

	Set<ProductInventory> getProductInventories(LocalDate from, LocalDate to, String productType, long productId,
			long bookId, boolean onlyOpenPositions) throws TradistaBusinessException;

	void closeExpiredProductsPositions();

	Map<String, BigDecimal> getBookProductContent(LocalDate date, long bookId);

}
