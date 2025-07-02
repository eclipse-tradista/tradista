package org.eclipse.tradista.core.marketdata.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

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

public abstract class Curve<X, Y> extends TradistaObject implements MarketData {

	private static final long serialVersionUID = -2297522690673041454L;

	protected Map<X, Y> points;

	@Id
	private String name;

	@Id
	private LegalEntity processingOrg;

	public Curve(String name, LegalEntity po) {
		this.name = name;
		processingOrg = po;
	}

	public String getName() {
		return name;
	}

	public Map<X, Y> getPoints() {
		if (points == null) {
			return null;
		}
		return new HashMap<>(points);
	}

	public void setPoints(Map<X, Y> points) {
		this.points = points;
	}

	public LegalEntity getProcessingOrg() {
		return TradistaModelUtil.clone(processingOrg);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Curve<X, Y> clone() {
		Curve<X, Y> curve = (Curve<X, Y>) super.clone();
		curve.processingOrg = TradistaModelUtil.clone(processingOrg);
		return curve;
	}

	public String toString() {
		return name;
	}

}