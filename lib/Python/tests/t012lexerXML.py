import os
import sys
from cStringIO import StringIO
import difflib

import antlr3
from t012lexerXMLLexer import t012lexerXMLLexer as Lexer
from t012lexerXMLLexer import EOF

class TestLexer(Lexer):
    def __init__(self, *args, **kwargs):
        Lexer.__init__(self, *args, **kwargs)

        self.outbuf = StringIO()
        
    def output(self, line):
        self.outbuf.write(line.encode('utf-8') + "\n")
        
inputPath = os.path.splitext(__file__)[0] + '.input'
stream = antlr3.StringStream(unicode(open(inputPath).read(), 'utf-8'))
lexer = TestLexer(stream)

while True:
    token = lexer.nextToken()
    if token.type == EOF:
        break


output = unicode(lexer.outbuf.getvalue(), 'utf-8')

outputPath = os.path.splitext(__file__)[0] + '.output'
testOutput = unicode(open(outputPath).read(), 'utf-8')

success = (output == testOutput)
if not success:
    d = difflib.Differ()
    r = d.compare(output.splitlines(1), testOutput.splitlines(1))
    for l in r:
        sys.stderr.write(l.encode('ascii', 'backslashreplace'))
    
    sys.exit(1)


# malformed input
input = """\
<?xml version='1.0'?>
<document d>
</document>
"""

stream = antlr3.StringStream(input)
lexer = TestLexer(stream)

try:
    while True:
        token = lexer.nextToken()
        if token.type == EOF:
            break

    raise AssertionError

except antlr3.NoViableAltException, exc:
    assert exc.unexpectedType == '>', repr(exc.unexpectedType)
    assert exc.charPositionInLine == 11, repr(exc.charPositionInLine)
    assert exc.line == 2, repr(exc.line)


input = """\
<?tml version='1.0'?>
<document>
</document>
"""

stream = antlr3.StringStream(input)
lexer = TestLexer(stream)

try:
    while True:
        token = lexer.nextToken()
        if token.type == EOF:
            break

    raise AssertionError

except antlr3.MismatchedSetException, exc:
    assert exc.unexpectedType == 't', repr(exc.unexpectedType)
    assert exc.charPositionInLine == 2, repr(exc.charPositionInLine)
    assert exc.line == 1, repr(exc.line)
    

input = """\
<?xml version='1.0'?>
<docu ment attr="foo">
</document>
"""

stream = antlr3.StringStream(input)
lexer = TestLexer(stream)

try:
    while True:
        token = lexer.nextToken()
        if token.type == EOF:
            break

    raise AssertionError

except antlr3.NoViableAltException, exc:
    assert exc.unexpectedType == 'a', repr(exc.unexpectedType)
    assert exc.charPositionInLine == 11, repr(exc.charPositionInLine)
    assert exc.line == 2, repr(exc.line)
    

## # run an infinite loop with randomly mangled input
## while True:
##     print "ping"

##     input = """\
## <?xml version='1.0'?>
## <!DOCTYPE component [
## <!ELEMENT component (PCDATA|sub)*>
## <!ATTLIST component
##           attr CDATA #IMPLIED
##           attr2 CDATA #IMPLIED
## >
## <!ELMENT sub EMPTY>

## ]>
## <component attr="val'ue" attr2='val"ue'>
## <!-- This is a comment -->
## Text
## <![CDATA[huhu]]>
## &amp;
## &lt;
## <?xtal cursor='11'?>
## <sub/>
## <sub></sub>
## </component>
## """

##     import random
##     input = list(input) # make it mutable
##     for _ in range(3):
##         p1 = random.randrange(len(input))
##         p2 = random.randrange(len(input))

##         c1 = input[p1]
##         input[p1] = input[p2]
##         input[p2] = c1
##     input = ''.join(input) # back to string
        
##     stream = antlr3.StringStream(input)
##     lexer = TestLexer(stream)

##     try:
##         while True:
##             token = lexer.nextToken()
##             if token.type == EOF:
##                 break

##     except antlr3.RecognitionException, exc:
##         print exc
##         for l in input.splitlines()[0:exc.line]:
##             print l
##         print ' '*exc.charPositionInLine + '^'

##     except BaseException, exc:
##         print '\n'.join(['%02d: %s' % (idx+1, l) for idx, l in enumerate(input.splitlines())])
##         print "%s at %d:%d" % (exc, stream.line, stream.charPositionInLine)
##         print
        
##         raise
    
