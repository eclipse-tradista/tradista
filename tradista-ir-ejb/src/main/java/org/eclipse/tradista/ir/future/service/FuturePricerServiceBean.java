package org.eclipse.tradista.ir.future.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.inventory.model.ProductInventory;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.ir.future.model.Future;
import org.eclipse.tradista.ir.future.model.FutureTrade;
import org.eclipse.tradista.ir.future.service.FuturePricerService;
import org.eclipse.tradista.ir.future.service.FutureTradeService;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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
@Interceptors(FutureProductScopeFilteringInterceptor.class)
public class FuturePricerServiceBean implements FuturePricerService {

	@EJB
	private FutureTradeService futureTradeService;

	@Override
	public BigDecimal npvValuation(PricingParameter params, FutureTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getProduct().getMaturityDate())
				|| !pricingDate.isBefore(trade.getProduct().getMaturityDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}

		BigDecimal pv = pvValuation(params, trade, currency, pricingDate);

		BigDecimal convertedCost = PricerUtil.convertAmount(trade.getAmount().multiply(trade.getQuantity()),
				trade.getCurrency(), currency, pricingDate, params.getQuoteSet().getId(),
				paramFXCurve != null ? paramFXCurve.getId() : 0);

		BigDecimal npv;

		if (trade.isBuy()) {
			npv = pv.subtract(convertedCost);
		} else {
			npv = pv.add(convertedCost);
		}

		return npv;
	}

	@Override
	public BigDecimal pnlDefault(PricingParameter params, Future future, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		return realizedPnlDefault(params, future, book, currency, pricingDate)
				.add(unrealizedPnlDefault(params, future, book, currency, pricingDate));
	}

	@Override
	public BigDecimal realizedPnlDefault(PricingParameter params, Future future, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		long bookId = book != null ? book.getId() : 0;
		Set<ProductInventory> inventories = new ProductInventoryBusinessDelegate()
				.getInventoriesBeforeDateByProductAndBookIds(future.getId(), bookId, pricingDate);
		BigDecimal realizedPnl = BigDecimal.ZERO;
		// First, we add the payments of the future we owned before the pricing
		// date
		if (inventories != null && !inventories.isEmpty()) {
			for (ProductInventory inv : inventories) {
				LocalDate deliveryDate = future.getMaturityDate();
				if (!deliveryDate.isBefore(inv.getFrom())
						&& (!(inv.getTo() != null && deliveryDate.isAfter(inv.getTo())))) {
					if (!deliveryDate.isAfter(pricingDate)) {
						String quoteName = Future.FUTURE + "." + future.getContractSpecification().getName() + "."
								+ future.getSymbol();
						BigDecimal payment = inv.getQuantity()
								.multiply(future.getContractSpecification().getNotional()
										.divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN))
								.multiply(BigDecimal.valueOf(100)
										.subtract(future.getContractSpecification().getPriceVariationByBasisPoint()
												.divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN)
												.multiply(BigDecimal.valueOf(100)
														.subtract(PricerUtil.getValueAsOfDateFromQuote(quoteName,
																params.getQuoteSet().getId(), QuoteType.FUTURE_PRICE,
																QuoteValue.CLOSE, future.getMaturityDate())))));
						realizedPnl.add(payment);
					}
				}
			}
		}
		// then, we subtract the prices of the futures traded before the pricing
		// date
		List<FutureTrade> trades = futureTradeService.getFutureTradesBeforeTradeDateByFutureAndBookIds(pricingDate,
				future.getId(), bookId);
		if (trades != null && !trades.isEmpty()) {
			for (FutureTrade trade : trades) {
				if (trade.isBuy()) {
					realizedPnl.subtract(trade.getAmount().multiply(trade.getQuantity()));
				} else {
					realizedPnl.add(trade.getAmount().multiply(trade.getQuantity()));
				}
			}
		}
		return realizedPnl;
	}

	@Override
	public BigDecimal unrealizedPnlDefault(PricingParameter params, Future future, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		long bookId = book != null ? book.getId() : 0;
		Set<ProductInventory> inventories = new ProductInventoryBusinessDelegate()
				.getOpenPositionsFromInventoryByProductAndBookIds(future.getId(), bookId);
		BigDecimal unrealizedPnl = BigDecimal.ZERO;
		if (inventories != null && !inventories.isEmpty()) {
			// Create a virtual trade to use the pv measure
			FutureTrade trade = new FutureTrade();
			trade.setBuySell(true);
			trade.setProduct(future);
			trade.setQuantity(inventories.toArray(new ProductInventory[0])[0].getQuantity());
			unrealizedPnl.add(pvValuation(params, trade, currency, pricingDate));

		}
		return unrealizedPnl;
	}

	@Override
	public BigDecimal pvValuation(PricingParameter params, FutureTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getProduct().getMaturityDate())
				|| !pricingDate.isBefore(trade.getProduct().getMaturityDate())) {
			// TODO Log warn
			return BigDecimal.ZERO;
		}

		InterestRateCurve futureIRCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (futureIRCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getCurrency()));
		}

		InterestRateCurve indexCurve = params.getIndexCurves().get(trade.getReferenceRateIndex());
		if (indexCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getReferenceRateIndex()));
		}

		CurrencyPair pair = new CurrencyPair(trade.getCurrency(), currency);
		FXCurve paramFXCurve = params.getFxCurves().get(pair);
		if (paramFXCurve == null) {
			// TODO Add log warn
		}

		try {

			LocalDate endDate = DateUtil.addTenor(trade.getProduct().getMaturityDate(),
					trade.getReferenceRateIndexTenor());
			BigDecimal rateAsOfExpiryDate = PricerUtil.getForwardRate(indexCurve.getId(),
					trade.getProduct().getMaturityDate(), endDate, trade.getDayCountConvention());

			BigDecimal futurePayment = trade.getQuantity().multiply((trade.getProduct().getContractSpecification()
					.getNotional().divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN))
					.multiply(BigDecimal.valueOf(100)
							.subtract(trade.getProduct().getContractSpecification().getPriceVariationByBasisPoint()
									.divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN)
									.multiply(rateAsOfExpiryDate))));

			BigDecimal discountedFuturePayment = PricerUtil.discountAmount(futurePayment, futureIRCurve.getId(),
					pricingDate, trade.getProduct().getMaturityDate(), trade.getDayCountConvention());

			if (trade.isSell()) {
				discountedFuturePayment = discountedFuturePayment.negate();
			}

			discountedFuturePayment = PricerUtil.convertAmount(discountedFuturePayment, trade.getCurrency(), currency,
					pricingDate, params.getQuoteSet().getId(), paramFXCurve != null ? paramFXCurve.getId() : 0);

			return discountedFuturePayment;
		} catch (PricerException pe) {
			pe.printStackTrace();
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, FutureTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException {

		if (!LocalDate.now().isBefore(trade.getMaturityDate())) {
			throw new TradistaBusinessException(
					"When the trade maturity date has passed, it is not possible to forecast cashflows.");
		}

		if (!pricingDate.isBefore(trade.getMaturityDate())) {
			throw new TradistaBusinessException(
					"When the pricing date is not before the trade maturity date, it is not possible to forecast cashflows.");
		}

		if (!LocalDate.now().isBefore(trade.getSettlementDate())) {
			if (pricingDate.isBefore(LocalDate.now())) {
				throw new TradistaBusinessException(
						"When the trade settlement date has passed and the pricing date is in the past, it is not possible to forecast cashflows.");
			}
		}

		InterestRateCurve indexCurve = params.getIndexCurves().get(trade.getReferenceRateIndex());
		if (indexCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain an Index Curve for %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getReferenceRateIndex()));
		}

		CashFlow cashSettlementCf = new CashFlow();
		cashSettlementCf.setDate(trade.getMaturityDate());
		cashSettlementCf.setPurpose(TransferPurpose.CASH_SETTLEMENT);
		cashSettlementCf.setCurrency(trade.getCurrency());

		LocalDate endDate = DateUtil.addTenor(trade.getProduct().getMaturityDate(), trade.getReferenceRateIndexTenor());
		BigDecimal rateAsOfExpiryDate;
		try {
			rateAsOfExpiryDate = PricerUtil.getForwardRate(indexCurve.getId(), trade.getProduct().getMaturityDate(),
					endDate, trade.getDayCountConvention());
		} catch (PricerException pe) {
			throw new TradistaBusinessException(pe.getMessage());
		}
		BigDecimal settlementPrice = new BigDecimal("100").subtract(rateAsOfExpiryDate);
		BigDecimal difference = trade.getAmount().subtract(settlementPrice);
		BigDecimal amount = difference.abs().multiply(new BigDecimal("100"))
				.multiply(trade.getProduct().getContractSpecification().getPriceVariationByBasisPoint())
				.multiply(trade.getQuantity());

		cashSettlementCf.setAmount(amount);

		if (trade.isBuy()) {
			if (difference.signum() >= 0) {
				cashSettlementCf.setDirection(CashFlow.Direction.PAY);
			} else {
				cashSettlementCf.setDirection(CashFlow.Direction.RECEIVE);
			}
		} else {
			if (difference.signum() >= 0) {
				cashSettlementCf.setDirection(CashFlow.Direction.RECEIVE);
			} else {
				cashSettlementCf.setDirection(CashFlow.Direction.PAY);
			}
		}

		InterestRateCurve discountCurve = params.getDiscountCurves().get(trade.getCurrency());
		if (discountCurve == null) {
			throw new TradistaBusinessException(String.format(
					"%s Pricing Parameter doesn't contain a discount curve for currency %s. please add it or change the Pricing Parameter.",
					params.getName(), trade.getCurrency()));
		}

		List<CashFlow> cashFlows = new ArrayList<CashFlow>();

		if (discountCurve != null) {
			try {
				PricerUtil.discountCashFlow(cashSettlementCf, pricingDate, discountCurve.getId(), null);
			} catch (PricerException pe) {
				throw new TradistaBusinessException(pe.getMessage());
			}
		}

		cashFlows.add(cashSettlementCf);

		return cashFlows;
	}

}