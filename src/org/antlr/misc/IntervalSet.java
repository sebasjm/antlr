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

import org.antlr.analysis.Label;
import org.antlr.tool.Grammar;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplate;

import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;

/** A set of integers that relies on ranges being common to do
 *  "run-length-encoded" like compression (if you view an IntSet like
 *  a BitSet with runs of 0s and 1s).  Only ranges are recorded so that
 *  a few ints up near value 1000 don't cause massive bitsets, just two
 *  integer intervals.
 *
 *  element values may be negative.  Useful for sets of EPSILON and EOF.
 *
 *  0..9 char range is index pair ['\u0030','\u0039'].
 *  Multiple ranges are encoded with multiple index pairs.  Isolated
 *  elements are encoded with an index pair where both intervals are the same.
 *
 *  The ranges are ordered and disjoint so that 2..6 appears before 101..103.
 */
public class IntervalSet implements IntSet {
    public static final IntervalSet empty = new IntervalSet();

    /** The list of sorted, disjoint intervals. */
    protected List intervals;

    /** Create a set with no elements */
    public IntervalSet() {
        intervals = new LinkedList();
    }

    /** Create a set with a single element, el. */
    public static IntervalSet of(int a) {
        IntervalSet s = new IntervalSet();
        s.add(a);
        return s;
    }

    /** Create a set with all ints within range [a..b] (inclusive) */
    public static IntervalSet of(int a, int b) {
        IntervalSet s = new IntervalSet();
        s.add(a,b);
        return s;
    }

    /** Add a single element to the set.  An isolated element is stored
     *  as a range el..el.
     */
    public void add(int el) {
        add(el,el);
    }

    /** Add interval; i.e., add all integers from a to b to set.
     *  If b<a, do nothing.
     *  Keep list in sorted order (by left range value).
     *  If overlap, combine ranges.  For example,
     *  If this is {1..5, 10..20}, adding 6..7 yields
     *  {1..5, 6..7, 10..20}.  Adding 4..8 yields {1..8, 10..20}.
     */
    public void add(int a, int b) {
        add(new Interval(a,b));
    }

    /*
    protected void add(Interval addition) {
        if ( addition.getB()<addition.getA() ) {
            return;
        }
        // find position in list
        for (ListIterator iter = intervals.listIterator(); iter.hasNext();) {
            Interval r = (Interval) iter.next();
            if ( addition.adjacent(r) ) {
                // next to each other, make a single larger interval
                iter.set(addition.union(r));
                return;
            }
            if ( addition.startsBeforeDisjoint(r) ) {
                // insert before r
                iter.previous();
                iter.add(addition);
                return;
            }
            if ( !addition.disjoint(r) ) {
                // overlap: make a single larger interval
                iter.set(addition.union(r));
                return;
            }
            // if disjoint and after r, a future iteration will handle it
        }
        // ok, must be after last interval (and disjoint from last interval)
        // just add it
        intervals.add(addition);
    }
    */

    protected void add(Interval addition) {
        //System.out.println("add "+addition+" to "+intervals.toString());
        if ( addition.b<addition.a ) {
            return;
        }
        // find position in list
        for (ListIterator iter = intervals.listIterator(); iter.hasNext();) {
            Interval r = (Interval) iter.next();
            if ( addition.equals(r) ) {
                return;
            }
            if ( addition.adjacent(r) || !addition.disjoint(r) ) {
                // next to each other, make a single larger interval
                Interval bigger = addition.union(r);
                iter.set(bigger);
                // make sure we didn't just create an interval that
                // should be merged with next interval in list
                if ( iter.hasNext() ) {
                    Interval next = (Interval) iter.next();
                    if ( bigger.adjacent(next)||!bigger.disjoint(next) ) {
                        // if we bump up against or overlap next, merge
                        iter.remove();   // remove this one
                        iter.previous(); // move backwards to what we just set
                        iter.set(bigger.union(next)); // set to 3 merged ones
                    }
                }
                return;
            }
            if ( addition.startsBeforeDisjoint(r) ) {
                // insert before r
                iter.previous();
                iter.add(addition);
                return;
            }
            /*
            if ( !addition.disjoint(r) ) {
                // next to each other, make a single larger interval
                Interval bigger = addition.union(r);
                iter.set(bigger);
                // make sure we didn't just create an interval that
                // should be merged with next interval in list
                if ( iter.hasNext() ) {
                    Interval next = (Interval) iter.next();
                    if ( bigger.adjacent(next)||!bigger.disjoint(next) ) {
                        // if we bump up against or overlap next, merge
                        iter.remove();   // remove this one
                        iter.previous(); // move backwards to what we just set
                        iter.set(bigger.union(next)); // set to 3 merged ones
                    }
                }
                return;
            }
            */
            // if disjoint and after r, a future iteration will handle it
        }
        // ok, must be after last interval (and disjoint from last interval)
        // just add it
        intervals.add(addition);
    }

    public void addAll(IntSet set) {
		if ( set==null ) {
			return;
		}
        if ( !(set instanceof IntSet) ) {
            throw new IllegalArgumentException("can't add non IntSet ("+
											   set.getClass().getName()+
											   ") to IntervalSet");
        }
        IntervalSet other = (IntervalSet)set;
        // walk set and add each interval
        for (Iterator iter = other.intervals.iterator(); iter.hasNext();) {
            Interval I = (Interval) iter.next();
            this.add(I.a,I.b);
        }
    }

    // TODO set complement broken for {1..96, 99..65534}
    public IntSet complement() {
        return this.complement(1,Label.MAX_CHAR_VALUE);
    }

    public IntSet complement(int maxElement) {
        return this.complement(1,maxElement);
    }

    public IntSet complement(int minElement, int maxElement) {
        return this.complement(IntervalSet.of(minElement,maxElement));
    }

    /** Given the set of possible values (rather than, say UNICODE or MAXINT),
     *  return a new set containing all elements in vocabulary, but not in
     *  this.  The computation is (vocabulary - this).
     *
     *  'this' is assumed to be either a subset or equal to vocabulary.
     */
    public IntSet complement(IntSet vocabulary) {
        if ( vocabulary==null ) {
            return null; // nothing in common with null set
        }

        return vocabulary.subtract(this); // difference - this
    }

    /** return a new set containing all elements in this but not in other.
     *  Intervals may have to be broken up when ranges in this overlap
     *  with ranges in other.  other is assumed to be a subset of this;
     *  anything that is in other but not in this will be ignored.
     */
    public IntSet subtract(IntSet other) {
        if ( other==null || !(other instanceof IntervalSet) ) {
            return null; // nothing in common with null set
        }

        IntervalSet diff = new IntervalSet();

        // iterate down both interval lists
        ListIterator thisIter = this.intervals.listIterator();
        ListIterator otherIter = ((IntervalSet)other).intervals.listIterator();
        Interval mine=null;
        Interval theirs=null;
        if ( thisIter.hasNext() ) {
            mine = (Interval)thisIter.next();
        }
        if ( otherIter.hasNext() ) {
            theirs = (Interval)otherIter.next();
        }
        while ( mine!=null ) {
            //System.out.println("mine="+mine+", theirs="+theirs);
            // CASE 1: nothing in theirs removes a chunk from mine
            if ( theirs==null || mine.disjoint(theirs) ) {
                // SUBCASE 1a: finished traversing theirs; keep adding mine now
                if ( theirs==null ) {
                    // add everything in mine to difference since theirs done
                    diff.add(mine);
                    mine = null;
                    if ( thisIter.hasNext() ) {
                        mine = (Interval)thisIter.next();
                    }
                }
                else {
                    // SUBCASE 1b: mine is completely to the left of theirs
                    // so we can add to difference; move mine, but not theirs
                    if ( mine.startsBeforeDisjoint(theirs) ) {
                        diff.add(mine);
                        mine = null;
                        if ( thisIter.hasNext() ) {
                            mine = (Interval)thisIter.next();
                        }
                    }
                    // SUBCASE 1c: theirs is completely to the left of mine
                    else {
                        // keep looking in theirs
                        theirs = null;
                        if ( otherIter.hasNext() ) {
                            theirs = (Interval)otherIter.next();
                        }
                    }
                }
            }
            else {
                // CASE 2: theirs breaks mine into two chunks
                if ( mine.properlyContains(theirs) ) {
                    // must add two intervals: stuff to left and stuff to right
                    diff.add(mine.a, theirs.a-1);
                    // don't actually add stuff to right yet as next 'theirs'
                    // might overlap with it
                    // The stuff to the right might overlap with next "theirs".
                    // so it is considered next
                    Interval right = new Interval(theirs.b+1, mine.b);
                    mine = right;
                    // move theirs forward
                    theirs = null;
                    if ( otherIter.hasNext() ) {
                        theirs = (Interval)otherIter.next();
                    }
                }

                // CASE 3: theirs covers mine; nothing to add to diff
                else if ( theirs.properlyContains(mine) ) {
                    // nothing to add, theirs forces removal totally of mine
                    // just move mine looking for an overlapping interval
                    mine = null;
                    if ( thisIter.hasNext() ) {
                        mine = (Interval)thisIter.next();
                    }
                }

                // CASE 4: non proper overlap
                else {
                    // overlap, but not properly contained
                    diff.add(mine.differenceNotProperlyContained(theirs));
                    // update iterators
                    boolean moveTheirs = true;
                    if ( mine.startsBeforeNonDisjoint(theirs) ||
                         theirs.b > mine.b )
                    {
                        // uh oh, right of theirs extends past right of mine
                        // therefore could overlap with next of mine so don't
                        // move theirs iterator yet
                        moveTheirs = false;
                    }
                    // always move mine
                    mine = null;
                    if ( thisIter.hasNext() ) {
                        mine = (Interval)thisIter.next();
                    }
                    if ( moveTheirs ) {
                        theirs = null;
                        if ( otherIter.hasNext() ) {
                            theirs = (Interval)otherIter.next();
                        }
                    }
                }
            }
        }
        return diff;
    }

    /** TODO: implement this! */
	public IntSet or(IntSet a) {
        return null;
    }

    /** Return a new set with the intersection of this set with other.  Because
     *  the intervals are sorted, we can use an iterator for each list and
     *  just walk them together.  This is roughly O(min(n,m)) for interval
     *  list lengths n and m.
     */
    public IntSet and(IntSet other) {
        if ( other==null || !(other instanceof IntervalSet) ) {
            return null; // nothing in common with null set
        }

        IntervalSet intersection = new IntervalSet();

        // iterate down both interval lists looking for nondisjoint intervals
        ListIterator thisIter = this.intervals.listIterator();
        ListIterator otherIter = ((IntervalSet)other).intervals.listIterator();
        Interval mine=null;
        Interval theirs=null;
        if ( thisIter.hasNext() ) {
            mine = (Interval)thisIter.next();
        }
        if ( otherIter.hasNext() ) {
            theirs = (Interval)otherIter.next();
        }
        while ( mine!=null && theirs!=null ) {
            //System.out.println("mine="+mine+" and theirs="+theirs);
            if ( mine.startsBeforeDisjoint(theirs) ) {
                // move this iterator looking for interval that might overlap
                mine = null;
                if ( thisIter.hasNext() ) {
                    mine = (Interval)thisIter.next();
                }
            }
            else if ( theirs.startsBeforeDisjoint(mine) ) {
                // move other iterator looking for interval that might overlap
                theirs = null;
                if ( otherIter.hasNext() ) {
                    theirs = (Interval)otherIter.next();
                }
            }
            else if ( mine.properlyContains(theirs) ) {
                // overlap, compute intersection and add to new list
                intersection.add(mine.intersection(theirs));
                theirs = null;
                if ( otherIter.hasNext() ) {
                    theirs = (Interval)otherIter.next();
                }
            }
            else if ( theirs.properlyContains(mine) ) {
                // overlap, compute intersection and add to new list
                intersection.add(mine.intersection(theirs));
                mine = null;
                if ( thisIter.hasNext() ) {
                    mine = (Interval)thisIter.next();
                }
            }
            else if ( !mine.disjoint(theirs) ) {
                // overlap, compute intersection and add to new list
                intersection.add(mine.intersection(theirs));
                // move both iterators to next ranges
                mine = null;
                if ( thisIter.hasNext() ) {
                    mine = (Interval)thisIter.next();
                }
                theirs = null;
                if ( otherIter.hasNext() ) {
                    theirs = (Interval)otherIter.next();
                }
            }
        }
        return intersection;
    }

    /** Is el in any range of this set? */
    public boolean member(int el) {
        for (ListIterator iter = intervals.listIterator(); iter.hasNext();) {
            Interval I = (Interval) iter.next();
            if ( el<I.a ) {
                break; // list is sorted and el is before this interval; not here
            }
            if ( el>=I.a && el<=I.b ) {
                return true; // found in this interval
            }
        }
        return false;
    }

    /** return true if this set has no members */
    public boolean isNil() {
        return intervals==null || intervals.size()==0;
    }

    /** If this set is a single integer, return it otherwise Label.INVALID */
    public int getSingleElement() {
        if ( intervals!=null && intervals.size()==1 ) {
            Interval I = (Interval)intervals.get(0);
            if ( I.a == I.b ) {
                return I.a;
            }
        }
        return Label.INVALID;
    }

    /** Return a list of Interval objects. */
    public List getIntervals() {
        return intervals;
    }

    /** Are two IntervalSets equal?  Because all intervals are sorted
     *  and disjoint, equals is a simple linear walk over both lists
     *  to make sure they are the same.  Interval.equals() is used
     *  by the List.equals() method to check the ranges.
     */
    public boolean equals(Object obj) {
        if ( obj==null || !(obj instanceof IntervalSet) ) {
            return false;
        }
        IntervalSet other = (IntervalSet)obj;
        return this.intervals.equals(other.intervals);
    }

    public String toString() {
        return toString(null);
    }

    public String toString(Grammar g) {
        StringBuffer buf = new StringBuffer();
        if ( this.intervals.size()>1 ) {
            buf.append("{");
        }
        if ( this.intervals==null || this.intervals.size()==0 ) {
            return "{}";
        }
        Iterator iter = this.intervals.iterator();
        while (iter.hasNext()) {
            Interval I = (Interval) iter.next();
            int a = I.a;
            int b = I.b;
            if ( a==b ) {
                if ( g!=null ) {
                    buf.append(g.getTokenName(a));
                }
                else {
                    buf.append(a);
                }
            }
            else {
                if ( g!=null ) {
                    buf.append(g.getTokenName(a)+".."+g.getTokenName(b));
                }
                else {
                    buf.append(a+".."+b);
                }
            }
            if ( iter.hasNext() ) {
                buf.append(", ");
            }
        }
        if ( this.intervals.size()>1 ) {
            buf.append("}");
        }
        return buf.toString();
    }

    public int size() {
        throw new NoSuchMethodError("IntervalSet.size() unimplemented");
    }

    public int[] toArray() {
        throw new NoSuchMethodError("IntervalSet.toArray() unimplemented");
    }

    public void remove(int el) {
        throw new NoSuchMethodError("IntervalSet.remove() unimplemented");
    }
}
