import antlr3
from t015calcLexer import t015calcLexer as Lexer
from t015calcParser import t015calcParser as Parser

def evaluate(expr, expected):
    cStream = antlr3.StringStream(expr)
    lexer = Lexer(cStream)
    tStream = antlr3.CommonTokenStream(lexer)
    parser = Parser(tStream)
    result = parser.evaluate()
    assert result == expected, "%r != %r" % (result, expected)


evaluate("1 + 2", 3)
evaluate("1 + 2 * 3", 7)
evaluate("10 / 2", 5)
evaluate("6 + 2*(3+1) - 4", 10)
