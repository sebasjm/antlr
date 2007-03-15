import textwrap
import antlr3
from t036multipleReturnValuesLexer import t036multipleReturnValuesLexer as Lexer
from t036multipleReturnValuesParser import t036multipleReturnValuesParser as Parser

class TLexer(Lexer):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise

class TParser(Parser):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise


cStream = antlr3.StringStream(
    '   a'
    )

lexer = TLexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
ret = parser.a()
assert ret.foo == 'foo', ret.foo
assert ret.bar == 'bar', ret.bar
