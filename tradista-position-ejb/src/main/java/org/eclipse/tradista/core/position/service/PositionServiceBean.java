package org.eclipse.tradista.core.position.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.position.model.Position;
import org.eclipse.tradista.core.position.model.PositionCalculationError;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.persistence.PositionSQL;
import org.eclipse.tradista.core.pricing.pricer.Pricer;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.service.TradeBusinessDelegate;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
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
@Stateless
public class PositionServiceBean implements LocalPositionService, PositionService {

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	public void init() {
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	@EJB
	private PositionDefinitionService positionDefinitionService;

	@EJB
	private PositionCalculationErrorService positionCalculationErrorService;

	@EJB
	private PositionService positionService;

	@Override
	@Interceptors(PositionProductScopeFilteringInterceptor.class)
	public long savePosition(Position position) {
		return PositionSQL.savePosition(position);
	}

	@Override
	@Interceptors(PositionProductScopeFilteringInterceptor.class)
	public void savePositions(List<Position> positions) {
		PositionSQL.savePositions(positions);

	}

	@Interceptors(PositionFilteringInterceptor.class)
	@Override
	public List<Position> getPositionsByDefinitionIdAndValueDates(long positionDefinitionId, LocalDate valueDateFrom,
			LocalDate valueDateTo) throws TradistaBusinessException {
		return PositionSQL.getPositionsByDefinitionAndValueDates(positionDefinitionId, valueDateFrom, valueDateTo);
	}

	@Interceptors(PositionDefinitionProductScopeFilteringInterceptor.class)
	@Override
	public void calculatePosition(String positionDefinitionName, LocalDateTime valueDateTime)
			throws TradistaBusinessException {
		PositionDefinition posDef = positionDefinitionService.getPositionDefinitionByName(positionDefinitionName);
		if (posDef == null) {
			throw new TradistaBusinessException(
					String.format("The position definition named '%s' cannot be found.", positionDefinitionName));
		}
		ProductBusinessDelegate productBusinessDelegate = new ProductBusinessDelegate();
		PricerBusinessDelegate pricerBusinessDelegate = new PricerBusinessDelegate();
		List<PositionCalculationError> posErrors = new ArrayList<>();
		List<PositionCalculationError> existingErrors = positionCalculationErrorService.getPositionCalculationErrors(
				posDef.getId(), org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED, 0, 0,
				valueDateTime.toLocalDate(), valueDateTime.toLocalDate(), null, null, null, null);
		boolean canBeOTC = true;
		boolean canBeListed = true;
		BigDecimal averagePrice = null;
		BigDecimal quantity = null;
		try {
			if (posDef.getProductType() != null) {
				canBeOTC = productBusinessDelegate.canBeOTC(posDef.getProductType());
				canBeListed = productBusinessDelegate.canBeListed(posDef.getProductType());
			}
		} catch (TradistaBusinessException _) {
			// Should not happen at this stage as every position
			// definitions should have a product type.
		}

		BigDecimal positionPnl = BigDecimal.ZERO;
		BigDecimal positionRealizedPnl = BigDecimal.ZERO;
		BigDecimal positionUnrealizedPnl = BigDecimal.ZERO;

		posErrors.clear();

		if (canBeOTC) {
			// 2.1 retrieve the concerned trades
			Set<Trade<? extends Product>> trades = new TradeBusinessDelegate().getTrades(posDef);
			if (trades != null) {
				for (Trade<? extends Product> trade : trades) {
					// We want only OTC deals
					if (trade.getProduct() == null) {
						// 2.1.1 for each of the trade, calculate the PNL -
						// if
						// there is an
						// exception, put it in a list of error
						Pricer pricer = pricerBusinessDelegate.getPricer(trade.getProductType(),
								posDef.getPricingParameter());
						try {
							positionRealizedPnl = positionRealizedPnl
									.add(pricerBusinessDelegate.calculate(trade, pricer, posDef.getPricingParameter(),
											posDef.getCurrency(), valueDateTime.toLocalDate(), "REALIZED_PNL"));
							positionUnrealizedPnl = positionUnrealizedPnl
									.add(pricerBusinessDelegate.calculate(trade, pricer, posDef.getPricingParameter(),
											posDef.getCurrency(), valueDateTime.toLocalDate(), "UNREALIZED_PNL"));
							positionPnl = positionRealizedPnl.add(positionUnrealizedPnl);
						} catch (TradistaBusinessException tbe) {
							PositionCalculationError error = null;
							if (existingErrors != null && !existingErrors.isEmpty()) {
								// If there was already an error for this
								// position definition, trade and value
								// date, we update it.
								for (PositionCalculationError err : existingErrors) {
									if (err.getTrade().getId() == trade.getId()) {
										error = err;
										break;
									}
								}
								if (error == null) {
									// There were errors for this position and value
									// date but not for ths trade, we create a new
									// error
									error = new PositionCalculationError();
									error.setPositionDefinition(posDef);
									error.setValueDate(valueDateTime.toLocalDate());
									error.setTrade(trade);
								}
								error.setErrorMessage(tbe.getMessage());
								error.setErrorDate(LocalDateTime.now());
							} else {
								// New error
								error = new PositionCalculationError();
								error.setPositionDefinition(posDef);
								error.setErrorMessage(tbe.getMessage());
								error.setValueDate(valueDateTime.toLocalDate());
								error.setErrorDate(LocalDateTime.now());
								error.setTrade(trade);
							}
							posErrors.add(error);
						}
					}
				}
			}
		}

		if (posErrors.isEmpty()) {
			if (canBeListed) {
				// 3.1 retrieve the concerned products
				Set<? extends Product> products = null;
				averagePrice = BigDecimal.ZERO;
				quantity = BigDecimal.ZERO;
				ProductInventoryBusinessDelegate inventoryBusinessDelegate = new ProductInventoryBusinessDelegate();
				long bookId = posDef.getBook() != null ? posDef.getBook().getId() : 0;
				try {
					products = new ProductBusinessDelegate().getProducts(posDef);
				} catch (TradistaBusinessException tbe) {
					// no exception should appear at this stage as
					// posDef is not null.
				}
				if (products != null) {
					for (Product product : products) {
						Pricer pricer = pricerBusinessDelegate.getPricer(product.getProductType(),
								posDef.getPricingParameter());
						// 3.1.1 for each of the product, calculate the
						// PNL -
						// if
						// there is an
						// exception, put it in a list of error
						try {
							positionRealizedPnl = positionRealizedPnl.add(pricerBusinessDelegate.calculate(product,
									posDef.getBook(), pricer, posDef.getPricingParameter(), posDef.getCurrency(),
									valueDateTime.toLocalDate(), "REALIZED_PNL"));
							positionUnrealizedPnl = positionUnrealizedPnl.add(pricerBusinessDelegate.calculate(product,
									posDef.getBook(), pricer, posDef.getPricingParameter(), posDef.getCurrency(),
									valueDateTime.toLocalDate(), "UNREALIZED_PNL"));
							positionPnl = positionRealizedPnl.add(positionUnrealizedPnl);
							BigDecimal currentProductQuantity = inventoryBusinessDelegate
									.getQuantityByDateProductAndBookIds(product.getId(), bookId,
											valueDateTime.toLocalDate());
							if (currentProductQuantity != null
									&& currentProductQuantity.add(quantity).compareTo(BigDecimal.valueOf(0)) != 0) {
								BigDecimal currentProductAveragePrice = inventoryBusinessDelegate
										.getAveragePriceByDateProductAndBookIds(product.getId(), bookId,
												valueDateTime.toLocalDate());
								if (currentProductAveragePrice != null) {
									averagePrice = averagePrice.multiply(quantity)
											.add(currentProductAveragePrice.multiply(currentProductQuantity))
											.divide(quantity.add(currentProductQuantity),
													configurationBusinessDelegate.getRoundingMode());
								}
							}
							if (currentProductQuantity != null) {
								quantity = quantity.add(currentProductQuantity);
							}
						} catch (TradistaBusinessException tbe) {
							PositionCalculationError error = null;
							if (existingErrors != null && !existingErrors.isEmpty()) {
								// If there was already an error for
								// this
								// position definition, product and
								// value
								// date, we update it.
								for (PositionCalculationError err : existingErrors) {
									if (err.getProduct().getId() == product.getId()) {
										error = err;
										break;
									}
								}
								if (error == null) {
									// There were errors for this position and
									// value
									// date but not for this product, we create
									// a
									// new
									// error
									error = new PositionCalculationError();
									error.setPositionDefinition(posDef);
									error.setValueDate(valueDateTime.toLocalDate());
									error.setProduct(product);
								}
								error.setErrorMessage(tbe.getMessage());
								error.setErrorDate(LocalDateTime.now());
							} else {
								// New error
								error = new PositionCalculationError();
								error.setPositionDefinition(posDef);
								error.setErrorMessage(tbe.getMessage());
								error.setValueDate(valueDateTime.toLocalDate());
								error.setErrorDate(LocalDateTime.now());
								error.setProduct(product);
							}
							posErrors.add(error);
						}
					}
				}
			}
		}

		if (!posErrors.isEmpty()) {
			positionCalculationErrorService.savePositionCalculationErrors(posErrors);
		} else {
			// 2.2 if there is no exception, create a position in
			// the system
			Position position = new Position();
			position.setPnl(positionPnl);
			position.setRealizedPnl(positionRealizedPnl);
			position.setUnrealizedPnl(positionUnrealizedPnl);
			position.setPositionDefinition(posDef);
			position.setValueDateTime(valueDateTime);
			position.setAveragePrice(averagePrice);
			position.setQuantity(quantity);
			positionService.savePosition(position);
			positionCalculationErrorService.solvePositionCalculationError(posDef.getId(), LocalDate.now());
		}
	}

	@Override
	public Position getLastPositionByDefinitionNameAndValueDate(String positionDefinitionName, LocalDate valueDate) {
		return PositionSQL.getLastPositionByDefinitionNameAndValueDate(positionDefinitionName, valueDate);
	}

}