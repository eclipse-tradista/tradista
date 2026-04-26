package org.eclipse.tradista.core.user.service;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.user.model.User;
import org.eclipse.tradista.core.user.persistence.UserSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class UserServiceBean implements UserService {

	@Interceptors(UserFilteringInterceptor.class)
	@Override
	public Set<User> getAllUsers() {
		return UserSQL.getAllUsers();
	}

	@Interceptors(UserFilteringInterceptor.class)
	@Override
	public long saveUser(User user) throws TradistaBusinessException {
		checkLoginExistence(user);
		if (user.getId() == 0) {
			checkIdentityExistence(user);
		} else {
			User oldUser = UserSQL.getUserById(user.getId());
			if (!oldUser.getFirstName().equals(user.getFirstName()) || !oldUser.getSurname().equals(user.getSurname())
					|| !oldUser.getProcessingOrg().equals(user.getProcessingOrg())) {
				checkIdentityExistence(user);
			}
		}
		return UserSQL.saveUser(user);
	}

	private void checkLoginExistence(User user) throws TradistaBusinessException {
		User u = UserSQL.getUserByLogin(user.getLogin());
		if (u != null && u.getId() != user.getId()) {
			throw new TradistaBusinessException(
					String.format("A user with the login '%s' already exists in the system.", user.getLogin()));
		}
	}

	private void checkIdentityExistence(User user) throws TradistaBusinessException {
		User u = UserSQL.getUserByFirstNameSurnameAndPoId(user.getFirstName(), user.getSurname(),
				user.getProcessingOrg().getId());
		if (u != null && u.getId() != user.getId()) {
			throw new TradistaBusinessException(
					String.format("A user with this first name (%s), surname (%s) and processing org (%s) already exists.",
							user.getFirstName(), user.getSurname(), user.getProcessingOrg()));
		}
	}
	
	@Interceptors(UserFilteringInterceptor.class)
	@Override
	public Set<User> getUsersBySurname(String surname) {
		return UserSQL.getUsersBySurname(surname);
	}

	@Interceptors(UserFilteringInterceptor.class)
	@Override
	public User getUserById(long id) {
		return UserSQL.getUserById(id);
	}

	// No interceptor as this method is the method used to authenticate the user in
	// UserFilteringInterceptor
	@Override
	public User getUserByLogin(String login) {
		return UserSQL.getUserByLogin(login);
	}

	@Interceptors(UserFilteringInterceptor.class)
	@Override
	public Set<User> getUsersByPoId(long poId) {
		return UserSQL.getUsersByPoId(poId);
	}

	@Interceptors(UserFilteringInterceptor.class)
	@Override
	public User getUserByFirstNameSurnameAndPoId(String firstName, String surname, long poId) {
		return UserSQL.getUserByFirstNameSurnameAndPoId(firstName, surname, poId);
	}

}