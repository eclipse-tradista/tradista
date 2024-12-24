package org.eclipse.tradista.core.batch.job;

import org.eclipse.tradista.core.batch.model.TradistaJob;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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

public class ExpirationJob extends TradistaJob {

	@Override
	public void executeTradistaJob(JobExecutionContext execContext)
			throws JobExecutionException, TradistaBusinessException {
		ProductInventoryBusinessDelegate productInventoryBusinessDelegate = new ProductInventoryBusinessDelegate();
		productInventoryBusinessDelegate.closeExpiredProductsPositions();
	}

	@Override
	public String getName() {
		return "Expiration";
	}

	@Override
	public void checkJobProperties() throws TradistaBusinessException {
	}

}