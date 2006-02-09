/*
 [The "BSD licence"]
 Copyright (c) 2005 Terence Parr
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
package org.antlr.codegen;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.tool.Grammar;
import org.antlr.Tool;

import java.io.IOException;

public class CTarget extends Target {
	protected void genRecognizerHeaderFile(Tool tool,
										   CodeGenerator generator,
										   Grammar grammar,
			 							   StringTemplate headerFileST)
		throws IOException
	{
		generator.write(headerFileST, grammar.name+".h");
	}
        protected StringTemplate chooseWhereCyclicDFAsGo(Tool tool,
										   CodeGenerator generator,
										   Grammar grammar,
										   StringTemplate recognizerST,
										   StringTemplate cyclicDFAST)
	{
		return cyclicDFAST;
	}
 	/** Is scope in @scope::name {action} valid for this kind of grammar?
	 *  Targets like C++ may want to allow new scopes like headerfile or
	 *  some such.  The action names themselves are not policed at the
	 *  moment so targets can add template actions w/o having to recompile
	 *  ANTLR.
	 */
	public boolean isValidActionScope(int grammarType, String scope) {
		switch (grammarType) {
			case Grammar.LEXER :
				if ( scope.equals("lexer") ) {return true;}
                                if ( scope.equals("header") ) {return true;}
                                if ( scope.equals("includes") ) {return true;}
				break;
			case Grammar.PARSER :
				if ( scope.equals("parser") ) {return true;}
                                if ( scope.equals("header") ) {return true;}
                                if ( scope.equals("includes") ) {return true;}
				break;
			case Grammar.COMBINED :
				if ( scope.equals("parser") ) {return true;}
				if ( scope.equals("lexer") ) {return true;}
                                if ( scope.equals("header") ) {return true;}
                                if ( scope.equals("includes") ) {return true;}
				break;
			case Grammar.TREE_PARSER :
				if ( scope.equals("treeparser") ) {return true;}
                                if ( scope.equals("header") ) {return true;}
                                if ( scope.equals("includes") ) {return true;}
				break;
		}
		return false;
	}               
}

