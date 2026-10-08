package org.eclipse.tradista.core.marketdata.ui.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

/**
 * Common property class representing a point on a Volatility Surface table.
 * Corresponds to a SurfacePoint(xAxis, yAxis, zAxis).
 */
public class SurfacePointProperty {

	private final StringProperty xAxis;
	private final StringProperty yAxis;
	private final StringProperty zAxis;

	public SurfacePointProperty(String xAxis, String yAxis, String zAxis) {
		this.xAxis = new SimpleStringProperty(xAxis);
		this.yAxis = new SimpleStringProperty(yAxis);
		this.zAxis = new SimpleStringProperty(zAxis);
	}

	public StringProperty xAxisProperty() {
		return xAxis;
	}

	public String getXAxis() {
		return xAxis.get();
	}

	public void setXAxis(String xAxis) {
		this.xAxis.set(xAxis);
	}

	public StringProperty yAxisProperty() {
		return yAxis;
	}

	public String getYAxis() {
		return yAxis.get();
	}

	public void setYAxis(String yAxis) {
		this.yAxis.set(yAxis);
	}

	public StringProperty zAxisProperty() {
		return zAxis;
	}

	public String getZAxis() {
		return zAxis.get();
	}

	public void setZAxis(String zAxis) {
		this.zAxis.set(zAxis);
	}

	// Semantic accessors for Option Expiry (X axis)
	public StringProperty optionExpiryProperty() {
		return xAxis;
	}

	public String getOptionExpiry() {
		return xAxis.get();
	}

	public void setOptionExpiry(String optionExpiry) {
		this.xAxis.set(optionExpiry);
	}

	// Semantic accessors for Volatility (Z axis)
	public StringProperty volatilityProperty() {
		return zAxis;
	}

	public String getVolatility() {
		return zAxis.get();
	}

	public void setVolatility(String volatility) {
		this.zAxis.set(volatility);
	}

	// Semantic accessors for Delta (Y axis)
	public StringProperty deltaProperty() {
		return yAxis;
	}

	public String getDelta() {
		return yAxis.get();
	}

	public void setDelta(String delta) {
		this.yAxis.set(delta);
	}

	public void getDelta(String delta) {
		this.yAxis.set(delta);
	}

	// Semantic accessors for Swap Length (Y axis)
	public StringProperty swapLengthProperty() {
		return yAxis;
	}

	public String getSwapLength() {
		return yAxis.get();
	}

	public void setSwapLength(String swapLength) {
		this.yAxis.set(swapLength);
	}

	public void getSwapLength(String swapLength) {
		this.yAxis.set(swapLength);
	}

	// Semantic accessors for Strike (Y axis)
	public StringProperty strikeProperty() {
		return yAxis;
	}

	public String getStrike() {
		return yAxis.get();
	}

	public void setStrike(String strike) {
		this.yAxis.set(strike);
	}
}
