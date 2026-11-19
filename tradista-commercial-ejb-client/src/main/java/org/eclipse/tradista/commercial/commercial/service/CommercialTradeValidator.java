package org.eclipse.tradista.commercial.commercial.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.commercial.commercial.model.CommercialTrade.CommercialCashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.springframework.util.CollectionUtils;

public class CommercialTradeValidator extends DefaultTradeValidator {

	private static final long serialVersionUID = 1L;

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		StringBuilder errMsg = validateTradeBasics(trade);
		CommercialTrade commercialTrade = (CommercialTrade) trade;
		if (StringUtils.isBlank(commercialTrade.getDocumentReference())) {
			errMsg.append(String.format("The document reference is mandatory.%n"));
		}
		if (CollectionUtils.isEmpty(commercialTrade.getItems())) {
			errMsg.append(String.format("At least one commercial cash flow is required.%n"));
		} else {
			for (CommercialCashFlow item : commercialTrade.getItems()) {
				if (item.getSettlementDate() == null) {
					errMsg.append(String.format("Each commercial cash flow must have a settlement date.%n"));
				} else {
					if (commercialTrade.getTradeDate() != null
							&& commercialTrade.getTradeDate().isAfter(item.getSettlementDate())) {
						errMsg.append(String.format(
								"The trade date (%s) cannot be after the cash flow settlement date (%s).%n",
								commercialTrade.getTradeDate(), item.getSettlementDate()));
					}
					if (item.isSettled() && item.getSettlementDate().isAfter(LocalDate.now())) {
						errMsg.append(String.format("A future cash flow (%s) cannot be marked as settled.%n",
								item.getSettlementDate()));
					}
				}
				if (item.getAmount() == null || item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
					errMsg.append(String.format("Each commercial cash flow amount must be strictly positive.%n"));
				}
			}
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}
