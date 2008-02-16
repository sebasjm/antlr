package ANTLR::Runtime::Lexer::Test;

use strict;
use warnings;

use base qw( Test::Class );
use Test::More;

use ANTLR::Runtime::Lexer;

sub test_new_stream :Test() {
    my $input = ANTLR::Runtime::ANTLRStringStream->new('ABC');
    my $lexer = ANTLR::Runtime::Lexer->new($input);
}

1;
