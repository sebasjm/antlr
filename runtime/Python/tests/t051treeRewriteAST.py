import unittest
import textwrap
import antlr3
import antlr3.tree
import testbase

class T(testbase.ANTLRTest):
    def walkerClass(self, base):
        class TWalker(base):
            def __init__(self, *args, **kwargs):
                base.__init__(self, *args, **kwargs)
                self.buf = ""

            def traceIn(self, ruleName, ruleIndex):
                self.traces.append('>'+ruleName)


            def traceOut(self, ruleName, ruleIndex):
                self.traces.append('<'+ruleName)


            def recover(self, input, re):
                # no error recovery yet, just crash!
                raise
            
        return TWalker
    

    def execTreeParser(self, grammar, grammarEntry, treeGrammar, treeEntry, input):
        lexerCls, parserCls = self.compileInlineGrammar(grammar)
        walkerCls = self.compileInlineGrammar(treeGrammar)

        cStream = antlr3.StringStream(input)
        lexer = lexerCls(cStream)
        tStream = antlr3.CommonTokenStream(lexer)
        parser = parserCls(tStream)
        r = getattr(parser, grammarEntry)()
        nodes = antlr3.tree.CommonTreeNodeStream(r.tree)
        nodes.setTokenStream(tStream)
        walker = walkerCls(nodes)
        r = getattr(walker, treeEntry)()

        if r.tree is not None:
            return r.tree.toStringTree()

        return ""
    

    def testFlatList(self):
        grammar = textwrap.dedent(
        r'''
        grammar T1;
        options {
            language=Python;
            output=AST;
        }
        a : ID INT;
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP1;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T1;
        }
        
        a : ID INT -> INT ID;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "abc 34"
            )

        self.failUnlessEqual("34 abc", found)


    def testSimpleTree(self):
        grammar = textwrap.dedent(
        r'''
        grammar T2;
        options {
            language=Python;
            output=AST;
        }
        a : ID INT -> ^(ID INT);
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP2;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T2;
        }
        a : ^(ID INT) -> ^(INT ID);
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "abc 34"
            )

        self.failUnlessEqual("(34 abc)", found)


    def testCombinedRewriteAndAuto(self):
        grammar = textwrap.dedent(
        r'''
        grammar T3;
        options {
            language=Python;
            output=AST;
        }
        a : ID INT -> ^(ID INT) | INT ;
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP3;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T3;
        }
        a : ^(ID INT) -> ^(INT ID) | INT;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "abc 34"
            )

        self.failUnlessEqual("(34 abc)", found)


        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "34"
            )

        self.failUnlessEqual("34", found)


    def testAvoidDup(self):
        grammar = textwrap.dedent(
        r'''
        grammar T4;
        options {
            language=Python;
            output=AST;
        }
        a : ID ;
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP4;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T4;
        }
        a : ID -> ^(ID ID);
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "abc"
            )

        self.failUnlessEqual("(abc abc)", found)


    def testLoop(self):
        grammar = textwrap.dedent(
        r'''
        grammar T5;
        options {
            language=Python;
            output=AST;
        }
        a : ID+ INT+ -> (^(ID INT))+ ;
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP5;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T5;
        }
        a : (^(ID INT))+ -> INT+ ID+;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "a b c 3 4 5"
            )

        self.failUnlessEqual("3 4 5 a b c", found)


    def testAutoDup(self):
        grammar = textwrap.dedent(
        r'''
        grammar T6;
        options {
            language=Python;
            output=AST;
        }
        a : ID ;
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP6;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T6;
        }
        a : ID;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "abc"
            )

        self.failUnlessEqual("abc", found)


    def testAutoDupRule(self):
        grammar = textwrap.dedent(
        r'''
        grammar T7;
        options {
            language=Python;
            output=AST;
        }
        a : ID INT ;
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP7;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T7;
        }
        a : b c ;
        b : ID ;
        c : INT ;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "a 1"
            )

        self.failUnlessEqual("a 1", found)


    def testAutoDupMultiple(self):
        grammar = textwrap.dedent(
        r'''
        grammar T8;
        options {
            language=Python;
            output=AST;
        }
        a : ID ID INT;
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP8;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T8;
        }
        a : ID ID INT
          ;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "a b 3"
            )

        self.failUnlessEqual("a b 3", found)


    def testAutoDupTree(self):
        grammar = textwrap.dedent(
        r'''
        grammar T9;
        options {
            language=Python;
            output=AST;
        }
        a : ID INT -> ^(ID INT);
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP9;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T9;
        }
        a : ^(ID INT)
          ;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "a 3"
            )

        self.failUnlessEqual("(a 3)", found)


    def testAutoDupTreeWithLabels(self):
        grammar = textwrap.dedent(
        r'''
        grammar T10;
        options {
            language=Python;
            output=AST;
        }
        a : ID INT -> ^(ID INT);
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP10;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T10;
        }
        a : ^(x=ID y=INT)
          ;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "a 3"
            )

        self.failUnlessEqual("(a 3)", found)


    def testAutoDupTreeWithListLabels(self):
        grammar = textwrap.dedent(
        r'''
        grammar T11;
        options {
            language=Python;
            output=AST;
        }
        a : ID INT -> ^(ID INT);
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP11;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T11;
        }
        a : ^(x+=ID y+=INT)
          ;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "a 3"
            )

        self.failUnlessEqual("(a 3)", found)


    def testAutoDupTreeWithRuleRoot(self):
        grammar = textwrap.dedent(
        r'''
        grammar T12;
        options {
            language=Python;
            output=AST;
        }
        a : ID INT -> ^(ID INT);
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP12;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T12;
        }
        a : ^(b INT) ;
        b : ID ;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "a 3"
            )

        self.failUnlessEqual("(a 3)", found)


    def testAutoDupTreeWithRuleRootAndLabels(self):
        grammar = textwrap.dedent(
        r'''
        grammar T13;
        options {
            language=Python;
            output=AST;
        }
        a : ID INT -> ^(ID INT);
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP13;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T13;
        }
        a : ^(x=b INT) ;
        b : ID ;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "a 3"
            )

        self.failUnlessEqual("(a 3)", found)


    def testAutoDupTreeWithRuleRootAndListLabels(self):
        grammar = textwrap.dedent(
        r'''
        grammar T14;
        options {
            language=Python;
            output=AST;
        }
        a : ID INT -> ^(ID INT);
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP14;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T14;
        }
        a : ^(x+=b y+=c) ;
        b : ID ;
        c : INT ;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "a 3"
            )

        self.failUnlessEqual("(a 3)", found)


    def testAutoDupNestedTree(self):
        grammar = textwrap.dedent(
        r'''
        grammar T15;
        options {
            language=Python;
            output=AST;
        }
        a : x=ID y=ID INT -> ^($x ^($y INT));
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP15;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T15;
        }
        a : ^(ID ^(ID INT))
          ;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "a b 3"
            )

        self.failUnlessEqual("(a (b 3))", found)


    def testDelete(self):
        grammar = textwrap.dedent(
        r'''
        grammar T16;
        options {
            language=Python;
            output=AST;
        }
        a : ID ;
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP16;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T16;
        }
        a : ID -> 
          ;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "abc"
            )

        self.failUnlessEqual("", found)


    ## REWRITE MODE

    def testRewriteModeCombinedRewriteAndAuto(self):
        grammar = textwrap.dedent(
        r'''
        grammar T17;
        options {
            language=Python;
            output=AST;
        }
        a : ID INT -> ^(ID INT) | INT ;
        ID : 'a'..'z'+ ;
        INT : '0'..'9'+;
        WS : (' '|'\\n') {$channel=HIDDEN;} ;
        ''')
        
        treeGrammar = textwrap.dedent(
        r'''
        tree grammar TP17;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T17;
            rewrite=true;
        }
        a : ^(ID INT) -> ^(ID["ick"] INT)
          | INT // leaves it alone, returning $a.start
          ;
        ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "abc 34"
            )

        self.failUnlessEqual("(ick 34)", found)


        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 'a',
            "34"
            )

        self.failUnlessEqual("34", found)


    def testRewriteModeFlatTree(self):
        grammar = textwrap.dedent(
            r'''
            grammar T18;
            options {
              language=Python;
              output=AST;
            }
            a : ID INT -> ID INT | INT ;
            ID : 'a'..'z'+ ;
            INT : '0'..'9'+;
            WS : (' '|'\n') {$channel=HIDDEN;} ;
            ''')
        
        # just checking that crash happens.  Can't replace child of flat tree
        treeGrammar = textwrap.dedent(
            r'''
            tree grammar TP18;
            options {
              language=Python;
              output=AST;
              ASTLabelType=CommonTree;
              tokenVocab=T18;
              rewrite=true;
            }
            s : ID a ;
            a : INT -> INT["1"]
              ;
            ''')
        
        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 's',
            "abc 34"
            )
        self.assertEquals("abc", found)


    def testRewriteModeWithPredicatedRewrites(self):
        grammar = textwrap.dedent(
            r'''
            grammar T19;
            options {
              language=Python;
              output=AST;
            }
            a : ID INT -> ^(ID["root"] ^(ID INT)) | INT -> ^(ID["root"] INT) ;
            ID : 'a'..'z'+ ;
            INT : '0'..'9'+;
            WS : (' '|'\n') {$channel=HIDDEN;} ;
            ''')

        treeGrammar = textwrap.dedent(
            r'''
            tree grammar TP19;
            options {
              language=Python;
              output=AST;
              ASTLabelType=CommonTree;
              tokenVocab=T19;
              rewrite=true;
            }
            s : ^(ID a) { self.buf += $s.start.toStringTree() };
            a : ^(ID INT) -> {True}? ^(ID["ick"] INT)
                          -> INT
              ;
            ''')

        found = self.execTreeParser(
            grammar, 'a',
            treeGrammar, 's',
            "abc 34"
            )
        
        self.assertEquals("(root (ick 34))", found)


if __name__ == '__main__':
    unittest.main()
