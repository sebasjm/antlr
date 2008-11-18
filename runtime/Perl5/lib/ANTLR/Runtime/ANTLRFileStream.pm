package ANTLR::Runtime::ANTLRFileStream;

use strict;
use warnings;

use Readonly;
use Carp;

use ANTLR::Runtime::Class;

use base qw( ANTLR::Runtime::ANTLRStringStream );

ANTLR::Runtime::Class::create_attributes(__PACKAGE__, [
    qw( file_name )
]);

sub new {
    Readonly my $usage => 'ANTLRFileStream new(file_name, encoding)';
    croak $usage if @_ != 2;
    my ($class, $arg_ref) = @_;

    my $self = $class->SUPER::new();
    $self->file_name($arg_ref->{file_name});
    $self->load(@$arg_ref{qw( file_name encoding )});
    return $self;
}

sub load {
    my ($self, $file_name, $encoding) = @_;

    if (!defined $file_name) {
        return;
    }

    my $fh;
    if (defined $encoding) {
        open $fh, "<:encoding($encoding)", $file_name
            or croak "Can't open $file_name: $!";
    }
    else {
        open $fh, '<', $file_name
            or croak "Can't open $file_name: $!";
    }

    my $content;
    {
        local $/;
        $content = <$fh>;
    }
    $self->{input} = $content;
    return;
}

sub get_source_name {
    my ($self) = @_;
    return $self->file_name;
}

1;
__END__
