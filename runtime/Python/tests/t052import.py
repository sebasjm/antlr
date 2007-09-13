import unittest
import textwrap
import antlr3
import antlr3.tree
import testbase
import sys

class T(testbase.ANTLRTest):
    def parserClass(self, base):
        class TParser(base):
            def __init__(self, *args, **kwargs):
                base.__init__(self, *args, **kwargs)

                self._output = ""


            def capture(self, t):
                self._output += t


            def traceIn(self, ruleName, ruleIndex):
                self.traces.append('>'+ruleName)


            def traceOut(self, ruleName, ruleIndex):
                self.traces.append('<'+ruleName)


            def recover(self, input, re):
                # no error recovery yet, just crash!
                raise
            
        return TParser
    

    def lexerClass(self, base):
        class TLexer(base):
            def __init__(self, *args, **kwargs):
                base.__init__(self, *args, **kwargs)

                self._output = ""


            def capture(self, t):
                self._output += t


            def traceIn(self, ruleName, ruleIndex):
                self.traces.append('>'+ruleName)


            def traceOut(self, ruleName, ruleIndex):
                self.traces.append('<'+ruleName)


            def recover(self, input, re):
                # no error recovery yet, just crash!
                raise
            
        return TLexer
    

    def execParser(self, grammar, grammarEntry, slaves, input):
        for slave in slaves:
            parserName = self.writeInlineGrammar(slave)[0]
            # slave parsers are imported as normal python modules
            # to force reloading current version, purge module from sys.modules
            try:
                del sys.modules[parserName+'Parser']
            except KeyError:
                pass
                
        lexerCls, parserCls = self.compileInlineGrammar(grammar)

        cStream = antlr3.StringStream(input)
        lexer = lexerCls(cStream)
        tStream = antlr3.CommonTokenStream(lexer)
        parser = parserCls(tStream)
        getattr(parser, grammarEntry)()

        return parser._output
    

    def execLexer(self, grammar, slaves, input):
        for slave in slaves:
            parserName = self.writeInlineGrammar(slave)[0]
            # slave parsers are imported as normal python modules
            # to force reloading current version, purge module from sys.modules
            try:
                del sys.modules[parserName+'Parser']
            except KeyError:
                pass
                
        lexerCls = self.compileInlineGrammar(grammar)

        cStream = antlr3.StringStream(input)
        lexer = lexerCls(cStream)

        while True:
            token = lexer.nextToken()
            if token is None or token.type == antlr3.EOF:
                break

            lexer._output += token.text
            
        return lexer._output
    

    def testDelegatorInvokesDelegateRule(self):
        slave = textwrap.dedent(
        r'''
        parser grammar S;
        options {
            language=Python;
        }
        @members {
            def capture(self, t):
                self.gM.capture(t)

        }
        
        a : B { self.capture("S.a") } ;
        ''')

        master = textwrap.dedent(
        r'''
        grammar M;
        options {
            language=Python;
        }
        import S;
        s : a ;
        B : 'b' ; // defines B from inherited token space
        WS : (' '|'\n') {self.skip()} ;
        ''')

        found = self.execParser(
            master, 's',
            slaves=[slave],
            input="b"
            )

        self.failUnlessEqual("S.a", found)


    def testDelegatorInvokesDelegateRuleWithArgs(self):
        slave = textwrap.dedent(
        r'''
        parser grammar S;
        options {
            language=Python;
        }
        @members {
            def capture(self, t):
                self.gM.capture(t)
        }
        a[x] returns [y] : B {self.capture("S.a"); $y="1000";} ;
        ''')

        master = textwrap.dedent(
        r'''
        grammar M;
        options {
            language=Python;
        }
        import S;
        s : label=a[3] {self.capture($label.y);} ;
        B : 'b' ; // defines B from inherited token space
        WS : (' '|'\n') {self.skip()} ;
        ''')

        found = self.execParser(
            master, 's',
            slaves=[slave],
            input="b"
            )

        self.failUnlessEqual("S.a1000", found)


    def testDelegatorAccessesDelegateMembers(self):
        slave = textwrap.dedent(
        r'''
        parser grammar S;
        options {
            language=Python;
        }
        @members {
            def capture(self, t):
                self.gM.capture(t)

            def foo(self):
                self.capture("foo")
        }
        a : B ;
        ''')

        master = textwrap.dedent(
        r'''
        grammar M;        // uses no rules from the import
        options {
            language=Python;
        }
        import S;
        s : 'b' {self.gS.foo();} ; // gS is import pointer
        WS : (' '|'\n') {self.skip()} ;
        ''')

        found = self.execParser(
            master, 's',
            slaves=[slave],
            input="b"
            )

        self.failUnlessEqual("foo", found)


    def testDelegatorInvokesFirstVersionOfDelegateRule(self):
        slave = textwrap.dedent(
        r'''
        parser grammar S;
        options {
            language=Python;
        }
        @members {
            def capture(self, t):
                self.gM.capture(t)
        }
        a : b {self.capture("S.a");} ;
        b : B ;
        ''')

        slave2 = textwrap.dedent(
        r'''
        parser grammar T;
        options {
            language=Python;
        }
        @members {
            def capture(self, t):
                self.gM.capture(t)
        }
        a : B {self.capture("T.a");} ; // hidden by S.a
        ''')

        master = textwrap.dedent(
        r'''
        grammar M;
        options {
            language=Python;
        }
        import S,T;
        s : a ;
        B : 'b' ;
        WS : (' '|'\n') {self.skip()} ;
        ''')

        found = self.execParser(
            master, 's',
            slaves=[slave, slave2],
            input="b"
            )

        self.failUnlessEqual("S.a", found)


    def testDelegatesSeeSameTokenType(self):
        slave = textwrap.dedent(
        r'''
        parser grammar S; // A, B, C token type order
        options {
            language=Python;
        }
        tokens { A; B; C; }
        @members {
            def capture(self, t):
                self.gM.capture(t)
        }
        x : A {self.capture("S.x ");} ;
        ''')

        slave2 = textwrap.dedent(
        r'''
        parser grammar T;
        options {
            language=Python;
        }
        tokens { C; B; A; } /// reverse order
        @members {
            def capture(self, t):
                self.gM.capture(t)
        }
        y : A {self.capture("T.y");} ;
        ''')

        master = textwrap.dedent(
        r'''
        grammar M;
        options {
            language=Python;
        }
        import S,T;
        s : x y ; // matches AA, which should be "aa"
        B : 'b' ; // another order: B, A, C
        A : 'a' ;
        C : 'c' ;
        WS : (' '|'\n') {self.skip()} ;
        ''')

        found = self.execParser(
            master, 's',
            slaves=[slave, slave2],
            input="aa"
            )

        self.failUnlessEqual("S.x T.y", found)


    def testDelegatorRuleOverridesDelegate(self):
        slave = textwrap.dedent(
        r'''
        parser grammar S;
        options {
            language=Python;
        }
        @members {
            def capture(self, t):
                self.gM.capture(t)
        }
        a : b {self.capture("S.a");} ;
        b : B ;
        ''')

        master = textwrap.dedent(
        r'''
        grammar M;
        options {
            language=Python;
        }
        import S;
        b : 'b'|'c' ;
        WS : (' '|'\n') {self.skip()} ;
        ''')

        found = self.execParser(
            master, 'a',
            slaves=[slave],
            input="c"
            )

        self.failUnlessEqual("S.a", found)


    # LEXER INHERITANCE

    def testLexerDelegatorInvokesDelegateRule(self):
        slave = textwrap.dedent(
        r'''
        lexer grammar S;
        options {
            language=Python;
        }
        @members {
            def capture(self, t):
                self.gM.capture(t)
        }
        A : 'a' {self.capture("S.A ");} ;
        C : 'c' ;
        ''')

        master = textwrap.dedent(
        r'''
        lexer grammar M;
        options {
            language=Python;
        }
        import S;
        B : 'b' ;
        WS : (' '|'\n') {self.skip()} ;
        ''')

        found = self.execLexer(
            master,
            slaves=[slave],
            input="abc"
            )

        self.failUnlessEqual("S.A abc", found)


    def testLexerDelegatorRuleOverridesDelegate(self):
        slave = textwrap.dedent(
        r'''
        lexer grammar S;
        options {
            language=Python;
        }
        @members {
            def capture(self, t):
                self.gM.capture(t)
        }
        A : 'a' {self.capture("S.A");} ;
        ''')

        master = textwrap.dedent(
        r'''
        lexer grammar M;
        options {
            language=Python;
        }
        import S;
        A : 'a' {self.capture("M.A ");} ;
        WS : (' '|'\n') {self.skip()} ;
        ''')

        found = self.execLexer(
            master,
            slaves=[slave],
            input="a"
            )

        self.failUnlessEqual("M.A a", found)

        
if __name__ == '__main__':
    unittest.main()
