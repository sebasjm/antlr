import antlr3
from t025lexerRulePropertyRefLexer import t025lexerRulePropertyRefLexer as Lexer
from t025lexerRulePropertyRefLexer import IDENTIFIER, WS, EOF

stream = antlr3.StringStream('foobar _Ab98 \n A12sdf')
lexer = Lexer(stream)

while True:
    token = lexer.nextToken()
    if token.type == EOF:
        break

assert len(lexer.properties) == 3, lexer.properties

text, type, line, pos, index, channel, start, stop = lexer.properties[0]
assert text == 'foobar', lexer.properties[0]
assert type == IDENTIFIER, lexer.properties[0]
assert line == 1, lexer.properties[0]
assert pos == 0, lexer.properties[0]
assert index == -1, lexer.properties[0]
assert channel == antlr3.DEFAULT_CHANNEL, lexer.properties[0]
assert start == 0, lexer.properties[0]
assert stop == 5, lexer.properties[0]

text, type, line, pos, index, channel, start, stop = lexer.properties[1]
assert text == '_Ab98', lexer.properties[1]
assert type == IDENTIFIER, lexer.properties[1]
assert line == 1, lexer.properties[1]
assert pos == 7, lexer.properties[1]
assert index == -1, lexer.properties[1]
assert channel == antlr3.DEFAULT_CHANNEL, lexer.properties[1]
assert start == 7, lexer.properties[1]
assert stop == 11, lexer.properties[1]

text, type, line, pos, index, channel, start, stop = lexer.properties[2]
assert text == 'A12sdf', lexer.properties[2]
assert type == IDENTIFIER, lexer.properties[2]
assert line == 2, lexer.properties[2]
assert pos == 1, lexer.properties[2]
assert index == -1, lexer.properties[2]
assert channel == antlr3.DEFAULT_CHANNEL, lexer.properties[2]
assert start == 15, lexer.properties[2]
assert stop == 20, lexer.properties[2]
