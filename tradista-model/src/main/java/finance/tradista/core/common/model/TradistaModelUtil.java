package org.eclipse.tradista.core.common.model;

import java.lang.reflect.InvocationTargetException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;

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

public final class TradistaModelUtil {

	private static DecimalFormat decimalFormat;

	static {
		decimalFormat = new DecimalFormat();
		decimalFormat.setMinimumFractionDigits(2);
		decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
	}

	private static UnaryOperator<Object> clone = x -> (x instanceof TradistaObject to) ? to.clone() : x;

	private static Function<Map.Entry<?, ?>, Object> cloneMapEntryKey = x -> x.getKey() instanceof TradistaObject
			? ((TradistaObject) x.getKey()).clone()
			: x.getKey();

	private static Function<Map.Entry<?, ?>, Object> cloneMapEntryValue = x -> x.getValue() instanceof TradistaObject
			? ((TradistaObject) x.getValue()).clone()
			: x.getValue();

	private TradistaModelUtil() {
	}

	public static Map<?, ?> deepCopy(Map<?, ?> originalMap) {
		if (originalMap == null) {
			return null;
		}
		Map<?, ?> copy = originalMap.entrySet().stream()
				.collect(Collectors.toMap(cloneMapEntryKey, cloneMapEntryValue));
		return copy;
	}

	public static List<?> deepCopy(List<? extends TradistaObject> originalList) {
		if (originalList == null) {
			return null;
		}
		List<?> copy = originalList.stream().map(clone).collect(Collectors.toList());
		return copy;
	}

	public static Set<?> deepCopy(Set<? extends TradistaObject> originalSet) {
		if (originalSet == null) {
			return null;
		}
		Set<?> copy = originalSet.stream().map(clone).collect(Collectors.toSet());
		return copy;
	}

	@SuppressWarnings("unchecked")
	public static <T extends TradistaObject> T clone(T tradistaObject) {
		if (tradistaObject == null) {
			return null;
		}
		return (T) tradistaObject.clone();
	}

	public static String formatNumber(Number number) {
		return decimalFormat.format(number);
	}

	public static String formatObject(Object object) {
		if (object == null) {
			return StringUtils.EMPTY;
		}
		if (object instanceof Number number) {
			return TradistaModelUtil.formatNumber(number);
		}
		return object.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInstance(Class<T> type, String className) {
		try {
			return (T) getInstance(Class.forName(className));
		} catch (ClassNotFoundException cnfe) {
			throw new TradistaTechnicalException(
					String.format("Could not create instance of %s : %s", className, cnfe));
		}
	}

	public static <T> T getInstance(Class<T> type) {
		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException
				| SecurityException e) {
			throw new TradistaTechnicalException(String.format("Could not create instance of %s : %s", type, e));
		} catch (InvocationTargetException ite) {
			throw new TradistaTechnicalException(
					String.format("Could not create instance of %s : %s", type, ite.getCause().getMessage()));
		}
	}

}