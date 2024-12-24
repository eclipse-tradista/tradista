package org.eclipse.tradista.web.demo;

import java.io.Serializable;

import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.user.service.UserBusinessDelegate;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

/********************************************************************************
 * Copyright (c) 2022 Olivier Asuncion
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

@Named
@RequestScoped
public class LoginDemoController implements Serializable {

	private static final long serialVersionUID = -7912603586721092288L;

	private String login;

	private String password;

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

	public String login() {

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		try {
			request.login(getLogin(), getPassword());
			ClientUtil.setCurrentUser(
					new UserBusinessDelegate().getUserByLogin(externalContext.getUserPrincipal().getName()));
		} catch (ServletException se) {
			context.addMessage(null, new FacesMessage("Login failed " + se));
			return null;
		}

		return "/pages/dashboard.xhtml?faces-redirect=true";
	}
}