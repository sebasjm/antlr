import textwrap
import antlr3
from t038lexerRuleLabelLexer import t038lexerRuleLabelLexer as Lexer

class TLexer(Lexer):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise

cStream = antlr3.StringStream(
    'a  2'
    )

lexer = TLexer(cStream)

while True:
    t = lexer.nextToken()
    if t.type == antlr3.EOF:
        break
    print t
    
