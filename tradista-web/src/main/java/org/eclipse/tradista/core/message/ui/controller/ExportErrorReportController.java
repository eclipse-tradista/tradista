package org.eclipse.tradista.core.message.ui.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.exporter.service.ExporterConfigurationBusinessDelegate;
import org.eclipse.tradista.core.message.model.ExportError;
import org.eclipse.tradista.core.message.model.ExportError.ExportErrorType;
import org.eclipse.tradista.core.message.service.ExportErrorBusinessDelegate;
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
public class ExportErrorReportController implements Serializable {

	private static final long serialVersionUID = 4922523467366860492L;

	private List<ExportError> errors;

	private List<String> selectedExporterTypes;

	private Set<String> allExporterTypes;

	private List<String> selectedExporterNames;

	private Set<String> allExporterNames;

	private Long messageId;

	private LocalDate[] solvingDates;

	private LocalDate[] errorDates;

	private List<Status> statuses;

	private Status[] allStatuses;

	private List<ExportErrorType> exportErrorTypes;

	private ExportErrorType[] allExportErrorTypes;

	private ExporterConfigurationBusinessDelegate exporterConfigurationBusinessDelegate;

	private ExportErrorBusinessDelegate exportErrorBusinessDelegate;

	private ExportError selectedError;

	@PostConstruct
	public void init() {
		allStatuses = Status.values();
		statuses = new ArrayList<>(List.of(Status.UNSOLVED));
		allExportErrorTypes = ExportErrorType.values();
		exporterConfigurationBusinessDelegate = new ExporterConfigurationBusinessDelegate();
		exportErrorBusinessDelegate = new ExportErrorBusinessDelegate();
		try {
			allExporterNames = exporterConfigurationBusinessDelegate.getAllExporterNames();
		} catch (TradistaTechnicalException _) {
		}
		try {
			allExporterTypes = exporterConfigurationBusinessDelegate.getModules();
		} catch (TradistaTechnicalException _) {
		}
	}

	/**
	 * Checks if the exporter app is available, if not, display a warning message.
	 * This check is a workaround, the target solution is to have the
	 * #getAllExporterNames and #getModules services in the core app, so they are
	 * not dependent on the availability of the exporter app
	 */
	public void onload() {
		try {
			exporterConfigurationBusinessDelegate.getAllExporterNames();
			exporterConfigurationBusinessDelegate.getModules();
		} catch (TradistaTechnicalException tte) {
			FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning",
					"Issue with the Exporter App: " + tte.getMessage()));
		}
	}

	public List<String> completeExporterType(String query) {
		String queryLowerCase = query.toLowerCase();
		return allExporterTypes.stream().filter(in -> in.toLowerCase().contains(queryLowerCase)).toList();
	}

	public List<String> completeExporterName(String query) {
		String queryLowerCase = query.toLowerCase();
		return allExporterNames.stream().filter(in -> in.toLowerCase().contains(queryLowerCase)).toList();
	}

	public List<ExportError> getErrors() {
		return errors;
	}

	public void setErrors(List<ExportError> errors) {
		this.errors = errors;
	}

	public Set<String> getAllExporterNames() {
		return allExporterNames;
	}

	public void setAllExporterNames(Set<String> allExporterNames) {
		this.allExporterNames = allExporterNames;
	}

	public List<String> getSelectedExporterNames() {
		return selectedExporterNames;
	}

	public void setSelectedExporterNames(List<String> selectedExporterNames) {
		this.selectedExporterNames = selectedExporterNames;
	}

	public List<String> getSelectedExporterTypes() {
		return selectedExporterTypes;
	}

	public void setSelectedExporterTypes(List<String> selectedExporterTypes) {
		this.selectedExporterTypes = selectedExporterTypes;
	}

	public Set<String> getAllExporterTypes() {
		return allExporterTypes;
	}

	public void setAllExporterTypes(Set<String> allExporterTypes) {
		this.allExporterTypes = allExporterTypes;
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
		ExportErrorType exportErrorType = null;
		Set<String> expTypes = null;
		Set<String> expNames = null;
		LocalDate errorDateFrom = errorDates != null ? errorDates[0] : null;
		LocalDate errorDateTo = errorDates != null ? errorDates[1] : null;
		LocalDate solvingDateFrom = solvingDates != null ? solvingDates[0] : null;
		LocalDate solvingDateTo = solvingDates != null ? solvingDates[1] : null;
		long msgId = messageId == null ? 0 : messageId;
		if (selectedExporterTypes != null) {
			expTypes = new HashSet<>(selectedExporterTypes);
		}
		if (selectedExporterNames != null) {
			expNames = new HashSet<>(selectedExporterNames);
		}
		if (!CollectionUtils.isEmpty(statuses) && statuses.size() == 1) {
			status = statuses.getFirst();
		}
		if (!CollectionUtils.isEmpty(exportErrorTypes) && exportErrorTypes.size() == 1) {
			exportErrorType = exportErrorTypes.getFirst();
		}
		try {
			errors = exportErrorBusinessDelegate.getExportErrors(expTypes, expNames, msgId, status, exportErrorType,
					errorDateFrom, errorDateTo, solvingDateFrom, solvingDateTo);
			if (errors != null) {
				errors = new ArrayList<>(errors);
			}
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage("msg",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public void tryToSolve() {
		try {
			exportErrorBusinessDelegate.tryToSolve(selectedError);
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
		if (!CollectionUtils.isEmpty(selectedExporterTypes) && !CollectionUtils.isEmpty(selectedExporterNames)) {
			selectedExporterNames.clear();
		}
	}

	public void onNameChange() {
		if (!CollectionUtils.isEmpty(selectedExporterNames) && !CollectionUtils.isEmpty(selectedExporterTypes)) {
			selectedExporterTypes.clear();
		}
	}

	public ExportError getSelectedError() {
		return selectedError;
	}

	public void setSelectedError(ExportError selectedError) {
		this.selectedError = selectedError;
	}

	public List<ExportErrorType> getExportErrorTypes() {
		return exportErrorTypes;
	}

	public void setExportErrorTypes(List<ExportErrorType> exportErrorTypes) {
		this.exportErrorTypes = exportErrorTypes;
	}

	public ExportErrorType[] getAllExportErrorTypes() {
		return allExportErrorTypes;
	}

	public void setAllExportErrorTypes(ExportErrorType[] allExportErrorTypes) {
		this.allExportErrorTypes = allExportErrorTypes;
	}

}