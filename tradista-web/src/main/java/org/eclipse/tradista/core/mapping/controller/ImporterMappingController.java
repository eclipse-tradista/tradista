package org.eclipse.tradista.core.mapping.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.importer.service.ImporterConfigurationBusinessDelegate;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet.Mapping;
import org.eclipse.tradista.core.mapping.model.MappingType;
import org.eclipse.tradista.core.mapping.service.MappingBusinessDelegate;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.CellEditEvent;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
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

	private InterfaceMappingSet interfaceMappingSet;

	private String importerNameLoadingCriterion;

	private MappingType mappingTypeLoadingCriterion;

	private MappingBusinessDelegate mappingBusinessDelegate;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	private ImporterConfigurationBusinessDelegate importerConfigurationBusinessDelegate;

	private LegalEntity processingOrg;

	private SortedSet<LegalEntity> allProcessingOrgs;

	private MappingType[] allMappingTypes;

	private Set<String> allImporterNames;

	private SortedSet<String> allLegalEntityShortNames;

	private SortedSet<String> allBookNames;

	private List<Mapping> displayedMappings;

	@PostConstruct
	public void init() {
		importerConfigurationBusinessDelegate = new ImporterConfigurationBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		mappingBusinessDelegate = new MappingBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();
		Set<String> allImpNames;
		if (ClientUtil.currentUserIsAdmin()) {
			Set<LegalEntity> processingOrgs = legalEntityBusinessDelegate.getAllProcessingOrgs();
			allProcessingOrgs = new TreeSet<>();
			if (processingOrgs != null) {
				allProcessingOrgs.addAll(processingOrgs);
			}
		}
		allImporterNames = new HashSet<>();
		allImporterNames.add(StringUtils.EMPTY);

		allImpNames = importerConfigurationBusinessDelegate.getAllImporterNames();

		if (allImpNames != null) {
			allImporterNames.addAll(allImpNames);
		}

		allMappingTypes = MappingType.values();
		Arrays.sort(allMappingTypes);
	}

	public LegalEntity getProcessingOrg() {
		return processingOrg;
	}

	public void setProcessingOrg(LegalEntity processingOrg) {
		this.processingOrg = processingOrg;
	}

	public SortedSet<LegalEntity> getAllProcessingOrgs() {
		return allProcessingOrgs;
	}

	public void setAllProcessingOrgs(SortedSet<LegalEntity> allProcessingOrgs) {
		this.allProcessingOrgs = allProcessingOrgs;
	}

	public void save() {
		try {
			interfaceMappingSet.setMappings(new HashSet<>(displayedMappings));
			interfaceMappingSet.setId(mappingBusinessDelegate.saveInterfaceMappingSet(interfaceMappingSet));
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Interface Mapping Set successfully saved"));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	public void load() {
		try {
			interfaceMappingSet = mappingBusinessDelegate.getInterfaceMappingSet(importerNameLoadingCriterion,
					mappingTypeLoadingCriterion, InterfaceMappingSet.Direction.INCOMING);
			if (interfaceMappingSet == null) {
				LegalEntity po;
				if (ClientUtil.currentUserIsAdmin()) {
					po = processingOrg;
				} else {
					po = ClientUtil.getCurrentUser().getProcessingOrg();
				}
				interfaceMappingSet = new InterfaceMappingSet(importerNameLoadingCriterion, mappingTypeLoadingCriterion,
						InterfaceMappingSet.Direction.INCOMING, po);
			}
			displayedMappings = new ArrayList<>(interfaceMappingSet.getMappings());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Interface Mapping Set successfully loaded."));
		} catch (TradistaBusinessException tbe) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
		}
	}

	@SuppressWarnings({ "unchecked" })
	public void onMappingAdded(CellEditEvent<String> event) {
		if (interfaceMappingSet != null) {
			if (!CollectionUtils.isEmpty(displayedMappings)) {
				Map<String, Object> attributes = ((UIComponent) event.getColumn()).getAttributes();
				boolean isOriginalValueColumn = "originalValue".equals(attributes.get("colKey"));
				DataTable table = (DataTable) ((UIComponent) event.getColumn()).getParent();
				List<Mapping> rows = (List<Mapping>) table.getValue();
				Set<String> seen = new HashSet<>();
				boolean hasDuplicate = false;
				if (StringUtils.isBlank(event.getNewValue())) {
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "The mapping cannot be blank."));
					Mapping mapping = (Mapping) event.getRowData();
					if (isOriginalValueColumn) {
						mapping.setValue(event.getOldValue());
					} else {
						mapping.setMappedValue(event.getOldValue());
					}
					return;
				}
				for (Mapping mapping : rows) {
					String value = isOriginalValueColumn ? mapping.getValue() : mapping.getMappedValue();
					if (!StringUtils.isBlank(value)) {
						if (!seen.add(value)) {
							hasDuplicate = true;
							break;
						}
					}
				}
				if (hasDuplicate) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
							"Warning", String.format("%s is already mapped.", event.getNewValue())));
					Mapping mapping = (Mapping) event.getRowData();
					if (isOriginalValueColumn) {
						mapping.setValue(event.getOldValue());
					} else {
						mapping.setMappedValue(event.getOldValue());
					}
				}
			}
		}
	}

	public void addMapping() {
		if (interfaceMappingSet != null) {
			displayedMappings.add(interfaceMappingSet.new Mapping(StringUtils.EMPTY, StringUtils.EMPTY));
		}
	}

	public void removeMapping(Mapping mapping) {
		if (interfaceMappingSet != null) {
			displayedMappings.remove(mapping);
		}
	}

	public SortedSet<String> getMappableObjects() {
		if (interfaceMappingSet != null) {
			if (interfaceMappingSet.getMappingType().equals(MappingType.LegalEntity)) {
				if (allLegalEntityShortNames == null) {
					Set<LegalEntity> legalEntities = legalEntityBusinessDelegate.getAllLegalEntities();
					allLegalEntityShortNames = new TreeSet<>();
					if (legalEntities != null) {
						allLegalEntityShortNames.addAll(
								legalEntities.stream().map(LegalEntity::getShortName).collect(Collectors.toSet()));
					}
				}
				return allLegalEntityShortNames;
			} else {
				if (allBookNames == null) {
					Set<Book> books = bookBusinessDelegate.getAllBooks();
					allBookNames = new TreeSet<>();
					if (books != null) {
						allBookNames.addAll(books.stream().map(Book::getName).collect(Collectors.toSet()));
					}
				}
				return allBookNames;
			}
		}
		return null;
	}

	public InterfaceMappingSet getInterfaceMappingSet() {
		return interfaceMappingSet;
	}

	public void setInterfaceMappingSet(InterfaceMappingSet interfaceMappingSet) {
		this.interfaceMappingSet = interfaceMappingSet;
	}

	public String getImporterNameLoadingCriterion() {
		return importerNameLoadingCriterion;
	}

	public void setImporterNameLoadingCriterion(String importerNameLoadingCriterion) {
		this.importerNameLoadingCriterion = importerNameLoadingCriterion;
	}

	public MappingType getMappingTypeLoadingCriterion() {
		return mappingTypeLoadingCriterion;
	}

	public void setMappingTypeLoadingCriterion(MappingType mappingTypeLoadingCriterion) {
		this.mappingTypeLoadingCriterion = mappingTypeLoadingCriterion;
	}

	public MappingType[] getAllMappingTypes() {
		return allMappingTypes;
	}

	public void setAllMappingTypes(MappingType[] allMappingTypes) {
		this.allMappingTypes = allMappingTypes;
	}

	public Set<String> getAllImporterNames() {
		return allImporterNames;
	}

	public void setAllImporterNames(Set<String> allImporterNames) {
		this.allImporterNames = allImporterNames;
	}

	public SortedSet<String> getAllLegalEntityShortNames() {
		return allLegalEntityShortNames;
	}

	public void setAllLegalEntityShortNames(SortedSet<String> allLegalEntityShortNames) {
		this.allLegalEntityShortNames = allLegalEntityShortNames;
	}

	public SortedSet<String> getAllBookNames() {
		return allBookNames;
	}

	public void setAllBookNames(SortedSet<String> allBookNames) {
		this.allBookNames = allBookNames;
	}

	public List<Mapping> getDisplayedMappings() {
		return displayedMappings;
	}

	public void setDisplayedMappings(List<Mapping> displayedMapings) {
		this.displayedMappings = displayedMapings;
	}

}