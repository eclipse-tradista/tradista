package org.eclipse.tradista.core.mapping.controller;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

	private ImporterConfigurationBusinessDelegate importerConfigurationBusinessDelegate;

	private LegalEntity processingOrg;

	private LegalEntity copyProcessingOrg;

	private SortedSet<LegalEntity> allProcessingOrgs;

	private MappingType[] allMappingTypes;

	private Set<String> allImporterNames;

	private SortedSet<String> availableLegalEntityShortNames;

	@PostConstruct
	public void init() {
		importerConfigurationBusinessDelegate = new ImporterConfigurationBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		mappingBusinessDelegate = new MappingBusinessDelegate();
		Set<String> allImpNames;
		if (ClientUtil.currentUserIsAdmin()) {
			Set<LegalEntity> processingOrgs = legalEntityBusinessDelegate.getAllProcessingOrgs();
			allProcessingOrgs = new TreeSet<>();
			if (processingOrgs != null) {
				allProcessingOrgs.addAll(processingOrgs);
			}
		}
		Set<LegalEntity> legalEntities = legalEntityBusinessDelegate.getAllLegalEntities();
		availableLegalEntityShortNames = new TreeSet<>();
		if (legalEntities != null) {
			availableLegalEntityShortNames
					.addAll(legalEntities.stream().map(le -> le.getShortName()).collect(Collectors.toSet()));
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
			// We reinitialize the set of available legal entity short names
			Set<LegalEntity> legalEntities = legalEntityBusinessDelegate.getAllLegalEntities();
			availableLegalEntityShortNames.clear();
			if (legalEntities != null) {
				availableLegalEntityShortNames
						.addAll(legalEntities.stream().map(le -> le.getShortName()).collect(Collectors.toSet()));
			}
			Set<Mapping> mappings = interfaceMappingSet.getMappings();
			if (!CollectionUtils.isEmpty(mappings)) {
				availableLegalEntityShortNames.removeAll(mappings.stream().map(Mapping::getMappedValue).toList());
			}
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
			Set<Mapping> mappings = interfaceMappingSet.getMappings();
			if (!CollectionUtils.isEmpty(mappings)) {
				Map<String, Object> attributes = ((UIComponent) event.getColumn()).getAttributes();
				boolean isOriginalValueColumn = "originalValue".equals(attributes.get("colKey"));
				DataTable table = (DataTable) ((UIComponent) event.getColumn()).getParent();
				Set<Mapping> rows = (Set<Mapping>) table.getValue();
				Set<String> seen = new HashSet<>();
				boolean hasDuplicate = false;
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
				if (!isOriginalValueColumn) {
					// availableLegalEntityShortNames.removeAll(mappings.stream().map(Mapping::getMappedValue).toList());
				}
			}
		}
	}

	public void addMapping() {
		if (interfaceMappingSet != null) {
			if (CollectionUtils.isEmpty(availableLegalEntityShortNames)) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Warning", "All Legal Entities are already mapped."));
			} else {
				interfaceMappingSet.addMapping(StringUtils.EMPTY, StringUtils.EMPTY);
			}
		}
	}

	public void removeMapping(Mapping mapping) {
		if (interfaceMappingSet != null) {
			interfaceMappingSet.removeMapping(mapping);
		}
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

	public SortedSet<String> getAvailableLegalEntityShortNames() {
		return availableLegalEntityShortNames;
	}

	public void setAvailableLegalEntityShortNames(SortedSet<String> allLegalEntityShortNames) {
		this.availableLegalEntityShortNames = allLegalEntityShortNames;
	}

}