package org.eclipse.tradista.core.position.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

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

public class PositionDefinitionFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private BookBusinessDelegate bookBusinessDelegate;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private PositionDefinitionBusinessDelegate positionDefinitionBusinessDelegate;

	public PositionDefinitionFilteringInterceptor() {
		super();
		bookBusinessDelegate = new BookBusinessDelegate();
		pricerBusinessDelegate = new PricerBusinessDelegate();
		positionDefinitionBusinessDelegate = new PositionDefinitionBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0 && parameters[0] instanceof PositionDefinition) {
			PositionDefinition posDef = (PositionDefinition) parameters[0];
			StringBuilder errMsg = new StringBuilder();
			if (posDef.getId() != 0) {
				PositionDefinition pd = positionDefinitionBusinessDelegate.getPositionDefinitionById(posDef.getId());
				if (pd == null) {
					errMsg.append(
							String.format("The position definition %s was not found.%n", posDef.getBook().getName()));
				} else {
					if (pd.getProcessingOrg() == null) {
						errMsg.append(String.format(
								"This Position Definition %d is a global one and you are not allowed to delete it.",
								pd.getId()));
					}
				}
			}
			if (posDef.getProcessingOrg() != null
					&& !posDef.getProcessingOrg().equals(getCurrentUser().getProcessingOrg())) {
				errMsg.append(String.format("The processing org %s was not found.", posDef.getProcessingOrg()));
			}
			if (posDef.getBook() != null) {
				Book book = bookBusinessDelegate.getBookById(posDef.getBook().getId());
				if (book == null) {
					errMsg.append(String.format("The book %s was not found.%n", posDef.getBook().getName()));
				}
			}
			PricingParameter pp = pricerBusinessDelegate.getPricingParameterById(posDef.getPricingParameter().getId());
			if (pp == null) {
				errMsg.append(String.format("The pricing parameters set %s was not found.",
						posDef.getPricingParameter().getName()));
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			if (value instanceof Set) {
				Set<PositionDefinition> pds = (Set<PositionDefinition>) value;
				if (!pds.isEmpty()) {
					value = pds.stream()
							.filter(b -> (b.getProcessingOrg() == null)
									|| (b.getProcessingOrg().equals(getCurrentUser().getProcessingOrg())))
							.collect(Collectors.toSet());
				}
			}
			if (value instanceof PositionDefinition) {
				PositionDefinition posDef = (PositionDefinition) value;
				if ((posDef.getProcessingOrg() != null)
						&& (!posDef.getProcessingOrg().equals(getCurrentUser().getProcessingOrg()))) {
					value = null;
				}
			}
		}
		return value;
	}

}