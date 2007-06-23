require 'CalculatorParser'

STDIN.each_line do |line|
	line.chomp!
	exit if %w{ quit exit q }.include?(line)
	next if line == ""
	begin
		puts "=> #{CalculatorParser.new(line).evaluate}" 
	rescue
		puts "=> can't parse: #{line}"
	end
end
