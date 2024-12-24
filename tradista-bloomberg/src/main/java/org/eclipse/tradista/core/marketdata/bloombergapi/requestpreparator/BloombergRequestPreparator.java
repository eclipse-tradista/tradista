package org.eclipse.tradista.core.marketdata.bloombergapi.requestpreparator;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.tradista.core.marketdata.bloombergapi.BloombergTokenGenerator;
import org.eclipse.tradista.core.marketdata.requestpreparator.RequestPreparator;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public class BloombergRequestPreparator implements RequestPreparator {

	Properties userProperties = new Properties();

	{
		InputStream in = getClass().getResourceAsStream("/user.properties");
		try {
			userProperties.load(in);
			in.close();
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}

	}

	@Override
	public Map<String, String> prepareRequest() {
		Map<String, String> props = new HashMap<String, String>();
		for (Map.Entry<Object, Object> entry : userProperties.entrySet()) {
			props.put((String) entry.getKey(), (String) entry.getValue());
		}
		BloombergTokenGenerator tokenGenerator = new BloombergTokenGenerator();
		userProperties.setProperty("BloombergToken", tokenGenerator.generateToken(userProperties));
		return props;
	}

}