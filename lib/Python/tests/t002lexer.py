import antlr3
from t002lexerLexer import t002lexerLexer as Lexer
from t002lexerLexer import ZERO, ONE, EOF

stream = antlr3.StringStream('01')
lexer = Lexer(stream)

token = lexer.nextToken()
assert token.type == ZERO

token = lexer.nextToken()
assert token.type == ONE

token = lexer.nextToken()
assert token.type == EOF
