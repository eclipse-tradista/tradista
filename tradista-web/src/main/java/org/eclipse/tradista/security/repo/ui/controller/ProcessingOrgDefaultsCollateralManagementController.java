package org.eclipse.tradista.security.repo.ui.controller;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.tradista.core.marketdata.model.BlankQuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.security.repo.model.AllocationConfiguration;
import org.eclipse.tradista.security.repo.model.BlankAllocationConfiguration;
import org.eclipse.tradista.security.repo.service.AllocationConfigurationBusinessDelegate;

import jakarta.annotation.PostConstruct;
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
public class ProcessingOrgDefaultsCollateralManagementController implements Serializable {

	private static final long serialVersionUID = -2740894244286229939L;

	private SortedSet<QuoteSet> allQuoteSets;

	private SortedSet<AllocationConfiguration> allAllocationConfigurations;

	@PostConstruct
	public void init() {
		QuoteBusinessDelegate quoteBusinessDelegate = new QuoteBusinessDelegate();
		AllocationConfigurationBusinessDelegate allocationConfigurationBusinessDelegate = new AllocationConfigurationBusinessDelegate();
		Set<QuoteSet> allQs = quoteBusinessDelegate.getAllQuoteSets();
		allQuoteSets = new TreeSet<>();
		allQuoteSets.add(BlankQuoteSet.getInstance());
		if (allQs != null && !allQs.isEmpty()) {
			allQuoteSets.addAll(allQs);
		}
		Set<AllocationConfiguration> allAc = allocationConfigurationBusinessDelegate.getAllAllocationConfigurations();
		allAllocationConfigurations = new TreeSet<>();
		allAllocationConfigurations.add(BlankAllocationConfiguration.getInstance());
		if (allAc != null && !allAc.isEmpty()) {
			allAllocationConfigurations.addAll(allAc);
		}
	}

	public SortedSet<QuoteSet> getAllQuoteSets() {
		return allQuoteSets;
	}

	public void setAllQuoteSets(SortedSet<QuoteSet> quoteSets) {
		this.allQuoteSets = quoteSets;
	}

	public SortedSet<AllocationConfiguration> getAllAllocationConfigurations() {
		return allAllocationConfigurations;
	}

	public void setAllAllocationConfigurations(SortedSet<AllocationConfiguration> allAllocationConfigurations) {
		this.allAllocationConfigurations = allAllocationConfigurations;
	}

}