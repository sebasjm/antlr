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

import java.util.Set;
import java.util.HashSet;

public abstract class Token {
	public static final int EOR_TOKEN_TYPE = 1;

    public static final int EOF = CharStream.EOF;
    public static final Token EOFToken = new CommonToken(EOF);
	public static final int DEFAULT_CHANNEL = 0;

	/** The set of @label.property references that are valid for a token
	 *  reference label.  @label.text -> label.getText() for example.
	 */
	public static final Set predefinedTokenProperties = new HashSet();
	static {
		predefinedTokenProperties.add("text");
		predefinedTokenProperties.add("type");
		predefinedTokenProperties.add("line");
		predefinedTokenProperties.add("index");
		predefinedTokenProperties.add("pos");
		predefinedTokenProperties.add("channel");
	}

	/** Get the text of the token */
	public abstract String getText();

	public abstract int getType();
    public abstract int getLine();
    public abstract void setLine(int line);

	/** The index of the character relative to the beginning of the line 0..n-1 */
	public abstract int getCharPositionInLine();
	public abstract void setCharPositionInLine(int pos);

	public abstract int getChannel();
	public abstract void setChannel(int channel);
	public abstract int getTokenIndex();
	public abstract void setTokenIndex(int index);
}
