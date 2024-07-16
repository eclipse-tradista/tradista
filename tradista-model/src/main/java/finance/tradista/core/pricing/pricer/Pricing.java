package finance.tradista.core.pricing.pricer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

@Retention(RetentionPolicy.RUNTIME)
public @interface Pricing {
	boolean defaultREALIZED_PNL() default false;

	boolean defaultUNREALIZED_PNL() default false;

	boolean defaultPNL() default false;

}
