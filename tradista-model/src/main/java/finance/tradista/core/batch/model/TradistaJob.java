package org.eclipse.tradista.core.batch.model;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.UnableToInterruptJobException;
import org.quartz.ee.jta.UserTransactionHelper;
import org.springframework.scheduling.quartz.QuartzJobBean;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;

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
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
/**
 * I extend QuartzJobBean because It is needed to have the properties dependency
 * injection in the job instances. If TradistaJob doesn't inherit from
 * QuartzJobBean, the properties dependency injection doesn't work. This because
 * Spring Quartz integration use SpringBeanJobFactory as JobFactory and this
 * factory does DI only with QuartzJobBean instances...
 * 
 * @author OA
 *
 */
public abstract class TradistaJob extends QuartzJobBean implements InterruptableJob {

	public static final String ERROR_CAUSE = "ERROR_CAUSE";

	public static final String STOPPED = "STOPPED";

	protected boolean isInterrupted;

	protected UserTransaction userTransaction;

	public abstract String getName();

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		isInterrupted = true;
	}

	protected void performInterruption(JobExecutionContext execContext) throws JobExecutionException {
		execContext.put(TradistaJob.STOPPED, "Job stopped.");
		try {
			userTransaction.rollback();
		} catch (IllegalStateException | SecurityException | SystemException e) {
			throw new JobExecutionException(e);
		}
		// Thrown because we want to stop the job immediately. This exception is
		// then handled by the Quartz scheduler (Other exceptions are not, they
		// are considered as abnormal terminations by Quartz)
		throw new JobExecutionException("Job stopped.");
	}

	protected void raiseError(JobExecutionContext execContext, Exception e) throws JobExecutionException {
		execContext.put(TradistaJob.ERROR_CAUSE, e.getMessage());
		try {
			userTransaction.rollback();
		} catch (IllegalStateException | SecurityException | SystemException e2) {
			throw new JobExecutionException(e);
		}
		throw new JobExecutionException(e);
	}

	@Override
	public void executeInternal(JobExecutionContext execContext) throws JobExecutionException {

		try {
			userTransaction = UserTransactionHelper.lookupUserTransaction();
			userTransaction.begin();
		} catch (SchedulerException | NotSupportedException | SystemException e) {
			throw new JobExecutionException(e);
		}
		try {
			checkJobProperties();
			executeTradistaJob(execContext);
		} catch (TradistaBusinessException | TradistaTechnicalException e) {
			raiseError(execContext, e);
		}
		try {
			userTransaction.commit();
		} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
				| HeuristicRollbackException | SystemException e) {
			try {
				userTransaction.rollback();
				throw new JobExecutionException(e);
			} catch (IllegalStateException | SecurityException | SystemException e2) {
				throw new JobExecutionException(e2);
			}
		}

	}

	protected abstract void executeTradistaJob(JobExecutionContext execContext)
			throws JobExecutionException, TradistaBusinessException;

	public abstract void checkJobProperties() throws TradistaBusinessException;
}