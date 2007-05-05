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


# malformed input
stream = antlr3.StringStream('2')
lexer = Lexer(stream)

try:
    token = lexer.nextToken()
    raise AssertionError

except antlr3.NoViableAltException, exc:
    assert exc.unexpectedType == '2', repr(exc.unexpectedType)
