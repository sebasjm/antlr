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

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

public class ANTLRFileStream implements CharStream {
    protected int p=0;
    String fileName;
    // todo: doesn't handle supplemental unicode codes
    StringBuffer data = new StringBuffer(1000);

    public ANTLRFileStream(String fileName) throws IOException {
        this.fileName = fileName;
        System.out.println("opening file "+fileName);
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        try {
            int c = br.read();
            // todo: slow reading char by char, but read(char[]) requires
            // extra move :(
            while ( c!=-1 ) {
                data.append((char)c);
                c = br.read();
            }
        }
        finally {
            br.close();
            fr.close();
        }
    }

    public void consume() {
        if ( p < data.length() ) {
            p++;
        }
    }

    public int LA(int i) {
        if ( (p+i-1) >= data.length() ) {
            //System.out.println("char LA("+i+")=EOF; p="+p);
            return CharStream.EOF;
        }
        //System.out.println("char LA("+i+")="+data.charAt(p+i-1)+"; p="+p);
        return data.charAt(p+i-1);
    }

    public int LA(int marker, int i) {
        return data.charAt(marker+i-1);
    }

    public int mark() {
        return index(); // already buffered, just return index
    }

    /** Return the current input symbol index 0..n where n indicates the
     *  last symbol has been read.
     */
    public int index() {
        return p;
    }

	public int size() {
		return data.length();
	}

    public void rewind(int marker) {
        p = marker;
    }

	public String substring(int start, int stop) {
		return data.substring(start,stop+1);
	}
}
