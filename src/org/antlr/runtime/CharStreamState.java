package org.antlr.runtime;

/** When walking ahead with cyclic DFA (or in the future for syntactic
 *  predicates), we need to record the state of the input stream (char index,
 *  line, etc...) so that we can rewind the state after scanning ahead.
 */
class CharStreamState {
	int p;
	int line;
	int charPositionInLine;
}
