package org.eclipse.tradista.core.batch.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.model.LegalEntity.Role;
import org.quartz.JobDetail;

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

public class TradistaJobInstance extends TradistaObject {

	private static final long serialVersionUID = 8887650019932154806L;

	private JobDetail jobDetail;

	private String jobType;

	@Id
	private String name;

	@Id
	private LegalEntity processingOrg;

	private Map<String, Object> properties;

	public static final String PROCESSING_ORG_PROPERTY_KEY = "ProcessingOrg";

	public TradistaJobInstance(JobDetail jobDetail, String jobType, LegalEntity po) {
		super();
		this.jobDetail = jobDetail;
		this.jobType = jobType;
		this.name = jobDetail.getKey().getName();
		properties = new HashMap<String, Object>();
		if (po != null && po.getRole().equals(Role.PROCESSING_ORG)) {
			jobDetail.getJobDataMap().put(PROCESSING_ORG_PROPERTY_KEY, po);
		}
		processingOrg = po;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public TradistaJobInstance(String name, String jobType, LegalEntity po) {
		super();
		this.jobType = jobType;
		this.name = name;
		properties = new HashMap<String, Object>();
		if (po != null && po.getRole().equals(Role.PROCESSING_ORG)) {
			properties.put(PROCESSING_ORG_PROPERTY_KEY, po);
		}
		processingOrg = po;
	}

	public String getJobType() {
		return jobType;
	}

	public String getName() {
		return name;
	}

	public JobDetail getJobDetail() {
		if (jobDetail == null) {
			return null;
		}
		return (JobDetail) jobDetail.clone();
	}

	/**
	 * Uses new properties if it was set, otherwise uses stored properties
	 * 
	 * @param key key of the property
	 * @return value of the property
	 */
	public Object getJobPropertyValue(String key) {
		if (properties.isEmpty() && (jobDetail != null)) {
			return jobDetail.getJobDataMap().get(key);
		} else {
			// For now, properties are only String.
			return properties.get(key);
		}
	}

	/**
	 * Returns new properties if it was set, otherwise returns stored properties
	 * 
	 * @return new properties if it was set, otherwise stored properties
	 */
	public Map<String, Object> getProperties() {
		if (properties.isEmpty() && (jobDetail != null)) {
			return new HashMap<>(jobDetail.getJobDataMap());
		} else {
			return new HashMap<>(properties);
		}
	}

	public LegalEntity getProcessingOrg() {
		return TradistaModelUtil.clone(processingOrg);
	}

	/**
	 * Flag to indicate if the object was already persisted or is a totally new one.
	 * 
	 * @return true if the object was already persisted, false if the object is
	 *         totally new.
	 */
	public boolean isSaved() {
		return (jobDetail != null);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public TradistaJobInstance clone() {
		TradistaJobInstance tradistaJobInstance = (TradistaJobInstance) super.clone();
		if (jobDetail != null) {
			tradistaJobInstance.jobDetail = (JobDetail) jobDetail.clone();
		}
		if (properties != null) {
			tradistaJobInstance.properties = new HashMap<>(properties);
		}
		tradistaJobInstance.processingOrg = TradistaModelUtil.clone(processingOrg);
		return tradistaJobInstance;
	}

}