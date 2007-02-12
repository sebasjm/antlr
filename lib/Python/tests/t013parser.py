import antlr3
from t013parserLexer import t013parserLexer as Lexer
from t013parserParser import t013parserParser as Parser

class TestParser(Parser):
    def __init__(self, *args, **kwargs):
        Parser.__init__(self, *args, **kwargs)

        self.identifiers = []
        self.reportedErrors = []
        

    def foundIdentifier(self, name):
        self.identifiers.append(name)


    def emitErrorMessage(self, msg):
        self.reportedErrors.append(msg)
        
    
cStream = antlr3.StringStream('foobar')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TestParser(tStream)
parser.document()

assert len(parser.reportedErrors) == 0, parser.reportedErrors
assert parser.identifiers == ['foobar']


# malformed input
cStream = antlr3.StringStream('')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TestParser(tStream)

parser.document()

# FIXME: currently strings with formatted errors are collected
# can't check error locations yet
assert len(parser.reportedErrors) == 1, parser.reportedErrors
