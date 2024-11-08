import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.user.service.UserBusinessDelegate;

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

public class FXTester {

	public static void main(String[] args) {

		try {
			new UserBusinessDelegate().login("oli", "oli");
		} catch (TradistaBusinessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// 1. Save the formulas
		// Formula formula1 = new Formula("ExpectedBenefit = 0.2.");
//		//FolFormula formula2 = new FolFormula(
//		//		"forall X: (forall Y: (forall Z: !(NPVFX(X,Y,Z) > ExpectedBenefit) || BUYFX(X,Y,Z)))");
		// Formula formula2 = new Formula(
		// "NPVFX(X,Y,Z) > ExpectedBenefit :- BUYFX(X,Y,Z).");
		// new FormulaBusinessDelegate().saveFormulas(formula1,
		// formula2);

		// 3. Save the book

		// Book book = new Book();
		// book.setName("TEST_AI");
		// try {
		// book.setProcessingOrg(new
		// LegalEntityBusinessDelegate().getLegalEntityById(2));
		// } catch (TradistaBusinessException e1) {
		// TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// new BookBusinessDelegate().saveBook(book);

		// 3. Save the mandate

//		Mandate mandate = new Mandate();
//		mandate.setAcceptedRiskLevel(Mandate.RiskLevel.AVERAGE);
//		try {
//			mandate.setBook(new BookBusinessDelegate().getBookByName("TEST_AI"));
//		} catch (TradistaBusinessException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		Map<String, Mandate.Allocation> allocMap = new HashMap<String, Mandate.Allocation>();
//		Mandate.Allocation alloc = mandate.new Allocation();
//		alloc.setMinAllocation((short) 100);
//		alloc.setMaxAllocation((short) 100);
//		allocMap.put("FXForward", alloc);
//		Map<String, Mandate.Allocation> currAlloc = new HashMap<String, Mandate.Allocation>();
//		Mandate.Allocation eurAlloc = mandate.new Allocation();
//		eurAlloc.setMinAllocation((short) 20);
//		eurAlloc.setMaxAllocation((short) 80);
//		currAlloc.put("EUR", eurAlloc);
//		Mandate.Allocation usdAlloc = mandate.new Allocation();
//		usdAlloc.setMinAllocation((short) 20);
//		usdAlloc.setMaxAllocation((short) 80);
//		currAlloc.put("USD", usdAlloc);
//		mandate.setProductTypeAllocations(allocMap);
//		mandate.setCurrencyAllocations(currAlloc);
//		mandate.setInitialCashAmount(BigDecimal.valueOf(100));
//		Currency currency = null;
//		try {
//			currency = new CurrencyBusinessDelegate().getCurrencyByIsoCode("USD");
//		} catch (TradistaBusinessException e) { // TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		mandate.setInitialCashCurrency(currency);
//		mandate.setName("TEST AI");
//		mandate.setStartDate(LocalDate.now());
//		mandate.setEndDate(LocalDate.of(2021, 12, 31));
//
//		try {
//			new MandateBusinessDelegate().saveMandate(mandate);
//		} catch (TradistaBusinessException e) { // TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// 4. Save the QS
//		QuoteSet qs;
//		try {
//			qs = new QuoteSet("TEST_AI", new LegalEntityBusinessDelegate().getLegalEntityById(2));
//			new QuoteBusinessDelegate().saveQuoteSet(qs);
//		} catch (TradistaBusinessException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		// 5. Save the pricing parameter
//		PricingParameter pp;
//		try {
//			pp = new PricingParameter("TEST_AI", new QuoteBusinessDelegate().getQuoteSetByName("TEST_AI"), new LegalEntityBusinessDelegate().getLegalEntityById(2));
//			new PricerBusinessDelegate().savePricingParameter(pp);
//		} catch (TradistaBusinessException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		// 6. Save the agent
//		AssetManagerAgent agent = new AssetManagerAgent();
//		agent.setName("TestFX");
//		try {
//			agent.setMandate(new MandateBusinessDelegate().getMandateById(2));
//		} catch (TradistaBusinessException e) { // TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		agent.setOnlyInformative(false);
//		agent.setPricingParameter(new PricerBusinessDelegate().getPricingParameterById(1));
//		agent.setStarted(true);
//		try {
//			new AssetManagerAgentBusinessDelegate().saveAssetManagerAgent(agent);
//		} catch (TradistaBusinessException e) { // TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// Formula formula1;
		// try {
//			FolSignature sig = new FolSignature();
//			Sort decimal = new Sort("Decimal");
//			sig.add(decimal);
//			Constant expBen = new Constant("ExpectedBenefit", decimal);
//			sig.add(expBen);
//			Sort currency = new Sort("Currency");
//			sig.add(currency);
//			Sort date = new Sort("Date");
//			sig.add(date);
//			Constant eur = new Constant("EUR", currency);
//			sig.add(eur);
//			Constant usd = new Constant("USD", currency);
//			sig.add(usd);
//			Constant brl = new Constant("BRL", currency);
//			sig.add(brl);
//			List<Sort> npvArgs = new ArrayList<Sort>(3);
//			npvArgs.add(currency);
//			npvArgs.add(currency);
//			npvArgs.add(date);
//			Functor npv = new Functor("NPVFX", npvArgs, decimal);
//			Predicate buyFx = new Predicate("BUYFX", npvArgs);
//			sig.add(npv);
//			sig.add(buyFx);
		// formula1 = new Formula("ExpectedBenefit = 0.2.");
//			//FolFormula formula2 = new FolFormula(
//			//		"forall X: (forall Y: (forall Z: !(NPVFX(X,Y,Z) > ExpectedBenefit) || BUYFX(X,Y,Z)))");
		// Formula formula2 = new Formula(
		// "NPVFX(X,Y,Z) > ExpectedBenefit :- BUYFX(X,Y,Z).");
		// new FormulaBusinessDelegate().saveFormulas(formula1, formula2);
		// new BookBusinessDelegate().getAllBooks();
		// } catch (TradistaBusinessException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

}