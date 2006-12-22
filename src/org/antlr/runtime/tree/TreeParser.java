package org.antlr.runtime.tree;

import org.antlr.runtime.*;

import java.util.List;
import java.util.ArrayList;

/** A parser for a stream of tree nodes.  "tree grammars" result in a subclass
 *  of this.  All the error reporting and recovery is shared with Parser via
 *  the BaseRecognizer superclass.
*/
public class TreeParser extends BaseRecognizer {
	public static final int DOWN = Token.DOWN;
	public static final int UP = Token.UP;

	protected TreeNodeStream input;

	public TreeParser(TreeNodeStream input) {
		setTreeNodeStream(input);
	}

	public void reset() {
		super.reset(); // reset all recognizer state variables
		if ( input!=null ) {
			input.seek(0); // rewind the input
		}
	}

	/** Set the input stream */
	public void setTreeNodeStream(TreeNodeStream input) {
		this.input = input;
	}

	public TreeNodeStream getTreeNodeStream() {
		return input;
	}

	/** Match '.' in tree parser has special meaning.  Skip node or
	 *  entire tree if node has children.  If children, scan until
	 *  corresponding UP node.
	 */
	public void matchAny(IntStream ignore) { // ignore stream, copy of this.input
		errorRecovery = false;
		failed = false;
		Object look = input.LT(1);
		if ( input.getTreeAdaptor().getChildCount(look)==0 ) {
			input.consume(); // not subtree, consume 1 node and return
			return;
		}
		// current node is a subtree, skip to corresponding UP.
		// must count nesting level to get right UP
		int level=0;
		int tokenType = input.getTreeAdaptor().getType(look);
		while ( tokenType!=Token.EOF && !(tokenType==UP && level==0) ) {
			input.consume();
			look = input.LT(1);
			tokenType = input.getTreeAdaptor().getType(look);
			if ( tokenType == DOWN ) {
				level++;
			}
			else if ( tokenType == UP ) {
				level--;
			}
		}
		input.consume(); // consume UP
	}

	/** Convert a List<RuleReturnScope> to List<StringTemplate> by copying
	 *  out the .st property.  Useful when converting from
	 *  list labels to template attributes:
	 *
	 *    a : ids+=rule -> foo(ids={toTemplates($ids)})
	 *      ;
	 */
	public List toTemplates(List retvals) {
		if ( retvals==null ) return null;
		List strings = new ArrayList(retvals.size());
		for (int i=0; i<retvals.size(); i++) {
			strings.add(((TreeRuleReturnScope)retvals.get(i)).getTemplate());
		}
		return strings;
	}

	/** We have DOWN/UP nodes in the stream that have no line info; override.
	 *  plus we want to alter the exception type.
	 */
	protected void mismatch(IntStream input, int ttype, BitSet follow)
		throws RecognitionException
	{
		MismatchedTreeNodeException mte =
			new MismatchedTreeNodeException(ttype, (TreeNodeStream)input);
		recoverFromMismatchedToken(input, mte, ttype, follow);
	}

	public void traceIn(String ruleName, int ruleIndex)  {
		super.traceIn(ruleName, ruleIndex, input.LT(1));
	}

	public void traceOut(String ruleName, int ruleIndex)  {
		super.traceOut(ruleName, ruleIndex, input.LT(1));
	}

}
