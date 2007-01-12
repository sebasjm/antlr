import antlr3
from t013parserLexer import t013parserLexer as Lexer
from t013parserParser import t013parserParser as Parser

class TestParser(Parser):
    def __init__(self, *args, **kwargs):
        Parser.__init__(self, *args, **kwargs)

        self.identifiers = []


    def foundIdentifier(self, name):
        self.identifiers.append(name)

        
cStream = antlr3.StringStream('foobar')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TestParser(tStream)
parser.document()

assert parser.identifiers == ['foobar']


# malformed input
cStream = antlr3.StringStream('')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TestParser(tStream)

try:
    parser.document()
    raise AssertionError

except antlr3.NoViableAltException, exc:
    assert exc.unexpectedType == '-', repr(exc.unexpectedType)
    assert exc.charPositionInLine == 1, repr(exc.charPositionInLine)
    assert exc.line == 1, repr(exc.line)
