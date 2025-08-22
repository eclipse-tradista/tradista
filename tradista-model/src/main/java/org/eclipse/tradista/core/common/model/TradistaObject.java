package org.eclipse.tradista.core.common.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;

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

public abstract class TradistaObject implements Serializable, Cloneable {

	private static final long serialVersionUID = 7708737499544151077L;

	protected TradistaObject(long id) {
		this.id = id;
	}

	protected TradistaObject() {
	}

	private long id;

	public long getId() {
		return id;
	}

	/**
	 * Important: Use with caution, setId is expected to be used in the DAL only.
	 * Modifying the id programmatically can have undesired side effects as the id
	 * is used in the equals method.
	 * 
	 * @param id the id identifies an object in Tradista.
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * hashCode is calculated using the object composite key (@see
	 * org.eclipse.tradista.core.common.model.Id). If the object doesn't have a
	 * composite key, id will be used if positive, otherwise Object's hashCode
	 * implementation is used.
	 */
	@Override
	public int hashCode() {
		List<Object> values = getAllIdValues();
		if (values.isEmpty()) {
			if (id > 0) {
				return Objects.hashCode(id);
			} else {
				return super.hashCode();
			}
		}
		return Objects.hash(values);
	}

	/**
	 * Two Tradista objects are equal if they have the same id and if this id is
	 * positive (this means that they both refer to the same object that have been
	 * persisted in Tradista). If they don't have both positive ids, they are equal
	 * if they have the same composite key (@see
	 * org.eclipse.tradista.core.common.model.Id). In all other cases, they are
	 * different.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		// If both objects were persisted, they are equal only if they have the same id
		if (id > 0 && ((TradistaObject) obj).getId() > 0) {
			return (id == ((TradistaObject) obj).getId());
		}
		List<Field> ids = getAllIds();
		if (ids.isEmpty()) {
			// No ids are defined, so we keep the default behaviour
			return super.equals(obj);
		}
		for (Field f : ids) {
			f.setAccessible(true);
			Object value;
			try {
				value = f.get(this);
				Object otherValue = f.get(obj);
				if (value == null) {
					if (otherValue != null) {
						return false;
					}
				} else {
					if (otherValue == null) {
						return false;
					}
					if (value instanceof BigDecimal bd) {
						if (bd.compareTo((BigDecimal) otherValue) != 0) {
							return false;
						}
					} else if (!value.equals(otherValue)) {
						return false;
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// If these issues arrive, no need to pursue the process
				throw new TradistaTechnicalException(e);
			}
		}
		// At this stage all id fields are equal
		return true;
	}

	private List<Field> getIds(Class<? extends TradistaObject> klass) {
		final List<Field> fields = new ArrayList<>();
		final List<Field> allFields = new ArrayList<>(Arrays.asList(klass.getDeclaredFields()));
		for (final Field field : allFields) {
			if (field.isAnnotationPresent(Id.class)) {
				fields.add(field);
			}
		}
		return fields;
	}

	public List<Object> getAllIdValues() {
		List<Field> ids = getAllIds();
		List<Object> values = new ArrayList<>();
		for (Field f : ids) {
			f.setAccessible(true);
			Object o;
			try {
				o = f.get(this);
				values.add(o);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// If these issues arrive, no need to pursue the process
				throw new TradistaTechnicalException(e);
			}
		}
		return values;
	}

	public String getAllIdNames() {
		StringBuilder key = new StringBuilder();
		for (Field f : getAllIds()) {
			key.append(f.getName());
		}
		return key.toString();
	}

	@SuppressWarnings("unchecked")
	public List<Field> getAllIds() {
		Class<? extends TradistaObject> klass = (Class<? extends TradistaObject>) this.getClass().getSuperclass();
		List<Field> ids = new ArrayList<>();
		while (!klass.equals(TradistaObject.class)) {
			ids.addAll(getIds(klass));
			klass = (Class<? extends TradistaObject>) klass.getSuperclass();
		}
		ids.addAll(getIds(this.getClass()));
		return ids;
	}

	public String toString() {
		List<Field> fields = getAllIds();
		List<Object> values = getAllIdValues();
		StringBuilder display = new StringBuilder(this.getClass().getSimpleName());
		display.append(" [");
		for (int i = 0; i < fields.size(); i++) {
			display.append(fields.get(i).getName() + "=" + values.get(i));
			if (i < fields.size() - 1) {
				display.append(", ");
			}
		}
		display.append("]");
		return display.toString();
	}

	@Override
	public TradistaObject clone() {
		TradistaObject clone = null;
		try {
			clone = (TradistaObject) super.clone();
		} catch (CloneNotSupportedException _) {
			// Not expected, TradistaObject and subclasses are Cloneable
		}
		return clone;
	}

}