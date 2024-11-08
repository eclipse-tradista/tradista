package org.eclipse.tradista.core.position.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.position.model.Position;
import org.eclipse.tradista.core.position.model.PositionCalculationError;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.pricing.pricer.Pricer;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.service.TradeBusinessDelegate;

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
@Startup
@Singleton
public class PositionCalculator {

	private static ConfigurationBusinessDelegate configurationBusinessDelegate = new ConfigurationBusinessDelegate();

	@EJB
	private LocalConfigurationService configurationService;

	@EJB
	private LocalPositionDefinitionService positionDefinitionService;

	@EJB
	private LocalPositionService positionService;

	@EJB
	private LocalPositionCalculationErrorService positionCalculationErrorService;

	@PostConstruct
	private void init() {
		Timer timer = new Timer(true);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				calculate();
			}
		};
		timer.schedule(task, Calendar.getInstance().getTime(), configurationService.getFrequency() * 1000);
	}

	private void calculate() {
		// 1. Get the real time position definitions
		Set<PositionDefinition> allRealTimePositionDefinitions = positionDefinitionService
				.getAllRealTimePositionDefinitions();
		PricerBusinessDelegate pricerBusinessDelegate = new PricerBusinessDelegate();
		ProductBusinessDelegate productBusinessDelegate = new ProductBusinessDelegate();
		List<PositionCalculationError> errors = new ArrayList<PositionCalculationError>();
		List<Position> positions = new ArrayList<Position>();
		Set<Long> solved = new HashSet<Long>();
		if (allRealTimePositionDefinitions != null) {
			List<PositionCalculationError> posErrors = new ArrayList<PositionCalculationError>();
			List<PositionCalculationError> existingErrors = null;
			try {
				existingErrors = positionCalculationErrorService.getPositionCalculationErrors(0,
						org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED, 0, 0, LocalDate.now(), LocalDate.now(),
						null, null, null, null);
			} catch (TradistaBusinessException abe) {
				// Should not happen at this stage because dates are consistent.
			}

			LocalDate valueDate = LocalDate.now();

			for (PositionDefinition posDef : allRealTimePositionDefinitions) {
				boolean canBeOTC = true;
				boolean canBeListed = true;
				BigDecimal averagePrice = null;
				BigDecimal quantity = null;
				try {
					if (posDef.getProductType() != null) {
						if (!productBusinessDelegate.getAllProducts().contains(posDef.getProduct())) {
							addError(posErrors, existingErrors, valueDate, posDef, posDef.getProduct(),
									new TradistaBusinessException(String.format(
											"%s is not found among the allowed product types. Please contact your administrator.",
											posDef.getProductType())));
							continue;
						}
						canBeOTC = productBusinessDelegate.canBeOTC(posDef.getProductType());
						canBeListed = productBusinessDelegate.canBeListed(posDef.getProductType());
					}
				} catch (TradistaBusinessException tbe) {
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
								try {
									Pricer pricer = pricerBusinessDelegate.getPricer(trade.getProductType(),
											posDef.getPricingParameter());

									positionRealizedPnl = positionRealizedPnl.add(pricerBusinessDelegate.calculate(
											trade, pricer, posDef.getPricingParameter(), posDef.getCurrency(),
											valueDate, "REALIZED_PNL"));
									positionUnrealizedPnl = positionUnrealizedPnl.add(pricerBusinessDelegate.calculate(
											trade, pricer, posDef.getPricingParameter(), posDef.getCurrency(),
											valueDate, "UNREALIZED_PNL"));
									positionPnl = positionRealizedPnl.add(positionUnrealizedPnl);
								} catch (TradistaBusinessException tbe) {
									PositionCalculationError error = null;
									if (existingErrors != null && !existingErrors.isEmpty()) {
										// If there was already an error for this
										// position definition, trade and value
										// date, we update it.
										for (PositionCalculationError err : existingErrors) {
											if (err.getTrade().getId() == trade.getId()
													&& err.getPositionDefinition().getName().equals(posDef.getName())) {
												error = err;
												break;
											}
										}
										// If the position definition has
										// changed or if the trade is new, it
										// can be a new error
										if (error == null) {
											error = new PositionCalculationError();
											error.setPositionDefinition(posDef);
											error.setValueDate(valueDate);
											error.setTrade(trade);
										}
										error.setMessage(tbe.getMessage());
										error.setErrorDate(LocalDateTime.now());
									} else {
										// New error
										error = new PositionCalculationError();
										error.setPositionDefinition(posDef);
										error.setMessage(tbe.getMessage());
										error.setValueDate(valueDate);
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
						averagePrice = BigDecimal.ZERO;
						quantity = BigDecimal.ZERO;
						ProductInventoryBusinessDelegate inventoryBusinessDelegate = new ProductInventoryBusinessDelegate();
						long bookId = posDef.getBook() != null ? posDef.getBook().getId() : 0;
						// 3.1 retrieve the concerned products
						Set<? extends Product> products = null;
						try {
							products = new ProductBusinessDelegate().getProducts(posDef);
						} catch (TradistaBusinessException abe) {
							// no exception should appear at this stage as
							// posDef is not null.
						}
						if (products != null) {
							for (Product product : products) {
								// 3.1.1 for each of the product, calculate the
								// PNL -
								// if
								// there is an
								// exception, put it in a list of error
								try {
									Pricer pricer = pricerBusinessDelegate.getPricer(product.getProductType(),
											posDef.getPricingParameter());
									positionRealizedPnl = positionRealizedPnl.add(pricerBusinessDelegate.calculate(
											product, posDef.getBook(), pricer, posDef.getPricingParameter(),
											posDef.getCurrency(), valueDate, "REALIZED_PNL"));
									positionUnrealizedPnl = positionUnrealizedPnl.add(pricerBusinessDelegate.calculate(
											product, posDef.getBook(), pricer, posDef.getPricingParameter(),
											posDef.getCurrency(), valueDate, "UNREALIZED_PNL"));
									BigDecimal currentProductQuantity = inventoryBusinessDelegate
											.getQuantityByDateProductAndBookIds(product.getId(), bookId, valueDate);
									if (currentProductQuantity != null
											&& currentProductQuantity.add(quantity).compareTo(BigDecimal.ZERO) != 0) {
										BigDecimal currentProductAveragePrice = inventoryBusinessDelegate
												.getAveragePriceByDateProductAndBookIds(product.getId(),
														posDef.getBook().getId(), valueDate);
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
									positionPnl = positionRealizedPnl.add(positionUnrealizedPnl);
								} catch (TradistaBusinessException tbe) {
									addError(posErrors, existingErrors, valueDate, posDef, product, tbe);
								}
							}
						}
					}
				}

				if (!posErrors.isEmpty()) {
					errors.addAll(posErrors);
				} else {
					// 2.2 if there is no exception, create a position
					// in
					// the system
					solved.add(posDef.getId());
					Position position = new Position();
					position.setPnl(positionPnl);
					position.setRealizedPnl(positionRealizedPnl);
					position.setUnrealizedPnl(positionUnrealizedPnl);
					position.setPositionDefinition(posDef);
					position.setValueDateTime(LocalDateTime.now());
					position.setQuantity(quantity);
					position.setAveragePrice(averagePrice);
					positions.add(position);
				}
			}

			// Store the positions in DB
			if (!positions.isEmpty()) {
				positionService.savePositions(positions);
			}

			// Mark as Solved the previous position problems that were
			// solved

			if (!solved.isEmpty()) {
				positionCalculationErrorService.solvePositionCalculationError(solved, LocalDate.now());
			}

			// Finally, we save all the errors

			if (!errors.isEmpty()) {
				positionCalculationErrorService.savePositionCalculationErrors(errors);
			}

		}
	}

	private static void addError(List<PositionCalculationError> posErrors,
			List<PositionCalculationError> existingErrors, LocalDate valueDate, PositionDefinition posDef,
			Product product, TradistaBusinessException tbe) {
		PositionCalculationError error = null;
		if (existingErrors != null && !existingErrors.isEmpty()) {
			// If there was already an error for
			// this
			// position definition, product and
			// value
			// date, we update it.
			for (PositionCalculationError err : existingErrors) {
				if (err.getProduct().getId() == product.getId()
						&& err.getPositionDefinition().getName().equals(posDef.getName())) {
					error = err;
					break;
				}
			}
			// If the position definition has
			// changed or if the trade is new, it
			// can be a new error
			if (error == null) {
				error = new PositionCalculationError();
				error.setPositionDefinition(posDef);
				error.setValueDate(valueDate);
				error.setProduct(product);
			}
			error.setMessage(tbe.getMessage());
			error.setErrorDate(LocalDateTime.now());
		} else {
			// New error
			error = new PositionCalculationError();
			error.setPositionDefinition(posDef);
			error.setMessage(tbe.getMessage());
			error.setValueDate(valueDate);
			error.setErrorDate(LocalDateTime.now());
		}
		posErrors.add(error);
	}
}