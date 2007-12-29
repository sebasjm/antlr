package ANTLR::Runtime::RecognitionException;

use strict;
use warnings;

use base qw( ANTLR::Runtime::Exception );

use Carp;
use Readonly;

use ANTLR::Runtime::Class;


ANTLR::Runtime::Class::create_attributes(__PACKAGE__, [
    qw( input index token node c line char_position_in_line )
]);

sub new {
    Readonly my $usage => 'RecognitionException new(IntStream input?)';
    if (@_ == 1) {
        my ($class) = @_;
        my $self = $class->SUPER::new();

        $self->input(undef);
        $self->index(undef);
        $self->token(undef);
        $self->node(undef);
        $self->c(undef);
        $self->line(undef);
        $self->char_position_in_line(undef);

        return $self;
    } elsif (@_ == 2) {
        my ($class, $input) = @_;
        my $self = $class->SUPER::new();

        $self->input($input);
        $self->index($input->index());

        if ($input->isa('ANTLR::Runtime::TokenStream')) {
            $self->token($input->LT(1));
            $self->line($self->token->get_line());
            $self->char_position_in_line($self->token->get_char_position_in_line());
        }
        if ($input->isa('ANTLR::Runtime::CommonTreeNodeStream')) {
            $self->node($input->LT(1));
            if ($self->node->isa('ANTLR::Runtime::CommonTree')) {
                $self->token($self->node->token);
                $self->line = $self->token->get_line();
                $self->char_position_in_line($self->token->get_char_position_in_line());
            }

        } elsif ($input->isa('ANTLR::Runtime::CharStream')) {
            $self->c($input->LA(1));
            $self->line($input->get_line());
            $self->char_position_in_line($input->get_char_position_in_line());
        } else {
            $self->c($input->LA(1));
        }

        return $self;
    } else {
        croak $usage;
    }
}

sub get_unexpected_type {
    Readonly my $usage => 'int get_unexpected_type()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    if ($self->input->isa('ANTLR::Runtime::TokenStream')) {
        return $self->token->get_type();
    } else {
        return $self->c;
    }
}

1;
