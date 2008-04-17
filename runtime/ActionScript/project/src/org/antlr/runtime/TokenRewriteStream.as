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
	
	/** Useful for dumping out the input stream after doing some
	 *  augmentation or other manipulations.
	 *
	 *  You can insert stuff, replace, and delete chunks.  Note that the
	 *  operations are done lazily--only if you convert the buffer to a
	 *  String.  This is very efficient because you are not moving data around
	 *  all the time.  As the buffer of tokens is converted to strings, the
	 *  toString() method(s) check to see if there is an operation at the
	 *  current index.  If so, the operation is done and then normal String
	 *  rendering continues on the buffer.  This is like having multiple Turing
	 *  machine instruction streams (programs) operating on a single input tape. :)
	 *
	 *  Since the operations are done lazily at toString-time, operations do not
	 *  screw up the token index values.  That is, an insert operation at token
	 *  index i does not change the index values for tokens i+1..n-1.
	 *
	 *  Because operations never actually alter the buffer, you may always get
	 *  the original token stream back without undoing anything.  Since
	 *  the instructions are queued up, you can easily simulate transactions and
	 *  roll back any changes if there is an error just by removing instructions.
	 *  For example,
	 *
	 *   var input:CharStream = new ANTLRFileStream("input");
	 *   var lex:TLexer = new TLexer(input);
	 *   var tokens:TokenRewriteStream = new TokenRewriteStream(lex);
	 *   var parser:T = new T(tokens);
	 *   parser.startRule();
	 *
	 * 	 Then in the rules, you can execute
	 *      var t:Token t, u:Token;
	 *      ...
	 *      input.insertAfter(t, "text to put after t");}
	 * 		input.insertAfter(u, "text after u");}
	 * 		trace(tokens.toString());
	 *
	 *  Actually, you have to cast the 'input' to a TokenRewriteStream. :(
	 *
	 *  You can also have multiple "instruction streams" and get multiple
	 *  rewrites from a single pass over the input.  Just name the instruction
	 *  streams and use that name again when printing the buffer.  This could be
	 *  useful for generating a C file and also its header file--all from the
	 *  same buffer:
	 *
	 *      tokens.insertAfter("pass1", t, "text to put after t");}
	 * 		tokens.insertAfter("pass2", u, "text after u");}
	 * 		trace(tokens.toString("pass1"));
	 * 		trace(tokens.toString("pass2"));
	 *
	 *  If you don't use named rewrite streams, a "default" stream is used as
	 *  the first example shows.
	 */
	public class TokenRewriteStream extends CommonTokenStream {
		public static const DEFAULT_PROGRAM_NAME:String = "default";
		public static const MIN_TOKEN_INDEX:int = 0;
	
		/** You may have multiple, named streams of rewrite operations.
		 *  I'm calling these things "programs."
		 *  Maps String (name) -> rewrite (List)
		 */
		protected var programs:Object = new Object();
	
		/** Map String (program name) -> Integer index */
		protected var lastRewriteTokenIndexes:Object = new Object();
	
		public function TokenRewriteStream(tokenSource:TokenSource = null, channel:int = TokenConstants.DEFAULT_CHANNEL) {
			super(tokenSource, channel);
			programs[DEFAULT_PROGRAM_NAME] = new Array();
		}
	
	    /** Rollback the instruction stream for a program so that
		 *  the indicated instruction (via instructionIndex) is no
		 *  longer in the stream.  UNTESTED!
		 */
		public function rollback(instructionIndex:int, programName:String = DEFAULT_PROGRAM_NAME):void {
			var isn:Array = programs[programName] as Array;
			if ( isn != null ) {
				programs[programName] = isn.slice(MIN_TOKEN_INDEX,instructionIndex);
			}
		}
	
		/** Reset the program so that no instructions exist */
		public function deleteProgram(programName:String = DEFAULT_PROGRAM_NAME):void {
			rollback(MIN_TOKEN_INDEX, programName);
		}
	
		/** Add an instruction to the rewrite instruction list ordered by
		 *  the instruction number (use a binary search for efficiency).
		 *  The list is ordered so that toString() can be done efficiently.
		 *
		 *  When there are multiple instructions at the same index, the instructions
		 *  must be ordered to ensure proper behavior.  For example, a delete at
		 *  index i must kill any replace operation at i.  Insert-before operations
		 *  must come before any replace / delete instructions.  If there are
		 *  multiple insert instructions for a single index, they are done in
		 *  reverse insertion order so that "insert foo" then "insert bar" yields
		 *  "foobar" in front rather than "barfoo".  This is convenient because
		 *  I can insert new InsertOp instructions at the index returned by
		 *  the binary search.  A ReplaceOp kills any previous replace op.  Since
		 *  delete is the same as replace with null text, i can check for
		 *  ReplaceOp and cover DeleteOp at same time. :)
		 */
		protected function addToSortedRewriteList(op:RewriteOperation, programName:String = DEFAULT_PROGRAM_NAME):void {
			var rewrites:Array = getProgram(programName);
			
			// Note : Modified from Java because of lack of binary search in Flex framework, quick implementation for now.
			
			// empty list or op greater than end of list, add to back.
			if (rewrites.length == 0 || RewriteOperation(rewrites[rewrites.length - 1]).index < op.index) {
				rewrites.push(op);
			}
			else if (op.index < RewriteOperation(rewrites[0]).index) {			
				// if we are at beginning of list
				rewrites.unshift(op);
			}
			else {
				// in the middle of the list, find starting point			
				for (var pos:int = 0; pos < rewrites.length; pos++) {
					if (op.index <= RewriteOperation(rewrites[pos]).index) {
						break;
					}
				}
				
				if ( op is ReplaceOp ) {
					var replaced:Boolean = false;
					var i:int;
					// look for an existing replace
					for (i=pos; i<rewrites.length; i++) {
						var prevOp:RewriteOperation = RewriteOperation(rewrites[pos]);
						if ( prevOp.index!=op.index ) {
							break;
						}
						if ( prevOp is ReplaceOp ) {
							rewrites[pos] = op; // replace old with new
							replaced=true;
							break;
						}
						// keep going; must be an insert
					}
					if ( !replaced ) {
						// add replace op to the end of all the inserts
						rewrites.splice(i, 0, op);
					}
				}
				else {
					// inserts are added in front of existing inserts
					rewrites.splice(pos, 0, op);
				}
			}
		}
	
		public function insertAfterToken(t:Token, text:Object, programName:String = DEFAULT_PROGRAM_NAME):void {
			insertAfter(t.tokenIndex, text, programName);
		}
	
		public function insertAfter(index:int, text:Object, programName:String = DEFAULT_PROGRAM_NAME):void {
			// to insert after, just insert before next index (even if past end)
			insertBefore(index+1, text, programName);
		}
	
		public function insertBeforeToken(t:Token, text:Object, programName:String = DEFAULT_PROGRAM_NAME):void {
			insertBefore(t.tokenIndex, text, programName);
		}
	
		public function insertBefore(index:int, text:Object, programName:String = DEFAULT_PROGRAM_NAME):void {
			addToSortedRewriteList(new InsertBeforeOp(index, text), programName);
		}
			
		public function replace(index:int, text:Object, programName:String = DEFAULT_PROGRAM_NAME):void {
			replaceRange(index, index, text, programName);
		}
	
		public function replaceRange(fromIndex:int, toIndex:int, text:Object, programName:String = DEFAULT_PROGRAM_NAME):void {
			if ( fromIndex > toIndex || fromIndex<0 || toIndex<0 ) {
				return;
			}
			addToSortedRewriteList(new ReplaceOp(fromIndex, toIndex, text), programName);
		}
	
		public function replaceToken(indexT:Token, text:Object, programName:String = DEFAULT_PROGRAM_NAME):void {
			replaceTokenRange(indexT, indexT, text, programName);
		}
	
		public function replaceTokenRange(fromToken:Token, toToken:Token, text:Object, programName:String = DEFAULT_PROGRAM_NAME):void {
			replaceRange(fromToken.tokenIndex, toToken.tokenIndex, text, programName);
		}
	
		public function remove(index:int, programName:String = DEFAULT_PROGRAM_NAME):void {
			removeRange(index, index, programName);
		}
	
		public function removeRange(fromIndex:int, toIndex:int, programName:String = DEFAULT_PROGRAM_NAME):void {
			replaceRange(fromIndex, toIndex, null, programName);
		}
	
		public function removeToken(token:Token, programName:String = DEFAULT_PROGRAM_NAME):void {
			removeTokenRange(token, token, programName);
		}
	
		public function removeTokenRange(fromToken:Token, toToken:Token, programName:String = DEFAULT_PROGRAM_NAME):void {
			replaceTokenRange(fromToken, toToken, null, programName);
		}
	
		public function getLastRewriteTokenIndex(programName:String = DEFAULT_PROGRAM_NAME):int {
			var i:* = lastRewriteTokenIndexes[programName];
			if ( i == undefined ) {
				return -1;
			}
			return i as int;
		}
	
		protected function setLastRewriteTokenIndex(programName:String, i:int):void {
			lastRewriteTokenIndexes[programName] = i;
		}
	
		protected function getProgram(name:String):Array {
			var isn:Array = programs[name] as Array;
			if ( isn==null ) {
				isn = initializeProgram(name);
			}
			return isn;
		}
	
		private function initializeProgram(name:String):Array {
			var isn:Array = new Array();
			programs[name] =  isn;
			return isn;
		}
	
		public function toOriginalString():String {
			return toOriginalStringWithRange(MIN_TOKEN_INDEX, size-1);
		}
	
		public function toOriginalStringWithRange(start:int, end:int):String {
			var buf:String = new String();
			for (var i:int=start; i>=MIN_TOKEN_INDEX && i<=end && i<tokens.size; i++) {
				buf += getToken(i).text;
			}
			return buf.toString();
		}
	
		public override function toString():String {
			return toStringWithRange(MIN_TOKEN_INDEX, size-1, DEFAULT_PROGRAM_NAME);
		}
	
		public function toStringWithRange(start:int, end:int, programName:String = DEFAULT_PROGRAM_NAME):String {
			var rewrites:Array = programs[programName] as Array;
			if ( rewrites==null || rewrites.length==0 ) {
				return toOriginalStringWithRange(start,end); // no instructions to execute
			}
			var state:RewriteState = new RewriteState();
			
			state.index = start;
			
			/// Index of first rewrite we have not done
			var rewriteOpIndex:int = 0;
	
			while ( state.index>=MIN_TOKEN_INDEX &&
					state.index<=end &&
					state.index<tokens.length )
			{
				//System.out.println("state.index="+state.index);
				// execute instructions associated with this token index
				if ( rewriteOpIndex<rewrites.length ) {
					var op:RewriteOperation = RewriteOperation(rewrites[rewriteOpIndex]);
	
					// skip all ops at lower index
					while ( op.index<state.index && rewriteOpIndex<rewrites.length ) {
						rewriteOpIndex++;
						if ( rewriteOpIndex<rewrites.length ) {
							op = RewriteOperation(rewrites[rewriteOpIndex]);
						}
					}
	
					// while we have ops for this token index, exec them
					while ( state.index==op.index && rewriteOpIndex<rewrites.length ) {
						//System.out.println("execute "+op+" at instruction "+rewriteOpIndex);
						op.execute(state);
						//System.out.println("after execute state.index = "+state.index);
						rewriteOpIndex++;
						if ( rewriteOpIndex<rewrites.length ) {
							op = RewriteOperation(rewrites[rewriteOpIndex]);
						}
					}
				}
				// dump the token at this index
				if ( state.index<=end ) {
					state.buf += getToken(state.index).text;
					state.index++;
				}
			}
			// now see if there are operations (append) beyond last token index
			for (var opi:int=rewriteOpIndex; opi<rewrites.length; opi++) {
				op = RewriteOperation(rewrites[opi]);
				if ( op.index>=size ) {
					op.execute(state); // must be insertions if after last token
				}
				//System.out.println("execute "+op+" at "+opi);
				//op.execute(buf); // must be insertions if after last token
			}
	
			return state.buf;
		}
	
		public function toDebugString():String {
			return toDebugStringWithRange(MIN_TOKEN_INDEX, size-1);
		}
	
		public function toDebugStringWithRange(start:int, end:int):String {
			var buf:String = new String();
			for (var i:int=start; i>=MIN_TOKEN_INDEX && i<=end && i<tokens.length; i++) {
				buf += getToken(i);
			}
			return buf;
		}
		

	}
}
	import flash.utils.getQualifiedClassName;
	

// Define the rewrite operation hierarchy

class RewriteState {
	public var buf:String = new String();
	public var index:int;
}

class RewriteOperation {
	public var index:int;
	internal var text:Object;
	public function RewriteOperation(index:int, text:Object) {
		this.index = index;
		this.text = text;
	}
	/** Execute the rewrite operation by possibly adding to the buffer.
	 *  Return the index of the next token to operate on.
	 */
	public function execute(state:RewriteState):void {
		state.index = index;
	}
	public function toString():String {
		return getQualifiedClassName(this) + "@" + index + '"' + text + '"';
	}
}

class InsertBeforeOp extends RewriteOperation {
	public function InsertBeforeOp(index:int, text:Object) {
		super(index,text);
	}
	public override function execute(state:RewriteState):void {
		state.buf += text;
		state.index = index;
	}
}

/** I'm going to try replacing range from x..y with (y-x)+1 ReplaceOp
 *  instructions.
 */
class ReplaceOp extends RewriteOperation {
	internal var lastIndex:int;
	public function ReplaceOp(fromIndex:int, toIndex:int, text:Object) {
		super(fromIndex, text);
		lastIndex = toIndex;
	}
	public override function execute(state:RewriteState):void {
		if ( text!=null ) {
			state.buf += text;
		}
		state.index = lastIndex+1;
	}
}

class DeleteOp extends ReplaceOp {
	public function DeleteOp(fromIndex:int, toIndex:int) {
		super(fromIndex, toIndex, null);
	}
}
