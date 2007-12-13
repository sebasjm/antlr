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
package org.antlr.runtime {
	
	/** A lexer is recognizer that draws input symbols from a character stream.
	 *  lexer grammars result in a subclass of this object. A Lexer object
	 *  uses simplified match() and error recovery mechanisms in the interest
	 *  of speed.
	 */
	public class Lexer extends BaseRecognizer implements TokenSource {
		/** Where is the lexer drawing characters from? */
	    protected var input:CharStream;
	
		/** The goal of all lexer rules/methods is to create a token object.
		 *  This is an instance variable as multiple rules may collaborate to
		 *  create a single token.  nextToken will return this object after
		 *  matching lexer rule(s).  If you subclass to allow multiple token
		 *  emissions, then set this to the last token to be matched or
		 *  something nonnull so that the auto token emit mechanism will not
		 *  emit another token.
		 */
	    protected var token:Token;
	
		/** What character index in the stream did the current token start at?
		 *  Needed, for example, to get the text for current token.  Set at
		 *  the start of nextToken.
	 	 */
		protected var tokenStartCharIndex:int = -1;
	
		/** The line on which the first character of the token resides */
		protected var tokenStartLine:int;
	
		/** The character position of first character within the line */
		protected var tokenStartCharPositionInLine:int;
	
		/** The channel number for the current token */
		protected var channel:int;
	
		/** The token type for the current token */
		protected var type:int;
	
		/** You can set the text for the current token to override what is in
		 *  the input char buffer.  Use setText() or can set this instance var.
	 	 */
		protected var _text:String;
	
		// GMS: merged constructors and use default
		public function Lexer(input:CharStream = null) {
			this.input = input;
		}
		
		public override function reset():void {
			super.reset(); // reset all recognizer state variables
			// wack Lexer state variables
			token = null;
			type = TokenConstants.INVALID_TOKEN_TYPE;
			channel = TokenConstants.DEFAULT_CHANNEL;
			tokenStartCharIndex = -1;
			tokenStartCharPositionInLine = -1;
			tokenStartLine = -1;
			_text = null;
			if ( input!=null ) {
				input.seek(0); // rewind the input
			}
		}
	
		/** Return a token from this source; i.e., match a token on the char
		 *  stream.
		 */
	    public function nextToken():Token {
			while (true) {
				token = null;
				channel = TokenConstants.DEFAULT_CHANNEL;
				tokenStartCharIndex = input.index;
				tokenStartCharPositionInLine = input.charPositionInLine;
				tokenStartLine = input.line;
				_text = null;
				if ( input.LA(1)==CharStreamConstants.EOF ) {
	                return TokenConstants.EOF_TOKEN;
	            }
	            try {
	                mTokens();
					if ( token==null ) {
						emit();
					}
					else if ( token==TokenConstants.SKIP_TOKEN ) {
						continue;
					}
					return token;
				}
	            catch (re:RecognitionException) {
	                reportError(re);
	                recover(re);
	                continue;
	            }
	        }
	        // Can't happen, but will quiet complier error
	        return null;
	    }
	
		/** Instruct the lexer to skip creating a token for current lexer rule
		 *  and look for another token.  nextToken() knows to keep looking when
		 *  a lexer rule finishes with token set to SKIP_TOKEN.  Recall that
		 *  if token==null at end of any token rule, it creates one for you
		 *  and emits it.
		 */
		public function skip():void {
			token = TokenConstants.SKIP_TOKEN;
		}
	
		/** This is the lexer entry point that sets instance var 'token' */
		// GMS: made non-abstract
		public function mTokens():void {}
	
		/** Set the char stream and reset the lexer */
		public function setCharStream(input:CharStream):void {
			this.input = null;
			reset();
			this.input = input;
		}
	
		/** Currently does not support multiple emits per nextToken invocation
		 *  for efficiency reasons.  Subclass and override this method and
		 *  nextToken (to push tokens into a list and pull from that list rather
		 *  than a single variable as this implementation does).
		 * GMS: renamed from emit()
		 */
		public function emitToken(token:Token):void {
			this.token = token;
		}
	
		/** The standard method called to automatically emit a token at the
		 *  outermost lexical rule.  The token object should point into the
		 *  char buffer start..stop.  If there is a text override in 'text',
		 *  use that to set the token's text.  Override this method to emit
		 *  custom Token objects.
		 */
		public function emit():Token {
			// GMS changed to remove charIndex subtraction
			var t:Token = CommonToken.createFromStream(input, type, channel, tokenStartCharIndex, charIndex - 1);
			t.line = tokenStartLine;
			t.text = text;
			t.charPositionInLine = tokenStartCharPositionInLine;
			emitToken(t);
			return t;
		}
	
		// GMS: renamed from match()
		public function matchString(s:String):void {
	        var i:int = 0;
	        while ( i<s.length ) {
	        	// GMS: Changed charAt to charCodeAt()
	            if ( input.LA(1) != s.charCodeAt(i) ) {
					if ( backtracking>0 ) {
						failed = true;
						return;
					}
					// GMS: Changed charAt to charCodeAt()
					var mte:MismatchedTokenException =
						new MismatchedTokenException(s.charCodeAt(i), input);
					recover(mte);
					throw mte;
	            }
	            i++;
	            input.consume();
				failed = false;
	        }
	    }
	
	    public function matchAny():void {
	        input.consume();
	    }
	
	    public function match(c:int):void {
	        if ( input.LA(1)!=c ) {
				if ( backtracking>0 ) {
					failed = true;
					return;
				}
				var mte:MismatchedTokenException =
					new MismatchedTokenException(c, input);
				recover(mte);
				throw mte;
	        }
	        input.consume();
			failed = false;
	    }
	
	    public function matchRange(a:int, b:int):void
		{
	        if ( input.LA(1)<a || input.LA(1)>b ) {
				if ( backtracking>0 ) {
					failed = true;
					return;
				}
	            var mre:MismatchedRangeException =
					new MismatchedRangeException(a,b,input);
				recover(mre);
				throw mre;
	        }
	        input.consume();
			failed = false;
	    }
	
	    public function get line():int {
	        return input.line;
	    }
	
	    public function get charPositionInLine():int {
	        return input.charPositionInLine;
	    }
	
		/** What is the index of the current character of lookahead? */
		public function get charIndex():int {
			return input.index;
		}
	
		/** Return the text matched so far for the current token or any
		 *  text override.
		 */
		public function get text():String {
			if ( _text!=null ) {
				return _text;
			}
			return input.substring(tokenStartCharIndex, charIndex-1);
		}
	
		/** Set the complete text of this token; it wipes any previous
		 *  changes to the text.
		 */
		public function set text(text:String):void {
			_text = text;
		}
	
		public override function reportError(e:RecognitionException):void {
			/** TODO: not thought about recovery in lexer yet.
			 *
			// if we've already reported an error and have not matched a token
			// yet successfully, don't report any errors.
			if ( errorRecovery ) {
				//System.err.print("[SPURIOUS] ");
				return;
			}
			errorRecovery = true;
			 */
	
			displayRecognitionError(this.getTokenNames(), e);
		}
	
		public override function getErrorMessage(e:RecognitionException, tokenNames:Array):String {
			var msg:String = null;
			if ( e is MismatchedTokenException ) {
				var mte:MismatchedTokenException = MismatchedTokenException(e);
				msg = "mismatched character "+getCharErrorDisplay(e.c)+" expecting "+getCharErrorDisplay(mte.expecting);
			}
			else if ( e is NoViableAltException ) {
				var nvae:NoViableAltException = NoViableAltException(e);
				// for development, can add "decision=<<"+nvae.grammarDecisionDescription+">>"
				// and "(decision="+nvae.decisionNumber+") and
				// "state "+nvae.stateNumber
				msg = "no viable alternative at character "+getCharErrorDisplay(e.c);
			}
			else if ( e is EarlyExitException ) {
				var eee:EarlyExitException = EarlyExitException(e);
				// for development, can add "(decision="+eee.decisionNumber+")"
				msg = "required (...)+ loop did not match anything at character "+getCharErrorDisplay(e.c);
			}
			else if ( e is MismatchedNotSetException ) {
				var mnse:MismatchedNotSetException = MismatchedNotSetException(e);
				msg = "mismatched character "+getCharErrorDisplay(e.c)+" expecting set "+mnse.expecting;
			}
			else if ( e is MismatchedSetException ) {
				var mse:MismatchedSetException = MismatchedSetException(e);
				msg = "mismatched character "+getCharErrorDisplay(e.c)+" expecting set "+mse.expecting;
			}
			else if ( e is MismatchedRangeException ) {
				var mre:MismatchedRangeException = MismatchedRangeException(e);
				msg = "mismatched character "+getCharErrorDisplay(e.c)+" expecting set "+
					getCharErrorDisplay(mre.a)+".."+getCharErrorDisplay(mre.b);
			}
			else {
				msg = super.getErrorMessage(e, tokenNames);
			}
			return msg;
		}
	
		public function getCharErrorDisplay(c:int):String {
			// GMS: Changed from valueOf to fromCharCode
			var s:String = String.fromCharCode(c);
			switch ( c ) {
				case TokenConstants.EOF :
					s = "<EOF>";
					break;
				case '\n' :
					s = "\\n";
					break;
				case '\t' :
					s = "\\t";
					break;
				case '\r' :
					s = "\\r";
					break;
			}
			return "'"+s+"'";
		}
	
		/** Lexers can normally match any char in it's vocabulary after matching
		 *  a token, so do the easy thing and just kill a character and hope
		 *  it all works out.  You can instead use the rule invocation stack
		 *  to do sophisticated error recovery if you are in a fragment rule.
		 */
		public function recover(re:RecognitionException):void {
			//System.out.println("consuming char "+(char)input.LA(1)+" during recovery");
			//re.printStackTrace();
			input.consume();
		}
	
		public function traceIn(ruleName:String, ruleIndex:int):void {
			// GMS: changed from cast of input.LT(1) to fromCharCode
			var inputSymbol:String = String.fromCharCode(input.LT(1))+" line="+ line +":"+ charPositionInLine;
			super.traceInSymbol(ruleName, ruleIndex, inputSymbol);
		}
	
		public function traceOut(ruleName:String, ruleIndex:int):void {
			// GMS: changed from cast of input.LT(1) to fromCharCode
			var inputSymbol:String = String.fromCharCode(input.LT(1))+" line="+ line +":"+ charPositionInLine;
			super.traceOutSymbol(ruleName, ruleIndex, inputSymbol);
		}
	}
}