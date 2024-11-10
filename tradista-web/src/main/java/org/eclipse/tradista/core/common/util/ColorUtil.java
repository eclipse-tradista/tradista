package org.eclipse.tradista.core.common.util;

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
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

import java.util.ArrayList;
import java.util.List;

import software.xdev.chartjs.model.color.RGBAColor;

public final class ColorUtil {

	private ColorUtil() {
	}

	/**
	 * Returns a series of shade of blue colors see
	 * https://www.infoworld.com/article/2074744/styling-javafx-pie-chart-with-css.html
	 * 
	 * @return a series of shade of blue colors
	 */
	public static List<String> getBlueColorsAsStringList() {
		List<String> colors = new ArrayList<>();
		String turquoise = "rgb(64,224,208)";
		String aquamarine = "rgb(127,255,212)";
		String cornflowerblue = "rgb(100,149,237)";
		String blue = "rgb(0,0,255)";
		String cadetblue = "rgb(95,158,160)";
		String navy = "rgb(0,0,128)";
		String deepskyblue = "rgb(0,191,255)";
		String cyan = "rgb(0,255,255)";
		String steelblue = "rgb(70,130,180)";
		String teal = "rgb(0,128,128)";
		String royalblue = "rgb(65,105,225)";
		String dodgerblue = "rgb(30,144,255)";
		colors.add(turquoise);
		colors.add(aquamarine);
		colors.add(cornflowerblue);
		colors.add(blue);
		colors.add(cadetblue);
		colors.add(navy);
		colors.add(deepskyblue);
		colors.add(cyan);
		colors.add(steelblue);
		colors.add(teal);
		colors.add(royalblue);
		colors.add(dodgerblue);
		return colors;
	}

	/**
	 * Returns a series of shade of blue colors see
	 * https://www.infoworld.com/article/2074744/styling-javafx-pie-chart-with-css.html
	 * 
	 * @return a series of shade of blue colors
	 */
	public static List<RGBAColor> getBlueColorsList() {
		List<RGBAColor> colors = new ArrayList<>();
		RGBAColor turquoise = new RGBAColor(64, 224, 208);
		RGBAColor aquamarine = new RGBAColor(127, 255, 212);
		RGBAColor cornflowerblue = new RGBAColor(100, 149, 237);
		RGBAColor blue = new RGBAColor(0, 0, 255);
		RGBAColor cadetblue = new RGBAColor(95, 158, 160);
		RGBAColor navy = new RGBAColor(0, 0, 128);
		RGBAColor deepskyblue = new RGBAColor(0, 191, 255);
		RGBAColor cyan = new RGBAColor(0, 255, 255);
		RGBAColor steelblue = new RGBAColor(70, 130, 180);
		RGBAColor teal = new RGBAColor(0, 128, 128);
		RGBAColor royalblue = new RGBAColor(65, 105, 225);
		RGBAColor dodgerblue = new RGBAColor(30, 144, 255);
		colors.add(turquoise);
		colors.add(aquamarine);
		colors.add(cornflowerblue);
		colors.add(blue);
		colors.add(cadetblue);
		colors.add(navy);
		colors.add(deepskyblue);
		colors.add(cyan);
		colors.add(steelblue);
		colors.add(teal);
		colors.add(royalblue);
		colors.add(dodgerblue);
		return colors;
	}

	/**
	 * Returns a series of shade of red colors see
	 * https://htmlcolorcodes.com/colors/shades-of-red/
	 * 
	 * @return a series of shade of red colors
	 */
	public static List<String> getRedColorsAsStringList() {
		List<String> colors = new ArrayList<>();
		String bloodred = "rgb(136,8,8)";
		String brickred = "rgb(170,74,68)";
		String burntorange = "rgb(204,85,0)";
		String byzantium = "rgb(112,41,99)";
		String cadmiumred = "rgb(210,43,43)";
		String carmine = "rgb(215,0,64)";
		String cerise = "rgb(222,49,99)";
		String claret = "rgb(129,19,49)";
		String mahogany = "rgb(192,64,0)";
		String neonred = "rgb(255,49,49)";
		String pastelred = "rgb(250,160,160)";
		String raspberry = "rgb(227,11,92)";
		colors.add(bloodred);
		colors.add(brickred);
		colors.add(burntorange);
		colors.add(byzantium);
		colors.add(cadmiumred);
		colors.add(carmine);
		colors.add(cerise);
		colors.add(claret);
		colors.add(mahogany);
		colors.add(neonred);
		colors.add(pastelred);
		colors.add(raspberry);
		return colors;
	}

	/**
	 * Returns a series of shade of red colors see
	 * https://htmlcolorcodes.com/colors/shades-of-red/
	 * 
	 * @return a series of shade of red colors
	 */
	public static List<RGBAColor> getRedColorsList() {
		List<RGBAColor> colors = new ArrayList<>();
		RGBAColor bloodred = new RGBAColor(136, 8, 8);
		RGBAColor brickred = new RGBAColor(170, 74, 68);
		RGBAColor burntorange = new RGBAColor(204, 85, 0);
		RGBAColor byzantium = new RGBAColor(112, 41, 99);
		RGBAColor cadmiumred = new RGBAColor(210, 43, 43);
		RGBAColor carmine = new RGBAColor(215, 0, 64);
		RGBAColor cerise = new RGBAColor(222, 49, 99);
		RGBAColor claret = new RGBAColor(129, 19, 49);
		RGBAColor mahogany = new RGBAColor(192, 64, 0);
		RGBAColor neonred = new RGBAColor(255, 49, 49);
		RGBAColor pastelred = new RGBAColor(250, 160, 160);
		RGBAColor raspberry = new RGBAColor(227, 11, 92);
		colors.add(bloodred);
		colors.add(brickred);
		colors.add(burntorange);
		colors.add(byzantium);
		colors.add(cadmiumred);
		colors.add(carmine);
		colors.add(cerise);
		colors.add(claret);
		colors.add(mahogany);
		colors.add(neonred);
		colors.add(pastelred);
		colors.add(raspberry);
		return colors;
	}

	/**
	 * Returns the blood red color see https://htmlcolorcodes.com/colors/blood-red/
	 * 
	 * @return the blood red color
	 */
	public static String getBloodRedAsString() {
		return getRedColorsAsStringList().get(0);
	}

	/**
	 * Returns the turquoise color see https://htmlcolorcodes.com/colors/turquoise/
	 * 
	 * @return the turquoise color
	 */
	public static String getTurquoiseAsString() {
		return getBlueColorsAsStringList().get(0);
	}
	
	/**
	 * Returns the blood red color see https://htmlcolorcodes.com/colors/blood-red/
	 * 
	 * @return the blood red color
	 */
	public static RGBAColor getBloodRed() {
		return getRedColorsList().get(0);
	}

	/**
	 * Returns the turquoise color see https://htmlcolorcodes.com/colors/turquoise/
	 * 
	 * @return the turquoise color
	 */
	public static RGBAColor getTurquoise() {
		return getBlueColorsList().get(0);
	}

}