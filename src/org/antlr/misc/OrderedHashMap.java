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
package org.antlr.misc;

import java.util.*;

/** A HashMap that remembers the order that the elements were added.
 *  You can alter the ith element with set(i,value) too :)  Unique list.
 *
 *  TODO: replace with LinkedHashMap
 */
public class OrderedHashMap extends HashMap {

    protected List elements = new ArrayList();
    protected List keys = new ArrayList();

    public Object get(int i) {
        return elements.get(i);
    }

    /** Replace an existing value with a new value; updates the element
     *  list and the hash table, but not the key as that has not changed.
     *
     *  ####### FOR NOW, just a set, can't put key->value
     */
    public Object set(int i, Object value) {
        keys.set(i, value);
        Object oldElement = elements.get(i);
        elements.set(i,value); // update list
        super.remove(oldElement); // now update the hashtable, remove/add
        super.put(value,value);
        return oldElement;
    }

    public Object put(Object key, Object value) {
        keys.add(key);
        elements.add(value);
        return super.put(key, value);
    }

    /** Add a value to list; keep in hashtable for consistency also;
     *  Key is object itself.  Good for say asking if a certain string is in
     *  a list of strings.
     */
    public Object add(Object value) {
        return put(value, value);
    }

    public Object remove(Object o) {
        keys.remove(o);
        elements.remove(o);
        return super.remove(o);
    }

    public void clear() {
        keys.clear();
        elements.clear();
        super.clear();
    }

    /** Return the List holding list of table elements.  Note that you are
     *  NOT getting a copy so don't write to the list.
     */
    public List elements() {
        return elements;
    }

    public Set keySet() {
        return super.keySet();
    }

    public int size() {
        if ( elements.size()!=super.size() ) {
            System.err.println("size mismatch hashmap: "+super.toString());
            System.err.println("elements size is "+elements.size());
            System.err.println("keys size is "+keys.size());
            System.err.println("hashmap size is "+super.size());
        }
        return elements.size();
    }

    public String toString() {
        return elements.toString();
        /*
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for (int i = 0; i < elements.size(); i++) {
            Object o = (Object) elements.get(i);
            if ( i>0 ) {
                buf.append(", ");
            }
            buf.append(o);
        }
        buf.append("]");
        return buf.toString();
        */
    }

}
