import antlr3
from t005lexerLexer import t005lexerLexer as Lexer
from t005lexerLexer import FOO, EOF

stream = antlr3.StringStream('fofoofooo')
lexer = Lexer(stream)

token = lexer.nextToken()
assert token.type == FOO
assert token.start == 0, token.start
assert token.stop == 1, token.stop
assert token.text == 'fo', token.text

token = lexer.nextToken()
assert token.type == FOO
assert token.start == 2, token.start
assert token.stop == 4, token.stop
assert token.text == 'foo', token.text

token = lexer.nextToken()
assert token.type == FOO
assert token.start == 5, token.start
assert token.stop == 8, token.stop
assert token.text == 'fooo', token.text

token = lexer.nextToken()
assert token.type == EOF


# malformed input
stream = antlr3.StringStream('2')
lexer = Lexer(stream)

try:
    token = lexer.nextToken()
    raise AssertionError

except antlr3.MismatchedTokenException, exc:
    assert exc.expecting == 'f', repr(exc.expecting)
    assert exc.unexpectedType == '2', repr(exc.unexpectedType)


stream = antlr3.StringStream('f')
lexer = Lexer(stream)

try:
    token = lexer.nextToken()
    raise AssertionError

except antlr3.EarlyExitException, exc:
    assert exc.unexpectedType == antlr3.EOF, repr(exc.unexpectedType)
