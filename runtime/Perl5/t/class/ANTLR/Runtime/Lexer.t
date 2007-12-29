use Test::More qw( no_plan );

use Data::Dumper;
use ANTLR::Runtime::ANTLRStringStream;

use strict;
use warnings;

BEGIN { use_ok( 'ANTLR::Runtime::Lexer' ); }
require_ok( 'ANTLR::Runtime::Lexer' );

my $input = ANTLR::Runtime::ANTLRStringStream->new('ABC');
my $lexer = ANTLR::Runtime::Lexer->new($input);

