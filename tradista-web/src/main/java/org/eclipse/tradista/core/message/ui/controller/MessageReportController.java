package org.eclipse.tradista.core.message.ui.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.message.service.MessageBusinessDelegate;
import org.eclipse.tradista.core.workflow.model.Status;
import org.eclipse.tradista.core.workflow.service.WorkflowBusinessDelegate;
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

	private Set<String> allIInterfaceNames;
	
	private List<String> selectedObjectTypes;

	private Set<String> allIObjectTypes;

	private long messageId;

	private LocalDate[] creationDates;

	private LocalDate[] lastUpdateDates;

	private List<String> statuses;

	private Set<String> allStatuses;

	private MessageBusinessDelegate messageBusinessDelegate;
	
	private WorkflowBusinessDelegate workflowBusinessDelegate;

	@PostConstruct
	public void init() {
		messageBusinessDelegate = new MessageBusinessDelegate();
		workflowBusinessDelegate = new WorkflowBusinessDelegate();
		allStatuses = workflowBusinessDelegate.getAllMessageStatusNames();
		allTypes = messageBusinessDelegate.getAllMessageTypes();
	}

	public List<String> completeImporterType(String query) {
		String queryLowerCase = query.toLowerCase();
		return allImporterTypes.stream().filter(in -> in.toLowerCase().contains(queryLowerCase)).toList();
	}

	public List<String> completeImporterName(String query) {
		String queryLowerCase = query.toLowerCase();
		return allImporterNames.stream().filter(in -> in.toLowerCase().contains(queryLowerCase)).toList();
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

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public LocalDate[] getSolvingDates() {
		return creationDates;
	}

	public void setSolvingDates(LocalDate[] solvingDates) {
		this.creationDates = solvingDates;
	}

	public LocalDate[] getErrorDates() {
		return lastUpdateDates;
	}

	public void setErrorDates(LocalDate[] errorDates) {
		this.lastUpdateDates = errorDates;
	}

	public void load() {
		Status status = null;
		Set<String> impTypes = null;
		Set<String> impNames = null;
		LocalDate errorDateFrom = lastUpdateDates != null ? lastUpdateDates[0] : null;
		LocalDate errorDateTo = lastUpdateDates != null ? lastUpdateDates[1] : null;
		LocalDate solvingDateFrom = creationDates != null ? creationDates[0] : null;
		LocalDate solvingDateTo = creationDates != null ? creationDates[1] : null;
		if (selectedImporterTypes != null) {
			impTypes = new HashSet<>(selectedImporterTypes);
		}
		if (selectedImporterNames != null) {
			impNames = new HashSet<>(selectedImporterNames);
		}
		if (!CollectionUtils.isEmpty(statuses) && statuses.size() == 1) {
			status = statuses.getFirst();
		}
		try {
			errors = importErrorBusinessDelegate.getImportErrors(impTypes, impNames, messageId, status, errorDateFrom,
					errorDateTo, solvingDateFrom, solvingDateTo);
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage("msg",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public List<String> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<String> statuses) {
		this.statuses = statuses;
	}

	public Set<String> getAllStatuses() {
		return allStatuses;
	}

	public void setAllStatuses(Set<String> allStatuses) {
		this.allStatuses = allStatuses;
	}

	public void onTypeChange() {
		if (!CollectionUtils.isEmpty(selectedImporterTypes) && !CollectionUtils.isEmpty(selectedImporterNames)) {
			selectedImporterNames.clear();
		}
	}

	public void onNameChange() {
		if (!CollectionUtils.isEmpty(selectedImporterNames) && !CollectionUtils.isEmpty(selectedImporterTypes)) {
			selectedImporterTypes.clear();
		}
	}

}