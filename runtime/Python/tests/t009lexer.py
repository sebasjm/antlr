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


# malformed input
stream = antlr3.StringStream('2a')
lexer = Lexer(stream)

lexer.nextToken()
try:
    token = lexer.nextToken()
    raise AssertionError, token

except antlr3.MismatchedRangeException, exc:
    assert exc.a == '0', repr(exc.a)
    assert exc.b == '9', repr(exc.b)
    assert exc.unexpectedType == 'a', repr(exc.unexpectedType)
    assert exc.charPositionInLine == 1, repr(exc.charPositionInLine)
    assert exc.line == 1, repr(exc.line)
