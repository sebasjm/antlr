# [The "BSD licence"]
# Copyright (c) 2005 Martin Traverso
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
# 1. Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
# 3. The name of the author may not be used to endorse or promote products
#    derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
# IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
# OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
# IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
# INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
# NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
# THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

require 'test/unit'
require 'antlrtest'

class TestSyntacticPredicateEvaluation < Test::Unit::TestCase

    def test_two_preds_with_naked_alt
        grammar = <<-END
			grammar Foo;
			options {
			    language = Ruby;
			}


	        s : (a ';')+ ;

			a
			options {
			  k=1;
			}
			  : (b '.')=> b '.' { print "alt 1" }
			  | (b)=> b { print "alt 2" }
			  | c       { print "alt 3" }
			  ;

			b
			@init { print "enter b" }
			   : '(' 'x' ')' ;

			c
			@init { print "enter c" }
			   : '(' c ')' | 'x' ;

			WS : (' '|'\\n')+ { channel = 99 }
			   ;
	    END

	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "(x) ;");
	    assert_equal("enter benter benter balt 2", found);

	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "(x). ;");
	    assert_equal("enter benter balt 1", found);

	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "((x)) ;");
	    assert_equal("enter benter benter center center calt 3", found);
    end

    def test_two_preds_with_naked_alt_not_last
        grammar = <<-END
			grammar Foo;
			options {
			    language = Ruby;
			}


            s : (a ';')+ ;
			a
			options {
			  k=1;
			}
			  : (b '.')=> b '.' { print "alt 1" }
			  | c       { print "alt 2" }
			  | (b)=> b { print "alt 3" }
			  ;
			b
			@init { print "enter b" }
			   : '(' 'x' ')' ;
			c
			@init { print "enter c" }
			   : '(' c ')' | 'x' ;
			WS : (' '|'\\n')+ {channel=99}
			   ;
	    END

	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "(x) ;");
	    assert_equal("enter benter center calt 2", found);


	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "(x). ;");
	    assert_equal("enter benter balt 1", found);

	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "((x)) ;");
	    assert_equal("enter benter center center calt 2", found);
    end

    def test_lexer_pred
        grammar = <<-END
            grammar Foo;
            options {
                language = Ruby;
            }


            s : A ;
			A options {k=1;}
			  : (B '.')=>B '.' { print "alt1" }
			  | B { print "alt2" }
			  ;
			fragment
			B : 'x'+ ;
	    END

	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "s", "xxx");
	    assert_equal("alt2", found);

	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "s", "xxx.");
	    assert_equal("alt1", found);
    end
end