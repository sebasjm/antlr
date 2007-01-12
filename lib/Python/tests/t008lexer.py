import antlr3
from t008lexerLexer import t008lexerLexer as Lexer
from t008lexerLexer import FOO, EOF

stream = antlr3.StringStream('ffaf')
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
assert token.text == 'fa', token.text

token = lexer.nextToken()
assert token.type == FOO
assert token.start == 3, token.start
assert token.stop == 3, token.stop
assert token.text == 'f', token.text

token = lexer.nextToken()
assert token.type == EOF


# malformed input
stream = antlr3.StringStream('fafb')
lexer = Lexer(stream)

lexer.nextToken()
lexer.nextToken()
try:
    token = lexer.nextToken()
    raise AssertionError, token

except antlr3.MismatchedTokenException, exc:
    assert exc.unexpectedType == 'b', repr(exc.unexpectedType)
    assert exc.charPositionInLine == 3, repr(exc.charPositionInLine)
    assert exc.line == 1, repr(exc.line)
