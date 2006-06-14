Early Access ANTLR v3
ANTLR 3.0ea10
June 2, 2006

Terence Parr, parrt at cs usfca edu
ANTLR project lead and supreme dictator
University of San Francisco

INTRODUCTION 

Welcome to ANTLR v3!  I've been working on this for 3 years and, while
it is not ready for a full release, it may prove useful to some of
you.  No primary functionality is missing.  Ultimately, I need to
rewrite ANTLR v3 in itself (it's written in 2.7.6 at the moment and
also needs StringTemplate 2.3b7 or later).

You should use v3 in conjunction with ANTLRWorks:

    http://www.antlr.org/works/index.html 

WARNING: You should only be playing with this release if you are an
experienced antlr user or language developer.  There is no
documentation and only a few examples plus the source to guide you.
Furthermore, this software will be in a state of flux including
changes in syntax and methods/classes until we get closer to a real
release.  There are many things that just plain don't work at the
moment.

See the new wiki FAQ:

    http://www.antlr.org/wiki/display/ANTLR3/ANTLR+v3+FAQ

I have made absolutely no effort yet to deal well with erroneous input
(well, semantic checking is pretty good, but bad syntax makes ANTLR
crash).  I will clean this up after I've rewritten v3 in v3.

Per the license in LICENSE.txt, this software is not guaranteed to
work and might even destroy all life on this planet:

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

----------------------------------------------------------------------

What is ANTLR?

ANTLR stands for (AN)other (T)ool for (L)anguage (R)ecognition and was
originally known as PCCTS.  ANTLR is a language tool that provides a
framework for constructing recognizers, compilers, and translators
from grammatical descriptions containing actions (this release only
allows Java actions).  See http://www.antlr.org/v3 for a list of
targets/contributors.

----------------------------------------------------------------------

How is ANTLR v3 different than ANTLR v2?

ANTLR v3 has a far superior parsing algorithm called LL(*) that
handles many more grammars than v2 does.  In practice, it means you
can throw almost any grammar at ANTLR that is non-left-recursive and
unambiguous (same input can be matched by multiple rules); the cost is
perhaps a tiny bit of backtracking, but with a DFA not a full parser.
You can manually set the max lookahead k as an option for any decision
though.  The LL(*) algorithm ramps up to use more lookahead when it
needs to and is much more efficient than normal LL backtracking. There
is support for syntactic predicate (full LL backtracking) when LL(*)
fails.

Lexers are much easier due to the LL(*) algorithm as well.  Previously
these two lexer rules would cause trouble because ANTLR couldn't
distinguish between them with finite lookahead to see the decimal
point:

INT : ('0'..'9')+ ;
FLOAT : INT '.' INT ;

The syntax is almost identical for features in common, but you should
note that labels are always '=' not ':'.  So do id=ID not id:ID.

You can do combined lexer/parser grammars again (ala PCCTS) both lexer
and parser rules are defined in the same file.  See the examples.
Really nice.  You can reference strings and characters in the grammar
and ANTLR will generate the lexer for you.

The attribute structure has been enhanced.  Rules may have multiple
return values, for example.  Further, there are dynamically scoped
attributes whereby a rule may define a value usable by any rule it
invokes directly or indirectly w/o having to pass a parameter all the
way down.

ANTLR v3 tree construction is far superior--it provides tree rewrite
rules where the right hand side is simply the tree grammar fragment
describing the tree you want to build:

formalArgs
	:	typename declarator (',' typename declarator )*
		-> ^(ARG typename declarator)+
	;

That builds tree sequences like:

^(ARG int v1) ^(ARG int v2)

ANTLR v3 also incorporates StringTemplate:

      http://www.stringtemplate.org

just like AST support.  It is useful for generating output.  For
example this rule creates a template called 'import' for each import
definition found in the input stream:

grammar Java;
options {
  output=template;
}
...
importDefinition
    :   'import' identifierStar SEMI
        -> import(name={$identifierStar.st},
                begin={$identifierStar.start},
                end={$identifierStar.stop})
    ;

The attributes are set via assignments in the argument list.  The
arguments are actions with arbitrary expressions in the target
language.  The .st label property is the result template from a rule
reference.  There is a nice shorthand in actions too:

    %foo(a={},b={},...) ctor
    %({name-expr})(a={},...) indirect template ctor reference
    %{string-expr} anonymous template from string expr
    %{expr}.y = z; template attribute y of StringTemplate-typed expr to z
    %x.y = z; set template attribute y of x (always set never get attr)
              to z [languages like python without ';' must still use the
              ';' which the code generator is free to remove during code gen]
              Same as '(x).setAttribute("y", z);'

For ANTLR v3 I decided to make the most common tasks easy by default
rather.  This means that some of the basic objects are heavier weight
than some speed demons would like, but they are free to pare it down
leaving most programmers the luxury of having it "just work."  For
example, to read in some input, tweak it, and write it back out
preserving whitespace, is easy in v3.

The ANTLR source code is much prettier.  You'll also note that the
run-time classes are conveniently encapsulated in the
org.antlr.runtime package.

----------------------------------------------------------------------

How do I install this damn thing?

Just untar and you'll get:

antlr-3.0ea10/README.txt (this file)
antlr-3.0ea10/LICENSE.txt
antlr-3.0ea10/src/org/antlr/...
antlr-3.0ea10/lib/stringtemplate-2.2b7.jar (3.0ea10 needs 2.2b7)
antlr-3.0ea10/lib/antlr-2.7.6.jar
antlr-3.0ea10/lib/antlr-3.0ea10.jar

Then you need to add all the jars in lib to your CLASSPATH.

----------------------------------------------------------------------

How do I use ANTLR v3?

[I am assuming you are only using the command-line (and not the
ANTLRWorks GUI)].

Running ANTLR with no parameters shows you:

ANTLR Parser Generator   Early Access Version 3.0ea10 (Mar 11, 2006) 1989-2006
usage: java org.antlr.Tool [args] file.g [file2.g file3.g ...]
  -o outputDir   specify output directory where all output is
generated
  -lib dir       specify location of token files
  -report        print out a report about the grammar(s) processed
  -print         print out the grammar without actions
  -debug         generate a parser that emits debugging events
  -profile       generate a parser that computes profiling information
  -nomemo        when backtracking don't generate memoization code
  -nfa           generate an NFA for each rule
  -dfa           generate a DFA for each decision point

For example, consider how to make the LL-star example from the examples 
tarball you can get at http://www.antlr.org/download/examples-v3.tar.gz

$ cd examples/java/LL-star
$ java org.antlr.Tool simplec.g
$ jikes *.java

For input:

char c;
int x;
void bar(int x);
int foo(int y, char d) {
  int i;
  for (i=0; i<3; i=i+1) {
    x=3;
    y=5;
  }
}

you will see output as follows:

$ java Main input
bar is a declaration
foo is a definition

What if I want to test my parser without generating code?  Easy.  Just
run ANTLR in interpreter mode.  It can't execute your actions, but it
can create a parse tree from your input to show you how it would be
matched.  Use the org.antlr.tool.Interp main class.  In the following,
I interpret simplec.g on t.c, which contains "int x;"

$ java org.antlr.tool.Interp simplec.g WS program t.c
( <grammar SimpleC>
  ( program
    ( declaration
      ( variable
        ( type [@0,0:2='int',<14>,1:0] )
        ( declarator [@2,4:4='x',<2>,1:4] )
        [@3,5:5=';',<5>,1:5]
      )
    )
  )
)

where I have formatted the output to make it more readable.  I have
told it to ignore all WS tokens.

----------------------------------------------------------------------

How do I rebuild ANTLR v3?

Make sure the following two jars are in your CLASSPATH

antlr-3.0ea10/lib/stringtemplate-2.2b7.jar
antlr-3.0ea10/lib/antlr-2.7.6.jar

then jump into antlr-3.0ea10/src directory and then type:

$ javac -d . org/antlr/Tool.java org/antlr/*/*.java org/antlr/*/*/*.java

Takes 9 seconds on my 1Ghz laptop or 4 seconds with jikes.  Later I'll
have a real build mechanism, though I must admit the one-liner appeals
to me.  I use Intellij so I never type anything actually to build.

-----------------------------------------------------------------------

CHANGES

3.0ea11 -

June 14, 2006

* -> ($rulelabel)? didn't generate proper code for ASTs.

* bug in code gen (did not compile)
a : ID -> ID
  | ID -> ID
  ;
Problem is repeated ref to ID from left side.  Juergen pointed this out.

* use of tokenVocab with missing file yielded exception

* (A|B)=> foo yielded an exception as (A|B) is a set not a block. Fixed.

* Didn't set ID1= and INT1= for this alt:
  | ^(ID INT+ {System.out.print(\"^(\"+$ID+\" \"+$INT+\")\");})

* Fixed so repeated dangling state errors only occur once like:
t.g:4:17: the decision cannot distinguish between alternative(s) 2,1 for at least one input sequence

* tracking of rule elements was on (making list defs at start of
  method) with templates instead of just with ASTs.  Turned off.

June 13, 2006

* ^(ROOT ID?) Didn't work; nor did any other nullable child list such as
  ^(ROOT ID* INT?).  Now, I check to see if child list is nullable using
  Grammar.LOOK() and, if so, I generate an "IF lookahead is DOWN" gate 
  around the child list so the whole thing is optional.

* Fixed a bug in LOOK that made it not look through nullable rules.

* Using AST suffixes or -> rewrite syntax now gives an error w/o a grammar
  output option.  Used to crash ;)

* References to EOF ended up with improper -1 refs instead of EOF in output.

* didn't warn of ambig ref to $expr in rewrite; fixed.
list 
     :	'[' expr 'for' type ID 'in' expr ']'
	-> comprehension(expr={$expr.st},type={},list={},i={})
	;

June 12, 2006

* EOF works in the parser as a token name.

* Rule b:(A B?)*; didn't display properly in AW due to the way ANTLR
  generated NFA.

* "scope x;" in a rule for unknown x gives no error.  Fixed.  Added unit test.

* Label type for refs to start/stop in tree parser and other parsers were
  not used.  Lots of casting.  Ick. Fixed.

* couldn't refer to $tokenlabel in isolation; but need so we can test if
  something was matched.  Fixed.

* Lots of little bugs fixed in $x.y, %... translation due to new
  action translator.

* Improperly tracking block nesting level; result was that you couldn't
  see $ID in action of rule "a : A+ | ID {Token t = $ID;} | C ;"

* a : ID ID {$ID.text;} ; did not get a warning about ambiguous $ID ref.

* No error was found on $COMMENT.text:

COMMENT
    :   '/*' (options {greedy=false;} : . )* '*/'
        {System.out.println("found method "+$COMMENT.text);}
    ;

  $enclosinglexerrule scope does not exist.  Use text or setText() here.

June 11, 2006

* Single return values are initialized now to default or to your spec.

* cleaned up input stream stuff.  Added ANTLRReaderStream, ANTLRInputStream
  and refactored.  You can specify encodings now on ANTLRFileStream (and
  ANTLRInputStream) now.

* You can set text local var now in a lexer rule and token gets that text.
  start/stop indexes are still set for the token.

* Changed lexer slightly.  Calling a nonfragment rule from a
  nonfragment rule does not set the overall token.

June 10, 2006

* Fixed bug where unnecessary escapes yield char==0 like '\{'.

* Fixed analysis bug.  This grammar didn't report a recursion warning:
x   : y X
    | y Y
    ;
y   : L y R
    | B
    ;
  The DFAState.equals() method was messed up.

* Added @synpredgate {...} action so you can tell ANTLR how to gate actions
  in/out during syntactic predicate evaluation.

* Fuzzy parsing should be more efficient.  It should backtrack over a rule
  and then rewind and do it again "with feeling" to exec actions.  It was
  actually doing it 3x not 2x.

June 9, 2006

* Gutted and rebuilt the action translator for $x.y, $x::y, ...
  Uses ANTLR v3 now for the first time inside v3 source. :)
  ActionTranslator.java

* Fixed a bug where referencing a return value on a rule didn't work
  because later a ref to that rule's predefined properties didn't
  properly force a return value struct to be built.  Added unit test.

June 6, 2006

* New DFA mechanisms.  Cyclic DFA are implemented as state tables,
  encoded via strings as java cannot handle large static arrays :(
  States with edges emanating that have predicates are specially
  treated.  A method is generated to do these states.  The DFA
  simulation routine uses the "special" array to figure out if the
  state is special.  See March 25, 2006 entry for description:
  http://www.antlr.org/blog/antlr3/codegen.tml.  analysis.DFA now has
  all the state tables generated for code gen.  CyclicCodeGenerator.java
  disappeared as it's unneeded code. :)

* Internal general clean up of the DFA.states vs uniqueStates thing.
  Fixed lookahead decisions no longer fill uniqueStates.  Waste of
  time.  Also noted that when adding sem pred edges, I didn't check
  for state reuse.  Fixed.

June 4, 2006

* When resolving ambig DFA states predicates, I did not add the new states
  to the list of unique DFA states.  No observable effect on output except
  that DFA state numbers were not always contiguous for predicated decisions.
  I needed this fix for new DFA tables.

3.0ea10 - June 2, 2006

June 2, 2006

* Improved grammar stats and added syntactic pred tracking.

June 1, 2006

* Due to a type mismatch, the DebugParser.recoverFromMismatchedToken()
  method was not called.  Debug events for mismatched token error
  notification were not sent to ANTLRWorks probably

* Added getBacktrackingLevel() for any recognizer; needed for profiler.

* Only writes profiling data for antlr grammar analysis with -profile set

* Major update and bug fix to (runtime) Profiler.

May 27, 2006

* Added Lexer.skip() to force lexer to ignore current token and look for
  another; no token is created for current rule and is not passed on to
  parser (or other consumer of the lexer).

* Parsers are much faster now.  I removed use of java.util.Stack for pushing
  follow sets and use a hardcoded array stack instead.  Dropped from
  5900ms to 3900ms for parse+lex time parsing entire java 1.4.2 source.  Lex
  time alone was about 1500ms.  Just looking at parse time, we get about 2x
  speed improvement. :)

May 26, 2006

* Fixed NFA construction so it generates NFA for (A*)* such that ANTLRWorks 
  can display it properly.

May 25, 2006

* added abort method to Grammar so AW can terminate the conversion if it's
  taking too long.

May 24, 2006
 
* added method to get left recursive rules from grammar without doing full
  grammar analysis.

* analysis, code gen not attempted if serious error (like
  left-recursion or missing rule definition) occurred while reading
  the grammar in and defining symbols.

* added amazing optimization; reduces analysis time by 90% for java
  grammar; simple IF statement addition!

3.0ea9 - May 20, 2006

* added global k value for grammar to limit lookahead for all decisions unless
overridden in a particular decision.

* added failsafe so that any decision taking longer than 2 seconds to create
the DFA will fall back on k=1.  Use -ImaxtimeforDFA n (in ms) to set the time.

* added an option (turned off for now) to use multiple threads to
perform grammar analysis.  Not much help on a 2-CPU computer as
garbage collection seems to peg the 2nd CPU already. :( Gotta wait for
a 4 CPU box ;)

* switched from #src to // $ANTLR src directive.

* CommonTokenStream.getTokens() looked past end of buffer sometimes. fixed.

* unicode literals didn't really work in DOT output and generated code. fixed.

* fixed the unit test rig so it compiles nicely with Java 1.5

* Added ant build.xml file (reads build.properties file)

* predicates sometimes failed to compile/eval properly due to missing (...)
  in IF expressions.  Forced (..)

* (...)? with only one alt were not optimized.  Was:

        // t.g:4:7: ( B )?
        int alt1=2;
        int LA1_0 = input.LA(1);
        if ( LA1_0==B ) {
            alt1=1;
        }
        else if ( LA1_0==-1 ) {
            alt1=2;
        }
        else {
            NoViableAltException nvae =
                new NoViableAltException("4:7: ( B )?", 1, 0, input);
            throw nvae;
        }

is now:

        // t.g:4:7: ( B )?
        int alt1=2;
        int LA1_0 = input.LA(1);
        if ( LA1_0==B ) {
            alt1=1;
        }

  Smaller, faster and more readable.

* Allow manual init of return values now:
  functionHeader returns [int x=3*4, char (*f)()=null] : ... ;

* Added optimization for DFAs that fixed a codegen bug with rules in lexer:
   EQ			 : '=' ;
   ASSIGNOP		 : '=' | '+=' ;
  EQ is a subset of other rule.  It did not given an error which is
  correct, but generated bad code.

* ANTLR was sending column not char position to ANTLRWorks.

* Bug fix: location 0, 0 emitted for synpreds and empty alts.

* debugging event handshake how sends grammar file name.  Added getGrammarFileName() to recognizers.  Java.stg generates it:

    public String getGrammarFileName() { return "<fileName>"; }

* tree parsers can do arbitrary lookahead now including backtracking.  I
  updated CommonTreeNodeStream.

* added events for debugging tree parsers:

	/** Input for a tree parser is an AST, but we know nothing for sure
	 *  about a node except its type and text (obtained from the adaptor).
	 *  This is the analog of the consumeToken method.  Again, the ID is
	 *  the hashCode usually of the node so it only works if hashCode is
	 *  not implemented.
	 */
	public void consumeNode(int ID, String text, int type);

	/** The tree parser looked ahead */
	public void LT(int i, int ID, String text, int type);

	/** The tree parser has popped back up from the child list to the
	 *  root node.
	 */
	public void goUp();

	/** The tree parser has descended to the first child of a the current
	 *  root node.
	 */
	public void goDown();

* Added DebugTreeNodeStream and DebugTreeParser classes

* Added ctor because the debug tree node stream will need to ask quesitons about nodes and since  nodes are just Object, it needs an adaptor to decode the nodes and get text/type info for the debugger.

public CommonTreeNodeStream(TreeAdaptor adaptor, Tree tree);

* added getter to TreeNodeStream:
	public TreeAdaptor getTreeAdaptor();

* Implemented getText/getType in CommonTreeAdaptor.

* Added TraceDebugEventListener that can dump all events to stdout.

* I broke down and make Tree implement getText

* tree rewrites now gen location debug events.

* added AST debug events to listener; added blank listener for convenience

* updated debug events to send begin/end backtrack events for debugging

* with a : (b->b) ('+' b -> ^(PLUS $a b))* ; you get b[0] each time as
  there is no loop in rewrite rule itself.  Need to know context that
  the -> is inside the rule and hence b means last value of b not all
  values.

* Bug in TokenRewriteStream; ops at indexes < start index blocked proper op.

* Actions in ST rewrites "-> ({$op})()" were not translated

* Added new action name:

@rulecatch {
catch (RecognitionException re) {
    reportError(re);
    recover(input,re);
}
catch (Throwable t) {
    System.err.println(t);
}
}
Overrides rule catch stuff.

* Isolated $ refs caused exception

3.0ea8 - March 11, 2006

* added @finally {...} action like @init for rules.  Executes in
  finally block (java target) after all other stuff like rule memoization.
  No code changes needs; ST just refs a new action:
      <ruleDescriptor.actions.finally>

* hideous bug fixed: PLUS='+' didn't result in '+' rule in lexer

* TokenRewriteStream didn't do toString() right when no rewrites had been done.

* lexer errors in interpreter were not printed properly

* bitsets are dumped in hex not decimal now for FOLLOW sets

* /* epsilon */ is not printed now when printing out grammars with empty alts

* Fixed another bug in tree rewrite stuff where it was checking that elements
  had at least one element.  Strange...commented out for now to see if I can remember what's up.

* Tree rewrites had problems when you didn't have x+=FOO variables.  Rules
  like this work now:

  a : (x=ID)? y=ID -> ($x $y)?;

* filter=true for lexers turns on k=1 and backtracking for every token
  alternative.  Put the rules in priority order.

* added getLine() etc... to Tree to support better error reporting for
  trees.  Added MismatchedTreeNodeException.

* $templates::foo() is gone.  added % as special template symbol.
  %foo(a={},b={},...) ctor (even shorter than $templates::foo(...))
  %({name-expr})(a={},...) indirect template ctor reference

  The above are parsed by antlr.g and translated by codegen.g
  The following are parsed manually here:

  %{string-expr} anonymous template from string expr
  %{expr}.y = z; template attribute y of StringTemplate-typed expr to z
  %x.y = z; set template attribute y of x (always set never get attr)
            to z [languages like python without ';' must still use the
            ';' which the code generator is free to remove during code gen]

* -> ({expr})(a={},...) notation for indirect template rewrite.
  expr is the name of the template.

* $x[i]::y and $x[-i]::y notation for accesssing absolute scope stack
  indexes and relative negative scopes.  $x[-1]::y is the y attribute
  of the previous scope (stack top - 1).

* filter=true mode for lexers; can do this now...upon mismatch, just
  consumes a char and tries again:
lexer grammar FuzzyJava;
options {filter=true;}

FIELD
    :   TYPE WS? name=ID WS? (';'|'=')
        {System.out.println("found var "+$name.text);}
    ;

* refactored char streams so ANTLRFileStream is now a subclass of
  ANTLRStringStream.

* char streams for lexer now allowed nested backtracking in lexer.

* added TokenLabelType for lexer/parser for all token labels

* line numbers for error messages were not updated properly in antlr.g
  for strings, char literals and <<...>>

* init action in lexer rules was before the type,start,line,... decls.

* Tree grammars can now specify output; I've only tested output=templat
  though.

* You can reference EOF now in the parser and lexer.  It's just token type
  or char value -1.

* Bug fix: $ID refs in the *lexer* were all messed up.  Cleaned up the
  set of properties available...

* Bug fix: .st not found in rule ref when rule has scope:
field
scope {
	StringTemplate funcDef;
}
    :   ...
	{$field::funcDef = $field.st;}
    ;
it gets field_stack.st instead

* return in backtracking must return retval or null if return value.

* $property within a rule now works like $text, $st, ...

* AST/Template Rewrites were not gated by backtracking==0 so they
  executed even when guessing.  Auto AST construction is now gated also.

* CommonTokenStream was somehow returning tokens not text in toString()

* added useful methods to runtime.BitSet and also to CommonToken so you can
  update the text.  Added nice Token stream method:

  /** Given a start and stop index, return a List of all tokens in
   *  the token type BitSet.  Return null if no tokens were found.  This
   *  method looks at both on and off channel tokens.
   */
  public List getTokens(int start, int stop, BitSet types);

* literals are now passed in the .tokens files so you can ref them in
  tree parses, for example.

* added basic exception handling; no labels, just general catches:

a : {;}A | B ;
        exception
                catch[RecognitionException re] {
                        System.out.println("recog error");
                }
                catch[Exception e] {
                        System.out.println("error");
                }

* Added method to TokenStream:
  public String toString(Token start, Token stop);

* antlr generates #src lines in lexer grammars generated from combined grammars
  so error messages refer to original file.

* lexers generated from combined grammars now use originally formatting.

* predicates have $x.y stuff translated now.  Warning: predicates might be
  hoisted out of context.

* return values in return val structs are now public.

* output=template with return values on rules was broken.  I assume return values with ASTs was broken too.  Fixed.

3.0ea7 - December 14, 2005

* Added -print option to print out grammar w/o actions

* Renamed BaseParser to be BaseRecognizer and even made Lexer derive from
  this; nice as it now shares backtracking support code.

* Added syntactic predicates (...)=>.  See December 4, 2005 entry:

  http://www.antlr.org/blog/antlr3/lookahead.tml

  Note that we have a new option for turning off rule memoization during
  backtracking:

  -nomemo        when backtracking don't generate memoization code

* Predicates are now tested in order that you specify the alts.  If you
  leave the last alt "naked" (w/o pred), it will assume a true pred rather
  than union of other preds.

* Added gated predicates "{p}?=>" that literally turn off a production whereas
disambiguating predicates are only hoisted into the predictor when syntax alone
is not sufficient to uniquely predict alternatives.

A : {p}?  => "a" ;
B : {!p}? => ("a"|"b")+ ;

* bug fixed related to predicates in predictor
lexer grammar w;
A : {p}? "a" ;
B : {!p}? ("a"|"b")+ ;
DFA is correct.  A state splits for input "a" on the pred.
Generated code though was hosed.  No pred tests in prediction code!
I added testLexerPreds() and others in TestSemanticPredicateEvaluation.java

* added execAction template in case we want to do something in front of
  each action execution or something.

* left-recursive cycles from rules w/o decisions were not detected.

* undefined lexer rules were not announced! fixed.

* unreachable messages for Tokens rule now indicate rule name not alt. E.g.,

  Ruby.lexer.g:24:1: The following token definitions are unreachable: IVAR

* nondeterminism warnings improved for Tokens rule:

Ruby.lexer.g:10:1: Multiple token rules can match input such as ""0".."9"": INT, FLOAT
As a result, tokens(s) FLOAT were disabled for that input


* DOT diagrams didn't show escaped char properly.

* Char/string literals are now all 'abc' not "abc".

* action syntax changed "@scope::actionname {action}" where scope defaults
  to "parser" if parser grammar or combined grammar, "lexer" if lexer grammar,
  and "treeparser" if tree grammar.  The code generation targets decide
  what scopes are available.  Each "scope" yields a hashtable for use in
  the output templates.  The scopes full of actions are sent to all output
  file templates (currently headerFile and outputFile) as attribute actions.
  Then you can reference <actions.scope> to get the map of actions associated
  with scope and <actions.parser.header> to get the parser's header action
  for example.  This should be very flexible.  The target should only have
  to define which scopes are valid, but the action names should be variable
  so we don't have to recompile ANTLR to add actions to code gen templates.

  grammar T;
  options {language=Java;}
  @header { package foo; }
  @parser::stuff { int i; } // names within scope not checked; target dependent
  @members { int i; }
  @lexer::header {head}
  @lexer::members { int j; }
  @headerfile::blort {...} // error: this target doesn't have headerfile
  @treeparser::members {...} // error: this is not a tree parser
  a
  @init {int i;}
    : ID
    ;
  ID : 'a'..'z';

  For now, the Java target uses members and header as a valid name.  Within a
  rule, the init action name is valid.

* changed $dynamicscope.value to $dynamicscope::value even if value is defined
  in same rule such as $function::name where rule function defines name.

* $dynamicscope gets you the stack

* rule scopes go like this now:

  rule
  scope {...}
  scope slist,Symbols;
  	: ...
	;

* Created RuleReturnScope as a generic rule return value.  Makes it easier
  to do this:
    RuleReturnScope r = parser.program();
    System.out.println(r.getTemplate().toString());

* $template, $tree, $start, etc...

* $r.x in current rule.  $r is ignored as fully-qualified name. $r.start works too

* added warning about $r referring to both return value of rule and dynamic scope of rule

* integrated StringTemplate in a very simple manner

Syntax:
-> template(arglist) "..."
-> template(arglist) <<...>>
-> namedTemplate(arglist)
-> {free expression}
-> // empty

Predicate syntax:
a : A B -> {p1}? foo(a={$A.text})
        -> {p2}? foo(a={$B.text})
        -> // return nothing

An arg list is just a list of template attribute assignments to actions in curlies.

There is a setTemplateLib() method for you to use with named template rewrites.

Use a new option:

grammar t;
options {output=template;}
...

This all should work for tree grammars too, but I'm still testing.

* fixed bugs where strings were improperly escaped in exceptions, comments, etc..  For example, newlines came out as newlines not the escaped version

3.0ea6 - November 13, 2005

* turned off -debug/-profile, which was on by default

* completely refactored the output templates; added some missing templates.

* dramatically improved infinite recursion error messages (actually
  left-recursion never even was printed out before).

* wasn't printing dangling state messages when it reanalyzes with k=1.

* fixed a nasty bug in the analysis engine dealing with infinite recursion.
  Spent all day thinking about it and cleaned up the code dramatically.
  Bug fixed and software is more powerful and I understand it better! :)

* improved verbose DFA nodes; organized by alt

* got much better random phrase generation.  For example:

 $ java org.antlr.tool.RandomPhrase simple.g program
 int Ktcdn ';' method wh '(' ')' '{' return 5 ';' '}'

* empty rules like "a : ;" generated code that didn't compile due to
  try/catch for RecognitionException.  Generated code couldn't possibly
  throw that exception.

* when printing out a grammar, such as in comments in generated code,
  ANTLR didn't print ast suffix stuff back out for literals.

* This never exited loop:
  DATA : (options {greedy=false;}: .* '\n' )* '\n' '.' ;
  and now it works due to new default nongreedy .*  Also this works:
  DATA : (options {greedy=false;}: .* '\n' )* '.' ;

* Dot star ".*" syntax didn't work; in lexer it is nongreedy by
  default.  In parser it is on greedy but also k=1 by default.  Added
  unit tests.  Added blog entry to describe.

* ~T where T is the only token yielded an empty set but no error

* Used to generate unreachable message here:

  parser grammar t;
  a : ID a
    | ID
    ;

  z.g:3:11: The following alternatives are unreachable: 2

  In fact it should really be an error; now it generates:

  no start rule in grammar t (no rule can obviously be followed by EOF)

  Per next change item, ANTLR cannot know that EOF follows rule 'a'.

* added error message indicating that ANTLR can't figure out what your
  start rule is.  Required to properly generate code in some cases.

* validating semantic predicates now work (if they are false, they
  throw a new FailedPredicateException

* two hideous bug fixes in the IntervalSet, which made analysis go wrong
  in a few cases.  Thanks to Oliver Zeigermann for finding lots of bugs
  and making suggested fixes (including the next two items)!

* cyclic DFAs are now nonstatic and hence can access instance variables

* labels are now allowed on lexical elements (in the lexer)

* added some internal debugging options

* ~'a'* and ~('a')* were not working properly; refactored antlr.g grammar

3.0ea5 - July 5, 2005

* Using '\n' in a parser grammar resulted in a nonescaped version of '\n' in the token names table making compilation fail.  I fixed this by reorganizing/cleaning up portion of ANTLR that deals with literals.  See comment org.antlr.codegen.Target.

* Target.getMaxCharValue() did not use the appropriate max value constant.

* ALLCHAR was a constant when it should use the Target max value def.  set complement for wildcard also didn't use the Target def.  Generally cleaned up the max char value stuff.

* Code gen didn't deal with ASTLabelType properly...I think even the 3.0ea7 example tree parser was broken! :(

* Added a few more unit tests dealing with escaped literals

3.0ea4 - June 29, 2005

* tree parsers work; added CommonTreeNodeStream.  See simplecTreeParser
  example in examples-v3 tarball.

* added superClass and ASTLabelType options

* refactored Parser to have a BaseParser and added TreeParser

* bug fix: actions being dumped in description strings; compile errors
  resulted

3.0ea3 - June 23, 2005

Enhancements

* Automatic tree construction operators are in: ! ^ ^^

* Tree construction rewrite rules are in
	-> {pred1}? rewrite1
	-> {pred2}? rewrite2
	...
	-> rewriteN

  The rewrite rules may be elements like ID, expr, $label, {node expr}
  and trees ^( <root> <children> ).  You have have (...)?, (...)*, (...)+
  subrules as well.

  You may have rewrites in subrules not just at outer level of rule, but
  any -> rewrite forces auto AST construction off for that alternative
  of that rule.

  To avoid cycles, copy semantics are used:

  r : INT -> INT INT ;

  means make two new nodes from the same INT token.

  Repeated references to a rule element implies a copy for at least one
  tree:

  a : atom -> ^(atom atom) ; // NOT CYCLE! (dup atom tree)

* $ruleLabel.tree refers to tree created by matching the labeled element.

* A description of the blocks/alts is generated as a comment in output code

* A timestamp / signature is put at top of each generated code file

3.0ea2 - June 12, 2005

Bug fixes

* Some error messages were missing the stackTrace parameter

* Removed the file locking mechanism as it's not cross platform

* Some absolute vs relative path name problems with writing output
  files.  Rules are now more concrete.  -o option takes precedence
  // -o /tmp /var/lib/t.g => /tmp/T.java
  // -o subdir/output /usr/lib/t.g => subdir/output/T.java
  // -o . /usr/lib/t.g => ./T.java
  // -o /tmp subdir/t.g => /tmp/subdir/t.g
  // If they didn't specify a -o dir so just write to location
  // where grammar is, absolute or relative

* does error checking on unknown option names now

* Using just language code not locale name for error message file.  I.e.,
  the default (and for any English speaking locale) is en.stg not en_US.stg
  anymore.

* The error manager now asks the Tool to panic rather than simply doing
  a System.exit().

* Lots of refactoring concerning grammar, rule, subrule options.  Now
  detects invalid options.

New features


3.0ea1 - June 1, 2005

Initial early access release 
