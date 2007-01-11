import antlr3
from t017parserLexer import t017parserLexer as Lexer
from t017parserParser import t017parserParser as Parser

cStream = antlr3.StringStream("int foo;")
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
parser.program()
