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
package org.eclipse.tradista.core.rating.ui.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.ClientUtil;
import org.eclipse.tradista.core.rating.model.Rating;
import org.eclipse.tradista.core.rating.model.RatingAgency;
import org.eclipse.tradista.core.rating.service.RatingBusinessDelegate;
import org.primefaces.event.CellEditEvent;
import jakarta.faces.component.UIComponent;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class RatingAgencyController implements Serializable {

	private static final long serialVersionUID = 1L;

	private RatingAgency agency;

	private List<RatingAgency> allAgencies;

	private List<RatingDTO> ratings;

	private String newAgencyName;

	private String newRatingCode;
	private String newRatingDescription;

	private RatingBusinessDelegate ratingBusinessDelegate;

	@PostConstruct
	public void init() {
		ratingBusinessDelegate = new RatingBusinessDelegate();
		loadAllAgencies();
	}

	private void loadAllAgencies() {
		Set<RatingAgency> agencies = ratingBusinessDelegate.getAllRatingAgencies();
		allAgencies = new ArrayList<>();
		if (agencies != null) {
			for (RatingAgency ag : agencies) {
				if (ag.isActive()) {
					allAgencies.add(ag);
				}
			}
			Collections.sort(allAgencies);
		}
	}

	public void load() {
		if (agency != null) {
			try {
				Set<Rating> agsRatings = ratingBusinessDelegate.getRatingsByAgencyId(agency.getId());
				ratings = new ArrayList<>();
				if (agsRatings != null) {
					for (Rating r : agsRatings) {
						ratings.add(new RatingDTO(r));
					}
				}
			} catch (TradistaBusinessException e) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
			}
		}
	}

	public void saveAgency() {
		if (StringUtils.isBlank(newAgencyName)) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Agency name is mandatory."));
			return;
		}
		try {
			RatingAgency newAg = new RatingAgency(newAgencyName);
			long id = ratingBusinessDelegate.saveRatingAgency(newAg);
			newAg.setId(id);
			allAgencies.add(newAg);
			Collections.sort(allAgencies);
			agency = newAg;
			newAgencyName = StringUtils.EMPTY;
			load(); // refresh ratings
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Rating Agency successfully saved."));
		} catch (TradistaBusinessException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
		}
	}

	public void saveRating() {
		if (agency == null) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select an agency first."));
			return;
		}
		if (StringUtils.isBlank(newRatingCode)) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Rating code is mandatory."));
			return;
		}
		if (ratings != null) {
			for (RatingDTO r : ratings) {
				if (r.getCode().equals(newRatingCode)) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Error", "A rating with this code already exists."));
					return;
				}
			}
		}
		try {
			Rating rating = new Rating(newRatingCode, agency);
			rating.setDescription(newRatingDescription);
			long id = ratingBusinessDelegate.saveRating(rating);
			rating.setId(id);
			if (ratings == null) {
				ratings = new ArrayList<>();
			}
			ratings.add(new RatingDTO(rating));
			newRatingCode = StringUtils.EMPTY;
			newRatingDescription = StringUtils.EMPTY;
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Rating successfully saved."));
		} catch (TradistaBusinessException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
		}
	}

	public void deleteRating(RatingDTO ratingDto) {
		try {
			ratingBusinessDelegate.deleteRating(ratingDto.getId());
			ratings.remove(ratingDto);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Rating successfully deleted."));
		} catch (TradistaBusinessException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
		}
	}

	public void deleteAgency() {
		if (agency == null) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select an agency first."));
			return;
		}
		try {
			boolean softDeleted = false;
			try {
				ratingBusinessDelegate.deleteRatingAgency(agency.getId());
			} catch (TradistaBusinessException tbe) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", tbe.getMessage()));
				softDeleted = true;
			}

			if (!softDeleted) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Rating Agency successfully deleted."));
			}
			allAgencies.remove(agency);
			agency = null;
			if (ratings != null) {
				ratings.clear();
			}
			loadAllAgencies();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
		}
	}

	public void onRatingEdit(CellEditEvent<String> event) {
		String oldValue = event.getOldValue();
		String newValue = event.getNewValue();

		if (newValue != null && !newValue.equals(oldValue)) {
			int rowIndex = event.getRowIndex();
			RatingDTO editedRating = ratings.get(rowIndex);

			Map<String, Object> attributes = ((UIComponent) event.getColumn()).getAttributes();
			boolean isCodeColumn = "code".equals(attributes.get("colKey"));

			if (isCodeColumn) {
				for (RatingDTO r : ratings) {
					if (r != editedRating && r.getCode().equals(newValue)) {
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
								"Error", "A rating with this code already exists."));
						editedRating.setCode(oldValue);
						return;
					}
				}
			}

			try {
				Rating realRating = new Rating(editedRating.getCode(), agency);
				realRating.setId(editedRating.getId());
				realRating.setDescription(editedRating.getDescription());
				ratingBusinessDelegate.saveRating(realRating);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Rating successfully updated."));
			} catch (TradistaBusinessException e) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
				if (isCodeColumn) {
					editedRating.setCode(oldValue);
				} else {
					editedRating.setDescription(oldValue);
				}
			}
		}
	}

	public RatingAgency getAgency() {
		return agency;
	}

	public void setAgency(RatingAgency agency) {
		this.agency = agency;
	}

	public List<RatingAgency> getAllAgencies() {
		return allAgencies;
	}

	public List<RatingDTO> getRatings() {
		return ratings;
	}

	public String getNewAgencyName() {
		return newAgencyName;
	}

	public void setNewAgencyName(String newAgencyName) {
		this.newAgencyName = newAgencyName;
	}

	public String getNewRatingCode() {
		return newRatingCode;
	}

	public void setNewRatingCode(String newRatingCode) {
		this.newRatingCode = newRatingCode;
	}

	public String getNewRatingDescription() {
		return newRatingDescription;
	}

	public void setNewRatingDescription(String newRatingDescription) {
		this.newRatingDescription = newRatingDescription;
	}

	public boolean isAdmin() {
		return ClientUtil.currentUserIsAdmin();
	}
}
