package org.eclipse.tradista.core.position.service;

import java.util.Set;

import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.CheckProcessingOrg;
import org.eclipse.tradista.core.common.service.ProtectGlobal;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.persistence.PositionDefinitionSQL;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import org.eclipse.tradista.core.trade.service.ProductScope;
import org.eclipse.tradista.core.trade.service.ProductScopeMode;
import org.eclipse.tradista.core.user.model.User;
import org.eclipse.tradista.core.user.service.UserBusinessDelegate;
import org.eclipse.tradista.legalentity.service.LegalEntityBusinessDelegate;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJBContext;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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
public class PositionDefinitionServiceBean implements LocalPositionDefinitionService, PositionDefinitionService {

	@Resource
	private EJBContext ctx;

	private UserBusinessDelegate userBusinessDelegate;

	private BookBusinessDelegate bookBusinessDelegate;

	private ProductBusinessDelegate productBusinessDelegate;

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	private CurrencyBusinessDelegate currencyBusinessDelegate;

	private PricerBusinessDelegate pricerBusinessDelegate;

	@PostConstruct
	private void init() {
		userBusinessDelegate = new UserBusinessDelegate();
		bookBusinessDelegate = new BookBusinessDelegate();
		productBusinessDelegate = new ProductBusinessDelegate();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
		currencyBusinessDelegate = new CurrencyBusinessDelegate();
		pricerBusinessDelegate = new PricerBusinessDelegate();
	}

	@Override
	public Set<PositionDefinition> getAllPositionDefinitions() {
		return PositionDefinitionSQL.getAllPositionDefinitions();
	}

	@Override
	public PositionDefinition getPositionDefinitionByName(String name) {
		return PositionDefinitionSQL.getPositionDefinitionByNameAndPoId(name,
				getCurrentUser().getProcessingOrg() == null ? 0 : getCurrentUser().getProcessingOrg().getId());
	}

	@Override
	public PositionDefinition getPositionDefinitionById(long id) {
		return PositionDefinitionSQL.getPositionDefinitionById(id);
	}

	@ProtectGlobal
	@ProductScope(mode = ProductScopeMode.ON_CREATION)
	@Override
	public long savePositionDefinition(@CheckPositionDefinitionAccess PositionDefinition positionDefinition)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		checkBook(positionDefinition, errMsg);
		checkCounterparty(positionDefinition, errMsg);
		checkProduct(positionDefinition, errMsg);
		checkCurrency(positionDefinition, errMsg);
		checkPricingParameter(positionDefinition, errMsg);

		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		if (positionDefinition.getId() == 0) {
			checkPositionDefinitionName(positionDefinition);
		} else {
			PositionDefinition oldPositionDefinition = PositionDefinitionSQL
					.getPositionDefinitionById(positionDefinition.getId());
			if (!oldPositionDefinition.getName().equals(positionDefinition.getName())) {
				checkPositionDefinitionName(positionDefinition);
			}
		}
		return PositionDefinitionSQL.savePositionDefinition(positionDefinition);
	}

	private void checkPositionDefinitionName(PositionDefinition positionDefinition) throws TradistaBusinessException {
		if (PositionDefinitionSQL.getPositionDefinitionByNameAndPoId(positionDefinition.getName(),
				positionDefinition.getProcessingOrg() == null ? 0
						: positionDefinition.getProcessingOrg().getId()) != null) {
			throw new TradistaBusinessException(
					String.format("A position definition named %s already exists for this Processing Org.",
							positionDefinition.getName()));
		}
	}

	@Override
	public boolean deletePositionDefinition(String name) {
		return PositionDefinitionSQL.deletePositionDefinition(name, getCurrentUser().getProcessingOrg().getId());
	}

	private User getCurrentUser() {
		User user = null;
		if (ctx.getContextData().get(SecurityUtil.CURRENT_USER) == null) {
			try {
				user = userBusinessDelegate.getUserByLogin(ctx.getCallerPrincipal().getName());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
			ctx.getContextData().put(SecurityUtil.CURRENT_USER, user);
		} else {
			user = (User) ctx.getContextData().get(SecurityUtil.CURRENT_USER);
		}
		return user;
	}

	@Override
	public Set<PositionDefinition> getAllRealTimePositionDefinitions() {
		return PositionDefinitionSQL.getAllRealTimePositionDefinitions();
	}

	@Override
	public Set<String> getPositionDefinitionsByPricingParametersSetId(long id) {
		return PositionDefinitionSQL.getPositionDefinitionsByPricingParametersSetId(id);
	}

	@Override
	public Set<PositionDefinition> getPositionDefinitionsByPoId(@CheckProcessingOrg long poId) {
		return PositionDefinitionSQL.getPositionDefinitionsByPoId(poId);
	}

	private void checkBook(PositionDefinition positionDefinition, StringBuilder errMsg)
			throws TradistaBusinessException {
		if (positionDefinition.getBook() != null) {
			if (bookBusinessDelegate.getBookById(positionDefinition.getBook().getId()) == null) {
				errMsg.append(String.format("The book %s was not found.%n", positionDefinition.getBook()));
			}
		}
	}

	private void checkProduct(PositionDefinition positionDefinition, StringBuilder errMsg)
			throws TradistaBusinessException {
		if (positionDefinition.getProduct() != null) {
			if (productBusinessDelegate.getProductById(positionDefinition.getProduct().getId()) == null) {
				errMsg.append(String.format("The product %s was not found.%n", positionDefinition.getProduct()));
			}
		}
	}

	private void checkCounterparty(PositionDefinition positionDefinition, StringBuilder errMsg)
			throws TradistaBusinessException {
		if (positionDefinition.getCounterparty() != null) {
			if (legalEntityBusinessDelegate.getLegalEntityById(positionDefinition.getCounterparty().getId()) == null) {
				errMsg.append(
						String.format("The counterparty %s was not found.%n", positionDefinition.getCounterparty()));
			}
		}
	}

	private void checkCurrency(PositionDefinition positionDefinition, StringBuilder errMsg)
			throws TradistaBusinessException {
		if (positionDefinition.getCurrency() != null) {
			if (currencyBusinessDelegate.getCurrencyById(positionDefinition.getCurrency().getId()) == null) {
				errMsg.append(String.format("The currency %s was not found.%n", positionDefinition.getCurrency()));
			}
		}
	}

	private void checkPricingParameter(PositionDefinition positionDefinition, StringBuilder errMsg)
			throws TradistaBusinessException {
		if (positionDefinition.getPricingParameter() != null) {
			if (pricerBusinessDelegate
					.getPricingParameterById(positionDefinition.getPricingParameter().getId()) == null) {
				errMsg.append(String.format("The pricing parameters set %s was not found.%n",
						positionDefinition.getPricingParameter()));
			}
		}
	}
}