package org.eclipse.tradista.core.batch.service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.batch.jobproperty.JobProperty;
import org.eclipse.tradista.core.batch.model.TradistaJob;
import org.eclipse.tradista.core.batch.model.TradistaJobExecution;
import org.eclipse.tradista.core.batch.model.TradistaJobInstance;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;

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

public class BatchBusinessDelegate {

	private BatchService batchService;

	public BatchBusinessDelegate() {
		batchService = TradistaServiceLocator.getInstance().getBatchService();
	}

	public void saveJobInstance(TradistaJobInstance jobInstance) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(jobInstance.getName())) {
			errMsg.append(String.format("The job instance name cannot be empty.%n"));
		}
		if (StringUtils.isBlank(jobInstance.getJobType())) {
			errMsg.append(String.format("The job type cannot be empty.%n"));
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		SecurityUtil.runEx(() -> batchService.saveJobInstance(jobInstance));
	}

	public void deleteJobInstance(String jobInstanceName, String po) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(jobInstanceName)) {
			errMsg.append(String.format("The job instance cannot be null.%n"));
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		SecurityUtil.run(() -> batchService.deleteJobInstance(jobInstanceName, po));
	}

	public Set<String> getAllJobTypes() {
		return SecurityUtil.run(() -> batchService.getAllJobTypes());
	}

	public String getJobTypeByClass(Class<? extends TradistaJob> klass) throws TradistaBusinessException {
		return SecurityUtil.runEx(() -> batchService.getJobTypeByClass(klass));
	}

	public Set<TradistaJobInstance> getAllJobInstances(String po) throws TradistaBusinessException {
		return SecurityUtil.runEx(() -> batchService.getAllJobInstances(po));
	}

	public TradistaJobInstance getJobInstanceByNameAndPo(String jobInstanceName, String po)
			throws TradistaBusinessException {
		return SecurityUtil.runEx(() -> batchService.getJobInstanceByNameAndPo(jobInstanceName, po));
	}

	public Set<TradistaJobExecution> getJobExecutions(LocalDate executionDate, String po)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (executionDate == null) {
			errMsg.append(String.format("The execution date cannot be null.%n"));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.run(() -> batchService.getJobExecutions(executionDate, po));
	}

	public void runJobInstance(String jobInstanceName, String po) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(jobInstanceName)) {
			errMsg.append(String.format("The job instance name cannot be empty.%n"));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		SecurityUtil.run(() -> batchService.runJobInstance(jobInstanceName, po));
	}

	public long saveJobExecution(String name, String po, String status, LocalDateTime startTime, LocalDateTime endTime,
			String errorCause, String jobInstanceName, String jobType) {
		return SecurityUtil.run(() -> batchService.saveJobExecution(name, po, status, startTime, endTime, errorCause,
				jobInstanceName, jobType));
	}

	public void stopJobExecution(String jobExecutionId) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(jobExecutionId)) {
			errMsg.append(String.format("The job execution id cannot be empty.%n"));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		SecurityUtil.run(() -> batchService.stopJobExecution(jobExecutionId));
	}

	public Class<? extends TradistaJob> getJobClassByType(String jobType) throws TradistaBusinessException {

		if (StringUtils.isEmpty(jobType)) {
			throw new TradistaBusinessException("The job type is mandatory.");
		}
		return SecurityUtil.runEx(() -> batchService.getJobClassByType(jobType));
	}

	public Set<String> getAllJobPropertyNames(TradistaJobInstance jobInstance) throws TradistaBusinessException {

		if (jobInstance == null) {
			throw new TradistaBusinessException("The job instance is mandatory.");
		}

		final Set<String> jobPropertyNames = new HashSet<String>();

		Class<?> klass;
		if (jobInstance.getJobDetail() != null) {
			klass = jobInstance.getJobDetail().getJobClass();
		} else {
			klass = getJobClassByType(jobInstance.getJobType());
		}
		// iterate though the list of fields declared in the class and
		// retrieve the name specified in the @JobProperty annotation
		final List<Field> allFields = new ArrayList<Field>(Arrays.asList(klass.getDeclaredFields()));
		for (final Field field : allFields) {
			if (field.isAnnotationPresent(JobProperty.class)) {
				jobPropertyNames.add(field.getAnnotation(JobProperty.class).name());
			}
		}
		return jobPropertyNames;
	}

	public String getPropertyType(TradistaJobInstance jobInstance, String propertyName)
			throws TradistaBusinessException {

		StringBuilder errMsg = new StringBuilder();

		if (jobInstance == null) {
			errMsg.append("The job instance is mandatory.%n");
		}
		if (StringUtils.isEmpty(propertyName)) {
			errMsg.append("The property name is mandatory.");
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		Class<?> klass;
		if (jobInstance.getJobDetail() != null) {
			klass = jobInstance.getJobDetail().getJobClass();
		} else {
			klass = getJobClassByType(jobInstance.getJobType());
		}

		// iterate though the list of fields declared in the class and
		// retrieve the name specified in the @JobProperty annotation
		final List<Field> allFields = new ArrayList<Field>(Arrays.asList(klass.getDeclaredFields()));
		for (final Field field : allFields) {
			if (field.isAnnotationPresent(JobProperty.class)) {
				if (propertyName.equals(field.getAnnotation(JobProperty.class).name())) {
					return field.getAnnotation(JobProperty.class).type();
				}
			}
		}

		throw new TradistaBusinessException(
				String.format("No '%s' property could be find in the %s job.", propertyName, klass));

	}

	public TradistaJobExecution getJobExecutionById(String jobExecutionId) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isEmpty(jobExecutionId)) {
			errMsg.append(String.format("The job execution id is mandatory.%n"));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.run(() -> batchService.getJobExecutionById(jobExecutionId));
	}

}