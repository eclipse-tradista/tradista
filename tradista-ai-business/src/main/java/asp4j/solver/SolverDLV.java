package asp4j.solver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

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
public class SolverDLV extends SolverBase {

	@Override
	protected String solverCommand() {
		return "dlv -silent";
	}

	@Override
	protected List<String> getAnswerSetStrings(Process exec) throws IOException {
		InputStream inputStream = exec.getInputStream();
		List<String> allLines = IOUtils.readLines(inputStream);
		if (allLines.isEmpty()) {
			throw new IOException("dlv output error: lines empty");
		}
		List<String> answerSetLines = new ArrayList<>();
		for (String line : allLines) {
			if (!line.startsWith("{")) {
				throw new IOException("dlv output error: not an answer set: " + line);
			}
			answerSetLines.add(line);
		}
		return answerSetLines;
	}

	/**
	 * 
	 * @param answerSetString "{atom_1,...,atom_n}"
	 * @return "atom_1,...,atom_n"
	 */
	@Override
	protected String prepareAnswerSetString(String answerSetString) {
		return answerSetString.substring(1, answerSetString.length() - 1);
	}

	@Override
	protected String atomDelimiter() {
		return ", ";
	}
}
