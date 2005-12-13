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

require 'tmpdir'
require 'tempfile'
require 'stringio'
require 'fileutils'

include FileUtils

class ANTLRTester
    def self.execParser(grammar, lexerName, parserName, startRule, input)
        tempfile = Tempfile.new("antlr")
        dirname = tempfile.path + ".dir"
        Dir.mkdir(dirname)

        cp("../runtime/antlr.rb", dirname)

        result = nil
        cd(dirname) do
            # write the grammar to a file
            File.open("grammar.g", "w") { |f| f.puts grammar }

            # run antlr
            `java -cp #{ENV['CLASSPATH']} org.antlr.Tool grammar.g`

            File.open("driver.rb", "w") do |f|
                f.puts <<-DRIVER
                    require '#{lexerName}'
                    require '#{parserName}'

                    charstream = ANTLR::CharStream.new(STDIN)
                    lexer = #{lexerName}.new(charstream)
                    tokenstream = ANTLR::TokenStream.new(lexer)
                    parser = #{parserName}.new(tokenstream)

                    parser.#{startRule}
                DRIVER
            end

            # run the test
            result = IO.popen("ruby driver.rb", "r+") do |pipe|
                pipe.print(input)
                pipe.close_write
                pipe.gets
            end

            # delete created files
            Dir.new(dirname).each { |file|
                File.delete(file) if file != ".." && file != "."
            }
        end

        Dir.delete(dirname)

        result
    end
end
