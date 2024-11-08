package org.eclipse.tradista.ai.reasoning.common.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.eclipse.tradista.ai.reasoning.asp.atom.AtomMapper;
import org.jboss.ejb3.annotation.SecurityDomain;

import asp4j.lang.AnswerSet;
import asp4j.mapping.annotations.Arg;
import asp4j.program.Program;
import asp4j.program.ProgramBuilder;
import asp4j.solver.SolverClingo;
import asp4j.solver.SolverException;
import asp4j.solver.object.Filter;
import asp4j.solver.object.ObjectSolver;
import asp4j.solver.object.ObjectSolverImpl;
import org.eclipse.tradista.ai.reasoning.common.model.Formula;
import org.eclipse.tradista.ai.reasoning.common.persistence.FormulaSQL;
import org.eclipse.tradista.ai.reasoning.common.util.TradistaAIProperties;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class FormulaServiceBean implements FormulaService {

	@Override
	public long saveFormula(Formula formula) throws TradistaBusinessException {
		return FormulaSQL.saveFormula(formula);
	}

	@Override
	public List<Formula> getAllFormulas() {
		return FormulaSQL.getAllFormulas();
	}

	@Override
	public boolean saveFormulas(Formula... formulas) throws TradistaBusinessException {
		List<Formula> validatedFormulas = validateFormulas(formulas);
		return FormulaSQL.saveFormulas(validatedFormulas.toArray(new Formula[validatedFormulas.size()]));
	}

	@Override
	public List<Formula> validateFormulas(Formula... formulas) throws TradistaBusinessException {
		List<Formula> newFormulas = Arrays.asList(formulas);
		List<Formula> existingFormulas = FormulaSQL.getAllFormulas();
		Program<Object> program = null;
		StringBuilder sBuilder = new StringBuilder();

		if (existingFormulas != null && !existingFormulas.isEmpty()) {
			for (Formula f : existingFormulas) {
				sBuilder.append(f.getFormula());
			}
		}
		for (Formula f : formulas) {
			sBuilder.append(f.getFormula());
		}
		ObjectSolver solver = new ObjectSolverImpl(new SolverClingo());

		File file = new File(TradistaAIProperties.getSolverPath() + File.separator + "kb.txt");
		try {
			file.delete();
			file.createNewFile();

			FileOutputStream fop;

			fop = new FileOutputStream(file);

			// get the content in bytes
			byte[] contentInBytes = sBuilder.toString().getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		program = new ProgramBuilder<>().add(file).build();

		try {
			solver.getAnswerSets(program);
		} catch (SolverException se) {
			se.printStackTrace();
			return null;
		}
		return newFormulas;
	}

	@Override
	public boolean query(String functionName, String... parameters) throws TradistaBusinessException {
		List<Formula> formulas = FormulaSQL.getAllFormulas();
		if (formulas == null || formulas.isEmpty()) {
			throw new TradistaBusinessException("The knowledge base doesn't contain any formula.");
		}

		List<Formula> existingFormulas = FormulaSQL.getAllFormulas();
		Program<Object> program = null;
		StringBuilder sBuilder = new StringBuilder();

		if (existingFormulas != null && !existingFormulas.isEmpty()) {
			for (Formula f : existingFormulas) {
				sBuilder.append(f.getFormula());
			}
		}

		ObjectSolver solver = new ObjectSolverImpl(new SolverClingo());

		File file = new File(TradistaAIProperties.getSolverPath() + File.separator + "kb.txt");
		try {
			file.delete();
			file.createNewFile();

			FileOutputStream fop;

			fop = new FileOutputStream(file);

			// get the content in bytes
			byte[] contentInBytes = sBuilder.toString().getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		program = new ProgramBuilder<>().add(file).build();

		Class<?> atomClass = AtomMapper.getAtom(functionName);
		List<AnswerSet<Object>> answerSet = null;
		try {
			answerSet = solver.getAnswerSets(program, new Filter(atomClass));
		} catch (SolverException se) {
			se.printStackTrace();
		}

		if (answerSet != null && !answerSet.isEmpty()) {
			if (parameters.length == 0) {
				return true;
			} else {
				for (AnswerSet<Object> o : answerSet) {
					for (Object a : o.atoms()) {
						int i = 0;
						int ok = 0;
						boolean ko = false;
						for (String p : parameters) {
							for (Method m : a.getClass().getMethods()) {
								Arg argAnnotation = m.getAnnotation(Arg.class);
								if (argAnnotation == null) {
									continue;
								}
								if (argAnnotation.value() == i) {
									try {
										String v = (String) m.invoke(a);
										if (p.equals(v)) {
											ok++;
										} else {
											ko = true;
										}
										break;
									} catch (IllegalAccessException | InvocationTargetException e) {
										// Should not appear here.
									}
								}
							}
							if (ko) {
								break;
							} else {
								if (ok == parameters.length) {
									return true;
								}
							}
							i++;
						}
					}
				}
			}
		}
		return false;
	}
}