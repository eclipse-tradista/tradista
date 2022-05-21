package finance.tradista.ai.reasoning.fol.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;

import finance.tradista.ai.reasoning.fol.model.FolFormula;
import finance.tradista.ai.reasoning.fol.persistence.FolFormulaSQL;
import finance.tradista.core.common.exception.TradistaBusinessException;
import net.sf.tweety.commons.Formula;
import net.sf.tweety.commons.ParserException;
import net.sf.tweety.logics.fol.parser.FolParser;
import net.sf.tweety.logics.fol.reasoner.FolReasoner;
import net.sf.tweety.logics.fol.reasoner.SimpleFolReasoner;
import net.sf.tweety.logics.fol.syntax.FolBeliefSet;

/*
 * Copyright 2019 Olivier Asuncion
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

@Stateless
public class FolFormulaServiceBean implements FolFormulaService {

	@Override
	public long saveFolFormula(FolFormula folFormula) throws TradistaBusinessException {
		return FolFormulaSQL.saveFolFormula(folFormula);
	}

	@Override
	public List<FolFormula> getAllFolFormulas() {
		return FolFormulaSQL.getAllFolFormulas();
	}

	@Override
	public boolean saveFolFormulas(FolFormula... formulas) throws TradistaBusinessException {
		List<FolFormula> validatedFormulas = validateFolFormulas(formulas);
		return FolFormulaSQL.saveFolFormulas(validatedFormulas.toArray(new FolFormula[validatedFormulas.size()]));
	}

	@Override
	public List<FolFormula> validateFolFormulas(FolFormula... formulas) throws TradistaBusinessException {
		List<FolFormula> newFormulas = Arrays.asList(formulas);
		List<FolFormula> existingFormulas = FolFormulaSQL.getAllFolFormulas();
		Set<net.sf.tweety.logics.fol.syntax.FolFormula> folFormulas = new HashSet<net.sf.tweety.logics.fol.syntax.FolFormula>();
		FolBeliefSet beliefSet = null;
		if (existingFormulas != null && !existingFormulas.isEmpty()) {
			for (FolFormula formula : existingFormulas) {
				folFormulas.add(formula.getFolFormula());
			}
			beliefSet = new net.sf.tweety.logics.fol.syntax.FolBeliefSet(folFormulas);
		} else {
			beliefSet = new net.sf.tweety.logics.fol.syntax.FolBeliefSet();
		}
		FolParser parser = new FolParser();
		try {
			if (!folFormulas.isEmpty()) {
				parser.parseBeliefBase(beliefSet.toString());
			}
			for (FolFormula formula : formulas) {
				Formula f = parser.parseFormula(formula.getFormula());
				formula.setFolFormula((net.sf.tweety.logics.fol.syntax.FolFormula) f);
				newFormulas.add(formula);
			}
		} catch (ParserException pe) {
			throw new TradistaBusinessException(pe.getMessage());
		} catch (IOException ioe) {
			throw new TradistaBusinessException(ioe.getMessage());
		}
		return newFormulas;
	}

	@Override
	public boolean query(String functionName, String... parameters) throws TradistaBusinessException {
		List<FolFormula> formulas = FolFormulaSQL.getAllFolFormulas();
		if (formulas == null || formulas.isEmpty()) {
			throw new TradistaBusinessException("The knowledge base doesn't contain any formula.");
		}

		StringBuilder queryFormulaString = new StringBuilder();
		queryFormulaString.append(functionName);
		queryFormulaString.append("(");
		if (parameters != null && parameters.length > 0) {
			for (String param : parameters) {
				queryFormulaString.append(param + ",");
			}
			queryFormulaString.deleteCharAt(queryFormulaString.length() - 1);
		}
		queryFormulaString.append(")");
		FolFormula queryFormula = new FolFormula(queryFormulaString.toString());

		Set<net.sf.tweety.logics.fol.syntax.FolFormula> folFormulas = new HashSet<net.sf.tweety.logics.fol.syntax.FolFormula>();
		for (FolFormula formula : formulas) {
			folFormulas.add(formula.getFolFormula());
		}
		FolBeliefSet beliefSet = new net.sf.tweety.logics.fol.syntax.FolBeliefSet(folFormulas);
		FolReasoner reasoner = new SimpleFolReasoner();
		Boolean answer = reasoner.query(beliefSet, queryFormula.getFolFormula());
		return answer;
	}

}