package org.eclipse.tradista.core.batch.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import org.eclipse.tradista.core.batch.model.TradistaJob;
import org.eclipse.tradista.core.batch.model.TradistaJobExecution;
import org.eclipse.tradista.core.batch.model.TradistaJobInstance;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import jakarta.ejb.Remote;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

@Remote
public interface BatchService {

	void saveJobInstance(TradistaJobInstance jobInstance) throws TradistaBusinessException;

	Set<String> getAllJobTypes();

	String getJobTypeByClass(Class<? extends TradistaJob> klass) throws TradistaBusinessException;

	Class<? extends TradistaJob> getJobClassByType(String jobType) throws TradistaBusinessException;

	void deleteJobInstance(String jobInstanceName, String po);

	Set<TradistaJobInstance> getAllJobInstances(String po) throws TradistaBusinessException;

	TradistaJobInstance getJobInstanceByNameAndPo(String jobInstanceName, String po) throws TradistaBusinessException;

	Set<TradistaJobExecution> getJobExecutions(LocalDate date, String po);

	void runJobInstance(String jobInstanceName, String po);

	void stopJobExecution(String jobExecutionId);

	long saveJobExecution(String name, String po, String status, LocalDateTime startTime, LocalDateTime endTime,
			String errorCause, String jobInstanceName, String jobType);

	TradistaJobExecution getJobExecutionById(String jobExecutionId);

}
