package org.eclipse.tradista.web.demo;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.service.TradeBusinessDelegate;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/********************************************************************************
 * Copyright (c) 2022 Olivier Asuncion
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

@Named
@ViewScoped
public class TradesListController implements Serializable {

	private static final long serialVersionUID = -4314139362201924213L;
	private List<EquityTrade> trades;
	private List<EquityTrade> filteredTrades;
	private PositionDefinition posDef;

	@PostConstruct
	public void init() {
		posDef = new PositionDefinition(StringUtils.EMPTY, ClientUtil.getCurrentUser().getProcessingOrg());
		posDef.setProductType(Equity.EQUITY);
		try {
			posDef.setBook(new BookBusinessDelegate().getBookByName("Demo Book"));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
		loadTrades();
	}

	public List<EquityTrade> getTrades() {
		return trades;
	}

	public List<EquityTrade> getFilteredTrades() {
		return filteredTrades;
	}

	public void setFilteredTrades(List<EquityTrade> filteredTrades) {
		this.filteredTrades = filteredTrades;
	}

	public void loadTrades() {
		Set<Trade<?>> tradesSet = new TradeBusinessDelegate().getTrades(posDef);
		if (tradesSet != null) {
			trades = tradesSet.stream().map(trade -> (EquityTrade) trade).collect(Collectors.toList());
		}
	}

}