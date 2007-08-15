/*
 [The "BSD licence"]
 Copyright (c) 2005-2006 Terence Parr
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
package org.antlr.test;

import org.antlr.tool.ErrorManager;
import org.antlr.tool.GrammarSemanticsMessage;
import org.antlr.tool.Message;
import org.antlr.tool.Grammar;
import org.antlr.Tool;
import org.antlr.codegen.CodeGenerator;

/** Tree rewrites in tree parsers are basically identical to rewrites
 *  in a normal grammar except that the atomic element is a node not
 *  a Token.  Tests here ensure duplication of nodes occurs properly
 *  and basic functionality.
 */
public class TestTreeGrammarRewriteAST extends BaseTest {
	protected boolean debug = false;

	public void testFlatList() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ID INT -> INT ID\n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "abc 34");
		assertEquals("34 abc\n", found);
	}

	public void testSimpleTree() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> ^(ID INT);\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ^(ID INT) -> ^(INT ID)\n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "abc 34");
		assertEquals("(34 abc)\n", found);
	}

	public void testCombinedRewriteAndAuto() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> ^(ID INT) | INT ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ^(ID INT) -> ^(INT ID) | INT\n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "abc 34");
		assertEquals("(34 abc)\n", found);

		found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "34");
		assertEquals("34\n", found);
	}

	public void testAvoidDup() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ID -> ^(ID ID)\n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "abc");
		assertEquals("(abc abc)\n", found);
	}

	public void testLoop() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID+ INT+ -> (^(ID INT))+ ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : (^(ID INT))+ -> INT+ ID+\n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "a b c 3 4 5");
		assertEquals("3 4 5 a b c\n", found);
	}

	public void testAutoDup() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ID \n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "abc");
		assertEquals("abc\n", found);
	}

	public void testAutoDupRule() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : b c ;\n" +
			"b : ID ;\n" +
			"c : INT ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "a 1");
		assertEquals("a 1\n", found);
	}

	public void testAutoDupMultiple() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID ID INT;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ID ID INT\n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "a b 3");
		assertEquals("a b 3\n", found);
	}

	public void testAutoDupTree() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> ^(ID INT);\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ^(ID INT)\n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "a 3");
		assertEquals("(a 3)\n", found);
	}

	public void testAutoDupTreeWithLabels() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> ^(ID INT);\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ^(x=ID y=INT)\n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "a 3");
		assertEquals("(a 3)\n", found);
	}

	public void testAutoDupTreeWithListLabels() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> ^(ID INT);\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ^(x+=ID y+=INT)\n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "a 3");
		assertEquals("(a 3)\n", found);
	}

	public void testAutoDupTreeWithRuleRoot() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> ^(ID INT);\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ^(b INT) ;\n" +
			"b : ID ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "a 3");
		assertEquals("(a 3)\n", found);
	}

	public void testAutoDupTreeWithRuleRootAndLabels() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> ^(ID INT);\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ^(x=b INT) ;\n" +
			"b : ID ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "a 3");
		assertEquals("(a 3)\n", found);
	}

	public void testAutoDupTreeWithRuleRootAndListLabels() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> ^(ID INT);\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ^(x+=b y+=c) ;\n" +
			"b : ID ;\n" +
			"c : INT ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "a 3");
		assertEquals("(a 3)\n", found);
	}

	public void testAutoDupNestedTree() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : x=ID y=ID INT -> ^($x ^($y INT));\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ^(ID ^(ID INT))\n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "a b 3");
		assertEquals("(a (b 3))\n", found);
	}

	public void testDelete() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T;}\n" +
			"a : ID -> \n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "abc");
		assertEquals("", found);
	}

	// REWRITE MODE

	public void testRewriteModeCombinedRewriteAndAuto() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> ^(ID INT) | INT ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {$channel=HIDDEN;} ;\n";

		String treeGrammar =
			"tree grammar TP;\n"+
			"options {output=AST; ASTLabelType=CommonTree; tokenVocab=T; rewrite=true;}\n" +
			"a : ^(ID INT) -> ^(INT ID) | INT\n" +
			"  ;\n";

		String found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "abc 34");
		assertEquals("(34 abc)\n", found);

		found = execTreeParser("T.g", grammar, "TParser", "TP.g",
				    treeGrammar, "TP", "TLexer", "a", "a", "34");
		assertEquals("", found);
	}

}
