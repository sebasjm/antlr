import antlr3
from t043synpredLexer import t043synpredLexer as Lexer
from t043synpredParser import t043synpredParser as Parser

cStream = antlr3.StringStream('   +foo>')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
events = parser.a()
    
