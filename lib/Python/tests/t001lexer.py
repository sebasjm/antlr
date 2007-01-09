import antlr3
from t001lexerLexer import t001lexerLexer as Lexer
from t001lexerLexer import ZERO, EOF

stream = antlr3.StringStream('0')
lexer = Lexer(stream)

token = lexer.nextToken()
assert token.type == ZERO

token = lexer.nextToken()
assert token.type == EOF
