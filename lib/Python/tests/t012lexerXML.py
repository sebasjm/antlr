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
    
sys.exit(not success)

