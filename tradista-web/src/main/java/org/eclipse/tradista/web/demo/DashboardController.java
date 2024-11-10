package org.eclipse.tradista.web.demo;

import java.io.Serializable;

import org.primefaces.event.CloseEvent;
import org.primefaces.event.DashboardReorderEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.dashboard.DashboardModel;
import org.primefaces.model.dashboard.DashboardWidget;
import org.primefaces.model.dashboard.DefaultDashboardModel;
import org.primefaces.model.dashboard.DefaultDashboardWidget;

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
public class DashboardController implements Serializable {

	private static final long serialVersionUID = 2526660705463413881L;
	private DashboardModel model;
	private static final String LARGE_COLUMN_RESPONSIVE_CLASS = "col-12 lg:col-6 xl:col-8";

	private static final String SMALL_COLUMN_RESPONSIVE_CLASS = "col-12 lg:col-6 xl:col-4";

	@PostConstruct
	public void init() {
		model = new DefaultDashboardModel();
		DashboardWidget column1 = new DefaultDashboardWidget();
		DashboardWidget column2 = new DefaultDashboardWidget();

		column1.setStyleClass(SMALL_COLUMN_RESPONSIVE_CLASS);
		column1.addWidget("book");
		column1.addWidget("inventory");

		column2.setStyleClass(LARGE_COLUMN_RESPONSIVE_CLASS);
		column2.addWidget("tradeBooking");
		column2.addWidget("tradesList");

		model.addWidget(column1);
		model.addWidget(column2);
	}

	public void handleReorder(DashboardReorderEvent event) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_INFO);
		message.setSummary("Reordered: " + event.getWidgetId());
		message.setDetail("Item index: " + event.getItemIndex() + ", Column index: " + event.getColumnIndex()
				+ ", Sender index: " + event.getSenderColumnIndex());

		addMessage(message);
	}

	public void handleClose(CloseEvent event) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Panel Closed",
				"Closed panel id:'" + event.getComponent().getId() + "'");

		addMessage(message);
	}

	public void handleToggle(ToggleEvent event) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, event.getComponent().getId() + " toggled",
				"Status:" + event.getVisibility().name());

		addMessage(message);
	}

	private void addMessage(FacesMessage message) {
		FacesContext.getCurrentInstance().addMessage("msgs", message);
	}

	public DashboardModel getModel() {
		return model;
	}
}