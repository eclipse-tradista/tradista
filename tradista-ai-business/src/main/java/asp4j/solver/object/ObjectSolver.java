package asp4j.solver.object;

import java.util.List;
import java.util.Set;

import asp4j.lang.AnswerSet;
import asp4j.program.Program;
import asp4j.solver.ReasoningMode;
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
 * @author hbeck May 19, 2013
 */
public interface ObjectSolver {

	List<AnswerSet<Object>> getAnswerSets(Program<Object> program) throws SolverException;

	List<AnswerSet<Object>> getAnswerSets(Program<Object> program, Binding binding) throws SolverException;

	List<AnswerSet<Object>> getAnswerSets(Program<Object> program, Filter filter) throws SolverException;

	List<AnswerSet<Object>> getAnswerSets(Program<Object> program, Binding binding, Filter filter)
			throws SolverException;

	Set<Object> getConsequence(Program<Object> program, ReasoningMode mode) throws SolverException;

	Set<Object> getConsequence(Program<Object> program, ReasoningMode mode, Binding binding) throws SolverException;

	Set<Object> getConsequence(Program<Object> program, ReasoningMode mode, Filter filter) throws SolverException;

	Set<Object> getConsequence(Program<Object> program, ReasoningMode mode, Binding binding, Filter filter)
			throws SolverException;

}
