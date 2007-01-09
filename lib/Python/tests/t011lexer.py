import antlr3
from t011lexerLexer import t011lexerLexer as Lexer
from t011lexerLexer import IDENTIFIER, WS, EOF

stream = antlr3.StringStream('foobar _Ab98 \n A12sdf')
lexer = Lexer(stream)

token = lexer.nextToken()
assert token.type == IDENTIFIER
assert token.start == 0, token.start
assert token.stop == 5, token.stop
assert token.text == 'foobar', token.text

token = lexer.nextToken()
assert token.type == WS
assert token.start == 6, token.start
assert token.stop == 6, token.stop
assert token.text == ' ', token.text

token = lexer.nextToken()
assert token.type == IDENTIFIER
assert token.start == 7, token.start
assert token.stop == 11, token.stop
assert token.text == '_Ab98', token.text

token = lexer.nextToken()
assert token.type == WS
assert token.start == 12, token.start
assert token.stop == 14, token.stop
assert token.text == ' \n ', token.text

token = lexer.nextToken()
assert token.type == IDENTIFIER
assert token.start == 15, token.start
assert token.stop == 20, token.stop
assert token.text == 'A12sdf', token.text

token = lexer.nextToken()
assert token.type == EOF
