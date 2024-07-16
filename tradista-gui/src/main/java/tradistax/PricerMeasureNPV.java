package tradistax;

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
//package azurx;
//
//import java.math.BigDecimal;
//
//import com.azur.bonds.model.Bond;
//import com.azur.core.currency.model.Currency;
//import com.azur.core.marketdata.Curve;
//import com.azur.core.marketdata.InterestRateCurveBusinessDelegate;
//import com.azur.core.pricer.PricerMeasure;
//import com.azur.core.pricer.Pricing;
//import com.azur.core.pricer.PricingParameter;
//import com.azur.core.pricer.exception.PricerException;
//import com.azur.core.product.Product;
//import com.azur.core.trade.Trade;
//
//public class PricerMeasureNPV extends PricerMeasure {
//	
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 3638413117741181582L;
//
//	@Pricing
//	public BigDecimal discountedCashFlow(PricingParameter params, Trade<Product> trade, Currency currency) throws PricerException {
//	
//	
//	//First, sum all the discounted coupons
//	Bond bond = (Bond) trade.getProduct();
//	String curveRef = bond.getCurrency().getIsoCode() + ".IRCurve";
//	String curveName = params.getParams().get(curveRef);
//	if (curveName == null) {
//		throw new PricerException(params.getName() + " Pricing Parameter doesn't contain a ' "+ curveRef +"' value. please add it or change the Pricing Parameter.");
//	}
//	else {
//		Curve<?, ?> curve = new InterestRateCurveBusinessDelegate().getInterestRateCurveByName(curveName);
//		if (curve == null) {
//			throw new PricerException("The '" + curveName + "' curve does not exist.");
//		}
//		
//	}
//	BigDecimal discountedCoupons;// = PricerBondUtil.discountCoupons(null, curveName, bond, trade.getTradeDate());
//	
//	// Secondly, subtract the price paid
//	//discountedCoupons = discountedCoupons.subtract(((BondTrade<?>)trade).getAmount());
//	
//	if (trade.isSell()) {
//		//discountedCoupons = discountedCoupons.negate();
//	}
//	
//	// Finally, convert in pricing currency
//	//discountedCoupons = PricerUtil.convertAmount(discountedCoupons, trade.getCurrency(), currency, trade.getTradeDate());
//	
//	return null;
//		
//		
//	}	
//
//	public String toString() {
//		return "NPV";
//	}
//
//}