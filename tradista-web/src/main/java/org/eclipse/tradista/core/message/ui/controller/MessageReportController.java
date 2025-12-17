package org.eclipse.tradista.core.message.ui.controller;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.action.constants.ActionConstants;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.importer.service.ImporterConfigurationBusinessDelegate;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.message.service.MessageBusinessDelegate;
import org.eclipse.tradista.core.workflow.service.WorkflowBusinessDelegate;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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
public class MessageReportController implements Serializable {

	private static final long serialVersionUID = -4933473879177821174L;

	private List<Message> messages;

	private List<String> selectedTypes;

	private Set<String> allTypes;

	private List<String> selectedInterfaceNames;

	private Set<String> allInterfaceNames;

	private List<String> selectedObjectTypes;

	private Set<String> allObjectTypes;

	private List<String> selectedDirections;

	private Set<String> allDirections;

	private Long messageId;

	private Long objectId;

	private LocalDateTime[] creationDateTimes;

	private LocalDateTime[] lastUpdateDateTimes;

	private List<String> selectedStatuses;

	private Set<String> allStatuses;

	private MessageBusinessDelegate messageBusinessDelegate;

	private WorkflowBusinessDelegate workflowBusinessDelegate;

	private ImporterConfigurationBusinessDelegate importerConfigurationBusinessDelegate;

	private Message selectedMsg;

	private MenuModel contextMenuModel;

	@PostConstruct
	public void init() {
		messageBusinessDelegate = new MessageBusinessDelegate();
		workflowBusinessDelegate = new WorkflowBusinessDelegate();
		importerConfigurationBusinessDelegate = new ImporterConfigurationBusinessDelegate();
		allStatuses = workflowBusinessDelegate.getAllMessageStatusNames();
		allTypes = messageBusinessDelegate.getAllMessageTypes();
		allDirections = Set.of("Incoming", "Outgoing");
		allObjectTypes = messageBusinessDelegate.getAllObjectTypes();
		// For now, interfaces are only importers
		allInterfaceNames = importerConfigurationBusinessDelegate.getAllImporterNames();
	}

	public List<String> completeType(String query) {
		String queryLowerCase = query.toLowerCase();
		return allTypes.stream().filter(in -> in.toLowerCase().contains(queryLowerCase)).toList();
	}

	public List<String> completeInterfaceName(String query) {
		String queryLowerCase = query.toLowerCase();
		return allInterfaceNames.stream().filter(in -> in.toLowerCase().contains(queryLowerCase)).toList();
	}

	public List<String> completeObjectType(String query) {
		String queryLowerCase = query.toLowerCase();
		return allObjectTypes.stream().filter(in -> in.toLowerCase().contains(queryLowerCase)).toList();
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public List<String> getSelectedTypes() {
		return selectedTypes;
	}

	public void setSelectedTypes(List<String> selectedTypes) {
		this.selectedTypes = selectedTypes;
	}

	public List<String> getSelectedInterfaceNames() {
		return selectedInterfaceNames;
	}

	public void setSelectedInterfaceNames(List<String> selectedInterfaceNames) {
		this.selectedInterfaceNames = selectedInterfaceNames;
	}

	public List<String> getSelectedObjectTypes() {
		return selectedObjectTypes;
	}

	public void setSelectedObjectTypes(List<String> selectedObjectTypes) {
		this.selectedObjectTypes = selectedObjectTypes;
	}

	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public void load() {
		Set<String> statuses = null;
		Set<String> types = null;
		Set<String> interfaceNames = null;
		Set<String> objectTypes = null;
		Boolean direction = null;
		LocalDateTime lastUpdateDateFrom = lastUpdateDateTimes != null ? lastUpdateDateTimes[0] : null;
		LocalDateTime lastUpdateDateTo = lastUpdateDateTimes != null ? lastUpdateDateTimes[1] : null;
		LocalDateTime creationDateFrom = creationDateTimes != null ? creationDateTimes[0] : null;
		LocalDateTime creationDateTo = creationDateTimes != null ? creationDateTimes[1] : null;
		long msgId = messageId == null ? 0 : messageId.longValue();
		long objId = objectId == null ? 0 : objectId.longValue();
		if (selectedTypes != null) {
			types = new HashSet<>(selectedTypes);
		}
		if (selectedInterfaceNames != null) {
			interfaceNames = new HashSet<>(selectedInterfaceNames);
		}
		if (selectedObjectTypes != null) {
			objectTypes = new HashSet<>(selectedObjectTypes);
		}
		if (selectedStatuses != null) {
			statuses = new HashSet<>(selectedStatuses);
		}
		if (!CollectionUtils.isEmpty(selectedDirections) && selectedDirections.size() == 1) {
			direction = Boolean.valueOf(selectedDirections.getFirst().equals("Incoming"));
		}
		try {
			messages = new ArrayList<>(
					messageBusinessDelegate.getMessages(msgId, direction, types, interfaceNames, objId, objectTypes,
							statuses, lastUpdateDateFrom, lastUpdateDateTo, creationDateFrom, creationDateTo));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage("msg",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public Set<String> getAllStatuses() {
		return allStatuses;
	}

	public void setAllStatuses(Set<String> allStatuses) {
		this.allStatuses = allStatuses;
	}

	public void onTypeChange() {
		if (!CollectionUtils.isEmpty(selectedTypes) && !CollectionUtils.isEmpty(selectedInterfaceNames)) {
			selectedInterfaceNames.clear();
		}
		if (!CollectionUtils.isEmpty(selectedTypes) && !CollectionUtils.isEmpty(selectedObjectTypes)) {
			selectedObjectTypes.clear();
		}
	}

	public void onInterfaceNameChange() {
		if (!CollectionUtils.isEmpty(selectedInterfaceNames) && !CollectionUtils.isEmpty(selectedTypes)) {
			selectedTypes.clear();
		}
		if (!CollectionUtils.isEmpty(selectedInterfaceNames) && !CollectionUtils.isEmpty(selectedObjectTypes)) {
			selectedObjectTypes.clear();
		}
	}

	public void onObjectTypeChange() {
		if (!CollectionUtils.isEmpty(selectedObjectTypes) && !CollectionUtils.isEmpty(selectedInterfaceNames)) {
			selectedInterfaceNames.clear();
		}
		if (!CollectionUtils.isEmpty(selectedObjectTypes) && !CollectionUtils.isEmpty(selectedTypes)) {
			selectedTypes.clear();
		}
	}

	public void onContextMenuLoad() {
		contextMenuModel = new DefaultMenuModel();
		DefaultSubMenu subMenu = DefaultSubMenu.builder().label("Apply Action").build();

		Set<String> availableActions = null;
		try {
			availableActions = workflowBusinessDelegate.getAvailableActionsFromStatus(selectedMsg.getWorkflow(),
					selectedMsg.getStatus());
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage("msg",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}

		// For now, we hard code the authorized action to "RETRY" but the long term
		// solution is to have an authorization mechanism for workflow actions.
		if (!CollectionUtils.isEmpty(availableActions)) {
			subMenu.getElements().addAll(availableActions.stream().filter(ActionConstants.RETRY::equals).map(a -> {
				Map<String, List<String>> params = new HashMap<>();
				params.put("action", List.of(a));
				return DefaultMenuItem.builder().value(a).command("#{messageReportController.applyMsgAction()}")
						.params(params).build();
			}).toList());
		}
		contextMenuModel.getElements().add(subMenu);
	}

	public void applyMsgAction() {
		String action = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("action");
		try {
			messageBusinessDelegate.saveMessage(selectedMsg, action);
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			FacesContext.getCurrentInstance().addMessage("msg",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", te.getMessage()));
		}
	}

	public List<String> getSelectedDirections() {
		return selectedDirections;
	}

	public void setSelectedDirections(List<String> selectedDirections) {
		this.selectedDirections = selectedDirections;
	}

	public Set<String> getAllDirections() {
		return allDirections;
	}

	public void setAllDirections(Set<String> allDirections) {
		this.allDirections = allDirections;
	}

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}

	public List<String> getSelectedStatuses() {
		return selectedStatuses;
	}

	public void setSelectedStatuses(List<String> selectedStatuses) {
		this.selectedStatuses = selectedStatuses;
	}

	public Set<String> getAllTypes() {
		return allTypes;
	}

	public void setAllTypes(Set<String> allTypes) {
		this.allTypes = allTypes;
	}

	public Set<String> getAllInterfaceNames() {
		return allInterfaceNames;
	}

	public void setAllInterfaceNames(Set<String> allInterfaceNames) {
		this.allInterfaceNames = allInterfaceNames;
	}

	public Set<String> getAllObjectTypes() {
		return allObjectTypes;
	}

	public void setAllObjectTypes(Set<String> allObjectTypes) {
		this.allObjectTypes = allObjectTypes;
	}

	public LocalDateTime[] getCreationDateTimes() {
		return creationDateTimes;
	}

	public void setCreationDateTimes(LocalDateTime[] creationDateTimes) {
		this.creationDateTimes = creationDateTimes;
	}

	public LocalDateTime[] getLastUpdateDateTimes() {
		return lastUpdateDateTimes;
	}

	public void setLastUpdateDateTimes(LocalDateTime[] lastUpdateDateTimes) {
		this.lastUpdateDateTimes = lastUpdateDateTimes;
	}

	public Message getSelectedMsg() {
		return selectedMsg;
	}

	public void setSelectedMsg(Message selectedMsg) {
		this.selectedMsg = selectedMsg;
	}

	public MenuModel getContextMenuModel() {
		return contextMenuModel;
	}

	public void setMsgSubMenu(MenuModel contextMenuModel) {
		this.contextMenuModel = contextMenuModel;
	}

}