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

package org.eclipse.tradista.core.common.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that links a parameter annotation to its
 * {@link AccessChecker} implementation.
 *
 * <p>
 * When {@link TradistaSegregationHandlerInterceptor} encounters a parameter
 * annotation carrying this meta-annotation, it instantiates the specified
 * {@link AccessChecker} and delegates the access check to it.
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * {@literal @}AccessCheckedBy(LegalEntityAccessChecker.class)
 * {@literal @}Target(ElementType.PARAMETER)
 * {@literal @}Retention(RetentionPolicy.RUNTIME)
 * public {@literal @}interface CheckLegalEntityAccess {}
 * </pre>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessCheckedBy {

	Class<? extends AccessChecker> value();

}