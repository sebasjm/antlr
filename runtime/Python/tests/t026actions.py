import antlr3
from t026actionsLexer import t026actionsLexer as Lexer
from t026actionsParser import t026actionsParser as Parser
from t026actionsLexer import IDENTIFIER, WS, EOF

cStream = antlr3.StringStream('foobar _Ab98 \n A12sdf')
lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = Parser(tStream)
try:
    events = parser.prog()
except antlr3.RecognitionException:
    pass
    
