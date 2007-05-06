
import unittest

import antlr3


class TestDFA(unittest.TestCase):
    """Test case for the DFA class."""

    def setUp(self):
        """Setup test fixure.

        We need a Recognizer in order to instanciate a DFA.

        """

        self.recog = antlr3.BaseRecognizer()
        
        
    def testInit(self):
        """DFA.__init__()

        Just a smoke test.
        
        """

        dfa = antlr3.DFA(
            self.recog, 1,
            eot=[],
            eof=[],
            min=[],
            max=[],
            accept=[],
            special=[],
            transition=[]
            )      
        

if __name__ == "__main__":
    unittest.main(testRunner=unittest.TextTestRunner(verbosity=2))
