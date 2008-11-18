package ANTLR::Runtime::ANTLRStringStreamTest;

use strict;
use warnings;

use base qw( Test::Class );
use Test::More;

use ANTLR::Runtime::ANTLRStringStream;

sub test_new_string :Test(2) {
    my $s = ANTLR::Runtime::ANTLRStringStream->new({ input => 'ABC' });
    is ($s->LA(1), 'A');
    $s->consume();
    is ($s->LA(1), 'B');
}

sub test_LA :Test(5) {
    my $s = ANTLR::Runtime::ANTLRStringStream->new({ input => 'ABC' });
    is($s->LA(0), undef);
    is($s->LA(1), 'A');
    is($s->LA(2), 'B');
    is($s->LA(3), 'C');
    is($s->LA(4), ANTLR::Runtime::ANTLRStringStream->EOF);
}

1;
