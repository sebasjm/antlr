package ANTLR::Runtime::ANTLRStringStream;
use base qw( ANTLR::Runtime::CharStream );

use ANTLR::Runtime::Class;
use Readonly;
use Carp;

use strict;
use warnings;

sub new {
    Readonly my $usage => 'ANTLRStringStream new($input)';
    croak $usage if @_ != 2;
    my ($class, $input) = @_;

    my $self = bless {}, $class;

    $self->{input} = $input;
    $self->{p} = 0;
    $self->{line} = 1;
    $self->{char_position_in_line} = 0;
    $self->{mark_depth} = 0;
    $self->{markers} = [];
    $self->{last_marker} = 0;

    return $self;
}

ANTLR::Runtime::Class::create_accessors(__PACKAGE__, {
    input => 'rw',
    p     => 'rw',
    line  => 'rw',
    char_position_in_line => 'rw',
    mark_depth => 'rw',
    markers => 'rw',
    last_marker => 'rw'
});

sub reset {
    Readonly my $usage => 'reset()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    $self->{p} = 0;
    $self->{line} = 1;
    $self->{char_position_in_line} = 0;
    $self->{mark_depth} = 0;
}

sub consume {
    Readonly my $usage => 'consume()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    if ($self->{p} < length $self->{input}) {
        ++$self->{char_position_in_line};
        if (substr($self->{input}, $self->{p}, 1) eq "\n") {
            ++$self->{line};
            $self->{char_position_in_line} = 0;
        }
        ++$self->{p};
    }
}

sub LA {
    Readonly my $usage => 'char LA($i)';
    croak $usage if @_ != 2;
    my ($self, $i) = @_;

    if ($i == 0) {
        return undef;
    }

    if ($i < 0) {
        ++$i; # e.g., translate LA(-1) to use offset i=0; then input[p+0-1]
        if ($self->{p} + $i - 1 < 0) {
            return $self->EOF;
        }
    }

    if ($self->{p} + $i - 1 >= length $self->{input}) {
        return $self->EOF;
    }

    return substr $self->{input}, $self->{p} + $i - 1, 1;
}

sub LT {
    Readonly my $usage => 'char LT($i)';
    croak $usage if @_ != 2;
    my ($self, $i) = @_;

    return $self->LA($i);
}

sub index {
    Readonly my $usage => 'int index()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    return $self->{p};
}

sub size {
    Readonly my $usage => 'int size()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    return length $self->{input};
}

sub mark {
    Readonly my $usage => 'int mark()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    ++$self->{mark_depth};
    my $state;
    if ($self->{mark_depth} >= @{$self->{markers}}) {
        $state = ANTLR::Runtime::CharStreamState->new();
        push @{$self->{markers}}, $state;
    } else {
        $state = $self->{markers}->get($self->{mark_depth});
    }

    $state->set_p($self->get_p);
    $state->set_line($self->get_line);
    $state->set_char_position_in_line($self->get_char_position_in_line);
    $self->set_last_marker($self->get_mark_depth);
    return $self->mark_depth;
}

sub rewind {
    Readonly my $usage => 'rewind($m)';
    croak $usage if @_ != 1 && @_ != 2;
    my ($self, $m);
    if (@_ == 1) {
        ($self, $m) = (@_, $self->get_last_marker);
    } else {
        ($self, $m) = @_;
    }

    my $state = $self->get_markers->get($m);
    # restore stream state
    $self->seek($state->get_p);
    $self->set_line($state->get_line);
    $self->set_char_position_in_line($state->get_char_position_in_line);
    $self->release($m);
}

sub release {
    Readonly my $usage => 'release($marker)';
    croak $usage if @_ != 2;
    my ($self, $marker) = @_;

    # unwind any other markers made after m and release m
    $self->set_mark_depth($marker);
    # release this marker
    $self->set_mark_depth($self->get_mark_depth - 1);
}

# consume() ahead unit p == index; can't just set p = index as we must update
# line and char_position_in_line
sub seek {
    Readonly my $usage => 'seek($index)';
    croak $usage if @_ != 2;
    my ($self, $index) = @_;

    if ($index <= $self->get_p) {
        # just jump; don't update stream state (line, ...)
        $self->set_p($index);
        return;
    }

    # seek forward, consume until p hits index
    while ($self->get_p < $index) {
        $self->consume();
    }
}

sub substring {
    Readonly my $usage => 'string substring($start, $stop)';
    croak $usage if @_ != 3;
    my ($self, $start, $stop) = @_;

    return substr $self->get_input, $start, $stop - $start + 1;
}

1;
