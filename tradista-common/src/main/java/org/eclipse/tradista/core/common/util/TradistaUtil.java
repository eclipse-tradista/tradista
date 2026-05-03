package org.eclipse.tradista.core.common.util;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.error.model.Error;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import org.eclipse.tradista.core.trade.messaging.TradeEvent;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.validator.TradeValidator;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.util.ClassUtils;

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

public final class TradistaUtil {

	private static final String RETURN_TYPE_CANNOT_BE_NULL = "The return type cannot be null.%n";

	private TradistaUtil() {
	}

	private static Map<String, TransferManager<TradeEvent<?>>> transferManagersCache = new ConcurrentHashMap<>();

	private static Map<String, TradeValidator> tradeValidatorCache = new ConcurrentHashMap<>();

	private static Set<Class<? extends Error>> errorClassCache = ConcurrentHashMap.newKeySet();

	private static final String COULD_NOT_CREATE_INSTANCE_OF = "Could not create instance of %s : %s";

	@SuppressWarnings("unchecked")
	public static <T> List<Class<T>> getAllClassesByType(Class<T> type, String pckg) {
		List<Class<T>> classes = new ArrayList<>();
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AssignableTypeFilter(type));
		for (BeanDefinition bd : scanner.findCandidateComponents(pckg)) {
			try {
				Class<T> klass = (Class<T>) Class.forName(bd.getBeanClassName());
				classes.add(klass);
			} catch (ClassNotFoundException cnfe) {
				throw new TradistaTechnicalException(cnfe);
			}

		}
		return classes;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> getAllInstancesByType(Class<T> type, String pckg) {
		List<T> instances = new ArrayList<>();
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AssignableTypeFilter(type));
		for (BeanDefinition bd : scanner.findCandidateComponents(pckg)) {
			try {
				T instance = (T) TradistaUtil.getInstance(Class.forName(bd.getBeanClassName()));
				instances.add(instance);
			} catch (ClassNotFoundException cnfe) {
				throw new TradistaTechnicalException(cnfe);
			}
		}
		return instances;
	}

	public static Set<String> getAvailableNames(Class<?> klass, String pckg) {
		Set<String> names = new HashSet<>();
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AssignableTypeFilter(klass));
		for (BeanDefinition bd : scanner.findCandidateComponents(pckg)) {
			String fullClassName = bd.getBeanClassName();
			names.add(fullClassName.substring(fullClassName.lastIndexOf(".") + 1));
		}
		return names;
	}

	private static synchronized Set<Class<? extends Error>> getAllErrorClasses() {
		if (errorClassCache.isEmpty()) {
			ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
					false);
			scanner.addIncludeFilter(new AssignableTypeFilter(Error.class));
			scanner.setResourceLoader(null);
			for (BeanDefinition bd : scanner.findCandidateComponents("org.eclipse.tradista.**")) {
				if (!bd.isAbstract()) {
					Class<? extends Error> klass;
					try {
						klass = Class.forName(bd.getBeanClassName()).asSubclass(Error.class);
					} catch (ClassNotFoundException cnfe) {
						throw new TradistaTechnicalException(cnfe);
					}
					errorClassCache.add(klass);
				}
			}
		}
		return errorClassCache;
	}

	public static Set<String> getAllErrorClassNames() {
		return getAllErrorClasses().stream().map(Class::getSimpleName).collect(Collectors.toSet());
	}

	public static Set<String> getAllErrorTypes() {
		return getAllErrorClasses().stream().map(c -> TradistaUtil.getInstance(c).getType())
				.collect(Collectors.toSet());

	}

	public static List<Class<?>> getAllClassesByRegex(String regex, String pckg) {
		List<Class<?>> classes = new ArrayList<>();
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(regex)));
		for (BeanDefinition bd : scanner.findCandidateComponents(pckg)) {
			try {
				Class<?> klass = Class.forName(bd.getBeanClassName());
				classes.add(klass);
			} catch (ClassNotFoundException cnfe) {
				throw new TradistaTechnicalException(cnfe);
			}
		}

		return classes;
	}

	public static List<Class<?>> getAllClassesByTypeAndAnnotation(Class<?> type, Class<? extends Annotation> annotation,
			String pckg) {
		List<Class<?>> classes = new ArrayList<>();
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AssignableTypeFilter(type));
		scanner.addIncludeFilter(new AnnotationTypeFilter(annotation));
		for (BeanDefinition bd : scanner.findCandidateComponents(pckg)) {
			try {
				Class<?> klass = Class.forName(bd.getBeanClassName());
				classes.add(klass);
			} catch (ClassNotFoundException cnfe) {
				throw new TradistaTechnicalException(cnfe);
			}

		}
		return classes;
	}

	private static Class<?> getClassByProductTypeSubPackageAndName(String productType, String subPackage, String name)
			throws TradistaBusinessException {
		Class<?> klass = null;
		try {
			klass = Class.forName("org.eclipse.tradista." + new ProductBusinessDelegate().getProductFamily(productType)
					+ "." + productType.toLowerCase() + "." + subPackage + "." + name);
		} catch (ClassNotFoundException cnfe) {
			throw new TradistaTechnicalException(cnfe);
		}

		return klass;
	}

	@SuppressWarnings("unchecked")
	public static TransferManager<TradeEvent<?>> getTransferManager(String productType)
			throws TradistaBusinessException {
		if (transferManagersCache.containsKey(productType)) {
			return transferManagersCache.get(productType);
		} else {
			TransferManager<TradeEvent<?>> transferManager = (TransferManager<TradeEvent<?>>) TradistaUtil.getInstance(
					getClassByProductTypeSubPackageAndName(productType, "transfer", productType + "TransferManager"));
			transferManagersCache.put(productType, transferManager);
			return transferManager;
		}
	}

	public static TradeValidator getTradeValidator(String productType) throws TradistaBusinessException {
		if (tradeValidatorCache.containsKey(productType)) {
			return tradeValidatorCache.get(productType);
		} else {
			TradeValidator tradeValidator = (TradeValidator) TradistaUtil.getInstance(
					getClassByProductTypeSubPackageAndName(productType, "validator", productType + "TradeValidator"));
			tradeValidatorCache.put(productType, tradeValidator);
			return tradeValidator;
		}
	}

	/**
	 * Returns a type token for Trades
	 * 
	 * @return a type token for Trades
	 */
	@SuppressWarnings("unchecked")
	public static Class<Trade<? extends Product>> getTradeToken() {
		return (Class<Trade<? extends Product>>) (Class<?>) Trade.class;
	}

	/**
	 * Calls a method using introspection.
	 * 
	 * @param fullClassName the full class name of the method
	 * @param returnType    return type set as type token so the type is not erased
	 * @param methodName    the method name
	 * @param params        the method parameters (optional)
	 * @return the result of the method invocation
	 */
	public static <T> T callMethod(String fullClassName, Class<T> returnType, String methodName, Object... params)
			throws TradistaBusinessException {
		T toBeReturned = null;
		Class<?>[] klasses = new Class<?>[params.length];
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(fullClassName)) {
			errMsg.append(String.format("The full class name is mandatory.%n"));
		}
		if (returnType == null) {
			errMsg.append(String.format(RETURN_TYPE_CANNOT_BE_NULL));
		}
		if (StringUtils.isBlank(methodName)) {
			errMsg.append("The method name is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		try {
			Class<?> klass = Class.forName(fullClassName);
			Method method = TradistaUtil.getMethod(klass, methodName, params);
			if (method == null) {
				if (params.length > 0) {
					for (int i = 0; i < params.length; i++) {
						klasses[i] = params[i].getClass();
					}
				}
				throw new TradistaTechnicalException(
						String.format("%s method with %s parameters has not been found in %s class.", methodName,
								klasses, fullClassName));
			} else {
				// TradistaUtil.getInstance(klass) is used so the method invocation always work,
				// whether the method is static or not
				toBeReturned = (T) callMethod(returnType, method, TradistaUtil.getInstance(klass), params);
			}
		} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
			throw new TradistaTechnicalException(e);
		}
		return toBeReturned;
	}

	/**
	 * Calls a method using introspection.
	 * 
	 * @param returnType return type set as type token so the type is not erased
	 * @param method     the method to invoke
	 * @param obj        (optional) the instance on which the method should be
	 *                   invoked. It can be null if the method is static.
	 * @param params     the method parameters (optional)
	 * @return the result of the method invocation
	 */
	@SuppressWarnings("unchecked")
	public static <T> T callMethod(Class<T> returnType, Method method, Object obj, Object... params)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (returnType == null) {
			errMsg.append(String.format(RETURN_TYPE_CANNOT_BE_NULL));
		}
		if (method == null) {
			errMsg.append(String.format("The method cannot be null.%n"));
		}
		if (returnType != null && method != null) {
			if (!ClassUtils.isAssignable(returnType, method.getReturnType())) {
				errMsg.append(
						String.format("The method return type is %s and not %s.", method.getReturnType(), returnType));
			}
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		T toBeReturned;
		try {
			toBeReturned = (T) method.invoke(obj, params);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException e) {
			throw new TradistaTechnicalException(e);
		} catch (InvocationTargetException ite) {
			if (ite.getCause() instanceof TradistaBusinessException tbe) {
				throw tbe;
			} else {
				throw new TradistaTechnicalException(ite.getCause().getMessage());
			}
		}
		return toBeReturned;
	}

	/**
	 * Calls a method using introspection.
	 * 
	 * @param returnType return type set as type token so the type is not erased
	 * @param methodName the method to invoke
	 * @param obj        the instance on which the method should be invoked.
	 * @param params     the method parameters (optional)
	 * @return the result of the method invocation
	 */
	@SuppressWarnings("unchecked")
	public static <T> T callMethod(Class<T> returnType, String methodName, Object obj, Object... params)
			throws TradistaBusinessException {
		Method method = null;
		StringBuilder errMsg = new StringBuilder();
		if (returnType == null) {
			errMsg.append(String.format(RETURN_TYPE_CANNOT_BE_NULL));
		}
		if (StringUtils.isBlank(methodName)) {
			errMsg.append(String.format("The method name is mandatory.%n"));
		}
		if (obj == null) {
			errMsg.append(String.format("The object cannot be null.%n"));
		}
		if (!StringUtils.isBlank(methodName) && obj != null) {
			method = TradistaUtil.getMethod(obj.getClass(), methodName, params);
		}
		if (returnType != null && method != null) {
			if (!ClassUtils.isAssignable(returnType, method.getReturnType())) {
				errMsg.append(
						String.format("The method return type is %s and not %s.", method.getReturnType(), returnType));
			}
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		T toBeReturned;
		try {
			toBeReturned = (T) method.invoke(obj, params);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException e) {
			throw new TradistaTechnicalException(e);
		} catch (InvocationTargetException ite) {
			if (ite.getCause() instanceof TradistaBusinessException tbe) {
				throw tbe;
			} else {
				throw new TradistaTechnicalException(ite.getCause().getMessage());
			}
		}
		return toBeReturned;
	}

	public static Method getMethod(Class<?> klass, String methodName, Object[] params) {
		Class<?>[] klasses = new Class<?>[params.length];
		StringBuilder errMsg = new StringBuilder();
		if (klass == null) {
			errMsg.append(String.format("The class cannot be null.%n"));
		}
		if (StringUtils.isBlank(methodName)) {
			errMsg.append("The method name is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		try {
			if (params != null && params.length > 0) {
				for (int i = 0; i < params.length; i++) {
					klasses[i] = params[i].getClass();
				}
			}
			for (Method m : klass.getMethods()) {
				if (!m.getName().equals(methodName)) {
					continue;
				}
				Class<?>[] parameterTypes = m.getParameterTypes();
				boolean matches = true;
				if (parameterTypes.length != params.length) {
					continue;
				}
				for (int i = 0; i < parameterTypes.length; i++) {
					// Using Spring's ClassUtils because we want primitive types to be also
					// considered
					if (!ClassUtils.isAssignable(parameterTypes[i], klasses[i])) {
						matches = false;
						break;
					}
				}
				if (matches) {
					return m;
				}
			}
		} catch (SecurityException | IllegalArgumentException e) {
			throw new TradistaTechnicalException(e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInstance(Class<T> type, String className) {
		try {
			return (T) getInstance(Class.forName(className));
		} catch (ClassNotFoundException cnfe) {
			throw new TradistaTechnicalException(String.format(COULD_NOT_CREATE_INSTANCE_OF, className, cnfe));
		}
	}

	public static <T> T getInstance(Class<T> type) {
		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException
				| SecurityException e) {
			throw new TradistaTechnicalException(String.format(COULD_NOT_CREATE_INSTANCE_OF, type, e));
		} catch (InvocationTargetException ite) {
			throw new TradistaTechnicalException(
					String.format(COULD_NOT_CREATE_INSTANCE_OF, type, ite.getCause().getMessage()));
		}
	}

	/**
	 * Get a class for a given name. Note: the class should be available in
	 * TradistaUtil's class loader.
	 * 
	 * @param className a class full name.
	 * @return A class
	 */
	public static Class<?> getClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException cnfe) {
			throw new TradistaTechnicalException(
					String.format("Could not create class from this name '%s' : %s", className, cnfe));
		}
	}

	/**
	 * Get a class for a given name. The class will be looked from the given class
	 * loader. If not found, the loading will be recursively tried from the class
	 * loader's parents. Ultimately, the loading is tried with TradistaUtil's class
	 * loader. TradistaUtil's class loader is also the class loader tried by default
	 * when there is no class loader set.
	 * 
	 * @param className   a class full name
	 * @param classLoader a class loader from which the class is looked. By default,
	 *                    TradistaUtil's class loader is used.
	 * @return
	 */
	public static Class<?> getClass(String className, ClassLoader classLoader) {
		try {
			if (classLoader == null) {
				return Class.forName(className);
			}
			while (classLoader != null) {
				Class<?> klass = classLoader.loadClass(className);
				if (klass != null) {
					return klass;
				}
				classLoader = classLoader.getParent();
			}
			// class not found. We try with TradistaUtil's class loader.
			return Class.forName(className);
		} catch (ClassNotFoundException cnfe) {
			throw new TradistaTechnicalException(
					String.format("Could not create class from this name '%s' : %s", className, cnfe));
		}
	}

	public static Set<String> getDistinctValuesFromProperties(String directory, String fileName) {
		Set<String> values = null;
		Properties prop = new Properties();
		InputStream in = TradistaUtil.class.getResourceAsStream("/" + directory + "/" + fileName + ".properties");
		try {
			prop.load(in);
			in.close();
			for (Object value : prop.keySet()) {
				if (values == null) {
					values = new HashSet<>();
				}
				values.add((String) value);
			}
		} catch (Exception _) {
		}
		return values;
	}

	public static String getModuleVersion(String packageName, ClassLoader classLoader) {
		if (classLoader == null) {
			return getModuleVersion(packageName);
		}
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(packageName)) {
			errMsg.append("The package name cannot be blank.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		String fullPackageName = "org.eclipse.tradista." + packageName.toLowerCase();
		while (classLoader != null) {
			Package p = classLoader.getDefinedPackage(fullPackageName);
			if (p != null) {
				return p.getImplementationVersion();
			}
			classLoader = classLoader.getParent();
		}
		// package not found. We try with TradistaUtil's class loader.
		return getModuleVersion(packageName);
	}

	public static String getModuleVersion(String packageName) {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(packageName)) {
			errMsg.append("The package name cannot be blank.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		ClassLoader classLoader = TradistaUtil.class.getClassLoader();
		String fullPackageName = "org.eclipse.tradista." + packageName.toLowerCase();
		while (classLoader != null) {
			Package p = classLoader.getDefinedPackage(fullPackageName);
			if (p != null) {
				return p.getImplementationVersion();
			}
			classLoader = classLoader.getParent();
		}
		// package not found.
		return null;
	}

}