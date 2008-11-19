package ANTLR::Runtime::MismatchedTokenException;

use strict;
use warnings;

use Params::Validate qw( :types );
use ANTLR::Runtime::Token;

use overload
    '""' => \&to_string,
    'bool' => sub { 1 },
    fallback => 1;

use Object::InsideOut qw( ANTLR::Runtime::RecognitionException );

my @expecting :Field :Accessor(Name => 'expecting', Private => 1, lvalue => 1);

my %init_args :InitArgs = (
    'expecting' => {
    },
    'input' => {
        Type => 'ANTLR::Runtime::IntStream',
    },
);

sub init :Init {
    my $self = shift;
    my $param_ref = __PACKAGE__->unpack_params(\@_, {
        spec => [
            {
                name => 'expecting',
                type => SCALAR,
                optional => 1,
            },
            {
                name => 'input',
                isa => 'ANTLR::Runtime::IntStream',
                optional => 1,
            }
        ]
    });
    my $expecting = $param_ref->{expecting};
    my $input = $param_ref->{input};

    if (exists $param_ref->{expecting}) {
        $self->expecting = $expecting;
    }
    else {
        $self->expecting = ANTLR::Runtime::Token->INVALID_TOKEN_TYPE;
    }
}

sub get_expecting {
    my ($self) = @_;
    return $self->expecting;
}

sub to_string {
    my ($self) = @_;
    return "MismatchedTokenException(" . $self->get_unexpected_type() . "!=" . $self->expecting . ")";
}

1;
