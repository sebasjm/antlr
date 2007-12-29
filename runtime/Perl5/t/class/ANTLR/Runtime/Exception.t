use Test::More tests => 5;

use strict;
use warnings;

use Test::Exception;

BEGIN { use_ok( 'ANTLR::Runtime::Exception' ); }
require_ok( 'ANTLR::Runtime::Exception' );

{
    my $ex = ANTLR::Runtime::Exception->new();
    is $ex->message, '';
}

{
    my $ex = ANTLR::Runtime::Exception->new(message => 'test error message');
    is $ex->message, 'test error message';
}

{
    eval {
        ANTLR::Runtime::Exception->throw(message => 'test error message');
    };
    my $ex = ANTLR::Runtime::Exception->caught();
    is $ex->message, 'test error message';
}
