import textwrap
import antlr3
from t032subrulePredictParser import t032subrulePredictParser as Parser
from t032subrulePredictLexer import t032subrulePredictLexer as Lexer

class TParser(Parser):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise


cStream = antlr3.StringStream(
    textwrap.dedent('''\
    <!SGML "ISO 8879:1986"

    CHARSET
      BASESET  "ISO bla bla"
      DESCSET 0 65536 0        
    >
    '''
                    ))

lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
events = parser.rSgmlDeclaration()


cStream = antlr3.StringStream(
    textwrap.dedent('''\
    CHARSET
      BASESET  "ISO bla bla"
      DESCSET 0 65536 0        
    '''
                    ))

lexer = Lexer(cStream)
tStream = antlr3.CommonTokenStream(lexer)
parser = TParser(tStream)
events = parser.rDocumentCharacterSet()


