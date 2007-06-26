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

class TestLexer < Test::Unit::TestCase

  def test_match_any
    parser = Grammar::compile("A: .;")
    
    assert_nothing_raised do
      assert_equal :A, parser.parse('a').first.token_type
    end
  end

  def test_match_char
    parser = Grammar::compile("A: 'a';")

    assert_nothing_raised do
      assert_equal :A, parser.parse('a').first.token_type
    end
    
    assert_raises(RuntimeError) { parser.parse('b') }
  end

  def test_match_string
    parser = Grammar::compile("A: 'abc';")

    assert_nothing_raised do 
      assert_equal :A, parser.parse('abc').first.token_type
    end
    
    assert_raises(RuntimeError) { parser.parse('x') }
    assert_raises(RuntimeError) { parser.parse('ab') }
  end
  
  def test_conditional
    parser = Grammar.compile("A: 'a'? 'bc';");
  
    assert_nothing_raised do
      assert_equal :A, parser.parse("abc").first.token_type
      assert_equal :A, parser.parse("bc").first.token_type
    end
    
    assert_raises(RuntimeError) { parser.parse('c') }
  end
  
  def test_positive_closure
    parser = Grammar.compile("A: 'a'+ 'bc';");
  
    assert_nothing_raised do
      assert_equal :A, parser.parse("aaabc").first.token_type
      assert_equal :A, parser.parse("aabc").first.token_type  
      assert_equal :A, parser.parse("abc").first.token_type
    end
    
    assert_raises(RuntimeError) { parser.parse('c') }
    assert_raises(RuntimeError) { parser.parse('bc') }
  end
  
  def test_closure
    parser = Grammar.compile("A: 'a'* 'bc';");
  
    assert_nothing_raised do
      assert_equal :A, parser.parse("aaabc").first.token_type
      assert_equal :A, parser.parse("aabc").first.token_type  
      assert_equal :A, parser.parse("abc").first.token_type
      assert_equal :A, parser.parse("bc").first.token_type
    end
    
    assert_raises(RuntimeError) { parser.parse('c') }
  end

  def test_hidden_channel
    parser = Grammar.compile("A: 'a' {$channel = :hidden};");
  
	 assert_equal :hidden, parser.parse("a").first.channel;
    
  end

  def test_line
    parser = Grammar.compile("A: 'a' {$line = 2};");

	 assert_equal 2, parser.parse("a").first.line;
  end

  def test_pos
    parser = Grammar.compile("A: 'a' {$pos = 2};");

	 assert_equal 2, parser.parse("a").first.pos;
  end

  def test_text
    parser = Grammar.compile("A: 'a' {$text='b'};");
  
	 assert_equal 'b', parser.parse("a").first.text;
  end  

  def test_match_any_with_label
    grammar = <<-END
      // #{__FILE__}:#{__LINE__}
      @lexer::members {
        attr_reader :a
      }
  
      @lexer::init {
        @a = nil
      }

      A : a=. { @a = $a };
    END

	  parser = Grammar::compile(grammar)

    assert_equal '0', parser.parse('0').first.text
    assert_equal ?0, parser.instance.a
  end
  
  def test_match_char_with_label
    grammar = <<-END
      // #{__FILE__}:#{__LINE__}
      @lexer::members {
        attr_reader :a
      }
  
      @lexer::init {
        @a = nil
      }

      A : a='a' { @a = $a };
    END

	  parser = Grammar::compile(grammar)

    assert_equal 'a', parser.parse('a').first.text
    assert_equal ?a, parser.instance.a
  end
  
  def test_match_range_with_label
    grammar = <<-END
      // #{__FILE__}:#{__LINE__}
      @lexer::members {
        attr_reader :a
      }
  
      @lexer::init {
        @a = nil
      }

      A : a='0'..'9' { @a = $a };
    END

	  parser = Grammar::compile(grammar)

    assert_equal '0', parser.parse('0').first.text
    assert_equal ?0, parser.instance.a
  end
  
  def test_match_string_with_label
    grammar = <<-END
      // #{__FILE__}:#{__LINE__}
      @lexer::members {
        attr_reader :a
      }
  
      @lexer::init {
        @a = nil
      }

      A : a='abc' { @a = $a };
    END

	  parser = Grammar::compile(grammar)

    assert_equal 'abc', parser.parse('abc').first.text
    assert_equal 'abc', parser.instance.a
  end
  
  def test_match_set_with_label
    grammar = <<-END
      // #{__FILE__}:#{__LINE__}
      @lexer::members {
        attr_reader :a
      }

      @lexer::init {
        @a = nil
      }

      A : a+=ID a+=ID { @a = $a };
      
      fragment
      ID: '0'|'1';
    END

	  parser = Grammar::compile(grammar)

    assert_equal '01', parser.parse('01').first.text
    assert_equal ['0', '1'], parser.instance.a.map { |t| t.text }
  end
  
  def test_lexer_rule_ref
    grammar = <<-END
      // #{__FILE__}:#{__LINE__}
      @lexer::members {
        attr_reader :a
      }
  
      @lexer::init {
        @a = nil
      }

      A : a=ID { @a = $a };
      
      fragment
      ID: '0';
    END

	  parser = Grammar::compile(grammar)

    assert_equal '0', parser.parse('0').first.text
    assert_equal '0', parser.instance.a.text
  end
  
end
