package org.eclipse.tradista.core.pricing.pricer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/********************************************************************************
 * Copyright (c) 2014 Olivier Asuncion
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

public abstract class Pricer implements Serializable {

	private static final long serialVersionUID = -6862711764817078759L;

	private List<PricerMeasure> pricerMeasures;

	private List<PricerMeasure> productPricerMeasures;

	public Pricer() {
		pricerMeasures = new ArrayList<>();
		productPricerMeasures = new ArrayList<>();
	}

	public List<PricerMeasure> getPricerMeasures() {
		return pricerMeasures;
	}

	public List<PricerMeasure> getProductPricerMeasures() {
		return productPricerMeasures;
	}
}