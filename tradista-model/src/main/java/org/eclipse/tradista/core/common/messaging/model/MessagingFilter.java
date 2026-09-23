package org.eclipse.tradista.core.common.messaging.model;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public class MessagingFilter extends TradistaObject implements Comparable<MessagingFilter> {

	private static final long serialVersionUID = 1L;

	@Id
	private String name;

	private String className;

	public MessagingFilter(String name, String className) {
		this.name = name;
		this.className = className;
	}

	public MessagingFilter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public int compareTo(MessagingFilter o) {
		if (o == null) {
			return 1;
		}
		if (this.name == null) {
			return o.name == null ? 0 : -1;
		}
		return this.name.compareTo(o.name);
	}

	@Override
	public MessagingFilter clone() {
		return (MessagingFilter) super.clone();
	}

}
