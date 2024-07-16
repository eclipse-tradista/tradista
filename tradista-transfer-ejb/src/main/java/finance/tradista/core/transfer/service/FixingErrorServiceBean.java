package finance.tradista.core.transfer.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

import org.jboss.ejb3.annotation.SecurityDomain;

import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.error.model.Error.Status;
import finance.tradista.core.transfer.model.CashTransfer;
import finance.tradista.core.transfer.model.FixingError;
import finance.tradista.core.transfer.persistence.FixingErrorSQL;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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
public class FixingErrorServiceBean implements FixingErrorService {

	@EJB
	TransferService transferService;

	@Override
	public boolean saveFixingErrors(List<FixingError> errors) {
		return FixingErrorSQL.saveFixingErrors(errors);
	}

	@Override
	public void solveFixingError(Set<Long> solved, LocalDate date) {
		FixingErrorSQL.solveFixingError(solved, date);
	}

	/**
	 * no existence controls on parameters here for performance reasons. Existence
	 * controls are important for saving methods but for getXXX methods like this
	 * one, not sure it is a good idea.
	 */
	@Interceptors(FixingErrorFilteringInterceptor.class)
	@Override
	public List<FixingError> getFixingErrors(long transferId, Status status, LocalDate errorDateFrom,
			LocalDate errorDateTo, LocalDate solvingDateFrom, LocalDate solvingDateTo)
			throws TradistaBusinessException {
		return FixingErrorSQL.getFixingErrors(transferId, status, errorDateFrom, errorDateTo, solvingDateFrom,
				solvingDateTo);
	}

	@Override
	public void solveFixingError(long transferId, LocalDate date) throws TradistaBusinessException {

		CashTransfer transfer = (CashTransfer) transferService.getTransferById(transferId);

		if (transfer == null) {
			throw new TradistaBusinessException(
					String.format("The CashTransfer with id %s does not exist in the system.", transferId));
		}

		FixingErrorSQL.solveFixingError(transferId, date);
	}

}