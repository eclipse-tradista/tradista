package asp4j.solver;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import asp4j.lang.AnswerSet;
import asp4j.lang.AnswerSetImpl;
import asp4j.lang.Atom;
import asp4j.program.Program;
import asp4j.util.ParseUtils;

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
 * @author hbeck Apr 14, 2013
 */
public abstract class SolverBase implements Solver {

	protected File inputFile;
	protected int lastProgramHashCode;
	protected List<AnswerSet<Atom>> lastProgramAnswerSets;

	public SolverBase() {
		inputFile = null;
		lastProgramAnswerSets = null;
	}

	/**
	 * command to start the solver, including params (excluding input programs)
	 *
	 * @return part before list of files
	 */
	protected abstract String solverCommand();

	/**
	 * @return list of strings, each representing an answer set
	 */
	protected abstract List<String> getAnswerSetStrings(Process exec) throws IOException;

	/**
	 * prepare answer set string for tokenization. e.g. surrounding braces may be
	 * removed.
	 *
	 * @param answerSetString
	 * @return string to tokenize based on standard configuration
	 * @see anwerSetDelimiter
	 */
	protected abstract String prepareAnswerSetString(String answerSetString);

	/**
	 * @return separator of atoms withing an answer set
	 */
	protected abstract String atomDelimiter();

	//
	// std implementations
	//
	protected void clear() {
		lastProgramAnswerSets = null;
	}

	@Override
	public List<AnswerSet<Atom>> getAnswerSets(Program<Atom> program) throws SolverException {
		if (lastProgramAnswerSets != null && program.hashCode() == lastProgramHashCode) {
			return lastProgramAnswerSets;
		}
		lastProgramHashCode = program.hashCode();
		preSolverExec(program);
		try {
			Process exec = Runtime.getRuntime().exec(solverCallString(program));
			List<String> answerSetStrings = getAnswerSetStrings(exec);
			postSolverExec(program);
			return lastProgramAnswerSets = Collections.unmodifiableList(mapAnswerSetStrings(answerSetStrings));
		} catch (IOException | ParseException e) {
			throw new SolverException(e);
		}
	}

	/**
	 * maps a list of answer sets, represented as strings, to a list of (low level)
	 * AnswerSet objects
	 */
	protected List<AnswerSet<Atom>> mapAnswerSetStrings(List<String> answerSetStrings) throws ParseException {
		List<AnswerSet<Atom>> answerSets = new ArrayList<>();
		for (String answerSetString : answerSetStrings) {
			answerSetString = prepareAnswerSetString(answerSetString);
			String[] atomStrings = answerSetString.split(atomDelimiter());
			Set<Atom> atoms = new HashSet<>();
			for (String atomString : atomStrings) {
				atoms.add(ParseUtils.parseAtom(atomString));
			}
			answerSets.add(new AnswerSetImpl<>(atoms));
		}
		return answerSets;
	}

	@Override
	public Set<Atom> getConsequence(Program<Atom> program, ReasoningMode mode) throws SolverException {
		List<AnswerSet<Atom>> as = getAnswerSets(program);
		switch (mode) {
		case BRAVE:
			return braveConsequence(as);
		case CAUTIOUS:
			return cautiousConsequence(as);
		default:
			return null;
		}
	}

	protected Set<Atom> cautiousConsequence(List<AnswerSet<Atom>> answerSets) {
		Set<Atom> intersection = new HashSet<>();
		Iterator<AnswerSet<Atom>> it = answerSets.iterator();
		if (it.hasNext()) {
			intersection.addAll(it.next().atoms());
			while (it.hasNext()) {
				intersection.retainAll(it.next().atoms());
			}
		}
		return Collections.unmodifiableSet(intersection);
	}

	protected Set<Atom> braveConsequence(List<AnswerSet<Atom>> answerSets) {
		Set<Atom> set = new HashSet<>();
		for (AnswerSet<Atom> answerSet : answerSets) {
			set.addAll(answerSet.atoms());
		}
		return Collections.unmodifiableSet(set);
	}

	/**
	 *
	 * @param program
	 * @return full call to solver, i.e., solver command plus programs
	 * @throws IOException
	 */
	protected String solverCallString(Program<Atom> program) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(solverCommand());
		for (File file : program.getFiles()) {
			sb.append(" ").append(file.getAbsolutePath());
		}
		if (!program.getInput().isEmpty()) {
			sb.append(" ").append(inputFile.getAbsolutePath());
		}
		return sb.toString();
	}

	protected File tempInputFile() throws IOException {
		if (inputFile == null) {
			inputFile = File.createTempFile("asp4j-tmp-prog-", ".lp");
			inputFile.deleteOnExit();
		}
		return inputFile;
	}

	/**
	 * executed before call to solver
	 */
	protected void preSolverExec(Program<Atom> program) throws SolverException {
		Collection<Atom> inputAtoms = program.getInput();
		if (inputAtoms.isEmpty()) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (Atom atom : inputAtoms) {
			sb.append(atom.toString());
		}
		try {
			FileUtils.writeStringToFile(tempInputFile(), sb.toString());
		} catch (IOException ex) {
			throw new SolverException(ex);
		}
	}

	/**
	 * executed after call to solver
	 */
	protected void postSolverExec(Program<Atom> program) {
	}
}
