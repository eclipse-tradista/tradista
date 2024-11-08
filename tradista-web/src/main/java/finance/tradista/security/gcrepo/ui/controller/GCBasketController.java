package org.eclipse.tradista.security.gcrepo.ui.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.primefaces.model.DualListModel;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.common.service.SecurityBusinessDelegate;
import org.eclipse.tradista.security.gcrepo.model.GCBasket;
import org.eclipse.tradista.security.gcrepo.service.GCBasketBusinessDelegate;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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
public class GCBasketController implements Serializable {

	private static final long serialVersionUID = -6056004849327796923L;

	private GCBasket gcBasket;

	private String loadingCriterion;

	private String idOrName;

	private String copyGCBasketName;

	private String[] allLoadingCriteria = { "Id", "Name" };

	private DualListModel<Security> securities;

	private GCBasketBusinessDelegate gcBasketBusinessDelegate;

	private SecurityBusinessDelegate securityBusinessDelegate;

	private List<Security> availableSecurities;

	@PostConstruct
	public void init() {
		gcBasketBusinessDelegate = new GCBasketBusinessDelegate();
		securityBusinessDelegate = new SecurityBusinessDelegate();
		gcBasket = new GCBasket();
		availableSecurities = new ArrayList<>();
		Set<Security> allSecurities = securityBusinessDelegate.getAllSecurities();
		if (allSecurities != null) {
			availableSecurities.addAll(allSecurities);
		}
		initModel();
	}

	private void initModel() {
		securities = new DualListModel<>(availableSecurities, new ArrayList<>());
	}

	public String getLoadingCriterion() {
		return loadingCriterion;
	}

	public void setLoadingCriterion(String loadingCriterion) {
		this.loadingCriterion = loadingCriterion;
	}

	public GCBasket getGcBasket() {
		return gcBasket;
	}

	public void setGcBasket(GCBasket gcBasket) {
		this.gcBasket = gcBasket;
	}

	public String[] getAllLoadingCriteria() {
		return allLoadingCriteria;
	}

	public void setAllLoadingCriteria(String[] allLoadingCriteria) {
		this.allLoadingCriteria = allLoadingCriteria;
	}

	public DualListModel<Security> getSecurities() {
		return securities;
	}

	public void setSecurities(DualListModel<Security> securities) {
		this.securities = securities;
	}

	public String getIdOrName() {
		return idOrName;
	}

	public void setIdOrName(String idOrName) {
		this.idOrName = idOrName;
	}

	public String getCopyGCBasketName() {
		return copyGCBasketName;
	}

	public void setCopyGCBasketName(String copyGCBasketName) {
		this.copyGCBasketName = copyGCBasketName;
	}

	public void save() {
		try {
			Set<Security> secSet = null;
			if (securities.getTarget() != null && !securities.getTarget().isEmpty()) {
				secSet = new HashSet<>(securities.getTarget());
			}
			gcBasket.setSecurities(secSet);
			long gcBasketId = gcBasketBusinessDelegate.saveGCBasket(gcBasket);
			if (gcBasket.getId() == 0) {
				gcBasket.setId(gcBasketId);
			}
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
					"GC Basket " + gcBasket.getId() + " successfully saved"));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public void copy() {
		if (copyGCBasketName.equals(gcBasket.getName())) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
					"The name of the copied GC Basket must be different than the original one."));
			return;
		}
		try {
			GCBasket copyGCBasket = new GCBasket();
			copyGCBasket.setName(copyGCBasketName);
			Set<Security> secSet = null;
			if (securities.getTarget() != null && !securities.getTarget().isEmpty()) {
				secSet = new HashSet<>(securities.getTarget());
			}
			copyGCBasket.setSecurities(secSet);
			long gcBasketId = gcBasketBusinessDelegate.saveGCBasket(copyGCBasket);
			copyGCBasket.setId(gcBasketId);
			gcBasket.setId(copyGCBasket.getId());
			gcBasket.setName(copyGCBasket.getName());
			gcBasket.setSecurities(copyGCBasket.getSecurities());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
					"GC Basket " + gcBasket.getId() + " successfully created"));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		} finally {
			copyGCBasketName = null;
		}
	}

	public void load() {
		GCBasket gcBasket;
		try {
			if (loadingCriterion.equals("Id")) {
				gcBasket = gcBasketBusinessDelegate.getGCBasketById(Long.parseLong(idOrName));
			} else {
				gcBasket = gcBasketBusinessDelegate.getGCBasketByName(idOrName);
			}
			if (gcBasket != null) {
				this.gcBasket.setId(gcBasket.getId());
				this.gcBasket.setName(gcBasket.getName());
				this.gcBasket.setSecurities(gcBasket.getSecurities());
				List<Security> basketSecurities = new ArrayList<>();
				if (gcBasket.getSecurities() != null) {
					basketSecurities = new ArrayList<>(gcBasket.getSecurities());
					final List<Security> tmpBasketSecurities = basketSecurities;
					securities.setSource(
							securities.getSource().stream().filter(s -> !tmpBasketSecurities.contains(s)).toList());
				} else {
					securities.setSource(availableSecurities);
				}
				securities.setTarget(basketSecurities);
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
						"GC Basket " + gcBasket.getId() + " successfully loaded."));
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Error", "GC Basket " + idOrName + " was not found."));
			}

		} catch (NumberFormatException nfe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please type a valid id."));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}

	}

	public void clear() {
		gcBasket = new GCBasket();
		initModel();
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Form cleared"));
	}

}