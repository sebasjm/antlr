import textwrap
import antlr3
from t041parametersLexer import t041parametersLexer as Lexer
from t041parametersParser import t041parametersParser as Parser

class TLexer(Lexer):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise

class TParser(Parser):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise


cStream = antlr3.StringStream(
    'a a a'
    )

lexer = TLexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
r = parser.a('foo', 'bar')

assert r == ('foo', 'bar'), r

