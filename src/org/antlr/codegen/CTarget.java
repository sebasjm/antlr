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
package org.antlr.codegen;

import org.antlr.Tool;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.tool.Grammar;

import java.io.IOException;
import java.util.ArrayList;
        
public class CTarget extends Target {
    
        ArrayList strings = new ArrayList();
 
        protected void genRecognizerFile(Tool tool,
									CodeGenerator generator,
									Grammar grammar,
									StringTemplate outputFileST)
		throws IOException
	{
                // Before we write this, and cause it to generate its string,
                // we need to add all the string literals that we are going to match
                //
                outputFileST.setAttribute("literals", strings);
                //System.out.println(outputFileST.toStructureString());
		String fileName = generator.getRecognizerFileName(grammar.name, grammar.type);
		generator.write(outputFileST, fileName);
	}
                
	protected void genRecognizerHeaderFile(Tool tool,
										   CodeGenerator generator,
										   Grammar grammar,
										   StringTemplate headerFileST,
										   String extName)
		throws IOException
	{
            generator.write(headerFileST, grammar.name+ Grammar.grammarTypeToFileNameSuffix[grammar.type] +extName);
	}
        
        protected StringTemplate chooseWhereCyclicDFAsGo(Tool tool,
										   CodeGenerator generator,
										   Grammar grammar,
										   StringTemplate recognizerST,
										   StringTemplate cyclicDFAST)
	{
		return recognizerST;
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
                                if ( scope.equals("preincludes") ) {return true;}
                                if ( scope.equals("overrides") ) {return true;}
				break;
			case Grammar.PARSER :
				if ( scope.equals("parser") ) {return true;}
                                if ( scope.equals("header") ) {return true;}
                                if ( scope.equals("includes") ) {return true;}
                                if ( scope.equals("preincludes") ) {return true;}
                                if ( scope.equals("overrides") ) {return true;}
				break;
			case Grammar.COMBINED :
				if ( scope.equals("parser") ) {return true;}
				if ( scope.equals("lexer") ) {return true;}
                                if ( scope.equals("header") ) {return true;}
                                if ( scope.equals("includes") ) {return true;}
                                if ( scope.equals("preincludes") ) {return true;}
                                if ( scope.equals("overrides") ) {return true;}
				break;
			case Grammar.TREE_PARSER :
				if ( scope.equals("treeparser") ) {return true;}
                                if ( scope.equals("header") ) {return true;}
                                if ( scope.equals("includes") ) {return true;}
                                if ( scope.equals("preincludes") ) {return true;}
                                if ( scope.equals("overrides") ) {return true;}
				break;
		}
		return false;
	}
        
        public String getTargetCharLiteralFromANTLRCharLiteral(
		CodeGenerator generator,
		String literal)
	{
                
                
                
                
                if  (literal.startsWith("'\\u") )
                {
                    literal = "0x" +literal.substring(3, 7);
                }
                else
                {
					// hi, Jim.  I think:
					// int c = Character.valueOf(literal.charAt(1));
					// should be just:
					int c = literal.charAt(1); // TJP
                    if  (c < 32 || c > 127) {
                        literal  =  "0x" + Integer.toHexString(c);
                    }
                }
                
                return literal;
	}
        
	/** Convert from an ANTLR string literal found in a grammar file to
	 *  an equivalent string literal in the target language.  For Java, this
	 *  is the translation 'a\n"' -> "a\n\"".  Expect single quotes
	 *  around the incoming literal.  Just flip the quotes and replace
	 *  double quotes with \"
	 */
	public String getTargetStringLiteralFromANTLRStringLiteral(
		CodeGenerator generator,
		String literal)
	{
            int index;
	
            String bytes;
            
            StringBuffer buf = new StringBuffer();
            
            buf.append("{ ");
            
            for (int i = 1; i< literal.length()-1; i++)
            {
                buf.append("0x");
                buf.append(Integer.toHexString((int)literal.charAt(i)));
                buf.append(", ");
               
            }
            buf.append(" ANTLR3_STRING_TERMINATOR}");
            
            bytes   = buf.toString();            
            index   = strings.indexOf(bytes);
            
            if  (index == -1)
            {
                strings.add(bytes);
                index = strings.indexOf(bytes);
            }
             
            String strref = "lit_" + String.valueOf(index+1);

            return strref;
	}
       
}

