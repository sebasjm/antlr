package ANTLR::Runtime::Exception::Test;

use strict;
use warnings;

use base qw( Test::Class );
use Test::More;

use ANTLR::Runtime::Exception;

sub test_new_default :Test(1) {
    # pick any error
    $! = 1;
    my $expected = "$!";
    my $ex = ANTLR::Runtime::Exception->new();
    is $ex->message, $expected;
}

sub test_new_message :Test(1) {
    my $ex = ANTLR::Runtime::Exception->new(message => 'test error message');
    is $ex->message, 'test error message';
}

sub test_throw_message :Test(1) {
    eval {
        ANTLR::Runtime::Exception->throw(message => 'test error message');
    };
    my $ex = ANTLR::Runtime::Exception->caught();
    is $ex->message, 'test error message';
}

1;
