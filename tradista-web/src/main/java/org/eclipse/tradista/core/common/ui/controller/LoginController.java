package org.eclipse.tradista.core.common.ui.controller;

import java.io.Serializable;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.common.util.MathProperties;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.user.service.UserBusinessDelegate;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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
@ViewScoped
public class LoginController implements Serializable {

	private static final long serialVersionUID = -7912603586721092288L;

	private String login;

	private String password;

	private String originalUrl;

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	@PostConstruct
	public void init() {
		originalUrl = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
				.get("jakarta.servlet.forward.servlet_path");
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
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

	public String login() {
		final String FACES_REDIRECT_EQUALS_TRUE = "?faces-redirect=true";
		String url = "/pages/gcrepotrade" + FACES_REDIRECT_EQUALS_TRUE;
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		try {
			request.login(getLogin(), getPassword());
			ClientUtil.setCurrentUser(
					new UserBusinessDelegate().getUserByLogin(externalContext.getUserPrincipal().getName()));
			MathProperties.setUIDecimalFormat(
					configurationBusinessDelegate.getUIConfiguration(ClientUtil.getCurrentUser()).getDecimalFormat());
		} catch (ServletException se) {
			context.addMessage("errMsg", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login failed.", null));
			return null;
		} catch (TradistaBusinessException tbe) {
			// Cannot appear here.
		}
		if (originalUrl != null) {
			url = originalUrl + FACES_REDIRECT_EQUALS_TRUE;
		}

		return url;
	}
}