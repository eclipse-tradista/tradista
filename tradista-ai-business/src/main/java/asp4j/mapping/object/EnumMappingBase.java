package asp4j.mapping.object;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import asp4j.lang.LangElem;
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
import asp4j.mapping.annotations.MapAs;

/**
 *
 * @author hbeck Jun 8, 2013
 */
public abstract class EnumMappingBase<T extends Enum<T>, E extends LangElem> implements Mapping<T, E>, HasTargetNames {

	private final Class<T> enumType;
	private final Map<String, String> constantName2targetName;
	private final Map<String, String> targetName2constantName;
	private Set<String> targetNames;

	public EnumMappingBase(Class<T> enumType) {
		this.enumType = enumType;
		this.constantName2targetName = new HashMap<>();
		this.targetName2constantName = new HashMap<>();
		this.targetNames = new HashSet<>();
		init();
	}

	private void init() {
		for (Field field : enumType.getDeclaredFields()) {
			if (field.isEnumConstant()) {
				if (field.isAnnotationPresent(MapAs.class)) {
					String targetName = field.getAnnotation(MapAs.class).value();
					constantName2targetName.put(field.getName(), targetName);
					targetName2constantName.put(targetName, field.getName());
					targetNames.add(targetName);
				} else {
					targetNames.add(field.getName());
				}
			}
		}
	}

	protected final String getTargetName(T t) {
		return constantName2targetName.containsKey(t.name()) ? constantName2targetName.get(t.name()) : t.name();
	}

	@Override
	public final T asObject(E a) {
		String constantName = targetName2constantName.containsKey(a.symbol()) ? targetName2constantName.get(a.symbol())
				: a.symbol();
		return Enum.valueOf(enumType, constantName);
	}

	@Override
	public final Set<String> getTargetNames() {
		return Collections.unmodifiableSet(targetNames);
	}
}
