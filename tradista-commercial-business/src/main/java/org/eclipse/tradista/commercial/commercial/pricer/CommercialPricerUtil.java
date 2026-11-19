package org.eclipse.tradista.commercial.commercial.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.commercial.commercial.model.CommercialTrade.CommercialCashFlow;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.springframework.util.CollectionUtils;

public final class CommercialPricerUtil {

	private CommercialPricerUtil() {
	}

	public static BigDecimal npvDiscountedCashFlow(PricingParameter params, CommercialTrade trade, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {
		List<CashFlow> cashFlows = generateCashFlows(params, trade, pricingDate);
		if (cashFlows.isEmpty()) {
			return BigDecimal.ZERO;
		}
		BigDecimal npv = BigDecimal.ZERO;
		for (CashFlow cf : cashFlows) {
			if (cf.getDate() != null && !cf.getDate().isBefore(pricingDate)) {
				BigDecimal amount = cf.getDiscountedAmount() != null ? cf.getDiscountedAmount() : cf.getAmount();
				if (amount != null) {
					BigDecimal sign = cf.getDirection() == CashFlow.Direction.PAY ? BigDecimal.valueOf(-1)
							: BigDecimal.ONE;
					npv = npv.add(amount.multiply(sign));
				}
			}
		}
		if (currency != null && trade.getCurrency() != null && !currency.equals(trade.getCurrency())) {
			long quoteSetId = (params != null && params.getQuoteSet() != null) ? params.getQuoteSet().getId() : 0;
			long fxCurveId = 0;
			if (params != null && params.getFxCurves() != null) {
				FXCurve fxCurve = params.getFxCurves().get(new CurrencyPair(trade.getCurrency(), currency));
				if (fxCurve != null) {
					fxCurveId = fxCurve.getId();
				}
			}
			npv = PricerUtil.convertAmount(npv, trade.getCurrency(), currency, pricingDate, quoteSetId, fxCurveId);
		}
		return npv;
	}

	public static List<CashFlow> generateCashFlows(PricingParameter params, CommercialTrade trade,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {
		List<CashFlow> cashFlows = new ArrayList<>();
		if (trade == null || CollectionUtils.isEmpty(trade.getItems())) {
			return cashFlows;
		}
		LocalDate lastSettlementDate = null;
		for (CommercialCashFlow item : trade.getItems()) {
			if (item.getSettlementDate() != null
					&& (lastSettlementDate == null || item.getSettlementDate().isAfter(lastSettlementDate))) {
				lastSettlementDate = item.getSettlementDate();
			}
		}
		if (lastSettlementDate != null && pricingDate != null && pricingDate.isAfter(lastSettlementDate)) {
			throw new TradistaBusinessException(
					"When the pricing date is after the trade last settlement date, it is not possible to forecast cashflows.");
		}
		CashFlow.Direction direction = trade.isBuy() ? CashFlow.Direction.PAY : CashFlow.Direction.RECEIVE;
		for (CommercialCashFlow item : trade.getItems()) {
			if (item.getSettlementDate() == null
					|| (pricingDate != null && item.getSettlementDate().isBefore(pricingDate))) {
				continue;
			}
			CashFlow cf = new CashFlow();
			cf.setDate(item.getSettlementDate());
			cf.setAmount(item.getAmount());
			cf.setCurrency(trade.getCurrency());
			cf.setPurpose(TransferPurpose.CASH_SETTLEMENT);
			cf.setDirection(direction);
			cashFlows.add(cf);
		}
		if (!cashFlows.isEmpty() && params != null && params.getDiscountCurves() != null
				&& trade.getCurrency() != null) {
			InterestRateCurve discountCurve = params.getDiscountCurves().get(trade.getCurrency());
			if (discountCurve != null) {
				PricerUtil.discountCashFlows(cashFlows, pricingDate, discountCurve.getId(), null);
			}
		}
		return cashFlows;
	}

}
