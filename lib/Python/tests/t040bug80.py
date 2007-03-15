import textwrap
import antlr3
from t040bug80Lexer import t040bug80Lexer as Lexer

class TLexer(Lexer):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise

cStream = antlr3.StringStream('defined')
lexer = TLexer(cStream)
while True:
    t = lexer.nextToken()
    if t.type == antlr3.EOF:
        break
    print t
