package org.antlr.runtime.misc;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/** A queue that can dequeue in O(1) and grow arbitrarily large.
 *  Grows until you dequeue last element in last valid index position. Then
 *  it resets to start filling at 0 again.
 */
public class CircularQueue<T> implements Iterator {
    /** dynamically-sized buffer of elements */
    protected List<T> data = new ArrayList<T>();
    /** index of next element to fill */
    protected int p = 0;

    public CircularQueue() {;}

    public void remove() {
        p++;
        // have we hit end of buffer?
        if ( p == data.size() ) {
            // if so, it's an opportunity to start filling at index 0 again
            clear(); // size goes to 0, but retains memory
        }
    }

    public T dequeue() {
        T o = get(0);
        remove();
        return o;
    }

    public void add(T o) { data.add(o); }

    public int size() { return data.size() - p; }

    /** Get and remove first element in queue */
    public T next() { return dequeue(); }

    public T get(int i) {
        if ( p+i >= data.size() ) {
            throw new NoSuchElementException("queue index "+i+" > size "+size());
        }
        return data.get(p+i);
    }

    public boolean hasNext() { return data.size()-p>0; }

    public void clear() { p = 0; data.clear(); }

    /** Return string of current buffer contents; non-destructive */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        int n = size();
        for (int i=0; i<n; i++) {
            buf.append(get(i));
            if ( (i+1)<n ) buf.append(" ");
        }
        return buf.toString();
    }
}