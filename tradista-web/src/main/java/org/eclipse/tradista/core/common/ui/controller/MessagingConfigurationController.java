package org.eclipse.tradista.core.common.ui.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.messaging.model.MessagingEventSubscription;
import org.eclipse.tradista.core.common.messaging.model.MessagingFilter;
import org.eclipse.tradista.core.common.messaging.model.MessagingListener;
import org.eclipse.tradista.core.common.messaging.service.MessagingConfigurationBusinessDelegate;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.trade.messaging.TradeEvent;
import org.eclipse.tradista.core.transfer.messaging.CashTransferEvent;
import org.eclipse.tradista.core.transfer.messaging.ProductTransferEvent;
import org.primefaces.model.DualListModel;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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
public class MessagingConfigurationController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String RESTART_SERVER_MSG = "Configuration saved successfully. Please restart the Tradista servers to apply the changes to the messaging infrastructure.";

	private MessagingConfigurationBusinessDelegate delegate;

	// Listeners
	private List<MessagingListener> listeners;
	private String newListenerName;
	private long newListenerPollerDelay = 5000;
	private int newListenerPollerMaxMessages = 20;
	private boolean newListenerEnabled = true;

	// Filters
	private List<MessagingFilter> filters;
	private String newFilterName;
	private String newFilterClassName;

	// Filter assignment
	private MessagingListener filterAssignmentListener;
	private DualListModel<String> listenerFiltersPickList;

	// Subscriptions / Routing
	private List<String> eventTypes;
	private String selectedEventType;
	private DualListModel<String> subscriptionListenersPickList;

	@PostConstruct
	public void init() {
		delegate = new MessagingConfigurationBusinessDelegate();
		refreshData();

		// Event types
		eventTypes = new ArrayList<>();
		eventTypes.add(TradeEvent.TRADE);
		eventTypes.add(CashTransferEvent.CASH_TRANSFER);
		eventTypes.add(ProductTransferEvent.PRODUCT_TRANSFER);
		selectedEventType = TradeEvent.TRADE;

		initSubscriptionPickList();
		initFilterAssignmentPickList();
	}

	public void refreshData() {
		listeners = delegate.getAllListeners();
		if (listeners == null) {
			listeners = new ArrayList<>();
		}
		filters = delegate.getAllFilters();
		if (filters == null) {
			filters = new ArrayList<>();
		}
	}

	// --- LISTENERS ACTIONS ---

	public void addNewListener() {
		if (!ClientUtil.currentUserIsAdmin()) {
			addErrorMessage("Only administrator users are allowed to add listeners.");
			return;
		}
		try {
			if (StringUtils.isBlank(newListenerName)) {
				addErrorMessage("Listener name is mandatory.");
				return;
			}
			String queueName = newListenerName.trim() + "Queue";
			MessagingListener listener = new MessagingListener(newListenerName.trim(), queueName,
					newListenerPollerDelay, newListenerPollerMaxMessages, newListenerEnabled);
			delegate.saveListener(listener);
			refreshData();
			initSubscriptionPickList();
			initFilterAssignmentPickList();
			newListenerName = null;
			newListenerPollerDelay = 5000;
			newListenerPollerMaxMessages = 20;
			newListenerEnabled = true;
			addInfoMessage(RESTART_SERVER_MSG);
		} catch (TradistaBusinessException e) {
			addErrorMessage(e.getMessage());
		}
	}

	public void saveListeners() {
		if (!ClientUtil.currentUserIsAdmin()) {
			addErrorMessage("Only administrator users are allowed to update listeners.");
			return;
		}
		if (CollectionUtils.isEmpty(listeners)) {
			return;
		}
		try {
			for (MessagingListener l : listeners) {
				delegate.saveListener(l);
			}
			refreshData();
			addInfoMessage(RESTART_SERVER_MSG);
		} catch (TradistaBusinessException e) {
			addErrorMessage(e.getMessage());
		}
	}

	public void saveListener(MessagingListener listener) {
		if (!ClientUtil.currentUserIsAdmin()) {
			addErrorMessage("Only administrator users are allowed to update listeners.");
			return;
		}
		try {
			delegate.saveListener(listener);
			refreshData();
			addInfoMessage(RESTART_SERVER_MSG);
		} catch (TradistaBusinessException e) {
			addErrorMessage(e.getMessage());
		}
	}

	public void deleteListener(MessagingListener listener) {
		if (!ClientUtil.currentUserIsAdmin()) {
			addErrorMessage("Only administrator users are allowed to delete listeners.");
			return;
		}
		try {
			delegate.deleteListener(listener.getId());
			refreshData();
			initSubscriptionPickList();
			initFilterAssignmentPickList();
			addInfoMessage(RESTART_SERVER_MSG);
		} catch (TradistaBusinessException e) {
			addErrorMessage(e.getMessage());
		}
	}

	// --- FILTERS ACTIONS ---

	public void addNewFilter() {
		if (!ClientUtil.currentUserIsAdmin()) {
			addErrorMessage("Only administrator users are allowed to add filters.");
			return;
		}
		try {
			if (StringUtils.isBlank(newFilterName)) {
				addErrorMessage("Filter name is mandatory.");
				return;
			}
			if (StringUtils.isBlank(newFilterClassName)) {
				addErrorMessage("Filter class name is mandatory.");
				return;
			}
			MessagingFilter filter = new MessagingFilter(newFilterName.trim(), newFilterClassName.trim());
			delegate.saveFilter(filter);
			refreshData();
			initFilterAssignmentPickList();
			newFilterName = null;
			newFilterClassName = null;
			addInfoMessage(RESTART_SERVER_MSG);
		} catch (TradistaBusinessException e) {
			addErrorMessage(e.getMessage());
		}
	}

	public void deleteFilter(MessagingFilter filter) {
		if (!ClientUtil.currentUserIsAdmin()) {
			addErrorMessage("Only administrator users are allowed to delete filters.");
			return;
		}
		try {
			delegate.deleteFilter(filter.getId());
			refreshData();
			initFilterAssignmentPickList();
			addInfoMessage(RESTART_SERVER_MSG);
		} catch (TradistaBusinessException e) {
			addErrorMessage(e.getMessage());
		}
	}

	// --- FILTER ASSIGNMENT ACTIONS ---

	public void initFilterAssignmentPickList() {
		if (listeners != null && !listeners.isEmpty()) {
			if (filterAssignmentListener == null) {
				filterAssignmentListener = listeners.get(0);
			} else {
				// Re-sync with refreshed listeners
				filterAssignmentListener = listeners.stream()
						.filter(l -> l.getId() == filterAssignmentListener.getId()).findFirst()
						.orElse(listeners.get(0));
			}
			onFilterAssignmentListenerChange();
		} else {
			listenerFiltersPickList = new DualListModel<>(new ArrayList<>(), new ArrayList<>());
		}
	}

	public void onFilterAssignmentListenerChange() {
		if (filterAssignmentListener == null) {
			listenerFiltersPickList = new DualListModel<>(new ArrayList<>(), new ArrayList<>());
			return;
		}
		List<String> target = new ArrayList<>();
		if (filterAssignmentListener.getFilters() != null) {
			for (MessagingFilter f : filterAssignmentListener.getFilters()) {
				target.add(f.getName());
			}
		}
		List<String> source = new ArrayList<>();
		if (filters != null) {
			for (MessagingFilter f : filters) {
				if (!target.contains(f.getName())) {
					source.add(f.getName());
				}
			}
		}
		listenerFiltersPickList = new DualListModel<>(source, target);
	}

	public void saveListenerFilterAssignments() {
		if (!ClientUtil.currentUserIsAdmin()) {
			addErrorMessage("Only administrator users are allowed to update filter assignments.");
			return;
		}
		if (filterAssignmentListener == null) {
			return;
		}
		try {
			List<MessagingFilter> assigned = new ArrayList<>();
			for (String filterName : listenerFiltersPickList.getTarget()) {
				MessagingFilter f = filters.stream().filter(fi -> fi.getName().equals(filterName)).findFirst()
						.orElse(null);
				if (f != null) {
					assigned.add(f);
				}
			}
			filterAssignmentListener.setFilters(assigned);
			delegate.saveListener(filterAssignmentListener);
			refreshData();
			addInfoMessage(RESTART_SERVER_MSG);
		} catch (TradistaBusinessException e) {
			addErrorMessage(e.getMessage());
		}
	}

	// --- SUBSCRIPTION / ROUTING ACTIONS ---

	public void initSubscriptionPickList() {
		onEventTypeChange();
	}

	public void onEventTypeChange() {
		if (StringUtils.isBlank(selectedEventType)) {
			subscriptionListenersPickList = new DualListModel<>(new ArrayList<>(), new ArrayList<>());
			return;
		}
		List<MessagingEventSubscription> subs = null;
		try {
			subs = delegate.getSubscriptionsByEventType(selectedEventType);
		} catch (TradistaBusinessException e) {
			subs = new ArrayList<>();
		}
		List<String> target = new ArrayList<>();
		if (subs != null) {
			for (MessagingEventSubscription s : subs) {
				if (s.getListener() != null) {
					target.add(s.getListener().getName());
				}
			}
		}
		List<String> source = new ArrayList<>();
		if (listeners != null) {
			for (MessagingListener l : listeners) {
				if (!target.contains(l.getName())) {
					source.add(l.getName());
				}
			}
		}
		subscriptionListenersPickList = new DualListModel<>(source, target);
	}

	public void saveSubscriptions() {
		if (!ClientUtil.currentUserIsAdmin()) {
			addErrorMessage("Only administrator users are allowed to update event subscriptions.");
			return;
		}
		if (StringUtils.isBlank(selectedEventType)) {
			return;
		}
		try {
			Set<Long> listenerIds = new HashSet<>();
			for (String listenerName : subscriptionListenersPickList.getTarget()) {
				MessagingListener l = listeners.stream().filter(li -> li.getName().equals(listenerName)).findFirst()
						.orElse(null);
				if (l != null) {
					listenerIds.add(l.getId());
				}
			}
			delegate.saveSubscriptions(selectedEventType, listenerIds);
			refreshData();
			addInfoMessage(RESTART_SERVER_MSG);
		} catch (TradistaBusinessException e) {
			addErrorMessage(e.getMessage());
		}
	}

	// --- GETTERS & SETTERS ---

	public boolean isCurrentUserAdmin() {
		return ClientUtil.currentUserIsAdmin();
	}

	public List<MessagingListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<MessagingListener> listeners) {
		this.listeners = listeners;
	}

	public String getNewListenerName() {
		return newListenerName;
	}

	public void setNewListenerName(String newListenerName) {
		this.newListenerName = newListenerName;
	}

	public long getNewListenerPollerDelay() {
		return newListenerPollerDelay;
	}

	public void setNewListenerPollerDelay(long newListenerPollerDelay) {
		this.newListenerPollerDelay = newListenerPollerDelay;
	}

	public int getNewListenerPollerMaxMessages() {
		return newListenerPollerMaxMessages;
	}

	public void setNewListenerPollerMaxMessages(int newListenerPollerMaxMessages) {
		this.newListenerPollerMaxMessages = newListenerPollerMaxMessages;
	}

	public boolean isNewListenerEnabled() {
		return newListenerEnabled;
	}

	public void setNewListenerEnabled(boolean newListenerEnabled) {
		this.newListenerEnabled = newListenerEnabled;
	}

	public List<MessagingFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<MessagingFilter> filters) {
		this.filters = filters;
	}

	public String getNewFilterName() {
		return newFilterName;
	}

	public void setNewFilterName(String newFilterName) {
		this.newFilterName = newFilterName;
	}

	public String getNewFilterClassName() {
		return newFilterClassName;
	}

	public void setNewFilterClassName(String newFilterClassName) {
		this.newFilterClassName = newFilterClassName;
	}

	public long getFilterAssignmentListenerId() {
		return filterAssignmentListener != null ? filterAssignmentListener.getId() : 0;
	}

	public void setFilterAssignmentListenerId(long id) {
		if (listeners != null) {
			filterAssignmentListener = listeners.stream().filter(l -> l.getId() == id).findFirst().orElse(null);
		}
	}

	public MessagingListener getFilterAssignmentListener() {
		return filterAssignmentListener;
	}

	public void setFilterAssignmentListener(MessagingListener filterAssignmentListener) {
		this.filterAssignmentListener = filterAssignmentListener;
	}

	public DualListModel<String> getListenerFiltersPickList() {
		return listenerFiltersPickList;
	}

	public void setListenerFiltersPickList(DualListModel<String> listenerFiltersPickList) {
		this.listenerFiltersPickList = listenerFiltersPickList;
	}

	public List<String> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<String> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public String getSelectedEventType() {
		return selectedEventType;
	}

	public void setSelectedEventType(String selectedEventType) {
		this.selectedEventType = selectedEventType;
	}

	public DualListModel<String> getSubscriptionListenersPickList() {
		return subscriptionListenersPickList;
	}

	public void setSubscriptionListenersPickList(DualListModel<String> subscriptionListenersPickList) {
		this.subscriptionListenersPickList = subscriptionListenersPickList;
	}

	private void addInfoMessage(String msg) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", msg));
	}

	private void addErrorMessage(String msg) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
	}

}
