package org.eclipse.tradista.core.common.ui.controller;

import java.io.Serializable;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import org.primefaces.model.menu.Submenu;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
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
@SessionScoped
public class MenuController implements Serializable {

	private static final long serialVersionUID = 6546524155625144194L;

	private MenuModel model;

	@PostConstruct
	public void init() {
		model = new DefaultMenuModel();

		Submenu tradeMenu = DefaultSubMenu.builder().label("Trades").build();
		Submenu productMenu = DefaultSubMenu.builder().label("Products").build();
		Submenu reportMenu = DefaultSubMenu.builder().label("Reports").build();
		Submenu configurationMenu = DefaultSubMenu.builder().label("Configuration").build();
		tradeMenu.getElements().add(DefaultMenuItem.builder().value("GC Repo").url("gcrepotrade.xhtml").build());
		tradeMenu.getElements()
				.add(DefaultMenuItem.builder().value("Specific Repo").url("specificrepotrade.xhtml").build());
		productMenu.getElements().add(DefaultMenuItem.builder().value("GC Basket").url("gcbasket.xhtml").build());
		reportMenu.getElements().add(DefaultMenuItem.builder().value("Import Errors").url("importerror.xhtml").build());
		reportMenu.getElements().add(DefaultMenuItem.builder().value("Messages").url("message.xhtml").build());
		configurationMenu.getElements().add(
				DefaultMenuItem.builder().value("Processing Org Defaults").url("processingorgdefaults.xhtml").build());
		configurationMenu.getElements().add(DefaultMenuItem.builder().value("Allocation Configuration")
				.url("allocationconfiguration.xhtml").build());
		configurationMenu.getElements()
				.add(DefaultMenuItem.builder().value("Importer Mapping").url("importermapping.xhtml").build());
		model.getElements().add(tradeMenu);
		model.getElements().add(productMenu);
		model.getElements().add(reportMenu);
		model.getElements().add(configurationMenu);
	}

	public MenuModel getModel() {
		return model;
	}

	public void setModel(MenuModel tradeModel) {
		this.model = tradeModel;
	}

	public String logout() {
		FacesContext context = FacesContext.getCurrentInstance();
		context.getExternalContext().invalidateSession();
		return "/login.xhtml?faces-redirect=true";
	}
}