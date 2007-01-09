import antlr3
from t004lexerLexer import t004lexerLexer as Lexer
from t004lexerLexer import FOO, EOF

stream = antlr3.StringStream('ffofoofooo')
lexer = Lexer(stream)

token = lexer.nextToken()
assert token.type == FOO
assert token.start == 0, token.start
assert token.stop == 0, token.stop
assert token.text == 'f', token.text

token = lexer.nextToken()
assert token.type == FOO
assert token.start == 1, token.start
assert token.stop == 2, token.stop
assert token.text == 'fo', token.text

token = lexer.nextToken()
assert token.type == FOO
assert token.start == 3, token.start
assert token.stop == 5, token.stop
assert token.text == 'foo', token.text

token = lexer.nextToken()
assert token.type == FOO
assert token.start == 6, token.start
assert token.stop == 9, token.stop
assert token.text == 'fooo', token.text

token = lexer.nextToken()
assert token.type == EOF
