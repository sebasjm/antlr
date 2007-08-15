package org.antlr.runtime.tree;

import java.util.List;

/** Queues up nodes matched on left side of -> in a tree parser. This is
 *  the analog of RewriteRuleTokenStream for normal parsers. 
 */
public class RewriteRuleNodeStream extends RewriteRuleElementStream {

	public RewriteRuleNodeStream(TreeAdaptor adaptor, String elementDescription) {
		super(adaptor, elementDescription);
	}

	/** Create a stream with one element */
	public RewriteRuleNodeStream(TreeAdaptor adaptor,
								 String elementDescription,
								 Object oneElement)
	{
		super(adaptor, elementDescription, oneElement);
	}

	/** Create a stream, but feed off an existing list */
	public RewriteRuleNodeStream(TreeAdaptor adaptor,
								 String elementDescription,
								 List elements)
	{
		super(adaptor, elementDescription, elements);
	}

	public Object nextNode() {
		return _next();
	}

	protected Object toTree(Object el) {
		return adaptor.dupNode(el);
	}

	protected Object dup(Object el) {
		// we dup every node, so don't have to worry about calling dup; short-
		// circuited next() so it doesn't call.
		throw new UnsupportedOperationException("dup can't be called for a node stream.");
	}
}
