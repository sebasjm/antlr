import antlr3
from t027eofLexer import t027eofLexer as Lexer, SPACE, END

cStream = antlr3.StringStream(' ')
lexer = Lexer(cStream)

tok = lexer.nextToken()
assert tok.type == SPACE, tok

tok = lexer.nextToken()
assert tok.type == END, tok

