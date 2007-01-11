import os
import antlr3
from t019lexerLexer import t019lexerLexer as Lexer

inputPath = os.path.splitext(__file__)[0] + '.input'
stream = antlr3.StringStream(open(inputPath).read())
lexer = Lexer(stream)

while True:
    token = lexer.nextToken()
    if token.type == antlr3.EOF:
        break
