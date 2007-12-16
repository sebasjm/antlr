/*
[The "BSD licence"]
Copyright (c) 2005-2007 Terence Parr
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/package org.antlr.tool;

import org.antlr.analysis.Label;
import org.antlr.analysis.NFAState;
import org.antlr.misc.Utils;

import java.util.*;

/** A tree of component (delegate) grammars.
 *
 *  Rules defined in delegates are "inherited" like multi-inheritance
 *  so you can override them.  All token types must be consistent across
 *  rules from all delegate grammars, so they must be stored here in one
 *  central place.
 *
 *  We have to start out assuming a composite grammar situation as we can't
 *  look into the grammar files a priori to see if there is a delegate
 *  statement.  Because of this, and to avoid duplicating token type tracking
 *  in each grammar, even single noncomposite grammars use one of these objects
 *  to track token types.
 */
public class CompositeGrammar {
	public CompositeGrammarTree delegateGrammarTreeRoot;

	/** Used to assign state numbers; all grammars in composite share common
	 *  NFA space.  This NFA tracks state numbers number to state mapping.
	 */
	public int stateCounter = 0;

	/** The NFA states in the NFA built from rules across grammars in composite.
	 *  Maps state number to NFAState object.
	 *  This is a Vector instead of a List because I need to be able to grow
	 *  this properly.  After talking to Josh Bloch, Collections guy at Sun,
	 *  I decided this was easiest solution.
	 */
	protected Vector<NFAState> numberToStateList = new Vector<NFAState>(1000);

	/** Token names and literal tokens like "void" are uniquely indexed.
     *  with -1 implying EOF.  Characters are different; they go from
     *  -1 (EOF) to \uFFFE.  For example, 0 could be a binary byte you
     *  want to lexer.  Labels of DFA/NFA transitions can be both tokens
     *  and characters.  I use negative numbers for bookkeeping labels
     *  like EPSILON. Char/String literals and token types overlap in the same
	 *  space, however.
     */
    protected int maxTokenType = Label.MIN_TOKEN_TYPE-1;

	/** Map token like ID (but not literals like "while") to its token type */
	protected Map tokenIDToTypeMap = new HashMap();

	/** Map token literals like "while" to its token type.  It may be that
	 *  WHILE="while"=35, in which case both tokenNameToTypeMap and this
	 *  field will have entries both mapped to 35.
	 */
	protected Map stringLiteralToTypeMap = new HashMap();

	/** Map a token type to its token name.
	 *  Must subtract MIN_TOKEN_TYPE from index.
	 */
	protected Vector<String> typeToTokenList = new Vector<String>();

	protected void initTokenSymbolTables() {
        // the faux token types take first NUM_FAUX_LABELS positions
		// then we must have room for the predefined runtime token types
		// like DOWN/UP used for tree parsing.
        typeToTokenList.setSize(Label.NUM_FAUX_LABELS+Label.MIN_TOKEN_TYPE-1);
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.INVALID, "<INVALID>");
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.EOT, "<EOT>");
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.SEMPRED, "<SEMPRED>");
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.SET, "<SET>");
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.EPSILON, Label.EPSILON_STR);
		typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.EOF, "EOF");
		typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.EOR_TOKEN_TYPE-1, "<EOR>");
		typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.DOWN-1, "DOWN");
		typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.UP-1, "UP");
        tokenIDToTypeMap.put("<INVALID>", Utils.integer(Label.INVALID));
        tokenIDToTypeMap.put("<EOT>", Utils.integer(Label.EOT));
        tokenIDToTypeMap.put("<SEMPRED>", Utils.integer(Label.SEMPRED));
        tokenIDToTypeMap.put("<SET>", Utils.integer(Label.SET));
        tokenIDToTypeMap.put("<EPSILON>", Utils.integer(Label.EPSILON));
		tokenIDToTypeMap.put("EOF", Utils.integer(Label.EOF));
		tokenIDToTypeMap.put("<EOR>", Utils.integer(Label.EOR_TOKEN_TYPE));
		tokenIDToTypeMap.put("DOWN", Utils.integer(Label.DOWN));
		tokenIDToTypeMap.put("UP", Utils.integer(Label.UP));
    }

	public CompositeGrammar() {
		initTokenSymbolTables();
	}

	public CompositeGrammar(Grammar g) {
		this();
		delegateGrammarTreeRoot = new CompositeGrammarTree(g);
	}
	
	public Rule getRule(String ruleName) {
		return delegateGrammarTreeRoot.getRule(ruleName);
	}

	/** Add delegate grammar as child of delegator */
	public void addGrammar(Grammar delegator, Grammar delegate) {
		// set root if empty tree
		if ( delegateGrammarTreeRoot == null ) {
			// whoever loads first must be master/delegator of any that follow
			// in terms of imports (does not apply across multiple ANTLR
			// invocations nor multiple grammars on command line.
			delegateGrammarTreeRoot = new CompositeGrammarTree(delegator);
		}
		// find delegator in tree so we can add a child to it
		CompositeGrammarTree t = delegateGrammarTreeRoot.findNode(delegator);
		t.addChild(new CompositeGrammarTree(delegate));
		// make sure new grammar shares this composite
		delegate.composite = this;
	}

	public void setDelegationRoot(Grammar root) {
		delegateGrammarTreeRoot = new CompositeGrammarTree(root);
	}

	/** Get list of all delegates from all grammars in the delegate subtree of g.
	 *  The grammars are in delegation tree preorder.  Don't include g itself
	 *  in list as it is not a delegate of itself.
	 */
	public List<Grammar> getDelegates(Grammar g) {
		CompositeGrammarTree t = delegateGrammarTreeRoot.findNode(g);
		List<Grammar> grammars = t.collectAllGrammars();
		Iterator it = grammars.iterator();
		// remove g from list of all grammars to make delegates
		it.next(); it.remove();
		return grammars;
	}

	public List<Grammar> getDirectDelegates(Grammar g) {
		CompositeGrammarTree t = delegateGrammarTreeRoot.findNode(g);
		List<CompositeGrammarTree> children = t.children;
		if ( children==null ) {
			return null;
		}
		List<Grammar> grammars = new ArrayList();
		for (int i = 0; children!=null && i < children.size(); i++) {
			CompositeGrammarTree child = (CompositeGrammarTree) children.get(i);
			grammars.add(child.grammar);
		}
		return grammars;
	}

	public List<Grammar> getDelegators(Grammar g) {
		if ( g==delegateGrammarTreeRoot.grammar ) {
			return null;
		}
		List<Grammar> grammars = new ArrayList();
		CompositeGrammarTree t = delegateGrammarTreeRoot.findNode(g);
		// walk backwards to root, collecting grammars
		CompositeGrammarTree p = t.parent;
		while ( p!=null ) {
			grammars.add(p.grammar);
			p = p.parent;
		}
		return grammars;
	}

	/** Get set of rules for grammar g that need to have manual delegation
	 *  methods.  This is the list of rules collected from all direct/indirect
	 *  delegates minus rules overridden in grammar g.
	 *
	 *  This return null except for the delegate root because it is the only
	 *  one that has to have a complete grammar rule interface.  The delegates
	 *  should not be instantiated directly for use as parsers (you can create
	 *  them to pass to the root parser's ctor as arguments).
	 */
	public Set<Rule> getDelegatedRules(Grammar g) {
		if ( g!=delegateGrammarTreeRoot.grammar ) {
			return null;
		}
		Set<Rule> rules = getAllImportedRules(g);
		for (Iterator it = rules.iterator(); it.hasNext();) {
			Rule r = (Rule) it.next();
			if ( g.getLocallyDefinedRule(r.name)!=null ) {
				it.remove(); // kill overridden rules
			}
		}
		return rules;
	}

	/** Get all rule definitions from all direct/indirect delegate grammars
	 *  of g.
	 */
	public Set<Rule> getAllImportedRules(Grammar g) {
		Set<String> ruleNames = new HashSet();
		Set<Rule> rules = new HashSet();
		CompositeGrammarTree subtreeRoot = delegateGrammarTreeRoot.findNode(g);
		List<Grammar> grammars = subtreeRoot.collectAllGrammars();
		// walk all grammars
		for (int i = 0; i < grammars.size(); i++) {
			Grammar delegate = (org.antlr.tool.Grammar) grammars.get(i);
			// for each rule in delegate, add to rules if no rule with that
			// name as been seen.  (can't use removeAll; wrong hashcode/equals on Rule)
			for (Iterator it = delegate.getRules().iterator(); it.hasNext();) {
				Rule r = (Rule)it.next();
				if ( !ruleNames.contains(r.name) ) {
					ruleNames.add(r.name); // track that we've seen this
					rules.add(r);
				}
			}
		}
		return rules;
	}

	public Grammar getGrammar(String grammarName) {
		CompositeGrammarTree t = delegateGrammarTreeRoot.findNode(grammarName);
		if ( t!=null ) {
			return t.grammar;
		}
		return null;
	}

	// NFA spans multiple grammars, must handle here

	public int getNewNFAStateNumber() {
		return stateCounter++;
	}
	
	public void addState(NFAState state) {
		numberToStateList.setSize(state.stateNumber+1); // make sure we have room
		numberToStateList.set(state.stateNumber, state);
	}

	public NFAState getState(int s) {
		return (NFAState)numberToStateList.get(s);
	}

	/*
	public void trackNFAStatesThatHaveLabeledEdge(Label label,
												  NFAState stateWithLabeledEdge)
	{
		Set<NFAState> states = typeToNFAStatesWithEdgeOfTypeMap.get(label);
		if ( states==null ) {
			states = new HashSet<NFAState>();
			typeToNFAStatesWithEdgeOfTypeMap.put(label, states);
		}
		states.add(stateWithLabeledEdge);
	}

	public Map<Label, Set<NFAState>> getTypeToNFAStatesWithEdgeOfTypeMap() {
		return typeToNFAStatesWithEdgeOfTypeMap;
	}

	public Set<NFAState> getStatesWithEdge(Label label) {
		return typeToNFAStatesWithEdgeOfTypeMap.get(label);
	}
*/
}
