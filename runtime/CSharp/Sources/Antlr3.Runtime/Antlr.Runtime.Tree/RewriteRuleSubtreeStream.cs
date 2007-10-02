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


namespace Antlr.Runtime.Tree
{
	using System;
	using IList = System.Collections.IList;
	
	public class RewriteRuleSubtreeStream : RewriteRuleElementStream
	{
		public RewriteRuleSubtreeStream(ITreeAdaptor adaptor, string elementDescription)
			: base(adaptor, elementDescription)
		{
		}
		
		/// <summary>
		/// Create a stream with one element
		/// </summary>
		public RewriteRuleSubtreeStream(ITreeAdaptor adaptor, string elementDescription, object oneElement)
			: base(adaptor, elementDescription, oneElement)
		{
		}
		
		/// <summary>
		/// Create a stream, but feed off an existing list
		/// </summary>
		public RewriteRuleSubtreeStream(ITreeAdaptor adaptor, string elementDescription, IList elements)
			: base(adaptor, elementDescription, elements)
		{
		}
		
		/// <summary>
		/// Treat next element as a single node even if it's a subtree.
		/// </summary>
		/// <remarks>
		/// This is used instead of next() when the result has to be a
		/// tree root node.  Also prevents us from duplicating recently-added
		/// children; e.g., ^(type ID)+ adds ID to type and then 2nd iteration
		/// must dup the type node, but ID has been added.
		///
		/// Referencing a rule result twice is ok; dup entire tree as
		/// we can't be adding trees as root; e.g., expr expr. 
		/// 
		/// Hideous code duplication here with respect to base.NextTree() inherited from Java version.
		/// Can't think of a proper way to refactor.  This needs to always call dup node
		/// and base.NextTree() doesn't know which to call: dup node or dup tree.
		/// </remarks>
		public object NextNode()
		{
			int size = Size();
			if (dirty || ((cursor >= size) && (size == 1)))
			{
				// if out of elements and size is 1, dup (at most a single node
				// since this is for making root nodes).
				object el = _Next();
				return adaptor.DupNode(el);
			}
			// test size above then fetch
			object elem = _Next();
			return elem;
		}
		
		override protected object Dup(object el) 
		{
			return adaptor.DupTree(el);
		}
	}
}