package ANTLR::Runtime::RecognitionException;

use strict;
use warnings;

use Carp;
use Readonly;

use Object::InsideOut qw( ANTLR::Runtime::Exception );

my @input :Field :Accessor(Name => 'input', Private => 1, lvalue => 1);
my @index :Field :Accessor(Name => 'index', Private => 1, lvalue => 1);
my @token :Field :Accessor(Name => 'token', Private => 1, lvalue => 1);
my @node :Field :Accessor(Name => 'node', Private => 1, lvalue => 1);
my @c :Field :Accessor(Name => 'c', Private => 1, lvalue => 1);
my @line :Field :Accessor(Name => 'line', Private => 1, lvalue => 1);
my @char_position_in_line :Field :Accessor(Name => 'char_position_in_line', Private => 1, lvalue => 1);

sub init :Init {
    my ($self, $arg_ref) = @_;

    if ($arg_ref) {
        my $input = $arg_ref->{input};

        $self->input = $input;
        $self->index = $input->index();

        if ($input->isa('ANTLR::Runtime::TokenStream')) {
            $self->token = $input->LT(1);
            $self->line = $self->token->get_line();
            $self->char_position_in_line = $self->token->get_char_position_in_line();
        }
        if ($input->isa('ANTLR::Runtime::CommonTreeNodeStream')) {
            $self->node = $input->LT(1);
            if ($self->node->isa('ANTLR::Runtime::CommonTree')) {
                $self->token = $self->node->token;
                $self->line = $self->token->get_line();
                $self->char_position_in_line = $self->token->get_char_position_in_line();
            }

        } elsif ($input->isa('ANTLR::Runtime::CharStream')) {
            $self->c = $input->LA(1);
            $self->line = $input->get_line();
            $self->char_position_in_line = $input->get_char_position_in_line();
        } else {
            $self->c = $input->LA(1);
        }
    }
    else {
        $self->input = undef;
        $self->index = undef;
        $self->token = undef;
        $self->node = undef;
        $self->c = undef;
        $self->line = undef;
        $self->char_position_in_line = undef;
    }
    return;
}

sub Xnew {
    Readonly my $usage => 'RecognitionException new(IntStream input?)';
    if (@_ == 1) {
        my ($class) = @_;
        my $self = $class->SUPER::new();

        $self->input = undef;
        $self->index = undef;
        $self->token = undef;
        $self->node = undef;
        $self->c = undef;
        $self->line = undef;
        $self->char_position_in_line = undef;

        return $self;
    } elsif (@_ == 2) {
        my ($class, $arg_ref) = @_;
        my $input = $arg_ref->{input};
        my $self = $class->SUPER::new();

        $self->input = $input;
        $self->index = $input->index();

        if ($input->isa('ANTLR::Runtime::TokenStream')) {
            $self->token = $input->LT(1);
            $self->line = $self->token->get_line();
            $self->char_position_in_line = $self->token->get_char_position_in_line();
        }
        if ($input->isa('ANTLR::Runtime::CommonTreeNodeStream')) {
            $self->node = $input->LT(1);
            if ($self->node->isa('ANTLR::Runtime::CommonTree')) {
                $self->token = $self->node->token;
                $self->line = $self->token->get_line();
                $self->char_position_in_line = $self->token->get_char_position_in_line();
            }

        } elsif ($input->isa('ANTLR::Runtime::CharStream')) {
            $self->c = $input->LA(1);
            $self->line = $input->get_line();
            $self->char_position_in_line = $input->get_char_position_in_line();
        } else {
            $self->c = $input->LA(1);
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

sub get_c {
    my ($self) = @_;
    return $self->c;
}

sub get_line {
    my ($self) = @_;
    return $self->line;
}

sub get_char_position_in_line {
    my ($self) = @_;
    return $self->char_position_in_line;
}

sub get_token {
    my ($self) = @_;
    return $self->token;
}

1;
