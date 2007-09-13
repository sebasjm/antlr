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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
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
        grammar T;
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
        tree grammar TP;
        options {
            language=Python;
            output=AST;
            ASTLabelType=CommonTree;
            tokenVocab=T;
            rewrite=true;
        }
        a : ^(ID INT) -> ^(INT ID) | INT
          ;
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

        self.failUnlessEqual("", found)


        

if __name__ == '__main__':
    unittest.main()
