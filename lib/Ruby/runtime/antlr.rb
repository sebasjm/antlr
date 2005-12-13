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

class String
    def each_char(&block)
        each_byte(&block)
    end
end

module ANTLR
    class CharStream
        EOF = -1

        attr_reader :line
        attr_reader :column
        attr_reader :index

        def initialize(input)
            @buffer = ""
            @input = input
            @line = 1
            @column = 0

            @index = 0;
        end

        # returns a Fixnum between 0 and 0xFFFF or CharStream::EOF
        def look_ahead(pos)
            offset = @index + pos - 1
            if @buffer.length < offset + 1
                char = @input.read(offset + 1 - @buffer.length)
                @buffer << char if not char.nil?
            end

            if offset  < @buffer.length
                @buffer[offset]
            else
                EOF
            end
        end

        def mark
            @state = { :index => @index, :line => @line, :column => @column }
            return 0
        end

        def rewind(marker)
            @index = @state[:index]
            @line = @state[:line]
            @column = @state[:column]
        end

        def consume
           look_ahead(1) # force a read from the input if necessary
           @column = @column + 1
           if @buffer[@index] == ?\n
                @line = @line + 1
                @column = 0
           end
           @index = @index + 1
        end

        def substring(start, stop)
            @buffer.slice(start, stop - start + 1)
        end
    end

    class TokenStream
        attr_reader :index
        
        def initialize(input)
            @buffer = []
            @input = input
            @channel = Token::DEFAULT_CHANNEL

            @index = 0;
        end

        # returns a Token
        def look_ahead(pos)
            offset = @index + pos - 1

            while @buffer[-1] != Token::EOF && @buffer.length < offset + 1
                token = @input.next_token
                if token.channel == @channel || token == Token::EOF
                    @buffer << token
                end
            end

            offset = -1 if offset >= @buffer.length
            if offset < @buffer.length
                @buffer[offset]
            end
        end

        def mark
            @state = { :index => @index }
            return 0
        end

        def rewind(marker)
            @index = @state[:index]
        end

        def consume
           look_ahead(1) # force a read from the input if necessary
           @index = @index + 1
        end
    end


    class Token
        INVALID_TYPE = 0
        EOF = Token.new
        INVALID = Token.new
        DEFAULT_CHANNEL = 0

        attr_reader :token_type
        attr_reader :channel

        def initialize(token_type, channel, input, start, stop)
            @token_type = token_type
            @channel = channel
            @input = input
            @start = start
            @stop = stop
        end

        def line=(value)
            @line = value
        end

        def column=(value)
            @column = value
        end

        def to_s
            "[##{@token_type}, '#{text}', #{@line}:#{@column}, #{@start}, #{@stop}]"
        end

        def text
            @input.substring(@start, @stop)
        end

        def EOF.to_s
            "[EOF]"
        end

        def EOF.token_type
            -1
        end

        def INVALID.to_s
            "[INVALID]"
        end

        def INVALID.token_type
            INVALID_TYPE
        end
    end

    class Lexer
        def initialize(input)
            @input = input
            @backtracking = 0
            @failed = false
            @ruleStack = []
        end

        def match_range(from, to)
            char = @input.look_ahead(1)
            if char < from || char > to
                if @backtracking > 0
                    @failed = true
                    return
                end
                mre = MismatchedRangeException.new(from, to, @input)
                #recover(mre);
                raise mre;
            end
            @input.consume()
            @failed = false
        end

        def match_string(str)
            str.each_char { |b|
                if @input.look_ahead(1) != b
                    if @backtracking > 0
                        @failed = true
                        return
                    end
                    mte = MismatchedTokenException.new(b, @input)
                    #recover(mte);
                    raise mte
                end
                @input.consume()
                @failed = false
            }
        end

        def match(char)
            if @input.look_ahead(1) != char
                if @backtracking > 0
                    @failed = true
                    return
                end
                mte = MismatchedTokenException.new(char, @input)
                #recover(mte)
                raise mte
            end
            @input.consume
            @failed = false
        end

        def match_any
            @input.consume
        end

        def synpred(fragment)
            start = @input.mark
            @backtracking = @backtracking + 1
            begin
                send(fragment)
            rescue RecognitionException
                puts "impossible"
            end
            @input.rewind(start)
            @backtracking = @backtracking - 1;

            success = !@failed;
            @failed = false;
            return @success
        end

        def report_error(exception)
            STDERR << "[" + @ruleStack.join(",") + "]: " if !@ruleStack.empty?

            input = exception.input
            line = input.line
            column = input.column
            char = convert(input.look_ahead(1))

            STDERR << case exception
                when EarlyExitException
                    "required (...)+ loop (decision=#{exception.decisionNumber}) did not match anything; on line #{line}:#{column} char='#{char}'"
                when MismatchedTokenException
                    "mismatched char: '#{char}' on line #{line}; expecting char '#{convert(exception.expecting)}'"
                when MismatchedRangeException
                    "mismatched char: '#{char}' on line #{line}:#{column}; expecting set '#{convert(exception.from)}'..'#{convert(exception.to)}'"
                when NoViableAltException
                    "#{exception.description} state #{exception.stateNumber} (decision=#{exception.decisionNumber}) no viable alt line #{line}:#{column}; char='#{char}'"
                when FailedPredicateException
                    "rule #{exception.rule} failed predicate: {#{exception.predicate}}?"
                when MismatchedSetException
                    "mismatched char: '#{char}' on line #{line}:#{column}; expecting set #{exception.expecting}"
                else
                    raise exception
            end << "\n"
        end

        def convert(char)
            case char
                when CharStream::EOF: 'EOF'
                when ?\n: '\n'
                when ?\t: '\t'
                else char.chr
            end
        end
    end

    class DFA
        def DFA.predict(input, start)
            mark = input.mark
            begin
                state = start;
                while true
                    state = state.call(input)
                    return 1 if state.nil? # problem; nothing predicted.  Choose alt 1
                    return state if state.is_a? Fixnum # don't consume atom (then exit) if alt predicted
                    input.consume
                end
            ensure
                input.rewind(mark)
            end
        end
    end

    class Parser
        def initialize(input)
            @input = input
            @backtracking = 0
            @failed = false
            @errorRecovery = false
            @following = []
            @ruleStack = []
        end

        def match(tokenType, bitset)
            if @input.look_ahead(1).token_type == tokenType
                @input.consume
                @errorRecovery = false;
                @failed = false;
                return
            elsif @backtracking > 0
                @failed = true;
                return;
            else
                mte = MismatchedTokenException.new(tokenType, @input);
                #recoverFromMismatchedToken(input, mte, ttype, follow);

                raise mte
            end
        end

        def match_any
            @input.consume
        end

        def synpred(fragment)
            start = @input.mark
            @backtracking = @backtracking + 1
            begin
                send(fragment)
            rescue RecognitionException
                puts "impossible"
            end
            @input.rewind(start)
            @backtracking = @backtracking - 1

            success = !@failed
            @failed = false
            return success
        end

        def report_error(exception)
            return if @errorRecovery
            @errorRecovery = true

            STDERR << "[" + @ruleStack.join(",") + "]: "

            begin
                token = @input.look_ahead(1)
            rescue
                token = Token::INVALID
            end

            STDERR << case exception
                when MismatchedTokenException
                    "mismatched token: #{token_names[token.token_type]}; expecting type #{token_names[exception.expecting]}"
                when MismatchedSetException
                    "mismatched token: #{token_names[token.token_type]}; expecting set #{exception.expecting}"
                when NoViableAltException
                    "decision=<< #{exception.description}>> state #{exception.stateNumber} (decision=#{exception.decisionNumber}) no viable alt; token=#{token_names[token.token_type]}"
                when EarlyExitException
                    "required (...)+ loop (decision=#{exception.decisionNumber}) did not match anything; token=#{token_names[token.token_type]}"
                when FailedPredicateException
                    "rule #{exception.rule} failed predicate: {#{exception.predicate}}?"
                else
                    #raise exception
            end << "\n"
        end
    end

    #class BitSet
    #    def initialize(words)
    #        @value = 0
    #        words.each { |word|
    #            @value << 64
    #            @value = @value + word
    #        }
    #    end
    #end



    class RecognitionException < RuntimeError
        def initialize(input)
            @input = input
        end

        attr_reader :input
        attr :token
    end

    class EarlyExitException < RecognitionException
        def initialize(decisionNumber, input)
            super(input)
            @decisionNumber = decisionNumber
        end

        attr_reader :decisionNumber
    end

    class MismatchedTokenException < RecognitionException
        def initialize(expecting, input)
            super(input)
            @expecting = expecting
        end

        attr_reader :expecting
    end

    class MismatchedRangeException < RecognitionException
        def initialize(from, to, input)
            super(input)
            @from = from
            @to = to
        end

        attr_reader :from
        attr_reader :to
    end

    class NoViableAltException < RecognitionException
        def initialize(description, decisionNumber, stateNumber, input)
            super(input)
            @description = description
            @decisionNumber = decisionNumber
            @stateNumber = stateNumber
        end

        attr_reader :decisionNumber
        attr_reader :stateNumber
        attr_reader :description
    end

    class FailedPredicateException < RecognitionException
        def initialize(input, rule, predicate)
            super(input)
            @rule = rule
            @predicate = predicate
        end

        def to_s
            "FailedPredicateException(#{ruleName},{#{predicateText}}?)";
        end

        attr_reader :rule
        attr_reader :predicate
    end

    class MismatchedSetException < RecognitionException
        def initialize(expecting, input)
            super(input)
            @expecting = expecting
        end

        attr_reader :expecting
    end
end