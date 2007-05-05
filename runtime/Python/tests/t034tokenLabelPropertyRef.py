import textwrap
import antlr3
from t034tokenLabelPropertyRefLexer import t034tokenLabelPropertyRefLexer as Lexer
from t034tokenLabelPropertyRefParser import t034tokenLabelPropertyRefParser as Parser

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
events = parser.a()
    
