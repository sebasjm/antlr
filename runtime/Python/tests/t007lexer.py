import antlr3
from t007lexerLexer import t007lexerLexer as Lexer
from t007lexerLexer import FOO, EOF

stream = antlr3.StringStream('fofababbooabb')
lexer = Lexer(stream)

token = lexer.nextToken()
assert token.type == FOO
assert token.start == 0, token.start
assert token.stop == 1, token.stop
assert token.text == 'fo', token.text

token = lexer.nextToken()
assert token.type == FOO
assert token.start == 2, token.start
assert token.stop == 12, token.stop
assert token.text == 'fababbooabb', token.text

token = lexer.nextToken()
assert token.type == EOF


# malformed input
stream = antlr3.StringStream('foaboao')
lexer = Lexer(stream)

try:
    token = lexer.nextToken()
    raise AssertionError, token

except antlr3.EarlyExitException, exc:
    assert exc.unexpectedType == 'o', repr(exc.unexpectedType)
    assert exc.charPositionInLine == 6, repr(exc.charPositionInLine)
    assert exc.line == 1, repr(exc.line)
