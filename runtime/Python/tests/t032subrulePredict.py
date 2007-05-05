import textwrap
import antlr3
from t032subrulePredictParser import t032subrulePredictParser as Parser
from t032subrulePredictLexer import t032subrulePredictLexer as Lexer

class TParser(Parser):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise


cStream = antlr3.StringStream(
    'BEGIN A END'
    )

lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
events = parser.a()


cStream = antlr3.StringStream(
    'BEGIN A'
    )

lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
events = parser.b()


