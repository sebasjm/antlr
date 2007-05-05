import antlr3
from t016actionsLexer import t016actionsLexer as Lexer
from t016actionsParser import t016actionsParser as Parser

cStream = antlr3.StringStream("int foo;")
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
name = parser.declaration()
assert name == 'foo', name
