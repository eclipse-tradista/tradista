package org.eclipse.tradista.core.message.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.message.model.ImportError;
import org.eclipse.tradista.core.message.model.ImportError.ImportErrorType;
import org.eclipse.tradista.core.message.persistence.ImportErrorSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

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
public class ImportErrorServiceBean implements ImportErrorService {

	@Override
	public long saveImportError(ImportError error) {
		return ImportErrorSQL.saveImportError(error);
	}
	
	@Override
	public List<ImportError> getImportErrors(Set<String> importerTypes, Set<String> importerNames, long messageId, Status status,
			LocalDate errorDateFrom, LocalDate errorDateTo, LocalDate solvingDateFrom, LocalDate solvingDateTo) {
		return ImportErrorSQL.getImportErrors(importerTypes, importerNames, messageId, status,
				errorDateFrom, errorDateTo, solvingDateFrom, solvingDateTo);
	}
	
	public ImportError getImportError(long msgId, ImportErrorType importErrorType) {
		return ImportErrorSQL.getImportError(msgId, importErrorType);
	}

}