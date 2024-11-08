package org.eclipse.tradista.core.processingorgdefaults.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

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

public class ProcessingOrgDefaults extends TradistaObject {

	private static final long serialVersionUID = 1344658382239565337L;

	@Id
	private LegalEntity processingOrg;

	public ProcessingOrgDefaults(LegalEntity po) {
		processingOrg = po;
	}

	private List<ProcessingOrgDefaultsModule> modules;

	@SuppressWarnings("unchecked")
	public List<ProcessingOrgDefaultsModule> getModules() {
		return (List<ProcessingOrgDefaultsModule>) TradistaModelUtil.deepCopy(modules);
	}

	public void setModules(List<ProcessingOrgDefaultsModule> modules) {
		this.modules = modules;
	}

	public LegalEntity getProcessingOrg() {
		return TradistaModelUtil.clone(processingOrg);
	}

	public void setProcessingOrg(LegalEntity processingOrg) {
		this.processingOrg = processingOrg;
	}

	public ProcessingOrgDefaultsModule getModuleByName(String name) throws TradistaBusinessException {
		if (StringUtils.isEmpty(name)) {
			throw new TradistaBusinessException("Module name is mandatory.");
		}
		if (modules != null && !modules.isEmpty()) {
			Optional<? extends ProcessingOrgDefaultsModule> optMod = modules.stream()
					.filter(m -> m.getName().equals(name)).findAny();
			if (optMod.isPresent()) {
				return optMod.get();
			}
		}
		return null;
	}

	@Override
	public ProcessingOrgDefaults clone() {
		ProcessingOrgDefaults poDefaults = (ProcessingOrgDefaults) super.clone();
		poDefaults.processingOrg = TradistaModelUtil.clone(processingOrg);
		return poDefaults;
	}

	public void addModule(ProcessingOrgDefaultsModule module) {
		if (modules == null) {
			modules = new ArrayList<>();
		}
		modules.add(module);
	}

}