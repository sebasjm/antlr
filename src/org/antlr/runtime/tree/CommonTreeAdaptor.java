package org.antlr.runtime.tree;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;

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

	/** Tell me how to create a token for use with imaginary token nodes.
	 *  For example, there is probably no input symbol associated with imaginary
	 *  token DECL, but you need to create it as a payload or whatever for
	 *  the DECL node as in ^(DECL type ID).
	 *
	 *  If you care what the token payload objects' type is, you should
	 *  override this method and any other createToken variant.
	 */
	public Token createToken(int tokenType, String text) {
		return new CommonToken(tokenType, text);
	}

	/** Tell me how to create a token for use with imaginary token nodes.
	 *  For example, there is probably no input symbol associated with imaginary
	 *  token DECL, but you need to create it as a payload or whatever for
	 *  the DECL node as in ^(DECL type ID).
	 *
	 *  This is a variant of createToken where the new token is derived from
	 *  an actual real input token.  Typically this is for converting '{'
	 *  tokens to BLOCK etc...  You'll see
	 *
	 *    r : lc='{' ID+ '}' -> ^(BLOCK[$lc] ID+) ;
	 *
	 *  If you care what the token payload objects' type is, you should
	 *  override this method and any other createToken variant.
	 */
	public Token createToken(Token fromToken) {
		return new CommonToken(fromToken);
	}

	/** track start/stop token for subtree root created for a rule */
	public void setTokenBoundaries(Object t, Token startToken, Token stopToken) {
		if ( t==null ) {
			return;
		}
		int start = 0;
		int stop = 0;
		if ( startToken!=null ) {
			start = startToken.getTokenIndex();
		}
		if ( stopToken!=null ) {
			stop = stopToken.getTokenIndex();
		}
		((CommonTree)t).startIndex = start;
		((CommonTree)t).stopIndex = stop;
	}

	public int getTokenStartIndex(Object t) {
		return ((CommonTree)t).startIndex;
	}

	public int getTokenStopIndex(Object t) {
		return ((CommonTree)t).stopIndex;
	}
}
