package org.eclipse.tradista.core.processingorgdefaults.ui.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.processingorgdefaults.model.ProcessingOrgDefaults;
import org.eclipse.tradista.core.processingorgdefaults.service.ProcessingOrgDefaultsBusinessDelegate;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
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
public class ProcessingOrgDefaultsController implements Serializable {

	private static final long serialVersionUID = -265451284959975672L;

	private ProcessingOrgDefaults poDefaults;

	private ProcessingOrgDefaultsBusinessDelegate poDefaultsBusinessDelegate;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private SortedSet<LegalEntity> allPos;

	private LegalEntity selectedPo;

	private Map<String, String> moduleControllers;

	@PostConstruct
	public void init() {
		poDefaultsBusinessDelegate = new ProcessingOrgDefaultsBusinessDelegate();
		LegalEntity currentPo = ClientUtil.getCurrentUser().getProcessingOrg();
		long poId = currentPo != null ? currentPo.getId() : 0;
		if (poId > 0) {
			try {
				poDefaults = poDefaultsBusinessDelegate.getProcessingOrgDefaultsByPoId(poId);
			} catch (TradistaBusinessException tbe) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
			}
		} else {
			legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
			allPos = new TreeSet<>(legalEntityBusinessDelegate.getAllProcessingOrgs());
		}
		moduleControllers = new HashMap<>();
		Class<?> controllerClass = null;
		try {
			controllerClass = TradistaUtil.getClass(
					"org.eclipse.tradista.security.repo.ui.controller.ProcessingOrgDefaultsCollateralManagementController");
		} catch (TradistaTechnicalException tte) {
			// TODO Add log info
		}
		if (controllerClass != null) {
			moduleControllers.put("Collateral Management", "processingorgdefaultscollateralmanagement");
		}
	}

	public void save() {
		try {
			long poDefaultsId = poDefaultsBusinessDelegate.saveProcessingOrgDefaults(poDefaults);
			if (poDefaults.getId() == 0) {
				poDefaults.setId(poDefaultsId);
			}
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Processing Org Defaults successfully saved"));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public void load() {
		try {
			poDefaults = poDefaultsBusinessDelegate.getProcessingOrgDefaultsByPoId(selectedPo.getId());
			if (poDefaults != null) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
						"Processing Org  Defaults " + selectedPo.getShortName() + " successfully loaded."));
			}
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public ProcessingOrgDefaults getPoDefaults() {
		return poDefaults;
	}

	public void setPoDefaults(ProcessingOrgDefaults poDefaults) {
		this.poDefaults = poDefaults;
	}

	public Map<String, String> getModuleControllers() {
		return moduleControllers;
	}

	public void setModuleControllers(Map<String, String> moduleControllers) {
		this.moduleControllers = moduleControllers;
	}

	public String getProcessingOrg() {
		if (poDefaults != null) {
			return poDefaults.getProcessingOrg().getShortName();
		}
		return StringUtils.EMPTY;
	}

	public SortedSet<LegalEntity> getAllPos() {
		return allPos;
	}

	public void setAllPos(SortedSet<LegalEntity> allPos) {
		this.allPos = allPos;
	}

	public LegalEntity getSelectedPo() {
		return selectedPo;
	}

	public void setSelectedPo(LegalEntity selectedPo) {
		this.selectedPo = selectedPo;
	}

}