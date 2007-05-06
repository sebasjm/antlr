
import unittest

import antlr3


class TestLexer(unittest.TestCase):

    def testInit(self):
        """Lexer.__init__()"""

        stream = antlr3.StringStream('foo')
        antlr3.Lexer(stream)
        

if __name__ == "__main__":
    unittest.main(testRunner=unittest.TextTestRunner(verbosity=2))
