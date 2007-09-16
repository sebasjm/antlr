package ANTLR::Runtime::Token;

use Readonly;
use ANTLR::Runtime::CommonToken;
use ANTLR::Runtime::CharStream;

use strict;
use warnings;

Readonly my $EOR_TOKEN_TYPE => 1;
sub EOR_TOKEN_TYPE { return $EOR_TOKEN_TYPE; }

# imaginary tree navigation type; traverse "get child" link
Readonly my $DOWN => 2;
sub DOWN { return $DOWN; }

# imaginary tree navigation type; finish with a child list
Readonly my $UP => 3;
sub UP { return $UP; }

Readonly my $MIN_TOKEN_TYPE => $UP + 1;
sub MIN_TOKEN_TYPE { return $MIN_TOKEN_TYPE; }

Readonly my $EOF => ANTLR::Runtime::CharStream->EOF;
sub EOF { return $EOF; }

Readonly my $EOF_TOKEN => ANTLR::Runtime::CommonToken->new({ type => $EOF });
sub EOF_TOKEN { return $EOF_TOKEN; }

Readonly my $INVALID_TOKEN_TYPE => 0;
sub INVALID_TOKEN_TYPE { return $INVALID_TOKEN_TYPE; }

Readonly my $INVALID_TOKEN => ANTLR::Runtime::CommonToken->new({ type => $INVALID_TOKEN_TYPE });
sub INVALID_TOKEN { return $INVALID_TOKEN; }

# In an action, a lexer rule can set token to this SKIP_TOKEN and ANTLR
# will avoid creating a token for this symbol and try to fetch another.
Readonly my $SKIP_TOKEN => ANTLR::Runtime::CommonToken->new({ type => $INVALID_TOKEN_TYPE });
sub SKIP_TOKEN { return $SKIP_TOKEN; }

# All tokens go to the parser (unless skip() is called in that rule)
# on a particular "channel".  The parser tunes to a particular channel
# so that whitespace etc... can go to the parser on a "hidden" channel.
Readonly my $DEFAULT_CHANNEL => 0;
sub DEFAULT_CHANNEL { return $DEFAULT_CHANNEL; }

# Anything on different channel than DEFAULT_CHANNEL is not parsed
# by parser.
Readonly my $HIDDEN_CHANNEL => 99;
sub HIDDEN_CHANNEL { return $HIDDEN_CHANNEL; }

ANTLR::Runtime::Class::create_accessors(__PACKAGE__, {
    # Get the text of the token
    text => 'rw',
    type => 'rw',
    # The line number on which this token was matched; line=1..n
    line  => 'rw',
    # The index of the first character relative to the beginning of the line 0..n-1
    char_position_in_line => 'rw',
    channel => 'rw',
    # An index from 0..n-1 of the token object in the input stream.
    #This must be valid in order to use the ANTLRWorks debugger.
    token_index => 'rw',
});

1;
