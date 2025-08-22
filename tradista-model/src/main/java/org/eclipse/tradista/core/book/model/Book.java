package org.eclipse.tradista.core.book.model;

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
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

public class Book extends TradistaObject {

	private static final long serialVersionUID = 8145550319443892466L;

	@Id
	private String name;

	@Id
	private LegalEntity processingOrg;

	private String description;

	public Book(String name, LegalEntity processingOrg) {
		super();
		this.name = name;
		this.processingOrg = processingOrg;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LegalEntity getProcessingOrg() {
		return TradistaModelUtil.clone(processingOrg);
	}

	@Override
	public Book clone() {
		Book book = (Book) super.clone();
		book.processingOrg = TradistaModelUtil.clone(processingOrg);
		return book;
	}

	@Override
	public String toString() {
		return name;
	}

}