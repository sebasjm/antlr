/*
 * [The "BSD licence"]
 * Copyright (c) 2005-2008 Terence Parr
 * All rights reserved.
 *
 * Conversion to C#:
 * Copyright (c) 2009 Sam Harwell
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

namespace Antlr.Runtime
{
    using Antlr.Runtime.Misc;
    using CLSCompliant = System.CLSCompliantAttribute;
    using NotSupportedException = System.NotSupportedException;

    public class UnbufferedTokenStream : LookaheadStream<IToken>, ITokenStream
    {
        [CLSCompliant(false)]
        protected ITokenSource tokenSource;
        protected int tokenIndex;
        protected IToken previousToken;

        public UnbufferedTokenStream(ITokenSource tokenSource)
            : base(Tokens.EndOfFile)
        {
            this.tokenSource = tokenSource;
        }

        public ITokenSource TokenSource
        {
            get
            {
                return this.tokenSource;
            }
        }

        public string SourceName
        {
            get
            {
                return TokenSource.SourceName;
            }
        }

        public override IToken NextElement()
        {
            IToken t = this.tokenSource.NextToken();
            t.TokenIndex = this.tokenIndex++;
            this.previousToken = t;
            return t;
        }

        public IToken Get(int i)
        {
            throw new NotSupportedException();
        }

        public int LA(int i)
        {
            return LT(i).Type;
        }

        public string ToString(int start, int stop)
        {
            return "n/a";
        }

        public string ToString(IToken start, IToken stop)
        {
            return "n/a";
        }

        protected override IToken LB(int k)
        {
            if (k == 1)
                return this.previousToken;

            return null;
        }
    }
}
