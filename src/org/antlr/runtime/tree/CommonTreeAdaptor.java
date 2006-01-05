package org.antlr.runtime.tree;

import org.antlr.runtime.Token;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.ClassicToken;

import java.util.List;
import java.lang.reflect.Method;

/** A TreeAdaptor that works with any Tree implementation.  It provides
 *  really just factory methods; all the work is done by BaseTreeAdaptor.
 *  If you would like to have different tokens created than ClassicToken
 *  objects, you need to override this and then set the parser tree adaptor to
 *  use your subclass.
 *
 *  To get your parser to build nodes of a different type, override
 *  create(Token).
 */
public class CommonTreeAdaptor extends BaseTreeAdaptor {
	/** Duplicate a node.  This is part of the factory;
	 *	override if you want another kind of node to be built.
	 *
	 *  I could use reflection to prevent having to override this
	 *  but reflection is slow.
	 */
	public Object dupNode(Object treeNode) {
		return ((Tree)treeNode).dupNode();
	}

	public Object create(Token payload) {
		return new CommonTree(payload);
	}

	/** Create an imaginary token from a type and text */
	public Token createToken(int tokenType, String text) {
		return new ClassicToken(tokenType, text);
	}

	/** Create an imaginary token, copying the contents of a previous token */
	public Token createToken(Token fromToken) {
		return new ClassicToken(fromToken);
	}
}
