import textwrap
import antlr3
from t033backtrackingParser import t033backtrackingParser as Parser
from t033backtrackingLexer import t033backtrackingLexer as Lexer

class TParser(Parser):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise


cStream = antlr3.StringStream(
    'int a;'
    )

lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
events = parser.translation_unit()


