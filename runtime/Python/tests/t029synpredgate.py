import antlr3
from t029synpredgateLexer import t029synpredgateLexer as Lexer

stream = antlr3.StringStream('ac')
lexer = Lexer(stream)

token = lexer.nextToken()


