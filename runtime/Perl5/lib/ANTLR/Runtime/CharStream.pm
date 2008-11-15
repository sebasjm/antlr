package ANTLR::Runtime::CharStream;

use strict;
use warnings;

use base qw( ANTLR::Runtime::IntStream );

use Readonly;
use Carp;

Readonly our $EOF => -1;
sub EOF { return $EOF; }

sub substring {
}

sub LT {
}

sub get_line {
}

sub set_line {
}

sub set_char_position_in_line {
}

sub get_char_position_in_line {
}

1;
