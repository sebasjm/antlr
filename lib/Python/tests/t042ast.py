import unittest
import textwrap
import antlr3
from t042astLexer import t042astLexer as Lexer
from t042astParser import t042astParser as Parser

class TLexer(Lexer):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise

class TParser(Parser):
    def recover(self, input, re):
        # no error recovery yet, just crash!
        raise




class TestAst(unittest.TestCase):

    def parse(self, text, method):
        cStream = antlr3.StringStream(text)
        lexer = TLexer(cStream)
        tStream = antlr3.CommonTokenStream(lexer)
        parser = TParser(tStream)
        r = method(parser)
        return r.tree

    
    def testR1(self):
        tree = self.parse("1 + 2", TParser.r1)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(+ 1 2)'
            )


    def testR2a(self):
        tree = self.parse("assert 2+3;", TParser.r2)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(assert (+ 2 3))'
            )


    def testR2b(self):
        tree = self.parse("assert 2+3 : 5;", TParser.r2)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(assert (+ 2 3) 5)'
            )


    def testR3a(self):
        tree = self.parse("if 1 fooze", TParser.r3)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(if 1 fooze)'
            )


    def testR3b(self):
        tree = self.parse("if 1 fooze else fooze", TParser.r3)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(if 1 fooze fooze)'
            )


    def testR4a(self):
        tree = self.parse("while 2 fooze", TParser.r4)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(while 2 fooze)'
            )


    def testR5a(self):
        tree = self.parse("return;", TParser.r5)
        self.failUnlessEqual(
            tree.toStringTree(),
            'return'
            )


    def testR5b(self):
        tree = self.parse("return 2+3;", TParser.r5)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(return (+ 2 3))'
            )


    def testR6a(self):
        tree = self.parse("3", TParser.r6)
        self.failUnlessEqual(
            tree.toStringTree(),
            '3'
            )


    def testR6b(self):
        tree = self.parse("3 a", TParser.r6)
        self.failUnlessEqual(
            tree.toStringTree(),
            '3 a'
            )


    def testR7(self):
        tree = self.parse("3", TParser.r7)
        self.failUnlessEqual(
            tree.toStringTree(),
            'nil'
            )


    def testR8(self):
        tree = self.parse("var foo:bool", TParser.r8)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(var bool foo)'
            )


    def testR9(self):
        tree = self.parse("int foo;", TParser.r9)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(VARDEF int foo)'
            )


    def testR10(self):
        tree = self.parse("10", TParser.r10)
        self.failUnlessEqual(
            tree.toStringTree(),
            '10.0'
            )


    def testR11a(self):
        tree = self.parse("1+2", TParser.r11)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(EXPR (+ 1 2))'
            )


    def testR11b(self):
        tree = self.parse("", TParser.r11)
        self.failUnlessEqual(
            tree.toStringTree(),
            'EXPR'
            )


    def testR12a(self):
        tree = self.parse("foo", TParser.r12)
        self.failUnlessEqual(
            tree.toStringTree(),
            'foo'
            )


    def testR12b(self):
        tree = self.parse("foo, bar, gnurz", TParser.r12)
        self.failUnlessEqual(
            tree.toStringTree(),
            'foo bar gnurz'
            )


    def testR13a(self):
        tree = self.parse("int foo;", TParser.r13)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(int foo)'
            )


    def testR13b(self):
        tree = self.parse("bool foo, bar, gnurz;", TParser.r13)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(bool foo bar gnurz)'
            )


    def testR14a(self):
        tree = self.parse("1+2 int", TParser.r14)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(EXPR (+ 1 2) int)'
            )


    def testR14b(self):
        tree = self.parse("1+2 int bool", TParser.r14)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(EXPR (+ 1 2) int bool)'
            )


    def testR14c(self):
        tree = self.parse("int bool", TParser.r14)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(EXPR int bool)'
            )


    def testR14d(self):
        tree = self.parse("fooze fooze int bool", TParser.r14)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(EXPR fooze fooze int bool)'
            )


    def testR14e(self):
        tree = self.parse("7+9 fooze fooze int bool", TParser.r14)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(EXPR (+ 7 9) fooze fooze int bool)'
            )


    def testR15(self):
        tree = self.parse("7", TParser.r15)
        self.failUnlessEqual(
            tree.toStringTree(),
            '7 7'
            )


    def testR16a(self):
        tree = self.parse("int foo", TParser.r16)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(int foo)'
            )


    def testR16b(self):
        try:
            tree = self.parse("int foo, bar, gnurz", TParser.r16)
            self.fail()
            
            self.failUnlessEqual(
                tree.toStringTree(),
                '(int foo) (int bar) (int gnurz)'
                )

        except RuntimeError:
            # this is broken upstream
            # bug ANTLR-98
            pass


    def testR17a(self):
        tree = self.parse("for ( fooze ; 1 + 2 ; fooze ) fooze", TParser.r17)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(for fooze (+ 1 2) fooze fooze)'
            )


    def testR18a(self):
        tree = self.parse("for", TParser.r18)
        self.failUnlessEqual(
            tree.toStringTree(),
            'BLOCK'
            )


    def testR19a(self):
        tree = self.parse("for", TParser.r19)
        self.failUnlessEqual(
            tree.toStringTree(),
            'for'
            )


    def testR20a(self):
        tree = self.parse("for", TParser.r20)
        self.failUnlessEqual(
            tree.toStringTree(),
            'FOR'
            )


    def testR21a(self):
        tree = self.parse("for", TParser.r21)
        self.failUnlessEqual(
            tree.toStringTree(),
            'BLOCK'
            )


    def testR22a(self):
        tree = self.parse("for", TParser.r22)
        self.failUnlessEqual(
            tree.toStringTree(),
            'for'
            )


    def testR23a(self):
        tree = self.parse("for", TParser.r23)
        self.failUnlessEqual(
            tree.toStringTree(),
            'FOR'
            )


    def testR24a(self):
        tree = self.parse("fooze 1 + 2", TParser.r24)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(fooze (+ 1 2))'
            )


    def testR25a(self):
        tree = self.parse("fooze, fooze2 1 + 2", TParser.r25)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(fooze (+ 1 2))'
            )


    def testR26a(self):
        tree = self.parse("fooze, fooze2", TParser.r26)
        self.failUnlessEqual(
            tree.toStringTree(),
            '(BLOCK fooze fooze2)'
            )


    def testR27a(self):
        tree = self.parse("fooze 1 + 2", TParser.r27)
        
        try:
            tree.toStringTree(),
            self.fail()
            
        except RuntimeError:
            # Ter says:
            # Just a heads up. I am investigating the last major problem before  
            # version 3 release: AST rewrite rules. The cardinality checks are all  
            # screwed up for unusual cases and the rewrite rules don't duplicate  
            # nodes when they need to.  I fear that I will have to make major  
            # modifications to the code generation in order to make it general.
            # I think the infinite recursion here is what he's talking about.
            pass

        else:
            self.failUnlessEqual(
                tree.toStringTree(),
                '(fooze (+ 1 2))'
                )
            

    def testR28(self):
        tree = self.parse("foo28a", TParser.r28)
        self.failUnlessEqual(
            tree.toStringTree(),
            'nil'
            )


    def testR29(self):
        try:
            tree = self.parse("", TParser.r29)
            self.fail()
        except RuntimeError:
            pass


    def testR30(self):
        try:
            tree = self.parse("fooze fooze", TParser.r30)
            self.fail()
        except RuntimeError:
            pass
        






if __name__ == '__main__':
    unittest.main()

