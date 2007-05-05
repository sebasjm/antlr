import antlr3
from t023scopesLexer import t023scopesLexer as Lexer
from t023scopesParser import t023scopesParser as Parser

cStream = antlr3.StringStream('foobar')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
parser.prog()

