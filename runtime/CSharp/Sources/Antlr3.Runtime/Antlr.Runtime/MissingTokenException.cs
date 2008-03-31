/*
[The "BSD licence"]
Copyright (c) 2007-2008 Johannes Luber
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

using System;

namespace Antlr.Runtime
{
	
	/// <summary>
	/// We were expecting a token but it's not found. The current token
	/// is actually what we wanted next.
	/// </summary>
	[Serializable]
	public class MissingTokenException : MismatchedTokenException {
		public MissingTokenException(int expecting, IIntStream input)
			: base(expecting, input) {
		}
	
		public int MissingType {
			get { return expecting; }
		}
	
		public override String ToString() {
			return "MissingTokenException(expected " + expecting + ")";
		}
	}
}
