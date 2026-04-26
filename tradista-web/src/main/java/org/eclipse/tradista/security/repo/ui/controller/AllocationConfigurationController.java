package org.eclipse.tradista.security.repo.ui.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.eclipse.tradista.security.repo.model.AllocationConfiguration;
import org.eclipse.tradista.security.repo.service.AllocationConfigurationBusinessDelegate;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DualListModel;

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
public class AllocationConfigurationController implements Serializable {

	private static final long serialVersionUID = -1777343124539352925L;

	private AllocationConfiguration allocationConfiguration;

	private AllocationConfiguration loadAllocationConfiguration;

	private Set<AllocationConfiguration> allAllocationConfigurations;

	private String copyAllocationConfigurationName;

	private DualListModel<Book> books;

	private AllocationConfigurationBusinessDelegate allocationConfigurationBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private List<Book> availableBooks;

	private String allocationConfigurationName;

	private LegalEntity processingOrg;

	private LegalEntity copyProcessingOrg;

	private SortedSet<LegalEntity> allProcessingOrgs;

	@PostConstruct
	public void init() {
		allocationConfigurationBusinessDelegate = new AllocationConfigurationBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();
		if (ClientUtil.currentUserIsAdmin()) {
			legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
			Set<LegalEntity> processingOrgs = legalEntityBusinessDelegate.getAllProcessingOrgs();
			allProcessingOrgs = new TreeSet<>();
			if (processingOrgs != null) {
				allProcessingOrgs.addAll(processingOrgs);
			}
		}
		availableBooks = getBooksForCurrentContext();
		initModel();
		refresh();
	}

	private void refresh() {
		Set<AllocationConfiguration> allocConfigs = null;
		try {
			if (ClientUtil.getCurrentProcessingOrg() != null) {
				allocConfigs = allocationConfigurationBusinessDelegate
						.getAllocationConfigurationsByPoId(ClientUtil.getCurrentProcessingOrg().getId());
			} else {
				allocConfigs = allocationConfigurationBusinessDelegate.getAllAllocationConfigurations();
			}
		} catch (TradistaBusinessException _) {
			// ignore
		}
		if (allocConfigs != null) {
			allAllocationConfigurations = new HashSet<>(allocConfigs);
		} else {
			allAllocationConfigurations = new HashSet<>();
		}
	}

	private void initModel() {
		books = new DualListModel<>(availableBooks, new ArrayList<>());
	}

	/**
	 * Returns books filtered by the current PO context.
	 * If admin with a specific current PO selected: books of that PO only.
	 * Otherwise: all books.
	 */
	private List<Book> getBooksForCurrentContext() {
		Set<Book> allBooks = bookBusinessDelegate.getAllBooks();
		if (allBooks == null) {
			return new ArrayList<>();
		}
		LegalEntity currentPo = ClientUtil.getCurrentProcessingOrg();
		if (ClientUtil.currentUserIsAdmin() && currentPo != null) {
			return allBooks.stream()
					.filter(b -> b.getProcessingOrg().equals(currentPo))
					.collect(java.util.stream.Collectors.toList());
		}
		return new ArrayList<>(allBooks);
	}

	public AllocationConfiguration getAllocationConfiguration() {
		return allocationConfiguration;
	}

	public void setAllocationConfiguration(AllocationConfiguration allocationConfiguration) {
		this.allocationConfiguration = allocationConfiguration;
	}

	public DualListModel<Book> getBooks() {
		return books;
	}

	public void setBooks(DualListModel<Book> books) {
		this.books = books;
	}

	public AllocationConfiguration getLoadAllocationConfiguration() {
		return loadAllocationConfiguration;
	}

	public void setLoadAllocationConfiguration(AllocationConfiguration loadAllocationConfiguration) {
		this.loadAllocationConfiguration = loadAllocationConfiguration;
	}

	public Set<AllocationConfiguration> getAllAllocationConfigurations() {
		return allAllocationConfigurations;
	}

	public void setAllAllocationConfigurations(Set<AllocationConfiguration> allAllocationConfigurations) {
		this.allAllocationConfigurations = allAllocationConfigurations;
	}

	public String getCopyAllocationConfigurationName() {
		return copyAllocationConfigurationName;
	}

	public void setCopyAllocationConfigurationName(String copyAllocationConfigurationName) {
		this.copyAllocationConfigurationName = copyAllocationConfigurationName;
	}

	public String getAllocationConfigurationName() {
		return allocationConfigurationName;
	}

	public void setAllocationConfigurationName(String allocationConfigurationName) {
		this.allocationConfigurationName = allocationConfigurationName;
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

	public void checkSave() {
		if (allocationConfiguration == null && ClientUtil.currentUserIsAdmin()
				&& ClientUtil.getCurrentProcessingOrg() != null) {
			processingOrg = ClientUtil.getCurrentProcessingOrg();
			PrimeFaces.current().executeScript("PF('saveConfirmation').show()");
		} else {
			save();
		}
	}

	public void prepareCopy() {
		copyProcessingOrg = allocationConfiguration.getProcessingOrg();
	}

	public String getSaveMessage() {
		if (processingOrg == null) {
			return StringUtils.EMPTY;
		}
		return String.format("The new Allocation Configuration will be linked to Processing Org %s.", processingOrg.getShortName());
	}

	public String getCopyMessage() {
		if (copyProcessingOrg == null) {
			return StringUtils.EMPTY;
		}
		return String.format("The new Allocation Configuration will be linked to Processing Org %s.", copyProcessingOrg.getShortName());
	}

	public SortedSet<LegalEntity> getAllProcessingOrgs() {
		return allProcessingOrgs;
	}

	public void setAllProcessingOrgs(SortedSet<LegalEntity> allProcessingOrgs) {
		this.allProcessingOrgs = allProcessingOrgs;
	}

	public void save() {
		try {
			Set<Book> bookSet = null;
			if (books.getTarget() != null && !books.getTarget().isEmpty()) {
				bookSet = new HashSet<>(books.getTarget());
			}
			if (allocationConfiguration == null) {
				LegalEntity po;
				if (ClientUtil.currentUserIsAdmin()) {
					po = processingOrg;
				} else {
					po = ClientUtil.getCurrentUser().getProcessingOrg();
				}
				allocationConfiguration = new AllocationConfiguration(allocationConfigurationName, po);
			}
			allocationConfiguration.setBooks(bookSet);
			allocationConfiguration.setId(
					allocationConfigurationBusinessDelegate.saveAllocationConfiguration(allocationConfiguration));
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
					"Allocation Configuration " + allocationConfiguration.getId() + " successfully saved"));
			refresh();
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public void copy() {
		if (copyAllocationConfigurationName.equals(allocationConfiguration.getName())) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
					"The name of the copied Allocation Configuration must be different than the original one."));
			return;
		}
		try {
			LegalEntity po;
			if (ClientUtil.currentUserIsAdmin()) {
				po = copyProcessingOrg;
			} else {
				po = ClientUtil.getCurrentUser().getProcessingOrg();
			}
			AllocationConfiguration copyAllocationConfiguration = new AllocationConfiguration(
					copyAllocationConfigurationName, po);
			Set<Book> bookSet = null;
			if (books.getTarget() != null && !books.getTarget().isEmpty()) {
				bookSet = new HashSet<>(books.getTarget());
			}
			copyAllocationConfiguration.setBooks(bookSet);
			copyAllocationConfiguration.setId(
					allocationConfigurationBusinessDelegate.saveAllocationConfiguration(copyAllocationConfiguration));
			allocationConfiguration = copyAllocationConfiguration;
			allocationConfigurationName = copyAllocationConfigurationName;
			processingOrg = copyProcessingOrg;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
					"Allocation Configuration " + allocationConfiguration.getId() + " successfully created"));
			refresh();
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		} finally {
			copyAllocationConfigurationName = null;
			copyProcessingOrg = null;
		}
	}

	public void load() {
		if (loadAllocationConfiguration != null) {
			allocationConfiguration = loadAllocationConfiguration;
			allocationConfigurationName = allocationConfiguration.getName();
			List<Book> allocConfigBooks = new ArrayList<>();
			
			List<Book> sourceBooks = availableBooks.stream()
					.filter(b -> b.getProcessingOrg().equals(allocationConfiguration.getProcessingOrg()))
					.toList();
			
			if (allocationConfiguration.getBooks() != null) {
				allocConfigBooks = new ArrayList<>(allocationConfiguration.getBooks());
				final List<Book> tmpAllocConfigBooks = allocConfigBooks;
				books.setSource(sourceBooks.stream().filter(s -> !tmpAllocConfigBooks.contains(s)).toList());
			} else {
				books.setSource(sourceBooks);
			}
			books.setTarget(allocConfigBooks);
			processingOrg = allocationConfiguration.getProcessingOrg();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
					"Allocation Configuration " + allocationConfiguration.getId() + " successfully loaded."));
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
					"Please select an Allocation Configuration."));
		}
	}

	public void clear() {
		allocationConfiguration = null;
		allocationConfigurationName = null;
		processingOrg = null;
		loadAllocationConfiguration = null;
		availableBooks = getBooksForCurrentContext();
		initModel();
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Form cleared"));
	}

	public String getDisambiguatedBookName(Book book) {
		if (book == null) {
			return StringUtils.EMPTY;
		}
		if (!ClientUtil.currentUserIsAdmin() || ClientUtil.getCurrentProcessingOrg() != null) {
			return book.getName();
		}
		return book.getName() + " [" + book.getProcessingOrg().getShortName() + "]";
	}

	public String getDisambiguatedName(AllocationConfiguration ac) {
		if (ac == null) {
			return StringUtils.EMPTY;
		}
		if (!ClientUtil.currentUserIsAdmin() || ClientUtil.getCurrentProcessingOrg() != null) {
			return ac.getName();
		}
		return ac.getName() + " [" + ac.getProcessingOrg().getShortName() + "]";
	}

}