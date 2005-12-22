Early Access ANTLR v3
ANTLR 3.0ea7
December 14, 2005

Terence Parr, parrt at cs usfca edu
ANTLR project lead and supreme dictator
University of San Francisco

INTRODUCTION 

Welcome to ANTLR v3!  I've been working on this for 2.5 straight years
and, while it is not ready for a full release, it may prove useful to
some of you.  The main functionality missing is tree construction and
tree walking support.  Finally, I need to rewrite ANTLR v3 in itself
(it's written in 2.7.5 at the moment) before I can do my first alpha
release.

WARNING: You should only be playing with this release if you are an
experienced antlr user or language developer.  There is no
documentation and only a few examples plus the source to guide you.
Furthermore, this software will be in a state of flux including
changes in syntax and methods/classes until we get closer to a real
release.  There are many things that just plain don't work at the
moment.

[3.0ea7 has the key components now: lexer, parser, tree construction,
 and tree parser]

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
allows Java actions).

----------------------------------------------------------------------

How is ANTLR v3 different than ANTLR v2?

ANTLR v3 has a far superior parsing algorithm called LL(*) that
handles many more grammars than v2 does.  In practice, it means you
can throw almost any grammar at ANTLR that is non-left-recursive and
umambiguous (same input can be matched by multiple rules); the cost is
perhaps a tiny bit of backtracking.  There is currently no syntactic
predicate (full LL backtracking) support because the LL(*) algorithm
ramps up to use more lookahead when it needs to and is much more
efficient than normal LL backtracking.

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

antlr-3.0ea7/README.txt (this file)
antlr-3.0ea7/LICENSE.txt
antlr-3.0ea7/src/org/antlr/...
antlr-3.0ea7/lib/stringtemplate-2.2b4.jar (3.0ea7 needs 2.2b4)
antlr-3.0ea7/lib/antlr-2.7.5.jar
antlr-3.0ea7/lib/antlr-3.0ea7.jar

Then you need to add all the jars in lib to your CLASSPATH.

----------------------------------------------------------------------

How do I use ANTLR v3?

[I am assuming you are only using the command-line (and not the
ANTLRWorks GUI)].

Running ANTLR with no parameters shows you:

ANTLR Parser Generator   Early Access Version 3.0ea7 (June 29, 2005)  1989-2005
usage: java org.antlr.Tool [args] file.g [file2.g file3.g ...]
  -o outputDir   specify output directory where all output is generated
  -lib dir       specify location of token files
  -report        print out a report about the grammar(s) processed
  -debug         generate a parser that emits debugging events
  -profile       enerate a parser that computes profiling information
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

antlr-3.0ea7/lib/stringtemplate-2.2b4.jar
antlr-3.0ea7/lib/antlr-2.7.5.jar

then jump into antlr-3.0ea7/src directory and then type:

$ javac -d . org/antlr/Tool.java org/antlr/*/*.java org/antlr/*/*/*.java

Takes 9 seconds on my 1Ghz laptop or 4 seconds with jikes.  Later I'll
have a real build mechanism, though I must admit the one-liner appeals
to me.  I use Intellij so I never type anything actually to build.

-----------------------------------------------------------------------

CHANGES

3.0ea8 - ???

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
