package ANTLR::Runtime::BitSet;

use strict;
use warnings;

use Readonly;
use Carp;
use List::Util qw( max );
use ANTLR::Runtime::Class;

use Object::InsideOut qw( ANTLR::Runtime::Object );

use overload
    '|=' => \&or_in_place,
    '""' => \&str;

#ANTLR::Runtime::Class::create_attributes(__PACKAGE__, [
#   qw( bits )
#]);

our $BITS :Constant = 64;  # number of bits / long
our $LOG_BITS :Constant = 6;  # 2^6 == 64

# We will often need to do a mod operator (i mod nbits).  Its
# turns out that, for powers of two, this mod operation is
# same as (i & (nbits-1)).  Since mod is slow, we use a
# precomputed mod mask to do the mod instead.
our $MOD_MASK :Constant = BITS - 1;

# The actual data bit
my @bits :Field :Accessor(Name => 'bits', lvalue => 1);

sub _init :Init {
    my ($self, $args) = @_;

    my $bits;
    if (!%$args) {
        # Construct a bitset of size one word (64 bits)
        $bits = '0' x BITS;
    }
    elsif (exists $args->{bits}) {
        $bits = $args->{bits};
    }
    elsif (exists $args->{number}) {
        $bits = reverse unpack("B*", pack("N", $args->{number}));
    }
    elsif (exists $args->{words64}) {
        # Construction from a static array of longs
        my $words64 = $args->{words64};

        # $number is in hex format
        my $number = join '',
            map { my $word64 = $_; $word64 =~ s/^0x//; $word64; }
            reverse @$words64;

        $bits = '';
        foreach my $h (split //, reverse $number) {
            $bits .= reverse substr(unpack("B*", pack("h", hex $h)), 4);
        }
    }
    elsif (exists $args->{''}) {
       # Construction from a list of integers
    }
    elsif (exists $args->{size}) {
        # Construct a bitset given the size
        $bits = '0' x $args->{size};
    }
    else {
        croak "Invalid argument";
    }

    $self->bits($bits);
}

sub of {
    my ($class, $el) = @_;

    my $bs = ANTLR::Runtime::BitSet->new({ size => $el + 1 });
    $bs->add($el);

    return $bs;
}

sub or {
    my ($self, $a) = @_;

    if (!defined $a) {
        return $self;
    }

    my $s = $self->clone();
    $s->or_in_place($a);
    return $s;
}

sub add {
    my ($self, $el) = @_;

    $self->grow_to_include($el);
    substr($self->bits, $el, 1) = 1;

    return;
}

sub grow_to_include {
    my ($self, $bit) = @_;

    if ($bit > length $self->bits) {
        $self->bits .= '0' x ($bit - (length $self->bits) + 1);
    }

    return;
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

sub clone {
    my ($self) = @_;

    return ANTLR::Runtime::BitSet->new(bits => $self->bits);
}

sub size {
    my ($self) = @_;

    return scalar $self->bits =~ /1/;
}

sub equals {
    my ($self, $other) = @_;

    return $self->bits eq $other->bits;
}

sub member {
    Readonly my $usage => 'bool member($el)';
    croak $usage if @_ != 2;
    my ($self, $el) = @_;

    return (substr $self->bits, $el, 1) eq 1;
}

sub remove {
    my ($self, $el) = @_;

    substr($self->bits, $el, 1) = 0;
}

sub is_nil {
    my ($self) = @_;

    return $self->bits =~ /1/ ? 1 : 0;
}

sub num_bits {
    my ($self) = @_;
    return length $self->bits;
}

sub length_in_long_words {
    my ($self) = @_;
    return $self->num_bits() / $self->BITS;
}

sub to_array {
    my ($self) = @_;

    my $elems = [];

    while ($self->bits =~ /1/g) {
        push @$elems, $-[0];
    }
}

sub to_packed_array {
    my ($self) = @_;

    return [
        $self->bits =~ /.{$BITS}/g
    ];
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

__END__


=head1 NAME

ANTLR::Runtime::BitSet - A bit set


=head1 SYNOPSIS

    use <Module::Name>;
    # Brief but working code example(s) here showing the most common usage(s)

    # This section will be as far as many users bother reading
    # so make it as educational and exemplary as possible.


=head1 DESCRIPTION

A stripped-down version of org.antlr.misc.BitSet that is just good enough to
handle runtime requirements such as FOLLOW sets for automatic error recovery.


=head1 SUBROUTINES/METHODS

=over

=item C<of>

...

=item C<or>

Return this | a in a new set.

=item C<add>

Or this element into this set (grow as necessary to accommodate).

=item C<grow_to_include>

Grows the set to a larger number of bits.

=item C<set_size>

Sets the size of a set.

=item C<remove>

Remove this element from this set.

=item C<length_in_long_words>

Return how much space is being used by the bits array not how many actually
have member bits on.

=back

A separate section listing the public components of the module's interface.
These normally consist of either subroutines that may be exported, or methods
that may be called on objects belonging to the classes that the module provides.
Name the section accordingly.

In an object-oriented module, this section should begin with a sentence of the
form "An object of this class represents...", to give the reader a high-level
context to help them understand the methods that are subsequently described.


=head1 DIAGNOSTICS

A list of every error and warning message that the module can generate
(even the ones that will "never happen"), with a full explanation of each
problem, one or more likely causes, and any suggested remedies.
(See also "Documenting Errors" in Chapter 13.)


=head1 CONFIGURATION AND ENVIRONMENT

A full explanation of any configuration system(s) used by the module,
including the names and locations of any configuration files, and the
meaning of any environment variables or properties that can be set. These
descriptions must also include details of any configuration language used.
(See also "Configuration Files" in Chapter 19.)


=head1 DEPENDENCIES

A list of all the other modules that this module relies upon, including any
restrictions on versions, and an indication whether these required modules are
part of the standard Perl distribution, part of the module's distribution,
or must be installed separately.


=head1 INCOMPATIBILITIES

A list of any modules that this module cannot be used in conjunction with.
This may be due to name conflicts in the interface, or competition for
system or program resources, or due to internal limitations of Perl
(for example, many modules that use source code filters are mutually
incompatible).
