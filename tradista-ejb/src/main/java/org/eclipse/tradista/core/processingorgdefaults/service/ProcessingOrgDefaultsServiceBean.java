package org.eclipse.tradista.core.processingorgdefaults.service;

import org.eclipse.tradista.core.processingorgdefaults.model.ProcessingOrgDefaults;
import org.eclipse.tradista.core.processingorgdefaults.persistence.ProcessingOrgDefaultsSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class ProcessingOrgDefaultsServiceBean implements ProcessingOrgDefaultsService {

	@Override
	@Interceptors(ProcessingOrgDefaultsFilteringInterceptor.class)
	public ProcessingOrgDefaults getProcessingOrgDefaultsByPoId(long poId) {
		return ProcessingOrgDefaultsSQL.getProcessingOrgDefaultsByPoId(poId);
	}

	@Override
	@Interceptors(ProcessingOrgDefaultsFilteringInterceptor.class)
	public long saveProcessingOrgDefaults(ProcessingOrgDefaults poDefaults) {
		return ProcessingOrgDefaultsSQL.saveProcessingOrgDefaults(poDefaults);
	}

}