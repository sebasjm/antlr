/*
[The "BSD licence"]
Copyright (c) 2005-2007 Kunle Odutola
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code MUST RETAIN the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form MUST REPRODUCE the above copyright
   notice, this list of conditions and the following disclaimer in 
   the documentation and/or other materials provided with the 
   distribution.
3. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior WRITTEN permission.
4. Unless explicitly state otherwise, any contribution intentionally 
   submitted for inclusion in this work to the copyright owner or licensor
   shall be under the terms and conditions of this license, without any 
   additional terms or conditions.

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


namespace Antlr.Runtime
{
	using System;
	using StringBuilder = System.Text.StringBuilder;
	using IList			= System.Collections.IList;
	using ArrayList		= System.Collections.ArrayList;
	using IDictionary	= System.Collections.IDictionary;
	using Hashtable		= System.Collections.Hashtable;
	using IComparer		= System.Collections.IComparer;
	
	/// <summary>Useful for dumping out the input stream after doing some
	/// augmentation or other manipulations.
	/// </summary>
	/// 
	/// <remarks>
	/// You can insert stuff, Replace, and delete chunks.  Note that the
	/// operations are done lazily--only if you convert the buffer to a
	/// String.  This is very efficient because you are not moving data around
	/// all the time.  As the buffer of tokens is converted to strings, the
	/// ToString() method(s) check to see if there is an operation at the
	/// current index.  If so, the operation is done and then normal String
	/// rendering continues on the buffer.  This is like having multiple Turing
	/// machine instruction streams (programs) operating on a single input tape. :)
	/// 
	/// Since the operations are done lazily at ToString-time, operations do not
	/// screw up the token index values.  That is, an insert operation at token
	/// index i does not change the index values for tokens i+1..n-1.
	/// 
	/// Because operations never actually alter the buffer, you may always get
	/// the original token stream back without undoing anything.  Since
	/// the instructions are queued up, you can easily simulate transactions and
	/// roll back any changes if there is an error just by removing instructions.
	/// For example,
	/// 
	/// CharStream input = new ANTLRFileStream("input");
	/// TLexer lex = new TLexer(input);
	/// TokenRewriteStream tokens = new TokenRewriteStream(lex);
	/// T parser = new T(tokens);
	/// parser.startRule();
	/// 
	/// Then in the rules, you can execute
	/// IToken t,u;
	/// ...
	/// input.InsertAfter(t, "text to put after t");}
	/// input.InsertAfter(u, "text after u");}
	/// System.out.println(tokens.ToString());
	/// 
	/// Actually, you have to cast the 'input' to a TokenRewriteStream. :(
	/// 
	/// You can also have multiple "instruction streams" and get multiple
	/// rewrites from a single pass over the input.  Just name the instruction
	/// streams and use that name again when printing the buffer.  This could be
	/// useful for generating a C file and also its header file--all from the
	/// same buffer:
	/// 
	/// tokens.InsertAfter("pass1", t, "text to put after t");}
	/// tokens.InsertAfter("pass2", u, "text after u");}
	/// System.out.println(tokens.ToString("pass1"));
	/// System.out.println(tokens.ToString("pass2"));
	/// 
	/// If you don't use named rewrite streams, a "default" stream is used as
	/// the first example shows.
	/// </remarks>
	public class TokenRewriteStream : CommonTokenStream
	{
		public const string DEFAULT_PROGRAM_NAME = "default";
		public const int PROGRAM_INIT_SIZE = 100;
		public const int MIN_TOKEN_INDEX = 0;

		private class RewriteOpComparer : IComparer
		{
			public virtual int Compare(object o1, object o2)
			{
				RewriteOperation r1 = (RewriteOperation) o1;
				RewriteOperation r2 = (RewriteOperation) o2;
				if (r1.index < r2.index)
					return - 1;
				if (r1.index > r2.index)
					return 1;
				return 0;
			}
		}
		
		// Define the rewrite operation hierarchy
		
		protected internal class RewriteOperation
		{
			protected internal int index;
			protected internal object text;

			protected internal RewriteOperation(int index, object text)
			{
				this.index = index;
				this.text = text;
			}
			/// <summary>Execute the rewrite operation by possibly adding to the buffer.
			/// Return the index of the next token to operate on.
			/// </summary>
			public virtual int Execute(StringBuilder buf)
			{
				return index;
			}
			public override string ToString()
			{
				string opName = GetType().FullName;
				int dollarIndex = opName.IndexOf('$');
				opName = opName.Substring(dollarIndex + 1, (opName.Length) - (dollarIndex + 1));
				return opName + "@" + index + '"' + text + '"';
			}
		}
		
		protected internal class InsertBeforeOp : RewriteOperation
		{
			public InsertBeforeOp(int index, object text)
				: base(index, text)
			{
			}
			public override int Execute(StringBuilder buf)
			{
				buf.Append(text);
				return index;
			}
		}
		
		/// <summary>I'm going to try replacing range from x..y with (y-x)+1 ReplaceOp
		/// instructions.
		/// </summary>
		protected internal class ReplaceOp : RewriteOperation
		{
			int lastIndex;
			public ReplaceOp(int from, int to, object text)
				: base(from, text)
			{
				lastIndex = to;
			}
			public override int Execute(StringBuilder buf)
			{
				if (text != null)
				{
					buf.Append(text);
				}
				return lastIndex + 1;
			}
		}
		
		protected internal class DeleteOp : ReplaceOp
		{
			public DeleteOp(int from, int to)
				: base(from, to, null)
			{
			}
		}
		
		/// <summary>You may have multiple, named streams of rewrite operations.
		/// I'm calling these things "programs."
		/// Maps String (name) -> rewrite (List)
		/// </summary>
		protected IDictionary programs = null;
		
		/// <summary>Map String (program name) -> Integer index </summary>
		protected IDictionary lastRewriteTokenIndexes = null;
		
		public TokenRewriteStream()
		{
			Init();
		}
		
		public TokenRewriteStream(ITokenSource tokenSource)
			: base(tokenSource)
		{
			Init();
		}
		
		public TokenRewriteStream(ITokenSource tokenSource, int channel)
			: base(tokenSource, channel)
		{
			Init();
		}

		protected internal virtual void Init()
		{
			programs = new Hashtable();
			programs[DEFAULT_PROGRAM_NAME] = new ArrayList(PROGRAM_INIT_SIZE);
			lastRewriteTokenIndexes = new Hashtable();
		}

		public virtual void Rollback(int instructionIndex)
		{
			Rollback(DEFAULT_PROGRAM_NAME, instructionIndex);
		}
		
		/// <summary>Rollback the instruction stream for a program so that
		/// the indicated instruction (via instructionIndex) is no
		/// longer in the stream.  UNTESTED!
		/// </summary>
		public virtual void Rollback(string programName, int instructionIndex)
		{
			IList instructionStream = (IList) programs[programName];
			if (instructionStream != null)
			{
				programs[programName] = (IList) ((ArrayList) instructionStream).GetRange(MIN_TOKEN_INDEX, instructionIndex - MIN_TOKEN_INDEX);
			}
		}
		
		public virtual void DeleteProgram()
		{
			DeleteProgram(DEFAULT_PROGRAM_NAME);
		}
		
		/// <summary>Reset the program so that no instructions exist </summary>
		public virtual void DeleteProgram(string programName)
		{
			Rollback(programName, MIN_TOKEN_INDEX);
		}
		
		/// <summary>If op.index > lastRewriteTokenIndexes, just add to the end.
		/// Otherwise, do linear 
		/// </summary>
		protected virtual void AddToSortedRewriteList(RewriteOperation op)
		{
			AddToSortedRewriteList(DEFAULT_PROGRAM_NAME, op);
		}
		
		/// <summary>Add an instruction to the rewrite instruction list ordered by
		/// the instruction number (use a binary search for efficiency).
		/// The list is ordered so that ToString() can be done efficiently.
		/// </summary>
		/// 
		/// <remarks>
		/// When there are multiple instructions at the same index, the instructions
		/// must be ordered to ensure proper behavior.  For example, a delete at
		/// index i must kill any Replace operation at i.  Insert-before operations
		/// must come before any Replace / Delete instructions.  If there are
		/// multiple insert instructions for a single index, they are done in
		/// reverse insertion order so that "insert foo" then "insert bar" yields
		/// "foobar" in front rather than "barfoo".  This is convenient because
		/// I can insert new InsertOp instructions at the index returned by
		/// the binary search.  A ReplaceOp kills any previous Replace op.  Since
		/// Delete is the same as Replace with null text, i can check for
		/// ReplaceOp and cover DeleteOp at same time. :)
		/// </remarks>
		protected virtual void AddToSortedRewriteList(string programName, RewriteOperation op)
		{
			IList rewrites = GetProgram(programName);
			int pos = ((ArrayList) rewrites).BinarySearch(op, new RewriteOpComparer());
			
			if (pos >= 0)
			{
				// binarySearch does not guarantee first element when multiple
				// are found.  I must seach backwards for first op with op.index
				for (; pos >= 0; pos--)
				{
					RewriteOperation prevOp = (RewriteOperation) rewrites[pos];
					if (prevOp.index < op.index)
					{
						break;
					}
				}
				pos++; // pos points at first op before ops with op.index; go back up one
				// now pos is the index in rewrites of first op with op.index
				//System.out.println("first op with op.index: pos="+pos);
				
				// an instruction operating already on that index was found;
				// make this one happen after all the others
				if (op is ReplaceOp)
				{
					bool replaced = false;
					int i;
					// look for an existing Replace
					for (i = pos; i < rewrites.Count; i++)
					{
						RewriteOperation prevOp = (RewriteOperation) rewrites[pos];
						if (prevOp.index != op.index)
						{
							break;
						}
						if (prevOp is ReplaceOp)
						{
							rewrites[pos] = op; // Replace old with new
							replaced = true;
							break;
						}
						// keep going; must be an insert
					}
					if (!replaced)
					{
						// add Replace op to the end of all the inserts
						rewrites.Insert(i, op);
					}
				}
				else
				{
					// inserts are added in front of existing inserts
					rewrites.Insert(pos, op);
				}
			}
			else
			{
				rewrites.Insert(-pos - 1, op);
			}
		}
		
		public virtual void  InsertAfter(IToken t, object text)
		{
			InsertAfter(DEFAULT_PROGRAM_NAME, t, text);
		}
		
		public virtual void  InsertAfter(int index, object text)
		{
			InsertAfter(DEFAULT_PROGRAM_NAME, index, text);
		}
		
		public virtual void  InsertAfter(string programName, IToken t, object text)
		{
			InsertAfter(programName, t.TokenIndex, text);
		}
		
		public virtual void  InsertAfter(string programName, int index, object text)
		{
			// to insert after, just insert before next index (even if past end)
			InsertBefore(programName, index + 1, text);
			//AddToSortedRewriteList(programName, new InsertAfterOp(index,text));
		}
		
		public virtual void  InsertBefore(IToken t, object text)
		{
			InsertBefore(DEFAULT_PROGRAM_NAME, t, text);
		}
		
		public virtual void  InsertBefore(int index, object text)
		{
			InsertBefore(DEFAULT_PROGRAM_NAME, index, text);
		}
		
		public virtual void  InsertBefore(string programName, IToken t, object text)
		{
			InsertBefore(programName, t.TokenIndex, text);
		}
		
		public virtual void  InsertBefore(string programName, int index, object text)
		{
			AddToSortedRewriteList(programName, new InsertBeforeOp(index, text));
		}
		
		public virtual void  Replace(int index, object text)
		{
			Replace(DEFAULT_PROGRAM_NAME, index, index, text);
		}
		
		public virtual void  Replace(int from, int to, object text)
		{
			Replace(DEFAULT_PROGRAM_NAME, from, to, text);
		}
		
		public virtual void  Replace(IToken indexT, object text)
		{
			Replace(DEFAULT_PROGRAM_NAME, indexT, indexT, text);
		}
		
		public virtual void  Replace(IToken from, IToken to, object text)
		{
			Replace(DEFAULT_PROGRAM_NAME, from, to, text);
		}
		
		public virtual void  Replace(string programName, int from, int to, object text)
		{
			if ( (from > to) || (from < 0) || (to < 0) ) 
			{
				return;
			}
			AddToSortedRewriteList(programName, new ReplaceOp(from, to, text));
		}
		
		public virtual void  Replace(string programName, IToken from, IToken to, object text)
		{
			Replace(programName, from.TokenIndex, to.TokenIndex, text);
		}
		
		public virtual void  Delete(int index)
		{
			Delete(DEFAULT_PROGRAM_NAME, index, index);
		}
		
		public virtual void  Delete(int from, int to)
		{
			Delete(DEFAULT_PROGRAM_NAME, from, to);
		}
		
		public virtual void  Delete(IToken indexT)
		{
			Delete(DEFAULT_PROGRAM_NAME, indexT, indexT);
		}
		
		public virtual void  Delete(IToken from, IToken to)
		{
			Delete(DEFAULT_PROGRAM_NAME, from, to);
		}
		
		public virtual void  Delete(string programName, int from, int to)
		{
			Replace(programName, from, to, null);
		}
		
		public virtual void  Delete(string programName, IToken from, IToken to)
		{
			Replace(programName, from, to, null);
		}
		
		public virtual int GetLastRewriteTokenIndex()
		{
			return GetLastRewriteTokenIndex(DEFAULT_PROGRAM_NAME);
		}
		
		protected virtual int GetLastRewriteTokenIndex(string programName)
		{
			object I = lastRewriteTokenIndexes[programName];
			if (I == null)
			{
				return -1;
			}
			return (int)I;
		}
		
		protected virtual void  SetLastRewriteTokenIndex(string programName, int i)
		{
			lastRewriteTokenIndexes[programName] = i;
		}
		
		protected virtual IList GetProgram(string name)
		{
			IList instructionStream = (IList) programs[name];
			if (instructionStream == null)
			{
				instructionStream = InitializeProgram(name);
			}
			return instructionStream;
		}
		
		private IList InitializeProgram(string name)
		{
			IList instructionStream = new ArrayList(PROGRAM_INIT_SIZE);
			programs[name] = instructionStream;
			return instructionStream;
		}
		
		public virtual string ToOriginalString()
		{
			return ToOriginalString(MIN_TOKEN_INDEX, Size() - 1);
		}
		
		public virtual string ToOriginalString(int start, int end)
		{
			StringBuilder buf = new StringBuilder();
			for (int i = start; (i >= MIN_TOKEN_INDEX) && (i <= end) && (i < tokens.Count); i++)
			{
				buf.Append(Get(i).Text);
			}
			return buf.ToString();
		}
		
		public override string ToString()
		{
			return ToString(MIN_TOKEN_INDEX, Size()-1);
		}
		
		public virtual string ToString(string programName)
		{
			return ToString(programName, MIN_TOKEN_INDEX, Size()-1);
		}
		
		public override string ToString(int start, int end)
		{
			return ToString(DEFAULT_PROGRAM_NAME, start, end);
		}
		
		public virtual string ToString(string programName, int start, int end)
		{
			IList rewrites = (IList) programs[programName];
			if ( (rewrites == null) || (rewrites.Count == 0) )
			{
				return ToOriginalString(start, end); // no instructions to execute
			}
			StringBuilder buf = new StringBuilder();
			
			// Index of first rewrite we have not done
			int rewriteOpIndex = 0;
			
			int tokenCursor = start;
			while ( (tokenCursor >= MIN_TOKEN_INDEX) && (tokenCursor <= end) && (tokenCursor < tokens.Count) )
			{
				// execute instructions associated with this token index
				if (rewriteOpIndex < rewrites.Count)
				{
					RewriteOperation op = (RewriteOperation) rewrites[rewriteOpIndex];
		
					// skip all ops at lower index
					while ( (op.index < tokenCursor) && (rewriteOpIndex < rewrites.Count) ) 
					{
						rewriteOpIndex++;
						if ( rewriteOpIndex < rewrites.Count ) 
						{
							op = (RewriteOperation)rewrites[rewriteOpIndex];
						}
					}

					// while we have ops for this token index, exec them
					while ( (tokenCursor == op.index) && (rewriteOpIndex < rewrites.Count) )
					{
						tokenCursor = op.Execute(buf);
						rewriteOpIndex++;
						if (rewriteOpIndex < rewrites.Count)
						{
							op = (RewriteOperation) rewrites[rewriteOpIndex];
						}
					}
				}
				// dump the token at this index
				if (tokenCursor <= end)
				{
					buf.Append(Get(tokenCursor).Text);
					tokenCursor++;
				}
			}
			// now see if there are operations (append) beyond last token index
			for (int opi = rewriteOpIndex; opi < rewrites.Count; opi++)
			{
				RewriteOperation op = (RewriteOperation) rewrites[opi];
				if (op.index >= Size())
				{
					op.Execute(buf); // must be insertions if after last token
				}
			}
			return buf.ToString();
		}
		
		public virtual string ToDebugString()
		{
			return ToDebugString(MIN_TOKEN_INDEX, Size()-1);
		}
		
		public virtual string ToDebugString(int start, int end)
		{
			StringBuilder buf = new StringBuilder();
			for (int i = start; i >= MIN_TOKEN_INDEX && i <= end && i < tokens.Count; i++)
			{
				buf.Append(Get(i));
			}
			return buf.ToString();
		}
	}
}