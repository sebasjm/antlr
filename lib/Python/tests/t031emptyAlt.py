import antlr3
from t031emptyAltParser import t031emptyAltParser as Parser
from t031emptyAltLexer import t031emptyAltLexer as Lexer

class TParser(Parser):
    def __init__(self, *args, **kwargs):
        Parser.__init__(self, *args, **kwargs)

        self.cond = True
        
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise re

cStream = antlr3.StringStream('foo')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
events = parser.r()


cStream = antlr3.StringStream('foo name1')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
events = parser.r()


cStream = antlr3.StringStream('bar name1')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
parser.cond = False
events = parser.r()


cStream = antlr3.StringStream('bar name1 name2')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
parser.cond = False
events = parser.r()


