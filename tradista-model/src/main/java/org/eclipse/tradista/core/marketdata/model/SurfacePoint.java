package org.eclipse.tradista.core.marketdata.model;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

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

public class SurfacePoint<X extends Number, Y extends Number, Z extends Number> extends TradistaObject
		implements Comparable<SurfacePoint<X, Y, Z>> {

	private static final long serialVersionUID = -3013306908811949446L;

	@Id
	private X xAxis;

	@Id
	private Y yAxis;

	@Id
	private Z zAxis;

	public SurfacePoint(X x, Y y, Z z) {
		this.xAxis = x;
		this.yAxis = y;
		this.zAxis = z;
	}

	public X getxAxis() {
		return xAxis;
	}

	public Y getyAxis() {
		return yAxis;
	}

	public Z getzAxis() {
		return zAxis;
	}

	@Override
	public int compareTo(SurfacePoint<X, Y, Z> o) {
		return (xAxis.toString() + yAxis.toString() + zAxis.toString())
				.compareTo((o.xAxis.toString() + o.yAxis.toString() + o.zAxis.toString()));
	}

}