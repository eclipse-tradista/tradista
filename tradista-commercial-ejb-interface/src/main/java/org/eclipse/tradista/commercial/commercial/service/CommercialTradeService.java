package org.eclipse.tradista.commercial.commercial.service;

import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;

import jakarta.ejb.Remote;

@Remote
public interface CommercialTradeService {

	long saveCommercialTrade(CommercialTrade trade, String action) throws TradistaBusinessException;

	CommercialTrade getCommercialTradeById(long id);

}
