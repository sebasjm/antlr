package ANTLR::Runtime::BitSet;

use strict;
use warnings;

use Readonly;
use Carp;
use List::Util qw( max );
use ANTLR::Runtime::Class;

use overload
    '|=' => \&or_in_place,
    '""' => \&str;

ANTLR::Runtime::Class::create_attributes(__PACKAGE__, [
    qw( bits )
]);

sub new {
    Readonly my $usage => 'BitSet new()';
    if (@_ == 1) {
        my ($class) = @_;

        my $self = bless {}, $class;
        $self->bits('');

        return $self;
    } elsif (@_ == 2) {
        my ($class, $args) = @_;
        my $bits;
        if (exists $args->{bits}) {
            $bits = $args->{bits};
        } elsif (exists $args->{number}) {
            $bits = reverse unpack("B*", pack("N", $args->{number}));
        } elsif (exists $args->{words64}) {
            my $words64 = $args->{words64};

            # $number is in hex format
            my $number = join '', map { my $word64 = $_; $word64 =~ s/^0x//; $word64; } reverse @$words64;

            $bits = '';
            foreach my $h (split //, reverse $number) {
                $bits .= reverse substr(unpack("B*", pack("h", hex $h)), 4);
            }
        } elsif (exists $args->{size}) {
            $bits = '0' x $args->{size};
        } else {
            croak $usage;
        }

        my $self = bless {}, $class;
        $self->bits($bits);

        return $self;
    } else {
        croak $usage;
    }
}

sub of {
    my ($class, $el) = @_;

    my $bs = ANTLR::Runtime::BitSet->new({ size => $el + 1 });
    $bs->add($el);

    return $bs;
}

sub grow_to_include {
    my ($self, $bit) = @_;

    if ($bit > length $self->bits) {
        $self->{bits} .= '0' x ($bit - (length $self->bits) + 1);
    }

    return;
}

sub add {
    my ($self, $el) = @_;

    $self->grow_to_include($el);
    substr($self->{bits}, $el, 1) = 1;

    return;
}

sub remove {
    my ($self, $el) = @_;

    substr($self->{bits}, $el, 1) = 0;
}

sub member {
    Readonly my $usage => 'bool member($el)';
    croak $usage if @_ != 2;
    my ($self, $el) = @_;

    return (substr $self->{bits}, $el, 1) eq 1;
}

sub or_in_place {
    my ($self, $a) = @_;

    my $i = 0;
    foreach my $b (split //, $a->bits) {
        if ($b) {
            $self->add($i);
        }
    } continue {
        ++$i;
    }

    return $self;
}

sub str {
    my ($self) = @_;

    return $self->to_string();
}

sub to_string {
    my ($self, $args) = @_;

    my $token_names;
    if (defined $args && exists $args->{token_names}) {
        $token_names = $args->{token_names};
    }

    my @str;
    my $i = 0;
    foreach my $b (split //, $self->bits) {
        if ($b) {
            if (defined $token_names) {
                push @str, $token_names->[$i];
            } else {
                push @str, $i;
            }
        }
    } continue {
        ++$i;
    }

    return '{' . (join ',', @str) . '}';
}

1;
