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
require 'antlr'

class TestParser < Test::Unit::TestCase

    def test_1
        grammar = <<-END
	        s returns [result]
	        @init { @out = "" }: a { $result = @out };

    			a: 'a' { @out << "a" }
    			  ;
	    END

		  parser = Grammar::compile(grammar, "s")

      assert_equal("a", parser.parse('a'))
    end

    def test_2
        grammar = <<-END
	        s returns [result]
	        @init { @out = "" }: a { $result = @out };

    			a:  'a' { @out << "a" }
    			  | 'b' { @out << "b" }
    			  ;
	    END

		  parser = Grammar::compile(grammar, "s")

      assert_equal("a", parser.parse('a'))
      assert_equal("b", parser.parse('b'))
    end
    
    def test_3
        grammar = <<-END
	        s returns [result]
	        @init { @out = "" }: a { $result = @out };

    			a:  ('a' { @out << "a" })?
    			  ;
	    END

		  parser = Grammar::compile(grammar, "s")

      assert_equal("a", parser.parse('a'))
      assert_equal("", parser.parse(''))
    end
    
    def test_4
        grammar = <<-END
	        s returns [result]
	        @init { @out = "" }: a { $result = @out };

    			a:  ('a' { @out << "a" })+
    			  ;
	    END

		  parser = Grammar::compile(grammar, "s")

      assert_equal("a", parser.parse('a'))
      assert_equal("aa", parser.parse('aa'))
      assert_equal("aaa", parser.parse('aaa'))
    end
    
    
    def test_5
        grammar = <<-END
	        s returns [result]
	        @init { @out = "" }: a { $result = @out };

    			a:  ('a' { @out << "a" })*
    			  ;
	    END

		  parser = Grammar::compile(grammar, "s")

      assert_equal("", parser.parse(''))
      assert_equal("a", parser.parse('a'))
      assert_equal("aa", parser.parse('aa'))
      assert_equal("aaa", parser.parse('aaa'))
    end

    
    def test_6
        grammar = <<-END
	        s returns [result]
	        @init { @out = "" }: a { $result = @out };

    			a:  b | c;
    			
    			b: 'b' { @out << "b" }
    			  ;
    			
    			c: 'c' { @out << "c" }
      			  ;
	    END

		  parser = Grammar::compile(grammar, "s")

      assert_equal("b", parser.parse('b'))
      assert_equal("c", parser.parse('c'))
    end

    def test_7
        grammar = <<-END
	        s returns [result]
	        @init { @out = "" }: a { $result = @out };

    			a:  b* c;
    			
    			b: 'b' { @out << "b" }
    			  ;
    			
    			c: 'c' { @out << "c" }
      			  ;
	    END

		  parser = Grammar::compile(grammar, "s")

      assert_equal("c", parser.parse('c'))
      assert_equal("bc", parser.parse('bc'))
      assert_equal("bbc", parser.parse('bbc'))
      assert_equal("bbbc", parser.parse('bbbc'))
    end

    def test_8
        grammar = <<-END
	        s returns [result]
	        @init { @out = "" }: a { $result = @out };

    			a:  b* c;
    			
    			b:  'b' { @out << "b" }
    			  | 'd' { @out << "d" } b;
    			
    			c: 'c' { @out << "c" }
      			  ;
	    END

		  parser = Grammar::compile(grammar, "s")

      assert_equal("c", parser.parse('c'))
      assert_equal("bc", parser.parse('bc'))
      assert_equal("dbc", parser.parse('dbc'))
    end


end
