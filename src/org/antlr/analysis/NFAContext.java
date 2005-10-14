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
package org.antlr.analysis;

/** A tree node for tracking the call chains for NFAs that invoke
 *  other NFAs.  These trees only have to point upwards to their parents
 *  so we can walk back up the tree (i.e., pop stuff off the stack).  We
 *  never walk from stack down down through the children.
 *
 *  Each alt predicted in a decision has its own context tree,
 *  representing all possible return nodes.  The initial stack has
 *  EOF ("$") in it.  So, for m alternative productions, the lookahead
 *  DFA will have m NFAContext trees.
 *
 *  To "push" a new context, just do "new NFAContext(context-parent, state)"
 *  which will add itself to the parent.  The root is NFAContext(null, null).
 *
 *  The complete context for an NFA configuration is the set of invoking states
 *  on the path from this node thru the parent pointers to the root.
 */
public class NFAContext {
	/** This is similar to Bermudez's m constant in his LAR(m) where
	 *  you bound the stack so your states don't explode.  The main difference
	 *  is that I bound only recursion on the stack, not the simple stack size.
	 *  This looser constraint will let the conversion roam further to find
	 *  lookahead to resolve a decision.
	 *
	 *  Bermudez's m operates differently as it is his LR stack depth
	 *  I'm pretty sure it therefore includes all stack symbols.  Here I am
	 *  only preventing infinite recursion during a single closure operation.
	 *
	 *  max=0 implies you cannot ever recurse
	 *  (note you can make as many calls as you want--you just can't ever
	 *  visit a state that is on your rule invocation stack).
	 *  max=1 implies you are able to recurse once (i.e., call a rule twice
	 *  from the same place).
	 *
	 *  This tracks recursion to a rule specific to an invocation site!
	 *  It does not detect multiple calls to a rule from different rule
	 *  invocation states.  We are guaranteed to terminate because the
	 *  stack can only grow as big as the number of NFA states.
	 *
	 *  I noticed that the Java grammar didn't work with max=0, but did with
	 *  max=1.  Let's set to 2.  Recursion is sometimes needed to resolve some
	 *  fixed lookahead decisions.
	 */
	public static int MAX_RECURSIVE_INVOCATIONS = 1;

    public NFAContext parent;

    /** The NFA state that invoked another rule's start state is recorded
     *  on the rule invocation context stack.
     */
    public NFAState invokingState;

    /** Computing the hashCode is very expensive and closureBusy()
     *  uses it to track when it's seen a state|ctx before to avoid
     *  infinite loops.  As we add new contexts, record the hash code
     *  as this.invokingState + parent.cachedHashCode.  Avoids walking
     *  up the tree for every hashCode().  Note that this caching works
     *  because a context is a monotonically growing tree of context nodes
     *  and nothing on the stack is ever modified...ctx just grows
     *  or shrinks.
     */
    protected int cachedHashCode;

    public NFAContext(NFAContext parent, NFAState invokingState) {
        this.parent = parent;
        this.invokingState = invokingState;
        if ( invokingState!=null ) {
            this.cachedHashCode = invokingState.stateNumber;
        }
        if ( parent!=null ) {
            this.cachedHashCode += parent.cachedHashCode;
        }
    }

	/** Two contexts are equals() if both have
	 *  same call stack; walk upwards to the root.
	 *  Recall that the root sentinel node has no invokingStates and no parent.
	 *  Note that you may be comparing contexts in different alt trees.
	 *
	 *  The hashCode is now cheap as it's computed once upon each context
	 *  push on the stack.  Use it to make equals() more efficient.
	 */
	public boolean equals(Object o) {
		NFAContext other = ((NFAContext)o);
		if ( this.cachedHashCode != other.cachedHashCode ) {
			return false; // can't be same if hash is different
		}
		if ( this==other ) {
			return true;
		}
		// System.out.println("comparing "+this+" with "+other);
		NFAContext sp = this;
		while ( sp.parent!=null && other.parent!=null ) {
			if ( sp.invokingState != other.invokingState ) {
				return false;
			}
			sp = sp.parent;
			other = other.parent;
		}
		if ( !(sp.parent==null && other.parent==null) ) {
			return false; // both pointers must be at their roots after walk
		}
		return true;
	}

	/** Two contexts conflict() if they are equals() or one is a stack suffix
	 *  of the other.  For example, contexts [21 12 $] and [21 9 $] do not
	 *  conflict, but [21 $] and [21 12 $] do conflict.  Note that I should
	 *  probably not show the $ in this case.  There is a dummy node for each
	 *  stack that just means empty; $ is a marker that's all.
	 *
	 *  TODO: Seems that suffix returns true if equals; faster if we do suffix only?
	 */
	public boolean conflictsWith(Object o) {
		NFAContext other = ((NFAContext)o);
		return this.equals(other) || this.suffix(other);
	}

	/** [21 $] suffix [21 12 $]
	 *  [21 12 $] suffix [21 $]
	 *  [21 18 $] suffix [21 18 12 9 $]
	 *  [21 18 12 9 $] suffix [21 18 $]
	 *  [21 12 $] not suffix [21 9 $]
	 */
	protected boolean suffix(NFAContext other) {
		NFAContext sp = this;
		while ( sp.parent!=null && other.parent!=null ) {
			if ( sp.invokingState != other.invokingState ) {
				return false;
			}
			sp = sp.parent;
			other = other.parent;
		}
		//System.out.println("suffix");
		return true;
	}

    /** Walk upwards to the root of the call stack context looking
     *  for a particular invoking state.  Only look til before
	 *  initialContext (which is when the overall closure operation started).
	 *  We only care about re-invocations of a rule within same closure op
	 *  because that implies same rule is visited w/o consuming input.
	 *
	 *  The initialContext is always on the path to the root from 'this'
	 *  so walking upwards from 'this' will always hit initialContext
	 *  eventually; don't worry about hitting top of stack.
	 *
	 *  TOD O: use linked hashmap for this?
    public boolean contains(int state, NFAContext initialContext) {
        NFAContext sp = this;
		int n = 0; // track recursive invocations of state
		System.out.println("this.context is "+sp);
        //while ( sp.parent!=null && sp.parent!=initialContext ) {
		while ( sp.parent!=null ) {
            if ( sp.invokingState.stateNumber == state ) {
				n++;
                if ( n>=MAX_RECURSIVE_INVOCATIONS ) {
					System.out.println("contains");
					return true;
				}
            }
            sp = sp.parent;
        }
        return false;
    }
	 */

	/** Given an NFA state number, how many times has the NFA-to-DFA
	 *  conversion pushed that state on the stack?  In other words,
	 *  the NFA state must be a rule invocation state and this method
	 *  tells you how many times you've been to this state.  If none,
	 *  then you have not called the target rule from this state before
	 *  (though another NFA state could have called that target rule).
	 *  If n=1, then you've been to this state before during this
	 *  DFA construction and are going to invoke that rule again.
	 *
	 *  Note that many NFA states can invoke rule r, but we ignore recursion
	 *  unless you hit the same rule invocation state again.
	 */
	public int recursionDepthEmanatingFromState(int state) {
		NFAContext sp = this;
		int n = 0; // track recursive invocations of target from this state
		//System.out.println("this.context is "+sp);
		while ( sp.parent!=null ) {
			if ( sp.invokingState.stateNumber == state ) {
				n++;
			}
			sp = sp.parent;
		}
		return n;
	}

    public int hashCode() {
        return cachedHashCode;
        /*
        int h = 0;
        NFAContext sp = this;
        while ( sp.parent!=null ) {
            h += sp.invokingState.getStateNumber();
            sp = sp.parent;
        }
        return h;
        */
    }

	/** A context is empty if there is no parent; meaning nobody pushed
	 *  anything on the call stack.
	 */
	public boolean isEmpty() {
		return parent==null;
	}

    public String toString() {
        StringBuffer buf = new StringBuffer();
        NFAContext sp = this;
        buf.append("[");
        while ( sp.parent!=null ) {
            buf.append(sp.invokingState.stateNumber);
            buf.append(" ");
            sp = sp.parent;
        }
        buf.append("$]");
        return buf.toString();
    }
}
