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

    def parse(self, text, method, rArgs=[], **kwargs):
        cStream = antlr3.StringStream(text)
        lexer = TLexer(cStream)
        tStream = antlr3.CommonTokenStream(lexer)
        parser = TParser(tStream)
        
        for attr, val in kwargs.items():
            setattr(parser, attr, val)
            
        return method(parser, *rArgs)

    
    def testR1(self):
        r = self.parse("1 + 2", TParser.r1)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(+ 1 2)'
            )


    def testR2a(self):
        r = self.parse("assert 2+3;", TParser.r2)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(assert (+ 2 3))'
            )


    def testR2b(self):
        r = self.parse("assert 2+3 : 5;", TParser.r2)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(assert (+ 2 3) 5)'
            )


    def testR3a(self):
        r = self.parse("if 1 fooze", TParser.r3)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(if 1 fooze)'
            )


    def testR3b(self):
        r = self.parse("if 1 fooze else fooze", TParser.r3)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(if 1 fooze fooze)'
            )


    def testR4a(self):
        r = self.parse("while 2 fooze", TParser.r4)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(while 2 fooze)'
            )


    def testR5a(self):
        r = self.parse("return;", TParser.r5)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'return'
            )


    def testR5b(self):
        r = self.parse("return 2+3;", TParser.r5)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(return (+ 2 3))'
            )


    def testR6a(self):
        r = self.parse("3", TParser.r6)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '3'
            )


    def testR6b(self):
        r = self.parse("3 a", TParser.r6)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '3 a'
            )


    def testR7(self):
        r = self.parse("3", TParser.r7)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'nil'
            )


    def testR8(self):
        r = self.parse("var foo:bool", TParser.r8)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(var bool foo)'
            )


    def testR9(self):
        r = self.parse("int foo;", TParser.r9)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(VARDEF int foo)'
            )


    def testR10(self):
        r = self.parse("10", TParser.r10)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '10.0'
            )


    def testR11a(self):
        r = self.parse("1+2", TParser.r11)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(EXPR (+ 1 2))'
            )


    def testR11b(self):
        r = self.parse("", TParser.r11)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'EXPR'
            )


    def testR12a(self):
        r = self.parse("foo", TParser.r12)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'foo'
            )


    def testR12b(self):
        r = self.parse("foo, bar, gnurz", TParser.r12)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'foo bar gnurz'
            )


    def testR13a(self):
        r = self.parse("int foo;", TParser.r13)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(int foo)'
            )


    def testR13b(self):
        r = self.parse("bool foo, bar, gnurz;", TParser.r13)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(bool foo bar gnurz)'
            )


    def testR14a(self):
        r = self.parse("1+2 int", TParser.r14)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(EXPR (+ 1 2) int)'
            )


    def testR14b(self):
        r = self.parse("1+2 int bool", TParser.r14)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(EXPR (+ 1 2) int bool)'
            )


    def testR14c(self):
        r = self.parse("int bool", TParser.r14)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(EXPR int bool)'
            )


    def testR14d(self):
        r = self.parse("fooze fooze int bool", TParser.r14)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(EXPR fooze fooze int bool)'
            )


    def testR14e(self):
        r = self.parse("7+9 fooze fooze int bool", TParser.r14)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(EXPR (+ 7 9) fooze fooze int bool)'
            )


    def testR15(self):
        r = self.parse("7", TParser.r15)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '7 7'
            )


    def testR16a(self):
        r = self.parse("int foo", TParser.r16)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(int foo)'
            )


    def testR16b(self):
        r = self.parse("int foo, bar, gnurz", TParser.r16)
            
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(int foo) (int bar) (int gnurz)'
            )


    def testR17a(self):
        r = self.parse("for ( fooze ; 1 + 2 ; fooze ) fooze", TParser.r17)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(for fooze (+ 1 2) fooze fooze)'
            )


    def testR18a(self):
        r = self.parse("for", TParser.r18)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'BLOCK'
            )


    def testR19a(self):
        r = self.parse("for", TParser.r19)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'for'
            )


    def testR20a(self):
        r = self.parse("for", TParser.r20)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'FOR'
            )


    def testR21a(self):
        r = self.parse("for", TParser.r21)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'BLOCK'
            )


    def testR22a(self):
        r = self.parse("for", TParser.r22)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'for'
            )


    def testR23a(self):
        r = self.parse("for", TParser.r23)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'FOR'
            )


    def testR24a(self):
        r = self.parse("fooze 1 + 2", TParser.r24)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(fooze (+ 1 2))'
            )


    def testR25a(self):
        r = self.parse("fooze, fooze2 1 + 2", TParser.r25)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(fooze (+ 1 2))'
            )


    def testR26a(self):
        r = self.parse("fooze, fooze2", TParser.r26)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(BLOCK fooze fooze2)'
            )


    def testR27a(self):
        r = self.parse("fooze 1 + 2", TParser.r27)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(fooze (fooze (+ 1 2)))'
            )
            

    def testR28(self):
        r = self.parse("foo28a", TParser.r28)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'nil'
            )


    def testR29(self):
        try:
            r = self.parse("", TParser.r29)
            self.fail()
        except RuntimeError:
            pass


# FIXME: broken upstream?
##     def testR30(self):
##         try:
##             r = self.parse("fooze fooze", TParser.r30)
##             self.fail(r.tree.toStringTree())
##         except RuntimeError:
##             pass


    def testR31a(self):
        r = self.parse("public int gnurz = 1 + 2;", TParser.r31, flag=0)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(VARDEF gnurz public int (+ 1 2))'
            )


    def testR31b(self):
        r = self.parse("public int gnurz = 1 + 2;", TParser.r31, flag=1)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(VARIABLE gnurz public int (+ 1 2))'
            )


    def testR31c(self):
        r = self.parse("public int gnurz = 1 + 2;", TParser.r31, flag=2)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(FIELD gnurz public int (+ 1 2))'
            )


    def testR32a(self):
        r = self.parse("gnurz 32", TParser.r32, [1], flag=2)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'gnurz'
            )


    def testR32b(self):
        r = self.parse("gnurz 32", TParser.r32, [2], flag=2)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '32'
            )


    def testR32b(self):
        r = self.parse("gnurz 32", TParser.r32, [3], flag=2)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'nil'
            )


    def testR33a(self):
        r = self.parse("public private fooze", TParser.r33)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'fooze'
            )


    def testR34a(self):
        r = self.parse("public class gnurz { fooze fooze2 }", TParser.r34)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(class gnurz public fooze fooze2)'
            )


    def testR34b(self):
        r = self.parse("public class gnurz extends bool implements int, bool { fooze fooze2 }", TParser.r34)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(class gnurz public (extends bool) (implements int bool) fooze fooze2)'
            )


    def testR35(self):
        try:
            r = self.parse("{ extends }", TParser.r35)
            self.fail()
            
        except RuntimeError:
            pass


    def testR36a(self):
        r = self.parse("if ( 1 + 2 ) fooze", TParser.r36)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(if (EXPR (+ 1 2)) fooze)'
            )


    def testR36b(self):
        r = self.parse("if ( 1 + 2 ) fooze else fooze2", TParser.r36)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(if (EXPR (+ 1 2)) fooze fooze2)'
            )


    def testR37(self):
        r = self.parse("1 + 2 + 3", TParser.r37)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(+ (+ 1 2) 3)'
            )


    def testR38(self):
        r = self.parse("1 + 2 + 3", TParser.r38)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(+ (+ 1 2) 3)'
            )


    def testR39a(self):
        r = self.parse("gnurz[1]", TParser.r39)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(INDEX gnurz 1)'
            )


    def testR39b(self):
        r = self.parse("gnurz(2)", TParser.r39)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(CALL gnurz 2)'
            )


    def testR39c(self):
        r = self.parse("gnurz.gnarz", TParser.r39)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(FIELDACCESS gnurz gnarz)'
            )


    def testR39d(self):
        r = self.parse("gnurz.gnarz.gnorz", TParser.r39)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(FIELDACCESS (FIELDACCESS gnurz gnarz) gnorz)'
            )


    def testR40(self):
        r = self.parse("1 + 2 + 3;", TParser.r40)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(+ 1 2 3)'
            )


    def testR41(self):
        r = self.parse("1 + 2 + 3;", TParser.r41)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(3 (2 1))'
            )


    def testR42(self):
        r = self.parse("gnurz, gnarz, gnorz", TParser.r42)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'gnurz gnarz gnorz'
            )


    def testR43(self):
        r = self.parse("gnurz, gnarz, gnorz", TParser.r43)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'nil'
            )
        self.failUnlessEqual(
            r.res,
            ['gnurz', 'gnarz', 'gnorz']
            )


    def testR44(self):
        r = self.parse("gnurz, gnarz, gnorz", TParser.r44)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(gnorz (gnarz gnurz))'
            )


    def testR45(self):
        r = self.parse("gnurz", TParser.r45)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'gnurz'
            )


    def testR46(self):
        r = self.parse("gnurz, gnarz, gnorz", TParser.r46)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'nil'
            )
        self.failUnlessEqual(
            r.res,
            ['gnurz', 'gnarz', 'gnorz']
            )


    def testR47(self):
        r = self.parse("gnurz, gnarz, gnorz", TParser.r47)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'gnurz gnarz gnorz'
            )


    def testR48(self):
        r = self.parse("gnurz, gnarz, gnorz", TParser.r48)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            'gnurz gnarz gnorz'
            )


    def testR49(self):
        r = self.parse("gnurz gnorz", TParser.r49)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(gnurz gnorz)'
            )


    def testR50(self):
        r = self.parse("gnurz", TParser.r50)
        self.failUnlessEqual(
            r.tree.toStringTree(),
            '(1.0 gnurz)'
            )


##     def testA(self):
##         r = self.parse("gnurz gnarz", TParser.a)
##         self.failUnlessEqual(
##             r.tree.toStringTree(),
##             'gnurz gnarz gnorz'
##             )


##     def testB(self):
##         r = self.parse("gnurz gnarz", TParser.b)
##         self.failUnlessEqual(
##             r.tree.toStringTree(),
##             'gnurz gnarz gnorz'
##             )


if __name__ == '__main__':
    unittest.main()

