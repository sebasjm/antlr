import textwrap
import antlr3
from t035ruleLabelPropertyRefLexer import t035ruleLabelPropertyRefLexer as Lexer
from t035ruleLabelPropertyRefParser import t035ruleLabelPropertyRefParser as Parser

class TLexer(Lexer):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise

class TParser(Parser):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise


cStream = antlr3.StringStream(
    '   a a a a  '
    )

lexer = TLexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
start, stop, text = parser.a()

# first token of rule b is the 2nd token (counting hidden tokens)
assert start.index == 1, start

# first token of rule b is the 7th token (counting hidden tokens)
assert stop.index == 7, stop

assert text == "a a a a", text
