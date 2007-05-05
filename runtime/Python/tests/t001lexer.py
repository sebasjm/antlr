import antlr3
from t001lexerLexer import t001lexerLexer as Lexer
from t001lexerLexer import ZERO, EOF

stream = antlr3.StringStream('0')
lexer = Lexer(stream)

token = lexer.nextToken()
assert token.type == ZERO

token = lexer.nextToken()
assert token.type == EOF


# malformed input
stream = antlr3.StringStream('1')
lexer = Lexer(stream)

try:
    token = lexer.nextToken()
    raise AssertionError

except antlr3.MismatchedTokenException, exc:
    assert exc.expecting == '0', repr(exc.expecting)
    assert exc.unexpectedType == '1', repr(exc.unexpectedType)

