package asp4j.solver.object;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.util.TradistaUtil;

import asp4j.lang.AnswerSet;
import asp4j.lang.AnswerSetImpl;
import asp4j.lang.Atom;
import asp4j.lang.AtomImpl;
import asp4j.lang.Constant;
import asp4j.lang.ConstantImpl;
import asp4j.lang.HasArgs;
import asp4j.lang.HasSymbol;
import asp4j.lang.LangElem;
import asp4j.lang.Term;
import asp4j.lang.TermImpl;
import asp4j.mapping.MappingException;
import asp4j.mapping.annotations.Arg;
import asp4j.mapping.annotations.DefAtom;
import asp4j.mapping.annotations.DefConstant;
import asp4j.mapping.annotations.DefEnumAtoms;
import asp4j.mapping.annotations.DefEnumConstants;
import asp4j.mapping.annotations.DefTerm;
import asp4j.mapping.object.AnyMapping;
import asp4j.mapping.object.HasTargetNames;
import asp4j.mapping.object.InputMapping;
import asp4j.mapping.object.Mapping;
import asp4j.mapping.object.OutputMapping;
import asp4j.mapping.object.atom.AtomEnumMapping;
import asp4j.mapping.object.atom.AtomMapping;
import asp4j.mapping.object.constant.ConstantEnumMapping;
import asp4j.mapping.object.constant.ConstantMapping;
import asp4j.mapping.object.term.TermMapping;

/*Copyright (c) 2013, Harald Beck
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

  1. Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
  
  2. Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
  
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*/

/**
 *
 * @author hbeck May 23, 2013
 */
public class Binding {

	private Registry registry;

	public Binding() {
		this.registry = new Registry();
	}

	/**
	 * add explicit mapping for given class.
	 *
	 */
	public <T, E extends LangElem> Binding add(Class<T> clazz, AnyMapping<T, E> mapping) {
		registry.add(clazz, mapping);
		return this;
	}

	/**
	 * add implicit mapping for given class. mapping is derived by annotations of
	 * this class.
	 * 
	 */
	public <T> Binding add(Class<T> clazz) throws MappingException {
		registry.add(clazz);
		return this;
	}

	/**
	 */
	public Binding addAll(Collection<Class<?>> classes) throws MappingException {
		for (Class<?> clazz : classes) {
			registry.add(clazz);
		}
		return this;
	}

	//
	// solver methods
	//
	protected <T, E extends LangElem> E mapAsLangElem(T t) throws MappingException {
		InputMapping<T, E> mapping = (InputMapping<T, E>) registry.getInputMapping(t.getClass());
		return mapping.asLangElem(t);
	}

	protected <T, E extends LangElem> T mapAsObject(E e) throws MappingException {
		OutputMapping<T, E> mapping = (OutputMapping<T, E>) registry.getOutputMapping(e);
		return mapping.asObject(e);
	}

	protected <T, E extends LangElem> T mapAsObject(E e, Class<T> clazz) throws MappingException {
		OutputMapping<T, E> mapping = (OutputMapping<T, E>) registry.getOutputMapping(clazz);
		return mapping.asObject(e);
	}

	/**
	 * filters low level set of atoms based on the filters registered in this
	 * binding and returns according objects
	 *
	 * @param atoms
	 * @return unmodifiable set of filtered atoms, mapped to according objects
	 */
	protected Set<Object> filterAndMap(Set<Atom> atoms, Filter filter) throws MappingException {
		Set<Object> result = new HashSet<>();
		for (Atom atom : atoms) {
			Class clazz = registry.getClassForSymbol(atom.symbol());
			if (filter.accepts(clazz)) {
				result.add(mapAsObject(atom));
			}
		}
		return Collections.unmodifiableSet(result);
	}

	protected List<AnswerSet<Object>> filterAndMap(List<AnswerSet<Atom>> answerSets, Filter filter)
			throws MappingException {
		List<AnswerSet<Object>> list = new ArrayList<>();
		for (AnswerSet<Atom> answerSet : answerSets) {
			list.add(new AnswerSetImpl(filterAndMap(answerSet.atoms(), filter)));
		}
		return Collections.unmodifiableList(list);
	}

	//
	//
	private class Registry {

		Registry() {
			init();
		}

		//
		private Map<Class<?>, InputMapping<?, ?>> inputMappings = new HashMap<>();
		private Map<Class<?>, OutputMapping<?, ?>> outputMappings = new HashMap<>();
		private Map<String, Class<?>> symbol2class = new HashMap<>();

		private void init() {
			ConstantMapping<String> cm = new ConstantMapping<String>() {
				@Override
				public Constant asLangElem(String s) {
					return new ConstantImpl(s);
				}

				@Override
				public String asObject(Constant constant) {
					return constant.symbol();
				}
			};
			addInputMapping(String.class, cm);
			addOutputMapping(String.class, cm);
		}

		private boolean isRegistered(Class<?> clazz) {
			return inputMappings.containsKey(clazz) || outputMappings.containsKey(clazz);
		}

		private <T, E extends LangElem> void addInputMapping(Class<T> clazz, InputMapping<T, E> mapping) {
			inputMappings.put(clazz, mapping);
		}

		private <T, E extends LangElem> void addOutputMapping(Class<T> clazz, OutputMapping<T, E> mapping) {
			outputMappings.put(clazz, mapping);
			if (clazz.isEnum()) {
				HasTargetNames enumMapping = (HasTargetNames) mapping;
				for (String targetName : enumMapping.getTargetNames()) {
					symbol2class.put(targetName, clazz);
				}
			} else if (mapping instanceof HasSymbol) {
				symbol2class.put(((HasSymbol) mapping).symbol(), clazz);
			}
		}

		/**
		 * add explicit mapping
		 *
		 * @param <T>
		 * @param <E>
		 * @param clazz
		 * @param mapping
		 */
		private <T, E extends LangElem> void add(Class<T> clazz, AnyMapping<T, E> mapping) {
			if (isRegistered(clazz)) {
				return;
			}
			if (mapping instanceof InputMapping) {
				addInputMapping(clazz, (InputMapping<T, E>) mapping);
			}
			if (mapping instanceof OutputMapping) {
				addOutputMapping(clazz, (OutputMapping<T, E>) mapping);
			}
		}

		/**
		 * add implicit mapping to be generated from annotations
		 */
		private void add(Class<?> clazz) throws MappingException {
			if (isRegistered(clazz)) {
				return;
			}
			if (clazz.isEnum()) {
				addEnum(clazz);
			} else {
				addAnnotatedClass(clazz);
			}
		}

		private void addEnum(Class clazz) {
			Mapping<?, ?> mapping;
			if (clazz.isAnnotationPresent(DefEnumAtoms.class)) {
				mapping = new AtomEnumMapping(clazz);
			} else if (clazz.isAnnotationPresent(DefEnumConstants.class)) {
				mapping = new ConstantEnumMapping(clazz);
			} else {
				return;
			}
			addInputMapping(clazz, (InputMapping<?, ?>) mapping);
			addOutputMapping(clazz, (OutputMapping<?, ?>) mapping);
		}

		private <T, E extends LangElem> void addAnnotatedClass(Class<T> clazz) throws MappingException {
			AnyMapping<T, ?> mapping;
			if (clazz.isAnnotationPresent(DefAtom.class)) {
				mapping = createAtomMapping(clazz);
			} else if (clazz.isAnnotationPresent(DefTerm.class)) {
				mapping = createTermMapping(clazz);
			} else if (clazz.isAnnotationPresent(DefConstant.class)) {
				mapping = createConstantMapping(clazz);
			} else {
				return; // no inner resolving. assume explicit mapping given
			}

			if (mapping instanceof InputMapping) {
				addInputMapping(clazz, (InputMapping<T, E>) mapping);
			}
			if (mapping instanceof OutputMapping) {
				addOutputMapping(clazz, (OutputMapping<T, E>) mapping);
			}

			Collection<Class<?>> innerClasses = getAnnotatedInnerClasses(clazz);
			for (Class<?> innerClass : innerClasses) {
				this.add(innerClass);
			}
		}

		private Collection<Class<?>> getAnnotatedInnerClasses(Class<?> clazz) {
			Collection<Class<?>> classes = new HashSet<>();
			for (Method method : clazz.getMethods()) {
				Arg argAnnotation = method.getAnnotation(Arg.class);
				if (argAnnotation == null) {
					continue;
				}
				Class<?> type = method.getReturnType();
				classes.add(type);
			}
			return classes;
		}

		private <T, E extends LangElem> InputMapping<T, E> getInputMapping(Class<T> clazz) throws MappingException {
			InputMapping<?, ?> mapping = inputMappings.get(clazz);
			if (mapping != null) {
				return (InputMapping<T, E>) mapping;
			}
			for (Class<?> candidateClass : clazz.getInterfaces()) {
				mapping = inputMappings.get(candidateClass);
				if (mapping != null) {
					return (InputMapping<T, E>) mapping;
				}
			}
			throw new MappingException("no input mapping found for " + clazz);
		}

		private <T, E extends LangElem> OutputMapping<T, E> getOutputMapping(E e) throws MappingException {
			Class<T> clazz = (Class<T>) symbol2class.get(e.symbol());
			return getOutputMapping(clazz);
		}

		private <T, E extends LangElem> OutputMapping<T, E> getOutputMapping(Class<T> clazz) throws MappingException {
			OutputMapping<?, ?> mapping = outputMappings.get(clazz);
			if (mapping != null) {
				return (OutputMapping<T, E>) mapping;
			}
			for (Class<?> candidateClass : clazz.getInterfaces()) {
				mapping = outputMappings.get(candidateClass);
				if (mapping != null) {
					return (OutputMapping<T, E>) mapping;
				}
			}
			throw new MappingException("no output mapping found for " + clazz);
		}

		private <T> AtomMapping<T> createAtomMapping(final Class<T> clazz) throws MappingException {
			DefAtom atomAnn = clazz.getAnnotation(DefAtom.class);
			final String predicateSymbol = atomAnn.value();
			return new AtomMapping<T>() {
				@Override
				public String symbol() {
					return predicateSymbol;
				}

				@Override
				public Atom asLangElem(T t) throws MappingException {
					Map<Integer, Term> termMap = new HashMap<>();
					try {
						for (Method method : clazz.getMethods()) {
							Arg argAnnotation = method.getAnnotation(Arg.class);
							if (argAnnotation == null) {
								continue;
							}
							int arg = argAnnotation.value();
							Object returnedObject = method.invoke(t);
							termMap.put(Integer.valueOf(arg), (Term) mapAsLangElem(returnedObject));
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new MappingException(e);
					}
					return new AtomImpl(predicateSymbol, asArray(termMap));
				}

				@Override
				public T asObject(Atom atom) throws MappingException {
					try {
						T object = TradistaUtil.getInstance(clazz);
						invokeSetters(object, clazz, atom);
						return object;
					} catch (TradistaTechnicalException tte) {
						throw new MappingException(tte);
					}
				}
			};
		}

		private <T> TermMapping<T> createTermMapping(final Class<T> clazz) {
			return new TermMapping<T>() {
				@Override
				public String symbol() {
					DefTerm termAnn = clazz.getAnnotation(DefTerm.class);
					return termAnn.value();
				}

				@Override
				public Term asLangElem(T t) throws MappingException {
					Map<Integer, Term> termMap = new HashMap<>();
					try {
						for (Method method : clazz.getMethods()) {
							Arg argAnnotation = method.getAnnotation(Arg.class);
							if (argAnnotation == null) {
								continue;
							}
							int arg = argAnnotation.value();
							Object returnedObject = method.invoke(t);
							termMap.put(Integer.valueOf(arg), (Term) mapAsLangElem(returnedObject));
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new MappingException(e);
					}
					return new TermImpl(symbol(), asArray(termMap));
				}

				@Override
				public T asObject(Term term) throws MappingException {
					try {
						T object = TradistaUtil.getInstance(clazz);
						invokeSetters(object, clazz, term);
						return object;
					} catch (TradistaTechnicalException tte) {
						throw new MappingException(tte);
					}
				}
			};
		}

		private <T> ConstantMapping<T> createConstantMapping(final Class<T> clazz) {
			return new ConstantMapping<T>() {
				@Override
				public Constant asLangElem(T t) {
					DefConstant constAnn = clazz.getAnnotation(DefConstant.class);
					return new ConstantImpl(constAnn.value());
				}

				@Override
				public T asObject(Constant constant) throws MappingException {
					try {
						return TradistaUtil.getInstance(clazz);
					} catch (TradistaTechnicalException tte) {
						throw new MappingException(tte);
					}
				}
			};
		}

		private <T, E extends LangElem> void invokeSetters(Object object, Class<T> clazz, HasArgs input)
				throws MappingException {
			try {
				for (Method method : clazz.getMethods()) {
					Arg argAnnotation = method.getAnnotation(Arg.class);
					if (argAnnotation == null) {
						continue;
					}
					String getterName = method.getName();
					Class<?> type = method.getReturnType();
					int pos = getterName.startsWith("get") ? 3 : 2; // get or is
					String setterName = "set" + getterName.substring(pos);
					int argIdx = argAnnotation.value();
					Method setterMethod = clazz.getMethod(setterName, type);
					Term term = input.getArg(argIdx);
					setterMethod.invoke(object, mapAsObject(term, type));
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new MappingException(e);
			}
		}

		private Term[] asArray(Map<Integer, Term> map) {
			if (map == null || map.isEmpty()) {
				return null;
			}
			Term[] arr = new Term[map.size()];
			for (Map.Entry<Integer, Term> entry : map.entrySet()) {
				arr[entry.getKey()] = entry.getValue();
			}
			return arr;
		}

		private Class getClassForSymbol(String symbol) {
			return symbol2class.get(symbol);
		}
	}
}