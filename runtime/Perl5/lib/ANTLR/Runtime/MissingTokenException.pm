package ANTLR::Runtime::MissingTokenException;

use strict;
use warnings;

use overload
    '""' => \&to_string;

use Object::InsideOut qw( ANTLR::Runtime::MismatchedTokenException );

my @inserted :Field :Accessor(Name => 'inserted', Private => 1, lvalue => 1);

sub init :Init {
    my ($self, $arg_ref) = @_;
    my $inserted = $arg_ref->{inserted};
    $self->inserted = $inserted;
}

sub get_missing_type {
    my ($self) = @_;
    return $self->expecting;
}

sub to_string {
    my ($self) = @_;

    if (defined (my $inserted = $self->inserted) && defined (my $token = $self->token)) {
        return "MissingTokenException(inserted $inserted at " . $token->get_text() . ")";
    }
    if (defined $self->token) {
        return "MissingTokenException(at " . $self->token->get_text() . ")";
    }

    return "MissingTokenException";
}

1;
__END__
