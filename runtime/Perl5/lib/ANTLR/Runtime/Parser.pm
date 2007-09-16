package ANTLR::Runtime::Parser;
use base qw( ANTLR::Runtime::BaseRecognizer );

use Readonly;
use Carp;
use ANTLR::Runtime::Class;

use strict;
use warnings;

ANTLR::Runtime::Class::create_attributes(__PACKAGE__, [
    qw( input )
]);

sub new {
    my ($class, $input) = @_;

    my $self = $class->SUPER::new();

    $self->set_token_stream($input);

    return $self;
}

sub reset {
    Readonly my $usage => 'void reset()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    $self->SUPER::reset();  #  reset all recognizer state variables
    if (defined $self->input) {
        $self->input->seek(0);  # rewind the input
    }
}

sub set_token_stream {
    Readonly my $usage => 'void set_token_stream(TokenStream input)';
    croak $usage if @_ != 2;
    my ($self, $input) = @_;

    $self->input(undef);
    $self->reset();
    $self->input($input);
}

sub get_token_stream {
    Readonly my $usage => 'TokenStream get_token_stream()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    return $self->input;
}

sub trace_in {
    Readonly my $usage => 'void trace_in(String rule_name, int rule_index)';
    croak $usage if @_ != 3;
    my ($self, $rule_name, $rule_index) = @_;

    $self->SUPER::trace_in($rule_name, $rule_index, $self->input->LT(1));
}

sub trace_out {
    Readonly my $usage => 'void trace_out(String rule_name, int rule_index)';
    croak $usage if @_ != 3;
    my ($self, $rule_name, $rule_index) = @_;

    $self->SUPER::trace_out($rule_name, $rule_index, $self->input->LT(1));
}

1;
