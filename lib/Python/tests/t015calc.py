import antlr3
from t015calcLexer import t015calcLexer as Lexer
from t015calcParser import t015calcParser as Parser

class TestParser(Parser):
    def __init__(self, *args, **kwargs):
        Parser.__init__(self, *args, **kwargs)

        self.reportedErrors = []


    def emitErrorMessage(self, msg):
        self.reportedErrors.append(msg)

        
def evaluate(expr, expected, errors=[]):
    cStream = antlr3.StringStream(expr)
    lexer = Lexer(cStream)
    tStream = antlr3.CommonTokenStream(lexer)
    parser = TestParser(tStream)
    result = parser.evaluate()
    assert result == expected, "%r != %r" % (result, expected)
    assert len(parser.reportedErrors) == len(errors), parser.reportedErrors

evaluate("1 + 2", 3)
evaluate("1 + 2 * 3", 7)
evaluate("10 / 2", 5)
evaluate("6 + 2*(3+1) - 4", 10)

# malformed input
evaluate("6 - (2*1", 4, ["mismatched token at pos 8"])

# FIXME: most parse errors result in TypeErrors in action code, because
# rules return None, which is then added/multiplied... to integers.
# evaluate("6 - foo 2", 4, ["some error"])
