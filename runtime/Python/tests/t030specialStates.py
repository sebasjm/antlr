import antlr3
from t030specialStatesParser import t030specialStatesParser as Parser
from t030specialStatesLexer import t030specialStatesLexer as Lexer

cStream = antlr3.StringStream('foo')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
events = parser.r()


cStream = antlr3.StringStream('foo name1')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
events = parser.r()


cStream = antlr3.StringStream('bar name1')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
parser.cond = False
events = parser.r()


cStream = antlr3.StringStream('bar name1 name2')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
parser.cond = False
events = parser.r()


