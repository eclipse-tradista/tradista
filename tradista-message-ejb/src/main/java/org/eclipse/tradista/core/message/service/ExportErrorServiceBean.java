package org.eclipse.tradista.core.message.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.message.model.ExportError;
import org.eclipse.tradista.core.message.model.ExportError.ExportErrorType;
import org.eclipse.tradista.core.message.persistence.ExportErrorSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class ExportErrorServiceBean implements ExportErrorService {

	@Interceptors(ExportErrorAuthorizationFilteringInterceptor.class)
	@Override
	public long saveExportError(ExportError error) {
		return ExportErrorSQL.saveExportError(error);
	}

	@Interceptors(ExportErrorAuthorizationFilteringInterceptor.class)
	@Override
	public List<ExportError> getExportErrors(Set<String> exporterTypes, Set<String> exporterNames, long messageId,
			Status status, ExportErrorType exportErrorType, LocalDate errorDateFrom, LocalDate errorDateTo,
			LocalDate solvingDateFrom, LocalDate solvingDateTo) {
		return ExportErrorSQL.getExportErrors(exporterTypes, exporterNames, messageId, status, exportErrorType,
				errorDateFrom, errorDateTo, solvingDateFrom, solvingDateTo);
	}

	@Interceptors(ExportErrorAuthorizationFilteringInterceptor.class)
	@Override
	public ExportError getExportError(long msgId, ExportErrorType importErrorType) {
		return ExportErrorSQL.getExportError(msgId, importErrorType);
	}

}