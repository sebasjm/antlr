/*
 [The "BSD licence"]
 Copyright (c) 2004 Terence Parr
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
package org.antlr.runtime;

/** The root of the ANTLR exception hierarchy.
 *
 *  To avoid English-only error messages and to generally make things
 *  as flexible as possible, these exceptions are not created with strings,
 *  but rather the information necessary to generate an error.  Then
 *  the various reporting methods in Parser and Lexer can be overridden
 *  to generate a localized error message.  For example, MismatchedToken
 *  exceptions are built with the found token and expected token types.
 *  So, don't expect getMessage() to return anything.
 *
 *  Note that as of Java 1.4, you can access the stack trace, which means
 *  that you can compute the complete trace of rules from the start symbol.
 *  This gives you considerable context information with which to generate
 *  useful error messages.
 *
 *  ANTLR generates code that throws exceptions upon recognition error and
 *  also generates code to catch these exceptions in each rule.  If you
 *  want to quit upon first error, you can turn off the automatic error
 *  handling mechanism.  If you want an ANTLR-generated recognizer to bail
 *  out after another kind of error, then just throw an exception not
 *  under this hierarchy.
 *
 *  In general, the recognition exceptions track where in a grammar an
 *  problem occurred and/or what was the expected input.  The parser
 *  knows its state (such as current input symbol and line info) so
 *  this information is left out of the exception and left to the reporting
 *  method(s) to fill in.  You might want to have an error report an
 *  entire line of input not just a single token, for example.  Better to
 *  just say the recognizer had a problem and then let the parser report
 *  where the heck it was.
 */
public class RecognitionException extends Exception {
}
