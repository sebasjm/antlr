package ANTLR::Runtime::Exception;

use strict;
use warnings;

use Exception::Class;
use Object::InsideOut qw( ANTLR::Runtime::Object Exception::Class::Base );

sub init :Init {
    my ($self, $arg_ref) = @_;

    my %base_args;
    if (defined (my $message = $arg_ref->{message})) {
        $base_args{message} = $message;
    }
    my $base = Exception::Class::Base->new(%base_args);
    $self->inherit($base);
    return;
}

sub description {
    return 'ANTLR::Runtime Base Exception';
}

1;
