import textwrap
import antlr3
from t039labelsLexer import t039labelsLexer as Lexer
from t039labelsParser import t039labelsParser as Parser

class TLexer(Lexer):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise

class TParser(Parser):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise


cStream = antlr3.StringStream(
    'a, b, c, 1, 2 A FOOBAR GNU1 A BLARZ'
    )

lexer = TLexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
ids, w = parser.a()

assert len(ids) == 6, ids
assert ids[0].text == 'a', ids[0]
assert ids[1].text == 'b', ids[1]
assert ids[2].text == 'c', ids[2]
assert ids[3].text == '1', ids[3]
assert ids[4].text == '2', ids[4]
assert ids[5].text == 'A', ids[5]

assert w.text == 'GNU1', w



## cStream = antlr3.StringStream(
##     'a, b, c, 1, 2'
##     )

## lexer = TLexer(cStream)
## tStream = antlr3.CommonTokenStream(lexer)
## parser = TParser(tStream)
## ids = parser.b()

## assert ids == ['a', 'b', 'c', '1', '2'], ids

