Early Access ANTLR v3
ANTLR 3.0ea1
June 1, 2005

Terence Parr, parrt at cs usfca edu
ANTLR Project lead
University of San Francisco

INTRODUCTION 

Welcome to ANTLR v3!  I've been working on this for 2 straight years
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

antlr-3.0ea1/README.txt (this file)
antlr-3.0ea1/LICENSE.txt
antlr-3.0ea1/src/org/antlr/...
antlr-3.0ea1/lib/stringtemplate-2.2b1.jar
antlr-3.0ea1/lib/antlr-2.7.5.jar
antlr-3.0ea1/lib/antlr-3.0ea1.jar

Then you need to add all the jars in lib to your CLASSPATH.

----------------------------------------------------------------------

How do I use ANTLR v3?

[I am assuming you are only using the command-line (and not the
ANTLRWorks GUI)].

Running ANTLR with no parameters shows you:

ANTLR Parser Generator   Early Access Version 3.0ea1 (June 1, 2005)  1989-2005
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

antlr-3.0ea1/lib/stringtemplate-2.2b1.jar
antlr-3.0ea1/lib/antlr-2.7.5.jar

then jump into antlr-3.0ea1/src directory and then type:

$ javac -d . org/antlr/Tool.java org/antlr/*/*.java org/antlr/*/*/*.java

Takes 9 seconds on my 1Ghz laptop or 4 seconds with jikes.  Later I'll
have a real build mechanism, though I must admit the one-liner appeals
to me.  I use Intellij so I never type anything actually to build.

-----------------------------------------------------------------------

CHANGES

3.0ea2

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

New features


3.0ea1 - June 1, 2005

Initial early access release 
