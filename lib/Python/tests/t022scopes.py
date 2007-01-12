import antlr3
from t022scopesLexer import t022scopesLexer as Lexer
from t022scopesParser import t022scopesParser as Parser

cStream = antlr3.StringStream('foobar')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
parser.prog()

