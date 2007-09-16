package ANTLR::Runtime::Class;

use strict;
use warnings;

sub create_accessor {
    my ($class, $name, $mode) = @_;

    if ($mode =~ /r/) {
        ## no critic (TestingAndDebugging::ProhibitNoStrict)
        no strict 'refs';
        *{"${class}::get_$name"} = sub {
            my ($self) = @_;
            return $self->{$name};
        };
    }

    if ($mode =~ /w/) {
        ## no critic (TestingAndDebugging::ProhibitNoStrict)
        no strict 'refs';
        *{"${class}::set_$name"} = sub {
            my ($self, $value) = @_;
            return $self->{$name} = $value;
        };
    }
}

sub create_accessors {
    my ($class, $accessors) = @_;
    while (my ($name, $mode) = each %$accessors) {
        create_accessor($class, $name, $mode);
    }
}


sub create_attribute {
    my ($class, $name) = @_;

    ## no critic (TestingAndDebugging::ProhibitNoStrict)
    no strict 'refs';
    *{"${class}::$name"} = sub {
        if (@_ == 1) {
            my ($self) = @_;
            return $self->{$name};
        } elsif (@_ == 2) {
            my ($self, $value) = @_;
            return $self->{$name} = $value;
        }
    };
}

sub create_attributes {
    my ($class, $attributes) = @_;
    foreach my $attribute (@$attributes) {
        create_attribute($class, $attribute);
    }
}

sub new {
    my ($class) = @_;

    my $self = bless {}, $class;
    return $self;
}


1;
