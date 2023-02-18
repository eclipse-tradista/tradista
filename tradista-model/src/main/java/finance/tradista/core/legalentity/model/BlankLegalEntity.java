package finance.tradista.core.legalentity.model;

import org.apache.commons.lang3.StringUtils;

/*
 * Copyright 2021 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

public final class BlankLegalEntity extends LegalEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -169563912556871116L;

	private static final BlankLegalEntity instance = new BlankLegalEntity();

	private BlankLegalEntity() {
		super(StringUtils.EMPTY);
	}

	public static BlankLegalEntity getInstance() {
		return instance;
	}

}