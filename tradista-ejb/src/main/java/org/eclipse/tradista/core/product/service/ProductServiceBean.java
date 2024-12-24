package org.eclipse.tradista.core.product.service;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.persistence.ProductSQL;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

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
public class ProductServiceBean implements ProductService {

	@Override
	public Set<String> getAvailableProductTypes() {
		Set<String> products = null;
		Properties prop = new Properties();
		InputStream in = TradeSQL.class.getResourceAsStream("/META-INF/products.properties");
		try {
			prop.load(in);
			in.close();
			for (Object product : prop.keySet()) {
				if (products == null) {
					products = new HashSet<String>();
				}
				products.add((String) product);
			}
		} catch (Exception e) {
		}
		return products;
	}

	@Override
	public Product getProductById(long id) {
		return ProductSQL.getProductById(id);
	}

	@Override
	public Set<? extends Product> getAllProductsByType(String productType) throws TradistaBusinessException {
		if (!getAvailableProductTypes().contains(productType)) {
			throw new TradistaBusinessException(String.format("The product type '%s' is not recognized", productType));
		}
		return ProductSQL.getAllProductsByType(productType);
	}

	@Override
	public Set<Product> getAllProducts() {
		Set<Product> allProducts = null;
		Set<String> listableProductTypes = new ProductBusinessDelegate().getAvailableListableProductTypes();
		if (listableProductTypes != null & !listableProductTypes.isEmpty()) {
			allProducts = new HashSet<Product>();
			for (String productType : listableProductTypes) {
				try {
					Set<? extends Product> products = getAllProductsByType(productType);
					if (products != null && !products.isEmpty()) {
						allProducts.addAll(products);
					}
				} catch (TradistaBusinessException tbe) {
					// Should not happen here.
				}
			}
		}
		return allProducts;
	}

}