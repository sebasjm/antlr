import antlr3
from t017parserLexer import t017parserLexer as Lexer
from t017parserParser import t017parserParser as Parser

class TestParser(Parser):
    def __init__(self, *args, **kwargs):
        Parser.__init__(self, *args, **kwargs)

        self.reportedErrors = []
        

    def emitErrorMessage(self, msg):
        self.reportedErrors.append(msg)
        
    
cStream = antlr3.StringStream("int foo;")
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TestParser(tStream)
parser.program()

assert len(parser.reportedErrors) == 0, parser.reportedErrors


# malformed input
cStream = antlr3.StringStream('int foo() { 1+2 }')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TestParser(tStream)
parser.program()

# FIXME: currently strings with formatted errors are collected
# can't check error locations yet
assert len(parser.reportedErrors) == 1, parser.reportedErrors


# malformed input
cStream = antlr3.StringStream('int foo() { 1+; 1+2 }')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TestParser(tStream)
parser.program()

# FIXME: currently strings with formatted errors are collected
# can't check error locations yet
assert len(parser.reportedErrors) == 2, parser.reportedErrors
