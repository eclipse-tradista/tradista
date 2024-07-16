package finance.tradista.core.pricing.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import finance.tradista.core.cashflow.model.CashFlow;
import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.pricing.pricer.Pricer;
import finance.tradista.core.pricing.pricer.PricerMeasure;
import finance.tradista.core.pricing.pricer.PricingParameter;
import jakarta.ejb.Remote;

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

@Remote
public interface PricerService {

	PricingParameter getPricingParameterById(long id);

	PricingParameter getPricingParameterByNameAndPoId(String name, long poId);

	Set<PricingParameter> getAllPricingParameters();

	long savePricingParameter(PricingParameter param) throws TradistaBusinessException;

	boolean deletePricingParameter(long id) throws TradistaBusinessException;

	Pricer getPricer(String product, PricingParameter pricingParameter) throws TradistaBusinessException;

	List<String> getAllPricingMethods(PricerMeasure pm);

	Set<String> getPricingParametersSetByQuoteSetId(long quoteSetId);

	List<CashFlow> generateCashFlows(long tradeId, PricingParameter pp, LocalDate valueDate)
			throws TradistaBusinessException;

	List<CashFlow> generateCashFlows(PricingParameter pp, LocalDate valueDate, long positionDefinitionId)
			throws TradistaBusinessException;

	List<CashFlow> generateAllCashFlows(PricingParameter pp, LocalDate valueDate) throws TradistaBusinessException;

}