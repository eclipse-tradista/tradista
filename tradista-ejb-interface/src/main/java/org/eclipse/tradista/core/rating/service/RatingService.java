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

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.rating.model.Rating;
import org.eclipse.tradista.core.rating.model.RatingAgency;
import org.eclipse.tradista.core.rating.model.RatingAssignment;

import jakarta.ejb.Remote;

@Remote
public interface RatingService {

	long saveRatingAgency(RatingAgency ratingAgency) throws TradistaBusinessException;

	boolean deleteRatingAgency(long id) throws TradistaBusinessException;

	Set<RatingAgency> getAllRatingAgencies();

	RatingAgency getRatingAgencyById(long id);

	RatingAgency getRatingAgencyByName(String name);

	long saveRating(Rating rating) throws TradistaBusinessException;

	boolean deleteRating(long id) throws TradistaBusinessException;

	Set<Rating> getAllRatings();

	Set<Rating> getRatingsByAgencyId(long agencyId);

	Rating getRatingById(long id);

	long saveRatingAssignment(RatingAssignment ratingAssignment) throws TradistaBusinessException;

	boolean deleteRatingAssignment(long id) throws TradistaBusinessException;

	Set<RatingAssignment> getRatingAssignmentsByRatableId(long ratableId, String ratableType);

}