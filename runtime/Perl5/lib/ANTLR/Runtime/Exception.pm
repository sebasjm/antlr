package ANTLR::Runtime::Exception;

use strict;
use warnings;

use Exception::Class;
use base qw( Exception::Class::Base );

sub description {
    return 'ANTLR::Runtime Base Exception';
}

1;
