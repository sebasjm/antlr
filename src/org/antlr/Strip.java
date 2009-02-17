/*
 [The "BSD licence"]
 Copyright (c) 2007-2008 Leon Jen-Yuan Su
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
*/
package org.antlr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.runtime.debug.*;
import org.antlr.grammar.v3.ANTLRv3Parser;
import org.antlr.grammar.v3.ANTLRv3Lexer;

/** This is an individual tool of ANTLRMorph which can be used for stripping
 *  off grammar options, actions, scopes, and grammar rule arguments, return
 *  values, options, scopes, actions, and rewrites.
 */
public class Strip {
	
	// options
	protected List<String> grammarFileNames = new ArrayList<String>();
	protected boolean stripRewrites = false;
	protected boolean stripLabels = false;
	protected boolean keepGrammarOptions = false;
	protected boolean keepGrammarActions = false;
	protected boolean keepGlobalScope = false;
	protected boolean keepRuleArgs = false;
	protected boolean keepRuleReturnValues = false;
	protected boolean keepRuleThrows = false;
	protected boolean keepRuleOptions = false;
	protected boolean keepRuleScopes = false;
	protected boolean keepRuleActions = false;
	protected boolean keepRuleException = false;
	protected boolean keepAltActions = false;
	protected boolean keepAltRuleRefArgs = false;
	protected boolean keepAltTokenRefArgs = false;
	protected boolean keepLabels = false;
	
	public static void main(String args[]) throws Exception {
		Strip tester= new Strip(args);
		try {
			tester.process();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (RecognitionException e) {
			e.printStackTrace();
		}
    }
	
	public Strip(String[] args) {
		processArgs(args);
	}
	
	public void processArgs(String[] args) {
		if ( args==null || args.length==0 ) {
			help();
			return;
		}
		for (int i = 0; i < args.length; i++) {
			if ( args[i].equals("-strip-rewrite") ) {
				stripRewrites = true;
			}
			else if ( args[i].equals("-strip-label") ) {
				stripLabels = true;
			}
			else if ( args[i].equals("-grammar-option") ) {
				keepGrammarOptions = true;
			}
			else if ( args[i].equals("-grammar-action") ) {
				keepGrammarActions = true;
			}
			else if ( args[i].equals("-globalscope") ) {
				keepGlobalScope = true;
			}
			else if ( args[i].equals("-rule-argument") ) {
				keepRuleArgs = true;
			}
			else if ( args[i].equals("-rule-returnvalue") ) {
				keepRuleReturnValues = true;
			}
			else if ( args[i].equals("-rule-throws") ) {
				keepRuleThrows = true;
			}
			else if ( args[i].equals("-rule-option") ) {
				keepRuleOptions = true;
			}
			else if ( args[i].equals("-rule-scope") ) {
				keepRuleScopes = true;
			}
			else if ( args[i].equals("-rule-action") ) {
				keepRuleActions = true;
			}
			else if ( args[i].equals("-rule-exception") ) {
				keepRuleException = true;
			}
			else if ( args[i].equals("-alt-action") ) {
				keepAltActions = true;
			}
			else if ( args[i].equals("-alt-ruleargument") ) {
				keepAltRuleRefArgs = true;
			}
			else if ( args[i].equals("-alt-tokenargument") ) {
				keepAltTokenRefArgs = true;
			}
			else if ( args[i].equals("-label") ) {
				keepLabels = true;
			}
			else {
				if ( args[i].charAt(0) != '-' ) {
					// Must be the grammar file
					grammarFileNames.add(args[i]);
				}
			}
		}
	}
	
	public void process() throws IOException, RecognitionException {
		if ( grammarFileNames.size()==0 ) {
			System.err.println("you need to provide a grammar file.");
			System.exit(1);
		}
		
		for (String grammarFileName: grammarFileNames) {
			CharStream input = new ANTLRFileStream(grammarFileName);
			// BUILD AST + PARSE TREES
			ANTLRv3Lexer lex = new ANTLRv3Lexer(input);
	        CommonTokenStream tokens = new CommonTokenStream(lex);
	        ParseTreeBuilder builder = new ParseTreeBuilder(grammarFileName);
	        // TODO: Make a generic grammar parser for this class, no need of MorphEngine
	        ANTLRv3Parser g = new ANTLRv3Parser(tokens, builder);
	        g.grammarDef();
	        ParseTree pt = builder.getTree();
	        
	        // walk tree: grammarDef
	        walkPT(pt.getChild(0));
	        //System.out.println("parse tree: "+pt.toStringTree());
	        System.out.println(pt.toInputString());
		}
	}
	
	public void walkPT(Tree pt) {
		int childCount = pt.getChildCount();
		int index = 0;
		//Note: tokensSpec is keeping
		while ( index<childCount ) {
			Tree child = pt.getChild(index);
			// handle grammar option
			if ( child.getText().equals("optionsSpec") &&!keepGrammarOptions ) {
				pt.deleteChild(index);
				childCount--;
			}
			// handle grammar actions including header, members
			else if ( child.getText().equals("action") &&!keepGrammarActions ) {
				pt.deleteChild(index);
				childCount--;
			}
			// handle grammar global scope 
			else if ( child.getText().equals("attrScope") &&!keepGlobalScope ) {
				pt.deleteChild(index);
				childCount--;
			}
			// handle rule and rule alternative actions
			else if ( child.getText().equals("rule") ) {
				/*
				// skip lexical rules
				if ( child.getChild(0)!=null && child.getChild(0).getChild(0)!=null ) {
					if ( Character.isUpperCase(child.getChild(0).getChild(0).getText().charAt(0))) {
						index++;
						continue;
					}
				}
				// 'fragment' keyword must be used with lexical rule, so skip too
				else if ( child.getChild(0)!=null && child.getChild(0).getText().equals("fragment") ) {
					index++;
					continue;
				}
				*/
				// start processing rule elements
				int ruleChildCount = child.getChildCount();
				int ruleChildIndex = 0;
				while ( ruleChildIndex<ruleChildCount ) {
					Tree ruleComponent = child.getChild(ruleChildIndex);
					// handle rule arguments, return values
					if ( ruleComponent.getText().equals("id") ) {
						// move to next rule component
						ruleComponent = child.getChild(++ruleChildIndex);
						if (!ruleComponent.getText().equals(":") && !ruleComponent.getText().equals("!") && 
							!ruleComponent.getText().equals("throwsSpec") && !ruleComponent.getText().equals("optionsSpec") && 
							!ruleComponent.getText().equals("ruleScopeSpec") && !ruleComponent.getText().equals("ruleAction") ) {
							if ( !ruleComponent.getText().equals("returns") ) {	// must be arguments
								if ( !keepRuleArgs ) {
									child.deleteChild(ruleChildIndex);
									ruleChildCount--;
									ruleComponent = child.getChild(ruleChildIndex);	// move to next rule component
								}
								else ruleComponent = child.getChild(++ruleChildIndex);	// move to next rule component
							}
							if ( ruleComponent.getText().equals("returns") && !keepRuleReturnValues ) {
								child.deleteChild(ruleChildIndex);
								child.deleteChild(ruleChildIndex);
								ruleChildCount-=2;
								ruleComponent = child.getChild(ruleChildIndex);	// move to next rule component
							}
		
						}
					}
					// remove the rule AST constructor '!' if -strip-rewrite
					if ( ruleComponent.getText().equals("!") && stripRewrites ) {
						child.deleteChild(ruleChildIndex);
						ruleChildCount--;
					}
					// handle rule throws specification
					else if ( ruleComponent.getText().equals("throwsSpec") &&!keepRuleThrows ) {
						child.deleteChild(ruleChildIndex);
						ruleChildCount--;
					}
					// handle rule options
					else if ( ruleComponent.getText().equals("optionsSpec") &&!keepRuleOptions ) {
						child.deleteChild(ruleChildIndex);
						ruleChildCount--;
					}
					// handle rule scope
					else if ( ruleComponent.getText().equals("ruleScopeSpec") &&!keepRuleScopes ) {
						child.deleteChild(ruleChildIndex);
						ruleChildCount--;
					}
					// handle rule actions: @init/@after
					else if ( ruleComponent.getText().equals("ruleAction") &&!keepRuleActions ) {
						child.deleteChild(ruleChildIndex);
						ruleChildCount--;
					}
					// handle alt actions and alt rewrites
					else if ( ruleComponent.getText().equals("altList") ) {
						// Note: altList may contain a nested structure, so we need a different walk
						walkTree(ruleComponent);
						ruleChildIndex++;
					}
					else if ( ruleComponent.getText().equals("exceptionGroup") &&!keepRuleException ) {
						child.deleteChild(ruleChildIndex);
						ruleChildCount--;
					}
					else ruleChildIndex++;
				}
				index++;	// move to next rule
			}
			else index++;
		}
    }
	
	// depth first walk the whole input tree, this is only used for walking the altList sub-tree
	private void walkTree(Tree t) {
		int childCount = t.getChildCount();
		int index = 0;
		while ( index<childCount ) {
			Tree child = t.getChild(index);
			if ( child.getChildCount()>0 ) walkTree(child);
			/** match and remove the specific node after walking the child nodes */
			// remove rewrites if -striprewrite
			if ( child.getText().equals("rewrite") && (t.getText().equals("altList") || t.getText().equals("block")) && stripRewrites ) {
				t.deleteChild(index);
				childCount--;
			}
			//TODO: removing labels has the same issue, may need to check hiddentokens in ParseTree.java
			// replace actions in alternative with a space, which prevents 2 elements connect to each other...
			else if ( child.getText().equals("altAction") &&!keepAltActions ) {
				CommonToken token = new CommonToken(99, " ");
				ParseTree pt = new ParseTree(token);
				t.replaceChildren(index, index, pt);
				index++;
				//t.deleteChild(index);
				//childCount--;
			}
			// examine and strip off labels if -strip-rewrite or -strip-label and not -label
			else if ( child.getText().equals("elementNoOptionSpec") ) {
				if ( child.getChild(0)!=null && child.getChild(0).getText().equals("id") && child.getChild(1)!=null && 
					(child.getChild(1).getText().equals("=") || child.getChild(1).getText().equals("+=")) && 
					(stripRewrites || stripLabels) && !keepLabels ) {
					//child.deleteChild(0);	// remove id (label)
					//child.deleteChild(0);	// remove '=' or '+='
					CommonToken token = new CommonToken(99, " ");
					ParseTree pt = new ParseTree(token);
					child.replaceChildren(0, 1, pt); // replace id (label) and '='/'+=' with a white space
				}
				index++;
			}
			// remove alt arguments: RULE_REF ARG_ACTION? and AST constructor '^'|'!'
			else if ( child.getText().equals("atom") ) {
				if ( child.getChild(0)!=null && !child.getChild(0).getText().equals("range") && 
					!child.getChild(0).getText().equals("terminal") &&
					!child.getChild(0).getText().equals("notSet") ) {
					// strip off rule ref argument if necessary
					if ( child.getChild(1)!=null && !child.getChild(1).getText().equals("^") && 
						!child.getChild(1).getText().equals("!") && !keepAltRuleRefArgs ) {
						child.deleteChild(1);
					}
				}
				// examine the last child, if it's an AST constructor and -strip-rewrite, strip if off
				int n = child.getChildCount();
				if ( n>1 && (child.getChild(n-1).getText().equals("^") || 
					 child.getChild(n-1).getText().equals("!")) && stripRewrites ) {
					 child.deleteChild(n-1);
				}
				// handle terminal respectively
				else if ( child.getChild(0)!=null && child.getChild(0).getText().equals("terminal") ) {
					if ( stripRewrites ) {	// examine the last child, and remove tree construction operator if -strip-rewrite
						int m = child.getChild(0).getChildCount();
						if ( m>1 && (child.getChild(0).getChild(m-1).getText().equals("^") || 
							 child.getChild(0).getChild(m-1).getText().equals("!")) ) {
							child.getChild(0).deleteChild(m-1);
						}
					}
					if ( !keepAltTokenRefArgs ) {	// examine the 2nd child, and remove token argument if necessary
						int m = child.getChild(0).getChildCount();
						if ( m>1 && !child.getChild(0).getChild(1).getText().equals("^") && 
							 !child.getChild(0).getChild(1).getText().equals("!") ) {
							child.getChild(0).deleteChild(1);
						}
					}
				}
				index++;
			}
			// remove token argument of rewrites alts if not -strip-rewrite and not -alt-tokenargument
			else if ( child.getText().equals("rewrite_tree_atom") && !stripRewrites && !keepAltTokenRefArgs ) {
				int n = child.getChildCount();
				if ( n==2 && !child.getChild(0).getText().equals("$") ) {
					child.deleteChild(1);
				}
				index++;
			}
			/***/
			else index++;
		}
	}
	
	private static void help() {
		System.err.println("usage: java org.antlr.morph.Strip [args] file.g [file2.g [file3.g ...]]");
		System.err.println("  -strip-rewrite        strip off rewrites and rule/token reference labels");
		System.err.println("  -strip-label          strip off rule/token reference labels");
		System.err.println("  -grammar-option       keep grammar options");
		System.err.println("  -grammar-action       keep grammar actions, e.g. header, members.");
		System.err.println("  -globalscope          keep global scopes");
		System.err.println("  -rule-argument        keep rule arguments");
		System.err.println("  -rule-returnvalue     keep rule return values");
		System.err.println("  -rule-throws          keep rule throws specification");
		System.err.println("  -rule-option          keep rule options");
		System.err.println("  -rule-scope           keep rule scope");
		System.err.println("  -rule-action          keep rule actions, e.g. @init, @after.");
		System.err.println("  -rule-exception       keep rule exception group");
		System.err.println("  -alt-action           keep alternative actions");
		System.err.println("  -alt-ruleargument     keep alternative rule reference arguments");
		System.err.println("  -alt-tokenargument    keep alternative token reference arguments");
		System.err.println("  -label                keep rule/token reference labels");
	}

}
