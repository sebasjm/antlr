use strict;
use warnings;

use ExtUtils::MakeMaker;
use Test::More tests => 1;

my $file = 'lib/ANTLR/Runtime.pm';

like(MM->parse_version($file), qr/^\d+\.\d{2,}(_\d{2,})?$/);
