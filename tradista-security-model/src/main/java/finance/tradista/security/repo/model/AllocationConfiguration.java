package org.eclipse.tradista.security.repo.model;

import java.util.Set;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public class AllocationConfiguration extends TradistaObject implements Comparable<AllocationConfiguration> {

	private static final long serialVersionUID = -1407477512383084643L;

	@Id
	private String name;

	@Id
	private LegalEntity processingOrg;

	private Set<Book> books;

	public AllocationConfiguration(String name, LegalEntity po) {
		this.name = name;
		this.processingOrg = po;
	}

	public String getName() {
		return name;
	}

	public LegalEntity getProcessingOrg() {
		return processingOrg;
	}

	@SuppressWarnings("unchecked")
	public Set<Book> getBooks() {
		return (Set<Book>) TradistaModelUtil.deepCopy(books);
	}

	public void setBooks(Set<Book> books) {
		this.books = books;
	}

	@Override
	public int compareTo(AllocationConfiguration ac) {
		return name.compareTo(ac.getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public AllocationConfiguration clone() {
		AllocationConfiguration allocConfig = (AllocationConfiguration) super.clone();
		allocConfig.books = ((Set<Book>) TradistaModelUtil.deepCopy(books));
		allocConfig.processingOrg = TradistaModelUtil.clone(processingOrg);
		return allocConfig;
	}

	@Override
	public String toString() {
		return name;
	}
}