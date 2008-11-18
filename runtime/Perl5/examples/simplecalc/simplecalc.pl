#!perl

use strict;
use warnings;

use ANTLR::Runtime::ANTLRFileStream;
use ANTLR::Runtime::CommonTokenStream;
use ANTLR::Runtime::RecognitionException;
use SimpleCalcLexer;
use SimpleCalcParser;

my $input = ANTLR::Runtime::ANTLRFileStream->new({ file_name => $ARGV[0] });
my $lexer = SimpleCalcLexer->new($input);
my $tokens = ANTLR::Runtime::CommonTokenStream->new({ token_source => $lexer });
my $parser = SimpleCalcParser->new($tokens);
eval {
    $parser->expr();
};
if (my $ex = ANTLR::Runtime::RecognitionException->caught()) {
    print $ex->trace, "\n";
}
