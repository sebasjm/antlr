use Test::More qw( no_plan );

use Data::Dumper;

use strict;
use warnings;

BEGIN { use_ok( 'ANTLR::Runtime::ANTLRStringStream' ); }
require_ok( 'ANTLR::Runtime::ANTLRStringStream' );

my $s;
$s = ANTLR::Runtime::ANTLRStringStream->new('ABC');
$s->consume();

$s = ANTLR::Runtime::ANTLRStringStream->new('ABC');
is($s->LA(0), undef);
is($s->LA(1), 'A');
is($s->LA(2), 'B');
is($s->LA(3), 'C');
is($s->LA(4), ANTLR::Runtime::ANTLRStringStream->EOF);
