package org.eclipse.tradista.core.mapping.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.mapping.model.Mapping;
import org.eclipse.tradista.core.mapping.model.MappingType;
import org.eclipse.tradista.core.mapping.service.MappingBusinessDelegate;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.eclipse.tradista.security.repo.model.AllocationConfiguration;
import org.primefaces.model.DualListModel;

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
public class ImporterMappingController implements Serializable {

	private static final long serialVersionUID = -5732691208687962353L;

	private Set<Mapping> importerMappings;

	private String importerNameloadingCriterion;
	
	private MappingType mappingTypeloadingCriterion;

	private MappingBusinessDelegate mappingBusinessDelegate;
	
	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private LegalEntity processingOrg;

	private LegalEntity copyProcessingOrg;

	private SortedSet<LegalEntity> allProcessingOrgs;

	@PostConstruct
	public void init() {
		mappingBusinessDelegate = new MappingBusinessDelegate();
		if (ClientUtil.currentUserIsAdmin()) {
			legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
			Set<LegalEntity> processingOrgs = legalEntityBusinessDelegate.getAllProcessingOrgs();
			allProcessingOrgs = new TreeSet<>();
			if (processingOrgs != null) {
				allProcessingOrgs.addAll(processingOrgs);
			}
		}
		initModel();
	}

	private void initModel() {
		//books = new DualListModel<>(availableBooks, new ArrayList<>());
	}

	public LegalEntity getProcessingOrg() {
		return processingOrg;
	}

	public void setProcessingOrg(LegalEntity processingOrg) {
		this.processingOrg = processingOrg;
	}

	public LegalEntity getCopyProcessingOrg() {
		return copyProcessingOrg;
	}

	public void setCopyProcessingOrg(LegalEntity copyProcessingOrg) {
		this.copyProcessingOrg = copyProcessingOrg;
	}

	public SortedSet<LegalEntity> getAllProcessingOrgs() {
		return allProcessingOrgs;
	}

	public void setAllProcessingOrgs(SortedSet<LegalEntity> allProcessingOrgs) {
		this.allProcessingOrgs = allProcessingOrgs;
	}

	public void save() {
		try {
			if (importerMappings == null) {
				LegalEntity po;
				if (ClientUtil.currentUserIsAdmin()) {
					po = processingOrg;
				} else {
					po = ClientUtil.getCurrentUser().getProcessingOrg();
				}
				importerMappings = new AllocationConfiguration(allocationConfigurationName, po);
			}
			allocationConfiguration.setBooks(bookSet);
			allocationConfiguration.setId(
					allocationConfigurationBusinessDelegate.saveAllocationConfiguration(allocationConfiguration));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
					"Allocation Configuration " + allocationConfiguration.getId() + " successfully saved"));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public void load() {
		AllocationConfiguration allocationConfiguration;
		try {
			if (loadingCriterion.equals("Id")) {
				allocationConfiguration = allocationConfigurationBusinessDelegate
						.getAllocationConfigurationById(Long.parseLong(idOrName));
			} else {
				allocationConfiguration = allocationConfigurationBusinessDelegate
						.getAllocationConfigurationByName(idOrName);
			}
			if (allocationConfiguration != null) {
				this.allocationConfiguration = allocationConfiguration;
				allocationConfigurationName = allocationConfiguration.getName();
				List<Book> allocConfigBooks = new ArrayList<>();
				if (allocationConfiguration.getBooks() != null) {
					allocConfigBooks = new ArrayList<>(allocationConfiguration.getBooks());
					final List<Book> tmpAllocConfigBooks = allocConfigBooks;
					books.setSource(books.getSource().stream().filter(s -> !tmpAllocConfigBooks.contains(s)).toList());
				} else {
					books.setSource(availableBooks);
				}
				books.setTarget(allocConfigBooks);
				processingOrg = allocationConfiguration.getProcessingOrg();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
						"Allocation Configuration " + allocationConfiguration.getId() + " successfully loaded."));
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Error", "Allocation Configuration " + idOrName + " was not found."));
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
		allocationConfiguration = null;
		allocationConfigurationName = null;
		processingOrg = null;
		initModel();
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Form cleared"));
	}

}