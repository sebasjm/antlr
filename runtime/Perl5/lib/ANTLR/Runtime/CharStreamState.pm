package ANTLR::Runtime::CharStreamState;

use strict;
use warnings;

use Object::InsideOut qw( ANTLR::Runtime::Object );

# Index into the char stream of next lookahead char
my @p :Field :Accessor(Name => 'p', lvalue => 1) :Standard(p);

# What line number is the scanner at before processing buffer[p]?
my @line :Field :Acessor(Name => 'line', lvalue => 1) :Standard(line);

# What char position 0..n-1 in line is scanner before processing buffer[p]?
my @char_position_in_line :Field :Accessor(Name => 'char_position_in_line', lvalue => 1) :Standard(char_position_in_line);

1;
