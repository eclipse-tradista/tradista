package org.eclipse.tradista.core.user.model;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

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
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

public class User extends TradistaObject {

	private static final long serialVersionUID = 7677381398422965047L;

	@Id
	private String firstName;

	@Id
	private String surname;

	@Id
	private LegalEntity processingOrg;

	public User(String firstName, String surname, LegalEntity processingOrg) {
		this.firstName = firstName;
		this.surname = surname;
		this.processingOrg = processingOrg;
	}

	private String login;

	private String password;

	public String getFirstName() {
		return firstName;
	}

	public String getSurname() {
		return surname;
	}

	public LegalEntity getProcessingOrg() {
		return TradistaModelUtil.clone(processingOrg);
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAdmin() {
		return processingOrg == null;
	}

	@Override
	public User clone() {
		User user = (User) super.clone();
		user.processingOrg = TradistaModelUtil.clone(processingOrg);
		return user;
	}

}