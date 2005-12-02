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

import antlr.collections.AST;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.codegen.CodeGenerator;
import org.antlr.tool.ANTLRParser;
import org.antlr.tool.GrammarAST;

import java.util.Set;

/** A binary tree structure used to record the semantic context in which
 *  an NFA configuration is valid.  It's either a single predicate or
 *  a tree representing an operation tree such as: p1&&p2 or p1||p2.
 *
 *  For NFA o-p1->o-p2->o, create tree AND(p1,p2).
 *  For NFA (1)-p1->(2)
 *           |       ^
 *           |       |
 *          (3)-p2----
 *  we will have to combine p1 and p2 into DFA state as we will be
 *  adding NFA configurations for state 2 with two predicates p1,p2.
 *  So, set context for combined NFA config for state 2: OR(p1,p2).
 *
 *  I have scoped the AND, NOT, OR, and Predicate subclasses of
 *  SemanticContext within the scope of this outer class.
 */
public abstract class SemanticContext {
    /** Create a default value for the semantic context shared among all
     *  NFAConfigurations that do not have an actual semantic context.
     *  This prevents lots of if!=null type checks all over; it represents
     *  just an empty set of predicates.
     */
    public static final SemanticContext EMPTY_SEMANTIC_CONTEXT = new Predicate();

    public abstract SemanticContext reduce();

	/** Given a semantic context expression tree, return a tree with all
	 *  nongated predicates set to true and then reduced.  So p&&(q||r) would
	 *  return p&&r if q is nongated but p and r are gated.
	 */
	public abstract SemanticContext getGatedPredicateContext();

    /** Generate an expression that will evaluate the semantic context,
     *  given a set of output templates.
     */
    public abstract StringTemplate genExpr(CodeGenerator generator,
										   StringTemplateGroup templates);

    public static class Predicate extends SemanticContext {
        /** The AST node in tree created from the grammar holding the predicate */
        protected AST predicate;

		/** Is this a {...}?=> gating predicate or a normal disambiguating {..}?
		 *  If any predicate in expression is gated, then expression is considered
		 *  gated.
		 *
		 *  The simple Predicate object's predicate AST's type is used to set
		 *  gated to true if type==GATED_SEMPRED.
		 */
		protected boolean gated = false;

		public static final int INVALID_PRED_VALUE = -1;
		public static final int FALSE_PRED = 0;
		public static final int TRUE_PRED = 1;

		/** sometimes predicates are known to be true or false; we need
		 *  a way to represent this without resorting to a target language
		 *  value like true or TRUE.
		 */
		protected int constantValue = INVALID_PRED_VALUE;

        public Predicate() {
			predicate = new GrammarAST();
			//predicate.initialize(ANTLRParser.SEMPRED, "true");
			this.gated=false;
        }

        public Predicate(AST predicate) {
            this.predicate = predicate;
			this.gated = predicate.getType()==ANTLRParser.GATED_SEMPRED;
        }

		public Predicate(Predicate p) {
			this.predicate = p.predicate;
			this.gated = p.gated;
			this.constantValue = p.constantValue;
		}

        public SemanticContext reduce() {
            // single pred is already reduced
            return this;
        }

        /** Two predicates are the same if they are literally the same
         *  text rather than same node in the grammar's AST.
		 *  Or, if they have the same constant value, return equal.
         */
        public boolean equals(Object o) {
            if ( !(o instanceof Predicate) ) {
                return false;
            }
            return predicate.getText().equals(((Predicate)o).predicate.getText());
        }

        public int hashCode() {
            if ( predicate==null ) {
                return 0;
            }
            return predicate.getText().hashCode();
        }

        public StringTemplate genExpr(CodeGenerator generator,
									  StringTemplateGroup templates)
		{
			StringTemplate eST = null;
			if ( templates!=null ) {
				eST = templates.getInstanceOf("evalPredicate");
				eST.setAttribute("pred", this.toString());
			}
			else {
				eST = new StringTemplate("$pred$");
				eST.setAttribute("pred", this.toString());
				return eST;
			}
			if ( generator!=null ) {
				String description =
					generator.target.getTargetStringLiteralFromString(this.toString());
				eST.setAttribute("description", description);
			}
            return eST;
        }

		public SemanticContext getGatedPredicateContext() {
			if ( gated ) {
				return this;
			}
			return null;
		}

        public String toString() {
			if ( constantValue==TRUE_PRED ) {
				return "true";
			}
			if ( constantValue==FALSE_PRED ) {
				return "false";
			}
            if ( predicate==null ) {
                return "<nopred>";
            }
            return predicate.getText();
        }
    }

	public static class TruePredicate extends Predicate {
		public TruePredicate() {
			super();
			this.constantValue = TRUE_PRED;
		}
	}

	public static class FalsePredicate extends Predicate {
		public FalsePredicate() {
			super();
			this.constantValue = FALSE_PRED;
		}
	}

	public static class AND extends SemanticContext {
        protected SemanticContext left,right;
        public AND(SemanticContext a, SemanticContext b) {
            this.left = a;
            this.right = b;
        }
        public SemanticContext reduce() {
            left.reduce();
            right.reduce();
            return this;
        }
        public boolean equals(Object o) {
            if ( !(o instanceof AND) ) {
                return false;
            }
            return left.equals(((AND)o).left) && right.equals(((AND)o).right);
        }
        public int hashCode() {
            return left.hashCode() + right.hashCode();
        }
        public StringTemplate genExpr(CodeGenerator generator,
									  StringTemplateGroup templates)
		{
			StringTemplate eST = null;
			if ( templates!=null ) {
				eST = templates.getInstanceOf("andPredicates");
			}
			else {
				eST = new StringTemplate("($left$&&$right$)");
			}
            eST.setAttribute("left", left);
            eST.setAttribute("right", right);
            return eST;
        }
		public SemanticContext getGatedPredicateContext() {
			SemanticContext gatedLeft = left.getGatedPredicateContext();
			SemanticContext gatedRight = right.getGatedPredicateContext();
			if ( gatedLeft==null ) {
				return gatedRight;
			}
			if ( gatedRight==null ) {
				return gatedLeft;
			}
			return new AND(gatedLeft, gatedRight);
		}
        public String toString() {
            return "("+left+"&&"+right+")";
        }
    }

    public static class OR extends SemanticContext {
        protected SemanticContext left,right;
        public OR(SemanticContext a, SemanticContext b) {
            this.left = a;
            this.right = b;
        }
        /** Interestingly, I only seem to need OR's reduce */
        public SemanticContext reduce() {
            // (p1a||p1b)||p1a => p1a||p1b where a=(p1a||p1b) and b=p1a
            left.reduce();
            right.reduce();
			// (p||q)||p => p||q or vice versa
			if ( left instanceof OR && right instanceof Predicate ) {
                OR leftOr = (OR)left;
                Predicate p = (Predicate)right;
                if ( leftOr.left.equals(p) || leftOr.right.equals(p) ) {
                    return left;
                }
            }
            if ( left instanceof Predicate && right instanceof OR ) {
                OR rightOr = (OR)right;
                Predicate p = (Predicate)left;
                if ( rightOr.left.equals(p) || rightOr.right.equals(p) ) {
                    return right;
                }
            }
			// p||!p => true
			if ( left.equals(not(right)) ) {
				return new TruePredicate(); // return true pred
			}
			// !p||p => true
			if ( right.equals(not(left)) ) {
				return new TruePredicate(); // return true pred
			}

            return this;
        }
        public boolean equals(Object o) {
            if ( !(o instanceof OR) ) {
                return false;
            }
            return left.equals(((OR)o).left) && right.equals(((OR)o).right);
        }
        public int hashCode() {
            return left.hashCode() + right.hashCode();
        }
        public StringTemplate genExpr(CodeGenerator generator,
									  StringTemplateGroup templates)
		{
            StringTemplate eST = null;
			if ( templates!=null ) {
				eST = templates.getInstanceOf("orPredicates");
			}
			else {
				eST = new StringTemplate("($left$||$right$)");
			}
            eST.setAttribute("left", left);
            eST.setAttribute("right", right);
            return eST;
        }
		public SemanticContext getGatedPredicateContext() {
			SemanticContext gatedLeft = left.getGatedPredicateContext();
			SemanticContext gatedRight = right.getGatedPredicateContext();
			if ( gatedLeft==null ) {
				return gatedRight;
			}
			if ( gatedRight==null ) {
				return gatedLeft;
			}
			return new AND(gatedLeft, gatedRight);
		}
        public String toString() {
            return "("+left+"||"+right+")";
        }
    }

    public static class NOT extends SemanticContext {
        protected SemanticContext ctx;
        public NOT(SemanticContext ctx) {
            this.ctx = ctx;
        }
        public SemanticContext reduce() {
            ctx.reduce();
            return this;
        }
        public boolean equals(Object o) {
            if ( !(o instanceof NOT) ) {
                return false;
            }
            return ctx.equals(((NOT)o).ctx);
        }
        public int hashCode() {
            return ctx.hashCode();
        }
        public StringTemplate genExpr(CodeGenerator generator,
									  StringTemplateGroup templates)
		{
			StringTemplate eST = null;
			if ( templates!=null ) {
				eST = templates.getInstanceOf("notPredicate");
			}
			else {
				eST = new StringTemplate("?!($pred$)");
			}
            eST.setAttribute("pred", ctx);
            return eST;
        }
		public SemanticContext getGatedPredicateContext() {
			SemanticContext p = ctx.getGatedPredicateContext();
			if ( p==null ) {
				return null;
			}
			return new NOT(p);
		}
        public String toString() {
            return "!("+ctx+")";
        }
    }

    public static SemanticContext and(SemanticContext a, SemanticContext b) {
        if ( a==EMPTY_SEMANTIC_CONTEXT || a==null ) {
            return b;
        }
        if ( b==EMPTY_SEMANTIC_CONTEXT || b==null ) {
            return a;
        }
        if ( a.equals(b) ) {
            return a; // if same, just return left one
        }
        return new AND(a,b).reduce();
    }

    public static SemanticContext or(SemanticContext a, SemanticContext b) {
        if ( a==EMPTY_SEMANTIC_CONTEXT || a==null ) {
            return b;
        }
        if ( b==EMPTY_SEMANTIC_CONTEXT || b==null ) {
            return a;
        }
        if ( a.equals(b) ) {
            return a; // if same, just return left one
        }
        return new OR(a,b).reduce();
    }

    public static SemanticContext not(SemanticContext a) {
        return new NOT(a).reduce();
    }

}
