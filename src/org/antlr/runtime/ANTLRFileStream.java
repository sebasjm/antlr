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
package org.antlr.runtime;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;

/** This is a char buffer stream that is loaded from a file
 *  all at once when you construct the object.
 */
public class ANTLRFileStream extends ANTLRStringStream {
    protected String fileName;

	public ANTLRFileStream() {
	}

	public ANTLRFileStream(String fileName) throws IOException {
		load(fileName);
	}

	public void load(String fileName) throws IOException {
		//System.out.println("loading "+fileName);
		this.fileName = fileName;
		FileReader fr = null;
		try {
			File f = new File(fileName);
			data = new char[(int)f.length()];
			fr = new FileReader(fileName);
			fr.read(data);
		}
		finally {
			if ( fr!=null ) {
				fr.close();
			}
		}
    }

	public String getSourceName() {
		return fileName;
	}
}
