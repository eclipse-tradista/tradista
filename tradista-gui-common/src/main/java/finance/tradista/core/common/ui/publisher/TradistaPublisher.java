package finance.tradista.core.common.ui.publisher;

import finance.tradista.core.common.ui.subscriber.TradistaSubscriber;

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

public interface TradistaPublisher {

	void publish();

	void addSubscriber(TradistaSubscriber subscriber);

	void removeSubscriber(TradistaSubscriber subscriber);

	boolean isError();

}