import antlr3
from t003lexerLexer import t003lexerLexer as Lexer
from t003lexerLexer import ZERO, FOOZE, ONE, EOF

stream = antlr3.StringStream('0fooze1')
lexer = Lexer(stream)

token = lexer.nextToken()
assert token.type == ZERO

token = lexer.nextToken()
assert token.type == FOOZE

token = lexer.nextToken()
assert token.type == ONE

token = lexer.nextToken()
assert token.type == EOF
