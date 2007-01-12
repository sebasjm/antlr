import antlr3
from t006lexerLexer import t006lexerLexer as Lexer
from t006lexerLexer import FOO, EOF

stream = antlr3.StringStream('fofaaooa')
lexer = Lexer(stream)

token = lexer.nextToken()
assert token.type == FOO
assert token.start == 0, token.start
assert token.stop == 1, token.stop
assert token.text == 'fo', token.text

token = lexer.nextToken()
assert token.type == FOO
assert token.start == 2, token.start
assert token.stop == 7, token.stop
assert token.text == 'faaooa', token.text

token = lexer.nextToken()
assert token.type == EOF


# malformed input
stream = antlr3.StringStream('fofoaooaoa2')
lexer = Lexer(stream)

lexer.nextToken()
lexer.nextToken()
try:
    token = lexer.nextToken()
    raise AssertionError, token

except antlr3.MismatchedTokenException, exc:
    assert exc.expecting == 'f', repr(exc.expecting)
    assert exc.unexpectedType == '2', repr(exc.unexpectedType)
    assert exc.charPositionInLine == 10, repr(exc.charPositionInLine)
    assert exc.line == 1, repr(exc.line)
