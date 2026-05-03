package org.eclipse.tradista.core.product.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CREATION_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;

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

public class ProductSQL {

	private static final Field ID_FIELD = new Field(ID);
	private static final Field CREATION_DATE_FIELD = new Field(CREATION_DATE);
	private static final Field[] FIELDS = { ID_FIELD, CREATION_DATE_FIELD };
	private static final Table TABLE = new Table("PRODUCT", FIELDS);

	public static Product getProductById(long id) {
		Product product = null;

		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetProductById = con.prepareStatement(sql.toString())) {
			stmtGetProductById.setLong(1, id);
			try (ResultSet results = stmtGetProductById.executeQuery()) {
				while (results.next()) {
					if (product == null) {
						product = getProduct(id);
						if (product == null) {
							// The product was not found
							return null;
						}
					}
					product.setId(results.getLong(ID_FIELD.getName()));
					product.setCreationDate(results.getDate(CREATION_DATE_FIELD.getName()).toLocalDate());
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return product;
	}

	private static Product getProduct(long id) {
		ProductBusinessDelegate productBusinessDelegate = new ProductBusinessDelegate();
		Set<String> products = productBusinessDelegate.getAvailableProductTypes();
		try {
			for (String prod : products) {
				try {
					Product product = TradistaUtil.callMethod(
							"org.eclipse.tradista." + productBusinessDelegate.getProductFamily(prod) + "."
									+ prod.toLowerCase() + ".persistence." + prod + "SQL",
							Product.class, "get" + prod + "ById", id);
					if (product != null) {
						return product;
					}
				} catch (TradistaTechnicalException _) {
					// There is no product for this product type (ex: FX)
				}
			}
		} catch (TradistaBusinessException tbe) {
			throw new TradistaTechnicalException(tbe);
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public static Set<? extends Product> getAllProductsByType(String productType) {
		try {
			String productNameInMethod;
			Set<? extends Product> products = null;
			if (productType.equals("Equity")) {
				productNameInMethod = productType.substring(0, productType.length() - 1) + "ies";
			} else {
				productNameInMethod = productType + "s";
			}
			try {
				products = TradistaUtil.callMethod(
						"org.eclipse.tradista." + new ProductBusinessDelegate().getProductFamily(productType) + "."
								+ productType.toLowerCase() + ".persistence." + productType + "SQL",
						Set.class, "getAll" + productNameInMethod);
			} catch (TradistaTechnicalException _) {
				// There is no product for this product type (ex: FX)
				return null;
			}

			return products;

		} catch (TradistaBusinessException tbe) {
			throw new TradistaTechnicalException(tbe);
		}
	}

	public static String getProductTableByType(String productType) {
		switch (productType) {
		case "Bond":
			return "BOND";
		case "Equity":
			return "EQUITY";
		case "EquityOption":
			return "EQUITY_OPTION";
		case "Future":
			return "FUTURE";
		default:
			return null;
		}
	}

}