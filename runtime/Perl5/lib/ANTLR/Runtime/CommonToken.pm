package ANTLR::Runtime::CommonToken;
use base qw( ANTLR::Runtime::Token );

use Readonly;

use strict;
use warnings;

ANTLR::Runtime::Class::create_attributes(__PACKAGE__, [
qw( type line char_position_in_line channel input text index start stop )
]);

sub new {
    Readonly my $usage => '__PACKAGE__ new($args)';
    croak $usage if @_ != 2;
    my ($class, $args) = @_;

    my $self = bless {}, $class;

    $self->{type} = undef;
    $self->{line} = undef;
    # set to invalid position
    $self->{char_position_in_line} = -1;
    $self->{channel} = $self->DEFAULT_CHANNEL;
    $self->{input} = undef;

    # We need to be able to change the text once in a while.  If
    # this is non-null, then getText should return this.  Note that
    # start/stop are not affected by changing this.
    $self->{text} = undef;

    # What token number is this from 0..n-1 tokens; < 0 implies invalid index
    $self->{index} = -1;

    # The char position into the input buffer where this token starts
    $self->{start} = undef;

    # The char position into the input buffer where this token stops
    $self->{stop} = undef;


    if (exists $args->{type}) {
        $self->{type} = $args->{type};
    }

    if (exists $args->{input}) {
        $self->{input} = $args->{input};
    }

    if (exists $args->{channel}) {
        $self->{channel} = $args->{channel};
    }

    if (exists $args->{start}) {
        $self->{start} = $args->{start};
    }

    if (exists $args->{stop}) {
        $self->{stop} = $args->{stop};
    }

    if (exists $args->{text}) {
        $self->{text} = $args->{text};
    }

    if (exists $args->{token}) {
        my $token = $args->{token};
        $self->{text} = $token->get_text();
        $self->{type} = $token->get_type();
        $self->{line} = $token->get_line();
        $self->{index} = $token->get_token_index();
        $self->{char_position_in_line} = $token->get_char_position_in_line();
        $self->{channel} = $token->get_channel();
    }

    return $self;
}

ANTLR::Runtime::Class::create_accessors(__PACKAGE__, {
    type => 'rw',
    line => 'rw',
    text => 'w',
    char_position_in_line => 'rw',
    channel => 'rw',
    input => 'rw',
    index => 'rw',
    start => 'rw',
    stop => 'rw',
});

sub get_text {
    Readonly my $usage => 'string get_text()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    if (defined $self->text) {
        return $self->text;
    }
    if (!defined $self->input) {
        return undef;
    }
    $self->text($self->input->substring($self->start, $self->stop));
    return $self->text;
}

sub get_start_index {
    my ($self) = @_;
    return $self->start;
}

sub set_start_index {
    my ($self, $start) = @_;
    $self->start($start);
}

sub get_stop_index {
    my ($self) = @_;
    return $self->stop;
}

sub set_stop_index {
    my ($self, $stop) = @_;
    $self->stop($stop);
}

sub get_token_index {
    my ($self) = @_;
    return $self->index;
}

sub set_token_index {
    my ($self, $index) = @_;
    $self->index($index);
}

=begin later

	public String toString() {
		String channelStr = "";
		if ( channel>0 ) {
			channelStr=",channel="+channel;
		}
		String txt = getText();
		if ( txt!=null ) {
			txt = txt.replaceAll("\n","\\\\n");
			txt = txt.replaceAll("\r","\\\\r");
			txt = txt.replaceAll("\t","\\\\t");
		}
		else {
			txt = "<no text>";
		}
		return "[@"+getTokenIndex()+","+start+":"+stop+"='"+txt+"',<"+type+">"+channelStr+","+line+":"+getCharPositionInLine()+"]";
	}

=end later

=cut


1;
