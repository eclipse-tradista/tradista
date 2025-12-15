package org.eclipse.tradista.core.message.ui.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.importer.service.ImporterConfigurationBusinessDelegate;
import org.eclipse.tradista.core.message.model.ImportError;
import org.eclipse.tradista.core.message.model.ImportError.ImportErrorType;
import org.eclipse.tradista.core.message.service.ImportErrorBusinessDelegate;
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
public class ImportErrorReportController implements Serializable {

	private static final long serialVersionUID = -4699413528097637586L;

	private List<ImportError> errors;

	private List<String> selectedImporterTypes;

	private Set<String> allImporterTypes;

	private List<String> selectedImporterNames;

	private Set<String> allImporterNames;

	private Long messageId;

	private LocalDate[] solvingDates;

	private LocalDate[] errorDates;

	private List<Status> statuses;

	private Status[] allStatuses;

	private List<ImportErrorType> importErrorTypes;

	private ImportErrorType[] allImportErrorTypes;

	private ImporterConfigurationBusinessDelegate importerConfigurationBusinessDelegate;

	private ImportErrorBusinessDelegate importErrorBusinessDelegate;

	private ImportError selectedError;

	@PostConstruct
	public void init() {
		allStatuses = Status.values();
		statuses = new ArrayList<>(List.of(Status.UNSOLVED));
		allImportErrorTypes = ImportErrorType.values();
		importerConfigurationBusinessDelegate = new ImporterConfigurationBusinessDelegate();
		importErrorBusinessDelegate = new ImportErrorBusinessDelegate();
		allImporterNames = importerConfigurationBusinessDelegate.getAllImporterNames();
		allImporterTypes = importerConfigurationBusinessDelegate.getModules();
	}

	public List<String> completeImporterType(String query) {
		String queryLowerCase = query.toLowerCase();
		return allImporterTypes.stream().filter(in -> in.toLowerCase().contains(queryLowerCase)).toList();
	}

	public List<String> completeImporterName(String query) {
		String queryLowerCase = query.toLowerCase();
		return allImporterNames.stream().filter(in -> in.toLowerCase().contains(queryLowerCase)).toList();
	}

	public List<ImportError> getErrors() {
		return errors;
	}

	public void setErrors(List<ImportError> errors) {
		this.errors = errors;
	}

	public Set<String> getAllImporterNames() {
		return allImporterNames;
	}

	public void setAllImporterNames(Set<String> allImporterNames) {
		this.allImporterNames = allImporterNames;
	}

	public List<String> getSelectedImporterNames() {
		return selectedImporterNames;
	}

	public void setSelectedImporterNames(List<String> selectedImporterNames) {
		this.selectedImporterNames = selectedImporterNames;
	}

	public List<String> getSelectedImporterTypes() {
		return selectedImporterTypes;
	}

	public void setSelectedImporterTypes(List<String> selectedImporterTypes) {
		this.selectedImporterTypes = selectedImporterTypes;
	}

	public Set<String> getAllImporterTypes() {
		return allImporterTypes;
	}

	public void setAllImporterTypes(Set<String> allImporterTypes) {
		this.allImporterTypes = allImporterTypes;
	}

	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public LocalDate[] getSolvingDates() {
		return solvingDates;
	}

	public void setSolvingDates(LocalDate[] solvingDates) {
		this.solvingDates = solvingDates;
	}

	public LocalDate[] getErrorDates() {
		return errorDates;
	}

	public void setErrorDates(LocalDate[] errorDates) {
		this.errorDates = errorDates;
	}

	public void load() {
		Status status = null;
		ImportErrorType importErrorType = null;
		Set<String> impTypes = null;
		Set<String> impNames = null;
		LocalDate errorDateFrom = errorDates != null ? errorDates[0] : null;
		LocalDate errorDateTo = errorDates != null ? errorDates[1] : null;
		LocalDate solvingDateFrom = solvingDates != null ? solvingDates[0] : null;
		LocalDate solvingDateTo = solvingDates != null ? solvingDates[1] : null;
		long msgId = messageId == null ? 0 : messageId;
		if (selectedImporterTypes != null) {
			impTypes = new HashSet<>(selectedImporterTypes);
		}
		if (selectedImporterNames != null) {
			impNames = new HashSet<>(selectedImporterNames);
		}
		if (!CollectionUtils.isEmpty(statuses) && statuses.size() == 1) {
			status = statuses.getFirst();
		}
		if (!CollectionUtils.isEmpty(importErrorTypes) && importErrorTypes.size() == 1) {
			importErrorType = importErrorTypes.getFirst();
		}
		try {
			errors = new ArrayList<>(importErrorBusinessDelegate.getImportErrors(impTypes, impNames, msgId, status,
					importErrorType, errorDateFrom, errorDateTo, solvingDateFrom, solvingDateTo));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage("msg",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public void tryToSolve() {
		try {
			importErrorBusinessDelegate.tryToSolve(selectedError);
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage("msg",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public List<Status> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<Status> statuses) {
		this.statuses = statuses;
	}

	public Status[] getAllStatuses() {
		return allStatuses;
	}

	public void setAllStatuses(Status[] allStatuses) {
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

	public ImportError getSelectedError() {
		return selectedError;
	}

	public void setSelectedError(ImportError selectedError) {
		this.selectedError = selectedError;
	}

	public List<ImportErrorType> getImportErrorTypes() {
		return importErrorTypes;
	}

	public void setImportErrorTypes(List<ImportErrorType> importErrorTypes) {
		this.importErrorTypes = importErrorTypes;
	}

	public ImportErrorType[] getAllImportErrorTypes() {
		return allImportErrorTypes;
	}

	public void setAllImportErrorTypes(ImportErrorType[] allImportErrorTypes) {
		this.allImportErrorTypes = allImportErrorTypes;
	}

}