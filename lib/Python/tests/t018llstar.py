import os
import sys
from cStringIO import StringIO
import difflib

import antlr3
from t018llstarLexer import t018llstarLexer as Lexer
from t018llstarParser import t018llstarParser as Parser

class TestParser(Parser):
    def __init__(self, *args, **kwargs):
        Parser.__init__(self, *args, **kwargs)

        self.output = StringIO()

        
inputPath = os.path.splitext(__file__)[0] + '.input'
cStream = antlr3.StringStream(open(inputPath).read())
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TestParser(tStream)
parser.program()


output = parser.output.getvalue()

outputPath = os.path.splitext(__file__)[0] + '.output'
testOutput = open(outputPath).read()

success = (output == testOutput)
if not success:
    d = difflib.Differ()
    r = d.compare(output.splitlines(1), testOutput.splitlines(1))
    for l in r:
        sys.stderr.write(l.encode('ascii', 'backslashreplace'))
    
sys.exit(not success)
