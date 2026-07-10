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
package org.eclipse.tradista.core.rating.service;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.rating.model.Rating;
import org.eclipse.tradista.core.rating.model.RatingAgency;
import org.eclipse.tradista.core.rating.model.RatingAssignment;

public class RatingBusinessDelegate {

	private RatingService ratingService;

	public RatingBusinessDelegate() {
		ratingService = TradistaServiceLocator.getInstance().getRatingService();
	}

	public Set<RatingAgency> getAllRatingAgencies() {
		return SecurityUtil.run(() -> ratingService.getAllRatingAgencies());
	}

	public RatingAgency getRatingAgencyById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The rating agency id must be positive.");
		}
		return SecurityUtil.run(() -> ratingService.getRatingAgencyById(id));
	}

	public RatingAgency getRatingAgencyByName(String name) throws TradistaBusinessException {
		if (StringUtils.isBlank(name)) {
			throw new TradistaBusinessException("The rating agency name cannot be empty.");
		}
		return SecurityUtil.run(() -> ratingService.getRatingAgencyByName(name));
	}

	public long saveRatingAgency(RatingAgency ratingAgency) throws TradistaBusinessException {
		if (ratingAgency == null) {
			throw new TradistaBusinessException("The rating agency cannot be null.");
		}
		if (StringUtils.isBlank(ratingAgency.getName())) {
			throw new TradistaBusinessException("The rating agency name cannot be empty.");
		} else {
			if (ratingAgency.getName().length() > 50) {
				throw new TradistaBusinessException("The rating agency name cannot exceed 50 characters.");
			}
		}
		return SecurityUtil.runEx(() -> ratingService.saveRatingAgency(ratingAgency));
	}

	public boolean deleteRatingAgency(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The rating agency id must be positive.");
		}
		return SecurityUtil.runEx(() -> ratingService.deleteRatingAgency(id));
	}

	public Set<Rating> getAllRatings() {
		return SecurityUtil.run(() -> ratingService.getAllRatings());
	}

	public Set<Rating> getRatingsByAgencyId(long agencyId) throws TradistaBusinessException {
		if (agencyId <= 0) {
			throw new TradistaBusinessException("The rating agency id must be positive.");
		}
		return SecurityUtil.run(() -> ratingService.getRatingsByAgencyId(agencyId));
	}

	public Rating getRatingById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The rating id must be positive.");
		}
		return SecurityUtil.run(() -> ratingService.getRatingById(id));
	}

	public long saveRating(Rating rating) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (rating == null) {
			throw new TradistaBusinessException("The rating cannot be null.");
		}
		if (StringUtils.isBlank(rating.getCode())) {
			errMsg.append(String.format("The rating code cannot be empty.%n"));
		} else {
			if (rating.getCode().length() > 20) {
				errMsg.append(String.format("The rating code (%s) cannot exceed 20 characters.%n", rating.getCode()));
			}
		}
		if (rating.getDescription() != null && rating.getDescription().length() > 255) {
			errMsg.append(
					String.format("The description (%s) cannot exceed 255 characters.%n", rating.getDescription()));
		}
		if (rating.getAgency() == null) {
			errMsg.append("The rating agency is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> ratingService.saveRating(rating));
	}

	public boolean deleteRating(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The rating id must be positive.");
		}
		return SecurityUtil.runEx(() -> ratingService.deleteRating(id));
	}

	public Set<RatingAssignment> getRatingAssignmentsByRatableId(long ratableId, String ratableType)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (ratableId <= 0) {
			errMsg.append(String.format("The ratable object id must be positive.%n"));
		}
		if (StringUtils.isBlank(ratableType)) {
			errMsg.append("The ratable object type cannot be empty.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.run(() -> ratingService.getRatingAssignmentsByRatableId(ratableId, ratableType));
	}

	public long saveRatingAssignment(RatingAssignment ratingAssignment) throws TradistaBusinessException {
		if (ratingAssignment == null) {
			throw new TradistaBusinessException("The rating assignment cannot be null.");
		}
		if (ratingAssignment.getRatable() == null) {
			throw new TradistaBusinessException("The ratable object is mandatory.");
		}
		if (ratingAssignment.getRating() == null) {
			throw new TradistaBusinessException("The rating is mandatory.");
		}
		if (ratingAssignment.getValidFrom() == null) {
			throw new TradistaBusinessException("The valid from date is mandatory.");
		}
		if (ratingAssignment.getValidTo() != null
				&& ratingAssignment.getValidFrom().isAfter(ratingAssignment.getValidTo())) {
			throw new TradistaBusinessException("The valid from date cannot be after the valid to date.");
		}
		return SecurityUtil.runEx(() -> ratingService.saveRatingAssignment(ratingAssignment));
	}

	public boolean deleteRatingAssignment(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The rating assignment id must be positive.");
		}
		return SecurityUtil.runEx(() -> ratingService.deleteRatingAssignment(id));
	}

}