package org.eclipse.tradista.core.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.inventory.model.ProductInventory;
import org.eclipse.tradista.core.inventory.persistence.ProductInventorySQL;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryFilteringInterceptor;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryService;
import org.eclipse.tradista.core.transfer.model.ProductTransfer;
import org.eclipse.tradista.ir.future.model.Future;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

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

@SecurityDomain(value = "other")
@PermitAll
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Stateless
public class ProductInventoryServiceBean implements ProductInventoryService {

	private ProductBusinessDelegate productBusinessDelegate;

	@PostConstruct
	private void init() {
		productBusinessDelegate = new ProductBusinessDelegate();
	}

	@Override
	@Lock(LockType.WRITE)
	public void updateProductInventory(ProductTransfer transfer) throws TradistaBusinessException {
		boolean foundOpenQuantity = false;
		LocalDate inventoryDate = transfer.getSettlementDate();
		boolean foundSameInventory = false;
		boolean inventoryFound = false;
		LocalDate firstInventoryFromDateAfterTradeSettlementDate = null;
		ConfigurationBusinessDelegate configurationBusinessDelegate = new ConfigurationBusinessDelegate();
		Set<ProductInventory> inventoriesToBeSaved = new HashSet<>();
		// 1. Gets the last inventory before the trade date for this product and
		// book
		ProductInventory inventory = ProductInventorySQL.getLastProductInventoryBeforeDateByProductAndBookIds(
				transfer.getProduct().getId(), transfer.getBook().getId(), transfer.getSettlementDate());
		if (inventory != null) {
			inventoryFound = true;
			if (inventory.getFrom().equals(transfer.getSettlementDate())) {
				foundSameInventory = true;
			}
			if (inventory.getTo() == null) {
				foundOpenQuantity = true;
			} else {
				inventoryDate = inventory.getTo();
			}
			if (foundSameInventory) {
				if ((inventory.getQuantity().add(transfer.getQuantity())).signum() != 0) {
					inventory.setAveragePrice(inventory.getAveragePrice().multiply(inventory.getQuantity())
							.add(transfer.getTrade().getAmount().multiply(transfer.getQuantity()))
							.divide(inventory.getQuantity().add(transfer.getQuantity()),
									configurationBusinessDelegate.getScale(),
									configurationBusinessDelegate.getRoundingMode()));
				} else {
					inventory.setAveragePrice(BigDecimal.ZERO);
				}
				inventory.setQuantity(inventory.getQuantity().add(transfer.getQuantity()));
			} else {
				ProductInventory newInventory = new ProductInventory(inventory.getBook(), transfer.getSettlementDate(),
						inventory.getProduct());
				newInventory.setTo(inventory.getTo());
				if ((inventory.getQuantity().add(transfer.getQuantity())).signum() != 0) {
					newInventory.setAveragePrice(inventory.getAveragePrice().multiply(inventory.getQuantity())
							.add(transfer.getTrade().getAmount().multiply(transfer.getQuantity()))
							.divide(inventory.getQuantity().add(transfer.getQuantity()),
									configurationBusinessDelegate.getScale(),
									configurationBusinessDelegate.getRoundingMode()));
				} else {
					newInventory.setAveragePrice(BigDecimal.ZERO);
				}
				newInventory.setQuantity(inventory.getQuantity().add(transfer.getQuantity()));
				inventoriesToBeSaved.add(newInventory);

				inventory.setTo(transfer.getSettlementDate().minusDays(1));
			}
			inventoriesToBeSaved.add(inventory);
		}

		if (!foundOpenQuantity) {
			while (!foundOpenQuantity) {
				inventory = ProductInventorySQL.getFirstProductInventoryAfterDateByProductAndBookIds(
						transfer.getProduct().getId(), transfer.getBook().getId(), inventoryDate);
				if (inventory != null) {
					if (firstInventoryFromDateAfterTradeSettlementDate == null) {
						firstInventoryFromDateAfterTradeSettlementDate = inventory.getFrom();
					}
					if ((inventory.getQuantity().add(transfer.getQuantity())).signum() != 0) {
						inventory.setAveragePrice(inventory.getAveragePrice().multiply(inventory.getQuantity())
								.add(transfer.getTrade().getAmount().multiply(transfer.getQuantity()))
								.divide(inventory.getQuantity().add(transfer.getQuantity()),
										configurationBusinessDelegate.getScale(),
										configurationBusinessDelegate.getRoundingMode()));
					} else {
						inventory.setAveragePrice(BigDecimal.ZERO);
					}
					inventory.setQuantity(inventory.getQuantity().add(transfer.getQuantity()));
					if (inventory.getTo() == null) {
						foundOpenQuantity = true;
					} else {
						inventoryDate = inventory.getTo().plusDays(1);
					}
					inventoriesToBeSaved.add(inventory);
				} else {
					foundOpenQuantity = true;
				}
			}

		}

		if (!inventoryFound) {
			ProductInventory newInventory = new ProductInventory(transfer.getBook(), transfer.getSettlementDate(),
					transfer.getProduct());
			LocalDate to = null;
			if (firstInventoryFromDateAfterTradeSettlementDate != null) {
				to = firstInventoryFromDateAfterTradeSettlementDate.minusDays(1);
			}
			newInventory.setQuantity(transfer.getQuantity());
			newInventory.setAveragePrice(transfer.getTrade().getAmount());
			newInventory.setTo(to);
			inventoriesToBeSaved.add(newInventory);
		}

		ProductInventorySQL.save(inventoriesToBeSaved);

		fusionContiguousInventories(transfer.getProduct().getId(), transfer.getBook().getId());
	}

	private void fusionContiguousInventories(long productId, long bookId) {
		Map<LocalDate, ProductInventory> inventories = new HashMap<>();
		Set<ProductInventory> inventoriesByProductAndBookIds = ProductInventorySQL
				.getProductInventoriesByProductAndBookIds(productId, bookId);
		Set<ProductInventory> inventoriesToBeSaved = new HashSet<>();
		Set<Long> inventoryIdsToBeDeleted = new HashSet<>();

		if (inventoriesByProductAndBookIds != null && !inventoriesByProductAndBookIds.isEmpty()) {
			for (ProductInventory inv : inventoriesByProductAndBookIds) {
				inventories.put(inv.getTo(), inv);
			}

			for (ProductInventory inv : inventoriesByProductAndBookIds) {
				LocalDate key = inv.getFrom().minusDays(1);
				if (inventories.containsKey(key)
						&& inv.getQuantity().compareTo(inventories.get(key).getQuantity()) == 0) {
					ProductInventory invToBeUpdated = inventories.get(key);
					invToBeUpdated.setTo(inv.getTo());
					inventoriesToBeSaved.add(invToBeUpdated);
					inventoryIdsToBeDeleted.add(inv.getId());
				}
			}

			if (!inventoryIdsToBeDeleted.isEmpty()) {
				ProductInventorySQL.remove(inventoryIdsToBeDeleted);
			}

			if (!inventoriesToBeSaved.isEmpty()) {
				ProductInventorySQL.save(inventoriesToBeSaved);
			}
		}

	}

	@Override
	public Set<ProductInventory> getProductInventoriesBeforeDateByProductAndBookIds(long productId, long bookId,
			LocalDate date) {
		return ProductInventorySQL.getProductInventoriesBeforeDateByProductAndBookIds(productId, bookId, date);
	}

	@Override
	public Set<ProductInventory> getOpenPositionsFromProductInventoryByProductAndBookIds(long productId, long bookId) {
		return ProductInventorySQL.getOpenPositionsFromProductInventoryByProductAndBookIds(productId, bookId);
	}

	@Override
	public BigDecimal getQuantityByDateProductAndBookIds(long productId, long bookId, LocalDate date) {
		return ProductInventorySQL.getQuantityByDateProductAndBookIds(productId, bookId, date);
	}

	@Override
	public BigDecimal getAveragePriceByDateProductAndBookIds(long productId, long bookId, LocalDate date) {
		return ProductInventorySQL.getAveragePriceByDateProductAndBookIds(productId, bookId, date);
	}

	@Interceptors(ProductInventoryFilteringInterceptor.class)
	@Override
	public Set<ProductInventory> getProductInventories(LocalDate from, LocalDate to, String productType, long productId,
			long bookId, boolean onlyOpenPositions) throws TradistaBusinessException {
		return ProductInventorySQL.getProductInventories(from, to, productType, productId, bookId, onlyOpenPositions);
	}

	@Override
	public void closeExpiredProductsPositions() {
		// 1. Get all the open positions
		Set<ProductInventory> openPositions = ProductInventorySQL
				.getOpenPositionsFromProductInventoryByProductAndBookIds(0, 0);

		// 2. For each of them, check if the product is expired.
		if (openPositions != null && !openPositions.isEmpty()) {
			Set<ProductInventory> expiredPos = new HashSet<>();
			for (ProductInventory inv : openPositions) {
				Product prod = inv.getProduct();
				if (prod instanceof Equity equity) {
					if (equity.getActiveTo().isBefore(LocalDate.now())) {
						inv.setTo(LocalDate.now());
						expiredPos.add(inv);
					}
				}
				if (prod instanceof Bond bond) {
					if (bond.getMaturityDate().isBefore(LocalDate.now())) {
						inv.setTo(LocalDate.now());
						expiredPos.add(inv);
					}
				}
				if (prod instanceof EquityOption equityOption) {
					if (equityOption.getMaturityDate().isBefore(LocalDate.now())) {
						inv.setTo(LocalDate.now());
						expiredPos.add(inv);
					}
				}
				if (prod instanceof Future future) {
					if (future.getMaturityDate().isBefore(LocalDate.now())) {
						inv.setTo(LocalDate.now());
						expiredPos.add(inv);
					}
				}
			}
			// 3. Save the closed positions
			ProductInventorySQL.save(expiredPos);
		}
	}

	@Override
	public Map<String, BigDecimal> getBookProductContent(LocalDate date, long bookId) {
		Set<Product> products = productBusinessDelegate.getAllProducts();
		Map<String, BigDecimal> bookProductContent = null;
		if (products != null && !products.isEmpty()) {
			bookProductContent = HashMap.newHashMap(products.size());
			for (Product prod : products) {
				BigDecimal qty = ProductInventorySQL.getQuantityByDateProductAndBookIds(prod.getId(), bookId, date);
				if (qty.signum() != 0) {
					bookProductContent.put(prod.toString(), qty);
				}
			}
		}
		return bookProductContent;
	}

}