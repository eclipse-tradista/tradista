package org.eclipse.tradista.core.batch.service;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

import static org.quartz.JobBuilder.newJob;

import java.beans.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.tradista.core.batch.model.TradistaJob;
import org.eclipse.tradista.core.batch.model.TradistaJobExecution;
import org.eclipse.tradista.core.batch.model.TradistaJobInstance;
import org.eclipse.tradista.core.batch.persistence.BatchSQL;
import org.eclipse.tradista.core.batch.triggerlistener.JobExecutionHistoryTriggerListener;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.service.CustomProperties;
import org.eclipse.tradista.core.common.util.TradistaConstants;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.configuration.service.LocalConfigurationService;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.service.LegalEntityService;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class BatchServiceBean implements BatchService {

	private static Map<String, Class<? extends TradistaJob>> jobTypes;

	private static Scheduler scheduler;

	@EJB
	private LocalConfigurationService configurationService;

	@EJB
	private LegalEntityService legalEntityService;

	public static void initJobTypes() {
		jobTypes = new HashMap<String, Class<? extends TradistaJob>>();
		String tradistaJobsPackage = TradistaConstants.CORE_PACKAGE + ".batch.job";
		List<Class<TradistaJob>> classes = TradistaUtil.getAllClassesByType(TradistaJob.class, tradistaJobsPackage);

		for (Class<TradistaJob> klass : classes) {
			try {
				jobTypes.put((TradistaUtil.getInstance(klass)).getName(), (Class<? extends TradistaJob>) klass);
			} catch (TradistaTechnicalException tte) {
				// TODO Auto-generated catch block
				tte.printStackTrace();
			}
		}

		// Retrieve Custom Jobs
		classes = TradistaUtil.getAllClassesByType(TradistaJob.class, CustomProperties.getCustomPackage());

		try {
			for (Class<TradistaJob> klass : classes) {
				jobTypes.put(TradistaUtil.getInstance(klass).getName(), (Class<? extends TradistaJob>) klass);
			}
		} catch (TradistaTechnicalException tte) {
			// TODO Auto-generated catch block
			tte.printStackTrace();
		}
	}

	@PostConstruct
	public void init() {
		if (jobTypes == null) {
			initJobTypes();
		}
		if (scheduler == null) {
			try {
				scheduler = configurationService.getScheduler();
				scheduler.getListenerManager().addTriggerListener(new JobExecutionHistoryTriggerListener());
				scheduler.start();
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Interceptors(JobFilteringInterceptor.class)
	@Override
	public void saveJobInstance(TradistaJobInstance jobInstance) throws TradistaBusinessException {
		String po = jobInstance.getProcessingOrg() != null ? jobInstance.getProcessingOrg().getShortName() : null;
		if (!jobInstance.isSaved()) {
			if (getJobInstanceByNameAndPo(jobInstance.getName(), po) != null) {
				throw new TradistaBusinessException(
						String.format("A job instance named %s already exists in the system for the PO %s.",
								jobInstance.getName(), po));
			}
		}

		JobBuilder jobBuilder = newJob(jobTypes.get(jobInstance.getJobType())).withIdentity(jobInstance.getName(), po)
				.storeDurably();
		if (jobInstance.getProperties() != null && !jobInstance.getProperties().isEmpty()) {
			JobDataMap map = new JobDataMap(jobInstance.getProperties());
			jobBuilder.setJobData(map);
		}

		if (jobInstance.isSaved()) {
			// Validate job properties
			validateJobProperties(jobBuilder);
		}

		try {
			scheduler.addJob(jobBuilder.build(), true);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void validateJobProperties(JobBuilder jobBuilder) throws TradistaBusinessException {
		JobDetail jobDetail = jobBuilder.build();
		TradistaJob job;
		try {
			job = (TradistaJob) TradistaUtil.getInstance(jobDetail.getJobClass());
			for (Entry<String, Object> entry : jobDetail.getJobDataMap().entrySet()) {
				if (!entry.getKey().equals(TradistaJobInstance.PROCESSING_ORG_PROPERTY_KEY)) {
					java.beans.Statement statement = new Statement(job, "set" + entry.getKey(),
							new Object[] { entry.getValue() });
					statement.execute();
				}
			}
		} catch (Exception e) {
			throw new TradistaTechnicalException(e);
		}
		job.checkJobProperties();
	}

	@Override
	public Set<String> getAllJobTypes() {
		return new HashSet<String>(jobTypes.keySet());
	}

	@Override
	public String getJobTypeByClass(Class<? extends TradistaJob> klass) throws TradistaBusinessException {
		for (Map.Entry<String, Class<? extends TradistaJob>> entry : jobTypes.entrySet()) {
			if (entry.getValue().equals(klass)) {
				return entry.getKey();
			}
		}
		throw new TradistaBusinessException("Job type was not found for '" + klass.getName() + "' class.");
	}

	@Override
	public Class<? extends TradistaJob> getJobClassByType(String jobType) throws TradistaBusinessException {
		Class<? extends TradistaJob> jobClass = jobTypes.get(jobType);
		if (jobClass != null) {
			return jobClass;
		}
		throw new TradistaBusinessException(String.format("Job class was not found for '%s' type.", jobType));
	}

	@Interceptors(JobFilteringInterceptor.class)
	public void deleteJobInstance(String jobInstanceName, String po) {
		try {
			scheduler.deleteJob(JobKey.jobKey(jobInstanceName, po));
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Interceptors(JobFilteringInterceptor.class)
	@Override
	public Set<TradistaJobInstance> getAllJobInstances(String po) throws TradistaBusinessException {
		Set<TradistaJobInstance> jobInstances = new HashSet<TradistaJobInstance>();

		LegalEntity processingOrg = null;
		if (po != null) {
			processingOrg = legalEntityService.getLegalEntityByShortName(po);
		}

		try {
			for (String groupName : scheduler.getJobGroupNames()) {
				if (po != null && !po.equals(groupName)) {
					continue;
				}
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
					JobDetail jobDetail = scheduler.getJobDetail(jobKey);
					String jobType = getJobTypeByClass((Class<? extends TradistaJob>) jobDetail.getJobClass());
					TradistaJobInstance inst = new TradistaJobInstance(jobDetail, jobType, processingOrg);
					jobInstances.add(inst);
				}
			}
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jobInstances;
	}

	@Interceptors(JobFilteringInterceptor.class)
	@Override
	public TradistaJobInstance getJobInstanceByNameAndPo(String jobInstanceName, String po)
			throws TradistaBusinessException {
		TradistaJobInstance jobInstance = null;

		LegalEntity processingOrg = null;
		if (po != null) {
			processingOrg = legalEntityService.getLegalEntityByShortName(po);
		}

		try {
			JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(jobInstanceName, po));
			if (jobDetail == null) {
				return null;
			}
			String jobType = getJobTypeByClass((Class<? extends TradistaJob>) jobDetail.getJobClass());
			jobInstance = new TradistaJobInstance(jobDetail, jobType, processingOrg);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jobInstance;
	}

	@Interceptors(JobFilteringInterceptor.class)
	@Override
	public Set<TradistaJobExecution> getJobExecutions(LocalDate date, String po) {
		return BatchSQL.getJobExecutions(date, po);
	}

	@Interceptors(JobFilteringInterceptor.class)
	@Override
	public void runJobInstance(String jobInstanceName, String po) {
		try {
			scheduler.triggerJob(JobKey.jobKey(jobInstanceName, po));
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Interceptors(JobFilteringInterceptor.class)
	@Override
	public void stopJobExecution(String jobExecutionId) {
		try {
			scheduler.interrupt(jobExecutionId);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Interceptors(JobFilteringInterceptor.class)
	@Override
	public long saveJobExecution(String name, String po, String status, LocalDateTime startTime, LocalDateTime endTime,
			String errorCause, String jobInstanceName, String jobType) {
		return BatchSQL.saveJobExecution(name, po, status, startTime, endTime, errorCause, jobInstanceName, jobType);
	}

	@Interceptors(JobFilteringInterceptor.class)
	@Override
	public TradistaJobExecution getJobExecutionById(String jobExecutionId) {
		return BatchSQL.getJobExecutionById(jobExecutionId);
	}

}