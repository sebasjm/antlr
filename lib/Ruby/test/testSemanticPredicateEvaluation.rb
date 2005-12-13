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

class TestSemanticPredicateEvaluation < Test::Unit::TestCase

    def test_simple_cyclic_DFA_with_predicate
        grammar = <<-END
			grammar Foo;
			options {
			    language = Ruby;
			}

			a : {false}? 'x'* 'y' {print "alt1"}
			  | {true}?  'x'* 'y' {print "alt2"}
			  ;
	    END

	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "xxxy")

	    assert_equal("alt2", found);
    end


    def test_simple_cyclic_DFA_with_instance_var_predicate
        grammar = <<-END
            grammar Foo;
            options {
                language = Ruby;
            }

			@members {
			    @v = true
			}

            a : {false}? 'x'* 'y' {print "alt1"}
              | {@v}?  'x'* 'y' {print "alt2"}
              ;
        END

        found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "xxxy")
        assert_equal("alt2", found);
    end


	def test_predicate_validation
	    grammar = <<-END
	        grammar Foo;
            options {
               language = Ruby;
            }

			@members {
			    def report_error(e)
			        print "error: FailedPredicateException(a,{false}?)"
			    end
			}

			a : {false}? 'x';
		END

        found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "x")
        assert_equal("error: FailedPredicateException(a,{false}?)", found);
	end

	def test_lexer_preds
        grammar = <<-END
            grammar Foo;
            options {
               language = Ruby;
            }

			@lexer::members {
                @p = false
    	    }

			a : (A|B)+ ;
			A : {@p}? 'a'  { print "token 1" } ;
			B : {!@p}? 'a' { print "token 2" } ;
		END

		found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "a")
        assert_equal("token 2", found);
    end

    def test_lexer_preds2
        grammar = <<-END
            grammar Foo;
            options {
               language = Ruby;
            }

            @lexer::members {
                @p = true
    	    }

			a : (A|B)+ ;
			A : {@p}? 'a' { print "token 1" } ;
			B : ('a'|'b')+ { print "token 2"} ;
        END

	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "a")
        assert_equal("token 1", found);
    end

    def test_lexer_pred_in_exit_branch
        grammar = <<-END
            grammar Foo;
            options {
               language = Ruby;
            }


            @lexer::members {
                @p = true
            }

			a : (A|B)+ ;
			A : ('a' { print "1" })*
			    {@p}?
			    ('a' { print "2" })* ;
	    END

	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "aaa")
        assert_equal("222", found);
    end


    def test_lexer_pred_in_exit_branch2
        grammar = <<-END
            grammar Foo;
            options {
               language = Ruby;
            }


            @lexer::members {
                @p = true
            }

			a : (A|B)+ ;
			A : ({@p}? 'a' { print "1" })*
			    ('a' { print "2" })* ;
	    END

	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "aaa")
        assert_equal("111", found);
    end


    def test_lexer_pred_in_exit_branch3
        grammar = <<-END
            grammar Foo;
            options {
               language = Ruby;
            }

            @lexer::members {
                @p = true
            }

			a : (A|B)+ ;
			A : ({@p}? 'a' { print "1" } | )
			    ('a' { print "2" })* ;
        END

        found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "aaa")
        assert_equal("122", found);
    end

    def test_lexer_pred_in_exit_branch4
        grammar = <<-END
            grammar Foo;
            options {
               language = Ruby;
            }

            a : (A|B)+ ;
			A @init { n = 0 } : ({ n < 2 }? 'a' { print n; n = n + 1})+
			    ('a' { print "x" })* ;
        END

        found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "aaaaa")
        assert_equal("01xxx", found);
    end

    def test_lexer_pred_in_cyclic_DFA
        grammar = <<-END
            grammar Foo;
            options {
               language = Ruby;
            }

			@lexer::members { @p = false }
            a : (A|B)+ ;
            A : {@p}? ('a')+ 'x'  { print "token 1" } ;
            B :      ('a')+ 'x' { print "token 2" };
        END

        found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "aax")
        assert_equal("token 2", found);
    end

    def test_lexer_pred_in_cyclic_DFA2
        grammar = <<-END
            grammar Foo;
            options {
               language = Ruby;
            }

            @lexer::members { @p = false }
            a : (A|B)+ ;
            A : {@p}? ('a')+ 'x' ('y')? { print "token 1" } ;
            B :      ('a')+ 'x' { print "token 2" } ;
        END

        found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "aax")
        assert_equal("token 2", found);
    end

    def test_gated_pred
        grammar = <<-END
            grammar Foo;
            options {
               language = Ruby;
            }

            a : (A|B)+ ;
			A : {true}?=> 'a' { print "token 1" } ;
			B : {false}?=>('a'|'b')+ { print "token 2" } ;
	    END

	    found = ANTLRTester.execParser(grammar, "FooLexer", "Foo", "a", "aa")
        assert_equal("token 1token 1", found);
    end
end