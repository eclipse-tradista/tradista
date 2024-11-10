package org.eclipse.tradista.security.specificrepo.ui.controller;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.repo.ui.controller.PartialTermination;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;
import org.eclipse.tradista.security.specificrepo.service.SpecificRepoTradeBusinessDelegate;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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
public class SpecificRepoPartialTerminationController implements Serializable {

	private static final long serialVersionUID = 8370537524792266345L;

	private Set<PartialTermination> partialTerminations;

	private SpecificRepoTradeBusinessDelegate specificRepoTradeBusinessDelegate;

	@PostConstruct
	public void init() {
		specificRepoTradeBusinessDelegate = new SpecificRepoTradeBusinessDelegate();
	}

	public Set<PartialTermination> getPartialTerminations() {
		return partialTerminations;
	}

	public void setPartialTerminations(Set<PartialTermination> partialTerminations) {
		this.partialTerminations = partialTerminations;
	}

	public void refresh(long specialRepotradeId) {
		try {
			SpecificRepoTrade trade = specificRepoTradeBusinessDelegate.getSpecificRepoTradeById(specialRepotradeId);
			if (trade != null) {
				if (trade.getPartialTerminations() != null) {
					partialTerminations = trade.getPartialTerminations().entrySet().stream()
							.map(e -> new PartialTermination(e.getKey(), e.getValue())).collect(Collectors.toSet());
				} else {
					partialTerminations = null;
				}
			}
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public void clear() {
		partialTerminations = null;
	}

}