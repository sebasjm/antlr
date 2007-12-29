package ANTLR::Runtime::ANTLRFileStream;
use base qw( ANTLR::Runtime::ANTLRStringStream );

use ANTLR::Runtime::Class;
use Readonly;
use Carp;

use strict;
use warnings;

ANTLR::Runtime::Class::create_attributes(__PACKAGE__, [
    qw( file_name )
]);

sub new {
    Readonly my $usage => 'ANTLRFileStream new(file_name, encoding)';
    croak $usage if @_ != 2;
    my ($class, $args) = @_;

    my $self = $class->SUPER::new({ input => '' });

    $self->file_name($args->{file_name});

    return $self;
}

sub get_source_name {
    my ($self) = @_;

    return $self->file_name;
}

1;
