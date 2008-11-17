package ANTLR::Runtime::NoViableAltException;

use strict;
use warnings;

use Object::InsideOut qw( ANTLR::Runtime::RecognitionException );

my @grammar_decision_description :Field :Accessor(Name => 'grammar_decision_description', Private => 1, lvalue => 1);
my @decision_number :Field :Accessor(Name => 'decision_number', Private => 1, lvalue => 1);
my @state_number :Field :Accessor(Name => 'state_number', Private => 1, lvalue => 1);

#use ANTLR::Runtime::Class;
#ANTLR::Runtime::Class::create_attributes(__PACKAGE__, [
#    qw( grammar_decision_description decision_number state_number )
#]);

sub init :Init {
    my ($self, $arg_ref) = @_;
    my ($grammar_decision_description, $decision_number, $state_number, $input) =
        @$arg_ref{qw( grammar_decision_description decision_number state_number input )};

    $self->grammar_decision_description = $grammar_decision_description;
    $self->decision_number = $decision_number;
    $self->state_number = $state_number;
    return;
}

sub Xnew {
    my ($class, $grammar_decision_description, $decision_number, $state_number, $input) = @_;

    my $self = $class->SUPER::new($input);
    $self->grammar_decision_description($grammar_decision_description);
    $self->decision_number($decision_number);
    $self->state_number($state_number);

    return $self;
}

1;
