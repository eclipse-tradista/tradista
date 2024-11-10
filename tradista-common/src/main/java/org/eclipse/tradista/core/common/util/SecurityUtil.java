package org.eclipse.tradista.core.common.util;

import java.util.function.Supplier;

import javax.security.sasl.SaslException;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.jboss.ejb.client.RequestSendFailedException;
import org.wildfly.common.function.ExceptionConsumer;
import org.wildfly.common.function.ExceptionSupplier;
import org.wildfly.security.auth.client.AuthenticationConfiguration;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.MatchRule;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.password.interfaces.ClearPassword;
import org.wildfly.security.sasl.SaslMechanismSelector;

import jakarta.ejb.EJBException;
import jakarta.ejb.NoSuchEJBException;

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

public final class SecurityUtil {

	private static AuthenticationContext authenticationContext;

	private static Credential credential;

	public static final String CURRENT_USER = "CURRENT_USER";

	static {
		authenticationContext = AuthenticationContext.captureCurrent();
	}

	@FunctionalInterface
	public interface ExRunnable {
		void runEx() throws TradistaBusinessException;
	}

	public static <T, E extends TradistaBusinessException> T runEx(ExceptionSupplier<T, E> consumer) throws E {
		try {
			return authenticationContext.runAsSupplierEx(consumer);
		} catch (EJBException ejbe) {
			Throwable suppressedException = ejbe.getSuppressed()[0];
			if (suppressedException != null) {
				if (suppressedException instanceof RequestSendFailedException) {
					Throwable supprCauseException = suppressedException.getCause();
					if (supprCauseException != null) {
						if (supprCauseException instanceof SaslException) {
							// TODO Add Log error
							supprCauseException.printStackTrace();
							throw new TradistaTechnicalException("Logon denied.");
						}
					}
				}
			}
			throw new TradistaTechnicalException(ejbe);
		}
	}

	public static <T> T run(Supplier<T> supplier) {
		try {
			return authenticationContext.runAsSupplier(supplier);
		} catch (RequestSendFailedException rsfe) {
			Throwable suppressedException = rsfe.getSuppressed()[0];
			if (suppressedException != null) {
				if (suppressedException instanceof RequestSendFailedException) {
					Throwable supprCauseException = suppressedException.getCause();
					if (supprCauseException != null) {
						if (supprCauseException instanceof SaslException) {
							// TODO Add Log error
							supprCauseException.printStackTrace();
							throw new TradistaTechnicalException("Logon denied.");
						}
					}
				}
			}
			throw new TradistaTechnicalException(rsfe);
		} catch (NoSuchEJBException nsee) { // I could see that NoSuchEJBException is thrown instead of
			// RequestSendFailedException when Business Delegates are invoked from
			// server side
			throw new TradistaTechnicalException(nsee);
		}
	}

	public static void run(Runnable runnable) {
		try {
			authenticationContext.run(runnable);
		} catch (RequestSendFailedException rsfe) {
			Throwable suppressedException = rsfe.getSuppressed()[0];
			if (suppressedException != null) {
				if (suppressedException instanceof RequestSendFailedException) {
					Throwable supprCauseException = suppressedException.getCause();
					if (supprCauseException != null) {
						if (supprCauseException instanceof SaslException) {
							// TODO Add Log error
							supprCauseException.printStackTrace();
							throw new TradistaTechnicalException("Logon denied.");
						}
					}
				}
			}
			throw new TradistaTechnicalException(rsfe);
		} catch (NoSuchEJBException nsee) { // I could see that NoSuchEJBException is thrown instead of
			// RequestSendFailedException when Business Delegates are invoked from
			// server side
			throw new TradistaTechnicalException(nsee);
		}
	}

	private static ExceptionConsumer<Void, TradistaBusinessException> toExConsumer(ExRunnable exRunnable) {
		try {
			return (v) -> exRunnable.runEx();
		} catch (RequestSendFailedException rsfe) {
			Throwable suppressedException = rsfe.getSuppressed()[0];
			if (suppressedException != null) {
				if (suppressedException instanceof RequestSendFailedException) {
					Throwable supprCauseException = suppressedException.getCause();
					if (supprCauseException != null) {
						if (supprCauseException instanceof SaslException) {
							// TODO Add Log error
							supprCauseException.printStackTrace();
							throw new TradistaTechnicalException("Logon denied.");
						}
					}
				}
			}
			throw new TradistaTechnicalException(rsfe);
		} catch (NoSuchEJBException nsee) { // I could see that NoSuchEJBException is thrown instead of
			// RequestSendFailedException when Business Delegates are invoked from
			// server side
			throw new TradistaTechnicalException(nsee);
		}
	}

	public static <V> void runEx(final ExRunnable exRunnable) throws TradistaBusinessException {
		try {
			authenticationContext.runExConsumer(toExConsumer(exRunnable), null);
		} catch (RequestSendFailedException rsfe) {
			Throwable suppressedException = rsfe.getSuppressed()[0];
			if (suppressedException != null) {
				if (suppressedException instanceof RequestSendFailedException) {
					Throwable supprCauseException = suppressedException.getCause();
					if (supprCauseException != null) {
						if (supprCauseException instanceof SaslException) {
							// TODO Add Log error
							supprCauseException.printStackTrace();
							throw new TradistaTechnicalException("Logon denied.");
						}
					}
				}
			}
			throw new TradistaTechnicalException(rsfe);
		} catch (NoSuchEJBException nsee) { // I could see that NoSuchEJBException is thrown instead of
											// RequestSendFailedException when Business Delegates are invoked from
											// server side
			throw new TradistaTechnicalException(nsee);
		}
	}

	public static void setCredential(String login, String password) {
		credential = new PasswordCredential(
				ClearPassword.createRaw(ClearPassword.ALGORITHM_CLEAR, password.toCharArray()));
		AuthenticationConfiguration adminConfig;
		adminConfig = AuthenticationConfiguration.empty()
				.setSaslMechanismSelector(SaslMechanismSelector.NONE.addMechanism("PLAIN")).useName(login)
				.useCredential(credential);
		authenticationContext = AuthenticationContext.empty();
		authenticationContext = authenticationContext.with(MatchRule.ALL, adminConfig);
	}

}