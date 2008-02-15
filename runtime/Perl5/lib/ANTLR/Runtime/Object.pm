package ANTLR::Runtime::Object;

use strict;
use warnings;

use Object::InsideOut;

use Attribute::Handlers;

sub Constant :ATTR(BEGIN) {
    my ($package, $symbol, $referent, $attr, $data, $phase, $filename, $linenum) = @_;
    my $name = *{$symbol}{NAME};

    no strict 'refs';
    *{"${package}::$name"} = sub {
        return $$referent;
    };
}

1;
