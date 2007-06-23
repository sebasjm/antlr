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
   
   def test_single_return_value
       grammar = <<-END
         s returns [a]
           : ('a')+ { 
               $a = 1 
           };
     END

 	  parser = Grammar::compile(grammar, "s")
     assert_equal 1, parser.parse('a');
   end

   def test_multiple_return_values
       grammar = <<-END
         s returns [a, b]
           : ('a')+ { 
               $a = 1 
               $b = 2
           };
     END

 	  parser = Grammar::compile(grammar, "s")
     assert_equal ({:a => 1, :b => 2}), parser.parse('a');
   end
   
    def test_single_param
        grammar = <<-END

          s returns [result]
          @init { @out = nil }
            : a[1] 
            { $result = @result }
            ;

          a[p1] : 'a' { @result = $p1 + 1 }
            ;
      END

  	  parser = Grammar::compile(grammar, "s")

      assert_equal 2, parser.parse('a');
    end
     
    def test_multiple_params
        grammar = <<-END
          s returns [result]
          @init { @out = nil }
            : a[1, 2, 3] 
            { $result = @result }
            ;

          a[p1, p2, p3] : 'a' { @result = $p1 + $p2 + $p3 }
            ;
      END

  	  parser = Grammar::compile(grammar, "s")

      assert_equal 6, parser.parse('a');
    end
    
    def test_rule_ref_with_single_retval
        grammar = <<-END
          s returns [result]
            : r=a[1, 2] { $result = $r.result }
            ;

          a[p1, p2] returns [result]: 'a' { $result = $p1 + $p2 }
            ;
      END

  	  parser = Grammar::compile(grammar, "s")

      assert_equal 3, parser.parse('a');
    end

    def test_rule_ref_with_multiple_retvals
        grammar = <<-END
          s returns [result]
            : r=a[1, 2] { $result = $r.r1 + $r.r2 }
            ;

          a[p1, p2] returns [r1, r2]: 'a' { 
            $r1 = $p1 
            $r2 = $p2
          }
          ;
      END

  	  parser = Grammar::compile(grammar, "s")

      assert_equal 3, parser.parse('a');
    end
end
