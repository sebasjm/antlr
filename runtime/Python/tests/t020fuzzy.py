import os
import sys
from cStringIO import StringIO
import difflib
import antlr3
from t020fuzzyLexer import t020fuzzyLexer as Lexer

inputPath = os.path.splitext(__file__)[0] + '.input'
stream = antlr3.StringStream(open(inputPath).read())
lexer = Lexer(stream)

while True:
    token = lexer.nextToken()
    if token.type == antlr3.EOF:
        break


output = lexer.output.getvalue()

outputPath = os.path.splitext(__file__)[0] + '.output'
testOutput = open(outputPath).read()

success = (output == testOutput)
if not success:
    d = difflib.Differ()
    r = d.compare(output.splitlines(1), testOutput.splitlines(1))
    for l in r:
        sys.stderr.write(l.encode('ascii', 'backslashreplace'))
    
sys.exit(not success)
