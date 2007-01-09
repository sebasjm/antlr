import antlr3
from t009lexerLexer import t009lexerLexer as Lexer
from t009lexerLexer import DIGIT, EOF

stream = antlr3.StringStream('085')
lexer = Lexer(stream)

token = lexer.nextToken()
assert token.type == DIGIT
assert token.start == 0, token.start
assert token.stop == 0, token.stop
assert token.text == '0', token.text

token = lexer.nextToken()
assert token.type == DIGIT
assert token.start == 1, token.start
assert token.stop == 1, token.stop
assert token.text == '8', token.text

token = lexer.nextToken()
assert token.type == DIGIT
assert token.start == 2, token.start
assert token.stop == 2, token.stop
assert token.text == '5', token.text

token = lexer.nextToken()
assert token.type == EOF
