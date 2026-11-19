package org.eclipse.tradista.commercial.commercial.service;

import org.eclipse.tradista.commercial.commercial.messaging.CommercialTradeEvent;
import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.commercial.commercial.persistence.CommercialTradeSQL;
import org.eclipse.tradista.commercial.commercial.workflow.mapping.CommercialTradeMapper;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.messaging.service.LocalCoreMessagingService;
import org.eclipse.tradista.core.workflow.model.mapping.StatusMapper;
import org.jboss.ejb3.annotation.SecurityDomain;

import finance.tradista.flow.exception.TradistaFlowBusinessException;
import finance.tradista.flow.exception.TradistaFlowTechnicalException;
import finance.tradista.flow.model.Workflow;
import finance.tradista.flow.service.WorkflowManager;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class CommercialTradeServiceBean implements CommercialTradeService {

	@EJB
	private LocalCoreMessagingService messagingConfigurationService;

	@Override
	public long saveCommercialTrade(CommercialTrade trade, String action) throws TradistaBusinessException {
		CommercialTradeEvent event = new CommercialTradeEvent();
		if (trade.getId() != 0) {
			CommercialTrade oldTrade = CommercialTradeSQL.getTradeById(trade.getId());
			event.setOldTrade(oldTrade);
		}

		if (action != null && !action.isEmpty()) {
			try {
				Workflow<org.eclipse.tradista.commercial.commercial.workflow.mapping.CommercialTrade> workflow = WorkflowManager
						.getWorkflowByName(trade.getWorkflow());
				org.eclipse.tradista.commercial.commercial.workflow.mapping.CommercialTrade mappedTrade = CommercialTradeMapper
						.map(trade, workflow);
				mappedTrade = WorkflowManager.applyAction(mappedTrade, action);
				trade.setStatus(StatusMapper.map(mappedTrade.getStatus()));
			} catch (TradistaFlowBusinessException tfbe) {
				throw new TradistaBusinessException(tfbe.getMessage());
			} catch (TradistaFlowTechnicalException tfte) {
				throw new TradistaFlowTechnicalException(tfte.getMessage());
			}
		}

		event.setTrade(trade);
		long result = CommercialTradeSQL.saveTrade(trade);
		messagingConfigurationService.publishEvent(event);
		return result;
	}

	@Override
	public CommercialTrade getCommercialTradeById(long id) {
		return CommercialTradeSQL.getTradeById(id);
	}

}