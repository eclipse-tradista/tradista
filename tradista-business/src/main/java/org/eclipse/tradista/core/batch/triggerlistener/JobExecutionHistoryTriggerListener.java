package org.eclipse.tradista.core.batch.triggerlistener;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.listeners.TriggerListenerSupport;
import org.springframework.util.StringUtils;

import org.eclipse.tradista.core.batch.model.TradistaJob;
import org.eclipse.tradista.core.batch.service.BatchBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;

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

public class JobExecutionHistoryTriggerListener extends TriggerListenerSupport {

	BatchBusinessDelegate batchBusinessDelegate;

	public JobExecutionHistoryTriggerListener() {
		batchBusinessDelegate = new BatchBusinessDelegate();
	}

	@Override
	public String getName() {
		return "JobExecutionHistoryTriggerListener";
	}

	public void triggerFired(Trigger trigger, JobExecutionContext context) {
		String jobType = null;
		try {
			jobType = batchBusinessDelegate
					.getJobTypeByClass((Class<? extends TradistaJob>) context.getJobDetail().getJobClass());
		} catch (TradistaBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		batchBusinessDelegate.saveJobExecution(context.getFireInstanceId(), trigger.getJobKey().getGroup(),
				"IN PROGRESS", LocalDateTime.ofInstant(context.getFireTime().toInstant(), ZoneId.systemDefault()), null,
				null, trigger.getJobKey().getName(), jobType);
	}

	public void triggerComplete(Trigger trigger, JobExecutionContext context,
			Trigger.CompletedExecutionInstruction triggerInstructionCode) {
		String status;
		String errorCause = (String) context.get(TradistaJob.ERROR_CAUSE);
		if (StringUtils.isEmpty(errorCause)) {
			status = "SUCCESS";
		} else {
			status = "FAILED";
		}
		if (!StringUtils.isEmpty(context.get(TradistaJob.STOPPED))) {
			status = "STOPPED";
		}
		String jobType = null;
		try {
			jobType = batchBusinessDelegate
					.getJobTypeByClass((Class<? extends TradistaJob>) context.getJobDetail().getJobClass());
		} catch (TradistaBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		batchBusinessDelegate.saveJobExecution(context.getFireInstanceId(), trigger.getJobKey().getGroup(), status,
				null, LocalDateTime.now(), errorCause, trigger.getJobKey().getName(), jobType);
	}

}