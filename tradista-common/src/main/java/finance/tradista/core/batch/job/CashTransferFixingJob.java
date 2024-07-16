package finance.tradista.core.batch.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import finance.tradista.core.batch.jobproperty.JobProperty;
import finance.tradista.core.batch.model.TradistaJob;
import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.marketdata.model.QuoteSet;
import finance.tradista.core.transfer.service.TransferBusinessDelegate;

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

public class CashTransferFixingJob extends TradistaJob {

	@JobProperty(name = "QuoteSet", type = "QuoteSet")
	private QuoteSet quoteSet;

	@Override
	public void executeTradistaJob(JobExecutionContext execContext)
			throws JobExecutionException, TradistaBusinessException {
		TransferBusinessDelegate transferBusinessDelegate = new TransferBusinessDelegate();
		transferBusinessDelegate.fixCashTransfers(quoteSet.getId());
	}

	@Override
	public String getName() {
		return "CashTransferFixing";
	}

	public void setQuoteSet(QuoteSet quoteSet) {
		this.quoteSet = quoteSet;
	}

	@Override
	public void checkJobProperties() throws TradistaBusinessException {
		if (quoteSet == null) {
			throw new TradistaBusinessException("The quote set is mandatory.");
		}
	}

}