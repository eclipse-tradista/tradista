package asp4j.solver.object;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import asp4j.lang.AnswerSet;
import asp4j.lang.Atom;
import asp4j.mapping.MappingException;
import asp4j.program.Program;
import asp4j.program.ProgramBuilder;
import asp4j.solver.ReasoningMode;
import asp4j.solver.Solver;
import asp4j.solver.SolverException;

/*Copyright (c) 2013, Harald Beck
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

  1. Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
  
  2. Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
  
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*/

/**
 *
 * @author hbeck May 25, 2013
 */
public class ObjectSolverImpl implements ObjectSolver {

	private final Solver solver;

	public ObjectSolverImpl(Solver solver) {
		this.solver = solver;
	}

	@Override
	public List<AnswerSet<Object>> getAnswerSets(Program<Object> program) throws SolverException {
		return Collections.unmodifiableList(computeAnswerSets(program, new Binding(), new Filter()));
	}

	@Override
	public List<AnswerSet<Object>> getAnswerSets(Program<Object> program, Binding binding) throws SolverException {
		return Collections.unmodifiableList(computeAnswerSets(program, binding, new Filter()));
	}

	@Override
	public List<AnswerSet<Object>> getAnswerSets(Program<Object> program, Filter filter) throws SolverException {
		return Collections.unmodifiableList(computeAnswerSets(program, new Binding(), filter));
	}

	@Override
	public List<AnswerSet<Object>> getAnswerSets(Program<Object> program, Binding binding, Filter filter)
			throws SolverException {
		return Collections.unmodifiableList(computeAnswerSets(program, binding, filter));
	}

	@Override
	public Set<Object> getConsequence(Program<Object> program, ReasoningMode mode) throws SolverException {
		return getConsequence(program, mode, new Binding(), new Filter());
	}

	@Override
	public Set<Object> getConsequence(Program<Object> program, ReasoningMode mode, Binding binding)
			throws SolverException {
		return getConsequence(program, mode, binding, new Filter());
	}

	@Override
	public Set<Object> getConsequence(Program<Object> program, ReasoningMode mode, Filter filter)
			throws SolverException {
		return getConsequence(program, mode, new Binding(), filter);
	}

	@Override
	public Set<Object> getConsequence(Program<Object> program, ReasoningMode mode, Binding binding, Filter filter)
			throws SolverException {
		Program<Atom> atomProgram = getAtomProgram(program, binding, filter);
		Set<Atom> atoms = solver.getConsequence(atomProgram, mode);
		try {
			return binding.filterAndMap(atoms, filter);
		} catch (MappingException e) {
			throw new SolverException(e);
		}
	}

	private List<AnswerSet<Object>> computeAnswerSets(Program<Object> program, Binding binding, Filter filter)
			throws SolverException {
		Program<Atom> atomProgram = getAtomProgram(program, binding, filter);
		List<AnswerSet<Atom>> answerSets = solver.getAnswerSets(atomProgram);
		try {
			return binding.filterAndMap(answerSets, filter);
		} catch (MappingException e) {
			throw new SolverException(e);
		}
	}

	private Program<Atom> getAtomProgram(Program<Object> program, Binding binding, Filter filter)
			throws SolverException {
		prepareIO(program, binding, filter);
		ProgramBuilder<Atom> builder = new ProgramBuilder<>();
		builder.addFiles(program.getFiles());
		try {
			for (Object input : program.getInput()) {
				builder.add((Atom) binding.mapAsLangElem(input));
			}
		} catch (MappingException e) {
			throw new SolverException(e);
		}
		return builder.build();
	}

	private void prepareIO(Program<Object> program, Binding binding, Filter filter) throws SolverException {
		Set<Class<?>> inputClasses = new HashSet<>();
		for (Object object : program.getInput()) {
			inputClasses.add(object.getClass());
		}
		try {
			// input
			binding.addAll(inputClasses);
			// output
			Set<Class<?>> filterClasses = filter.getClasses();
			if (filterClasses.isEmpty()) {
				filter.addAll(inputClasses);
			} else {
				binding.addAll(filterClasses);
			}
		} catch (MappingException e) {
			throw new SolverException(e);
		}
	}
}
