package ANTLR::Runtime::NoViableAltException;
use base qw( ANTLR::Runtime::RecognitionException );

use ANTLR::Runtime::Class;

use strict;
use warnings;

ANTLR::Runtime::Class::create_attributes(__PACKAGE__, [
    qw( grammar_decision_description decision_number state_number )
]);

sub new {
    my ($class, $grammar_decision_description, $decision_number, $state_number, $input) = @_;

    my $self = $class->SUPER::new($input);
    $self->grammar_decision_description($grammar_decision_description);
    $self->decision_number($decision_number);
    $self->state_number($state_number);

    return $self;
}

1;
