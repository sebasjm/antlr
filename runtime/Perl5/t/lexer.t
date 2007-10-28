use strict;
use warnings;

use FindBin;
use lib $FindBin::Bin;

use Test::More;
use ANTLR::Runtime::Test;

plan tests => 1;

g_test_output_is({ grammar => <<'GRAMMAR', test_program => <<'CODE', expected => <<'OUTPUT' });
lexer grammar ID;
options { language = Perl5; }

ID  :   ('a'..'z'|'A'..'Z')+ ;
INT :   '0'..'9'+ ;
NEWLINE:'\r'? '\n' { $self->skip(); } ;
WS  :   (' '|'\t')+ { $channel = HIDDEN; } ;
GRAMMAR
use English qw( -no_match_vars );
use ANTLR::Runtime::ANTLRStringStream;
use IDLexer;

use strict;
use warnings;

my $input = ANTLR::Runtime::ANTLRStringStream->new("Hello World!\n42\n");
my $lexer = IDLexer->new($input);

my $first_token = 1;
while (1) {
    my $token = $lexer->next_token();
    last if $token->get_type() == $IDLexer::EOF;

    if ($first_token) {
        $first_token = 0;
    }
    else {
        print "\n";
    }

    print "text: '", $token->get_text(), "'\n";
    print "type: ", $token->get_type(), "\n";
    print "pos: ", $token->get_line(), ':', $token->get_char_position_in_line(), "\n";
    print "channel: ", $token->get_channel(), "\n";
    print "token index: ", $token->get_token_index(), "\n";
}
CODE
text: 'Hello'
type: 4
pos: 1:0
channel: 0
token index: -1

text: ' '
type: 7
pos: 1:5
channel: 99
token index: -1

text: 'World'
type: 4
pos: 1:6
channel: 0
token index: -1

text: '42'
type: 5
pos: 2:0
channel: 0
token index: -1
OUTPUT
