package org.eclipse.tradista.core.product.service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.product.model.Product;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class ProductBusinessDelegate {

	// Product types

	private static final String IR_COLLAR = "IRCollar";

	private static final String IR_FLOOR = "IRFloor";

	private static final String IR_CAP = "IRCap";

	private static final String FX_FORWARD = "FXForward";

	private static final String FX_SPOT = "FXSpot";

	private static final String DEPOSIT = "Deposit";

	private static final String LOAN = "Loan";

	private static final String FUTURE = "Future";

	private static final String EQUITY = "Equity";

	private static final String BOND = "Bond";

	private static final String SPECIFIC_REPO = "SpecificRepo";

	private static final String GC_REPO = "GCRepo";

	private static final String FRA = "FRA";

	private static final String CCY_SWAP = "CcySwap";

	private static final String LOAN_DEPOSIT = "LoanDeposit";

	private static final String IR_SWAP_OPTION = "IRSwapOption";

	private static final String IR_CAP_FLOOR_COLLAR = "IRCapFloorCollar";

	private static final String IR_SWAP = "IRSwap";

	private static final String FXNDF = "FXNDF";

	private static final String FX_OPTION = "FXOption";

	private static final String FX_SWAP = "FXSwap";

	private static final String EQUITY_OPTION = "EquityOption";

	private static final String FX_PRODUCT_TYPE = "FX";

	// Product families

	private static final String IR = "ir";

	private static final String FX_PRODUCT_FAMILY = "fx";

	private static final String MM = "mm";

	private static final String SECURITY = "security";

	private ProductService productService;

	private static Set<String> canBeOTC;

	private static Set<String> canBeListed;

	private static Set<String> allProductTypes;

	static {
		canBeOTC = new HashSet<>();
		canBeOTC.add(FX_PRODUCT_TYPE);
		canBeOTC.add(FX_SWAP);
		canBeOTC.add(FX_OPTION);
		canBeOTC.add(FXNDF);
		canBeOTC.add(IR_SWAP);
		canBeOTC.add(IR_CAP_FLOOR_COLLAR);
		canBeOTC.add(IR_SWAP_OPTION);
		canBeOTC.add(LOAN_DEPOSIT);
		canBeOTC.add(CCY_SWAP);
		canBeOTC.add(FRA);
		canBeOTC.add(EQUITY_OPTION);
		canBeOTC.add(GC_REPO);
		canBeOTC.add(SPECIFIC_REPO);

		canBeListed = new HashSet<>();
		canBeListed.add(EQUITY_OPTION);
		canBeListed.add(BOND);
		canBeListed.add(EQUITY);
		canBeListed.add(FUTURE);

		allProductTypes = new HashSet<>();
		allProductTypes.addAll(canBeOTC);
		allProductTypes.addAll(canBeListed);
	}

	public ProductBusinessDelegate() {
		productService = TradistaServiceLocator.getInstance().getProductService();
	}

	public Set<String> getAvailableProductTypes() {
		return SecurityUtil.run(() -> productService.getAvailableProductTypes());
	}

	public Set<String> getAllProductTypes() {
		return allProductTypes;
	}

	public Set<? extends Product> getAllProductsByType(String productType) throws TradistaBusinessException {
		if (StringUtils.isEmpty(productType)) {
			Set<Product> products = null;
			for (String type : getAvailableListableProductTypes()) {
				Set<? extends Product> prods = SecurityUtil.runEx(() -> productService.getAllProductsByType(type));
				if (prods != null && !prods.isEmpty()) {
					if (products == null) {
						products = new HashSet<>();
					}
					products.addAll(prods);
				}
			}
			return products;
		}
		return SecurityUtil.runEx(() -> productService.getAllProductsByType(productType));
	}

	public String getProductFamily(String productType) throws TradistaBusinessException {
		switch (productType) {
		case LOAN_DEPOSIT:
			return MM;
		case LOAN:
			return MM;
		case DEPOSIT:
			return MM;
		case BOND:
			return SECURITY;
		case FX_PRODUCT_TYPE:
			return FX_PRODUCT_FAMILY;
		case FX_SPOT:
			return FX_PRODUCT_FAMILY;
		case FX_FORWARD:
			return FX_PRODUCT_FAMILY;
		case FX_SWAP:
			return FX_PRODUCT_FAMILY;
		case FX_OPTION:
			return FX_PRODUCT_FAMILY;
		case FXNDF:
			return FX_PRODUCT_FAMILY;
		case IR_SWAP:
			return IR;
		case IR_SWAP_OPTION:
			return IR;
		case CCY_SWAP:
			return IR;
		case IR_CAP_FLOOR_COLLAR:
			return IR;
		case IR_CAP:
			return IR;
		case IR_FLOOR:
			return IR;
		case IR_COLLAR:
			return IR;
		case FRA:
			return IR;
		case FUTURE:
			return IR;
		case EQUITY:
			return SECURITY;
		case EQUITY_OPTION:
			return SECURITY;
		case GC_REPO:
			return SECURITY;
		case SPECIFIC_REPO:
			return SECURITY;
		}
		throw new TradistaBusinessException(String.format("This product type %s is not identified.", productType));
	}

	public Product getProductById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The product id must be positive.");
		}
		return SecurityUtil.run(() -> productService.getProductById(id));
	}

	public boolean canBeOTC(String productType) throws TradistaBusinessException {
		if (StringUtils.isEmpty(productType)) {
			throw new TradistaBusinessException("The product type is mandatory.");
		}
		return canBeOTC.contains(productType);
	}

	public boolean canBeListed(String productType) throws TradistaBusinessException {
		if (StringUtils.isEmpty(productType)) {
			throw new TradistaBusinessException("The product type is mandatory.");
		}
		return canBeListed.contains(productType);
	}

	public Set<? extends Product> getProducts(PositionDefinition posDef) throws TradistaBusinessException {
		if (posDef == null) {
			throw new TradistaBusinessException("The position definition is mandatory.");
		}
		if (posDef.getProduct() != null) {
			Set<Product> product = new HashSet<>();
			product.add(posDef.getProduct());
			return product;
		} else {
			return getAllProductsByType(posDef.getProductType());
		}
	}

	public Set<String> getAvailableListableProductTypes() {
		Set<String> productTypes = getAvailableProductTypes();
		Set<String> listableProductTypes = null;
		if (productTypes != null) {
			for (String p : productTypes) {
				try {
					if (canBeListed(p)) {
						if (listableProductTypes == null) {
							listableProductTypes = new HashSet<>();
						}
						listableProductTypes.add(p);
					}
				} catch (TradistaBusinessException tbe) {
					// cannot happen here because p won't be empty.
				}
			}
		}
		return listableProductTypes;
	}

	public Set<String> getAvailableFXProductTypes() {
		Set<String> productTypes = getAvailableProductTypes();
		return productTypes.stream().filter(p -> {
			try {
				return getProductFamily(p).equals(FX_PRODUCT_FAMILY);
			} catch (TradistaBusinessException tbe) {
			}
			return false;
		}).collect(Collectors.toSet());
	}

	public Set<Product> getAllProducts() {
		return SecurityUtil.run(() -> productService.getAllProducts());
	}

}