Early Access ANTLR v3
ANTLR 3.1
???, 2007

Terence Parr, parrt at cs usfca edu
ANTLR project lead and supreme dictator for life
University of San Francisco

INTRODUCTION 

Welcome to ANTLR v3!  I've been working on this for nearly 4 years and it's
finally ready!  I have lots of features to add later, but this will be
the first set.

You should use v3 in conjunction with ANTLRWorks:

    http://www.antlr.org/works/index.html 

The book will also help you a great deal (printed May 15, 2007); you
can also buy the PDF:

http://www.pragmaticprogrammer.com/titles/tpantlr/index.html

See the getting started document:

http://www.antlr.org/wiki/display/ANTLR3/FAQ+-+Getting+Started

You also have the examples plus the source to guide you.

See the new wiki FAQ:

    http://www.antlr.org/wiki/display/ANTLR3/ANTLR+v3+FAQ

and general doc root:

    http://www.antlr.org/wiki/display/ANTLR3/ANTLR+3+Wiki+Home

Please help add/update FAQ entries.

If all else fails, you can buy support or ask the antlr-interest list:

    http://www.antlr.org/support.html

I have made very little effort at this point to deal well with
erroneous input (e.g., bad syntax might make ANTLR crash).  I will clean
this up after I've rewritten v3 in v3.  v3 is written in v2 at the moment.

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

EXAMPLES

ANTLR v3 sample grammars (currently for C, C#, Java targets):

    http://www.antlr.org/download/examples-v3.tar.gz

contains the following examples: LL-star, cminus, dynamic-scope,
fuzzy, hoistedPredicates, island-grammar, java, python, scopes,
simplecTreeParser, treeparser, tweak, xmlLexer.

Also check out Mantra Programming Language for a prototype (work in
progress) using v3:

    http://www.linguamantra.org/

----------------------------------------------------------------------

What is ANTLR?

ANTLR stands for (AN)other (T)ool for (L)anguage (R)ecognition and was
originally known as PCCTS.  ANTLR is a language tool that provides a
framework for constructing recognizers, compilers, and translators
from grammatical descriptions containing actions.  Target language list:

http://www.antlr.org/wiki/display/ANTLR3/Code+Generation+Targets

----------------------------------------------------------------------

How is ANTLR v3 different than ANTLR v2?

See "What is the difference between ANTLR v2 and v3?"

    http://www.antlr.org/wiki/pages/viewpage.action?pageId=719

See migration guide:

    http://www.antlr.org/wiki/display/ANTLR3/Migrating+from+ANTLR+2+to+ANTLR+3

----------------------------------------------------------------------

How do I install this damn thing?

Just untar and you'll get:

antlr-3.1/README.txt (this file)
antlr-3.1/LICENSE.txt
antlr-3.1/src/org/antlr/...
antlr-3.1/lib/stringtemplate-3.0.jar (3.1 needs 3.0)
antlr-3.1/lib/antlr-2.7.7.jar
antlr-3.1/lib/antlr-3.1.jar

Then you need to add all the jars in lib to your CLASSPATH.

Please see the FAQ

http://www.antlr.org/wiki/display/ANTLR3/ANTLR+v3+FAQ
