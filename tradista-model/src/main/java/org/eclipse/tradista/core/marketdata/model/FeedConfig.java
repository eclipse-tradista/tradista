package org.eclipse.tradista.core.marketdata.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

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
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

/**
 * Structure defined to map provider data to Tradista data. Defined in this
 * order : Provider Data to Tradista Data
 * 
 * @author olivier_asuncion
 *
 */
public class FeedConfig extends TradistaObject {

	private static final long serialVersionUID = -5519797441712646656L;

	private FeedType feedType;

	@Id
	private String name;

	private Map<String, Quote> mapping;

	private Map<String, Map<String, String>> fieldsMapping;

	@Id
	private LegalEntity processingOrg;

	@SuppressWarnings("unchecked")
	public Map<String, Map<String, String>> getFieldsMapping() {
		Map<String, Map<String, String>> fieldsMapping = new HashMap<String, Map<String, String>>();
		for (Map.Entry<String, Map<String, String>> entry : this.fieldsMapping.entrySet()) {
			if (entry.getValue() != null) {
				fieldsMapping.put(entry.getKey(),
						(Map<String, String>) ((HashMap<String, String>) entry.getValue()).clone());
			}
		}
		return fieldsMapping;
	}

	public void setFieldsMapping(Map<String, Map<String, String>> fieldsMapping) {
		this.fieldsMapping = fieldsMapping;
	}

	public FeedConfig(String name, LegalEntity po) {
		this.name = name;
		processingOrg = po;
		mapping = new HashMap<String, Quote>();
		fieldsMapping = new HashMap<String, Map<String, String>>();
	}

	public FeedConfig(String name, FeedType feedType, LegalEntity po) {
		this(name, po);
		this.feedType = feedType;
	}

	public String getName() {
		return name;
	}

	public FeedType getFeedType() {
		return feedType;
	}

	public void setFeedType(FeedType feedType) {
		this.feedType = feedType;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Quote> getMapping() {
		return (Map<String, Quote>) TradistaModelUtil.deepCopy(mapping);
	}

	public void setMapping(Map<String, Quote> mapping) {
		this.mapping = mapping;
	}

	public LegalEntity getProcessingOrg() {
		return TradistaModelUtil.clone(processingOrg);
	}

	public Quote putAddress(String feedValue, Quote quote) {
		return mapping.put(feedValue, quote);
	}

	public String putField(String providerData, String providerField, String tradistaField) {
		Map<String, String> fldMapping = fieldsMapping.get(providerData);
		if (fldMapping == null) {
			fldMapping = new HashMap<String, String>();
		}
		return fldMapping.put(providerField, tradistaField);
	}

	public String toString() {
		return name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FeedConfig clone() {
		FeedConfig feedConfig = (FeedConfig) super.clone();
		feedConfig.mapping = (Map<String, Quote>) TradistaModelUtil.deepCopy(mapping);
		if (fieldsMapping != null) {
			Map<String, Map<String, String>> fieldsMapping = new HashMap<String, Map<String, String>>();
			for (Map.Entry<String, Map<String, String>> entry : this.fieldsMapping.entrySet()) {
				if (entry.getValue() != null) {
					fieldsMapping.put(entry.getKey(),
							(Map<String, String>) ((HashMap<String, String>) entry.getValue()).clone());
				}
			}
			feedConfig.fieldsMapping = fieldsMapping;
		}
		feedConfig.processingOrg = TradistaModelUtil.clone(processingOrg);
		return feedConfig;
	}

}