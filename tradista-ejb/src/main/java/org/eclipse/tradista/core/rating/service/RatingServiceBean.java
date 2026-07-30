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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.rating.model.Rating;
import org.eclipse.tradista.core.rating.model.RatingAgency;
import org.eclipse.tradista.core.rating.model.RatingAssignment;
import org.eclipse.tradista.core.rating.persistence.RatingSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class RatingServiceBean implements RatingService {

	@Override
	public long saveRatingAgency(RatingAgency ratingAgency) throws TradistaBusinessException {
		return RatingSQL.saveRatingAgency(ratingAgency);
	}

	@Override
	public boolean deleteRatingAgency(long id) throws TradistaBusinessException {
		boolean hasAssignments = false;
		Set<Rating> ratings = RatingSQL.getRatingsByAgencyId(id);
		if (ratings != null) {
			for (Rating rating : ratings) {
				Set<RatingAssignment> assignments = RatingSQL.getRatingAssignmentsByRatingId(rating.getId());
				if (assignments != null && !assignments.isEmpty()) {
					hasAssignments = true;
					break;
				}
			}
		}

		if (!hasAssignments) {
			if (ratings != null) {
				for (Rating rating : ratings) {
					RatingSQL.deleteRating(rating.getId());
				}
			}
			return RatingSQL.hardDeleteRatingAgency(id);
		} else {
			boolean deleted = RatingSQL.deleteRatingAgency(id);
			if (deleted) {
				java.time.LocalDate today = LocalDate.now(ZoneId.systemDefault());
				if (ratings != null) {
					for (Rating rating : ratings) {
						Set<RatingAssignment> assignments = RatingSQL.getRatingAssignmentsByRatingId(rating.getId());
						if (assignments != null) {
							for (RatingAssignment assignment : assignments) {
								if (assignment.getValidTo() == null || assignment.getValidTo().isAfter(today)) {
									assignment.setValidTo(today);
									RatingSQL.saveRatingAssignment(assignment);
								}
							}
						}
					}
				}
				throw new TradistaBusinessException(
						"This agency is used by existing ratings. It cannot be physically deleted, but it has been disabled (archived).");
			}
			return deleted;
		}
	}

	@Override
	public Set<RatingAgency> getAllRatingAgencies() {
		return RatingSQL.getAllRatingAgencies();
	}

	@Override
	public RatingAgency getRatingAgencyById(long id) {
		return RatingSQL.getRatingAgencyById(id);
	}

	@Override
	public RatingAgency getRatingAgencyByName(String name) {
		return RatingSQL.getRatingAgencyByName(name);
	}

	@Override
	public long saveRating(Rating rating) throws TradistaBusinessException {
		return RatingSQL.saveRating(rating);
	}

	@Override
	public boolean deleteRating(long id) throws TradistaBusinessException {
		Set<RatingAssignment> assignments = RatingSQL.getRatingAssignmentsByRatingId(id);
		if (assignments != null && !assignments.isEmpty()) {
			throw new TradistaBusinessException(String.format(
					"This rating is used in %d existing assignments and cannot be deleted.", assignments.size()));
		}
		return RatingSQL.deleteRating(id);
	}

	@Override
	public Set<Rating> getAllRatings() {
		return RatingSQL.getAllRatings();
	}

	@Override
	public Set<Rating> getRatingsByAgencyId(long agencyId) {
		return RatingSQL.getRatingsByAgencyId(agencyId);
	}

	@Override
	public Rating getRatingById(long id) {
		return RatingSQL.getRatingById(id);
	}

	@Override
	public long saveRatingAssignment(RatingAssignment ratingAssignment) throws TradistaBusinessException {
		// Historical validation: no overlapping dates for the same agency
		Set<RatingAssignment> existingAssignments = getRatingAssignmentsByRatableId(
				ratingAssignment.getRatable().getId(), ratingAssignment.getRatable().getClass().getSimpleName());

		if (existingAssignments != null) {
			for (RatingAssignment existing : existingAssignments) {
				if (existing.getId() != ratingAssignment.getId() && existing.getRating().getAgency()
						.getId() == ratingAssignment.getRating().getAgency().getId()) {

					// Check overlap
					boolean overlap = true;
					if (ratingAssignment.getValidTo() != null
							&& ratingAssignment.getValidTo().isBefore(existing.getValidFrom())) {
						overlap = false;
					}
					if (existing.getValidTo() != null
							&& ratingAssignment.getValidFrom().isAfter(existing.getValidTo())) {
						overlap = false;
					}

					if (overlap) {
						throw new TradistaBusinessException(String.format(
								"Overlapping rating found for agency %s. A rated object can only have one active rating per agency at any given date.",
								ratingAssignment.getRating().getAgency().getName()));
					}
				}
			}
		}

		return RatingSQL.saveRatingAssignment(ratingAssignment);
	}

	@Override
	public boolean deleteRatingAssignment(long id) throws TradistaBusinessException {
		return RatingSQL.deleteRatingAssignment(id);
	}

	@Override
	public Set<RatingAssignment> getRatingAssignmentsByRatableId(long ratableId, String ratableType) {
		return RatingSQL.getRatingAssignmentsByRatableId(ratableId, ratableType);
	}

}