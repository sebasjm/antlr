import antlr3
from t014parserLexer import t014parserLexer as Lexer
from t014parserParser import t014parserParser as Parser

class TestParser(Parser):
    def __init__(self, *args, **kwargs):
        Parser.__init__(self, *args, **kwargs)

        self.events = []

        
cStream = antlr3.StringStream('var foobar; gnarz(); var blupp; flupp ( ) ;')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TestParser(tStream)
parser.document()

assert parser.events == [
    ('decl', 'foobar'),
    ('call', 'gnarz'),
    ('decl', 'blupp'),
    ('call', 'flupp')
    ], parser.events

