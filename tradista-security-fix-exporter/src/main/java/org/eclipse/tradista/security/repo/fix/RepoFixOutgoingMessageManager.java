package org.eclipse.tradista.security.repo.fix;

import static org.eclipse.tradista.fix.common.TradistaFixConstants.NOTICE_PERIOD_TAG;
import static org.eclipse.tradista.fix.common.TradistaFixConstants.REPO_RATE_TAG;
import static org.eclipse.tradista.fix.common.TradistaFixConstants.RIGHT_OF_REUSE_TAG;
import static org.eclipse.tradista.fix.common.TradistaFixConstants.RIGHT_OF_SUBSTITUTION_TAG;
import static org.eclipse.tradista.fix.common.TradistaFixConstants.TERMINABLE_ON_DEMAND_TAG;

import org.eclipse.tradista.security.repo.model.RepoTrade;

import quickfix.field.MarginRatio;
import quickfix.fix44.TradeCaptureReport;

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

public abstract class RepoFixOutgoingMessageManager<X extends RepoTrade> {

	public TradeCaptureReport createMarginRate(TradeCaptureReport tcReport, X trade) {
		tcReport.set(new MarginRatio(trade.getMarginRate().doubleValue()));
		return tcReport;
	}

	public TradeCaptureReport createRightOfSubstitution(TradeCaptureReport tcReport, X trade) {
		tcReport.setBoolean(RIGHT_OF_SUBSTITUTION_TAG, trade.isRightOfSubstitution());
		return tcReport;
	}

	public TradeCaptureReport createRightOfReuse(TradeCaptureReport tcReport, X trade) {
		tcReport.setBoolean(RIGHT_OF_REUSE_TAG, trade.isRightOfSubstitution());
		return tcReport;
	}

	public TradeCaptureReport createTerminableOnDemand(TradeCaptureReport tcReport, X trade) {
		tcReport.setBoolean(TERMINABLE_ON_DEMAND_TAG, trade.isRightOfSubstitution());
		return tcReport;
	}

	public TradeCaptureReport createRepoRate(TradeCaptureReport tcReport, X trade) {
		tcReport.setDecimal(REPO_RATE_TAG, trade.getRepoRate());
		return tcReport;
	}

	public TradeCaptureReport createNoticePeriod(TradeCaptureReport tcReport, X trade) {
		tcReport.setInt(NOTICE_PERIOD_TAG, trade.getNoticePeriod());
		return tcReport;
	}

}