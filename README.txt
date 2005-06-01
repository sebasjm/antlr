Early Access ANTLR v3
ANTLR 3.0ea1
May 31, 2005

Terence Parr
ANTLR Project lead
University of San Francisco

INTRODUCTION 

Welcome to ANTLR v3!  I've been working on this for 2 straight years
and, while it is not ready for a full release, it may prove useful to
some of you.  The main functionality missing is tree construction and
walking support as well.  Finally, I need to rewrite ANTLR v3 in
itself (it's written in 2.7.5 at the moment) before I can do my first
alpha release.

Warning: You should only be playing with this release if you are an
experienced antlr user or language developer.  There is no
documentation and only a few examples plus the source to guide you.

Per the license in LICENSE.txt, this software is not guaranteed to
destroy all life on this planet:

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

The source code is much prettier.  You'll also note that the run-time
classes are conveniently encapsulated in the org.antlr.runtime
package.

----------------------------------------------------------------------

How do I install this damn thing?

Just untar and you'll get:

antlr-3.0ea1
antlr-3.0ea1/src/org/antlr/...
antlr-3.0ea1/lib/stringtemplate-2.1.jar
antlr-3.0ea1/lib/antlr-2.7.5.jar
antlr-3.0ea1/lib/antlr-3.0ea1.jar
antlr-3.0ea1/examples/java/

Then you need to add all the jars in lib to your CLASSPATH.

----------------------------------------------------------------------

How do I use ANTLR v3?

[I am assuming you are only using the command-line (and not the
ANTLRWorks GUI)].

Running ANTLR with no parameters shows you:

ANTLR Parser Generator   Early Access Version 3.0ea1   1989-2005
usage: java org.antlr.Tool [args] file.g [file2.g file3.g ...]
  -o outputDir   specify output directory where all output is generated
  -lib dir       specify location of token files
  -report        print out a report about the grammar(s) processed
  -debug         generate a parser that emits debugging events
  -profile       enerate a parser that computes profiling information
  -nfa           generate an NFA for each rule
  -dfa           generate a DFA for each decision point

For example, consider how to make the LL-star example that comes with
this distribution.

$ cd examples/java/LL-star
$ java org.antlr.Tool simplec.g
$ jikes *.java
$ java Main input


run
	how to on grammar
	options
	Interp

Differences

No error checking

examples


