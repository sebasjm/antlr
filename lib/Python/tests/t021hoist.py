import antlr3
from t021hoistLexer import t021hoistLexer as Lexer
from t021hoistParser import t021hoistParser as Parser

cStream = antlr3.StringStream('enum')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
parser.enableEnum = True
enumIs = parser.stat()

assert enumIs == 'keyword', repr(enumIs)


cStream = antlr3.StringStream('enum')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
parser.enableEnum = False
enumIs = parser.stat()

assert enumIs == 'ID', repr(enumIs)

