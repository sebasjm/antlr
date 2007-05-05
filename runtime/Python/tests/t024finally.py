import antlr3
from t024finallyLexer import t024finallyLexer as Lexer
from t024finallyParser import t024finallyParser as Parser

cStream = antlr3.StringStream('foobar')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
events = parser.prog()

assert events == ['catch', 'finally'], events
